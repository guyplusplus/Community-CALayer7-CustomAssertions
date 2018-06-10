package community.layer7.customassertion.stringSimpleTransform.tranforms;

import java.io.UnsupportedEncodingException;

/**'
 * @author Guy Deffaux
 * @version 2016-08-15
 * https://tools.ietf.org/html/rfc4627
 */
public class JSONUtil {
	
    public static String encodeJSON(String string) {
        if (string == null)
            return null;
    	StringBuilder sb = new StringBuilder();
        int len = string.length();
        for (int i = 0; i < len; i += 1) {
            char c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
            case '/':
                sb.append('\\');
                sb.append(c);
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\r':
                sb.append("\\r");
                break;
            default:
                if (c < ' ') {
                    sb.append("\\u");
                    String hhhh = Integer.toHexString(c);
                    sb.append("0000", 0, 4 - hhhh.length());
                    sb.append(hhhh);
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
    
    public static String JSONStringToString(String jsonString) throws UnsupportedEncodingException {
    	int len = jsonString.length();
        char c;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            c = jsonString.charAt(i);
            switch (c) {
            case '\\':
            	i++;
            	if(i >= len)
            		throw new UnsupportedEncodingException("Missing escape character");
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
