package org.intellij.contest.keypromoter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

/**
 * Date: 20.09.2006
 * Time: 16:47:02
 */
class TipLabel extends JLabel {
    private float myAlphaValue;
    private static final float ALPHA_STEP = 0.1f;
    private static final float START_ALPHA = 0f;
    private KeyPromoterSettings mySettings;

    public TipLabel(String text, KeyPromoterSettings mySettings) {
        super();
        this.mySettings = mySettings;
        myAlphaValue = 0f;
        setOpaque(false);
        init(text, mySettings);
    }

    public void init(String text, KeyPromoterSettings mySettings) {
        setText(text);
        setForeground(mySettings.getTextColor());
    }

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
        Color backgroundColor = mySettings.getBackgroundColor();
        g2d.setColor(new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 96));
        g2d.fillRect(3, 3, getWidth() - 6, getHeight() - 6);
        super.paintComponent(g);
    }

}
