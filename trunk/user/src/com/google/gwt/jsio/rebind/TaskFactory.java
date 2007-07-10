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
package com.google.gwt.jsio.rebind;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.jsio.client.JSWrapper;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Examines types to produce Tasks.
 */
public class TaskFactory {
  /**
   * Defines an extraction policy when creating Tasks.
   */
  public static interface Policy {
    /**
     * Specifies the base interface type so that it will be ignored by
     * {@link #extractMethods()}.
     */
    public Collection getOperableMethods(TypeOracle oracle, JClassType clazz);

    /**
     * Exporting methods via a flyweight interface is done by binding an
     * instance of a type (or just the static methods of a type) to a JSO.
     */
    public boolean shouldBind(TypeOracle oracle, JMethod m);

    /**
     * Determines if a method should be treated as an invocation of an
     * underlying JavaScript constructor function.
     */
    public boolean shouldConstruct(TypeOracle oracle, JMethod m);

    /**
     * Determines if the generator should generate an export binding for the
     * method.
     */
    public boolean shouldExport(TypeOracle oracle, JMethod m);

    /**
     * Determines if the generator should implement a particular method. A
     * method will be implemented only if it is abstract and defined in a class
     * derived from JSWrapper
     */
    public boolean shouldImplement(TypeOracle oracle, JMethod m);

    /**
     * Determines if the generator should generate an import binding for the
     * method.
     */
    public boolean shouldImport(TypeOracle oracle, JMethod m);
  }

  /**
   * This policy only checks to see if methods are tagged with gwt.exported.
   * All other methods will be ignored under this policy.
   */
  private static class ExporterPolicy implements Policy {
    public Collection getOperableMethods(TypeOracle oracle, JClassType clazz) {
      Map toReturn = new HashMap();
      Stack stack = new Stack();

      // Start by creating a stack that will look at all supertypes of the
      // class under inspection
      while (clazz != null) {
        stack.push(clazz);
        clazz = clazz.getSuperclass();
      }

      for (Iterator i = stack.iterator(); i.hasNext();) {
        clazz = (JClassType) i.next();

        for (Iterator j = Arrays.asList(clazz.getMethods()).iterator(); j.hasNext();) {
          JMethod m = (JMethod) j.next();
          
          // We add a stripped declaration so that changes which don't affect
          // the overall signature will be overwritten by the methods in the
          // leaf type.
          toReturn.put(m.getReadableDeclaration(true, true, true, true, true),
              m);
        }
      }

      return toReturn.values();
    }

    public boolean shouldBind(TypeOracle oracle, JMethod m) {
      return false;
    }

    public boolean shouldConstruct(TypeOracle oracle, JMethod m) {
      return false;
    }

    public boolean shouldExport(TypeOracle typeOracle, JMethod method) {
      return JSWrapperGenerator.hasTag(method, JSWrapperGenerator.EXPORTED);
    }

    public boolean shouldImplement(TypeOracle oracle, JMethod m) {
      return false;
    }

    public boolean shouldImport(TypeOracle oracle, JMethod m) {
      return false;
    }
  }
  
  /**
   * A variation on WrapperPolicy that handles the different signatures of
   * flyweight-style methods.  Adds binding tasks.
   */
  private static class FlyweightPolicy extends WrapperPolicy {
    public boolean shouldBind(TypeOracle typeOracle, JMethod method) {

      boolean hasBindingTag = JSWrapperGenerator.hasTag(method,
          JSFlyweightWrapperGenerator.BINDING);
      JParameter[] params = method.getParameters();

      return method.isAbstract()
          && hasBindingTag
          && ((params.length == 1) || (params.length == 2))
          && isJsoOrPeer(typeOracle, params[0].getType())
          && ((params.length == 1) || (params[1].getType().isClassOrInterface() != null));
    }

