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
  var C = function(str, i, tree) {
    this.str = str;
    this.i = i;
    this.tree = tree;
  }

  C.prototype.getInt = function() {
    return this.i;
  }

  C.prototype.getString = function() {
    return this.str;
  }

  C.prototype.getTree = function() {
    return this.tree;
  }
  
  // One class in JS can handle an array of any type.  Not so for Java.
  var ArrayTest = function () {
    this.stringArrayValue = new Array();
  }

  ArrayTest.prototype.setArray = function(arr) {
    this.stringArrayValue = arr;
  }
  
  ArrayTest.prototype.getArray = function() {
    return this.stringArrayValue;
  }
  
  ArrayTest.prototype.getIndex = function(i) {
    return this.stringArrayValue[i];
  }
  
  ArrayTest.prototype.getLength = function() {
    return this.stringArrayValue.length;
  }
  
  ArrayTest.prototype.clear = function() {
    this.stringArrayValue = new Array();
  }
  

  JSFlyweightWrapperTest = {};
  JSFlyweightWrapperTest.ConstructedObject = C;
  JSFlyweightWrapperTest.ArrayTest = ArrayTest;
})();