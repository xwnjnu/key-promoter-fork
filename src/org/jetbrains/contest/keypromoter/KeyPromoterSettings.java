package org.jetbrains.contest.keypromoter;

import java.awt.*;

/**
 * Settings for KeyPromoter plugin.
 * @author Dmitry Kashin
 */
public class KeyPromoterSettings {

    /** Color of text in popup */
    public Color textColor = Color.BLACK;
    /** Color of border of popup */
    public Color borderColor = Color.RED;
    /** Background Color of popup */
    public Color backgroundColor = new Color(0x202040);

    /** Whether popup enabled or disabled on menus clicks. */
    public boolean menusEnabled = true;
    /** Whether popup enabled or disabled on toolbar buttons clicks. */
    public boolean toolbarButtonsEnabled = true;
    /** Whether popup enabled or disabled on toolwindow buttons clicks. */
    public boolean toolWindowButtonsEnabled = true;
    /** Whether popup enabled or disabled on all buttons with mnemonics clicks. */
    public boolean allButtonsEnabled = false;

    /** Time of popup display. */
    public long displayTime = 3000;
    /** Animation delay time. */
    public long flashAnimationDelay = 150;
    /** Count of invocations after which ask for creation of shortcut for actions without them. */
    public int proposeToCreateShortcutCount = 3;
    /** Popup position fixed or folow the mouse clicks. */
    public boolean fixedTipPosistion = false;
    /** Popup template. */
    public String popupTemplate = "<html>\n" +
            " <body>\n" +
            "  <table>\n" +
            "   <tr>\n" +
            "    <td align=\"center\"><font size=8>{0}</font></td>\n" +
            "   </tr>\n" +
            "   <tr>\n" +
            "    <td align=\"center\"><font size=6>{1} time(s)</font></td>\n" +
            "   </tr>\n" +
            "  </table>\n" +
            " </body>\n" +
            "</html>";

    public long getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(long displayTime) {
        this.displayTime = displayTime;
    }

    public long getFlashAnimationDelay() {
        return flashAnimationDelay;
    }

    public void setFlashAnimationDelay(long flashAnimationDelay) {
        this.flashAnimationDelay = flashAnimationDelay;
    }

    public boolean isMenusEnabled() {
        return menusEnabled;
    }

    public void setMenusEnabled(boolean menusEnabled) {
        this.menusEnabled = menusEnabled;
    }

    public boolean isToolbarButtonsEnabled() {
        return toolbarButtonsEnabled;
    }

    public void setToolbarButtonsEnabled(boolean toolbarButtonsEnabled) {
        this.toolbarButtonsEnabled = toolbarButtonsEnabled;
    }

    public boolean isToolWindowButtonsEnabled() {
        return toolWindowButtonsEnabled;
    }

    public void setToolWindowButtonsEnabled(boolean toolWindowButtonsEnabled) {
        this.toolWindowButtonsEnabled = toolWindowButtonsEnabled;
    }

    public boolean isAllButtonsEnabled() {
        return allButtonsEnabled;
    }

    public void setAllButtonsEnabled(boolean allButtonsEnabled) {
        this.allButtonsEnabled = allButtonsEnabled;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isFixedTipPosition() {
        return this.fixedTipPosistion;
    }

    public void setFixedTipPosistion(boolean fixedTipPosistion) {
        this.fixedTipPosistion = fixedTipPosistion;
    }

    public int getProposeToCreateShortcutCount() {
        return proposeToCreateShortcutCount;
    }

    public void setProposeToCreateShortcutCount(int proposeToCreateShortcutCount) {
        this.proposeToCreateShortcutCount = proposeToCreateShortcutCount;
    }

    public String getPopupTemplate() {
        return popupTemplate;
    }

    public void setPopupTemplate(String popupTemplate) {
        this.popupTemplate = popupTemplate;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyPromoterSettings that = (KeyPromoterSettings) o;

        if (allButtonsEnabled != that.allButtonsEnabled) return false;
        if (displayTime != that.displayTime) return false;
        if (fixedTipPosistion != that.fixedTipPosistion) return false;
        if (flashAnimationDelay != that.flashAnimationDelay) return false;
        if (menusEnabled != that.menusEnabled) return false;
        if (proposeToCreateShortcutCount != that.proposeToCreateShortcutCount) return false;
        if (toolWindowButtonsEnabled != that.toolWindowButtonsEnabled) return false;
        if (toolbarButtonsEnabled != that.toolbarButtonsEnabled) return false;
        if (backgroundColor != null ? !backgroundColor.equals(that.backgroundColor) : that.backgroundColor != null)
            return false;
        if (borderColor != null ? !borderColor.equals(that.borderColor) : that.borderColor != null) return false;
        if (popupTemplate != null ? !popupTemplate.equals(that.popupTemplate) : that.popupTemplate != null)
            return false;
        if (textColor != null ? !textColor.equals(that.textColor) : that.textColor != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (textColor != null ? textColor.hashCode() : 0);
        result = 31 * result + (borderColor != null ? borderColor.hashCode() : 0);
        result = 31 * result + (backgroundColor != null ? backgroundColor.hashCode() : 0);
        result = 31 * result + (menusEnabled ? 1 : 0);
        result = 31 * result + (toolbarButtonsEnabled ? 1 : 0);
        result = 31 * result + (toolWindowButtonsEnabled ? 1 : 0);
        result = 31 * result + (allButtonsEnabled ? 1 : 0);
        result = 31 * result + (int) (displayTime ^ (displayTime >>> 32));
        result = 31 * result + (int) (flashAnimationDelay ^ (flashAnimationDelay >>> 32));
        result = 31 * result + proposeToCreateShortcutCount;
        result = 31 * result + (fixedTipPosistion ? 1 : 0);
        result = 31 * result + (popupTemplate != null ? popupTemplate.hashCode() : 0);
        return result;
    }
}
