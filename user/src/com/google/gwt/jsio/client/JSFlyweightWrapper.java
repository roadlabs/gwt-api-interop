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
 * A variation on JSWrapper that implements a flyweight-style API binding. The
 * declared methods must accept a JavaScriptObject as the first parameter that
 * indicates the underlying JSO on which to operate. Unlike JSWrapper, the use
 * of JSFlyweightWrapper requires that the caller manage the backing
 * JavaScriptObject.
 * 
 * @see <a href="package-summary.html#package_description">JSIO reference</a>
 */
public interface JSFlyweightWrapper {
  /**
   * Allows access to flyweight internals.
   */
  public static final class Util {
    /**
     * Returns the Java peer Object associated with <code>obj</code>.
     * 
     * @param obj the stateful JavaScriptObject or <code>null</code> if none
     *          exists.
     * @return the Java Object associated with <code>obj</code> by a call to
     *         {@link #bind(JavaScriptObject, Object)}
     */
    public static native Object getJavaPeer(JavaScriptObject obj) /*-{
     // Must keep synchronized with JSFlyweightWrapperGenerator
     return obj.__gwtPeer || null;
     }-*/;
  }

  /**
   * Binds a JavaScriptObject to a peer Java object. This is required to
   * establish a function call context.
   * 
   * @throws MultipleWrapperException If <code>obj</code> has been previously
   *           initialized or is associated with a {@link JSWrapper}.
   */
  public void bind(JavaScriptObject obj, Object peer)
      throws MultipleWrapperException;
}
