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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.jsio.client.JSWrapper;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The Generator that provides implementations of {@link JSWrapper}.
 */
public class JSWrapperGenerator extends Generator {

  /**
   * Defines one or more methods to be generated.
   */
  private static class Task {
    JMethod getter;
    JMethod setter;
    JMethod imported;
    String fieldName;
  }

  /**
   * Allows JSFunction classes to explicitly specify the function to be
   * exported.
   */
  public static final String EXPORTED = "gwt.exported";

  /**
   * The name of the metadata field that contains the underlying property name
   * to use for the bean property.
   */
  public static final String FIELD_NAME = "gwt.fieldName";

  /**
   * The policy to use when mangling bean property names to JSON object property
   * names.
   */
  public static final String NAME_POLICY = "gwt.namePolicy";

  /**
   * Allows classes to provide the name of a function that will construct a
   * backing object for the wrapper at instantiation time.
   */
  public static final String CONSTRUCTOR = "gwt.constructor";

  /**
   * This annotation is used on a JSWrapper class in a manner similar to
   * <code>gwt.constructor</code> although the value is interpreted as a value
   * reference rather than a function.
   */
  public static final String GLOBAL = "gwt.global";

  /**
   * Allows methods that look like bean property setter/getters to be treated as
   * imported methods.
   */
  public static final String IMPORTED = "gwt.imported";

  /**
   * The name of the backing object field.
   */
  private static final String OBJ = "__jsonObject";

  /**
   * The name of the static field that contains the class's Extractor instance.
   */
  private static final String EXTRACTOR = "__extractor";

  /**
   * Singleton instance of the FragmentGeneratorOracle for the system.
   */
  private static final FragmentGeneratorOracle FRAGMENT_ORACLE = new FragmentGeneratorOracle();

  /**
   * Entry point into the Generator.
   */
  public String generate(TreeLogger logger, GeneratorContext context,
      java.lang.String typeName) throws UnableToCompleteException {

    // The TypeOracle knows about all types in the type system
    final TypeOracle typeOracle = context.getTypeOracle();

    // Get a reference to the type that the generator should implement
    final JClassType sourceType = typeOracle.findType(typeName);

    // Ensure that the requested type exists
    if (sourceType == null) {
      logger.log(TreeLogger.ERROR, "Could not find requested typeName", null);
      throw new UnableToCompleteException();
    }

    // Pick a name for the generated class to not conflict. Enclosing class
    // names must be preserved.
    final String generatedSimpleSourceName = "__"
        + sourceType.getName().replaceAll("\\.", "__") + "Impl";

    // Begin writing the generated source.
    final ClassSourceFileComposerFactory f = new ClassSourceFileComposerFactory(
        sourceType.getPackage().getName(), generatedSimpleSourceName);

    // Pull in source imports
    f.addImport(GWT.class.getName());
    f.addImport(JavaScriptObject.class.getName());
    // This is a cheat, but doesn't require excessive maintenance
    f.addImport("com.google.gwt.jsio.client.*");
    f.addImport("com.google.gwt.jsio.client.impl.*");

    // Either extend an abstract base class or implement the interface
    if (sourceType.isClass() != null) {
      f.setSuperclass(sourceType.getQualifiedSourceName());

    } else if (sourceType.isInterface() != null) {
      f.addImplementedInterface(sourceType.getQualifiedSourceName());

    } else {
      // Something is very wrong if this statement is reached
      logger.log(TreeLogger.ERROR,
          "Requested JClassType is neither a class nor an interface.", null);
      throw new UnableToCompleteException();
    }

    // All source gets written through this Writer
    final PrintWriter out = context.tryCreate(logger,
        sourceType.getPackage().getName(), generatedSimpleSourceName);

    // If an implementation already exists, we don't need to do any work
    if (out != null) {
      // We really use a SourceWriter since it's convenient
      final SourceWriter sw = f.createSourceWriter(context, out);

      // Get a Map<String, Task>
      final Map propertyAccessors = extractMethods(logger, typeOracle,
          sourceType);

      // Create the base context to be used during generation
      FragmentGeneratorContext fragmentContext = new FragmentGeneratorContext();
      fragmentContext.parentLogger = logger;
      fragmentContext.fragmentGeneratorOracle = FRAGMENT_ORACLE;
      fragmentContext.typeOracle = typeOracle;
      fragmentContext.sw = sw;
      fragmentContext.objRef = "@" + f.getCreatedClassName() + "::" + OBJ;
      fragmentContext.simpleTypeName = generatedSimpleSourceName;
      fragmentContext.qualifiedTypeName = f.getCreatedClassName();
      fragmentContext.returnType = sourceType;
      fragmentContext.creatorFixups = new HashSet();

      // Determine the correct expression to use to initialize the object
      String constructor;
      String[][] constructorMeta = sourceType.getMetaData(CONSTRUCTOR);
      String[][] globalMeta = sourceType.getMetaData(GLOBAL);
      if (globalMeta.length == 1 && globalMeta[0].length == 1) {
        constructor = globalMeta[0][0];
      } else if (constructorMeta.length == 1 && constructorMeta[0].length == 1) {
        constructor = "new " + constructorMeta[0][0] + "()";
      } else {
        constructor = "{}";
      }

      // Perform sanity checks on the extracted information
      validateType(propertyAccessors, fragmentContext);

      // Write all code that's not implementing methods
      writeBoilerplate(logger, fragmentContext, constructor);
      writeEmptyFieldInitializer(logger, propertyAccessors, fragmentContext);
      writeMethods(logger, typeOracle, sw, sourceType, propertyAccessors,
          fragmentContext);
      writeFixups(logger, typeOracle, sw, fragmentContext.creatorFixups);

      // Write the generated code to disk
      sw.commit(logger);
    }

    // Return the name of the concrete class
    return f.getCreatedClassName();
  }

