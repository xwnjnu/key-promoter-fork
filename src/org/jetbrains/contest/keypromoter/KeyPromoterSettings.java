package org.jetbrains.contest.keypromoter;

import java.awt.*;

/**
 * Date: 11.09.2006
 * Time: 16:14:06
 */
public class KeyPromoterSettings {

    public Color textColor = Color.BLACK;
    public Color borderColor = Color.RED;
    public Color backgroundColor = new Color(0x202040);

    public boolean menusEnabled = true;
    public boolean toolbarButtonsEnabled = true;
    public boolean toolWindowButtonsEnabled = true;
    public boolean allButtonsEnabled = false;

    public long displayTime = 3000;
    public long flashAnimationDelay = 150;
    public long fontSize = 40;
    public boolean fixedTipPosistion = false;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final KeyPromoterSettings that = (KeyPromoterSettings) o;

        if (allButtonsEnabled != that.allButtonsEnabled) return false;
        if (displayTime != that.displayTime) return false;
        if (fontSize != that.fontSize) return false;
        if (flashAnimationDelay != that.flashAnimationDelay) return false;
        if (fixedTipPosistion != that.fixedTipPosistion) return false;
        if (menusEnabled != that.menusEnabled) return false;
        if (toolWindowButtonsEnabled != that.toolWindowButtonsEnabled) return false;
        if (toolbarButtonsEnabled != that.toolbarButtonsEnabled) return false;
        if (!backgroundColor.equals(that.backgroundColor)) return false;
        if (!borderColor.equals(that.borderColor)) return false;
        if (!textColor.equals(that.textColor)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (int) (displayTime ^ (displayTime >>> 32));
        result = 29 * result + (int) (flashAnimationDelay ^ (flashAnimationDelay >>> 32));
        result = 29 * result + (int) (fontSize ^ (fontSize >>> 32));
        result = 29 * result + (menusEnabled ? 1 : 0);
        result = 29 * result + (toolbarButtonsEnabled ? 1 : 0);
        result = 29 * result + (toolWindowButtonsEnabled ? 1 : 0);
        result = 29 * result + (allButtonsEnabled ? 1 : 0);
        result = 29 * result + textColor.hashCode();
        result = 29 * result + borderColor.hashCode();
        result = 29 * result + backgroundColor.hashCode();
        return result;
    }

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

    public long getFontSize() {
        return fontSize;
    }

    public void setFontSize(long fontSize) {
        this.fontSize = fontSize;
    }

    public boolean isFixedTipPosistion() {
        return this.fixedTipPosistion;
    }

    public void setFixedTipPosistion(boolean fixedTipPosistion) {
        this.fixedTipPosistion = fixedTipPosistion;
    }
}
