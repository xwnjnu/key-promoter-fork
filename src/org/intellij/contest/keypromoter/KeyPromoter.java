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
    private Map<Class, Field> myClassFields = new HashMap<Class, Field>(5);
    private Class[] mySupportedClasses = new Class[]{ActionMenuItem.class, ActionButton.class};
    private Field myMenuItemDataContextField;
    private KeyPromoterSettings mySettings;

    public void initComponent() {
        listener = new MyAWTEventListener();
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
        private Logger logger = Logger.getInstance("org.intellij.contest.keypromoter.KeyPromoter");

        private Alarm myAlarm;
        private JLabel myTip;
        private int TIP_LAYER = TIP_LAYER = JLayeredPane.DRAG_LAYER + 1;

        public MyAWTEventListener() {
            myAlarm = new Alarm(Alarm.ThreadToUse.SHARED_THREAD);
        }

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
            if (myTip != null) {
                layeredPane.remove(myTip);
            }

            myTip = new TipLabel(text, mySettings);
            myTip.setLocation((int)(frame.getWidth() - myTip.getSize().getWidth()) / 2,
                    (int) (frame.getHeight() - myTip.getSize().getHeight() - 100));
            layeredPane.add(myTip, TIP_LAYER);

            long stepsCount = mySettings.getDisplayTime() / mySettings.getFlashAnimationDelay();

            // Alpha transparency decreased on each redraw by cycle
            myAlarm.addRequest(new RepaintRunnable(myTip, layeredPane, stepsCount), (int) mySettings.getFlashAnimationDelay());
        }

        private class RepaintRunnable implements Runnable {
            private final JLabel jLabel;
            private Container container;
            private long stepsCount;

            public RepaintRunnable(JLabel jLabel, Container layeredPane, long stepsCount) {
                this.jLabel = jLabel;
                this.container = layeredPane;
                this.stepsCount = stepsCount;
            }

            public void run() {
                jLabel.repaint();
                if (stepsCount-- > 0) {
                    myAlarm.addRequest(this, (int) mySettings.getFlashAnimationDelay());
                } else {
                    container.remove(jLabel);
                    container.repaint();
                }
            }
        }
    }
}
