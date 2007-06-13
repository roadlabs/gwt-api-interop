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

import java.util.Iterator;

/**
 * Tests native ivocation capabilities of the wrapper classes.
 */
public class JSExporterTest extends GWTTestCase {
  
  abstract static class ExportedMethods implements JSWrapper {
    /**
     * @gwt.exported
     */
    public int add(int a, int b) {
      return a + b;
    }
    
    /**
     * @gwt.exported
     * @gwt.fieldName sub
     */
    public int subtract(int a, int b) {
      return a - b;
    }
    
    /**
     * @gwt.exported
     * @gwt.typeArgs numbers <java.lang.Integer>
     */
    public int sum(JSList numbers) {
      int toReturn = 0;
      
      for (Iterator i = numbers.iterator(); i.hasNext();) {
        toReturn += ((Integer)i.next()).intValue();
      }
      
      return toReturn;
    }
  }

  public String getModuleName() {
    return "com.google.gwt.jsio.JSIO";
  }

  public void testAdd() {
    ExportedMethods export = (ExportedMethods)GWT.create(ExportedMethods.class);
    JavaScriptObject obj = export.getJavaScriptObject();
    
    assertTrue(testMethod(obj, "add"));
    assertEquals(10, export.add(3,7));
    assertEquals(10, testAddNative(obj, 3, 7));
  }
  
  public void testSub() {
    ExportedMethods export = (ExportedMethods)GWT.create(ExportedMethods.class);
    JavaScriptObject obj = export.getJavaScriptObject();
    
    assertTrue(testMethod(obj, "sub"));
    assertEquals(-4, export.subtract(3,7));
    assertEquals(-4, testSubNative(obj, 3, 7));
  }
  
  public void testSum() {
    ExportedMethods export = (ExportedMethods)GWT.create(ExportedMethods.class);
    assertEquals(15, testSumNative(export.getJavaScriptObject()));
  }
  
  private native int testAddNative(JavaScriptObject jso, int a, int b) /*-{
    return jso.add(a, b);
  }-*/;
  
  private native boolean testMethod(JavaScriptObject obj, String methodName) /*-{
    return methodName in obj;
  }-*/;

  private native int testSubNative(JavaScriptObject obj, int a, int b) /*-{
    return obj.sub(a, b);
  }-*/;
  
  private native int testSumNative(JavaScriptObject obj) /*-{
    return obj.sum([1, 2, 3, 4, 5]);
  }-*/;

}
