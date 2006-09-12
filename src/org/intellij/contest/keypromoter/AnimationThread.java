package org.intellij.contest.keypromoter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Area;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.application.ApplicationManager;

/**
 * Date: 05.09.2006
 * Time: 16:33:05
 */
class AnimationThread extends Thread {

    private JFrame frame;
    private String description;
    private String shortcutText;
    private KeyPromoterSettings mySettings;

    public AnimationThread(JFrame frame, String description, String shortcutText) {
        super();
        KeyPromoterConfiguration component = ApplicationManager.getApplication().getComponent(KeyPromoterConfiguration.class);
        mySettings = component.getSettings();
        this.frame = frame;
        this.description = description;
        this.shortcutText = shortcutText;
    }

    public void run() {
        JLayeredPane layeredPane = frame.getLayeredPane();
        String text = StringUtil.isEmpty(description) ? shortcutText : shortcutText + " (" + description + ")";

        JLabel jLabel = new JLabel(text) {
            private float myAlphaValue = 0f;
            private static final float ALPHA_STEP = 0.1f;
            private static final float START_ALPHA = 0f;
            // some painting fun
            public void paintComponent(Graphics g) {
                // Cast to Graphics2D so we can set composite information.
                Graphics2D g2d = (Graphics2D)g;

                // Save the original composite.
                Composite oldComp = g2d.getComposite();

                // Create an AlphaComposite with 50% translucency.
                Composite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, myAlphaValue);
                // Increase or rotate
                if (myAlphaValue < 1 - ALPHA_STEP) {
                    myAlphaValue += ALPHA_STEP;
                } else {
                    myAlphaValue = START_ALPHA;
                }

                // Set the composite on the Graphics2D object.
                g2d.setColor(mySettings.getBorderColor());
                g2d.setComposite(alphaComp);

                Area border = new Area(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
                border.subtract(new Area(new Rectangle2D.Double(2, 2, getWidth() - 4, getHeight() - 4)));
                g2d.fill(border);

                // Restore the old composite.
                g2d.setComposite(oldComp);
                g2d.setColor(mySettings.getBackgroundColor());
                g2d.fillRect(3, 3, getWidth() - 6, getHeight() - 6);
                super.paintComponent(g);
            }
        };
        jLabel.setOpaque(false);
        jLabel.setFont(jLabel.getFont().deriveFont(mySettings.getFontSize()));
        jLabel.setForeground(mySettings.getTextColor());
        int textWidth = FontUtil.getTextWidth(jLabel.getFont(), text);
        int textHeight = FontUtil.getFontHeight(jLabel.getFont());
        jLabel.setSize(textWidth, textHeight);
        jLabel.setLocation((frame.getWidth() - textWidth) / 2, frame.getHeight() - textHeight - 100);
        layeredPane.add(jLabel, JLayeredPane.DRAG_LAYER);

        // Alpha transparency decreased on each redraw by cycle
        for (int i = 0; i < (mySettings.getDisplayTime() / mySettings.getFlashAnimationDelay()); i++) {
            try {
                jLabel.repaint();
                sleep(mySettings.getFlashAnimationDelay());
            } catch (InterruptedException e1) {
            }
        }
        layeredPane.remove(jLabel);
        layeredPane.repaint();
    }
}
