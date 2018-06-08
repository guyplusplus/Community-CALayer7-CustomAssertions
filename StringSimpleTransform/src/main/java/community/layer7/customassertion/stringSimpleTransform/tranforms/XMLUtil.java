package community.layer7.customassertion.stringSimpleTransform.tranforms;

/**
 * @author http://www.java2s.com
 * With additions from https://en.wikipedia.org/wiki/Valid_characters_in_XML#XML_1.1 :
 * https://en.wikipedia.org/wiki/Valid_characters_in_XML#XML_1.1 :
 *   U+0001–U+0008, U+000B–U+000C, U+000E–U+001F : this includes most (not all) C0 control characters
 *   U+007F–U+0084, U+0086–U+009F  : this includes a C0 control character, and all but one C1 control.
 *   "\u000b" maps to "&#11;" for example
 */
public class XMLUtil {

	/*
	 * maps behavior of org.apache.commons.text.StringEscapeUtils 1.3
	 */
    public static String escapeXML(String str) {
        if (str == null)
            return null;
        int len = str.length();
        if (len == 0)
            return str;

        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == '<')
                encoded.append("&lt;");
            else if (c == '\"')
                encoded.append("&quot;");
            else if (c == '>')
                encoded.append("&gt;");
            else if (c == '\'')
                encoded.append("&apos;");
            else if (c == '&')
                encoded.append("&amp;");
            else if ((c >= 0x1 && c <= 0x8) || (c >= 0xB && c <= 0xC) || (c >= 0xE && c <= 0x1F) ||
            		(c >= 0x7F && c <= 0x84) || (c >= 0x86 && c <= 0x9F))
            	encoded.append("&#").append((int)c).append(';');
            else if (c == 0 || c == '\ufffe' || c == '\uffff') {
            	//do nothing
            }
            else
                encoded.append(c);
        }
        return encoded.toString();
    }
}
