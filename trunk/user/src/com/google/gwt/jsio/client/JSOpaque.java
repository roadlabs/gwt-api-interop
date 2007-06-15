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

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Allows by-name references to JavaScript values. This is intended for use with
 * opaque values such as those used in enumeration-like types.
 */
public class JSOpaque {
  /**
   * Stores the named reference. This value is never intended to be read or
   * dereferenced by GWT developers.
   */
  private final String reference;

  /**
   * Constructor.
   * 
   * @param reference A named reference to a globally-defined value.
   */
  public JSOpaque(String reference) {
    this.reference = reference;
  }
  
  /**
   * Allows comparisons of the JSOpaque to JavaScriptObjects.
   */
  public native boolean equals(JavaScriptObject o) /*-{
    var result = eval(this.@com.google.gwt.jsio.client.JSOpaque::reference);
    if (typeOf(result) == 'object' && typeOf(o) == 'object') {
      return result.equals(o);
    } else {
      return result == o;
    }
  }-*/;

  /**
   * Equality is defined for JSOpaque based on the referenced name.
   * @return <code>true</code> iff <code>o</code> refers to the same reference
   */
  public boolean equals(Object o) {
    return (o instanceof JSOpaque) && reference.equals(((JSOpaque)o).reference);
  }

  public int hashCode() {
    return reference.hashCode();
  }
  
  public native String toString() /*-{
    return String(eval(this.@com.google.gwt.jsio.client.JSOpaque::reference));
  }-*/;
}
