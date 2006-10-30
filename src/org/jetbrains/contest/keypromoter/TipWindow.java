package org.jetbrains.contest.keypromoter;

import com.intellij.openapi.application.ApplicationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

/**
 * Popup window with information about missed shortcut. Contains shortcut keys and number of invocations by mouse.
 * @author Dmitry Kashin
 */
public class TipWindow extends JWindow {

    public TipWindow(Frame owner, String text, Component sourceComponent) {
        super(owner);
        KeyPromoterConfiguration component = ApplicationManager.getApplication().getComponent(KeyPromoterConfiguration.class);
        KeyPromoterSettings mySettings = component.getSettings();

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(false);
        setContentPane(contentPane);
        TipLabel myTip = new TipLabel(text, mySettings);
        add(myTip);
        pack();
        // If fixed posistion show at the bottom of screen, if not show close to the mouse click position
        if (mySettings.isFixedTipPosistion()) {
            setLocation((int) (owner.getWidth() - myTip.getSize().getWidth()) / 2,
                    (int) (owner.getHeight() - myTip.getSize().getHeight() - 100));
        } else {
            // Trying fit to screen
            Point locationPoint = SwingUtilities.convertPoint(sourceComponent,
                    new Point(sourceComponent.getWidth() + 2, sourceComponent.getHeight() + 2), owner);
            locationPoint.x = (int) Math.min(locationPoint.getX(), owner.getWidth() - myTip.getSize().getWidth());
            locationPoint.y = (int) Math.min(locationPoint.getY(), owner.getHeight() - myTip.getSize().getHeight());
            setLocation(locationPoint);
        }

    }

    /**
     * Component for displaying tip with some simple animation.
     */
    class TipLabel extends JLabel {
        private float myAlphaValue;
        private static final float ALPHA_STEP = 0.1f;
        private static final float START_ALPHA = 0f;
        private KeyPromoterSettings mySettings;

        public TipLabel(String text, KeyPromoterSettings mySettings) {
            super();
            this.mySettings = mySettings;
            myAlphaValue = 0.5f;
            setOpaque(false);
            setText(text);
            setForeground(mySettings.getTextColor());
        }

        // some painting fun
        public void paintComponent(Graphics g) {
            // Cast to Graphics2D so we can set composite information.
            Graphics2D g2d = (Graphics2D)g;

            // Save the original composite.
            Composite oldComp = g2d.getComposite();

            // Create an AlphaComposite
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
            Color backgroundColor = mySettings.getBackgroundColor();
            g2d.setColor(new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 64));
            g2d.fillRect(3, 3, getWidth() - 6, getHeight() - 6);
            super.paintComponent(g);
        }

    }

}
