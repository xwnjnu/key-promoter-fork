package org.intellij.contest.keypromoter;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.actionSystem.impl.ActionMenuItem;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.keymap.impl.ui.EditKeymapsDialog;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.impl.IdeFrame;
import com.intellij.openapi.wm.impl.StripeButton;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.Alarm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 04.09.2006
 * Time: 14:11:03
 */
public class KeyPromoter implements ApplicationComponent {

    private AWTEventListener listener;
    // Fields with actions of supported classes
    private Map<Class, Field> myClassFields = new HashMap<Class, Field>(5);
    // Supported classes which contains actions in fields
    private Class[] mySupportedClasses = new Class[]{ActionMenuItem.class, ActionButton.class};
    // DataContext field to get frame on Mac for example
    private Field myMenuItemDataContextField;
    private KeyPromoterSettings mySettings;
    // Alarm object to perform animation effects
    private Alarm myAlarm = new Alarm(Alarm.ThreadToUse.SHARED_THREAD);
    // Popup template
    private static String template = "<html>\n" +
            "<body>\n" +
            " <table>\n" +
            "  <tr>\n" +
            "   <td align=\"center\"><font size=8>{0}</font></td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "   <td align=\"center\"><font size=6>{1}</font></td>\n" +
            "  </tr>\n" +
            " </table>\n" +
            "</body>\n" +
            "</html>";

    public void initComponent() {
        listener = new MyAWTEventListener();
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
        KeyPromoterConfiguration component = ApplicationManager.getApplication().getComponent(KeyPromoterConfiguration.class);
        mySettings = component.getSettings();
        // HACK !!!
        // Some reflection used to get actions from mouse event sources
        for (int i = 0; i < mySupportedClasses.length; i++) {
            Class mySupportedClass = mySupportedClasses[i];
            Field actionField = getFieldOfType(mySupportedClass, AnAction.class);
            if (actionField != null) {
                myClassFields.put(mySupportedClass, actionField);
            }
        }
        // DataContext field to get frame on Mac for example
        myMenuItemDataContextField = getFieldOfType(ActionMenuItem.class, DataContext.class);
    }

