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
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Returns FragmentGenerators.
 */
class FragmentGeneratorOracle {
  /**
   * The List will always be checked in-order.
   */
  private List fragmentGenerators = new ArrayList();

  /**
   * Constructor.
   */
  public FragmentGeneratorOracle() {
    fragmentGenerators.add(new BoxedTypeFragmentGenerator());
    fragmentGenerators.add(new JavaScriptObjectFragmentGenerator());
    fragmentGenerators.add(new JSFunctionFragmentGenerator());
    fragmentGenerators.add(new JSListFragmentGenerator());
    fragmentGenerators.add(new PrimitiveFragmentGenerator());
    fragmentGenerators.add(new StringFragmentGenerator());
    fragmentGenerators.add(new JSOpaqueFragmentGenerator());
    fragmentGenerators.add(new JSWrapperFragmentGenerator());
    fragmentGenerators.add(new PeeringFragmentGenerator());

    // We don't actually support some types, but we can at least provide useful
    // error messages.
    fragmentGenerators.add(new ArrayFragmentGenerator());
    fragmentGenerators.add(new JSFlyweightFragmentGenerator());
  }

  /**
   * Finds a FragmentGenerator that can operate on a given type.
   * 
   * @param logger the context's TreeLogger
   * @param oracle the type system in use
   * @param type the type to generate fragments for
   * @return <code>null</code> if there is no FragmentGenerator for the
   *         specified type
   * @throws UnableToCompleteException if there is no registered generator
   */
  public FragmentGenerator findFragmentGenerator(TreeLogger logger,
      TypeOracle oracle, JType type) throws UnableToCompleteException {

    for (Iterator i = fragmentGenerators.iterator(); i.hasNext();) {
      FragmentGenerator g = (FragmentGenerator)i.next();

      if (g.accepts(oracle, type)) {
        return g;
      }
    }

    logger.branch(TreeLogger.ERROR, "The type " + type.getQualifiedSourceName()
        + " cannot be handled by JSWrapper.", null);
    throw new UnableToCompleteException();
  }
}
