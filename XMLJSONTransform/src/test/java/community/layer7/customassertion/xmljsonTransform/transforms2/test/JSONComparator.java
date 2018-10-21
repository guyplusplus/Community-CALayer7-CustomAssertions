package community.layer7.customassertion.xmljsonTransform.transforms2.test;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONComparator {
	
	/**
	 * similar to JSONObject.similar() but when comparing number, it changes them to BigDecimal and then compare
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean areObjectsEqual(Object o1, Object o2) {
		if(o1 == null && o2 == null)
			return true;
		if(o1 == null || o2 == null)
			return false;
		if(o1.equals(JSONObject.NULL))
			return o2.equals(JSONObject.NULL);
		if(o1 instanceof Boolean || o1 instanceof String)
			return o1.equals(o2);
		if(o1 instanceof Integer || o1 instanceof Long || o1 instanceof Double || o1 instanceof Float || o1 instanceof BigDecimal || o1 instanceof BigInteger) {
			try {
				BigDecimal o1bd = new BigDecimal(o1.toString());
				BigDecimal o2bd = new BigDecimal(o2.toString());
				return o1bd.compareTo(o2bd) == 0;
			} catch (NumberFormatException e) {
				return false;
			}
		}
		if(o1 instanceof JSONObject) {
			if(!(o2 instanceof JSONObject))
				return false;
			JSONObject jo1 = (JSONObject)o1;
			JSONObject jo2 = (JSONObject)o2;
			if(jo1.length() != jo2.length())
				return false;
			for(String key:jo1.keySet()) {
				Object v1 = jo1.get(key);
				Object v2 = jo2.opt(key);
				if(v2 == null)
					return false;
				if(!areObjectsEqual(v1, v2))
					return false;
			}
			return true;
		}
		if(o1 instanceof JSONArray) {
			if(!(o2 instanceof JSONArray))
				return false;
			JSONArray ja1 = (JSONArray)o1;
			JSONArray ja2 = (JSONArray)o2;
			if(ja1.length() != ja2.length())
				return false;
			int l = ja1.length();
			for(int i = 0; i<l; i++) {
				if(!areObjectsEqual(ja1.get(i), ja2.get(i)))
					return false;
			}
			return true;
		}
		return false;
	}

}