    public boolean shouldImport(TypeOracle typeOracle, JMethod method) {
      JClassType enclosing = method.getEnclosingType();
      String methodName = method.getName();
      int arguments = method.getParameters().length;

      boolean hasBindingTag = JSWrapperGenerator.hasTag(method,
          JSFlyweightWrapperGenerator.BINDING);
      boolean hasImportTag = JSWrapperGenerator.hasTag(method,
          JSWrapperGenerator.IMPORTED);
      boolean methodHasBeanTag = JSWrapperGenerator.hasTag(method,
          JSWrapperGenerator.BEAN_PROPERTIES);
      boolean classHasBeanTag = JSWrapperGenerator.hasTag(enclosing,
          JSWrapperGenerator.BEAN_PROPERTIES);

      boolean isIs = (arguments == 1)
          && (methodName.startsWith("is"))
          && (JPrimitiveType.BOOLEAN.equals(method.getReturnType().isPrimitive()))
          && isJsoOrPeer(typeOracle, method.getParameters()[0].getType());
      boolean isGetter = (arguments == 1)
          && (methodName.startsWith("get") && isJsoOrPeer(typeOracle,
              method.getParameters()[0].getType()));
      boolean isSetter = (arguments == 2)
          && (methodName.startsWith("set") && isJsoOrPeer(typeOracle,
              method.getParameters()[0].getType()));
      boolean propertyAccessor = isIs || isGetter || isSetter;

      return !(hasBindingTag || methodHasBeanTag || (propertyAccessor
          && !hasImportTag && classHasBeanTag));
    }

    protected boolean isJsoOrPeer(TypeOracle oracle, JType type) {
      JClassType jsoType = oracle.findType(JavaScriptObject.class.getName()).isClass();
      return jsoType.isAssignableFrom(type.isClass())
          || (PeeringFragmentGenerator.findPeer(oracle, type) != null);
    }
  }

  /**
   * Creates constructor, import, and property Tasks.
   */
  private static class WrapperPolicy implements Policy {
    public Collection getOperableMethods(TypeOracle typeOracle, JClassType clazz) {
      return Arrays.asList(clazz.getOverridableMethods());
    }

    public boolean shouldBind(TypeOracle typeOracle, JMethod method) {
      return false;
    }

    public boolean shouldConstruct(TypeOracle typeOracle, JMethod method) {
      boolean methodConstructorTag = JSWrapperGenerator.hasTag(method,
          JSWrapperGenerator.CONSTRUCTOR);
      boolean methodGlobalTag = JSWrapperGenerator.hasTag(method,
          JSWrapperGenerator.GLOBAL);

      return methodConstructorTag || methodGlobalTag;
    }

    public boolean shouldExport(TypeOracle typeOracle, JMethod method) {
      return false;
    }

    public boolean shouldImplement(TypeOracle typeOracle, JMethod method) {
      JClassType enclosing = method.getEnclosingType();

      return method.isAbstract()
          && !enclosing.equals(typeOracle.findType(getOperableClassName()));
    }

    public boolean shouldImport(TypeOracle typeOracle, JMethod method) {
      JClassType enclosing = method.getEnclosingType();
      String methodName = method.getName();
      int arguments = method.getParameters().length;

      boolean hasImportTag = JSWrapperGenerator.hasTag(method,
          JSWrapperGenerator.IMPORTED);
      boolean methodHasBeanTag = JSWrapperGenerator.hasTag(method,
          JSWrapperGenerator.BEAN_PROPERTIES);
      boolean classHasBeanTag = JSWrapperGenerator.hasTag(enclosing,
          JSWrapperGenerator.BEAN_PROPERTIES);
      boolean isIs = (arguments == 0)
          && (methodName.startsWith("is"))
          && (JPrimitiveType.BOOLEAN.equals(method.getReturnType().isPrimitive()));
      boolean isGetter = (arguments == 0) && (methodName.startsWith("get"));
      boolean isSetter = (arguments == 1) && (methodName.startsWith("set"));
      boolean propertyAccessor = isIs || isGetter || isSetter;

      return !(methodHasBeanTag || (propertyAccessor && !hasImportTag && classHasBeanTag));
    }

    protected String getOperableClassName() {
      return JSWrapper.class.getName();
    }
  }

  public static final Policy FLYWEIGHT_POLICY = new FlyweightPolicy();

  public static final Policy WRAPPER_POLICY = new WrapperPolicy();

  public static final Policy EXPORTER_POLICY = new ExporterPolicy();

