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
 * Unit test for issue #20 intended to verify the behavior of a getter is when a
 * property is null or undefined, also testing some valid value cases to insure
 * that changes to null or undefined cases don't break the normal case.
 */
public class JSIOBeanReturnUndefined extends GWTTestCase {

  /**
   * Wraps a JSO for testing undefined properties (JSWrapper)
   */
  @BeanProperties
  @Constructor("$wnd.JSIOBeanReturnUndefined.EmptyObject")
  public interface EmptyObject extends JSWrapper<EmptyObject> {
    int getValueInt();

    JSList<Integer> getValueJSList();

    JavaScriptObject getValueJSO();

    String getValueString();
  }

  /**
   * Wraps a JSO for testing undefined properties (JSFlyweightWrapper)
   */
  @BeanProperties
  public interface EmptyObjectFW extends JSFlyweightWrapper {
    EmptyObjectFW impl = GWT.create(EmptyObjectFW.class);

    @Constructor("$wnd.JSIOBeanReturnUndefined.EmptyObject")
    JavaScriptObject construct();

    int getValueInt(JavaScriptObject jsoPeer);

    JSList<Integer> getValueJSList(JavaScriptObject jsoPeer);

    JavaScriptObject getValueJSO(JavaScriptObject jsoPeer);

    String getValueString(JavaScriptObject jsoPeer);
  }

  /**
   * Wraps a JSO for testing booleans (JSWrapper)
   */
  @BeanProperties
  @Constructor("$wnd.JSIOBeanReturnUndefined.MemberBoolean")
  public interface MemberBoolean extends JSWrapper<MemberBoolean> {
    boolean getValueFalse();

    boolean getValueNull();

    boolean getValueOne();

    boolean getValueTrue();

    boolean getValueUndefined();

    boolean getValueZero();
  }

  /**
   * Wraps a JSO for testing booleans (JSWrapper)
   */
  @BeanProperties
  public interface MemberBooleanFW extends JSFlyweightWrapper {
    MemberBooleanFW impl = GWT.create(MemberBooleanFW.class);

    @Constructor("$wnd.JSIOBeanReturnUndefined.MemberBoolean")
    JavaScriptObject construct();

    boolean getValueFalse(JavaScriptObject jsoPeer);

    boolean getValueNull(JavaScriptObject jsoPeer);

    boolean getValueOne(JavaScriptObject jsoPeer);

    boolean getValueTrue(JavaScriptObject jsoPeer);

    boolean getValueUndefined(JavaScriptObject jsoPeer);

    boolean getValueZero(JavaScriptObject jsoPeer);
  }

  /**
   * Wraps a JSO for testing null values (JSWrapper)
   */
  @BeanProperties
  @Constructor("$wnd.JSIOBeanReturnUndefined.MemberNull")
  public interface MemberNull extends JSWrapper<MemberNull> {
    byte getValueByte();

    char getValueChar();

    double getValueDouble();

    float getValueFloat();

    int getValueInt();

    JSList<Integer> getValueJSList();

    JavaScriptObject getValueJSO();

    short getValueShort();

    String getValueString();
  }

  /**
   * Wraps a JSO for testing null values (JSWrapper)
   */
  @BeanProperties
  public interface MemberNullFW extends JSFlyweightWrapper {
    MemberNullFW impl = GWT.create(MemberNullFW.class);

    @Constructor("$wnd.JSIOBeanReturnUndefined.MemberNull")
    JavaScriptObject construct();

    byte getValueByte(JavaScriptObject jsoPeer);

    char getValueChar(JavaScriptObject jsoPeer);

    double getValueDouble(JavaScriptObject jsoPeer);

    float getValueFloat(JavaScriptObject jsoPeer);

    int getValueInt(JavaScriptObject jsoPeer);

    JSList<Integer> getValueJSList(JavaScriptObject jsoPeer);

    JavaScriptObject getValueJSO(JavaScriptObject jsoPeer);

    short getValueShort(JavaScriptObject jsoPeer);

    String getValueString(JavaScriptObject jsoPeer);
  }

  /**
   * Wraps a JSO for testing null values (JSWrapper)
   */
  @BeanProperties
  @Constructor("$wnd.JSIOBeanReturnUndefined.MemberUndefined")
  public interface MemberUndefined extends JSWrapper<MemberUndefined> {
    int getValueInt();

    JSList<Integer> getValueJSList();

    JavaScriptObject getValueJSO();

    String getValueString();
  }

