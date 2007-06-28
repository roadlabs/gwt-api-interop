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
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.jsio.client.JSFlyweightWrapper;
import com.google.gwt.user.rebind.SourceWriter;

import java.util.Collections;
import java.util.Map;

/**
 * Generates a flyweight-style JSIO interface.
 */
public class JSFlyweightWrapperGenerator extends JSWrapperGenerator {

  /**
   * Indicates that a flyweight-style method should be used to bind exported
   * functions from a type into a JavaScriptObject.
   */
  public static final String BINDING = "gwt.binding";

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

  protected boolean isJsoOrPeer(TypeOracle oracle, JType type) {
    JClassType jsoType = oracle.findType(JavaScriptObject.class.getName()).isClass();
    return jsoType.isAssignableFrom(type.isClass())
        || (PeeringFragmentGenerator.findPeer(oracle, type) != null);
  }

  /**
   * Sets the objRef field on a FragmentGeneratorContext to refer to the correct
   * JavaScriptObject.
   */
  protected void setObjRef(FragmentGeneratorContext context, JMethod method)
      throws UnableToCompleteException {
    JParameter param = method.getParameters()[0];
    JClassType paramType = param.getType().isClassOrInterface();
    JField f;

    if (context.typeOracle.findType(JavaScriptObject.class.getName()).equals(
        paramType)) {
      context.objRef = param.getName();

    } else if ((f = PeeringFragmentGenerator.findPeer(context.typeOracle,
        paramType)) != null) {
      context.objRef = param.getName() + ".@"
          + f.getEnclosingType().getQualifiedSourceName() + "::" + f.getName();

    } else {
      context.parentLogger.branch(TreeLogger.ERROR,
          "Invalid first parameter type for flyweight imported function. "
              + "It is not a JavaScriptObject and it lacks a jsoPeer field.",
          null);
      throw new UnableToCompleteException();
    }
  }

  /**
   * Exporting methods via a flyweight interface is done by binding an instance
   * of a type (or just the static methods of a type) to a JSO.
   */
  protected boolean shouldBind(TypeOracle typeOracle, JMethod method) {

    boolean hasBindingTag = hasTag(method, BINDING);
    JParameter[] params = method.getParameters();

    return method.isAbstract()
        && hasBindingTag
        && ((params.length == 1) || (params.length == 2))
        && isJsoOrPeer(typeOracle, params[0].getType())
        && ((params.length == 1) || (params[1].getType().isClassOrInterface() != null));
  }

  /**
   * Methods can never be exported from the flyweight interface.
   */
  protected boolean shouldExport(TypeOracle typeOracle, JMethod method) {
    return false;
  }

  /**
   * Determines if the generator should generate an import binding for the
   * method. XXX extract just the arguments checks?
   */
  protected boolean shouldImport(TypeOracle typeOracle, JMethod method) {
    JClassType enclosing = method.getEnclosingType();
    String methodName = method.getName();
    int arguments = method.getParameters().length;

    boolean hasBindingTag = hasTag(method, BINDING);
    boolean hasImportTag = hasTag(method, IMPORTED);
    boolean methodHasBeanTag = hasTag(method, BEAN_PROPERTIES);
    boolean classHasBeanTag = hasTag(enclosing, BEAN_PROPERTIES);

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

  protected void writeBinding(FragmentGeneratorContext context, JMethod binding)
      throws UnableToCompleteException {
    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG,
        "Writing binding function", null);
    context = new FragmentGeneratorContext(context);
    context.parentLogger = logger;

    SourceWriter sw = context.sw;
    TypeOracle typeOracle = context.typeOracle;
    String[][] bindingMeta = binding.getMetaData(BINDING);

    sw.print("public native void ");
    sw.print(binding.getName());
    sw.print("(");
    JParameter[] params = binding.getParameters();

    context.parameterName = "jso";
    sw.print(params[0].getType().getQualifiedSourceName());
    sw.print(" ");
    sw.print(context.parameterName);

    JClassType bindingType = null;
    if (params.length == 2) {
      // Infer the binding type from the second parameter of the binding
      // method.
      bindingType = params[1].getType().isClassOrInterface();
      context.objRef = "obj";

      sw.print(", ");
      sw.print(bindingType.getQualifiedSourceName());
      sw.print(" ");
      sw.print(context.objRef);
    } else if ((bindingMeta.length == 1) && (bindingMeta[0].length == 1)) {
      // Use the binding type specified in the the gwt.binding annotation.
      bindingType = typeOracle.findType(bindingMeta[0][0]);
      if (bindingType == null) {
        logger.log(TreeLogger.ERROR, "Could not resolve binding type "
            + bindingMeta[0][0], null);
        throw new UnableToCompleteException();
      }
    }

    if (bindingType != null) {
      // Extract the exported methods
      // XXX move method extraction outside of the generator classes
      JSWrapperGenerator g = new JSWrapperGenerator();
      context.tasks = g.extractMethods(logger, typeOracle, bindingType).values();
    } else {
      logger.log(TreeLogger.WARN, "Not binding to any particular type.", null);
      context.tasks = Collections.EMPTY_SET;
    }

    sw.println(") /*-{");
    sw.indent();

    context.returnType = JPrimitiveType.VOID;

    if (context.maintainIdentity && params.length == 2) {
      // XXX link the Java object to the JSO?

      // Verify that the incoming object doesn't already have a wrapper object.
      // If there is a backreference, throw an exception.
      sw.print("if (");
      sw.print(context.parameterName);
      sw.print(".");
      sw.print(BACKREF);
      sw.println(") {");
      sw.indent();
      sw.println("@com.google.gwt.jsio.client.impl.JSONWrapperUtil::throwMultipleWrapperException()();");
      sw.outdent();
      sw.println("}");

      // Assign the backreference from the JSO object to the delegate
      sw.print(context.parameterName);
      sw.print(".");
      sw.print(BACKREF);
      sw.print(" = ");
      sw.print(context.objRef);
      sw.println(";");
    }

    writeEmptyFieldInitializers(context);

    sw.outdent();
    sw.println("}-*/;");
  }

