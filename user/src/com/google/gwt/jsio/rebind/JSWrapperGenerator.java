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
import com.google.gwt.core.ext.typeinfo.HasMetaData;
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
   * The presence of this annotation on the class indicates that getFoo, setFoo,
   * and isFoo should be treated as bean-style property accessors, rather than
   * imported functions.
   */
  public static final String BEAN_PROPERTIES = "gwt.beanProperties";

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
   * A class-level annotation that indicates that the generated JSWrapper should
   * not modify the underlying JSO. The read-only annotation implies
   * NO_IDENTITY.
   */
  public static final String READONLY = "gwt.readOnly";

  /**
   * This object disables maintaining a 1:1 identity mapping between a JSWrapper
   * and the backing JSO. The BACKREF field will not be added to the JSO.
   */
  public static final String NO_IDENTITY = "gwt.noIdentity";

  /**
   * The name of the field within the backing object that refers back to the
   * JSWrapper object.
   */
  public static final String BACKREF = "__gwtPeer";

  /**
   * The name of the backing object field.
   */
  protected static final String OBJ = "jsoPeer";

  /**
   * The name of the static field that contains the class's Extractor instance.
   */
  protected static final String EXTRACTOR = "__extractor";

  /**
   * Singleton instance of the FragmentGeneratorOracle for the system.
   */
  protected static final FragmentGeneratorOracle FRAGMENT_ORACLE = new FragmentGeneratorOracle();

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
   * Utility method to check for the presence of a particular metadata tag.
   */
  static boolean hasTag(HasMetaData item, String tagName) {
    String[] tags = item.getMetaDataTags();
    for (int i = 0; i < tags.length; i++) {
      if (tagName.equals(tags[i])) {
        return true;
      }
    }
    return false;
  }

  /**
   * Entry point into the Generator.
   */
  public final String generate(TreeLogger logger, GeneratorContext context,
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
      fragmentContext.objRef = "this.@" + f.getCreatedClassName() + "::" + OBJ;
      fragmentContext.simpleTypeName = generatedSimpleSourceName;
      fragmentContext.qualifiedTypeName = f.getCreatedClassName();
      fragmentContext.returnType = sourceType;
      fragmentContext.creatorFixups = new HashSet();
      fragmentContext.readOnly = hasTag(sourceType, READONLY);
      fragmentContext.maintainIdentity = !(fragmentContext.readOnly || hasTag(
          sourceType, NO_IDENTITY));
      fragmentContext.tasks = propertyAccessors.values();

      // Perform sanity checks on the extracted information
      validateType(propertyAccessors, fragmentContext);

      // Write all code that's not implementing methods
      writeBoilerplate(logger, fragmentContext);

      // Write the JSO initializer if required
      if (!fragmentContext.readOnly) {
        writeEmptyFieldInitializerMethod(logger, propertyAccessors,
            fragmentContext);
      }

      writeMethods(fragmentContext, propertyAccessors);
      writeFixups(logger, typeOracle, sw, fragmentContext.creatorFixups);

      // Write the generated code to disk
      sw.commit(logger);
    }

    // Return the name of the concrete class
    return f.getCreatedClassName();
  }

  /**
   * Popupulate propertyAccessors from an array of JMethods.
   */
  protected final Map extractMethods(TreeLogger logger,
      final TypeOracle typeOracle, final JClassType clazz)
      throws UnableToCompleteException {
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

      // Look for methods that are to be exported by the presence of
      // the gwt.exported annotation.
      if (shouldExport(typeOracle, m)) {
        Task task = getPropertyPair(propertyAccessors,
            m.getReadableDeclaration());
        task.exported = m;
        logger.log(TreeLogger.DEBUG, "Added as export", null);
        continue;
      }

      if (shouldBind(typeOracle, m)) {
        Task task = getPropertyPair(propertyAccessors,
            m.getReadableDeclaration());
        task.binding = m;
        logger.log(TreeLogger.DEBUG, "Added as binding", null);
        continue;
      }

      // Ignore concrete methods and those methods that are not declared in
      // a subtype of JSWrapper.
      if (!shouldImplement(typeOracle, m)) {
        logger.log(TreeLogger.DEBUG, "Ignoring method " + m.toString(), null);
        continue;
      }

      if (shouldConstruct(typeOracle, m)) {
        // getReadableDeclaration is used so that overloaded methods will
        // be stored with distinct keys.
        Task task = getPropertyPair(propertyAccessors,
            m.getReadableDeclaration());
        task.constructor = m;
        logger.log(TreeLogger.DEBUG, "Using constructor/global override", null);

        // Enable bypassing of name-determination logic with the presence of the
        // @gwt.imported annotation
      } else if (shouldImport(typeOracle, m)) {
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
   * Specifies the first parameter of imported methods to pass to the imported
   * JavaScript function.
   */
  protected int getImportOffset() {
    return 0;
  }

  /**
   * Specifies the base interface type so that it will be ignored by
   * {@link #extractMethods()}.
   */
  protected String getOperableClassName() {
    return JSWrapper.class.getName();
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
   * Extracts the parameter from a setter method that contains the value to
   * store into the backing object.
   */
  protected JParameter getSetterParameter(JMethod setter) {
    return setter.getParameters()[0];
  }

  /**
   * Determines if the generator should generate a binding for the method.
   */
  protected boolean shouldBind(TypeOracle typeOracle, JMethod method) {
    return false;
  }

  /**
   * Determines if a method should be treated as an invocation of an underlying
   * JavaScript constructor function.
   */
  protected boolean shouldConstruct(TypeOracle typeOracle, JMethod method) {
    boolean methodConstructorTag = hasTag(method, CONSTRUCTOR);
    boolean methodGlobalTag = hasTag(method, GLOBAL);

    return methodConstructorTag || methodGlobalTag;
  }

  /**
   * Determines if the generator should generate an export binding for the
   * method.
   */
  protected boolean shouldExport(TypeOracle typeOracle, JMethod method) {
    return hasTag(method, EXPORTED);
  }

  /**
   * Determines if the generator should implement a particular method. A method
   * will be implemented only if it is abstract and defined in a class derived
   * from JSWrapper
   */
  protected boolean shouldImplement(TypeOracle typeOracle, JMethod method) {
    JClassType enclosing = method.getEnclosingType();

    return method.isAbstract()
        && !enclosing.equals(typeOracle.findType(getOperableClassName()));
  }

  /**
   * Determines if the generator should generate an import binding for the
   * method.
   */
  protected boolean shouldImport(TypeOracle typeOracle, JMethod method) {
    JClassType enclosing = method.getEnclosingType();
    String methodName = method.getName();
    int arguments = method.getParameters().length;

    boolean hasImportTag = hasTag(method, IMPORTED);
    boolean methodHasBeanTag = hasTag(method, BEAN_PROPERTIES);
    boolean classHasBeanTag = hasTag(enclosing, BEAN_PROPERTIES);
    boolean isIs = (arguments == 0)
        && (methodName.startsWith("is"))
        && (JPrimitiveType.BOOLEAN.equals(method.getReturnType().isPrimitive()));
    boolean isGetter = (arguments == 0) && (methodName.startsWith("get"));
    boolean isSetter = (arguments == 1) && (methodName.startsWith("set"));
    boolean propertyAccessor = isIs || isGetter || isSetter;

    return !(methodHasBeanTag || (propertyAccessor && !hasImportTag && classHasBeanTag));
  }

  /**
   * Aggregate pre-write validation checks.
   */
  protected void validateType(Map propertyAccessors,
      FragmentGeneratorContext context) throws UnableToCompleteException {

    // Try to print as many errors as possible in a run before throwing the
    // exception
    boolean error = false;

    for (final Iterator i = propertyAccessors.entrySet().iterator(); i.hasNext();) {
      final Map.Entry entry = (Map.Entry) i.next();
      final Task pair = (Task) entry.getValue();

      error |= pair.validate(this, context);
    }

    if (error) {
      throw new UnableToCompleteException();
    }
  }

  /**
   * Writes common boilerplate code for all implementations.
   */
  protected void writeBoilerplate(TreeLogger logger,
      FragmentGeneratorContext context) throws UnableToCompleteException {

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

    // Determine the correct expression to use to initialize the object
    JClassType asClass = context.returnType.isClassOrInterface();
    String[][] constructorMeta = asClass.getMetaData(CONSTRUCTOR);
    String[][] globalMeta = asClass.getMetaData(GLOBAL);
    String constructor;
    if (globalMeta.length == 1 && globalMeta[0].length == 1) {
      constructor = globalMeta[0][0];
    } else if (constructorMeta.length == 1 && constructorMeta[0].length == 1) {
      constructor = "new " + constructorMeta[0][0] + "()";
    } else {
      constructor = "{}";
    }

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
    sw.println("public native JSWrapper setJavaScriptObject(JavaScriptObject obj) /*-{");
    sw.indent();

    if (context.maintainIdentity) {
      // Delete the backing object's reference to the current wrapper
      sw.print("if (");
      sw.print(context.objRef);
      sw.println(") {");
      sw.indent();
      sw.print("delete ");
      sw.print(context.objRef);
      sw.print(".");
      sw.print(BACKREF);
      sw.println(";");
      sw.outdent();
      sw.println("}");
    }

    // If the incoming JSO is null or undefined, reset the JSWrapper
    sw.println("if (!obj) {");
    sw.indent();
    sw.print("obj = this.@");
    sw.print(context.qualifiedTypeName);
    sw.println("::__nativeInit()();");
    sw.outdent();
    sw.println("}");

    if (context.maintainIdentity) {
      // Verify that the incoming object doesn't already have a wrapper object.
      // If there is a backreference, throw an exception.
      sw.print("if (obj.");
      sw.print(BACKREF);
      sw.println(") {");
      sw.indent();
      sw.println("@com.google.gwt.jsio.client.impl.JSONWrapperUtil::throwMultipleWrapperException()();");
      sw.outdent();
      sw.println("}");
    }

    // Capture the object in the wrapper
    sw.print(context.objRef);
    sw.println(" = obj;");

    if (context.maintainIdentity) {
      // Assign the backreference from the wrapped object to the wrapper
      sw.print(context.objRef);
      sw.print(".");
      sw.print(BACKREF);
      sw.println(" = this;");
    }

    if (!context.readOnly) {
      // Initialize any other fields if the JSWrapper is read-write
      sw.print("this.@");
      sw.print(context.qualifiedTypeName);
      sw.print("::__initializeEmptyFields(Lcom/google/gwt/core/client/JavaScriptObject;)(");
      sw.print(context.objRef);
      sw.println(");");
    }

    sw.println("return this;");
    sw.outdent();
    sw.println("}-*/;");

    // If the generated class will be used with a JSList, we need an Extractor
    // implementation. We'll create an implementation per generated
    // class to ensure that if the class is used with a JSList, only one
    // instance of the Extractor will ever exist.
    sw.println("public final Extractor getExtractor() {");
    sw.indent();
    sw.print("return ");
    sw.print(EXTRACTOR);
    sw.println(";");
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
        logger, typeOracle, returnType);

    sw.println("public native Object fromJS(JavaScriptObject obj) /*-{");
    sw.indent();
    sw.print("return ");
    fragmentGenerator.fromJS(subParams);
    sw.println(";");
    sw.outdent();
    sw.println("}-*/;");

    // Write the Extracor's toJS function and close the Extractor
    // implementation.
    sw.println("public native JavaScriptObject toJS(Object obj) /*-{");
    sw.indent();
    sw.print("return ");
    fragmentGenerator.toJS(subParams);
    sw.println(";");
    sw.outdent();
    sw.println("}-*/;");

    // Finish the class
    sw.outdent();
    sw.println("};");
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

    // The return type of the function we're importing.
    JType returnType = constructor.getReturnType();

    sw.print("var jsReturn = ");

    // If the imported method is acting as an invocation of a JavaScript
    // constructor, use the new Foo() syntax, otherwise treat is an an
    // invocation on a field on the underlying JSO.
    String[][] constructorMeta = constructor.getMetaData(CONSTRUCTOR);
    sw.print("new ");
    sw.print(constructorMeta[0][0]);

    // Write the invocation's parameter list
    sw.print("(");
    for (int i = getImportOffset(); i < parameters.length; i++) {
      // Create a sub-context to generate the wrap/unwrap logic
      JType subType = parameters[i].getType();
      FragmentGeneratorContext subParams = new FragmentGeneratorContext(context);
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
    sw.println(");");

    FragmentGeneratorContext subContext = new FragmentGeneratorContext(context);
    subContext.returnType = returnType;
    subContext.parameterName = "jsReturn";

    sw.println("return this.@com.google.gwt.jsio.client.JSWrapper::setJavaScriptObject(Lcom/google/gwt/core/client/JavaScriptObject;)(jsReturn);");
    sw.outdent();
    sw.println("}-*/;");
  }

  /**
   * Provides a method to encapsulate empty field initialization.
   */
  protected void writeEmptyFieldInitializerMethod(final TreeLogger logger,
      final Map propertyAccessors, final FragmentGeneratorContext context)
      throws UnableToCompleteException {
    SourceWriter sw = context.sw;

    sw.println("private native void __initializeEmptyFields(JavaScriptObject jso) /*-{");
    sw.indent();

    FragmentGeneratorContext subContext = new FragmentGeneratorContext(context);
    subContext.parameterName = "jso";
    writeMethodBindings(subContext);
    writeEmptyFieldInitializers(subContext);

    sw.outdent();
    sw.println("}-*/;");
  }

  /**
   * Ensures that no field referenced by generated logic will ever return an
   * undefined value. This allows every subsequent getFoo() call to simply
   * return the field value, without having to check it for an undefined value.
   */
  protected void writeEmptyFieldInitializers(FragmentGeneratorContext context)
      throws UnableToCompleteException {
    SourceWriter sw = context.sw;
    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG, "Writing field initializers",
        null);

    for (final Iterator i = context.tasks.iterator(); i.hasNext();) {
      final Task task = (Task) i.next();
      final String fieldName = task.getFieldName(logger);

      // Exported methods are always re-exported to ensure correct object
      // linkage.
      if (task.exported == null) {
        // If there is no getter, we don't need to worry about an empty
        // field initializer.
        if (task.getter == null) {
          continue;
        }

        final JType returnType = task.getter.getReturnType();

        FragmentGenerator fragmentGenerator = FRAGMENT_ORACLE.findFragmentGenerator(
            logger, context.typeOracle, returnType);

        sw.print("if (!");
        sw.print(context.parameterName);
        sw.print(".hasOwnProperty('");
        sw.print(fieldName);
        sw.println("')) {");
        sw.indent();

        sw.print(context.parameterName);
        sw.print(".");
        sw.print(fieldName);
        sw.print(" = ");
        sw.print(fragmentGenerator.defaultValue(context.typeOracle, returnType));
        sw.println(";");

        sw.outdent();
        sw.println("}");
      }
    }
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

  protected void writeGetter(FragmentGeneratorContext context, JMethod getter)
      throws UnableToCompleteException {

    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG,
        "Writing getter " + getter.getName(), null);
    TypeOracle typeOracle = context.typeOracle;
    SourceWriter sw = context.sw;

    final JType returnType = getter.getReturnType();

    FragmentGenerator fragmentGenerator = FRAGMENT_ORACLE.findFragmentGenerator(
        logger, typeOracle, context.returnType);

    sw.print("public native ");
    sw.print(returnType.getQualifiedSourceName());
    sw.print(" ");
    sw.print(getter.getName());
    sw.print("(");

    // This is only important when working with the flyweight subclass
    JParameter[] params = getter.getParameters();
    for (int i = 0; i < params.length; i++) {
      sw.print(params[i].getType().getQualifiedSourceName());
      sw.print(" ");
      sw.print(params[i].getName());

      if (i < params.length - 1) {
        sw.print(", ");
      }
    }
    sw.print(")");
    sw.println(" /*-{");
    sw.indent();

    sw.print("return ");
    fragmentGenerator.fromJS(context);
    sw.println(";");

    sw.outdent();
    sw.println("}-*/;");
  }

  protected void writeImported(FragmentGeneratorContext context,
      JMethod imported) throws UnableToCompleteException {

    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG,
        "Writing import " + imported.getName(), null);
    SourceWriter sw = context.sw;

    // Simplifies the rest of writeImported
    JParameter[] parameters = imported.getParameters();
    if (parameters == null) {
      parameters = new JParameter[0];
    }

    // Method declaration
    sw.print("public native ");
    sw.print(imported.getReturnType().getQualifiedSourceName());
    sw.print(" ");
    sw.print(imported.getName());
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

    // The return type of the function we're importing.
    final JType returnType = imported.getReturnType();

    // Don't bother recording a return value for void invocations.
    if (!JPrimitiveType.VOID.equals(returnType.isPrimitive())) {
      sw.print("var jsReturn = ");
    }

    sw.print(context.objRef);
    sw.print(".");
    sw.print(context.fieldName);

    // Write the invocation's parameter list
    sw.print("(");
    for (int i = getImportOffset(); i < parameters.length; i++) {
      // Create a sub-context to generate the wrap/unwrap logic
      JType subType = parameters[i].getType();
      FragmentGeneratorContext subParams = new FragmentGeneratorContext(context);
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
    sw.println(");");

    // Wrap the return type in the correct Java type. Void returns are ignored
    if (!JPrimitiveType.VOID.equals(returnType.isPrimitive())) {
      FragmentGeneratorContext subContext = new FragmentGeneratorContext(
          context);
      subContext.returnType = returnType;
      subContext.parameterName = "jsReturn";

      FragmentGenerator fragmentGenerator = FRAGMENT_ORACLE.findFragmentGenerator(
          logger, context.typeOracle, returnType);

      sw.print("return ");

      fragmentGenerator.fromJS(subContext);
      sw.println(";");
    }

    sw.outdent();
    sw.println("}-*/;");
  }

  protected void writeMethodBindings(FragmentGeneratorContext context)
      throws UnableToCompleteException {
    SourceWriter sw = context.sw;
    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG,
        "Writing method bindings initializers", null);

    for (final Iterator i = context.tasks.iterator(); i.hasNext();) {
      final Task task = (Task) i.next();
      final String fieldName = task.getFieldName(logger);

      if (task.exported != null) {
        sw.print(context.parameterName);
        sw.print(".");
        sw.print(fieldName);
        sw.print(" = ");

        FragmentGeneratorContext subContext = new FragmentGeneratorContext(
            context);
        subContext.parameterName = "this." + BACKREF;

        JSFunctionFragmentGenerator.writeFunctionForMethod(subContext,
            task.exported);
        sw.println(";");
      }
    }
  }

  /**
   * Write the field, getter, and setter for the properties we know about. Also
   * write BusObjectImpl methods for Map-style access.
   */
  protected void writeMethods(FragmentGeneratorContext context,
      Map propertyAccessors) throws UnableToCompleteException {
    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG,
        "Writing methods", null);

    for (final Iterator i = propertyAccessors.entrySet().iterator(); i.hasNext();) {

      final Map.Entry entry = (Map.Entry) i.next();
      final Task task = (Task) entry.getValue();
      final String fieldName = task.getFieldName(logger);

      context.fieldName = fieldName;

      writeSingleTask(context, task);
    }
  }

  protected void writeSetter(FragmentGeneratorContext context, JMethod setter)
      throws UnableToCompleteException {

    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG,
        "Writing setter " + setter.getName(), null);
    TypeOracle typeOracle = context.typeOracle;
    SourceWriter sw = context.sw;

    JType parameterType = context.returnType;

    FragmentGenerator fragmentGenerator = FRAGMENT_ORACLE.findFragmentGenerator(
        logger, typeOracle, context.returnType);
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
    // This is only important when working with the flyweight subclass
    JParameter[] params = setter.getParameters();
    for (int i = 0; i < params.length; i++) {
      sw.print(params[i].getType().getQualifiedSourceName());
      sw.print(" ");
      sw.print(params[i].getName());

      if (i < params.length - 1) {
        sw.print(", ");
      }
    }

    sw.println(") /*-{");
    sw.indent();
    sw.print(context.objRef);
    sw.print(".");
    sw.print(context.fieldName);
    sw.print(" = ");
    fragmentGenerator.toJS(context);
    sw.println(";");
    sw.outdent();
    sw.println("}-*/;");
  }

  protected void writeSingleTask(FragmentGeneratorContext context, Task task)
      throws UnableToCompleteException {
    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG,
        "Writing Task " + task.getFieldName(context.parentLogger), null);

    context = new FragmentGeneratorContext(context);
    context.parentLogger = logger;

    logger.log(TreeLogger.DEBUG, "Implementing task " + context.fieldName, null);

    if (task.getter != null) {
      context.returnType = task.getter.getReturnType();
      context.parameterName = context.objRef + "." + context.fieldName;
      writeGetter(context, task.getter);
    }

    if (task.imported != null) {
      context.returnType = task.imported.getReturnType();
      writeImported(context, task.imported);
    }

    if (task.setter != null) {
      if (context.readOnly) {
        logger.log(TreeLogger.ERROR,
            "Unable to write property setter on read-only wrapper.", null);
        throw new UnableToCompleteException();
      }

      JParameter parameter = getSetterParameter(task.setter);
      context.returnType = parameter.getType();
      // What the user called the parameter
      context.parameterName = parameter.getName();
      writeSetter(context, task.setter);
    }

    if (task.constructor != null) {
      context.returnType = task.constructor.getReturnType();
      context.parameterName = "this.";
      context.objRef = "this";
      writeConstructor(context, task.constructor);
    }
  }
}