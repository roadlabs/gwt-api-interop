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
  function MathLib() {};
  function MathLibFW() {};
  
  function makeLib() {
    return {
      add:function(a, b) {return a + b;},
      // subtract and multiply are intentionally broken
      subtract:function(a, b) {return 0;},
      multiply:function(a, b) {return 0;}
    };
  }
  
  MathLib.prototype = makeLib();
  MathLibFW.prototype = makeLib();
  
  JsoOverrideTest = {};
  JsoOverrideTest.MathLibConstructor = MathLib;
  JsoOverrideTest.MathLibFWConstructor = MathLibFW;
  JsoOverrideTest.MathLibInstance = new MathLib();
  JsoOverrideTest.MathLibFWInstance = new MathLibFW();
})();