    // Get first field of class with target type to use during click source handling
    private Field getFieldOfType(Class<?> aClass, Class<?> targetClass) {
        Field[] declaredFields = aClass.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Field declaredField = declaredFields[i];
            if (declaredField.getType().equals(targetClass)) {
                declaredField.setAccessible(true);
                return declaredField;
            }
        }
        return null;
    }

    public void disposeComponent() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
    }

    public String getComponentName() {
        return "KeyPromoter";
    }

    private class MyAWTEventListener implements AWTEventListener {

        private TipLabel myTip;
        private JWindow myTipWindow;
        private Map<String, Integer> stats = Collections.synchronizedMap(new HashMap<String, Integer>());
        private Map<String, Integer> withoutShortcutStats = Collections.synchronizedMap(new HashMap<String, Integer>());

        public void eventDispatched(AWTEvent e) {
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                final Object source = e.getSource();
                String shortcutText = "";
                String description = "";

                // Handle only menu and tool buttons clicks
                AnAction anAction = null;
                if (myClassFields.keySet().contains(source.getClass())) {
                    try {
                        if ((mySettings.isMenusEnabled() && source instanceof ActionMenuItem) ||
                                (mySettings.isToolbarButtonsEnabled() && source instanceof ActionButton)) {
                            anAction = (AnAction) myClassFields.get(source.getClass()).get(source);
                        }
                    } catch (IllegalAccessException e1) {
                        // it is bad but ...
                    }
                    if (anAction != null) {
                        shortcutText = KeymapUtil.getFirstKeyboardShortcutText(anAction);
                        description = anAction.getTemplatePresentation().getText();
                    }
                } else if (mySettings.isToolWindowButtonsEnabled() && source instanceof StripeButton) {
                    // This is hack!!!
                    char mnemonic = ((char) ((StripeButton) source).getMnemonic2());
                    if (mnemonic >= '0' && mnemonic <= '9') {
                        shortcutText = "Alt+" + mnemonic;
                        description = ((StripeButton) source).getText();
                    }
                } else if (mySettings.isAllButtonsEnabled() && source instanceof JButton) {
                    char mnemonic = ((char) ((JButton) source).getMnemonic());
                    if (mnemonic > 0) {
                        // Not respecting Mac Meta key yet
                        shortcutText = "Alt+" + mnemonic;
                        description = ((JButton) source).getText();
                    }
                }

                entertain(e, shortcutText, description, anAction);

            } else if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
                // To paint tip over dialogs
                if (myTipWindow != null && myTipWindow.isVisible()) {
                    myTipWindow.setVisible(false);
                    myTipWindow.setVisible(true);
                    myTipWindow.repaint();
                }
            }
        }

        private void entertain(final AWTEvent e, String shortcutText, String description, AnAction anAction) {
            Object source = e.getSource();
            JFrame frame = getFrame(source);
            if (!StringUtil.isEmpty(shortcutText)) {
                // Get current frame, not sure that it respects API
                if (frame != null) {
                    if (stats.get(shortcutText) == null) {
                        stats.put(shortcutText, 0);
                    }
                    stats.put(shortcutText, stats.get(shortcutText) + 1);

                    // Write shortcut to the brain card

                    showTip(frame, e, renderMessage(description, shortcutText));
                }
            } else {
                if (anAction != null) {
                    String id = ActionManager.getInstance().getId(anAction);
                    if (id != null) {
                        if (withoutShortcutStats.get(id) == null) {
                            withoutShortcutStats.put(id, 0);
                        }
                        withoutShortcutStats.put(id, withoutShortcutStats.get(id) + 1);
                        if (withoutShortcutStats.get(id) % 3 == 0) {
                            if (Messages.showYesNoDialog(frame, "Would you like to assign shortcut to '" + anAction.getTemplatePresentation().getDescription() + "'action cause we noticed it was used "+ withoutShortcutStats.get(id) + " time(s) by mouse?",
                                    "Keyboard usage more productive!", Messages.getQuestionIcon()) == 0) {
                                EditKeymapsDialog dialog = new EditKeymapsDialog(((IdeFrame) frame).getProject(), id);
                                dialog.show();
                            }
                        }
                    }
                }
            }
        }

        private JFrame getFrame(Object source) {
            JFrame frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, (Component) source);
            if (frame == null) {
                // On Mac menus detached from main frame :(
                if (source instanceof JMenuItem) {
                    try {
                        DataContext dataContext = (DataContext) myMenuItemDataContextField.get(source);
                        if (dataContext != null) {
                            Component component = (Component) dataContext.getData(DataConstants.CONTEXT_COMPONENT);
                            frame = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, component);
                        }
                    } catch (Exception e1) {
                        // it is bad but ...
                    }
                }
            }
            return frame;
        }

        private String renderMessage(String description, String shortcutText) {
            String text = MessageFormat.format(template,
                    (StringUtil.isEmpty(description) ? shortcutText : shortcutText + " (" + description + ")"),
                    stats.get(shortcutText) + " time(s)");
            return text;
        }

        private void showTip(JFrame frame, AWTEvent e, String text) {

            // Interrupt any pending requests
            myAlarm.cancelAllRequests();

            // Init tip if it is first run
            if (myTip == null) {
                myTipWindow = new JWindow(frame);
                myTipWindow.getContentPane().setLayout(new BorderLayout());
                myTip = new TipLabel(text, mySettings);
                myTipWindow.getRootPane().setOpaque(false);
                myTipWindow.add(myTip);
            } else {
                myTip.init(text, mySettings);
            }
            myTipWindow.pack();

            // If fixed posistion show at the bottom of screen, if not show close to the mouse click position
            if (mySettings.isFixedTipPosistion()) {
                myTipWindow.setLocation((int) (frame.getWidth() - myTip.getSize().getWidth()) / 2,
                        (int) (frame.getHeight() - myTip.getSize().getHeight() - 100));
            } else {
                Component source = (Component) e.getSource();
                // Trying fit to screen
                Point locationPoint = SwingUtilities.convertPoint(source,
                        new Point(source.getWidth() + 2, source.getHeight() + 2), frame);
                locationPoint.x = (int) Math.min(locationPoint.getX(), frame.getWidth() - myTip.getSize().getWidth());
                locationPoint.y = (int) Math.min(locationPoint.getY(), frame.getHeight() - myTip.getSize().getHeight());
                myTipWindow.setLocation(locationPoint);
            }
            myTipWindow.setVisible(true);

            long stepsCount = mySettings.getDisplayTime() / mySettings.getFlashAnimationDelay();

            // Alpha transparency decreased on each redraw by cycle
            myAlarm.addRequest(new RepaintRunnable(myTip, stepsCount), (int) mySettings.getFlashAnimationDelay());
        }

        // Repaints tip with periodically with ability to cancel
        private class RepaintRunnable implements Runnable {
            private final JLabel jLabel;
            private long stepsCount;

            public RepaintRunnable(JLabel jLabel, long stepsCount) {
                this.jLabel = jLabel;
                this.stepsCount = stepsCount;
            }

            public void run() {
                jLabel.repaint();
                if (stepsCount-- > 0) {
                    myAlarm.addRequest(this, (int) mySettings.getFlashAnimationDelay());
                } else {
                    myTipWindow.setVisible(false);
                }
            }
        }
    }
}
