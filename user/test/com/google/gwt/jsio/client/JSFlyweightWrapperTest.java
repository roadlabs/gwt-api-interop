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

/**
 * Tests the flyweight variant of the JSWrapper.
 */
public class JSFlyweightWrapperTest extends GWTTestCase {

  static interface ArrayInterface extends JSFlyweightWrapper {
    void clear(JavaScriptObject jso);

    @Constructor("$wnd.JSFlyweightWrapperTest.ArrayTest")
    JavaScriptObject construct();

    String getIndex(JavaScriptObject jso, int i);

    @FieldName("getArray")
    JSList<Integer> getIntegerArray(JavaScriptObject jso);

    int getLength(JavaScriptObject jso);

    @FieldName("getArray")
    JSList<String> getStringArray(JavaScriptObject jso);

    @FieldName("setArray")
    void setIntegerArray(JavaScriptObject jso, JSList<Integer> arrayArg);

    @FieldName("setArray")
    void setStringArray(JavaScriptObject jso, JSList<String> arrayArg);
  }

  static interface ConstructedInterface extends JSFlyweightWrapper {
    @Constructor("$wnd.JSFlyweightWrapperTest.ConstructedObject")
    JavaScriptObject construct(String a, int b, Tree t);

    int getInt(JavaScriptObject obj);

    String getString(JavaScriptObject obj);

    Tree getTree(JavaScriptObject obj);
  }

  /**
   * Contains getters for all primitive types.
   */
  @BeanProperties
  static interface PrimitiveInterface extends JSFlyweightWrapper {

    @Constructor("$wnd.Object")
    JavaScriptObject construct();

    Boolean getBoxedBoolean(JavaScriptObject jso);

    Byte getBoxedByte(JavaScriptObject jso);

    Character getBoxedChar(JavaScriptObject jso);

    Double getBoxedDouble(JavaScriptObject jso);

    Float getBoxedFloat(JavaScriptObject jso);

    Integer getBoxedInt(JavaScriptObject jso);

    Short getBoxedShort(JavaScriptObject jso);

    String getHello(JavaScriptObject jso);

    boolean getUnboxedBoolean(JavaScriptObject jso);

    byte getUnboxedByte(JavaScriptObject jso);

    char getUnboxedChar(JavaScriptObject jso);

    double getUnboxedDouble(JavaScriptObject jso);

    float getUnboxedFloat(JavaScriptObject jso);

    int getUnboxedInt(JavaScriptObject jso);

    short getUnboxedShort(JavaScriptObject jso);

    void setBoxedBoolean(JavaScriptObject jso, Boolean value);

    void setBoxedByte(JavaScriptObject jso, Byte value);

    void setBoxedChar(JavaScriptObject jso, Character value);

    void setBoxedDouble(JavaScriptObject jso, Double value);

    void setBoxedFloat(JavaScriptObject jso, Float value);

    void setBoxedInt(JavaScriptObject jso, Integer value);

    void setBoxedShort(JavaScriptObject jso, Short value);

    void setHello(JavaScriptObject jso, String hello);

    void setUnboxedBoolean(JavaScriptObject jso, boolean value);

    void setUnboxedByte(JavaScriptObject jso, byte value);

    void setUnboxedChar(JavaScriptObject jso, char value);

    void setUnboxedDouble(JavaScriptObject jso, double value);

    void setUnboxedFloat(JavaScriptObject jso, float value);

    void setUnboxedInt(JavaScriptObject jso, int value);

    void setUnboxedShort(JavaScriptObject jso, short value);
  }

  static class Tree {
    private static final TreeInterface IMPL = (TreeInterface) GWT.create(TreeInterface.class);

    static Tree createPeer(JavaScriptObject jso) {
      if (jso == null) {
        return null;
      }

      Tree toReturn = new Tree(jso);
      assertSame(toReturn, JSFlyweightWrapper.Util.getJavaPeer(jso));
      return toReturn;
    }

    final JavaScriptObject jsoPeer;

    Tree() {
      this(IMPL.construct());
    }

    Tree(JavaScriptObject jsoPeer) {
      this.jsoPeer = jsoPeer;
      IMPL.bind(jsoPeer, this);
    }

