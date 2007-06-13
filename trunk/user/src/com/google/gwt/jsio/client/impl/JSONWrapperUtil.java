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
package com.google.gwt.jsio.client.impl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.jsio.client.MultipleWrapperException;

/**
 * Internal utility functions to encapsulate often-used idioms.
 */
public class JSONWrapperUtil {
  public static final Extractor BOOLEAN_EXTRACTOR = new Extractor() {
    public native Object fromJS(JavaScriptObject obj) /*-{
     return @com.google.gwt.jsio.client.impl.JSONWrapperUtil::createWrapper(Z)(Boolean(obj));
     }-*/;

    public native JavaScriptObject toJS(Object o) /*-{
     return new Boolean(o.@java.lang.Boolean::booleanValue()());
     }-*/;
  };

  public static final Extractor BYTE_EXTRACTOR = new Extractor() {
    public native Object fromJS(JavaScriptObject obj) /*-{
     return @com.google.gwt.jsio.client.impl.JSONWrapperUtil::createWrapper(B)(Number(obj));
     }-*/;

    public native JavaScriptObject toJS(Object o) /*-{
     return new Number(o.@java.lang.Byte::byteValue()());
     }-*/;
  };

  public static final Extractor CHARACTER_EXTRACTOR = new Extractor() {
    public native Object fromJS(JavaScriptObject obj) /*-{
     return @com.google.gwt.jsio.client.impl.JSONWrapperUtil::createWrapper(C)(Number(obj));
     }-*/;

    public native JavaScriptObject toJS(Object o) /*-{
     return new Number(o.@java.lang.Character::charValue()());
     }-*/;
  };

  public static final Extractor DOUBLE_EXTRACTOR = new Extractor() {
    public native Object fromJS(JavaScriptObject obj) /*-{
     return @com.google.gwt.jsio.client.impl.JSONWrapperUtil::createWrapper(D)(Number(obj));
     }-*/;

    public native JavaScriptObject toJS(Object o) /*-{
     return new Number(o.@java.lang.Double::doubleValue()());
     }-*/;
  };

  public static final Extractor FLOAT_EXTRACTOR = new Extractor() {
    public native Object fromJS(JavaScriptObject obj) /*-{
     return @com.google.gwt.jsio.client.impl.JSONWrapperUtil::createWrapper(F)(Number(obj));
     }-*/;

    public native JavaScriptObject toJS(Object o) /*-{
     return new Number(o.@java.lang.Float::floatValue()());
     }-*/;
  };

  public static final Extractor INTEGER_EXTRACTOR = new Extractor() {
    public native Object fromJS(JavaScriptObject obj) /*-{
     return @com.google.gwt.jsio.client.impl.JSONWrapperUtil::createWrapper(I)(Number(obj));
     }-*/;

    public native JavaScriptObject toJS(Object o) /*-{
     return new Number(o.@java.lang.Integer::intValue()());
     }-*/;
  };

  /**
   * Essentially a no-op since JavaScriptObjects are transparent to the Java
   * side of the code.
   */
  public static final Extractor JSO_EXTRACTOR = new Extractor() {
    public native Object fromJS(JavaScriptObject obj) /*-{
     return obj;
     }-*/;

    public native JavaScriptObject toJS(Object o) /*-{
     return o;
     }-*/;
  };

  public static final Extractor LONG_EXTRACTOR = new Extractor() {
    public native Object fromJS(JavaScriptObject obj) /*-{
     return @com.google.gwt.jsio.client.impl.JSONWrapperUtil::createWrapper(J)(Number(obj));
     }-*/;

    public native JavaScriptObject toJS(Object o) /*-{
     return new Number(o.@java.lang.Long::longValue()());
     }-*/;
  };

  public static final Extractor SHORT_EXTRACTOR = new Extractor() {
    public native Object fromJS(JavaScriptObject obj) /*-{
     return @com.google.gwt.jsio.client.impl.JSONWrapperUtil::createWrapper(S)(Number(obj));
     }-*/;

    public native JavaScriptObject toJS(Object o) /*-{
     return new Number(o.@java.lang.Short::shortValue()());
     }-*/;
  };

  public static final Extractor STRING_EXTRACTOR = new Extractor() {
    public native Object fromJS(JavaScriptObject obj) /*-{
     return String(obj);
     }-*/;

    public native JavaScriptObject toJS(Object o) /*-{
     return new String(o);
     }-*/;
  };

  public static Boolean createWrapper(boolean b) {
    return Boolean.valueOf(b);
  }

  public static Byte createWrapper(byte b) {
    return new Byte(b);
  }

  public static Character createWrapper(char c) {
    return new Character(c);
  }

  public static Double createWrapper(double c) {
    return new Double(c);
  }

  public static Float createWrapper(float c) {
    return new Float(c);
  }

  public static Integer createWrapper(int c) {
    return new Integer(c);
  }

  public static Long createWrapper(long c) {
    return new Long(c);
  }

  public static Short createWrapper(short c) {
    return new Short(c);
  }

  /*
   * This method converts the json string into a JavaScriptObject inside of JSNI
   * method by simply evaluating the string in JavaScript.
   */
  public static native JavaScriptObject evaluate(String jsonString) /*-{
   var x = eval('(' + jsonString + ')');
   if (typeof x == 'number' || typeof x == 'string' || typeof x == 'array' || typeof x == 'boolean') {
   x = (Object(x));
   }
   return x;
   }-*/;
  
  /**
   * Utility method for JSWrapper to throw an exception.
   */
  public static void throwMultipleWrapperException() {
    throw new MultipleWrapperException();
  }

  private JSONWrapperUtil() {
  }
}