  /**
   * Determine the correct field name to use for a method.
   */
  protected String extractFieldName(TreeLogger logger, JMethod m,
      boolean imported) throws UnableToCompleteException {
    logger = logger.branch(TreeLogger.DEBUG, "Determining field name for "
        + m.getName(), null);

    String[][] meta = m.getMetaData(FIELD_NAME);

    if (meta == null || meta.length == 0) {
      // If the method is imported and there's no overriding annotation,
      // just return the methods original name. This ensures that native
      // JS methods that look like bean getter/setters don't get munged.
      if (imported) {
        return m.getName();
      }

      // If no gwt.fieldName is specified, see if there's a naming policy
      // defined on the enclosing class.
      JClassType enclosing = m.getEnclosingType();
      String[][] policyMeta = enclosing.getMetaData(NAME_POLICY);
      NamePolicy policy;

      // If there is no namePolicy or it's not of the desired form, default
      // to the JavaBean-style naming policy.
      if (policyMeta == null || policyMeta.length != 1
          || policyMeta[0].length != 1) {
        policy = NamePolicy.BEAN;
        logger.log(TreeLogger.DEBUG, "No useful policy metadata for class",
            null);

      } else {
        // Use the provided name to access fields within NamePolicy
        String policyName = policyMeta[0][0];
        try {
          Field f = NamePolicy.class.getDeclaredField(policyName.toUpperCase());
          policy = (NamePolicy) f.get(null);

        } catch (IllegalAccessException e) {
          logger.log(TreeLogger.ERROR, "Bad gwt.namePolicy " + policyName, e);
          throw new UnableToCompleteException();

        } catch (NoSuchFieldException e) {
          // This means that the value specified is not a field, but likely
          // a class name. Try instantiating one and seeing if it's a
          // subclass of NamePolicy.
          try {
            Class clazz = Class.forName(policyName);
            if (NamePolicy.class.isAssignableFrom(clazz)) {
              policy = (NamePolicy) clazz.newInstance();
            } else {
              logger.log(TreeLogger.ERROR,
                  "@gwt.namePolicy is not an implementation of NamePolicy",
                  null);
              throw new UnableToCompleteException();
            }
          } catch (ClassNotFoundException ee) {
            logger.log(TreeLogger.ERROR, "Bad gwt.namePolicy " + policyName, ee);
            throw new UnableToCompleteException();
          } catch (IllegalAccessException ee) {
            logger.log(TreeLogger.ERROR, "Bad gwt.namePolicy " + policyName, ee);
            throw new UnableToCompleteException();
          } catch (InstantiationException ee) {
            logger.log(TreeLogger.ERROR, "Bad gwt.namePolicy " + policyName, ee);
            throw new UnableToCompleteException();
          }
        }
      }

      // Execute the conversion
      String propertyName = getPropertyNameFromMethod(m);
      return policy.convert(propertyName);

    } else if (meta[0].length == 1) {
      // Use the field name specified on the method
      logger.log(TreeLogger.DEBUG, "Overriding field name based on annotation",
          null);
      return meta[0][0];

    } else {
      // There were multiple field name values specified, so bail.
      logger.log(TreeLogger.ERROR, "Unable to evaluate field name annotation",
          null);
      throw new UnableToCompleteException();
    }
  }

