/*
 * Copyright 2008 Google Inc.
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
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.junit.client.GWTTestCase;

import java.util.List;

/**
 * Tests data-access functions of the wrapper generation code.
 */
public class JSONWrapperTest extends GWTTestCase {

  /**
   * A class that uses class-based naming policy.
   */
  @BeanProperties
  @NamePolicy("com.google.gwt.jsio.rebind.TestNamePolicy")
  static interface ClassPolicyNamedInterface extends
      JSWrapper<ClassPolicyNamedInterface> {
    String getHello();

    void setHello(String hello);
  }

  @BeanProperties
  static interface ListInterface extends JSWrapper<ListInterface> {
    int getBasicInt();

    JSList<PartialWrapper> getPartialWrappers();

    JSList<Integer> getRank1();

    JSList<JSList<Integer>> getRank2();

    JSList<JSList<JSList<Integer>>> getRank3();

    JSList<String> getString1();

    void setPartialWrappers(JSList<PartialWrapper> list);
  }

  /**
   * A class that uses method-based naming policy.
   */
  @BeanProperties
  static interface NamedInterface extends JSWrapper<NamedInterface> {
    @FieldName("HELLO")
    String getHello();

    void setHello(String hello);
  }

  /**
   * Test "finishing" an abstract base class.
   */
  @BeanProperties
  abstract static class PartialWrapper implements JSWrapper<PartialWrapper> {
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
   */
  @BeanProperties
  @NamePolicy(NamePolicy.UPPER)
  static interface PolicyNamedInterface extends JSWrapper<PolicyNamedInterface> {
    String getHello();

    void setHello(String hello);
  }

  /**
   * Contains getters for all primitive types.
   */
  @BeanProperties
  static interface PrimitiveInterface extends JSWrapper<PrimitiveInterface> {
    Boolean getBoxedBoolean();

    Byte getBoxedByte();

    Character getBoxedChar();

    Double getBoxedDouble();

    Float getBoxedFloat();

    Integer getBoxedInt();

    Short getBoxedShort();

    String getHello();

    boolean getUnboxedBoolean();

    byte getUnboxedByte();

    char getUnboxedChar();

    double getUnboxedDouble();

    float getUnboxedFloat();

    int getUnboxedInt();

    short getUnboxedShort();

    void setBoxedBoolean(Boolean value);

    void setBoxedByte(Byte value);

    void setBoxedChar(Character value);

    void setBoxedDouble(Double value);

    void setBoxedFloat(Float value);

    void setBoxedInt(Integer value);

    void setBoxedShort(Short value);

    void setHello(String hello);

    void setUnboxedBoolean(boolean value);

    void setUnboxedByte(byte value);

    void setUnboxedChar(char value);

    void setUnboxedDouble(double value);

    void setUnboxedFloat(float value);

    void setUnboxedInt(int value);

    void setUnboxedShort(short value);
  }

  /**
   * This class exists to test object identity behavior of read-only wrappers.
   */
  @BeanProperties
  @ReadOnly
  abstract static class ReadOnlyInterface implements
      JSWrapper<ReadOnlyInterface> {
    public abstract String getHello();

    public abstract JSList<Integer> getNumbers();
  }

  /**
   * Tests interface with a set-only method.
   */
  @BeanProperties
  @NoIdentity
  static interface SetterOnly extends JSWrapper<SetterOnly> {
    void setHello(String hello);
  }

  /**
   * A tree-like structure.
   */
  @BeanProperties
  static interface TreeInterface extends JSWrapper<TreeInterface> {
    TreeInterface getLeft();

    TreeInterface getRight();

    int getValue();

    void setLeft(TreeInterface ti);

    void setRight(TreeInterface ti);

    void setValue(int value);
  }

  /**
   * Represents nested array data.
   */
  private static final String ARRAY_DATA;
  /**
   * A test array, used by makeJsonArray.
   */
  private static final int[] ARRAY_INT;

