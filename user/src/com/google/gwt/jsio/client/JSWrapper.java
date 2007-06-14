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
 * Automatically generates a Java proxy for a JavaScriptObject. The GWT compiler
 * can automatically generate:
 * <ul>
 * <li>Accessors for bean-style properties</li>
 * <li>Linkage to functions defined within the JavaScriptObject</li>
 * <li>Exports of Java functions to be made available to JavaScript callers</li>
 * </ul>
 * <br/> Example:
 * 
 * <pre>
 * interface HelloWrapper extends JSWrapper {
 *   public String getHello();
 * }
 * HelloWrapper hello = (HelloWrapper)GWT.create(HelloWrapper.class);
 * hello.setJSONData(&quot;{hello:'Hello world'}&quot;);
 * Window.alert(hello.getHello());
 * </pre>
 * 
 * <p>
 * The parameter and return types supported by JSWrapper are:
 * <ul>
 * <li>primitive</li>
 * <li>boxed primitive</li>
 * <li>String</li>
 * <li>{@link JSFunction}</li>
 * <li>JSWrapper</li>
 * <li>{@link JSList} having a compatible component type specified with the
 * <code>gwt.typeArgs</code> annotation</li>
 * <li>{@link com.google.gwt.core.client.JavaScriptObject} and subtypes</li>
 * </ul>
 * </p>
 * <p>
 * The lowercased bean property name will be used as the key in the JSO, unless
 * a <code>gwt.fieldName</code> annotation appears on the property's getter or
 * the class/interface is annotated with a <code>gwt.namePolicy</code>
 * annotation. The valid values for the <code>namePolicy</code> are the field
 * names on the NamePolicy class, or the name of a class that implements
 * NamePolicy.
 * </p>
 * <p>
 * If the backing object does not contain data for a property accessor,
 * <code>null</code>, <code>0</code>, <code>' '</code>,
 * <code>false</code>, or an empty {@link JSList} will be returned.
 * </p>
 * <p>
 * JSWrapper supports "importing" functions and properties into the generated
 * class. Abstract methods that look like bean-style accessors (getters/setters)
 * will be generated to access property values within the backing object. All
 * other abstract methods will attempt to invoke a method on the backing object.
 * The <code>gwt.imported</code> annotation can be applied to methods that
 * look like bean-style accessors in order to force those methods to be treated
 * as imports, rather than as property accessors.
 * </p>
 * <br/> Example:
 * 
 * <pre>
 * abstract class MixedWrapper implements JSWrapper {
 *   // Property accessor
 *   public abstract int getA();
 * 
 *   // Property accessor
 *   public abstract int getB();
 * 
 *   public int multiply() {
 *     return getA() * getB();
 *   }
 * 
 *   // This method would be imported
 *   public abstract int importedFunction(String s);
 * }
 * 
 * MixedWrapper wrapper = (MixedWrapper)GWT.create(MixedWrapper.class);
 * wrapper.setJSONData(&quot;{a:2, b:5}&quot;);
 * Window.alert(wrapper.multiply());
 * </pre>
 * 
 * would show you the value <code>10</code>.
 * <p>
 * Java functions may be exported to JavaScript callers by declaring a
 * <code>gwt.exported</code> annotation on a concrete Java method. The Java
 * method will be bound to a property on the backing object. When a JavaScript
 * <code>function</code> object is required, use {@link JSFunction}.
 * </p>
 * <p>
 * The annotation <code>gwt.constructor</code> may be applied to a class to
 * specify a JavaScript function to execute when constructing a JSWrapper to use
 * as the initial backing object. A JavaScript Date object could be created by
 * using the value <code>$wnd.Date</code>. If the
 * <code>gwt.constructor</code> annotation is applied to a method within a
 * JSWrapper and the method invoked, the parameters of the method will be passed
 * to the named global function and the resulting JavaScript object will be used
 * as the backing object. The annotation <code>gwt.global</code> is similar to
 * <code>gwt.constructor</code>, however it may be applied only at the class
 * level and the value is interpreted as a globally-accessible object name,
 * rather than a function.
 * </p>
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