  /**
   * Popupulate propertyAccessors from an array of JMethods.
   */
  protected Map extractMethods(TreeLogger logger, final TypeOracle typeOracle,
      final JClassType clazz) throws UnableToCompleteException {
    logger = logger.branch(TreeLogger.DEBUG, "Extracting methods from "
        + clazz.getName(), null);

    // Value to return
    final Map propertyAccessors = new HashMap();

    // Iterate over all methods that the generated subclass could override
    final JMethod[] methods = clazz.getOverridableMethods();
    for (int i = 0; i < methods.length; i++) {
      final JMethod m = methods[i];
      final String methodName = m.getName();
      logger.log(TreeLogger.DEBUG, "Examining " + m.toString(), null);

      // Ignore concrete methods and those methods that are not declared in
      // a subtype of JSWrapper.
      if (!shouldImplement(typeOracle, m)) {
        logger.log(TreeLogger.DEBUG, "Ignoring method " + m.toString(), null);
        continue;
      }

      // Enable bypassing of this logic with the presence of the
      // @gwt.imported annotation
      String[][] imported = m.getMetaData(IMPORTED);
      if (imported.length == 1 && imported[0].length == 1) {
        String fieldName = extractFieldName(logger, m, true);
        Task pair = getPropertyPair(propertyAccessors,
            m.getReadableDeclaration());
        pair.imported = m;
        pair.fieldName = fieldName;
        logger.log(TreeLogger.DEBUG, "Using import override", null);

        // Look for setFoo()
      } else if (methodName.startsWith("set")
          && (m.getParameters().length == 1)) {
        String propertyName = getPropertyNameFromMethod(m);
        Task pair = getPropertyPair(propertyAccessors, propertyName);
        pair.setter = m;
        logger.log(TreeLogger.DEBUG, "Determined this is a setter", null);

        // Look for getFoo() or isFoo()
      } else if ((methodName.startsWith("get") || methodName.startsWith("is"))
          && (m.getParameters().length == 0)) {
        String propertyName = getPropertyNameFromMethod(m);
        Task pair = getPropertyPair(propertyAccessors, propertyName);
        pair.getter = m;
        pair.fieldName = extractFieldName(logger, m, false);
        logger.log(TreeLogger.DEBUG, "Determined this is a getter", null);

        // Assume that it's an imported function
      } else {
        String fieldName = extractFieldName(logger, m, true);
        Task pair = getPropertyPair(propertyAccessors,
            m.getReadableDeclaration());
        pair.imported = m;
        pair.fieldName = fieldName;
        logger.log(TreeLogger.DEBUG, "Determined this is a imported", null);
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
  protected String getPropertyNameFromMethod(JMethod method) {
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
  protected Task getPropertyPair(Map propertyAccessors, String property) {
    if (propertyAccessors.containsKey(property)) {
      return (Task) propertyAccessors.get(property);
    } else {
      final Task pair = new Task();
      propertyAccessors.put(property, pair);
      return pair;
    }
  }

  /**
   * Determines if the generator should implement a particular method.
   */
  protected boolean shouldImplement(TypeOracle typeOracle, JMethod method) {
    JClassType enclosing = method.getEnclosingType();

    return method.isAbstract()
        && enclosing.isAssignableTo(typeOracle.findType(JSWrapper.class.getName()))
        && !enclosing.equals(typeOracle.findType(JSWrapper.class.getName()));
  }

  /**
   * Aggregate pre-write validation checks.
   */
  protected void validateType(Map propertyAccessors,
      FragmentGeneratorContext context) throws UnableToCompleteException {
    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG,
        "Validating extracted type information", null);

    for (final Iterator i = propertyAccessors.entrySet().iterator(); i.hasNext();) {

      final Map.Entry entry = (Map.Entry) i.next();
      final String propertyName = (String) entry.getKey();
      final Task pair = (Task) entry.getValue();

      if ((pair.imported != null)
          && ((pair.getter != null) || (pair.setter != null))) {
        logger.log(TreeLogger.ERROR, "Imported functions may not be combined "
            + "with bean-style accessors", null);
        throw new UnableToCompleteException();
      }

      // If we have no getter or imported function, then the property is
      // useless.
      if (pair.getter == null && pair.imported == null) {
        logger.log(TreeLogger.ERROR, "No getter or import for property "
            + propertyName
            + ". Perhaps your setter needs an @gwt.import annotation", null);
        throw new UnableToCompleteException();
      }

      // Sanity check that we picked up the right setter
      if ((pair.setter != null)
          && !pair.getter.getReturnType().equals(
              pair.setter.getParameters()[0].getType())) {
        logger.log(TreeLogger.ERROR, "Setter has different parameter type "
            + "from getter for property " + propertyName, null);
        throw new UnableToCompleteException();
      }
    }
  }

  /**
   * Writes common boilerplate code for all implementations.
   */
  protected void writeBoilerplate(final TreeLogger logger,
      final FragmentGeneratorContext context, final String constructor)
      throws UnableToCompleteException {

    SourceWriter sw = context.sw;
    TypeOracle typeOracle = context.typeOracle;
    JType returnType = context.returnType;

    // The backing object
    sw.print("private JavaScriptObject ");
    sw.print(OBJ);
    sw.println(";");

    // Build a constructor to initialize state.
    sw.print("public ");
    sw.print(context.simpleTypeName);
    sw.println("() {");
    sw.indent();
    sw.println("setJavaScriptObject(__nativeInit());");
    sw.outdent();
    sw.println("}");

    // Build a factory method for use by JSNI code. This method would be
    // unnecessary if it were possible to construct Java objects from within a
    // JSNI block.
    sw.print("public static ");
    sw.print(context.returnType.getQualifiedSourceName());
    sw.print(" __create__");
    sw.println("() {");
    sw.indent();
    sw.print("return (");
    sw.print(context.returnType.getQualifiedSourceName());
    sw.print(")GWT.create(");
    sw.print(context.returnType.getQualifiedSourceName());
    sw.println(".class);");
    sw.outdent();
    sw.println("}");

    // Initialize native state of the wrapper
    sw.println("private native JavaScriptObject __nativeInit() /*-{");
    sw.indent();
    sw.print("return ");
    sw.print(constructor);
    sw.println(";");
    sw.outdent();
    sw.println("}-*/;");

    // Allow the backing JSONObject to be accessed
    sw.println("public JavaScriptObject getJavaScriptObject() {");
    sw.indent();
    sw.print("return ");
    sw.print(OBJ);
    sw.println(";");
    sw.outdent();
    sw.println("}");

    // Defer actual parsing to JSONWrapperUtil to take advantage of using
    // a common function implementation between generated classes.
    sw.println("public void setJSONData(String data) throws JSONWrapperException {");
    sw.indent();
    sw.println("setJavaScriptObject(JSONWrapperUtil.evaluate(data));");
    sw.outdent();
    sw.println("}");

    // Satisfies JSWrapper and allows generated implementations to
    // efficiently initialize new objects.
    sw.println("public void setJavaScriptObject(JavaScriptObject obj) {");
    sw.indent();
    sw.print(OBJ);
    sw.println(" = obj;");
    sw.println("__initializeEmptyFields();");
    sw.outdent();
    sw.println("}");

    // If the generated class will be used with a JSList, we need an Extractor
    // implementation. We'll create an implementation per generated
    // class to ensure that if the class is used with a JSList, only one
    // instance of the Extractor will ever exist.
    sw.println("public final Extractor getExtractor() {");
    sw.indent();
    sw.print("return ");
    sw.print(EXTRACTOR);
    sw.print(";");
    sw.outdent();
    sw.println("}");

    // The one instance of the Extractor
    sw.print("private final static Extractor ");
    sw.print(EXTRACTOR);
    sw.print(" = new Extractor() {");
    sw.indent();
    FragmentGeneratorContext subParams = new FragmentGeneratorContext(context);
    subParams.parameterName = "obj";
    FragmentGenerator fragmentGenerator = context.fragmentGeneratorOracle.findFragmentGenerator(
        typeOracle, returnType);
    // We can't create new Java objects from within JSNI blocks, so we implement
    // a getter in Java that defers to an initializer that's writter in JS.
    boolean twoStep = fragmentGenerator.fromJSRequiresObject();
    if (twoStep) {
      sw.print("public native Object");
      sw.println(" fromJS(JavaScriptObject obj) /*-{");
      sw.indent();
      sw.print("var toReturn = ");
      fragmentGenerator.writeJSNIObjectCreator(subParams);
      sw.println(";");
      sw.print("toReturn.");
      fragmentGenerator.fromJS(subParams);
      sw.println(";");
      sw.println("return toReturn;");
      sw.outdent();
      sw.println("}-*/;");

    } else {
      sw.println("public native Object fromJS(JavaScriptObject obj) /*-{");
      sw.indent();
      sw.print("return ");
      fragmentGenerator.fromJS(subParams);
      sw.println(";");
      sw.outdent();
      sw.println("}-*/;");
    }

    // Write the Extracor's toJS function and close the Extractor
    // implementation.
    sw.println("public native JavaScriptObject toJS(Object obj) /*-{");
    sw.indent();
    sw.print("return ");
    fragmentGenerator.toJS(subParams);
    sw.println(";");
    sw.outdent();
    sw.println("}-*/;");
    sw.outdent();
    sw.println("};");
  }

  /**
   * Ensures that no field referenced by generated logic will ever return an
   * undefined value. This allows every subsequent getFoo() call to simply
   * return the field value, without having to check it for an undefined value.
   */
  protected void writeEmptyFieldInitializer(final TreeLogger logger,
      final Map propertyAccessors, final FragmentGeneratorContext context)
      throws UnableToCompleteException {
    SourceWriter sw = context.sw;

    sw.println("private native void __initializeEmptyFields() /*-{");
    sw.indent();

    for (final Iterator i = propertyAccessors.entrySet().iterator(); i.hasNext();) {
      final Map.Entry entry = (Map.Entry) i.next();
      final Task pair = (Task) entry.getValue();
      final JType returnType = (pair.getter != null ? pair.getter
          : pair.imported).getReturnType();

      FragmentGenerator fragmentGenerator = FRAGMENT_ORACLE.findFragmentGenerator(
          context.typeOracle, returnType);

      // Don't wrap imported function so that errors can be caught in
      // hosted mode
      if (pair.getter == null) {
        continue;
      }

      sw.print("if (!(\"");
      sw.print(pair.fieldName);
      sw.print("\" in this.");
      sw.print(context.objRef);
      sw.println(")) {");
      sw.indent();
      sw.print("this.");
      sw.print(context.objRef);
      sw.print(".");
      sw.print(pair.fieldName);
      sw.print(" = ");
      sw.print(fragmentGenerator.defaultValue(context.typeOracle, returnType));
      sw.println(";");
      sw.outdent();
      sw.println("}");
    }

    sw.outdent();
    sw.println("}-*/;");
  }

  protected void writeFixups(TreeLogger logger, TypeOracle typeOracle,
      SourceWriter sw, Set creatorFixups) throws UnableToCompleteException {
    for (Iterator i = creatorFixups.iterator(); i.hasNext();) {
      JClassType asClass = ((JType) i.next()).isClassOrInterface();

      // If the type is parameterized, we want to replace it with the raw type
      // so that no angle-brackets are used.
      JParameterizedType pType = asClass.isParameterized();
      if (pType != null) {
        asClass = pType.getRawType();
      }

      sw.print("private static ");
      sw.print(asClass.getQualifiedSourceName());
      sw.print(" __create__");
      sw.print(asClass.getQualifiedSourceName().replaceAll("\\.", "_"));
      sw.println("() {");
      sw.indent();
      sw.print("return (");
      sw.print(asClass.getQualifiedSourceName());
      sw.print(")GWT.create(");
      sw.print(asClass.getQualifiedSourceName());
      sw.println(".class);");
      sw.outdent();
      sw.println("}");
    }
  }

  protected void writeGetter(TreeLogger parentLogger, TypeOracle typeOracle,
      SourceWriter sw, JMethod getter, String fieldName,
      FragmentGeneratorContext context) throws UnableToCompleteException {

    TreeLogger logger = parentLogger.branch(TreeLogger.DEBUG, "Writing getter "
        + getter.getName(), null);

    final JType returnType = getter.getReturnType();

    FragmentGenerator fragmentGenerator = FRAGMENT_ORACLE.findFragmentGenerator(
        typeOracle, context.returnType);

    sw.print("public native ");
    sw.print(returnType.getQualifiedSourceName());
    sw.print(" ");
    sw.print(getter.getName());
    sw.print("()");
    sw.println(" /*-{");
    sw.indent();

    // Create a new wrapper object when a wrapper is returned from JS
    boolean twoStep = fragmentGenerator.fromJSRequiresObject();
    if (twoStep) {
      sw.print("if (");
      sw.print(context.parameterName);
      sw.print(" == null) {");
      sw.indent();
      sw.println("return null;");
      sw.outdent();
      sw.println("}");

      sw.print("var toReturn = ");
      fragmentGenerator.writeJSNIObjectCreator(context);
      sw.println(";");

      sw.print("toReturn.");
      fragmentGenerator.fromJS(context);
      sw.println(";");
      sw.println("return toReturn;");
    } else {
      sw.print("return ");
      fragmentGenerator.fromJS(context);
      sw.println(";");
    }
    sw.outdent();
    sw.println("}-*/;");
  }

  protected void writeImported(TreeLogger parentLogger, TypeOracle typeOracle,
      SourceWriter sw, JMethod imported, String fieldName,
      FragmentGeneratorContext context) throws UnableToCompleteException {

    TreeLogger logger = parentLogger.branch(TreeLogger.DEBUG, "Writing import "
        + imported.getName(), null);

    // Simplifies the rest of writeImported
    JParameter[] parameters = imported.getParameters();
    if (parameters == null) {
      parameters = new JParameter[0];
    }

    // Method declaration
    sw.print("native ");
    sw.print(imported.getReadableDeclaration(false, true, true, false, true));
    sw.println(" /*-{");
    sw.indent();

    // Assign the Java parameters to the function to their corresponding
    // JavaScriptObject values.
    // var jso0 = <conversion logic for first parameter>;
    // var jso1 = <conversion logic for second parameter>;
    // ......
    for (int i = 0; i < parameters.length; i++) {
      JType returnType = parameters[i].getType();

      FragmentGeneratorContext subParams = new FragmentGeneratorContext(context);
      subParams.returnType = returnType;
      subParams.parameterName = parameters[i].getName();

      FragmentGenerator fragmentGenerator = FRAGMENT_ORACLE.findFragmentGenerator(
          context.typeOracle, returnType);

      sw.print("var jso");
      sw.print(String.valueOf(i));
      sw.print(" = ");
      fragmentGenerator.toJS(subParams);
      sw.println(";");
    }

    // The return type of the function we're importing.
    JType returnType = imported.getReturnType();

    // Don't bother recording a return value for void invocations.
    if (!JPrimitiveType.VOID.equals(returnType.isPrimitive())) {
      sw.print("var toReturn = ");
    }

    // If the imported method is acting as an invocation of a JavaScript
    // constructor, use the new Foo() syntax, otherwise treat is an an
    // invocation on a field on the underlying JSO.
    String[][] constructorMeta = imported.getMetaData(CONSTRUCTOR);
    boolean useConstructor = (constructorMeta.length == 1)
        && (constructorMeta[0].length == 1);
    if (useConstructor) {
      sw.print("new ");
      sw.print(constructorMeta[0][0]);
    } else {
      sw.print("this.");
      sw.print(context.objRef);
      sw.print(".");
      sw.print(context.fieldName);
    }

    // Write the invocation's parameter list
    sw.print("(");
    for (int i = 0; i < parameters.length; i++) {
      sw.print("jso" + i);
      if (i < parameters.length - 1) {
        sw.print(", ");
      }
    }
    sw.println(");");

    // Wrap the return type in the correct Java type. Void returns are ignored
    if (!JPrimitiveType.VOID.equals(returnType.isPrimitive())) {
      FragmentGeneratorContext subParams = new FragmentGeneratorContext(context);
      subParams.returnType = returnType;
      subParams.objRef = "this";
      subParams.parameterName = "toReturn";

      FragmentGenerator fragmentGenerator = FRAGMENT_ORACLE.findFragmentGenerator(
          context.typeOracle, returnType);

      if (fragmentGenerator.fromJSRequiresObject()) {
        sw.print("this.");
      } else {
        sw.print("toReturn = ");
      }

      fragmentGenerator.fromJS(subParams);
      sw.println(";");
      sw.print("return ");
      sw.println(useConstructor ? "this;" : "toReturn;");
    }

    sw.outdent();
    sw.println("}-*/;");
  }

  /**
   * Write the field, getter, and setter for the properties we know about. Also
   * write BusObjectImpl methods for Map-style access.
   */
  protected void writeMethods(final TreeLogger logger,
      final TypeOracle typeOracle, final SourceWriter sw,
      final JClassType clazz, final Map propertyAccessors,
      final FragmentGeneratorContext context) throws UnableToCompleteException {

    for (final Iterator i = propertyAccessors.entrySet().iterator(); i.hasNext();) {

      final Map.Entry entry = (Map.Entry) i.next();
      final String propertyName = (String) entry.getKey();
      final Task pair = (Task) entry.getValue();
      final String fieldName = pair.fieldName;

      logger.log(TreeLogger.DEBUG, "Implementing property " + propertyName,
          null);

      context.returnType = (pair.getter != null ? pair.getter : pair.imported).getReturnType();
      context.fieldName = fieldName;

      if (pair.setter != null) {
        JParameter parameter = pair.setter.getParameters()[0];
        // What the user called the parameter
        context.parameterName = parameter.getName();
        writeSetter(logger, typeOracle, sw, pair.setter, fieldName, context);
      }

      if (pair.getter != null) {
        context.parameterName = "this." + context.objRef + "."
            + context.fieldName;
        writeGetter(logger, typeOracle, sw, pair.getter, fieldName, context);
      }

      if (pair.imported != null) {
        writeImported(logger, typeOracle, sw, pair.imported, fieldName, context);
      }
    }
  }

  protected void writeSetter(TreeLogger parentLogger, TypeOracle typeOracle,
      SourceWriter sw, JMethod setter, String fieldName,
      FragmentGeneratorContext context) throws UnableToCompleteException {

    TreeLogger logger = parentLogger.branch(TreeLogger.DEBUG, "Writing setter "
        + setter.getName(), null);

    JType parameterType = context.returnType;

    FragmentGenerator fragmentGenerator = FRAGMENT_ORACLE.findFragmentGenerator(
        typeOracle, context.returnType);
    if (fragmentGenerator == null) {
      throw new UnableToCompleteException();
    }

    // Ensure that there will be no angle-bracket in the output
    JParameterizedType pType = parameterType.isParameterized();
    if (pType != null) {
      parameterType = pType.getRawType();
    }

    sw.print("public native void ");
    sw.print(setter.getName());
    sw.print("(");
    sw.print(parameterType.getQualifiedSourceName());
    sw.print(" ");
    sw.print(context.parameterName);
    sw.println(") /*-{");
    sw.indent();
    sw.print("this.");
    sw.print(context.objRef);
    sw.print(".");
    sw.print(context.fieldName);
    sw.print(" = ");
    fragmentGenerator.toJS(context);
    sw.println(";");
    sw.outdent();
    sw.println("}-*/;");
  }
}
