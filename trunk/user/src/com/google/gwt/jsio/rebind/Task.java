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
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;

import java.lang.reflect.Field;

/**
 * Defines one or more methods to be implemented by JSWrapperGenerator.
 */
class Task {
  JMethod getter;
  JMethod setter;
  JMethod binding;
  JMethod imported;
  JMethod exported;
  JMethod constructor;

  /**
   * Determines the field name to be used by the methods associated with the
   * Task.
   * 
   * @param logger the TreeLogger in use by the enclosing content
   * @return the JSO property/Wrapper field name to use when generating the
   *         methods in the Task
   * @throws UnableToCompleteException
   */
  public String getFieldName(TreeLogger logger)
      throws UnableToCompleteException {
    logger =
        logger.branch(TreeLogger.DEBUG, "Determining field name for Task", null);
    if (getter != null) {
      return extractFieldName(logger, getter, false);
    } else if (setter != null) {
      return extractFieldName(logger, setter, false);
    } else if (binding != null) {
      return extractFieldName(logger, binding, true);
    } else if (exported != null) {
      return extractFieldName(logger, exported, true);
    } else if (imported != null) {
      return extractFieldName(logger, imported, true);
    } else if (constructor != null) {
      return extractFieldName(logger, constructor, true);
    } else {
      logger.log(TreeLogger.ERROR, "Unable to determine field name", null);
      throw new UnableToCompleteException();
    }
  }

  /**
   * Validation check to ensure that at least one method is defined within the
   * Task.
   * 
   * @return <code>true</code> iff there is at least one method defined for
   *         the Task.
   */
  public boolean hasMethods() {
    return (getter != null) || (setter != null) || (imported != null)
        || (exported != null) || (constructor != null) || (binding != null);
  }

  /**
   * Determine the correct field name to use for a method.
   */
  protected String extractFieldName(TreeLogger logger, JMethod m,
      boolean imported) throws UnableToCompleteException {
    logger = logger.branch(TreeLogger.DEBUG, "Extracting field name", null);

    String[][] meta = m.getMetaData(JSWrapperGenerator.FIELD_NAME);

    if (meta == null || meta.length == 0) {
      // If the method is imported and there's no overriding annotation,
      // just return the methods original name. This ensures that native
      // JS methods that look like bean getter/setters don't get munged.
      if (imported) {
        return m.getName();
      }

      // If no gwt.fieldName is specified, see if there's a naming policy
      // defined on the enclosing class.
      JClassType enclosing = m.getEnclosingType();
      String[][] policyMeta =
          enclosing.getMetaData(JSWrapperGenerator.NAME_POLICY);
      NamePolicy policy;

      // If there is no namePolicy or it's not of the desired form, default
      // to the JavaBean-style naming policy.
      if (policyMeta == null || policyMeta.length != 1
          || policyMeta[0].length != 1) {
        policy = NamePolicy.BEAN;
        logger.log(TreeLogger.DEBUG, "No useful policy metadata for class",
            null);

      } else {
        // Use the provided name to access fields within NamePolicy
        String policyName = policyMeta[0][0];
        try {
          Field f = NamePolicy.class.getDeclaredField(policyName.toUpperCase());
          policy = (NamePolicy)f.get(null);

        } catch (IllegalAccessException e) {
          logger.log(TreeLogger.ERROR, "Bad gwt.namePolicy " + policyName, e);
          throw new UnableToCompleteException();

        } catch (NoSuchFieldException e) {
          // This means that the value specified is not a field, but likely
          // a class name. Try instantiating one and seeing if it's a
          // subclass of NamePolicy.
          try {
            Class clazz = Class.forName(policyName);
            if (NamePolicy.class.isAssignableFrom(clazz)) {
              policy = (NamePolicy)clazz.newInstance();
            } else {
              logger.log(TreeLogger.ERROR,
                  "@gwt.namePolicy is not an implementation of NamePolicy",
                  null);
              throw new UnableToCompleteException();
            }
          } catch (ClassNotFoundException ee) {
            logger.log(TreeLogger.ERROR, "Bad gwt.namePolicy " + policyName, ee);
            throw new UnableToCompleteException();
          } catch (IllegalAccessException ee) {
            logger.log(TreeLogger.ERROR, "Bad gwt.namePolicy " + policyName, ee);
            throw new UnableToCompleteException();
          } catch (InstantiationException ee) {
            logger.log(TreeLogger.ERROR, "Bad gwt.namePolicy " + policyName, ee);
            throw new UnableToCompleteException();
          }
        }
      }

      // Execute the conversion
      String propertyName = JSWrapperGenerator.getPropertyNameFromMethod(m);
      return policy.convert(propertyName);

    } else if (meta[0].length == 1) {
      // Use the field name specified on the method
      logger.log(TreeLogger.DEBUG, "Overriding field name based on annotation",
          null);
      return meta[0][0];

    } else {
      // There were multiple field name values specified, so bail.
      logger.log(TreeLogger.ERROR, "Unable to evaluate field name annotation",
          null);
      throw new UnableToCompleteException();
    }
  }
}
