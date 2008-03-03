/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.jsio;

import com.google.gwt.jsio.client.JSExporterTest;
import com.google.gwt.jsio.client.JSFlyweightWrapperTest;
import com.google.gwt.jsio.client.JSIOBeanReturnUndefined;
import com.google.gwt.jsio.client.JSONInvokerTest;
import com.google.gwt.jsio.client.JSONWrapperTest;
import com.google.gwt.jsio.client.JsoOverrideTest;
import com.google.gwt.jsio.rebind.NamePolicyTest;
import com.google.gwt.junit.tools.GWTTestSuite;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * TestSuite for the JSIO API.
 */
public class JSIOTestSuite extends TestCase {
  public static Test suite() {
    GWTTestSuite suite = new GWTTestSuite("Test for the JSIO API");

    suite.addTestSuite(JSExporterTest.class);
    suite.addTestSuite(JSFlyweightWrapperTest.class);
    suite.addTestSuite(JSIOBeanReturnUndefined.class);
    suite.addTestSuite(JSONInvokerTest.class);
    suite.addTestSuite(JSONWrapperTest.class);
    suite.addTestSuite(JsoOverrideTest.class);
    suite.addTestSuite(NamePolicyTest.class);

    return suite;
  }
}
