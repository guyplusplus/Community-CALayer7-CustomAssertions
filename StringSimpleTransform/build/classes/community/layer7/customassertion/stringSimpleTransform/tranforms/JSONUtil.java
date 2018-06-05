package community.layer7.customassertion.stringSimpleTransform.tranforms;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**'
 * @author JSON.org
 * @version 2016-08-15
 */
public class JSONUtil {
	
    public static String quote(String string) {
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            try {
                return quote(string, sw).toString();
            } catch (IOException ignored) {
                // will never happen - we are writing to a string writer
                return "";
            }
        }
    }

    public static Writer quote(String string, Writer w) throws IOException {
        if (string == null || string.length() == 0) {
            //w.write("\"\"");
            return w;
        }

        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();

        //w.write('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                w.write('\\');
                w.write(c);
                break;
            case '/':
                if (b == '<') {
                    w.write('\\');
                }
                w.write(c);
                break;
            case '\b':
                w.write("\\b");
                break;
            case '\t':
                w.write("\\t");
                break;
            case '\n':
                w.write("\\n");
                break;
            case '\f':
                w.write("\\f");
                break;
            case '\r':
                w.write("\\r");
                break;
            default:
                if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                        || (c >= '\u2000' && c < '\u2100')) {
                    w.write("\\u");
                    hhhh = Integer.toHexString(c);
                    w.write("0000", 0, 4 - hhhh.length());
                    w.write(hhhh);
                } else {
                    w.write(c);
                }
            }
        }
        //w.write('"');
        return w;
    }
    
    public static String JSONStringToString(String jsonString) throws UnsupportedEncodingException {
    	int len = jsonString.length();
        char c;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            c = jsonString.charAt(i);
            switch (c) {
            case 0:
            case '\n':
            case '\r':
                throw new UnsupportedEncodingException("Unterminated string");
            case '\\':
            	i++;
            	if(i >= len)
            		throw new UnsupportedEncodingException("Missing escape character");
            	else
            		c = jsonString.charAt(i);
                switch (c) {
                case 'b':
                    sb.append('\b');
                    break;
                case 't':
                    sb.append('\t');
                    break;
                case 'n':
                    sb.append('\n');
                    break;
                case 'f':
                    sb.append('\f');
                    break;
                case 'r':
                    sb.append('\r');
                    break;
                case 'u':
                    try {
                    	String unicode = jsonString.substring(i + 1, i + 5);
                        sb.append((char)Integer.parseInt(unicode, 16));
                        i += 4;
                    }
                    catch (Exception e) {
                        throw new UnsupportedEncodingException("Illegal escape for 4 hexa characters: " + e);
                    }
                    break;
                case '"':
                case '\'':
                case '\\':
                case '/':
                    sb.append(c);
                    break;
                default:
                    throw new UnsupportedEncodingException("Illegal escape");
                }
                break;
            default:
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
