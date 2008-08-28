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
package com.google.gwt.inject.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.inject.client.Injector;

/**
 * Generator for implementations of {@link Injector}.
 */
public class InjectorGenerator extends Generator {
  public String generate(TreeLogger logger, GeneratorContext ctx, String requestedClass)
      throws UnableToCompleteException {
    TypeOracle oracle = ctx.getTypeOracle();

    JClassType injector = oracle.findType(requestedClass);
    checkInjectorClass(logger, requestedClass, oracle, injector);

    return new InjectorGeneratorImpl(logger, ctx, injector).generate();
  }

  private void checkInjectorClass(TreeLogger logger, String requestedClass,
      TypeOracle oracle, JClassType injector) throws UnableToCompleteException {
    if (injector == null) {
      logger.log(TreeLogger.ERROR, "Unable to find metadata for type '"
          + requestedClass + "'", null);
      throw new UnableToCompleteException();
    }

    if (injector.isInterface() == null) {
      logger.log(TreeLogger.ERROR, injector.getQualifiedSourceName()
          + " is not an interface", null);
      throw new UnableToCompleteException();
    }

    if (!injector.isAssignableTo(oracle.findType(Injector.class.getName()))) {
      logger.log(TreeLogger.ERROR, injector.getQualifiedSourceName()
          + " is not a subtype of " + Injector.class.getName());
    }
  }
}