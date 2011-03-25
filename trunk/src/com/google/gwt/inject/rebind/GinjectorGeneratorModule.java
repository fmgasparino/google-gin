/**
 * Copyright (C) 2008 Wideplay Interactive.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gwt.inject.rebind;

import java.lang.reflect.Method;
import java.util.Set;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.inject.client.GinModule;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.inject.rebind.BindingResolver.BindingResolverFactory;
import com.google.gwt.inject.rebind.binding.BindingIndex;
import com.google.gwt.inject.rebind.binding.Injectable;
import com.google.gwt.inject.rebind.reflect.FieldLiteral;
import com.google.gwt.inject.rebind.reflect.MethodLiteral;
import com.google.gwt.inject.rebind.util.GuiceUtil;
import com.google.gwt.inject.rebind.util.MemberCollector;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Guice module used in the implementation of {@link GinjectorGenerator}.
 * <p>
 * IMPLEMENTATION NOTE: thread safety concerns depend on how we create the Injector in
 * the {@link GinjectorGenerator} class, so make sure you consult that class before
 * adding bindings.
 */
class GinjectorGeneratorModule extends AbstractModule {
  private final TreeLogger logger;
  private final GeneratorContext ctx;
  private final Class<? extends Ginjector> ginjectorInterface;
  private final Set<Class<? extends GinModule>> configurationModules;

  public GinjectorGeneratorModule(TreeLogger logger, GeneratorContext ctx,
      Class<? extends Ginjector> ginjectorInterface, 
      Set<Class<? extends GinModule>> configurationModules) {
    this.logger = logger;
    this.ctx = ctx;
    this.ginjectorInterface = ginjectorInterface;
    this.configurationModules = configurationModules;
  }

  @Override
  protected void configure() {
    bind(TreeLogger.class).toInstance(logger);
    bind(GeneratorContext.class).toInstance(ctx);
    bind(new TypeLiteral<Class<? extends Ginjector>>(){})
        .annotatedWith(GinjectorInterfaceType.class)
        .toInstance(ginjectorInterface);
    bind(GinjectorBindings.class).annotatedWith(RootBindings.class)
        .to(GinjectorBindings.class).in(Singleton.class);
    bind(BindingIndex.class)
        .to(Key.get(GinjectorBindings.class, RootBindings.class))
        .in(Singleton.class);
    install(new FactoryModuleBuilder()
        .build(GuiceElementVisitor.GuiceElementVisitorFactory.class));
    install(new FactoryModuleBuilder()
        .build(BindingResolverFactory.class));
    bind(new TypeLiteral<Set<Class<? extends GinModule>>>(){})
        .annotatedWith(ConfigurationModuleTypes.class)
        .toInstance(configurationModules);
  }

  @Provides
  @Injectable
  @Singleton
  MemberCollector provideInjectablesCollector(MemberCollector collector) {
    collector.setMethodFilter(
        new MemberCollector.MethodFilter() {
          public boolean accept(MethodLiteral<?, Method> method) {
            // TODO(schmitt): Do injectable methods require at least one parameter?
            return GuiceUtil.hasInject(method) && !method.isStatic();
          }
        });

    collector.setFieldFilter(
        new MemberCollector.FieldFilter() {
          public boolean accept(FieldLiteral<?> field) {
            return (GuiceUtil.hasInject(field)) && !field.isStatic();
          }
        });

    return collector;
  }
}