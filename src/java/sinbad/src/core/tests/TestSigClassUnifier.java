package core.tests;

import org.json.JSONArray;
import org.junit.*;
import org.junit.rules.ExpectedException;

import core.sig.*;
import core.ops.*;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.io.PrintWriter;
import java.util.ArrayList;

import static core.sig.PrimSig.*;

public class TestSigClassUnifier {
    public TestSigClassUnifier() {

    }

    @Test
    public void testPrimSigSucceed() {
        assertEquals(BOOLEAN_SIG, BOOLEAN_SIG.apply(new SigClassUnifier(Boolean.class)));
        assertEquals(STRING_SIG, BOOLEAN_SIG.apply(new SigClassUnifier(String.class, true))); // widening conversion
    }
    
    @Test
    public void testPrimSigFail1() {
    	assertThrowsExactly(SignatureUnificationException.class, () ->
        		BOOLEAN_SIG.apply(new SigClassUnifier(Float.class)));
    }
    
    @Test
    public void testPrimSigFail2() {
    	assertThrowsExactly(SignatureUnificationException.class, () ->
        	BOOLEAN_SIG.apply(new SigClassUnifier(String.class)));  // no widening
    }
    
    
    
    @Test
    public void testListSigSucceed() {
        int[] nums = {};
        ListSig s1 = new ListSig(INT_SIG);
        assertEquals(s1, s1.apply(new SigClassUnifier(nums.getClass())));
        
        ArrayList<Integer> numlist = new ArrayList<Integer>();
        assertEquals(s1, s1.apply(new SigClassUnifier(numlist.getClass())));

        String[][] wds = {};
        ListSig s2 = new ListSig(new ListSig(STRING_SIG));
        assertEquals(s2, s2.apply(new SigClassUnifier(wds.getClass())));
    }
    
    @Test
    public void testListSigFail1() {                // listof int   with  String[]
    	assertThrowsExactly(SignatureUnificationException.class, () -> {
	        String[] nums = {};
	        new ListSig(INT_SIG).apply(new SigClassUnifier(nums.getClass(), true));
	    });
    }
    
    @Test
    public void testListSigFail2() {                // listof-listof-string  with  int[][]
    	assertThrowsExactly(SignatureUnificationException.class, () -> {
	        int[][] wds = {};
	        ListSig s2 = new ListSig(new ListSig(STRING_SIG));
	        s2.apply(new SigClassUnifier(wds.getClass())); 
	    });  
    }
    
    
    static class ClassA {
        public ClassA() {}
        public ClassA(String s) {}
        public ClassA(float f, int i) {}
        public ClassA(int a, int b, int c) {}
        public ClassA(String s, int i, boolean b) { }
    }
    
    static class ClassB {
        public ClassB(int a, int b, int c) {}
        public ClassB(double d, ClassA r, ClassA z) {}
    }
    
    @Test
    public void testCompSigSucceed() {
        SigClassUnifier uA = new SigClassUnifier(ClassA.class);
        SigClassUnifier uB = new SigClassUnifier(ClassB.class);
        
        CompSig<ClassA> c1 = new CompSig<ClassA>(ClassA.class, new ArgSpec("foo", STRING_SIG));
        assertEquals(true, uA.unifiesWith(c1));
        assertEquals(c1, c1.apply(uA));
     
        CompSig<ClassA> c2 = new CompSig<ClassA>(ClassA.class, new ArgSpec("f", WILDCARD_SIG),
                                                               new ArgSpec("t", INT_SIG),
                                                               new ArgSpec("zip", WILDCARD_SIG));
        CompSig<ClassA> c2spec = new CompSig<ClassA>(ClassA.class, new ArgSpec("f", STRING_SIG),
                                                                    new ArgSpec("t", INT_SIG),
                                                                    new ArgSpec("zip", BOOLEAN_SIG));
        assertEquals(true, uA.unifiesWith(c2));
        assertEquals(c2spec, c2.apply(uA));
        assertEquals(c2spec, c2.apply(uA));  // test cached constructors (see findConstructor)
        
        CompSig<ClassB> c3 = new CompSig<ClassB>(ClassB.class, new ArgSpec("querty", DOUBLE_SIG),
                                                                new ArgSpec("drum", c1),
                                                                new ArgSpec("tom", c2));
        CompSig<ClassB> c3spec = new CompSig<ClassB>(ClassB.class, new ArgSpec("querty", DOUBLE_SIG),
                                                                    new ArgSpec("drum", c1),
                                                                    new ArgSpec("tom", c2spec));
        assertEquals(c3spec, c3.apply(uB));
    }
    
    @Test
    public void testCompSigFail() {
        CompSig<ClassA> c2 = new CompSig<ClassA>(ClassA.class, new ArgSpec("foo", BOOLEAN_SIG));
        assertEquals(false, new SigClassUnifier(ClassA.class).unifiesWith(c2));
        
    }

    
}
