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

  static interface ConstructedInterface extends JSFlyweightWrapper {
    @Constructor("$wnd.JSFlyweightWrapperTest.ConstructedObject")
    JavaScriptObject construct(String a, int b, Tree t);

    int getInt(JavaScriptObject obj);

    String getString(JavaScriptObject obj);

    Tree getTree(JavaScriptObject obj);
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

  @Override
  public String getModuleName() {
    return "com.google.gwt.jsio.JSIOTest";
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

  public void testObjectGetters() throws JSONWrapperException {
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
