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
 * Demonstrates how to override behavior in an underlying JSO's prototype. This
 * simulates patching a singleton-style JavaScript API. There is an instance of
 * an API container object attached at $wnd.MathLib. This API has two methods,
 * add and subtract. We wish to use the add method, while replacing the subtract
 * method contained within the object's prototype. This test case shows how this
 * can be accomplished with both the flyweight and wrapper styles.
 */
public class JsoOverrideTest extends GWTTestCase {

  /**
   * This shows the flyweight style. This is tagged as noIdentity to prevent a
   * __gwtPeer field from being assigned to the prototype since the
   * SubtractOverride class is implemented as a utility class.
   * 
   * @gwt.noIdentity
   */
  static interface FlyweightMathLib extends JSFlyweightWrapper {
    public int add(JavaScriptObject jso, int a, int b);

    /**
     * Because the SubtractOverride is a static utility class, we don't need to
     * provide an instance of the class when we perform the binding. Without a
     * second parameter on the binding method from which to infer the binding
     * type, we have to rely on a type declaration within the annotation.
     * 
     * @gwt.binding com.google.gwt.jsio.client.JsoOverrideTest.SubtractOverride
     */
    public void bind(JavaScriptObject jso);

    /**
     * @gwt.global $wnd.MathLib.constructor.prototype
     */
    public JavaScriptObject construct();

    public int subtract(JavaScriptObject jso, int a, int b);
  }

  /**
   * This class is built as a utility class to avoid the overhead of carrying
   * around an instance of SubtractOverride per MathLib instance.
   */
  static class SubtractOverride {
    /**
     * @gwt.exported
     */
    public static int subtract(int x, int y) {
      return x - y;
    }

    /**
     * Private no-op constructor.
     */
    private SubtractOverride() {
    }
  }

  /**
   * @gwt.global $wnd.MathLib.constructor.prototype
   * @gwt.noIdentity
   */
  abstract static class WrapperMathLib implements JSWrapper {
    /**
     * Declaring the method to be static removes the dependency on the instance
     * of WrapperMathLib in the underlying linkage code. This is an optimization
     * and not a requirement.
     * 
     * @gwt.exported
     */
    public static int subtract(int a, int b) {
      return a - b;
    }

    public abstract int add(int a, int b);
  }

  public String getModuleName() {
    return "com.google.gwt.jsio.JSIO";
  }

  public void testFlyweightOverride() {
    createBackend();
    FlyweightMathLib flyweightMathLib = (FlyweightMathLib) GWT.create(FlyweightMathLib.class);

    JavaScriptObject mathJso = flyweightMathLib.construct();

    assertEquals(7, flyweightMathLib.add(mathJso, 3, 4));
    // Show the broken version is still there.
    assertEquals(0, flyweightMathLib.subtract(mathJso, 10, 5));
    // The results from JavaScript agree
    assertEquals(0, invokeSubtract(10, 5));

    // Because we're only exporting static methods from SubtractOverride, it's
    // unnecessary to actually provide an instance of a SubtractOverride to the
    // binding. If we did, it would be ignored.
    flyweightMathLib.bind(mathJso);

    assertEquals(7, flyweightMathLib.add(mathJso, 3, 4));

    // Show that the override has taken effect from the flyweight's view
    assertEquals(5, flyweightMathLib.subtract(mathJso, 10, 5));

    // Methods in JavaScript will also use the replaced method.
    assertEquals(5, invokeSubtract(10, 5));
    assertEquals(5, invokeSubtractOnNewInstance(10, 5));
  }

  public void testWrapperOverride() {
    createBackend();

    // Show that the default, broken implementation is used.
    assertEquals(0, invokeSubtract(10, 5));

    WrapperMathLib mathLib = (WrapperMathLib) GWT.create(WrapperMathLib.class);

    // Call out to the native method
    assertEquals(7, mathLib.add(3, 4));

    // This is a direct Java call
    assertEquals(5, mathLib.subtract(10, 5));

    // Methods in JavaScript will also use the replaced method.
    assertEquals(5, invokeSubtract(10, 5));
    assertEquals(5, invokeSubtractOnNewInstance(10, 5));
  }

  /**
   * This would normally happen via external script inclusion.
   */
  private native void createBackend() /*-{
   function MathLib() {}
   MathLib.prototype.add = function(a, b) {
   return a + b;
   }
   
   // This function is broken, we'll override it in Java
   MathLib.prototype.subtract = function(a, b) {
   return 0;
   }
   
   $wnd.MathLibConstructor = MathLib;
   $wnd.MathLib = new MathLib();
   }-*/;

  /**
   * Invokes the MathLib subtract function from JavaScript.
   */
  private native int invokeSubtract(int a, int b) /*-{
   return $wnd.MathLib.subtract(a, b);
   }-*/;

  /**
   * Creates a new instance of MathLib to verify that patching the prototype
   * will work for new instances of MathLib.
   */
  private native int invokeSubtractOnNewInstance(int a, int b) /*-{
   return (new $wnd.MathLibConstructor()).subtract(a, b);
   }-*/;
}