    public Tree getLeft() {
      return IMPL.getLeft(this);
    }

    public Tree getRight() {
      return IMPL.getRight(this);
    }

    public int getValue() {
      return IMPL.getValue(this);
    }

    public void setLeft(Tree left) {
      IMPL.setLeft(this, left);
    }

    public void setRight(Tree right) {
      IMPL.setRight(this, right);
    }

    public void setValue(int value) {
      IMPL.setValue(this, value);
    }
  }

  @BeanProperties
  static interface TreeInterface extends JSFlyweightWrapper {
    @Binding
    void bind(JavaScriptObject jso, Tree node);

    @Constructor("$wnd.Object")
    JavaScriptObject construct();

    Tree getLeft(Tree parent);

    Tree getRight(Tree parent);

    int getValue(Tree node);

    void setLeft(Tree parent, Tree left);

    void setRight(Tree parent, Tree right);

    void setValue(Tree node, int value);
  }

  private static native void nativeTestIntegerArrayToJS(JavaScriptObject peer) /*-{
    // peer should be an instance of ArrayTest
    if (! peer instanceof $wnd.JSFlyweightWrapperTest.ArrayTest) {
      throw new Error("Expected instance of $wnd.JSFlyweightWrapperTest.ArrayTest");
    }
    var arr = peer.getArray(); 
    if (arr.length != 3) {
      throw new Error("Expected three elements in the array got " + arr.length);
    }
  
    if (arr[0] != 1) {
      throw new Error("Expected arr[0] == 0 but got " + arr[0]);
    }
    if (arr[1] != 100) {
      throw new Error("Expected arr[1] == 100 but got " + arr[1]);
    }
    if (arr[2] != -100) {
      throw new Error("Expected arr[2] == -100 but got " + arr[2]);
    } 
  }-*/;

  private static native void nativeTestStringArrayToJS(JavaScriptObject peer) /*-{
  // peer should be an instance of ArrayTest
  if (! peer instanceof $wnd.JSFlyweightWrapperTest.ArrayTest) {
    throw new Error("Expected instance of $wnd.JSFlyweightWrapperTest.ArrayTest");
  }
  var arr = peer.getArray(); 
  if (arr.length != 3) {
    throw new Error("Expected three elements in the array, got " + arr.length);
  }
  if (arr[0] != "Peter") {
    throw new Error ("Expected arr[0] == Mary got " + arr[0] );
  }
  if (arr[1] != "Paul")  {
    throw new Error ("Expected arr[1] == Paul got " + arr[1] );
  } 
  if (arr[2] != "Mary") {
    throw new Error ("Expected arr[2] == Mary got " + arr[2] );
  }
}-*/;

  @Override
  public String getModuleName() {
    return "com.google.gwt.jsio.JSIOTest";
  }

  public void testArrayIntegerToJS() {
    int arrayLength = 3;
    ArrayInterface arrayInterface = GWT.create(ArrayInterface.class);
    JavaScriptObject jso = arrayInterface.construct();

    JSList<Integer> jslist = arrayInterface.getIntegerArray(jso);

    Integer intArray[] = new Integer[arrayLength];

    intArray[0] = Integer.valueOf(1);
    intArray[1] = Integer.valueOf(100);
    intArray[2] = Integer.valueOf(-100);

    for (Integer i : intArray) {
      jslist.add(i);
    }

    arrayInterface.setIntegerArray(jso, jslist);

    assertTrue("Expected 3 elements in the array",
        arrayInterface.getLength(jso) == arrayLength);
    JSList<Integer> jslist2 = arrayInterface.getIntegerArray(jso);
    assertTrue("stringArray2 expected to be 3 elements long",
        jslist2.size() == arrayLength);
    for (int i = 0; i < arrayLength; ++i) {
      assertTrue("Element: " + i + " didn't match.  " + intArray[i] + " != "
          + jslist2.get(i), jslist2.get(i).equals(intArray[i]));
    }
    // Do a few tests from the JavaScript side to make sure the data is intact
    nativeTestIntegerArrayToJS(jso);

    arrayInterface.clear(jso);
    assertTrue(arrayInterface.getLength(jso) == 0);
  }