  /**
   * Wraps a JSO for testing null values (JSFlyweightWrapper)
   */
  @BeanProperties
  public interface MemberUndefinedFW extends JSFlyweightWrapper {
    MemberUndefinedFW impl = GWT.create(MemberUndefinedFW.class);

    @Constructor("$wnd.JSIOBeanReturnUndefined.MemberUndefined")
    JavaScriptObject construct();

    int getValueInt(JavaScriptObject jsoPeer);

    JSList<Integer> getValueJSList(JavaScriptObject jsoPeer);

    JavaScriptObject getValueJSO(JavaScriptObject jsoPeer);

    String getValueString(JavaScriptObject jsoPeer);
  }

  /**
   * Wraps a JSO that returns valid values. (JSWrapper)
   */
  @BeanProperties
  @Constructor("$wnd.JSIOBeanReturnUndefined.MemberValid")
  public interface MemberValid extends JSWrapper<MemberValid> {
    String getValueEmptyString();

    int getValueInt();

    int getValueIntZero();

    JavaScriptObject getValueJSO();

    String getValueString();
  }

  /**
   * Wraps a JSO that returns valid values. (JSFlyweightWrapper)
   */
  @BeanProperties
  public interface MemberValidFW extends JSFlyweightWrapper {
    MemberValidFW impl = GWT.create(MemberValidFW.class);

    @Constructor("$wnd.JSIOBeanReturnUndefined.MemberValid")
    JavaScriptObject construct();

    String getValueEmptyString(JavaScriptObject jsoPeer);

    int getValueInt(JavaScriptObject jsoPeer);

    int getValueIntZero(JavaScriptObject jsoPeer);

    JavaScriptObject getValueJSO(JavaScriptObject jsoPeer);

    String getValueString(JavaScriptObject jsoPeer);
  }

  /**
   * Wraps a JSO that returns valid values. (JSWrapper)
   */
  @Constructor("$wnd.JSIOBeanReturnUndefined.ReturnUndefined")
  public interface ReturnUndefined extends JSWrapper<ReturnUndefined> {

    boolean valueBoolean();

    int valueInt();

    JSList<Integer> valueJSList();

    JavaScriptObject valueJSO();

    String valueString();
  }

  /**
   * Wraps a JSO that returns valid values. (JSFlyweightWrapper)
   */
  public interface ReturnUndefinedFW extends JSFlyweightWrapper {
    ReturnUndefinedFW impl = GWT.create(ReturnUndefinedFW.class);

    @Constructor("$wnd.JSIOBeanReturnUndefined.ReturnUndefined")
    JavaScriptObject construct();

    boolean valueBoolean(JavaScriptObject jsoPeer);

    int valueInt(JavaScriptObject jsoPeer);

    JSList<Integer> valueJSList(JavaScriptObject jsoPeer);

    JavaScriptObject valueJSO(JavaScriptObject jsoPeer);

    String valueString(JavaScriptObject jsoPeer);
  }

  static boolean initialized = false;

  // JSWrapper version of JS interface
  private static EmptyObject emptyObj;
  private static MemberBoolean memberBooleanObj;
  private static MemberNull memberNullObj;
  private static MemberUndefined memberUndefinedObj;
  private static MemberValid memberValidObj;
  // JSFlyweightWrapper version of JS interface
  private static JavaScriptObject peerEmptyObject;

  private static JavaScriptObject peerMemberBoolean;
  private static JavaScriptObject peerMemberNull;
  private static JavaScriptObject peerMemberUndefined;
  private static JavaScriptObject peerMemberValid;
  private static JavaScriptObject peerReturnUndefined;
  private static ReturnUndefined returnUndefinedObj;

  public static void initJS() {
    if (initialized == true) {
      return;
    }
    initialized = true;

    // GWT.create() the JSWrapper classes
    emptyObj = GWT.create(EmptyObject.class);
    memberUndefinedObj = GWT.create(MemberUndefined.class);
    memberNullObj = GWT.create(MemberNull.class);
    memberValidObj = GWT.create(MemberValid.class);
    memberBooleanObj = GWT.create(MemberBoolean.class);
    returnUndefinedObj = GWT.create(ReturnUndefined.class);

    // instantiate each Flyweight class
    peerEmptyObject = EmptyObjectFW.impl.construct();
    peerMemberUndefined = MemberUndefinedFW.impl.construct();
    peerMemberNull = MemberNullFW.impl.construct();
    peerMemberValid = MemberValidFW.impl.construct();
    peerMemberBoolean = MemberBooleanFW.impl.construct();
    peerReturnUndefined = ReturnUndefinedFW.impl.construct();
  }

