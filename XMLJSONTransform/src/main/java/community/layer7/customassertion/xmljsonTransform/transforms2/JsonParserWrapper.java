package community.layer7.customassertion.xmljsonTransform.transforms2;

import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * This class is a wrapper for JsonParser. It simply helps catching exceptions
 * related to this package and map it to a MapException with JSON path
 * information
 * @author Guy D.
 *
 */
public class JsonParserWrapper {
	
	private JsonParser parser;
	
	public JsonParserWrapper(JsonParser parser) {
		this.parser = parser;
	}
	
	public boolean hasNext(SimplePath jpath) throws MapException {
		try {
			return parser.hasNext();
		} catch (Exception e) {
			throw new MapException("Failed to parse JSON input", jpath.getFullJSONPath());
		}
	}

	public Event next(SimplePath jpath) throws MapException {
		try {
			return parser.next();
		} catch (Exception e) {
			throw new MapException("Failed to parse JSON input", jpath.getFullJSONPath());
		}
	}
	
	public String getString(SimplePath jpath) throws MapException {
		try {
			return parser.getString();
		} catch (Exception e) {
			throw new MapException("Failed to parse JSON input", jpath.getFullJSONPath());
		}
	}
}