  /**
   * Test creating an array in Java, accessing it in JS.
   */
  public void testArrayStringArrayToJS() {
    int arrayLength = 3;
    ArrayInterface arrayInterface = GWT.create(ArrayInterface.class);
    JavaScriptObject jso = arrayInterface.construct();

    JSList<String> jslist = arrayInterface.getStringArray(jso);

    String stringArray[] = new String[arrayLength];
    stringArray[0] = "Peter";
    stringArray[1] = "Paul";
    stringArray[2] = "Mary";

    for (String s : stringArray) {
      jslist.add(s);
    }

    arrayInterface.setStringArray(jso, jslist);

    assertTrue("Expected 3 elements in the array",
        arrayInterface.getLength(jso) == arrayLength);
    JSList<String> jslist2 = arrayInterface.getStringArray(jso);
    assertTrue("stringArray2 expected to be 3 elements long",
        jslist2.size() == arrayLength);
    for (int i = 0; i < arrayLength; ++i) {
      assertTrue("Element: " + i + " didn't match.  " + stringArray[i] + " != "
          + jslist2.get(i), jslist2.get(i).equals(stringArray[i]));
    }

    // Do a few tests from the JavaScript side to make sure the data is intact
    nativeTestStringArrayToJS(jso);

    arrayInterface.clear(jso);
    assertTrue(arrayInterface.getLength(jso) == 0);
  }

  public void testBoxedSetters() {
    PrimitiveInterface primitiveInterface = (PrimitiveInterface) GWT.create(PrimitiveInterface.class);
    assertTrue(primitiveInterface != null);

    JavaScriptObject jso = primitiveInterface.construct();

    primitiveInterface.setBoxedBoolean(jso, Boolean.TRUE);
    primitiveInterface.setBoxedByte(jso, new Byte((byte) 0x42));
    primitiveInterface.setBoxedChar(jso, new Character('A'));
    primitiveInterface.setBoxedDouble(jso, new Double(Math.PI));
    primitiveInterface.setBoxedFloat(jso, new Float((float) Math.E));
    primitiveInterface.setBoxedInt(jso, new Integer(42));
    primitiveInterface.setBoxedShort(jso, new Short((short) 44));

    assertEquals(Boolean.TRUE, primitiveInterface.getBoxedBoolean(jso));
    assertEquals(new Byte((byte) 0x42), primitiveInterface.getBoxedByte(jso));
    assertEquals(new Character('A'), primitiveInterface.getBoxedChar(jso));
    assertEquals(new Double(Math.PI), primitiveInterface.getBoxedDouble(jso));
    assertEquals(new Float((float) Math.E),
        primitiveInterface.getBoxedFloat(jso));
    assertEquals(new Integer(42), primitiveInterface.getBoxedInt(jso));
    assertEquals(new Short((short) 44), primitiveInterface.getBoxedShort(jso));
  }

  public void testConstructor() {
    Tree tree = new Tree();
    tree.setValue(42);

    ConstructedInterface ci = (ConstructedInterface) GWT.create(ConstructedInterface.class);

    JavaScriptObject jso = ci.construct("Hello world", 42, tree);
    assertEquals("Hello world", ci.getString(jso));
    assertEquals(42, ci.getInt(jso));
    assertSame(tree, ci.getTree(jso));
  }

  public void testObjectGetters() {
    Tree parent = new Tree();

    assertNotNull(parent);
    assertSame(parent, JSFlyweightWrapper.Util.getJavaPeer(parent.jsoPeer));
    assertNull(parent.getLeft());
    assertNull(parent.getRight());

    parent = new Tree(makeTreeData());

    assertNotNull(parent.getLeft());
    assertNotNull(parent.getRight());
    assertEquals(42, parent.getValue());
    assertEquals(43, parent.getLeft().getValue());
    assertEquals(44, parent.getRight().getValue());
  }

