package me.asu.text;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;


public abstract class EncodingUtils {

    static class MynsICharsetDetectionObserver implements nsICharsetDetectionObserver {

        ArrayList<String> charsets = new ArrayList<String>();
        boolean found = false;

        public String[] getCharsets() {
            if (charsets.contains("windows-1252")) {
                charsets.remove("windows-1252");
            }
            return charsets.toArray(new String[0]);
        }

        public boolean isFound() {
            return found;
        }

        /*
         * (non-Javadoc)
         * @see
         * org.mozilla.intl.chardet.nsICharsetDetectionObserver#Notify(java.
         * lang.String)
         */
        @Override
        public void Notify(final String charset) {
            //System.err.println("get " + charset);
            found = true;
            if (charset.toUpperCase().contains("GB")) {
                charsets.add("GB18030");
            } else if (charset.toUpperCase().contains("BIG")) {
                charsets.add("BIG5HKSCS");
            } else if (charset.contains("1252")) {
                charsets.add("GB18030");
            } else {
                charsets.add(charset);
            }
        }
    }

    private static int connectTimeout = 10000;

    private static int readTimeout = 10000;

    /**
     * @since 1.0.0
     */
    public static String convert(final String text, final String to) {
        if (isEmpty(text)) {
            return text;
        }
        if (isEmpty(to)) {
            return text;
        } else {
            String sysencoding = System.getProperty("file.encoding");
            if (sysencoding != null && sysencoding.equalsIgnoreCase(to)) {
                return text;
            }
            byte[] data = text.getBytes();
            try {
                return new String(data, to);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new String(data);
            }
        }
    }

    public static boolean isEmpty(CharSequence cs) {
        return null == cs || cs.length() == 0;
    }

    /**
     * @since 1.0.0
     */
    public static String convert(final String text, final String from, final String to) {
        try {
            if (isEmpty(text)) {
                return text;
            }
            byte[] data = null;
            if (isEmpty(from)) {
                return convert(text, to);
            }
            if (from.equalsIgnoreCase(to)) {
                return text;
            }
            data = text.getBytes(from);
            if (isEmpty(to)) {
                return new String(data);
            } else {
                return new String(data, to);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return text;
        }

    }

    public static String decodigURIWithJavaEncoding(final String str) {
        return URLDecoder.decode(str);
    }

    public static String decodingURI(final String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return URLDecoder.decode(str);
        }
    }

