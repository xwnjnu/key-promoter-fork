package org.intellij.contest.keypromoter;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.actionSystem.impl.ActionMenuItem;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.impl.StripeButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.Alarm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;

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

    public void initComponent() {
        listener = new MouseAWTEventListener();
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK);
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

    private class MouseAWTEventListener implements AWTEventListener {

        private TipLabel myTip;
        private JWindow myTipWindow;
        private Map<String,Integer> stats = Collections.synchronizedMap(new HashMap<String, Integer>());

        public void eventDispatched(AWTEvent e) {
            if (e.getID() == MouseEvent.MOUSE_RELEASED) {
                Object source = e.getSource();
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
                        char mnemonic = ((char) ((StripeButton) source).getMnemonic2());
                        if (mnemonic >= '0' && mnemonic <= '9') {
                            shortcutText = "Alt+" + mnemonic;
                            description = ((StripeButton) source).getText();
                        }
                } else if (mySettings.isAllButtonsEnabled() && source instanceof JButton) {
                        char mnemonic = ((char) ((JButton) source).getMnemonic());
                        if (mnemonic > 0) {
                            shortcutText = "Alt+" + mnemonic;
                            description = ((JButton) source).getText();
                        }
                }

                entertain(e, shortcutText, description);

            }
        }

        private void entertain(final AWTEvent e, String shortcutText, String description) {
            Object source = e.getSource();
            if (!StringUtil.isEmpty(shortcutText)) {
                // Get current frame, not sure that it respects API
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
                if (frame != null) {
                    // Write shortcut to the brain card
                    if (stats.get(shortcutText) == null) {
                        stats.put(shortcutText, 0);
                    }
                    stats.put(shortcutText, stats.get(shortcutText)+1);

                    final String text = (StringUtil.isEmpty(description) ? shortcutText : shortcutText + " (" + description + ")") + " " + stats.get(shortcutText) + " time(s)";
                    final JFrame frame1 = frame;

                    // Due to delayed dialogs popups or blocking was choosen to set 1 second timeout to wait until action will be invoked and dialog shown if so
                    myAlarm.addRequest(new Runnable() {
                        public void run() {
                            showTip(frame1, e, text);
                        }
                    }, 1000);
                }
            }
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
            // If fixed posistion show at the bottom of screen, if not show close to the mouse click position
            if (mySettings.isFixedTipPosistion()) {
                myTipWindow.setLocation((int)(frame.getWidth() - myTip.getSize().getWidth()) / 2,
                        (int) (frame.getHeight() - myTip.getSize().getHeight() - 100));
            } else {
                Component source = (Component) e.getSource();
                myTipWindow.setLocation(SwingUtilities.convertPoint(source,
                        new Point(source.getWidth() + 2, source.getHeight() + 2), frame));
            }
            myTipWindow.pack();
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
