/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.jsio.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

import java.util.List;
import java.util.Iterator;

/**
 *  Tests data-access functions of the wrapper generation code.
 */
public class JSONWrapperTest extends GWTTestCase {
  
  /**
   * A class that uses class-based naming policy.
   * @gwt.namePolicy com.google.gwt.jsio.rebind.TestNamePolicy
   */
  static interface ClassPolicyNamedInterface extends JSWrapper {
    public String getHello();
    public void setHello(String hello);
  }
  
  static interface ListInterface extends JSWrapper {
    public int getBasicInt();
    /**
     * @gwt.typeArgs <com.google.gwt.jsio.client.JSONWrapperTest.PartialWrapper>
     */
    public JSList getPartialWrappers();
    
    /**
     * @gwt.typeArgs <java.lang.Integer>
     */
    public JSList getRank1();
    
    /**
     * @gwt.typeArgs <com.google.gwt.jsio.client.JSList<java.lang.Integer>>
     */
    public JSList getRank2();
    
    /**
     * @gwt.typeArgs <com.google.gwt.jsio.client.JSList<com.google.gwt.jsio.client.JSList<java.lang.Integer>>>
     */
    public JSList getRank3();
    
    /**
     * @gwt.typeArgs <java.lang.String>
     */
    public JSList getString1();
    
    /**
     * @gwt.typeArgs list <com.google.gwt.jsio.client.JSONWrapperTest.PartialWrapper>
     */
    public void setPartialWrappers(JSList list);
  }

  /**
   * A class that uses method-based naming policy.
   */
  static interface NamedInterface extends JSWrapper {
    /**
     * @gwt.fieldName HELLO
     */
    public String getHello();
    public void setHello(String hello);
  }
  
  /**
   * Test "finishing" an abstract base class.
   */
  abstract static class PartialWrapper implements JSWrapper {
    public abstract int getA();
    public abstract int getB();
    public int getC() {
      return 100;
    }
    
    public int multiply() {
      return getA() * getB();
    }
    public abstract void setA(int a);
    
    public abstract void setB(int b);
  }
  
  /**
   * A class that uses a built-in class-based naming policy.
   * @gwt.namePolicy upper
   */
  static interface PolicyNamedInterface extends JSWrapper {
    public String getHello();
    public void setHello(String hello);
  }
  
  /**
   * Contains getters for all primitive types.
   */
  static interface PrimitiveInterface extends JSWrapper {
    public Boolean getBoxedBoolean();
    public Byte getBoxedByte();
    public Character getBoxedChar();
    public Double getBoxedDouble();
    public Float getBoxedFloat();
    public Integer getBoxedInt();
    public Long getBoxedLong();
    public Short getBoxedShort();
    public String getHello();
    
    public boolean getUnboxedBoolean();
    public byte getUnboxedByte();
    public char getUnboxedChar();
    public double getUnboxedDouble();
    public float getUnboxedFloat();
    public int getUnboxedInt();
    public long getUnboxedLong();
    public short getUnboxedShort();
    
    public void setBoxedBoolean(Boolean value);
    public void setBoxedByte(Byte value);
    public void setBoxedChar(Character value);
    public void setBoxedDouble(Double value);
    public void setBoxedFloat(Float value);
    public void setBoxedInt(Integer value);
    public void setBoxedLong(Long value);
    public void setBoxedShort(Short value);
    public void setHello(String hello);
    
    public void setUnboxedBoolean(boolean value);
    public void setUnboxedByte(byte value);
    public void setUnboxedChar(char value);
    public void setUnboxedDouble(double value);
    public void setUnboxedFloat(float value);
    public void setUnboxedInt(int value);
    public void setUnboxedLong(long value);
    public void setUnboxedShort(short value);
  }
  
  /**
   * Polymorphism test
   */
  abstract static class SuperInheritor extends PartialWrapper
      implements PrimitiveInterface, ListInterface {
  }
  
  static interface TreeInterface extends JSWrapper {
    public TreeInterface getLeft();
    public TreeInterface getRight();
    
    public int getValue();
    public void setLeft(TreeInterface ti);
    
    public void setRight(TreeInterface ti);
    public void setValue(int value);
  }
  
  /**
   * A test array, used by makeJsonArray.
   */
  private static final int[] ARRAY_INT = {0, 1, 2, 3, 4};
  
