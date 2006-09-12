package org.intellij.contest.keypromoter;

import java.awt.*;

/**
 * Date: 05.09.2006
 * Time: 16:36:48
 */
public class FontUtil {
    public static FontMetrics getFontMetrics(Font font) {
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }

    public static int getTextWidth(Font font, String text) {
        return getFontMetrics(font).stringWidth(text);
    }

    public static int getFontHeight(Font font)
    {
        return getFontMetrics(font).getAscent() + getFontMetrics(font).getDescent();
    }
}