  @Override
  public String getModuleName() {
    return "com.google.gwt.jsio.JSIOTest";
  }

  public void testEmptyObject() {
    initJS();

    assertNotNull(emptyObj);

    int intResult = emptyObj.getValueInt();
    assertTrue(intResult == 0);

    JavaScriptObject jsoResult = emptyObj.getValueJSO();
    assertTrue(jsoResult == null);

    JSList<Integer> jsListResult = emptyObj.getValueJSList();
    assertTrue(jsListResult.isEmpty() == true);

    String stringResult = emptyObj.getValueString();
    assertTrue(stringResult == null);
  }

  public void testEmptyObjectFW() {
    initJS();

    assertNotNull(peerEmptyObject);

    int intResult = EmptyObjectFW.impl.getValueInt(peerEmptyObject);
    assertTrue(intResult == 0);

    JavaScriptObject jsoResult = EmptyObjectFW.impl.getValueJSO(peerEmptyObject);
    assertTrue(jsoResult == null);

    JSList<Integer> jsListResult = EmptyObjectFW.impl.getValueJSList(peerEmptyObject);
    assertTrue(jsListResult.isEmpty() == true);

    String stringResult = EmptyObjectFW.impl.getValueString(peerEmptyObject);
    assertTrue(stringResult == null);
  }