  /**
   * Represents nested array data.
   */
  private static final String ARRAY_DATA = "{" +
    "basicInt: 0, " +
    "rank1: " + makeJsonArray(1) + ", " +
    "rank2: " + makeJsonArray(2) + ", " +
    "rank3: " + makeJsonArray(3) + ", " +
    "string1: ['this', 'is', 'a', 'test']" + ", " +
    "partialWrappers: [{a:1, b:2}, {a:3, b:4}, {a:5, b:6}]" + ", " +
    "}";
  
  /**
   * Create a json array of specified rank.  Each level will contain
   * <code>ARRAY_INT.length</code> elements, with an array of rank 1 being
   * equal to ARRAY_INT.
   */
  private static StringBuffer makeJsonArray(int rank) {
    StringBuffer toReturn = new StringBuffer();
    toReturn.append("[");
    for (int i = 0; i < ARRAY_INT.length; i++) {
      if (rank > 1) {
        toReturn.append(makeJsonArray(rank - 1));
      } else {
        toReturn.append(i);
      }
      
      if (i < 4) {
        toReturn.append(",");
      }
    }
    toReturn.append("]");
    
    return toReturn;
  }
  
  public String getModuleName() {
    return "com.google.gwt.jsio.JSON";
  }
  
  public void testBoxedSetters() {
    PrimitiveInterface ti =
        (PrimitiveInterface)GWT.create(PrimitiveInterface.class);
    assertTrue(ti != null);

    ti.setBoxedBoolean(Boolean.TRUE);
    ti.setBoxedByte(new Byte((byte)0x42));
    ti.setBoxedChar(new Character('A'));
    ti.setBoxedDouble(new Double(Math.PI));
    ti.setBoxedFloat(new Float((float)Math.E));
    ti.setBoxedInt(new Integer(42));
    ti.setBoxedLong(new Long(43));
    ti.setBoxedShort(new Short((short)44));
    
    assertEquals(Boolean.TRUE, ti.getBoxedBoolean());
    assertEquals(new Byte((byte)0x42), ti.getBoxedByte());
    assertEquals(new Character('A'), ti.getBoxedChar());
    assertEquals(new Double(Math.PI), ti.getBoxedDouble());
    assertEquals(new Float((float)Math.E), ti.getBoxedFloat());
    assertEquals(new Integer(42), ti.getBoxedInt());
    assertEquals(new Long(43), ti.getBoxedLong());
    assertEquals(new Short((short)44), ti.getBoxedShort());
  }
  
  public void testClassPolicyNamedObject() throws JSONWrapperException {
    ClassPolicyNamedInterface ni =
        (ClassPolicyNamedInterface)GWT.create(ClassPolicyNamedInterface.class);
    
    ni.setJSONData("{test:\"Hello world\"}");
    
    assertEquals("Hello world", ni.getHello());
  }
  
  /**
   * Test accessors of nested Lists of varying parameter types.
   */
  public void testListGetters() throws JSONWrapperException {
    ListInterface ai = (ListInterface)GWT.create(ListInterface.class);
    assertNotNull(ai.getRank1());
    
    ai.setJSONData(ARRAY_DATA);
    
    assertTrue(0 == ai.getBasicInt());
    
    List rank1 = ai.getRank1();
    for (int i = 0; i < ARRAY_INT.length; i++) {
      assertEquals("rank1, index " + i,
          new Integer(ARRAY_INT[i]), rank1.get(i));
    }
    
    List string1 = ai.getString1();
    assertEquals(4, string1.size());
    assertEquals("this", string1.get(0));
    assertEquals("is", string1.get(1));
    assertEquals("a", string1.get(2));
    assertEquals("test", string1.get(3));
    
    // Reset
    rank1.clear();
    string1.clear();
    assertEquals(0, rank1.size());
    assertEquals(0, string1.size());
    
    // Try adding to the list
    for (int i = 0; i < ARRAY_INT.length; i++) {
      rank1.add(new Integer(ARRAY_INT[i]));
    }
    for (int i = 0; i < ARRAY_INT.length; i++) {
      assertEquals("rank1, index " + i,
          new Integer(ARRAY_INT[i]), rank1.get(i));
    }
    
    List rank2 = ai.getRank2();
    assertTrue("rank2 of incorrect length " + rank2.size(),
        ARRAY_INT.length == rank2.size());
    for (int i = 0; i < ARRAY_INT.length; i++) {
      for (int j = 0; j < ARRAY_INT.length; j++) {
        assertEquals("rank2, index " + j,
            new Integer(ARRAY_INT[j]), ((List)rank2.get(i)).get(j));
      }
    }
    
    List rank3 = ai.getRank3();
    assertTrue("rank3 of incorrect length " + rank3.size(),
        ARRAY_INT.length == rank3.size());
    for (int i = 0; i < ARRAY_INT.length; i++) {
      for (int j = 0; j < ARRAY_INT.length; j++) {
        for (int k = 0; k < ARRAY_INT.length; k++) {
          assertEquals("rank3, index " + k,
              new Integer(ARRAY_INT[k]), ((List)((List)rank3.get(i)).get(j)).get(k));
        }
      }
    }

    List partialWrappers = ai.getPartialWrappers();
    assertEquals(2, ((PartialWrapper)partialWrappers.get(0)).multiply());
    assertEquals(12, ((PartialWrapper)partialWrappers.get(1)).multiply());
    assertEquals(30, ((PartialWrapper)partialWrappers.get(2)).multiply());
  }
  
