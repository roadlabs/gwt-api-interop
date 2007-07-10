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
import com.google.gwt.user.rebind.SourceWriter;

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

  protected TaskFactory.Policy getPolicy() {
    return TaskFactory.FLYWEIGHT_POLICY;
  }

  protected JParameter getSetterParameter(JMethod setter) {
    return setter.getParameters()[1];
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

    sw.println(") /*-{");
    sw.indent();

    // A binding should have been declared void
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

    if (bindingType != null) {
      // Extract the exported methods
      context.tasks = TaskFactory.extractMethods(logger, typeOracle,
          bindingType, TaskFactory.EXPORTER_POLICY).values();
      writeMethodBindings(context);
    } else {
      logger.log(TreeLogger.WARN, "Not binding to any particular type.", null);
    }

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

    sw.print("var ");
    sw.print(subContext.objRef);
    sw.print(" = ");

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

        fragmentGenerator.toJS(subParams);

        if (i < parameters.length - 1) {
          sw.print(", ");
        }
      }
      sw.print(")");

    } else if (hasTag(constructor, GLOBAL)) {
      String[][] globalMeta = constructor.getMetaData(GLOBAL);
      sw.print(globalMeta[0][0]);

    } else {
      logger.log(TreeLogger.ERROR,
          "Writing a constructor, but no constructor-appropriate annotations",
          null);
      throw new UnableToCompleteException();
    }
    sw.println(";");

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

    // It's invalid to have an imported method without a leading JSO object
    if (imported.getParameters().length > 0) {
      setObjRef(context, imported);
    } else {
      context.parentLogger.branch(TreeLogger.ERROR,
          "Imported methods in a flyweight interface must have a leading "
              + "JavaScriptObject parameter", null);
      throw new UnableToCompleteException();
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
