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
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.jsio.client.JSFlyweightWrapper;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Generates a flyweight-style JSIO interface.
 */
public class JSFlyweightWrapperGenerator extends JSWrapperGenerator {

  /**
   * The name of a static method that can be implemented in a class so that it
   * can receive a peer object. It must accept a JSO.
   */
  public static final String CREATE_PEER = "createPeer";

  protected int getImportOffset() {
    return 1;
  }

  protected String getOperableClassName() {
    return JSFlyweightWrapper.class.getName();
  }

  protected JParameter getSetterParameter(JMethod setter) {
    return setter.getParameters()[1];
  }

  /**
   * Determines if the generator should generate an import binding for the
   * method. XXX extract just the arguments checks?
   */
  protected boolean shouldImport(TypeOracle typeOracle, JMethod method) {
    JClassType enclosing = method.getEnclosingType();
    JClassType jsoType =
        typeOracle.findType(JavaScriptObject.class.getName()).isClass();
    String methodName = method.getName();
    final int arguments = method.getParameters().length;

    boolean hasImportTag = hasTag(method, IMPORTED);
    boolean methodHasBeanTag = hasTag(method, BEAN_PROPERTIES);
    boolean classHasBeanTag = hasTag(enclosing, BEAN_PROPERTIES);

    boolean isIs =
        (arguments == 0)
            && (methodName.startsWith("is"))
            && (JPrimitiveType.BOOLEAN.equals(method.getReturnType().isPrimitive()));
    boolean isGetter =
        (arguments == 1)
            && (methodName.startsWith("get") && method.getParameters()[0].getType().isClass().equals(
                jsoType));
    boolean isSetter =
        (arguments == 2)
            && (methodName.startsWith("set") && method.getParameters()[0].getType().isClass().equals(
                jsoType));
    boolean propertyAccessor = isIs || isGetter || isSetter;

    return !(methodHasBeanTag || (propertyAccessor && !hasImportTag && classHasBeanTag));
  }

  /**
   * Writes common boilerplate code for all implementations.
   */
  protected void writeBoilerplate(final TreeLogger logger,
      final FragmentGeneratorContext context, final String constructor)
      throws UnableToCompleteException {
    SourceWriter sw = context.sw;

    sw.println("public native void bind(JavaScriptObject jso, Object peer) /*-{");
    sw.indent();

    if (context.maintainIdentity) {
      // Verify that the incoming object doesn't already have a wrapper object.
      // If there is a backreference, throw an exception.
      sw.print("if (jso.hasOwnProperty('");
      sw.print(BACKREF);
      sw.println("')) {");
      sw.indent();
      sw.println("@com.google.gwt.jsio.client.impl.JSONWrapperUtil::throwMultipleWrapperException()();");
      sw.outdent();
      sw.println("}");

      // Assign the backreference from the wrapped object to the wrapper
      sw.print("jso.");
      sw.print(BACKREF);
      sw.println(" = peer;");
    }

    if (!context.readOnly) {
      // Initialize any other fields if the JSWrapper is read-write
      sw.print("this.@");
      sw.print(context.qualifiedTypeName);
      sw.println("::__initializeEmptyFields(Lcom/google/gwt/core/client/JavaScriptObject;)(jso);");
    }
    sw.outdent();
    sw.println("}-*/;");
  }

  protected void writeConstructor(TreeLogger logger, TypeOracle typeOracle,
      SourceWriter sw, JMethod constructor, FragmentGeneratorContext context)
      throws UnableToCompleteException {

    context = new FragmentGeneratorContext(context);
    context.parameterName = "";
    context.objRef = "toReturn";

    super.writeConstructor(logger, typeOracle, sw, constructor, context);
  }

  protected void writeGetter(TreeLogger logger, TypeOracle typeOracle,
      SourceWriter sw, JMethod getter, String fieldName,
      FragmentGeneratorContext context) throws UnableToCompleteException {

    context = new FragmentGeneratorContext(context);
    context.objRef = getter.getParameters()[0].getName();
    context.parameterName = context.objRef + "." + context.fieldName;

    super.writeGetter(logger, typeOracle, sw, getter, fieldName, context);
  }

  protected void writeImported(TreeLogger logger, TypeOracle typeOracle,
      SourceWriter sw, JMethod imported, String fieldName,
      FragmentGeneratorContext context) throws UnableToCompleteException {

    context = new FragmentGeneratorContext(context);
    // The only imported methods without a leading JSO param are constructors
    if (imported.getParameters().length > 0) {
      context.objRef = imported.getParameters()[0].getName();
    } else {
      context.objRef = null;
    }

    super.writeImported(logger, typeOracle, sw, imported, fieldName, context);
  }

  protected void writeSetter(TreeLogger logger, TypeOracle typeOracle,
      SourceWriter sw, JMethod setter, String fieldName,
      FragmentGeneratorContext context) throws UnableToCompleteException {

    context = new FragmentGeneratorContext(context);
    context.objRef = setter.getParameters()[0].getName();

    super.writeSetter(logger, typeOracle, sw, setter, fieldName, context);
  }
}
