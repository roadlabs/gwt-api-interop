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
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests the flyweight variant of the JSWrapper.
 */
public class JSFlyweightWrapperTest extends GWTTestCase {
  /**
   * Contains getters for all primitive types.
   * 
   * @gwt.beanProperties
   */
  static interface PrimitiveInterface extends JSFlyweightWrapper {
    /**
     * @gwt.constructor Object
     */
    public JavaScriptObject construct();

    public Boolean getBoxedBoolean(JavaScriptObject jso);

    public Byte getBoxedByte(JavaScriptObject jso);

    public Character getBoxedChar(JavaScriptObject jso);

    public Double getBoxedDouble(JavaScriptObject jso);

    public Float getBoxedFloat(JavaScriptObject jso);

    public Integer getBoxedInt(JavaScriptObject jso);

    public Long getBoxedLong(JavaScriptObject jso);

    public Short getBoxedShort(JavaScriptObject jso);

    public String getHello(JavaScriptObject jso);

    public boolean getUnboxedBoolean(JavaScriptObject jso);

    public byte getUnboxedByte(JavaScriptObject jso);

    public char getUnboxedChar(JavaScriptObject jso);

    public double getUnboxedDouble(JavaScriptObject jso);

    public float getUnboxedFloat(JavaScriptObject jso);

    public int getUnboxedInt(JavaScriptObject jso);

    public long getUnboxedLong(JavaScriptObject jso);

    public short getUnboxedShort(JavaScriptObject jso);

    public void setBoxedBoolean(JavaScriptObject jso, Boolean value);

    public void setBoxedByte(JavaScriptObject jso, Byte value);

    public void setBoxedChar(JavaScriptObject jso, Character value);

    public void setBoxedDouble(JavaScriptObject jso, Double value);

    public void setBoxedFloat(JavaScriptObject jso, Float value);

    public void setBoxedInt(JavaScriptObject jso, Integer value);

    public void setBoxedLong(JavaScriptObject jso, Long value);

    public void setBoxedShort(JavaScriptObject jso, Short value);

    public void setHello(JavaScriptObject jso, String hello);

    public void setUnboxedBoolean(JavaScriptObject jso, boolean value);

    public void setUnboxedByte(JavaScriptObject jso, byte value);

    public void setUnboxedChar(JavaScriptObject jso, char value);

    public void setUnboxedDouble(JavaScriptObject jso, double value);

    public void setUnboxedFloat(JavaScriptObject jso, float value);

    public void setUnboxedInt(JavaScriptObject jso, int value);

    public void setUnboxedLong(JavaScriptObject jso, long value);

    public void setUnboxedShort(JavaScriptObject jso, short value);
  }
  /**
   * @gwt.beanProperties
   */
  static interface TreeInterface extends JSFlyweightWrapper {
    /**
     * @gwt.constructor Object
     */
    public JavaScriptObject construct();

    public JavaScriptObject getLeft(JavaScriptObject jso);

    public JavaScriptObject getRight(JavaScriptObject jso);

    public int getValue(JavaScriptObject jso);

    public void setLeft(JavaScriptObject jso, JavaScriptObject ti);

    public void setRight(JavaScriptObject jso, JavaScriptObject ti);

    public void setValue(JavaScriptObject jso, int value);
  }

  public String getModuleName() {
    return "com.google.gwt.jsio.JSIO";
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
    primitiveInterface.setBoxedLong(jso, new Long(43));
    primitiveInterface.setBoxedShort(jso, new Short((short) 44));

    assertEquals(Boolean.TRUE, primitiveInterface.getBoxedBoolean(jso));
    assertEquals(new Byte((byte) 0x42), primitiveInterface.getBoxedByte(jso));
    assertEquals(new Character('A'), primitiveInterface.getBoxedChar(jso));
    assertEquals(new Double(Math.PI), primitiveInterface.getBoxedDouble(jso));
    assertEquals(new Float((float) Math.E),
        primitiveInterface.getBoxedFloat(jso));
    assertEquals(new Integer(42), primitiveInterface.getBoxedInt(jso));
    assertEquals(new Long(43), primitiveInterface.getBoxedLong(jso));
    assertEquals(new Short((short) 44), primitiveInterface.getBoxedShort(jso));
  }

  public void testObjectGetters() throws JSONWrapperException {
    TreeInterface treeInterface = (TreeInterface) GWT.create(TreeInterface.class);

    JavaScriptObject one = treeInterface.construct();

    assertTrue(treeInterface != null);
    assertNull(treeInterface.getLeft(one));
    assertNull(treeInterface.getRight(one));

    JavaScriptObject two = makeTreeData();

    assertNotNull(treeInterface.getLeft(two));
    assertNotNull(treeInterface.getRight(two));
    assertTrue(treeInterface.getValue(two) == 42);
    assertTrue(treeInterface.getValue(treeInterface.getLeft(two)) == 43);
    assertTrue(treeInterface.getValue(treeInterface.getRight(two)) == 44);
  }

  public void testObjectSetters() {
    TreeInterface treeInterface = (TreeInterface) GWT.create(TreeInterface.class);
    assertNotNull(treeInterface);

    JavaScriptObject one = treeInterface.construct();

    assertNull(treeInterface.getLeft(one));
    assertNull(treeInterface.getRight(one));

    JavaScriptObject two = treeInterface.construct();

    JavaScriptObject three = treeInterface.construct();

    treeInterface.setValue(one, 1);
    treeInterface.setValue(two, 2);
    treeInterface.setValue(three, 3);

    treeInterface.setLeft(one, two);
    treeInterface.setRight(one, three);

    assertTrue(treeInterface.getValue(treeInterface.getLeft(one)) == 2);
    assertTrue(treeInterface.getValue(treeInterface.getRight(one)) == 3);
  };

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
    primitiveInterface.setUnboxedLong(jso, 43);
    primitiveInterface.setUnboxedShort(jso, (short) 44);

    assertTrue(primitiveInterface.getUnboxedBoolean(jso));
    assertTrue(primitiveInterface.getUnboxedByte(jso) == 0x42);
    assertTrue(primitiveInterface.getUnboxedChar(jso) == 'A');
    assertTrue(primitiveInterface.getUnboxedDouble(jso) == Math.PI);
    assertTrue(primitiveInterface.getUnboxedFloat(jso) == (float) Math.E);
    assertTrue(primitiveInterface.getUnboxedInt(jso) == 42);
    assertTrue(primitiveInterface.getUnboxedLong(jso) == 43);
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
    assertEquals(new Long(0), primitiveInterface.getBoxedLong(jso));
    assertEquals(new Short((short) 0), primitiveInterface.getBoxedShort(jso));

    assertFalse(primitiveInterface.getUnboxedBoolean(jso));
    assertTrue(0 == primitiveInterface.getUnboxedByte(jso));
    assertTrue(' ' == primitiveInterface.getUnboxedChar(jso));
    assertTrue(0.0 == primitiveInterface.getUnboxedDouble(jso));
    assertTrue(0.0 == primitiveInterface.getUnboxedFloat(jso));
    assertTrue(0 == primitiveInterface.getUnboxedInt(jso));
    assertTrue(0 == primitiveInterface.getUnboxedLong(jso));
    assertTrue(0 == primitiveInterface.getUnboxedShort(jso));
  }

  private native JavaScriptObject makeTreeData() /*-{
   return {value:42, left:{value:43}, right:{value:44}};
   }-*/;
}