  public void testMemberBoolean() {
    initJS();

    assertNotNull(memberBooleanObj);

    boolean booleanResult;

    booleanResult = memberBooleanObj.getValueFalse();
    assertFalse(booleanResult);
    booleanResult = memberBooleanObj.getValueTrue();
    assertTrue(booleanResult);

    // The following tests only work in hosted mode
    if (!GWT.isScript()) {

      try {
        // throws a HostedModeException: JS value of type Number, expected
        // boolean
        booleanResult = memberBooleanObj.getValueOne();
        fail("Expected hosted mode exception returning value 1 as a boolean.");
      } catch (RuntimeException he) {
        // Expect an exception here.
      }

      try {
        // throws a HostedModeException: JS Value of type number, expected
        // boolean
        booleanResult = memberBooleanObj.getValueZero();
        fail("Expected hosted mode exception returning value 0 as a boolean.");
      } catch (RuntimeException he) {
        // Expect an exception here.
      }

      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.Boolean
        booleanResult = memberBooleanObj.getValueUndefined();
        fail("Expected hosted mode exception returning value undefined as a boolean.");
      } catch (RuntimeException he) {
        // Expect an exception here.
      }

      try {
        // throws a HostedModeException: return value null received, expected a
        // boolean
        booleanResult = memberBooleanObj.getValueNull();
        fail("Expected hosted mode exception returning value null as a boolean.");
      } catch (RuntimeException he) {
        // Expect an exception here.
      }
    } // end if running Hosted Mode
  }

  public void testMemberBooleanFW() {
    initJS();

    assertNotNull(peerMemberBoolean);

    boolean booleanResult;

    booleanResult = MemberBooleanFW.impl.getValueFalse(peerMemberBoolean);
    assertFalse(booleanResult);
    booleanResult = MemberBooleanFW.impl.getValueTrue(peerMemberBoolean);
    assertTrue(booleanResult);

    // The following tests only work in hosted mode
    if (!GWT.isScript()) {
      try {
        // throws a HostedModeException: JS value of type Number, expected
        // boolean
        booleanResult = MemberBooleanFW.impl.getValueOne(peerMemberBoolean);
        fail("Expected hosted mode exception returning value 1 as a boolean.");
      } catch (RuntimeException he) {
        // Expect an exception here.
      }

      try {
        // throws a HostedModeException: JS Value of type number, expected
        // boolean
        booleanResult = MemberBooleanFW.impl.getValueZero(peerMemberBoolean);
        fail("Expected hosted mode exception returning value 0 as a boolean.");
      } catch (RuntimeException he) {
        // Expect an exception here.
      }

      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.Boolean
        booleanResult = MemberBooleanFW.impl.getValueUndefined(peerMemberBoolean);
        fail("Expected hosted mode exception returning value undefined as a boolean.");
      } catch (RuntimeException he) {
        // Expect an exception here.
      }

      try {
        // throws a HostedModeException: return value null received, expected a
        // boolean
        booleanResult = MemberBooleanFW.impl.getValueNull(peerMemberBoolean);
        fail("Expected hosted mode exception returning value null as a boolean.");
      } catch (RuntimeException he) {
        // Expect an exception here.
      }
    }
  }

  @SuppressWarnings("unused")
  public void testMemberNull() {
    initJS();

    if (memberNullObj == null) {
      fail("Failed to initialize returnNullObj.");
    }

    int intResult;

    if (!GWT.isScript()) {
      try {
        intResult = memberNullObj.getValueInt();
        fail("Expected HostedModeException from null returned as int.");
      } catch (RuntimeException e) {
        // Expected Hosted mode exception.
      }
    }

    String stringResult = memberNullObj.getValueString();
    if (stringResult != null) {
      fail("Expected null, got: " + stringResult);
    }

    JSList<Integer> jsListResult = memberNullObj.getValueJSList();
    if (jsListResult != null) {
      fail("Expected null, got " + jsListResult.toString());
    }

    JavaScriptObject jsoResult = memberNullObj.getValueJSO();
    if (jsoResult != null) {
      fail("Expected null, got " + jsoResult.toString());
    }
  }

  @SuppressWarnings("unused")
  public void testMemberNullFW() {
    initJS();

    assertNotNull(peerMemberNull);

    int intResult;
    // This test fails with an NPE inside of ModuleSpace.invokeNativeInteger()
    // intResult = ReturnNullFW.impl.getValueInt(peerNull);
    // assertTrue(intResult == 0);
    String stringResult = MemberNullFW.impl.getValueString(peerMemberNull);
    assertTrue(stringResult == null);
    JavaScriptObject jsoResult = MemberNullFW.impl.getValueJSO(peerMemberNull);
    assertTrue(jsoResult == null);
  }

  @SuppressWarnings("unused")
  public void testMemberUndefined() {
    initJS();

    assertNotNull(memberUndefinedObj);

    // galgwt issue#20:
    // http://code.google.com/p/gwt-google-apis/issues/detail?id=20&q=issue%2020
    // throws a HostedModeException: JavaScript undefined, expected
    // com.google.gwt.core.client.JavaScriptObject
    JavaScriptObject jsoResult = memberUndefinedObj.getValueJSO();
    assertTrue(jsoResult == null);

    JSList<Integer> jsListResult = memberUndefinedObj.getValueJSList();
    assertTrue(jsListResult == null);

    if (!GWT.isScript()) {
      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.Integer
        int intResult = memberUndefinedObj.getValueInt();
        fail("Expected HostedModeException returning undefined int");

      } catch (RuntimeException e) {
        // Expected Hosted mode exception.
      }
      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.String
        String stringResult = memberUndefinedObj.getValueString();
        fail("Expected HostedModeException returning undefined String");
      } catch (RuntimeException e) {
        // Expected Hosted mode exception.
      }
    } // end if running in Hosted Mode
  }

  @SuppressWarnings("unused")
  public void testMemberUndefinedFW() {
    initJS();

    assertNotNull(peerMemberUndefined);

    // galgwt issue#20:
    // http://code.google.com/p/gwt-google-apis/issues/detail?id=20&q=issue%2020
    // throws a HostedModeException: JavaScript undefined, expected
    // com.google.gwt.core.client.JavaScriptObject
    JavaScriptObject jsoResult = MemberUndefinedFW.impl.getValueJSO(peerMemberUndefined);
    assertTrue(jsoResult == null);

    JSList<Integer> jsListResult = MemberUndefinedFW.impl.getValueJSList(peerMemberUndefined);
    assertTrue(jsListResult == null);

    if (!GWT.isScript()) {
      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.Integer
        int intResult = MemberUndefinedFW.impl.getValueInt(peerMemberUndefined);
        fail("Expected HostedModeException returning undefined int");

      } catch (RuntimeException e) {
        // Expected Hosted mode exception.
      }
      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.String
        String stringResult = MemberUndefinedFW.impl.getValueString(peerMemberUndefined);
        fail("Expected HostedModeException returning undefined String");
      } catch (RuntimeException e) {
        // Expected Hosted mode exception.
      }
    } // end if running in Hosted Mode
  }

  /**
   * This test is here to make sure that by changing the behavior of handling
   * undefined, we don't mess up the cases that already work.
   */
  public void testMemberValid() {
    initJS();

    assertNotNull(memberValidObj);

    int intResult;
    intResult = memberValidObj.getValueInt();
    assertTrue(intResult == 1);
    intResult = memberValidObj.getValueIntZero();
    assertTrue(intResult == 0);
    String stringResult = memberValidObj.getValueString();
    assertTrue(stringResult.equals("Hello World!"));
    stringResult = memberValidObj.getValueEmptyString();
    assertTrue(stringResult.equals(""));
    JavaScriptObject jsoResult = memberValidObj.getValueJSO();
    assertTrue(jsoResult != null);
    // TODO: Is there anything else we can test on the JSO?
  }

  /**
   * This test is here to make sure that by changing the behavior of handling
   * undefined, we don't mess up the cases that already work.
   */
  public void testMemberValidFW() {
    initJS();

    assertNotNull(peerMemberValid);

    int intResult;
    intResult = MemberValidFW.impl.getValueInt(peerMemberValid);
    assertTrue(intResult == 1);
    intResult = MemberValidFW.impl.getValueIntZero(peerMemberValid);
    assertTrue(intResult == 0);
    String stringResult = MemberValidFW.impl.getValueString(peerMemberValid);
    assertTrue(stringResult.equals("Hello World!"));
    stringResult = MemberValidFW.impl.getValueEmptyString(peerMemberValid);
    assertTrue(stringResult.equals(""));
    JavaScriptObject jsoResult = MemberValidFW.impl.getValueJSO(peerMemberValid);
    assertTrue(jsoResult != null);
    // TODO: Is there anything else we can test on the JSO?
  }

  @SuppressWarnings("unused")
  public void testReturnUndefined() {
    initJS();

    assertNotNull(returnUndefinedObj);

    // galgwt issue#20:
    // http://code.google.com/p/gwt-google-apis/issues/detail?id=20&q=issue%2020
    // throws a HostedModeException: JavaScript undefined, expected
    // com.google.gwt.core.client.JavaScriptObject
    JavaScriptObject jsoResult = returnUndefinedObj.valueJSO();
    assertTrue(jsoResult == null);

    JSList<Integer> jsListResult = returnUndefinedObj.valueJSList();
    assertTrue(jsListResult == null);

    if (!GWT.isScript()) {
      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.Boolean
        boolean booleanResult = returnUndefinedObj.valueBoolean();
        fail("Expected HostedModeException returning undefined boolean");

      } catch (RuntimeException e) {
        // Expected Hosted mode exception.
      }

      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.Integer
        int intResult = returnUndefinedObj.valueInt();
        fail("Expected HostedModeException returning undefined int");

      } catch (RuntimeException e) {
        // Expected Hosted mode exception.
      }

      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.String
        String stringResult = returnUndefinedObj.valueString();
        fail("Expected HostedModeException returning undefined String");
      } catch (RuntimeException e) {
        // Expected Hosted mode exception.
      }
    } // end if running in Hosted Mode
  }

  @SuppressWarnings("unused")
  public void testreturnUndefinedFW() {
    initJS();

    assertNotNull(peerReturnUndefined);

    // galgwt issue#20:
    // http://code.google.com/p/gwt-google-apis/issues/detail?id=20&q=issue%2020
    // throws a HostedModeException: JavaScript undefined, expected
    // com.google.gwt.core.client.JavaScriptObject
    JavaScriptObject jsoResult = ReturnUndefinedFW.impl.valueJSO(peerReturnUndefined);
    assertTrue(jsoResult == null);

    JSList<Integer> jsListResult = ReturnUndefinedFW.impl.valueJSList(peerReturnUndefined);
    assertTrue(jsListResult == null);

    if (!GWT.isScript()) {
      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.Boolean
        boolean booleanResult = ReturnUndefinedFW.impl.valueBoolean(peerReturnUndefined);
        fail("Expected HostedModeException returning undefined boolean");
      } catch (RuntimeException e) {
        // Expected Hosted mode exception.
      }

      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.Integer
        int intResult = ReturnUndefinedFW.impl.valueInt(peerReturnUndefined);
        fail("Expected HostedModeException returning undefined int");

      } catch (RuntimeException e) {
        // Expected Hosted mode exception.
      }
      try {
        // throws a HostedModeException: JavaScript undefined, expected
        // java.lang.String
        String stringResult = ReturnUndefinedFW.impl.valueString(peerReturnUndefined);
        fail("Expected HostedModeException returning undefined String");
      } catch (RuntimeException e) {
        // Expected Hosted mode exception.
      }
    } // end if running in Hosted Mode
  }
}
