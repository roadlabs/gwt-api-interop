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
import com.google.gwt.jsio.client.impl.Extractor;

/**
 * Automatically generates Java/JavaScript interface linkages.
 * @see <a href="package-summary.html#package_description">JSIO reference</a>
 */
public interface JSWrapper {

  /**
   * This is used by JSList as a helper.
   * @skip
   */
  public Extractor getExtractor();

  /**
   * Return the JavaScriptObject that is backing the wrapper.
   */
  public JavaScriptObject getJavaScriptObject();

  /**
   * Set the JavaScriptObject to be wrapped by the generated class.
   * 
   * @throws MultipleWrapperException if <code>obj</code> is already the
   *           target of another JSWrapper.
   */
  public void setJavaScriptObject(JavaScriptObject obj)
      throws MultipleWrapperException;

  /**
   * Convenience setter for wrapping JSON data. The data will be parsed and
   * wrapped by the instance of the JSWrapper
   */
  public void setJSONData(String data) throws JSONWrapperException;
}