  static {
    ARRAY_INT = new int[] {0, 1, 2, 3, 4};
    ARRAY_DATA = "{" + "basicInt: 0, " + "rank1: " + makeJsonArray(1) + ", "
        + "rank2: " + makeJsonArray(2) + ", " + "rank3: " + makeJsonArray(3)
        + ", " + "string1: ['this', 'is', 'a', 'test']" + ", "
        + "partialWrappers: [{a:1, b:2}, {a:3, b:4}, {a:5, b:6}]" + "}";
  }

  /**
   * Create a json array of specified rank. Each level will contain
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

  @Override
  public String getModuleName() {
    return "com.google.gwt.jsio.JSIOTest";
  }

  public void testBoxedSetters() {
    PrimitiveInterface ti = (PrimitiveInterface) GWT.create(PrimitiveInterface.class);
    assertTrue(ti != null);

    ti.setBoxedBoolean(Boolean.TRUE);
    ti.setBoxedByte(new Byte((byte) 0x42));
    ti.setBoxedChar(new Character('A'));
    ti.setBoxedDouble(new Double(Math.PI));
    ti.setBoxedFloat(new Float((float) Math.E));
    ti.setBoxedInt(new Integer(42));
    ti.setBoxedShort(new Short((short) 44));

    assertEquals(Boolean.TRUE, ti.getBoxedBoolean());
    assertEquals(new Byte((byte) 0x42), ti.getBoxedByte());
    assertEquals(new Character('A'), ti.getBoxedChar());
    assertEquals(new Double(Math.PI), ti.getBoxedDouble());
    assertEquals(new Float((float) Math.E), ti.getBoxedFloat());
    assertEquals(new Integer(42), ti.getBoxedInt());
    assertEquals(new Short((short) 44), ti.getBoxedShort());
  }

  public void testClassPolicyNamedObject() throws JSONWrapperException {
    ClassPolicyNamedInterface ni = (ClassPolicyNamedInterface) GWT.create(ClassPolicyNamedInterface.class);

    ni.setJSONData("{test:\"Hello world\"}");

    assertEquals("Hello world", ni.getHello());
  }

  /**
   * Test accessors of nested Lists of varying parameter types.
   */
  public void testListGetters() throws JSONWrapperException {
    ListInterface ai = (ListInterface) GWT.create(ListInterface.class);
    assertNotNull(ai.getRank1());

    ai.setJSONData(ARRAY_DATA);

    assertTrue(0 == ai.getBasicInt());

    List<Integer> rank1 = ai.getRank1();
    for (int i = 0; i < ARRAY_INT.length; i++) {
      assertEquals("rank1, index " + i, new Integer(ARRAY_INT[i]), rank1.get(i));
    }

    List<String> string1 = ai.getString1();
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
      assertEquals("rank1, index " + i, new Integer(ARRAY_INT[i]), rank1.get(i));
    }

    List<? extends List<Integer>> rank2 = ai.getRank2();
    assertTrue("rank2 of incorrect length " + rank2.size(),
        ARRAY_INT.length == rank2.size());
    for (int i = 0; i < ARRAY_INT.length; i++) {
      for (int j = 0; j < ARRAY_INT.length; j++) {
        assertEquals("rank2, index " + j, new Integer(ARRAY_INT[j]), rank2.get(
            i).get(j));
      }
    }

    List<? extends List<? extends List<Integer>>> rank3 = ai.getRank3();
    assertTrue("rank3 of incorrect length " + rank3.size(),
        ARRAY_INT.length == rank3.size());
    for (int i = 0; i < ARRAY_INT.length; i++) {
      for (int j = 0; j < ARRAY_INT.length; j++) {
        for (int k = 0; k < ARRAY_INT.length; k++) {
          assertEquals("rank3, index " + k, new Integer(ARRAY_INT[k]),
              (rank3.get(i)).get(j).get(k));
        }
      }
    }

    List<PartialWrapper> partialWrappers = ai.getPartialWrappers();
    assertEquals(2, partialWrappers.get(0).multiply());
    assertEquals(12, partialWrappers.get(1).multiply());
    assertEquals(30, partialWrappers.get(2).multiply());
  }

  public void testListInitialState() throws JSONWrapperException {
    ListInterface ai = (ListInterface) GWT.create(ListInterface.class);
    assertNotNull(ai.getRank1());
    assertEquals(0, ai.getRank1().size());
  }

