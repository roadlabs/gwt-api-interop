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

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.SourceWriter;

import java.util.Set;

/**
 * Defines context for the fragment generation code.
 */
class FragmentGeneratorContext {
  /**
   * The enclosing log context.
   */
  TreeLogger parentLogger;

  /**
   * A FragmentGeneratorOracle to find other FragmentGenerators with.
   */
  FragmentGeneratorOracle fragmentGeneratorOracle;

  /**
   * The type system in use.
   */
  TypeOracle typeOracle;

  /**
   * The desired output location for generated code.
   */
  SourceWriter sw;

  /**
   * The name of the field within the backing object to use.
   */
  String fieldName;

  /**
   * The type of the value being accessed.
   */
  JType returnType;

  /**
   * The name of the parameter in the setter.
   */
  String parameterName;

  /**
   * A JSNI reference to the backing JSO field.
   */
  String objRef;

  /**
   * The unqualified type name of the concrete class being implemented
   */
  String simpleTypeName;

  /**
   * The qualified type name of the concrete class being implemented
   */
  String qualifiedTypeName;

  /**
   * Implementations of FragmentGenerator can add wrapper JClassType objects to
   * this Set to indicate that they require a creator method for the given type.
   */
  Set/* <JType> */creatorFixups;

  /**
   * Constructor.
   */
  public FragmentGeneratorContext() {
  }

  /**
   * Copy constructor.
   */
  public FragmentGeneratorContext(FragmentGeneratorContext copyFrom) {
    parentLogger = copyFrom.parentLogger;
    fragmentGeneratorOracle = copyFrom.fragmentGeneratorOracle;
    typeOracle = copyFrom.typeOracle;
    sw = copyFrom.sw;
    returnType = copyFrom.returnType;
    parameterName = copyFrom.parameterName;
    fieldName = copyFrom.fieldName;
    objRef = copyFrom.objRef;
    qualifiedTypeName = copyFrom.qualifiedTypeName;
    creatorFixups = copyFrom.creatorFixups;
  }
}