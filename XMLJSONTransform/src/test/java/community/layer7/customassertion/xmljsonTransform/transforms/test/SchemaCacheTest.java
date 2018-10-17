package community.layer7.customassertion.xmljsonTransform.transforms.test;

import static org.junit.Assert.*;

import org.junit.Test;

import community.layer7.customassertion.xmljsonTransform.transforms.JSONSchemaForXML;
import community.layer7.customassertion.xmljsonTransform.transforms.JSONSchemaLoadException;
import community.layer7.customassertion.xmljsonTransform.transforms.SchemaCache;

public class SchemaCacheTest {

	@Test
	public void testNoCache() {
		SchemaCache.setJsonxmlSchemaCacheMaxEntries(0);
		SchemaCache.setJsonxmlSchemaCacheMaxDownloadSize(12800);
		SchemaCache.setJsonxmlSchemaCacheMaxAge(-1);
		try {
			SchemaCache.getSingleton().flushCache();
			JSONSchemaForXML sc1 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			JSONSchemaForXML sc2 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			assertTrue(sc1 != sc2); //no cache since max entries is 0
		} catch (JSONSchemaLoadException e) {
			fail("Should not throw exception");
		}
	}

	@Test
	public void testCacheMaxEntries() {
		SchemaCache.setJsonxmlSchemaCacheMaxEntries(2);
		SchemaCache.setJsonxmlSchemaCacheMaxDownloadSize(12800);
		SchemaCache.setJsonxmlSchemaCacheMaxAge(-1);
		try {
			SchemaCache.getSingleton().flushCache();
			JSONSchemaForXML sc1 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			JSONSchemaForXML sc2 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			assertTrue(sc1 == sc2);
			JSONSchemaForXML sc3 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString2\":{\"type\":\"string\"}}}");
			JSONSchemaForXML sc4 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString2\":{\"type\":\"string\"}}}");
			assertTrue(sc1 != sc3);
			assertTrue(sc3 == sc4); //cache is working
			JSONSchemaForXML sc5 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString3\":{\"type\":\"string\"}}}");
			JSONSchemaForXML sc6 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString3\":{\"type\":\"string\"}}}");
			assertTrue(sc1 != sc5);
			assertTrue(sc3 != sc5);
			assertTrue(sc5 != sc6); //cache is full, 3rd item not allowed
		} catch (JSONSchemaLoadException e) {
			fail("Should not throw exception");
		}
	}

	@Test
	public void testCacheMaxDownloadSize() {
		SchemaCache.setJsonxmlSchemaCacheMaxEntries(200);
		SchemaCache.setJsonxmlSchemaCacheMaxDownloadSize(128);
		SchemaCache.setJsonxmlSchemaCacheMaxAge(-1);
		try {
			SchemaCache.getSingleton().flushCache();
			JSONSchemaForXML sc1 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			JSONSchemaForXML sc2 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			assertTrue(sc1 == sc2);
			JSONSchemaForXML sc3 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString2\":{\"type\":\"string\"}}}");
			JSONSchemaForXML sc4 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString2\":{\"type\":\"string\"}}}");
			assertTrue(sc1 != sc3);
			assertTrue(sc3 == sc4); //cache is working
			JSONSchemaForXML sc5 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString3\":{\"type\":\"string\"}}}");
			JSONSchemaForXML sc6 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString3\":{\"type\":\"string\"}}}");
			assertTrue(sc1 != sc5);
			assertTrue(sc3 != sc5);
			assertTrue(sc5 != sc6); //cache is full, more than 128 chars (total 120 chars)
		} catch (JSONSchemaLoadException e) {
			fail("Should not throw exception");
		}
	}
	
	@Test
	public void testCacheMaxAge() {
		SchemaCache.setJsonxmlSchemaCacheMaxEntries(200);
		SchemaCache.setJsonxmlSchemaCacheMaxDownloadSize(128);
		SchemaCache.setJSONXML_SCHEMA_CACHE_AGE_SCAN_FREQUENCY(500);
		SchemaCache.setJsonxmlSchemaCacheMaxAge(1000);
		try {
			SchemaCache.getSingleton().flushCache();
			JSONSchemaForXML sc1 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			JSONSchemaForXML sc2 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			assertTrue(sc1 == sc2);
			Thread.sleep(600);
			JSONSchemaForXML sc3 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			JSONSchemaForXML sc4 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			assertTrue(sc1 == sc3); //600 does not exceed max age of 1000
			assertTrue(sc3 == sc4); //same since from cache
			Thread.sleep(600);
			JSONSchemaForXML sc5 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			JSONSchemaForXML sc6 = SchemaCache.getSingleton().getJSONSchemaForXML("{\"type\":\"object\",\"properties\":{\"aString\":{\"type\":\"string\"}}}");
			assertTrue(sc1 != sc5); //1200 exceeds max age of 1000
			assertTrue(sc5 == sc6); //same since from cache
		} catch (Exception e) {
			fail("Should not throw exception");
		}
	}
	
}