  public void testListSetters() {
    ListInterface ai = (ListInterface) GWT.create(ListInterface.class);

    List<PartialWrapper> wrappers = ai.getPartialWrappers();
    int a = 1;
    int b = 2;
    for (int i = 0; i < 10; i++) {
      PartialWrapper pw = (PartialWrapper) GWT.create(PartialWrapper.class);
      pw.setA(a++);
      pw.setB(b++);

      wrappers.add(pw);
    }

    a = 1;
    b = 2;
    // Intentional call to getPW() to ensure that this behavior works correctly
    for (PartialWrapper pw : ai.getPartialWrappers()) {
      assertEquals(a++ * b++, pw.multiply());
    }

    // Make sure that setJSList() works as expected
    ListInterface ai2 = (ListInterface) GWT.create(ListInterface.class);
    ai2.setPartialWrappers(ai.getPartialWrappers());
    a = 1;
    b = 2;
    for (PartialWrapper pw : ai2.getPartialWrappers()) {
      assertEquals(a++ * b++, pw.multiply());
    }

    // Mutate original and see if it's reflected in secondary.
    PartialWrapper pw = wrappers.get(0);
    pw.setA(10);
    pw.setB(10);
    assertEquals(pw.multiply(), ai2.getPartialWrappers().get(0).multiply());

    ai2.setPartialWrappers(null);
    assertNull(ai2.getPartialWrappers());
  }

  public void testMultipleWrapperException() {
    NamedInterface ni = (NamedInterface) GWT.create(NamedInterface.class);
    NamedInterface ni2 = (NamedInterface) GWT.create(NamedInterface.class);

    try {
      ni2.setJavaScriptObject(ni.getJavaScriptObject());
      fail("Should have thrown a MultipleWrapperException");
    } catch (MultipleWrapperException e) {
      // Expected behavior
    }
  }

  public void testNamedObject() throws JSONWrapperException {
    NamedInterface ni = (NamedInterface) GWT.create(NamedInterface.class);
    ni.setJSONData("{HELLO:\"Hello world\"}");

    assertEquals("Hello world", ni.getHello());

    // Re-parent the backing JSO
    NamedInterface ni2 = (NamedInterface) GWT.create(NamedInterface.class);
    JavaScriptObject jso = ni.getJavaScriptObject();
    ni.setJavaScriptObject(null);
    ni2.setJavaScriptObject(jso);
    assertEquals("Hello world", ni2.getHello());
  }

  public void testObjectGetters() throws JSONWrapperException {
    TreeInterface ti1 = (TreeInterface) GWT.create(TreeInterface.class);
    assertTrue(ti1 != null);
    assertNull(ti1.getLeft());
    assertNull(ti1.getRight());

    ti1.setJSONData("{value:42, left:{value:43}, right:{value:44}}");
    assertNotNull(ti1.getLeft());
    assertNotNull(ti1.getRight());
    assertTrue(ti1.getValue() == 42);
    assertTrue(ti1.getLeft().getValue() == 43);
    assertTrue(ti1.getRight().getValue() == 44);
  }

  public void testObjectSetters() {
    TreeInterface ti1 = (TreeInterface) GWT.create(TreeInterface.class);
    assertTrue(ti1 != null);
    assertNull(ti1.getLeft());
    assertNull(ti1.getRight());

    TreeInterface ti2 = (TreeInterface) GWT.create(TreeInterface.class);
    assertTrue(ti2 != null);

    TreeInterface ti3 = (TreeInterface) GWT.create(TreeInterface.class);
    assertTrue(ti3 != null);

    ti1.setValue(1);
    ti2.setValue(2);
    ti3.setValue(3);

    ti1.setLeft(ti2);
    ti1.setRight(ti3);

    assertTrue(ti1.getLeft().getValue() == 2);
    assertTrue(ti1.getRight().getValue() == 3);

    ti1.setLeft(null);
    assertNull(ti1.getLeft());
    assertSame(ti3, ti1.getRight());
  }

  public void testPartialWrapper() throws JSONWrapperException {
    PartialWrapper pw = (PartialWrapper) GWT.create(PartialWrapper.class);
    pw.setJSONData("{a:3, b:4, c:5}");
    assertTrue(12 == pw.multiply());

    // Test that we don't override existing getters
    assertTrue(100 == pw.getC());
  }

