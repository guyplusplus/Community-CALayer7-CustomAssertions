package community.layer7.customassertion.noDuplicateJSONName.logic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Guy Deffaux
 * July 2018
 */
@RunWith(MockitoJUnitRunner.class)
public class JSONDuplicatedNameCheckerTest {

    @Test
	public void testChecks() {    	
    	try {
    		JSONDuplicatedNameChecker.checkDuplicatedName("{\"a\":123, \"a2\":456, \"b\":[1,2,3], \"c\":[4,{\"c\":22,\"d\":{\"e\":44,\"f\":88,\"e\":999}},5]}");
    	}
    	catch(DuplicatedKeyName e) {
    		assertEquals(e.getKeyName(), "$.c[1].d.e");
    	}
    	try {
    		JSONDuplicatedNameChecker.checkDuplicatedName("[9,{\"a\":123, \"a2\":456, \"b\":[1,2,3], \"c\":[4,{\"c\":22,\"d\":{\"e\":44,\"f\":88,\"e\":999}},5]}]");
    	}
    	catch(DuplicatedKeyName e) {
    		assertEquals(e.getKeyName(), "$.[1].c[1].d.e");
    	}
    	try {
    		JSONDuplicatedNameChecker.checkDuplicatedName("{\"a\":1,\"a\":2}");
    	}
    	catch(DuplicatedKeyName e) {
    		assertEquals(e.getKeyName(), "$.a");
    	}
    	try {
    		JSONDuplicatedNameChecker.checkDuplicatedName("[[1,2],[3,4],[5,{\"a\":1,\"a\":2}]]");
    	}
    	catch(DuplicatedKeyName e) {
    		assertEquals(e.getKeyName(), "$.[2][1].a");
    	}

    	try {
    		JSONDuplicatedNameChecker.checkDuplicatedName("asdasd"); //invalid json
    		JSONDuplicatedNameChecker.checkDuplicatedName("{\"a\":123, \"a2\":456, \"b\":[1,2,3], \"c\":[4,,,,{\"c\":22,\"d\":{\"e\":44,\"f\":88,\"e\":999}},5]}"); //invalid json
    		JSONDuplicatedNameChecker.checkDuplicatedName("{\"a\":123, \"a2\":456, \"b\":[1,2,3], \"c\":[4,{\"c\":22,\"d\":{\"e\":44,\"f\":88,\"g\":999}},5]}"); //valid
    		JSONDuplicatedNameChecker.checkDuplicatedName("[9,{\"a\":123, \"a2\":456, \"b\":[1,2,3], \"c\":[4,{\"c\":22,\"d\":{\"e\":44,\"f\":88,\"g\":999}},5]}]"); //valid
    	}
    	catch(DuplicatedKeyName e) {
    		assertTrue(false);
    	}
    }
}
