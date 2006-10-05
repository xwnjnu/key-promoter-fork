package org.jetbrains.contest.keypromoter;

import com.intellij.openapi.util.text.StringUtil;

import java.lang.reflect.Field;
import java.text.MessageFormat;

/**
 * Date: 05.10.2006
 * Time: 15:01:47
 */
public class KeyPromoterUtils {
    // Popup template
    static String template = "<html>\n" +
            "<body>\n" +
            " <table>\n" +
            "  <tr>\n" +
            "   <td align=\"center\"><font size=8>{0}</font></td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "   <td align=\"center\"><font size=6>{1}</font></td>\n" +
            "  </tr>\n" +
            " </table>\n" +
            "</body>\n" +
            "</html>";

    // Get first field of class with target type to use during click source handling
    public static Field getFieldOfType(Class<?> aClass, Class<?> targetClass) {
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

    public static String renderMessage(String description, String shortcutText, Integer count) {
        String text = MessageFormat.format(template,
                (StringUtil.isEmpty(description) ? shortcutText : shortcutText + " (" + description + ")"),
                count + " time(s)");
        return text;
    }
}
