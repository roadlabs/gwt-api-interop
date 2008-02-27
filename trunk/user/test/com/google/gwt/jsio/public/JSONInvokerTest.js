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
  var Hello = function(param1, param2) {
    this.hello = 42;
    this.param1 = param1;
    this.param2 = param2;
    this.numbers = [1,2,3,4,5];
  }
  
  Hello.prototype.add = function(sum, sum2) {
    return this.hello += sum + (sum2 || 0);
  }
  
  Hello.prototype.sub = function(sum) {
    return this.hello -= sum;
  }
  
  Hello.prototype.increment = function() {
    this.hello++;
  }
  
  Hello.prototype.returnUndefined = function() {
    return undefined;
  }
  
  Hello.prototype.setHello = function(a) {
    this.hello = a + 10;
  }
  
  Hello.prototype.testCallback = function(param1, param2, callback) {
    return 5 * callback(param1, param2);
  }
  
  Hello.prototype.identityEquals = function(o1, o2) {
    return o1 === o2;
  }
  
  Hello.prototype.reverseNumbers = function(arr) {
    var toReturn = [];
    for (var i = 0; i < arr.length; i++) {
      toReturn[i] = arr[arr.length - i - 1];
    }
    return toReturn;
  }
  
  Hello.prototype.passthrough = function(o) {
    return o;
  }
  
  JSONInvokerTest = {};
  JSONInvokerTest.Hello = Hello;
  JSONInvokerTest.SingletonHello = new Hello("Singleton", 314159);
})();