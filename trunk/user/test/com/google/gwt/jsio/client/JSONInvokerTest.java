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
   */
  public static interface HelloWrapper extends JSWrapper {
    /**
     * These functions test using variable numbers of parameters to the same
     * underlying JS function.
     */
    public int add(int toAdd);
    public int add(int toAdd, int second);
    /**
     * @gwt.constructor $wnd.Hello
     */
    public HelloWrapper constructor(String param1, int param2);
    public int getHello();
    
    /**
     * @gwt.typeArgs <java.lang.Integer>
     */
    public JSList getNumbers();
    
    public String getParam1();
    public int getParam2();
    
    /**
     * Test that the function returned from a JSFunction is the same object
     * between invocations.
     */
    public boolean identityEquals(HelloCallbackInt a, HelloCallbackInt b);
    
    public void increment();
    
    /**
     * Don't implement this as a bean function, but make a call out to the
     * js object.
     * @gwt.imported true
     */
    public void setHello(int hello);
    
    /**
     * Alias test.
     *
     * @gwt.fieldName sub
     */
    public int subtract(int toSubtract);
    
    public int testCallback(int a, int b, HelloCallbackInt c);
    
    /**
     * @gwt.fieldName testCallback
     */
    public Integer testCallbackBoxed(Integer a, Integer b,
        HelloCallbackInteger c);
  }
  
  /**
   * @gwt.global $wnd.SingletonHello
   */
  public static interface SingletonHello extends HelloWrapper {
  }
  
  /**
   * This would normally be done by the JS included by the module.  It's
   * included inline for ease of comprehension.
   */
  private static native void initializeHello() /*-{
    function Hello(param1, param2) {
      this.hello = 42;
      this.param1 = param1;
      this.param2 = param2;
      this.numbers = [1,2,3,4,5];
    }
    
    Hello.prototype.add = function(sum, sum2) {
      return this.hello += sum + (sum2 || 0);
    }
    
    Hello.prototype.sub = function(sum) {
      return this.hello -= sum;
    }
    
    Hello.prototype.increment = function() {
      this.hello++;
    }
    
    Hello.prototype.setHello = function(a) {
      this.hello = a + 10;
    }
    
    Hello.prototype.testCallback = function(param1, param2, callback) {
      return 5 * callback(param1, param2);
    }
    
    Hello.prototype.identityEquals = function(o1, o2) {
      return o1 === o2;
    }
    
    $wnd.Hello = Hello;
    
    $wnd.SingletonHello = new Hello("Singleton", 314159);
  }-*/;
  
  public String getModuleName() {
    return "com.google.gwt.jsio.JSIO";
  }
  
  public void testCallback() {
    initializeHello();
    
    HelloWrapper wrapper = (HelloWrapper)GWT.create(HelloWrapper.class);
    wrapper.constructor("Hello world", 99);
    
    assertEquals(30, wrapper.testCallback(2, 3, new HelloCallbackInt()));
    assertEquals(new Integer(30), wrapper.testCallbackBoxed(
        new Integer(2), new Integer(3), new HelloCallbackInteger()));
    
    // Verify that the function object returned is the same so that
    // register/unregister APIs will work correctly.
    HelloCallbackInt c1 = new HelloCallbackInt();
    assertTrue(wrapper.identityEquals(c1, c1));
    HelloCallbackInt c2 = new HelloCallbackInt();
    assertFalse(wrapper.identityEquals(c1, c2));
  }
  
  public void testInvocation() {
    initializeHello();
    
    HelloWrapper wrapper = (HelloWrapper)GWT.create(HelloWrapper.class);
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
    
    JSList numbers = wrapper.getNumbers();
    for (int i = 0; i < numbers.size(); i++) {
      assertEquals(new Integer(i + 1), numbers.get(i));
    }
  }
  
  public void testSingleton() {
    initializeHello();
    
    HelloWrapper wrapper = (HelloWrapper)GWT.create(SingletonHello.class);
    
    assertEquals("Singleton", wrapper.getParam1());
    assertEquals(314159, wrapper.getParam2());
  }
}
