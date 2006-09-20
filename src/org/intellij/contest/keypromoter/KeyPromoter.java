package org.intellij.contest.keypromoter;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.actionSystem.impl.ActionMenuItem;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.impl.StripeButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.Alarm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.HashMap;

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
        // Some reflection to get actions
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

    // Get first field of class with target type
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
        private Logger logger = Logger.getInstance("org.intellij.contest.keypromoter.KeyPromoter");

        private TipLabel myTip;
        private int TIP_LAYER = TIP_LAYER = JLayeredPane.DRAG_LAYER + 1;

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
                        String text = StringUtil.isEmpty(description) ? shortcutText : shortcutText + " (" + description + ")";
                        showTip(frame, text);
                    }
                }

            }
        }

        private void showTip(JFrame frame, String text) {
            JLayeredPane layeredPane = frame.getLayeredPane();

            myAlarm.cancelAllRequests();
            if (myTip == null) {
                myTip = new TipLabel(text, mySettings);
                layeredPane.add(myTip, TIP_LAYER);
            } else {
                myTip.init(text, mySettings);
            }
            myTip.setLocation((int)(frame.getWidth() - myTip.getSize().getWidth()) / 2,
                    (int) (frame.getHeight() - myTip.getSize().getHeight() - 100));

            long stepsCount = mySettings.getDisplayTime() / mySettings.getFlashAnimationDelay();
            myTip.setVisible(true);
            // Alpha transparency decreased on each redraw by cycle
            myAlarm.addRequest(new RepaintRunnable(myTip, stepsCount), (int) mySettings.getFlashAnimationDelay());
        }

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
                    jLabel.setVisible(false);
                }
            }
        }
    }
}