  public void testListInitialState() throws JSONWrapperException {
    ListInterface ai = (ListInterface)GWT.create(ListInterface.class);
    assertNotNull(ai.getRank1());
    assertEquals(0, ai.getRank1().size());
  }
  
  public void testListSetters() {
    ListInterface ai = (ListInterface)GWT.create(ListInterface.class);
    
    List wrappers = ai.getPartialWrappers();
    int a = 1;
    int b = 2;
    for (int i = 0; i < 10; i++) {
      PartialWrapper pw = (PartialWrapper)GWT.create(PartialWrapper.class);
      pw.setA(a++);
      pw.setB(b++);
      
      wrappers.add(pw);
    }
    
    a = 1;
    b = 2;
    // Intential call to getPW() to ensure that this behavior works correctly
    for (Iterator i = ai.getPartialWrappers().iterator(); i.hasNext();) {
      PartialWrapper pw = (PartialWrapper)i.next();
      assertEquals(a++ * b++, pw.multiply());
    }
    
    
    // Make sure that setJSList() works as expected
    ListInterface ai2 = (ListInterface)GWT.create(ListInterface.class);
    ai2.setPartialWrappers(ai.getPartialWrappers());
    a = 1;
    b = 2;
    for (Iterator i = ai2.getPartialWrappers().iterator(); i.hasNext();) {
      PartialWrapper pw = (PartialWrapper)i.next();
      assertEquals(a++ * b++, pw.multiply());
    }
    
    // Mutate original and see if it's reflected in secondary.
    PartialWrapper pw = (PartialWrapper)wrappers.get(0);
    pw.setA(10);
    pw.setB(10);
    assertEquals(pw.multiply(), ((PartialWrapper)ai2.getPartialWrappers().get(0)).multiply());
  }
  
  public void testNamedObject() throws JSONWrapperException {
    NamedInterface ni = (NamedInterface)GWT.create(NamedInterface.class);
    ni.setJSONData("{HELLO:\"Hello world\"}");
    
    assertEquals("Hello world", ni.getHello());
    
    NamedInterface ni2 = (NamedInterface)GWT.create(NamedInterface.class);
    ni2.setJavaScriptObject(ni.getJavaScriptObject());
    assertEquals("Hello world", ni2.getHello());

    // Check that objects sharing the same backing object reflect one another's
    // changes
    ni2.setHello("foo");
    assertEquals("foo", ni.getHello());
  }
  
  public void testObjectGetters() throws JSONWrapperException {
    TreeInterface ti1 =
        (TreeInterface)GWT.create(TreeInterface.class);
    assertTrue(ti1 != null);
    assertNull(ti1.getLeft());
    assertNull(ti1.getRight());
    
    ti1.setJSONData("{value:42, left:{value:43}, right:{value:44}}");
    assertNotNull(ti1.getLeft());
    assertNotNull(ti1.getRight());
    assertTrue(ti1.getValue() == 42);
    assertTrue(ti1.getLeft().getValue() == 43);
    assertTrue(ti1.getRight().getValue() == 44);
  };

  public void testObjectSetters() {
    TreeInterface ti1 =
        (TreeInterface)GWT.create(TreeInterface.class);
    assertTrue(ti1 != null);
    assertNull(ti1.getLeft());
    assertNull(ti1.getRight());
    
    TreeInterface ti2 =
        (TreeInterface)GWT.create(TreeInterface.class);
    assertTrue(ti2 != null);
    
    TreeInterface ti3 =
        (TreeInterface)GWT.create(TreeInterface.class);
    assertTrue(ti3 != null);

    ti1.setValue(1);
    ti2.setValue(2);
    ti3.setValue(3);
    
    ti1.setLeft(ti2);
    ti1.setRight(ti3);
    
    assertTrue(ti1.getLeft().getValue() == 2);
    assertTrue(ti1.getRight().getValue() == 3);
  }
  
