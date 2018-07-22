package community.layer7.customassertion.noDuplicateJSONName.logic;

import java.io.StringReader;
import java.util.HashSet;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

public class JSONDuplicatedNameChecker {
	
	public static void checkDuplicatedName(String jsonStr) throws DuplicatedKeyName {
		if(jsonStr == null)
			return;
		JsonParser parser = Json.createParser(new StringReader(jsonStr));
		try {
			Event e = parser.next();
			if(e == Event.START_OBJECT)
				parseObject(parser, "$");
			else if(e == Event.START_ARRAY)
				parseArray(parser, "$.");
			//else ignore
		}
		catch(DuplicatedKeyName e) {
			throw e;
		}
		catch(Exception e) {
			//ignore any other case
			//System.out.println("JsonParsingException e=" + e);
		}
	}
	
	private static void parseArray(JsonParser parser, String currentPath) throws DuplicatedKeyName {
		//System.out.println("parseArray currentPath=" + currentPath);
		int index = 0;
		while (parser.hasNext()) {
			Event e = parser.next();
			if(e == Event.START_ARRAY) {
				parseArray(parser, currentPath + "[" + index + "]");
			}
			if(e == Event.END_ARRAY)
				return;
			if(e == Event.START_OBJECT) {
				parseObject(parser, currentPath + "[" + index + "]");
			}
			index++;
		}
		return;
	}

	private static void parseObject(JsonParser parser, String currentPath) throws DuplicatedKeyName {
		//System.out.println("parseObject currentPath=" + currentPath);
		String keyName = null;
		HashSet<String> keyNames = new HashSet<String>();
		while (parser.hasNext()) {
			Event e = parser.next();
			if(e == Event.START_ARRAY) {
				parseArray(parser, currentPath + "." + keyName);
			}
			if(e == Event.START_OBJECT) {
				parseObject(parser, currentPath + "." + keyName);
			}
			if(e == Event.END_OBJECT)
				return;
			if(e == Event.KEY_NAME) {
				keyName = parser.getString();
				//System.out.println("parseObject keyName=" + keyName);
				if(keyNames.contains(keyName))
					throw new DuplicatedKeyName(currentPath + "." + keyName);
				keyNames.add(keyName);
			}
		}
		return;
	}
}