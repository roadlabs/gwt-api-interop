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
JSIOBeanReturnUndefined = {}

JSIOBeanReturnUndefined.EmptyObject = function () {}

JSIOBeanReturnUndefined.MemberUndefined = function () {
 this.valueInt = undefined;
 this.valueString = undefined;
 this.valueJSList = undefined;
 this.valueJSO = undefined;
}

JSIOBeanReturnUndefined.ReturnUndefined = function () {
 this.valueBoolean = function () { return undefined; };
 this.valueInt = function () { return undefined; };
 this.valueString = function () { return undefined; };
 this.valueJSList = function () { return undefined; };
 this.valueJSO = function () { return undefined; };
}

JSIOBeanReturnUndefined.MemberNull = function () {
 this.valueShort = null;
 this.valueInt = null;
 this.valueLong = null;
 this.valueFloat = null;
 this.valueDouble = null;
 this.valueByte = null;
 this.valueChar = null;
 this.valueString = null;
 this.valueJSO = null;
 this.valueJSList = null;
}

JSIOBeanReturnUndefined.MemberValid = function () {
 this.valueInt = 1;
 this.valueIntZero = 0;
 this.valueString = "Hello World!";
 this.valueEmptyString = "";
 this.valueJSO = new Object();
}

JSIOBeanReturnUndefined.MemberBoolean = function () {
 this.valueTrue = true;
 this.valueFalse = false;
 this.valueZero = 0;
 this.valueOne = 1;
 this.valueNull = null;
 this.valueUndefined = undefined;
}   
})();