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
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests native ivocation capabilities of the wrapper classes.
 */
public class JSONInvokerTest extends GWTTestCase {
  /**
   * Test primitive passthrough into native functions.
   */
  public static class HelloCallbackInt extends JSFunction {
    public int multiply(int a, int b) {
      return a * b;
    }
  }

  /**
   * Test Boxed passthrough into native functions.
   */
  public static class HelloCallbackInteger extends JSFunction {
    public Integer multiply(Integer a, Integer b) {
      return new Integer(a.intValue() * b.intValue());
    }
  }

  /**
   * Testbed class for invocation testing.
   * 
   * @gwt.beanProperties
   */
  public static interface HelloWrapper extends JSWrapper {
    /**
     * These functions test using variable numbers of parameters to the same
     * underlying JS function.
     */
    int add(int toAdd);

    int add(int toAdd, int second);

    /**
     * @gwt.constructor $wnd.JSONInvokerTest.Hello
     */
    HelloWrapper constructor(String param1, int param2);

    int getHello();

    /**
     * @gwt.imported true
     * @gwt.fieldName returnUndefined
     */
    Integer getIntegerAsUndefined();

    JSList<Integer> getNumbers();

    String getParam1();

    int getParam2();

    /**
     * Test that the function returned from a JSFunction is the same object
     * between invocations.
     */
    boolean identityEquals(HelloCallbackInt a, HelloCallbackInt b);

    boolean identityEquals(JSWrapper a, JSWrapper b);

    void increment();

    /**
     * Test that a JSWrapper can be passed through and returned from a native
     * code block.
     */
    HelloWrapper passthrough(HelloWrapper w);

    OtherWrapper passthrough(OtherWrapper w);

    StatefulWrapper passthrough(StatefulWrapper w);

    JSList<Integer> reverseNumbers(JSList<Integer> arr);

    /**
     * Don't implement this as a bean function, but make a call out to the js
     * object.
     * 
     * @gwt.imported true
     */
    void setHello(int hello);

    /**
     * Alias test.
     * 
     * @gwt.fieldName sub
     */
    int subtract(int toSubtract);

    int testCallback(int a, int b, HelloCallbackInt c);

    /**
     * @gwt.fieldName testCallback
     */
    Integer testCallbackBoxed(Integer a, Integer b, HelloCallbackInteger c);
  }

  /**
   * Ensures an assertion fails when constructor method called.
   */
  public static interface MissingMethodWrapper extends HelloWrapper {
    /**
     * @gwt.import
     */
    void missingMethod();
  }

  /**
   * This is a blank interface just to be used as a reference type.
   */
  public static interface OtherWrapper extends JSWrapper {
  }

  /**
   * @gwt.global $wnd.JSONInvokerTest.SingletonHello
   */
  public static interface SingletonHello extends HelloWrapper {
  }
  /**
   * Tests state-preserving behavior across the JS/Java boundary.
   */
  public abstract static class StatefulWrapper implements HelloWrapper {
    static String staticField;
    final String finalField = "finalField";
    transient String transientField;
    /**
     * This field will always emit a warning.
     */
    String localField;
  }

  public String getModuleName() {
    return "com.google.gwt.jsio.JSIOTest";
  }

  public void testCallback() {
    HelloWrapper wrapper = (HelloWrapper) GWT.create(HelloWrapper.class);
    wrapper.constructor("Hello world", 99);

    assertEquals(30, wrapper.testCallback(2, 3, new HelloCallbackInt()));
    assertEquals(new Integer(30), wrapper.testCallbackBoxed(new Integer(2),
        new Integer(3), new HelloCallbackInteger()));

    // Verify that the function object returned is the same so that
    // register/unregister APIs will work correctly.
    HelloCallbackInt c1 = new HelloCallbackInt();
    assertTrue("c1 not identical to itself", wrapper.identityEquals(c1, c1));
    HelloCallbackInt c2 = new HelloCallbackInt();
    assertFalse("c1 and c2 should not be identical", wrapper.identityEquals(c1,
        c2));
  }

  /**
   * Ensure that undefined maps to null when returning a boxed primitive.
   */
  public void testIntegerAsUndefined() {
    HelloWrapper w = (HelloWrapper) GWT.create(HelloWrapper.class);
    w.constructor("Hello world", 99);
    assertNull(w.getIntegerAsUndefined());
  }

