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
import com.google.gwt.jsio.client.JSList;
import com.google.gwt.jsio.client.JSWrapper;
import com.google.gwt.jsio.client.JSONWrapperException;

import java.util.AbstractList;

/**
 * This an implementation of List that operates directly on JS arrays.
 */
public final class JSListWrapper extends AbstractList implements JSList,
    JSWrapper {

  /**
   * This is used with nested JSLists.
   */
  private static class WrappingExtractor implements Extractor {
    // package protected to avoid style warning, only read from JSNI
    final Extractor subExtractor;

    public WrappingExtractor(Extractor subExtractor) {
      this.subExtractor = subExtractor;
    }

    public native Object fromJS(JavaScriptObject obj) /*-{
     var toReturn = @com.google.gwt.jsio.client.impl.JSListWrapper::create(Lcom/google/gwt/jsio/client/impl/Extractor;)(
     this.@com.google.gwt.jsio.client.impl.JSListWrapper.WrappingExtractor::subExtractor);
     toReturn.@com.google.gwt.jsio.client.JSWrapper::setJavaScriptObject(Lcom/google/gwt/core/client/JavaScriptObject;)(obj);
     return toReturn;
     }-*/;

    public native JavaScriptObject toJS(Object o) /*-{
     return this.@com.google.gwt.jsio.client.JSWrapper::getJavaScriptObject()();
     }-*/;
  }

  /**
   * Used by JSNI code to construct new JSListWrappers.
   */
  public static JSListWrapper create(Extractor e) {
    return new JSListWrapper(e);
  }

  public static final Extractor createExtractor(final Extractor e) {
    return new WrappingExtractor(e);
  }

  /**
   * Used by JSNI code to throw an IndexOutOfBoundsException.
   */
  static void throwIndexOutOfBoundsException() {
    throw new IndexOutOfBoundsException();
  }

  JavaScriptObject arr;
  final Extractor extractor;

  public JSListWrapper(Extractor extractor) {
    this.extractor = extractor;
    initNative();
  }

  public native void add(int index, Object o) /*-{
   var arr = this.@com.google.gwt.jsio.client.impl.JSListWrapper::arr;

   if ((index < 0) || (index > arr.length)) {
   @com.google.gwt.jsio.client.impl.JSListWrapper::throwIndexOutOfBoundsException()();
   }

   var extractor = this.@com.google.gwt.jsio.client.impl.JSListWrapper::extractor;
   var jso = extractor.@com.google.gwt.jsio.client.impl.Extractor::toJS(Ljava/lang/Object;)(o);
   arr.splice(index, 0, jso);
   }-*/;

  public native void clear() /*-{
   this.@com.google.gwt.jsio.client.impl.JSListWrapper::arr = [];
   }-*/;

  public native Object get(int index) /*-{
   var arr = this.@com.google.gwt.jsio.client.impl.JSListWrapper::arr;
   if ((index < 0) || (index >= arr.length)) {
   @com.google.gwt.jsio.client.impl.JSListWrapper::throwIndexOutOfBoundsException()();
   }
   
   var extractor = this.@com.google.gwt.jsio.client.impl.JSListWrapper::extractor;
   return extractor.@com.google.gwt.jsio.client.impl.Extractor::fromJS(Lcom/google/gwt/core/client/JavaScriptObject;)(new Object(arr[index]));
   }-*/;

  public Extractor getExtractor() {
    return new WrappingExtractor(extractor);
  }

  /**
   * Return the JSONObject that is backing the wrapper. Modifications to the
   * returned JSONObject are not required to be correctly reflected in the
   * source wrapper.
   */
  public JavaScriptObject getJavaScriptObject() {
    return arr;
  }

  public native Object remove(int index) /*-{
   var arr = this.@com.google.gwt.jsio.client.impl.JSListWrapper::arr;
   if ((index < 0) || (index >= arr.length)) {
   @com.google.gwt.jsio.client.impl.JSListWrapper::throwIndexOutOfBoundsException()();
   }
   
   var toReturn = arr.splice(index, 1);
   
   var extractor = this.@com.google.gwt.jsio.client.impl.JSListWrapper::extractor;
   return extractor.@com.google.gwt.jsio.client.impl.Extractor::fromJS(Lcom/google/gwt/core/client/JavaScriptObject;)(new Object(toReturn[0]));
   }-*/;

  public native Object set(int index, Object o) /*-{
   var arr = this.@com.google.gwt.jsio.client.impl.JSListWrapper::arr;
   if (( index < 0) || (index >= arr.length)) {
   @com.google.gwt.jsio.client.impl.JSListWrapper::throwIndexOutOfBoundsException()();
   }
   
   var toReturn = null;
   var extractor = this.@com.google.gwt.jsio.client.impl.JSListWrapper::extractor;
   arr[index] = extractor.@com.google.gwt.jsio.client.impl.Extractor::toJS(Ljava/lang/Object;)(o);
   return toReturn;
   }-*/;

  /**
   * Convenience setter for generated subclasses to be able to initialize
   * newly-created instances without another parsing cycle. This is
   * intentionally not exposed via JSWrapper to prevent the backing JSONObject
   * from changing out from under the wrapper.
   */
  public void setJavaScriptObject(JavaScriptObject obj) {
    arr = obj;
  }

  /**
   * Unimplemented.
   */
  public void setJSONData(String data) throws JSONWrapperException {
    throw new JSONWrapperException("Unimplemented");
  }

  public native int size() /*-{
   var arr = this.@com.google.gwt.jsio.client.impl.JSListWrapper::arr;
   return arr.length;
   }-*/;

  protected native void removeRange(int fromIndex, int toIndex) /*-{
   var arr = this.@com.google.gwt.jsio.client.impl.JSListWrapper::arr;
   arr.splice(fromIndex, toIndex - fromIndex);
   }-*/;

  private native void initNative() /*-{
   this.@com.google.gwt.jsio.client.impl.JSListWrapper::arr = [];
   }-*/;
}
