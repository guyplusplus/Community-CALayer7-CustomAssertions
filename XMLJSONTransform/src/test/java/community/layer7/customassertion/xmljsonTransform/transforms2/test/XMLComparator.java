package community.layer7.customassertion.xmljsonTransform.transforms2.test;

import org.w3c.dom.Document;

import community.layer7.customassertion.xmljsonTransform.transforms2.JSONSchemaForXML;
import community.layer7.customassertion.xmljsonTransform.transforms2.MapException;

public class XMLComparator {

	public static boolean areObjectsEqual(Document d, String xml) throws MapException {
		String s = JSONSchemaForXML.xmlToString(d, false);
		return s.equals(xml);
	}
	
	public static void printAscii(String s) {
		int l = s.length();
		for(int i = 0; i<l; System.out.print(" " + (int)s.charAt(i++)));
		System.out.println();
	}

}