  public void testInvocation() {
    HelloWrapper wrapper = (HelloWrapper) GWT.create(HelloWrapper.class);
    wrapper.constructor("Hello world", 99);
    assertEquals(42, wrapper.getHello());
    assertEquals("Hello world", wrapper.getParam1());
    assertEquals(99, wrapper.getParam2());
    wrapper.increment();
    assertEquals(43, wrapper.getHello());
    assertEquals(143, wrapper.add(100));
    assertEquals(143, wrapper.getHello());
    assertEquals(43, wrapper.subtract(100));
    assertEquals(43, wrapper.getHello());
    assertEquals(143, wrapper.add(50, 50));
    assertEquals(143, wrapper.getHello());

    // We'll know that the JS object's setHello() was invoked, because it
    // adds 10 to the value of the parameter.
    wrapper.setHello(10);
    assertEquals(20, wrapper.getHello());

    // Check lists returned from bean-style accessors
    JSList<Integer> numbers = wrapper.getNumbers();
    for (int i = 0; i < numbers.size(); i++) {
      assertEquals(i + 1, numbers.get(i).intValue());
    }

    // Check a list returned through an imported function
    JSList<Integer> reversed = wrapper.reverseNumbers(numbers);
    assertEquals(numbers.size(), reversed.size());
    for (int i = 0; i < numbers.size(); i++) {
      assertEquals(numbers.get(i), reversed.get(numbers.size() - i - 1));
    }
  }

  public void testMissingMethod() {
    if (GWT.isScript()) {
      // This test does not make sense in web mode due to lack of asserts
      return;
    }

    try {
      MissingMethodWrapper wrapper = (MissingMethodWrapper) GWT.create(MissingMethodWrapper.class);
      wrapper.constructor("Hello world", 99);
      fail("Expected failed assertion on missing method. "
          + "Did you run with -ea?");
    } catch (RuntimeException e) {
      if (e.getCause() instanceof AssertionError) {
        // Expected behavior because the AssertionError hits the browser/JVM
        // boundary. The JSNI constructor method invokes setJSO() which is
        // where the AssertionError is generated.
      } else {
        throw e;
      }
    }
  }

  /**
   * Ensure that wrapped objects returned from a native JS API are returned
   * correctly and have the correct identity semantics for the underlying
   * object.
   */
  public void testPassthrough() {
    HelloWrapper w1 = (HelloWrapper) GWT.create(HelloWrapper.class);
    w1.constructor("hello", 1);

    assertNull(w1.passthrough((HelloWrapper) null));

    HelloWrapper w2 = (HelloWrapper) GWT.create(HelloWrapper.class);
    w2.constructor("world", 2);

    HelloWrapper w3 = w2.passthrough(w1);

    assertTrue(w3 instanceof HelloWrapper);
    assertTrue(w2.identityEquals(w1, w3));

    OtherWrapper o1 = (OtherWrapper) GWT.create(OtherWrapper.class);
    OtherWrapper o2 = w2.passthrough(o1);
    assertTrue(o2 instanceof OtherWrapper);
    assertTrue(w2.identityEquals(o1, o2));
  }

  /**
   * Test initialization of a wrapper via the gwt.global singleton constructor.
   */
  public void testSingleton() {
    HelloWrapper wrapper = (HelloWrapper) GWT.create(SingletonHello.class);

    assertEquals("Singleton", wrapper.getParam1());
    assertEquals(314159, wrapper.getParam2());
  }

  @SuppressWarnings("all")
  public void testStatePreservation() {
    StatefulWrapper w1 = (StatefulWrapper) GWT.create(StatefulWrapper.class);
    w1.constructor(null, 0);
    w1.localField = "bad field";
    w1.staticField = "static field";
    w1.transientField = "transient field";

    StatefulWrapper w2 = w1.passthrough(w1);
    assertSame(w1, w2);
    assertEquals(w1.localField, w2.localField);
    assertEquals(w1.finalField, w2.finalField);
    assertEquals(w1.staticField, w2.staticField);
    assertEquals(w1.transientField, w2.transientField);
  }
}
