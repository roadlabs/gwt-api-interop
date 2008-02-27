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

(function() {
  var MathLib = function() {};
  var MathLibFW = function() {};
  
  MathLibFW.prototype.add = MathLib.prototype.add = function(a, b) {
    return a + b;
  }
  
  // This function is broken, we'll override it in Java with a static method
  MathLibFW.prototype.subtract = MathLib.prototype.subtract = function(a, b) {
    return 0;
  }
  
  // This function is broken, we'll override it with an instance method
  MathLibFW.prototype.multiply = MathLib.prototype.multiply = function(a, b) {
    return 0;
  }
  
  JsoOverrideTest = {};
  JsoOverrideTest.MathLibConstructor = MathLib;
  JsoOverrideTest.MathLibFWConstructor = MathLibFW;
  JsoOverrideTest.MathLib = new MathLib();
  JsoOverrideTest.MathLibFW = new MathLibFW();
})();