  public void testPartialWrapper() throws JSONWrapperException {
    PartialWrapper pw = (PartialWrapper)GWT.create(PartialWrapper.class);
    pw.setJSONData("{a:3, b:4, c:5}");
    assertTrue(12 == pw.multiply());
    
    // Test that we don't override existing getters
    assertTrue(100 == pw.getC());
  }
  
  public void testPolicyNamedObject() throws JSONWrapperException {
    PolicyNamedInterface ni = (PolicyNamedInterface)GWT.create(PolicyNamedInterface.class);
    ni.setJSONData("{HELLO:\"Hello world\"}");
    
    assertEquals("Hello world", ni.getHello());
    
    PolicyNamedInterface ni2 = (PolicyNamedInterface)GWT.create(PolicyNamedInterface.class);
    ni2.setHello("Hello world");
//    assertTrue(ni2.getJSONData().indexOf("HELLO") != -1);
//    ni2.setJSONData(ni2.getJSONData());
    assertEquals("Hello world", ni2.getHello());
  }
  
  public void testPrimitiveSetters() {
    PrimitiveInterface ti =
        (PrimitiveInterface)GWT.create(PrimitiveInterface.class);
    assertTrue(ti != null);

    ti.setUnboxedBoolean(true);
    ti.setUnboxedByte((byte)0x42);
    ti.setUnboxedChar('A');
    ti.setUnboxedDouble(Math.PI);
    ti.setUnboxedFloat((float)Math.E);
    ti.setUnboxedInt(42);
    ti.setUnboxedLong(43);
    ti.setUnboxedShort((short)44);
    
    assertTrue(ti.getUnboxedBoolean());
    assertTrue(ti.getUnboxedByte() == 0x42);
    assertTrue(ti.getUnboxedChar() == 'A');
    assertTrue(ti.getUnboxedDouble() == Math.PI);
    assertTrue(ti.getUnboxedFloat() == (float)Math.E);
    assertTrue(ti.getUnboxedInt() == 42);
    assertTrue(ti.getUnboxedLong() == 43);
    assertTrue(ti.getUnboxedShort() == 44);
  }
  
  public void testStringSetters() {
    PrimitiveInterface ti =
        (PrimitiveInterface)GWT.create(PrimitiveInterface.class);
    
    assertTrue(ti != null);

    // Check that non-existence of a property
    assertNull(ti.getHello());
    
    ti.setHello("Hello world");
    assertEquals("Hello world", ti.getHello());
    
    ti.setHello("");
    assertEquals("", ti.getHello());
    
    ti.setHello(null);
    assertNull(ti.getHello());
  }
  
  public void testSuperInheritor() {
    SuperInheritor si = (SuperInheritor)GWT.create(SuperInheritor.class);
    assertTrue(si instanceof PartialWrapper);
    assertTrue(si instanceof PrimitiveInterface);
    assertTrue(si instanceof ListInterface);
  }
  
  /**
   * Test the state of an uninitialized wrapper.
   */
  public void testUninitializedWrapper() {
    PrimitiveInterface ti =
        (PrimitiveInterface)GWT.create(PrimitiveInterface.class);
    assertTrue(ti != null);

    assertNull(ti.getHello());
    
    assertEquals(Boolean.FALSE, ti.getBoxedBoolean());
    assertEquals(new Byte((byte)0), ti.getBoxedByte());
    assertEquals(new Character(' '), ti.getBoxedChar());
    assertEquals(new Double(0), ti.getBoxedDouble());
    assertEquals(new Float(0), ti.getBoxedFloat());
    assertEquals(new Integer(0), ti.getBoxedInt());
    assertEquals(new Long(0), ti.getBoxedLong());
    assertEquals(new Short((short)0), ti.getBoxedShort());
    
    assertFalse(ti.getUnboxedBoolean());
    assertTrue(0 == ti.getUnboxedByte());
    assertTrue(' ' == ti.getUnboxedChar());
    assertTrue(0.0 == ti.getUnboxedDouble());
    assertTrue(0.0 == ti.getUnboxedFloat());
    assertTrue(0 == ti.getUnboxedInt());
    assertTrue(0 == ti.getUnboxedLong());
    assertTrue(0 == ti.getUnboxedShort());
  }
}