  /**
   * Writes common boilerplate code for all implementations.
   */
  protected void writeBoilerplate(final TreeLogger logger,
      final FragmentGeneratorContext context) throws UnableToCompleteException {
  }

  protected void writeConstructor(FragmentGeneratorContext context,
      JMethod constructor) throws UnableToCompleteException {

    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG,
        "Writing constructor " + constructor.getName(), null);
    SourceWriter sw = context.sw;

    JParameter[] parameters = constructor.getParameters();
    if (parameters == null) {
      parameters = new JParameter[0];
    }

    // Method declaration
    sw.print("public native ");
    sw.print(constructor.getReturnType().getQualifiedSourceName());
    sw.print(" ");
    sw.print(constructor.getName());
    sw.print("(");
    for (int i = 0; i < parameters.length; i++) {
      JType returnType = parameters[i].getType();
      JParameterizedType pType = returnType.isParameterized();

      if (pType != null) {
        sw.print(pType.getRawType().getQualifiedSourceName());
      } else {
        sw.print(returnType.getQualifiedSourceName());
      }

      sw.print(" ");
      sw.print(parameters[i].getName());

      if (i < parameters.length - 1) {
        sw.print(", ");
      }
    }
    sw.print(")");
    sw.println(" /*-{");
    sw.indent();

    JType returnType = constructor.getReturnType();

    FragmentGeneratorContext subContext = new FragmentGeneratorContext(context);
    subContext.returnType = returnType;
    subContext.parameterName = "jsReturn";
    subContext.objRef = "jsReturn";

    sw.print("var jsReturn = ");

    if (hasTag(constructor, CONSTRUCTOR)) {
      // If the imported method is acting as an invocation of a JavaScript
      // constructor, use the new Foo() syntax, otherwise treat is an an
      // invocation on a field on the underlying JSO.
      String[][] constructorMeta = constructor.getMetaData(CONSTRUCTOR);
      sw.print("new ");
      sw.print(constructorMeta[0][0]);

      // Write the invocation's parameter list
      sw.print("(");
      for (int i = 0; i < parameters.length; i++) {
        // Create a sub-context to generate the wrap/unwrap logic
        JType subType = parameters[i].getType();
        FragmentGeneratorContext subParams = new FragmentGeneratorContext(
            context);
        subParams.returnType = subType;
        subParams.parameterName = parameters[i].getName();

        FragmentGenerator fragmentGenerator = context.fragmentGeneratorOracle.findFragmentGenerator(
            logger, context.typeOracle, subType);
        if (fragmentGenerator == null) {
          logger.log(TreeLogger.ERROR, "No fragment generator for "
              + returnType.getQualifiedSourceName(), null);
          throw new UnableToCompleteException();
        }

        fragmentGenerator.fromJS(subParams);

        if (i < parameters.length - 1) {
          sw.println(", ");
        }
        if (i < parameters.length - 1) {
          sw.print(", ");
        }
      }
      sw.println(");");

    } else if (hasTag(constructor, GLOBAL)) {
      String[][] globalMeta = constructor.getMetaData(GLOBAL);
      sw.print(globalMeta[0][0]);
      sw.println(";");

    } else {
      logger.log(TreeLogger.ERROR,
          "Writing a constructor, but no constructor-appropriate annotations",
          null);
      throw new UnableToCompleteException();
    }

    writeEmptyFieldInitializers(subContext);

    sw.print("return ");
    sw.print(subContext.objRef);
    sw.println(";");
    sw.outdent();
    sw.println("}-*/;");
  }

  /**
   * This is a no-op in the flyweight style.
   */
  protected void writeEmptyFieldInitializerMethod(final TreeLogger logger,
      final Map propertyAccessors, final FragmentGeneratorContext context)
      throws UnableToCompleteException {
  }

  protected void writeGetter(FragmentGeneratorContext context, JMethod getter)
      throws UnableToCompleteException {

    context = new FragmentGeneratorContext(context);
    setObjRef(context, getter);
    context.parameterName = context.objRef + "." + context.fieldName;

    super.writeGetter(context, getter);
  }

  protected void writeImported(FragmentGeneratorContext context,
      JMethod imported) throws UnableToCompleteException {

    context = new FragmentGeneratorContext(context);
    // The only imported methods without a leading JSO param are constructors
    if (imported.getParameters().length > 0) {
      setObjRef(context, imported);
    } else {
      context.objRef = null;
    }

    super.writeImported(context, imported);
  }

  protected void writeSetter(FragmentGeneratorContext context, JMethod setter)
      throws UnableToCompleteException {

    context = new FragmentGeneratorContext(context);
    setObjRef(context, setter);

    super.writeSetter(context, setter);
  }

  protected void writeSingleTask(FragmentGeneratorContext context, Task task)
      throws UnableToCompleteException {
    if (task.binding != null) {
      writeBinding(context, task.binding);
    } else {
      super.writeSingleTask(context, task);
    }
  }
}