    public static String decodingURI(final String str, final String encoding) {
        try {
            return URLDecoder.decode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * @see org.mozilla.intl.chardet.nsPSMDetector
     *
     * <pre>
     * 		1 => Japanese
     * 	2 => Chinese
     * 	3 => Simplified Chinese
     * 	4 => Traditional Chinese
     * 	5 => Korean
     * 	6 => Dont know (default)
     * 	null => all
     * </pre>
     * @since 1.0.0
     */
    public static String[] detectByContent(final byte data[], final int languageHint)
    throws Exception {
        if (data == null || data.length == 0) {
            return new String[]{Constants.CHARSET_US_ASCII};
        }

        if (isUTF8(data)) {
            return new String[]{Constants.CHARSET_UTF8};
        }

        if (isUTF16LEByOrderMark(data)) {
            return new String[]{Constants.CHARSET_UTF16LE};
        }

        if (isUTF16BEByOrderMark(data)) {
            return new String[]{Constants.CHARSET_UTF16BE};
        }

        // Initalize the nsDetector() ;
        int lang = languageHint > 0 ? languageHint : nsPSMDetector.ALL;
        nsDetector det = new nsDetector(lang);

        // Set an observer...
        // The Notify() will be called when a matching charset is found.
        MynsICharsetDetectionObserver charsetDetectionObserver = new MynsICharsetDetectionObserver();
        det.Init(charsetDetectionObserver);

        BufferedInputStream imp = new BufferedInputStream(new ByteArrayInputStream(data));

        byte[] buf = new byte[1024];
        int len;
        boolean done = false;
        boolean isAscii = true;

        while ((len = imp.read(buf, 0, buf.length)) != -1) {

            // Check if the stream is only ascii.
            if (isAscii) {
                isAscii = det.isAscii(buf, len);
            }

            // DoIt if non-ascii and not done yet.
            if (!isAscii && !done) {
                done = det.DoIt(buf, len, false);
            }
        }
        det.DataEnd();

        if (charsetDetectionObserver.isFound()) {
            String[] dectectEncodes = charsetDetectionObserver.getCharsets();
            return dectectEncodes;
        }

        if (isAscii) {
            return new String[]{Constants.CHARSET_US_ASCII};
        }

        return det.getProbableCharsets();

    }

    /**
     * @since 1.0.0
     */
    public static String detectByHtmlHeader(final String url) {
        return detectByHtmlHeader(url, connectTimeout, readTimeout, null);
    }

    /**
     * @since 1.0.0
     */
    public static String detectByHtmlHeader(final String url, final int ctimeout,
                                            final int rtimeout,
                                            final Map<String, String> requestHeaders) {
        HttpURLConnection conn = null;
        try {
            conn = getURL(url);
            if (conn == null) {
                return null;
            }
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(ctimeout);
            conn.setReadTimeout(rtimeout);
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            String contentType = conn.getContentType();
            String encoding = getEncodingFromContentType(contentType);
            return formatEncoding(encoding);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    public static String detectByHtmlKeyword(final byte[] data, final String oldEncoding) {
        if (data != null) {
            int chunk_size = 4096;
            int len = data.length < chunk_size ? data.length : chunk_size;
            byte[] newdata = new byte[len];
            System.arraycopy(data, 0, newdata, 0, len);
            String encoding = "";
            try {
                String htm = new String(newdata, "iso-8859-1");
                int encodeindex = htm.indexOf("charset=");
                if (encodeindex != -1) {
                    int start = encodeindex + 8; // 8 is length of "charset="
                    // skip white space character
                    for (int j = htm.length(); start < j; start++) {
                        char ch = htm.charAt(start);
                        if (!(Character.isWhitespace(ch) || ch == '\'' || ch == '\"')) {
                            break;
                        }
                    }
                    // find end
                    int end = start;
                    for (int j = htm.length(); end < j; end++) {
                        char ch = htm.charAt(end);
                        // if ('\"' == ch || '\'' == ch || '/' == ch || '>' ==
                        // ch
                        // || Character.isWhitespace(ch)) {
                        // break;

                        if (!(Character.isLetter(ch) || Character.isDigit(ch) || '-' == ch
                                || '_' == ch)) {
                            break;
                        }

                    }
                    encoding = htm.substring(start, end);
                    return formatEncoding(encoding);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return formatEncoding(oldEncoding);
    }

    public static String detectFromHtmlHeaders(final Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        String contentType = headers.get("Content-Type");
        String encoding = getEncodingFromContentType(contentType);
        return formatEncoding(encoding);
    }

    public static String encodigURI(final String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return URLEncoder.encode(str);
        }
    }

    public static String encodigURI(final String str, final String encoding) {
        try {
            return URLEncoder.encode(str, encoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String encodigURIWithJavaEncoding(final String str) {
        return URLEncoder.encode(str);
    }

    /**
     * java escape
     *
     * @since 1.0.0
     */
    public static String escape(final String src) {
        int i;
        char j;
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length() * 6);

        for (i = 0; i < src.length(); i++) {

            j = src.charAt(i);

            if (Character.isDigit(j) || Character.isLowerCase(j) || Character.isUpperCase(j)) {
                tmp.append(j);
            } else if (j < 256) {
                tmp.append("%");
                if (j < 16) {
                    tmp.append("0");
                }
                tmp.append(Integer.toString(j, 16));
            } else {
                tmp.append("%u");
                tmp.append(Integer.toString(j, 16));
            }
        }
        return tmp.toString();
    }

    public static String escapeCSV(final String str) {
        return "\"" + escapeString(str, '"', '"') + "\"";
    }

    public static String escapeString(final String str, final char escapeChar,
                                      final char charToEscape) {
        return escapeString(str, escapeChar, new char[]{charToEscape});
    }

    public static String escapeString(final String str, final char escapeChar,
                                      final char charsToEscape[]) {
        if (str == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char curChar = str.charAt(i);
            if (curChar == escapeChar || hasChar(charsToEscape, curChar)) {
                result.append(escapeChar);
            }
            result.append(curChar);
        }
        return result.toString();
    }

    /**
     * @since 1.0.0
     */
    public static String formatEncoding(String encoding) {
        if (encoding == null) {
            return "";
        }
        encoding = encoding.toUpperCase();
        if (encoding.startsWith("UTF")) {
            encoding = encoding.replace('_', '-');
        } else if (encoding.contains("GB")) {
            encoding = "GB18030";
        } else if (encoding.contains("BIG")) {
            encoding = "BIG5HKSCS";
        }
        return encoding;
    }

    public static boolean isUTF16BEByOrderMark(final byte[] b) {
        if (b != null && b.length > 2) {
            byte utf[] = {(byte) 0xFE, (byte) 0xFF};
            if (b[0] == utf[0] && b[1] == utf[1]) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUTF16LEByOrderMark(final byte[] b) {
        if (b != null && b.length > 2) {
            byte utf[] = {(byte) 0xFF, (byte) 0xFE};
            if (b[0] == utf[0] && b[1] == utf[1]) {
                return true;
            }
        }
        return false;
    }


    public static boolean isUTF8(final byte[] rawtext) {
        int score = 0;
        int i, rawtextlen = 0;
        int goodbytes = 0, asciibytes = 0;
        // Maybe also use UTF8 Byte Order Mark: EF BB BF
        boolean isUTF8 = isUTF8ByOrderMark(rawtext);
        if (isUTF8) {
            return true;
        }
        // Check to see if characters fit into acceptable ranges
        rawtextlen = rawtext.length;
        for (i = 0; i < rawtextlen; i++) {
            if ((rawtext[i] & (byte) 0x7F) == rawtext[i]) {
                asciibytes++;
            } else if (-64 <= rawtext[i] && rawtext[i] <= -33
                    // -0x40~-0x21
                    && // Two bytes
                    i + 1 < rawtextlen && -128 <= rawtext[i + 1] && rawtext[i + 1] <= -65) {
                goodbytes += 2;
                i++;
            } else if (-32 <= rawtext[i] && rawtext[i] <= -17 && // Three bytes
                    i + 2 < rawtextlen && -128 <= rawtext[i + 1] && rawtext[i + 1] <= -65
                    && -128 <= rawtext[i + 2] && rawtext[i + 2] <= -65) {
                goodbytes += 3;
                i += 2;
            }
        }
        if (asciibytes == rawtextlen) {
            return false;
        }
        score = 100 * goodbytes / (rawtextlen - asciibytes);
        // If not above 98, reduce to zero to prevent coincidental matches
        // Allows for some (few) bad formed sequences
        if (score > 98) {
            return true;
        } else if (score > 95 && goodbytes > 30) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isUTF8ByOrderMark(final byte[] b) {
        if (b != null && b.length > 3) {
            byte utf8[] = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            if (b[0] == utf8[0] && b[1] == utf8[1] && b[2] == utf8[2]) {
                return true;
            }
        }
        return false;
    }

    /**
     * java unescape
     *
     * @since 1.0.0
     */
    public static String unescape(final String src) {
        StringBuffer tmp = new StringBuffer();
        tmp.ensureCapacity(src.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < src.length()) {
            pos = src.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (src.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(src.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(src.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else if (pos == -1) {
                tmp.append(src.substring(lastPos));
                lastPos = src.length();
            } else {
                tmp.append(src.substring(lastPos, pos));
                lastPos = pos;
            }
        }
        return tmp.toString();
    }

    public static String unEscapeCSV(final String str) {
        String tmp = str;
        if (str.length() > 1 && str.startsWith("\"") && str.endsWith("\"")) {
            tmp = str.substring(1, str.length() - 1);
        }
        return tmp.replaceAll("\"\"", "\"");
    }

    public static String unEscapeString(final String str, final char escapeChar,
                                        final char charToEscape) {
        return unEscapeString(str, escapeChar, new char[]{charToEscape});
    }

    public static String unEscapeString(final String str, final char escapeChar,
                                        final char charsToEscape[]) {
        if (str == null) {
            return "";
        }
        StringBuilder result = new StringBuilder(str.length());
        boolean hasPreEscape = false;
        for (int i = 0; i < str.length(); i++) {
            char curChar = str.charAt(i);
            if (hasPreEscape) {
                if (curChar != escapeChar && !hasChar(charsToEscape, curChar)) {
                    throw new IllegalArgumentException(
                            (new StringBuilder()).append("Illegal escaped string ").append(str)
                                                 .append(" unescaped ").append(escapeChar)
                                                 .append(" at ").append(i - 1).toString());
                }
                result.append(curChar);
                hasPreEscape = false;
                continue;
            }
            if (hasChar(charsToEscape, curChar)) {
                throw new IllegalArgumentException(
                        (new StringBuilder()).append("Illegal escaped string ").append(str)
                                             .append(" unescaped ").append(curChar).append(" at ")
                                             .append(i).toString());
            }
            if (curChar == escapeChar) {
                hasPreEscape = true;
            } else {
                result.append(curChar);
            }
        }
        if (hasPreEscape) {
            throw new IllegalArgumentException(
                    (new StringBuilder()).append("Illegal escaped string ").append(str)
                                         .append(", not expecting ").append(escapeChar)
                                         .append(" in the end.").toString());
        } else {
            return result.toString();
        }
    }

    /**
     * get encoding from contentType
     *
     * @since 1.0.0
     */
    private static String getEncodingFromContentType(final String contentType) {
        String encoding = null;
        if (!isEmpty(contentType)) {
            int encodeindex = contentType.indexOf("charset=");
            if (-1 != encodeindex) {
                encoding = contentType.substring(encodeindex + "charset=".length());
            } else {
                encodeindex = contentType.indexOf("Language=");
                if (-1 != encodeindex) {
                    encoding = contentType.substring(encodeindex + "Language=".length());
                }

            }

        }
        if (encoding != null && encoding.endsWith(";")) {
            return encoding.substring(0, encoding.length() - 1);
        }
        return encoding;
    }

    private static HttpURLConnection getURL(String URLName) {
        try {
            URLName = URLName.trim();
            if (!URLName.startsWith("http")) {
                URLName = "http://" + URLName;
            }
            URL url = new URL(URLName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(connectTimeout);
            conn.setReadTimeout(readTimeout);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; JVM)");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean hasChar(final char chars[], final char character) {
        char arr$[] = chars;
        int len$ = arr$.length;
        for (int i$ = 0; i$ < len$; i$++) {
            char target = arr$[i$];
            if (character == target) {
                return true;
            }
        }
        return false;
    }

    public static class Constants {

        public static final String CHARSET_HZ = "HZ";

        public static final String CHARSET_GB = "GBK";

        public static final String CHARSET_GB2312 = "GB2312";

        public static final String CHARSET_GB18030 = "GB18030";

        public static final String CHARSET_US_ASCII = "US-ASCII";

        public static final String CHARSET_WINDOWS_1252 = "windows-1252";

        public static final String CHARSET_ISO_8859_1 = "iso-8859-1";

        public static final String CHARSET_UTF16 = "UTF-16";

        public static final String CHARSET_UTF16BE = "UTF-16BE";

        public static final String CHARSET_UTF16LE = "UTF-16LE";

        public static final String CHARSET_UTF8 = "UTF-8";


        public static final String CHARSET_BIG5 = "BIG5";


        public static final String CHARSET_BIG5HKSCS = "BIG5-HKSCS";

        public static final String CHARSET_DEFAULT = Charset.defaultCharset().name();

    }
}
