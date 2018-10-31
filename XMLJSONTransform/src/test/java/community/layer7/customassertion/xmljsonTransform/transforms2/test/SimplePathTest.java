package community.layer7.customassertion.xmljsonTransform.transforms2.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import community.layer7.customassertion.xmljsonTransform.transforms2.SimplePath;

public class SimplePathTest {

	@Test
	public void testSimplePathBasic() {
		SimplePath sp = new SimplePath();
		//test empty path
		assertEquals(sp.getFullXMLPath(), "/");
		assertEquals(sp.getFullJSONPath(), "$");
		//test single push
		sp.pushElement("e1");
		assertEquals(sp.getFullXMLPath(), "/e1");
		assertEquals(sp.getFullJSONPath(), "$.e1");
		//test multi push for all types
		sp.pushElement("e2").pushIndex(10).pushElement("e3").pushXMLAttribute("attr1");
		assertEquals(sp.getFullXMLPath(), "/e1/e2[10]/e3/@attr1");
		//test single pop
		sp.pop();
		assertEquals(sp.getFullXMLPath(), "/e1/e2[10]/e3");
		assertEquals(sp.getFullJSONPath(), "$.e1.e2[10].e3");
		//test multi pop
		sp.pop().pop();
		assertEquals(sp.getFullXMLPath(), "/e1/e2");
		assertEquals(sp.getFullJSONPath(), "$.e1.e2");
		//test pop until stack empty
		sp.pop().pop();
		assertEquals(sp.getFullXMLPath(), "/");
		assertEquals(sp.getFullJSONPath(), "$");
	}
	
	@Test
	public void testSimplePathClone() {
		SimplePath sp1 = new SimplePath();
		sp1.pushElement("e1").pushElement("e2").pushIndex(10).pushElement("e3").pushXMLAttribute("attr1");
		assertEquals(sp1.getFullXMLPath(), "/e1/e2[10]/e3/@attr1");
		SimplePath sp2 = sp1.clone();
		assertEquals(sp2.getFullXMLPath(), "/e1/e2[10]/e3/@attr1");
		sp1.pop();
		assertEquals(sp1.getFullXMLPath(), "/e1/e2[10]/e3");
		assertEquals(sp2.getFullXMLPath(), "/e1/e2[10]/e3/@attr1");
		sp2.pushElement("e4");
		assertEquals(sp1.getFullXMLPath(), "/e1/e2[10]/e3");
		assertEquals(sp2.getFullXMLPath(), "/e1/e2[10]/e3/@attr1/e4");
	}
}
