package community.layer7.customassertion.xmljsonTransform.transforms;

import java.util.HashMap;

public class SchemaCache {
	
	private static long JSONXML_SCHEMA_CACHE_AGE_SCAN_FREQUENCY = 60 * 1000; // standard 60 seconds
	private static long jsonxmlSchemaCacheMaxAge = -1; //default no aging
	private static int jsonxmlSchemaCacheMaxDownloadSize = 128 * 1024; //default 128KB
	private static int jsonxmlSchemaCacheMaxEntries = 128;//default 128 entries
	
	private static SchemaCache singleton = new SchemaCache();
	
	private HashMap<String, JSONSchemaForXML> jsonSchemaForXMLCache;
	private int totalDownloadSize;
	private long lastScanTimeInMs;
	
	public static SchemaCache getSingleton() {
		return singleton;
	}
	
	public static void setJsonxmlSchemaCacheMaxAge(long jsonxmlSchemaCacheMaxAge) {
		if(jsonxmlSchemaCacheMaxAge < 1 && jsonxmlSchemaCacheMaxAge != -1)
			return;
		SchemaCache.jsonxmlSchemaCacheMaxAge = jsonxmlSchemaCacheMaxAge;
	}

	public static void setJsonxmlSchemaCacheMaxDownloadSize(int jsonxmlSchemaCacheMaxDownloadSize) {
		if(jsonxmlSchemaCacheMaxDownloadSize < 1)
			return;
		SchemaCache.jsonxmlSchemaCacheMaxDownloadSize = jsonxmlSchemaCacheMaxDownloadSize;
	}

	public static void setJsonxmlSchemaCacheMaxEntries(int jsonxmlSchemaCacheMaxEntries) {
		if(jsonxmlSchemaCacheMaxEntries < 0)
			return;
		SchemaCache.jsonxmlSchemaCacheMaxEntries = jsonxmlSchemaCacheMaxEntries;
	}
	
	public static void setJSONXML_SCHEMA_CACHE_AGE_SCAN_FREQUENCY(long l) {
		JSONXML_SCHEMA_CACHE_AGE_SCAN_FREQUENCY = l;
	}
	
	public SchemaCache() {
		flushCache();
	}
	
	public synchronized void flushCache() {
		jsonSchemaForXMLCache = new HashMap<String, JSONSchemaForXML>(jsonxmlSchemaCacheMaxEntries);
		totalDownloadSize = 0;
		lastScanTimeInMs = System.currentTimeMillis();
	}
	
	private synchronized void removeOldCache() {
		if(jsonxmlSchemaCacheMaxAge == -1)
			return;
		long now = System.currentTimeMillis();
		if(lastScanTimeInMs > (now - JSONXML_SCHEMA_CACHE_AGE_SCAN_FREQUENCY))
			return;
		long pointInTime = now - jsonxmlSchemaCacheMaxAge;
		for(String schemaKey:jsonSchemaForXMLCache.keySet()) {
			JSONSchemaForXML jsonSchemaForXML = jsonSchemaForXMLCache.get(schemaKey);
			if(jsonSchemaForXML.getCreatedTimeInMs() < pointInTime) {
				jsonSchemaForXMLCache.remove(schemaKey);
				totalDownloadSize -= schemaKey.length();
			}
		}
		lastScanTimeInMs = now;
	}
	
	public synchronized JSONSchemaForXML getJSONSchemaForXML(String jsonSchema) throws JSONSchemaLoadException {
		removeOldCache();
		JSONSchemaForXML jsonSchemaForXML = jsonSchemaForXMLCache.get(jsonSchema);
		if(jsonSchemaForXML != null)
			return jsonSchemaForXML;
		jsonSchemaForXML = new JSONSchemaForXML(jsonSchema);
		int jsonSchemaLength = jsonSchema.length();
		if(totalDownloadSize + jsonSchemaLength > jsonxmlSchemaCacheMaxDownloadSize) {
			//cache byte size exceeded
			return jsonSchemaForXML;
		}
		if((jsonSchemaForXMLCache.size() + 1) > jsonxmlSchemaCacheMaxEntries) {
			//cache entry size exceeded
			return jsonSchemaForXML;
		}
		jsonSchemaForXMLCache.put(jsonSchema, jsonSchemaForXML);
		totalDownloadSize += jsonSchemaLength;
		return jsonSchemaForXML;
	}

}
