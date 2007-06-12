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
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Encapsulates accessors for JavaScriptObjects
 */
class JavaScriptObjectFragmentGenerator extends FragmentGenerator {
  boolean accepts(TypeOracle oracle, JType type) {
    JClassType asClass = type.isClassOrInterface();
    if (asClass == null) {
      return false;
    } else {
      return isAssignable(oracle, asClass, JavaScriptObject.class);
    }
  }

  String defaultValue(TypeOracle oracle, JType type) {
    return "null";
  }

  void fromJS(FragmentGeneratorContext context)
      throws UnableToCompleteException {
    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG,
        "Building jso value getter statement", null);
    SourceWriter sw = context.sw;

    sw.print(context.parameterName);
  }

  void toJS(FragmentGeneratorContext context) throws UnableToCompleteException {
    TreeLogger logger = context.parentLogger.branch(TreeLogger.DEBUG,
        "Building jso value setter statement", null);
    SourceWriter sw = context.sw;

    sw.print(context.parameterName);
  }

  void writeExtractorJSNIReference(FragmentGeneratorContext context) {
    SourceWriter sw = context.sw;
    sw.print("@com.google.gwt.jsio.client.impl.JSONWrapperUtil::JSO_EXTRACTOR");
  }
}
