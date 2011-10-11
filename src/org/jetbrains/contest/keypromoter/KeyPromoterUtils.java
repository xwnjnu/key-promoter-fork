package org.jetbrains.contest.keypromoter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.text.StringUtil;

import java.lang.reflect.Field;
import java.text.MessageFormat;

/**
 * Date: 05.10.2006
 * Time: 15:01:47
 */
public class KeyPromoterUtils {
    /**
     * Get first field of class with target type to use during click source handling.
     * @param aClass class to inspect
     * @param targetClass target class to check field to plug
     * @return
     */
    public static Field getFieldOfType(Class<?> aClass, Class<?> targetClass) {
        do {
            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (declaredField.getType().equals(targetClass)) {
                    declaredField.setAccessible(true);
                    return declaredField;
                }
            }
        } while ((aClass = aClass.getSuperclass()) != null);
        return null;
    }

    /**
     * Creates popup message body from template.
     * @param description action description
     * @param shortcutText key combination
     * @param count number of counted invocations
     * @return
     */
    public static String renderMessage(String description, String shortcutText, Integer count) {
        KeyPromoterConfiguration keyPromoterConfiguration = ApplicationManager.getApplication().getComponent(KeyPromoterConfiguration.class);
        KeyPromoterSettings settings = keyPromoterConfiguration.getSettings();
        String text = MessageFormat.format(settings.getPopupTemplate(),
                (StringUtil.isEmpty(description) ? shortcutText : shortcutText + " (" + description + ")"),
                count);
        return text;
    }
}