  public void testPolicyNamedObject() throws JSONWrapperException {
    PolicyNamedInterface ni = (PolicyNamedInterface) GWT.create(PolicyNamedInterface.class);
    ni.setJSONData("{HELLO:\"Hello world\"}");

    assertEquals("Hello world", ni.getHello());

    PolicyNamedInterface ni2 = (PolicyNamedInterface) GWT.create(PolicyNamedInterface.class);
    ni2.setHello("Hello world");
    // assertTrue(ni2.getJSONData().indexOf("HELLO") != -1);
    // ni2.setJSONData(ni2.getJSONData());
    assertEquals("Hello world", ni2.getHello());
  }

  public void testPrimitiveSetters() {
    PrimitiveInterface ti = (PrimitiveInterface) GWT.create(PrimitiveInterface.class);
    assertTrue(ti != null);

    ti.setUnboxedBoolean(true);
    ti.setUnboxedByte((byte) 0x42);
    ti.setUnboxedChar('A');
    ti.setUnboxedDouble(Math.PI);
    ti.setUnboxedFloat((float) Math.E);
    ti.setUnboxedInt(42);
    ti.setUnboxedShort((short) 44);

    assertTrue(ti.getUnboxedBoolean());
    assertTrue(ti.getUnboxedByte() == 0x42);
    assertTrue(ti.getUnboxedChar() == 'A');
    assertTrue(ti.getUnboxedDouble() == Math.PI);
    assertTrue(ti.getUnboxedFloat() == (float) Math.E);
    assertTrue(ti.getUnboxedInt() == 42);
    assertTrue(ti.getUnboxedShort() == 44);
  }

  /**
   * This is more of a manual test to verify that the class is generated without
   * any modifications on the underlying JSO.
   */
  public void testReadOnly() throws JSONWrapperException {
    ReadOnlyInterface ro = (ReadOnlyInterface) GWT.create(ReadOnlyInterface.class);
    ReadOnlyInterface ro2 = (ReadOnlyInterface) GWT.create(ReadOnlyInterface.class);
    ro.setJSONData("{hello:'Hello world', numbers:[1,2,3,4]}");

    // Read-only objects should allow multiple wrappers per JSO because we
    // can't have the __gwtObject field.
    ro2.setJavaScriptObject(ro.getJavaScriptObject());
  }

  public void testSetterOnly() {
    SetterOnly so = (SetterOnly) GWT.create(SetterOnly.class);
    SetterOnly so2 = (SetterOnly) GWT.create(SetterOnly.class);

    so.setHello("Hello world");

    // Check that the gwt.noIdentity annotation works
    so2.setJavaScriptObject(so.getJavaScriptObject());
  }

  public void testStringSetters() {
    PrimitiveInterface ti = (PrimitiveInterface) GWT.create(PrimitiveInterface.class);

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

  /**
   * Test the state of an uninitialized wrapper.
   */
  public void testUninitializedWrapper() {
    PrimitiveInterface ti = (PrimitiveInterface) GWT.create(PrimitiveInterface.class);
    assertTrue(ti != null);

    assertNull(ti.getHello());

    assertEquals(Boolean.FALSE, ti.getBoxedBoolean());
    assertEquals(new Byte((byte) 0), ti.getBoxedByte());
    assertEquals(new Character(' '), ti.getBoxedChar());
    assertEquals(new Double(0), ti.getBoxedDouble());
    assertEquals(new Float(0), ti.getBoxedFloat());
    assertEquals(new Integer(0), ti.getBoxedInt());
    assertEquals(new Short((short) 0), ti.getBoxedShort());

    assertFalse(ti.getUnboxedBoolean());
    assertTrue(0 == ti.getUnboxedByte());
    assertTrue(' ' == ti.getUnboxedChar());
    assertTrue(0.0 == ti.getUnboxedDouble());
    assertTrue(0.0 == ti.getUnboxedFloat());
    assertTrue(0 == ti.getUnboxedInt());
    assertTrue(0 == ti.getUnboxedShort());
  }
}
