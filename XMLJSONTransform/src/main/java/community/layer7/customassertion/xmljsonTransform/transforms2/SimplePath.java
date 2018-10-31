package community.layer7.customassertion.xmljsonTransform.transforms2;

import java.util.Iterator;
import java.util.Stack;

public class SimplePath {
	
	private static final String ROOT_XML_PATH = "/";
	private static final String ROOT_JSON_PATH = "$";
	
	private Stack<Object> path = new Stack<Object>();
	
	public SimplePath pushElement(String elementName) {
		if(elementName == null)
			throw new NullPointerException("Element is null");
		path.push(elementName);
		return this;
	}

	public SimplePath pushXMLAttribute(String attributeName) {
		if(attributeName == null)
			throw new NullPointerException("Attribute is null");
		path.push("@" + attributeName);
		return this;
	}

	public SimplePath pushIndex(int index) {
		if(index < 0)
			throw new IndexOutOfBoundsException("Index can not be negative");
		path.push(index);
		return this;
	}
	
	/**
	 * May throw EmptyStackException
	 */
	public SimplePath pop() {
		path.pop();
		return this;
	}
	
	/**
	 * 
	 * @return String such as /root/children[3]/@id
	 */
	public String getFullXMLPath() {
		if(path.isEmpty())
			return ROOT_XML_PATH;
		StringBuilder sb = new StringBuilder();
		Iterator<Object> pathIterator = path.iterator();
		while(pathIterator.hasNext()) {
			Object o = pathIterator.next();
			if(o instanceof String)
				sb.append('/').append(o);
			else if(o instanceof Integer)
				sb.append('[').append(o).append(']');
			//else error
		}
		return sb.toString();
	}

	/**
	 * 
	 * @return String such as $.children[3].firstname
	 */
	public String getFullJSONPath() {
		if(path.isEmpty())
			return ROOT_JSON_PATH;
		StringBuilder sb = new StringBuilder("$");
		Iterator<Object> pathIterator = path.iterator();
		while(pathIterator.hasNext()) {
			Object o = pathIterator.next();
			if(o instanceof String)
				sb.append('.').append(o);
			else if(o instanceof Integer)
				sb.append('[').append(o).append(']');
			//else error
		}
		return sb.toString();
	}
}
