package me.asu.actor.utils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import org.w3c.dom.NodeList;


public class Utils {

    public static boolean isNull(Object o) {
        return o == null;
    }

    /**
     * Test if array is null or empty.
     */
    public static boolean isEmpty(byte[] ba) {
        return ba == null || ba.length == 0;
    }

    /**
     * Test if array is null or empty.
     */
    public static boolean isEmpty(Object[] oa) {
        return oa == null || oa.length == 0;
    }

    /**
     * Test if string is null or empty.
     */
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    /**
     * Test if string is null or empty.
     */
    public static boolean isEmpty(Appendable s) {
        int length = -1;
        if (s != null) {
            Method                      m      = null;
            Class<? extends Appendable> sclass = s.getClass();
            try {
                m = sclass.getMethod("size");
            } catch (NoSuchMethodException e1) {
                try {
                    m = sclass.getMethod("length");
                } catch (NoSuchMethodException e2) {
                    try {
                        m = sclass.getMethod("getLength");
                    } catch (NoSuchMethodException e3) {
                    }
                }
            }
            if (m != null) {
                try {
                    length = (Integer) m.invoke(s);
                } catch (Exception e) {
                }
            }
        }
        return s == null || length == 0;
    }

    /**
     * Test if node list is null or empty.
     */
    public static boolean isEmpty(NodeList nl) {
        return nl == null || nl.getLength() == 0;
    }

    /**
     * Test if map is null or empty.
     */
    public static boolean isEmpty(Map<?, ?> m) {
        return m == null || m.size() == 0;
    }

    /**
     * Test if collection is null or empty.
     */
    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.size() == 0;
    }

    public static boolean isEmptyOrNull(String s) {
        return isEmpty(s) || s.trim().equalsIgnoreCase("null");
    }

    /**
     * Truncate a text string to 60 characters. Replace newlines with '~'.
     */
    public static String truncateText(String text) {
        return truncateText(text, 60);
    }

    /**
     * Truncate a text string to length characters. Replace newlines with '~'.
     */
    public static String truncateText(String text, int length) {
        if (!isEmpty(text)) {
            text = truncate(text, length);
            text = text.replace('\r', '~').replace('\n', '~');
        }
        return text;
    }

    /**
     * Truncate a text string to 100 characters.
     */
    public static String truncate(Object s) {
        return s != null ? truncate(s.toString(), 100) : "null";
    }

    /**
     * Remove repeated whitespace (including newlines)
     */
    public static String removeMultipleSpaces(String s) {
        return removeMultipleSpaces(s, " ");
    }

    /**
     * Remove repeated whitespace (including newlines)
     */
    public static String removeMultipleSpaces(String s, String replacement) {
        if (!isEmpty(s)) {
            s = s.replaceAll("\\s\\s+", replacement);
        }
        return s;
    }

    /**
     * Truncate a text string to size characters.
     */
    public static String truncate(String s, int size) {
        if (!isEmpty(s)) {
            s = removeMultipleSpaces(s);
            if (s.length() > size) {
                int leadLength = size / 2;
                int tailLength = size / 2;
                s = s.substring(0, leadLength) + " ... " + s.substring(
                        s.length() - tailLength);
            }
        }
        return s;
    }

    public static String camelCaseName(String name) {
        return isEmpty(name) ? ""
                : Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public static String capitalizeFirst(String s) {
        if (!isEmpty(s)) {
            s = Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
        return s;
    }

    /**
     * Safely implement sleep().
     */
    public static void sleep(long millis) {
        if (millis >= 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                // e.printStackTrace(System.out);
            }
        }
    }

}