  public void testObjectSetters() {
    Tree one = new Tree();

    assertNull(one.getLeft());
    assertNull(one.getRight());

    Tree two = new Tree();
    Tree three = new Tree();

    one.setValue(1);
    two.setValue(2);
    three.setValue(3);

    one.setLeft(two);
    one.setRight(three);

    assertSame(two, one.getLeft());
    assertSame(three, one.getRight());
    assertEquals(2, one.getLeft().getValue());
    assertEquals(3, one.getRight().getValue());
  }

  public void testPrimitiveSetters() {
    PrimitiveInterface primitiveInterface = (PrimitiveInterface) GWT.create(PrimitiveInterface.class);
    assertTrue(primitiveInterface != null);

    JavaScriptObject jso = primitiveInterface.construct();

    primitiveInterface.setUnboxedBoolean(jso, true);
    primitiveInterface.setUnboxedByte(jso, (byte) 0x42);
    // there is something wrong with this test case.
    primitiveInterface.setUnboxedChar(jso, 'A');
    primitiveInterface.setUnboxedDouble(jso, Math.PI);
    primitiveInterface.setUnboxedFloat(jso, (float) Math.E);
    primitiveInterface.setUnboxedInt(jso, 42);
    primitiveInterface.setUnboxedShort(jso, (short) 44);

    assertTrue(primitiveInterface.getUnboxedBoolean(jso));
    assertTrue(primitiveInterface.getUnboxedByte(jso) == 0x42);
    assertTrue(primitiveInterface.getUnboxedChar(jso) == 'A');
    assertTrue(primitiveInterface.getUnboxedDouble(jso) == Math.PI);
    assertTrue(primitiveInterface.getUnboxedFloat(jso) == (float) Math.E);
    assertTrue(primitiveInterface.getUnboxedInt(jso) == 42);
    assertTrue(primitiveInterface.getUnboxedShort(jso) == 44);
  }

  public void testStringSetters() {
    PrimitiveInterface primitiveInterface = (PrimitiveInterface) GWT.create(PrimitiveInterface.class);

    assertTrue(primitiveInterface != null);

    JavaScriptObject jso = primitiveInterface.construct();

    // Check that non-existence of a property
    assertNull(primitiveInterface.getHello(jso));

    primitiveInterface.setHello(jso, "Hello world");
    assertEquals("Hello world", primitiveInterface.getHello(jso));

    primitiveInterface.setHello(jso, "");
    assertEquals("", primitiveInterface.getHello(jso));

    primitiveInterface.setHello(jso, null);
    assertNull(primitiveInterface.getHello(jso));
  }

  /**
   * Test the state of an uninitialized wrapper.
   */
  public void testUninitializedWrapper() {
    PrimitiveInterface primitiveInterface = (PrimitiveInterface) GWT.create(PrimitiveInterface.class);

    assertTrue(primitiveInterface != null);

    JavaScriptObject jso = primitiveInterface.construct();

    assertNull(primitiveInterface.getHello(jso));

    assertEquals(Boolean.FALSE, primitiveInterface.getBoxedBoolean(jso));
    assertEquals(new Byte((byte) 0), primitiveInterface.getBoxedByte(jso));
    assertEquals(new Character(' '), primitiveInterface.getBoxedChar(jso));
    assertEquals(new Double(0), primitiveInterface.getBoxedDouble(jso));
    assertEquals(new Float(0), primitiveInterface.getBoxedFloat(jso));
    assertEquals(new Integer(0), primitiveInterface.getBoxedInt(jso));
    assertEquals(new Short((short) 0), primitiveInterface.getBoxedShort(jso));

    assertFalse(primitiveInterface.getUnboxedBoolean(jso));
    assertTrue(0 == primitiveInterface.getUnboxedByte(jso));
    assertTrue(' ' == primitiveInterface.getUnboxedChar(jso));
    assertTrue(0.0 == primitiveInterface.getUnboxedDouble(jso));
    assertTrue(0.0 == primitiveInterface.getUnboxedFloat(jso));
    assertTrue(0 == primitiveInterface.getUnboxedInt(jso));
    assertTrue(0 == primitiveInterface.getUnboxedShort(jso));
  }

  private native JavaScriptObject makeTreeData() /*-{
    return {value:42, left:{value:43}, right:{value:44}};
  }-*/;
}