  /**
   * Populate propertyAccessors from an array of JMethods.
   * @return A Map of Strings to Tasks.
   */
  public static Map extractMethods(TreeLogger logger, TypeOracle typeOracle,
      JClassType clazz, Policy policy) throws UnableToCompleteException {
    logger = logger.branch(TreeLogger.DEBUG, "Extracting methods from "
        + clazz.getName(), null);

    // Value to return
    final Map propertyAccessors = new HashMap();

    // Iterate over all methods that the generated subclass could override
    for (Iterator i = policy.getOperableMethods(typeOracle, clazz).iterator(); i.hasNext();) {
      final JMethod m = (JMethod) i.next();
      final String methodName = m.getName();
      logger.log(TreeLogger.DEBUG, "Examining " + m.toString(), null);

      // Look for methods that are to be exported by the presence of
      // the gwt.exported annotation.
      if (policy.shouldExport(typeOracle, m)) {
        Task task = getPropertyPair(propertyAccessors,
            m.getReadableDeclaration());
        task.exported = m;
        logger.log(TreeLogger.DEBUG, "Added as export", null);
        continue;
      }

      if (policy.shouldBind(typeOracle, m)) {
        Task task = getPropertyPair(propertyAccessors,
            m.getReadableDeclaration());
        task.binding = m;
        logger.log(TreeLogger.DEBUG, "Added as binding", null);
        continue;
      }

      // Ignore concrete methods and those methods that are not declared in
      // a subtype of JSWrapper.
      if (!policy.shouldImplement(typeOracle, m)) {
        logger.log(TreeLogger.DEBUG, "Ignoring method " + m.toString(), null);
        continue;
      }

      if (policy.shouldConstruct(typeOracle, m)) {
        // getReadableDeclaration is used so that overloaded methods will
        // be stored with distinct keys.
        Task task = getPropertyPair(propertyAccessors,
            m.getReadableDeclaration());
        task.constructor = m;
        logger.log(TreeLogger.DEBUG, "Using constructor/global override", null);

        // Enable bypassing of name-determination logic with the presence of the
        // @gwt.imported annotation
      } else if (policy.shouldImport(typeOracle, m)) {
        // getReadableDeclaration is used so that overloaded methods will
        // be stored with distinct keys.
        Task task = getPropertyPair(propertyAccessors,
            m.getReadableDeclaration());
        task.imported = m;
        logger.log(TreeLogger.DEBUG, "Using import override", null);

        // Look for setFoo()
      } else if (methodName.startsWith("set")) {
        String propertyName = getPropertyNameFromMethod(m);
        Task task = getPropertyPair(propertyAccessors, propertyName);
        task.setter = m;
        logger.log(TreeLogger.DEBUG, "Determined this is a setter", null);

        // Look for getFoo() or isFoo()
      } else if ((methodName.startsWith("get") || methodName.startsWith("is"))) {
        String propertyName = getPropertyNameFromMethod(m);
        Task task = getPropertyPair(propertyAccessors, propertyName);
        task.getter = m;
        logger.log(TreeLogger.DEBUG, "Determined this is a getter", null);

        // We could not make a decision on what should be done with the method.
      } else {
        logger.log(TreeLogger.ERROR, "Could not decide on implementation of "
            + m.getName(), null);
        throw new UnableToCompleteException();
      }
    }

    return propertyAccessors;
  }

  /**
   * Utility method to extract the bean-style property name from a method.
   * 
   * @return The property name if the method's name looks like a bean property,
   *         otherwise the method's name.
   */
  protected static String getPropertyNameFromMethod(JMethod method) {
    String methodName = method.getName();

    if (methodName.startsWith("get")) {
      return methodName.substring(3);

    } else if (methodName.startsWith("set")) {
      return methodName.substring(3);

    } else if (methodName.startsWith("is")) {
      return methodName.substring(2);

    } else {
      return methodName;
    }
  }

  /**
   * Utility method to access a Map of String, Tasks.
   * 
   * @param propertyAccessors The Map to operate on
   * @param property The name of the property
   * @return A Task in the given map; created if it does not exist
   */
  protected static Task getPropertyPair(Map propertyAccessors, String property) {
    if (propertyAccessors.containsKey(property)) {
      return (Task) propertyAccessors.get(property);
    } else {
      final Task pair = new Task();
      propertyAccessors.put(property, pair);
      return pair;
    }
  }
  
  /**
   * Utility class.
   */
  private TaskFactory() {
  }
}
