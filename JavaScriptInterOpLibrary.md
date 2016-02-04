# Introduction #

This document describes the JavaScript Interoperability( JSIO) library for the Google Web Toolkit (GWT).  These classes are for importing existing JavaScript APIs and exporting Java functions to be used JavaScript code.

Using this library, the GWT compiler can automatically generate:

  * Linkage to functions defined within a [com.google.gwt.core.client.JavaScriptObject](http://google-web-toolkit.googlecode.com/svn/javadoc/1.4/com/google/gwt/core/client/JavaScriptObject.html)
  * Exports of Java functions to be made available to JavaScript callers
  * Accessors for bean-style properties

Developers extend `JSWrapper` or `JSFlyweightWrapper` to create interface definitions to access the JavaScript object.  To use the interface, the GWT deferred binding mechanism `GWT.create()` is invoked to return an instance of the interface created through automatically generated code.

# JSWrapper interface #

Any method defined in an interface that extends [com.google.gwt.jsio.client.JSWrapper](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/JSWrapper.html) or an abstract method in a class that implements  [com.google.gwt.jsio.client.JSWrapper](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/JSWrapper.html) will be acted upon by the compiler. The default behavior is to invoke an identically-named method on the underlying [com.google.gwt.core.client.JavaScriptObject](http://google-web-toolkit.googlecode.com/svn/javadoc/1.4/com/google/gwt/core/client/JavaScriptObject.html). This behavior may be altered by the presence of various annotations on the class and its methods.

The parameter and return types supported by JSWrapper are:

  * primitive
  * boxed primitive
  * java.lang.String
  * [com.google.gwt.jsio.client.JSFunction](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/JSFunction.html)
  * [com.google.gwt.jsio.client.JSWrapper](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/JSWrapper.html)
  * [com.google.gwt.jsio.client.JSList](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/JSList.html) having a compatible component type specified with the gwt.typeArgs annotation
  * [com.google.gwt.core.client.JavaScriptObject](http://google-web-toolkit.googlecode.com/svn/javadoc/1.4/com/google/gwt/core/client/JavaScriptObject.html) and subtypes
  * [com.google.gwt.jsio.client.JSOpaque](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/JSOpaque.html) as a method parameter only.

In the general case, there may be at most one [com.google.gwt.jsio.client.JSWrapper](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/JSWrapper.html) associated with a [com.google.gwt.core.client.JavaScriptObject JavaScriptObject](http://google-web-toolkit.googlecode.com/svn/javadoc/1.4/com/google/gwt/core/client/JavaScriptObject.html). This allows the JSWrapper to maintain an object identity equivalent to that of the underlying JavaScriptObject. This is done by adding an additional property, `__gwtObject`, to the JavaScriptObject.

## Annotations ##

The behavior of the generated classes may be altered by the presence of various annotations.  In GWT 1.4 and earlier, this was signified by using a javadoc metadata comment as follows:

```
   /**
    * Deprecated - GWT 1.4 style JSIO annotation
    * @gwt.constructor("Object")
    */
   JavaScriptObject foo();
```

In GWT 1.5, the Java 5 annotations support is used.  This allows for the compiler to perform lexical and syntax checks on the annotations.

```
   @Constructor("Object")
   JavaScriptObject foo();
```


|Annotation 	        |Location 	|Description |
|:-------------------|:---------|:-----------|
| `@BeanProperties`, `gwt.beanProperties`    |Class, Method | Indicates that methods that look like bean-style property setters (methods that begin with the prefix `get`, `set`, or `is`) should be generated so as to read and write object properties rather than import functions. This is most useful with JSON-style objects. The setting may by applied on a per-method basis in an imported class and may by overridden on a per-method basis by `@Imported`. If the backing object does not contain data for a property accessor, null, 0, ' ', false, or an empty [com.google.gwt.jsio.client.JSList](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/JSList.html) will be returned. |
| `@Constructor(String)`, `gwt.constructor` 	|Class, Method  | The annotation `@Constructor` may be applied to a class to specify a JavaScript function to execute when constructing a JSWrapper to use as the initial backing object. A JavaScript Date object could be created by using the value $wnd.Date. If the `@Constructor` annotation is applied to a method within a JSWrapper and the method invoked, the parameters of the method will be passed to the named global function and the resulting JavaScript object will be used as the backing object. |
| `@Exported`, `gwt.exported` 	|Method 	  |Individual Java functions may be exported to JavaScript callers by declaring a `@Exported` annotation on a concrete Java method within a JSWrapper. The Java method will be bound to a property on the backing object per the class' NamePolicy or a `@FieldName` annotation on the method.  _GWT 1.4 only:_  When applied at the class level to a [com.google.gwt.jsio.client.JSFunction](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/JSFunction.html), it specifies which of the methods declared within to type to export as a JavaScript Function object.|
| `@FieldName(String)`, `gwt.fieldName` 	|Method 	  |When implementing a bean property accessor, the default NamePolicy will be used unless a `@FieldName` annotation appears on the property's getter or setter. This is also used with imported and exported functions to specify the object property to attach to.|
| `@Global(String)`,`gwt.global` 	|Class, Method 	|The annotation `@Global` is similar to `@Constructor`. It may be applied at the class level or method level and the value is interpreted as a globally-accessible object name, rather than a function. |
| `@Imported`, `gwt.imported` 	|Method 	  |  By default, any method in an interface is implicitly imported for use in Java.  Using `@Imported` is a way to override methods within classes annotated with `@BeanProperties`.  This will explicitly import a JavaScript method instead of generating a getter or setter method. |
| `@NamePolicy(String)`,`gwt.namePolicy` | Class    | The valid values for the namePolicy are the field names on the `com.google.gwt.jsio.rebind.NamePolicy` class, or the name of a class that implements NamePolicy.|
| `@NoIdentity`, `gwt.noIdentity` 	|Class 	   |This annotation suppresses the addition of the gwtObject property on the underlying JavaScriptObject. The object identity of the JSWrapper will no longer maintain a 1:1 correspondence with the underlying JavaScriptObject. Additionally,  [com.google.gwt.jsio.client.JSWrapper.setJavaScriptObject()](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/JSWrapper.html#setJavaScriptObject(JavaScriptObject)) will no longer throw [com.google.gwt.jsio.client.MultipleWrapperException](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/MultipleWrapperException.html).  |
| `@ReadOnly`, `gwt.readOnly`	|Class 	   |This prevents the generated JSWrapper implementation from containing any code that will modify the underlying JavaScriptObject. This implies `@NoIdentity`. Invoking a bean-style getter when the underlying JavaScriptObject does not contain a value for the property will result in undefined behavior. |


## JSWrapper Examples ##

See [JSIO JSWrapper Examples](JSIOJSWrapperExamples.md).


# JSFlyweightWrapper interface #

The [com.google.gwt.jsio.client.JSFlyweightWrapper](http://gwt-api-interop.googlecode.com/svn/javadoc/com/google/gwt/jsio/client/JSFlyweightWrapper.html) generates code so that multiple objects can share the same functions.  This cuts down on memory usage.

The tags are pretty much the same as JSWrapper, but you'll want to look at anything with a jsoPeer instance field.

## New Annotations ##

| Annotation   | Location | Description |
|:-------------|:---------|:------------|
| `@Binding`, `gwt.binding` | Method   | Used in conjuction with `@Exported`. Indicates that a flyweight-style method should be used to bind exported functions from a Java type into a JavaScriptObject.  The method should have the signature void bind (JavaScriptObject jsoPeer, 

&lt;classname&gt;

 obj);.  This creates a property named `__gwtPeer` on the JavaScriptObject that references the type. Passing a JavaScriptObject to a bind method will also perform some assertions on the JSO to make sure it has the same duck type as the one specified by the Java interface (by checking the existence of methods in the object for those being imported.) Turn on assertions on the commandline with `-ea` to get the extra checking in hosted mode.  Multiple methods may be annoated with `@Binding`, but a single JSO instance should be bound to no more than one Java object.  |


## New Methods: ##

| Method Name  | Description |
|:-------------|:------------|
| `createPeer()` | Used to initialize the generated instance with a JavaScript object. |


## Example use of JSFlyweightWrapper ##

See [JSIO JSFlyweightWrapper Examples](JSIOJSFlyweightWrapperExamples.md).

# Class Documentation #

[Javadoc Index](http://gwt-api-interop.googlecode.com/svn/javadoc/index.html)