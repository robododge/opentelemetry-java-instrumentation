/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0;

import static io.opentelemetry.javaagent.tooling.bytebuddy.matcher.ClassLoaderMatcher.hasClassesNamed;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.tooling.InstrumentationModule;
import io.opentelemetry.javaagent.tooling.TypeInstrumentation;
import java.util.List;
import java.util.Map;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.eclipse.jetty.client.api.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(InstrumentationModule.class)
public class JettyHttpClient9InstrumentationModule extends InstrumentationModule {
  public JettyHttpClient9InstrumentationModule() {
    super("jetty-httpclient-9", "jetty-httpclient-9.0");
  }

  private static final Logger LOG = LoggerFactory
      .getLogger(JettyHttpClient9InstrumentationModule.class);

  @Override
  public List<TypeInstrumentation> typeInstrumentations() {
    return singletonList(new JettyHttpClient9Instrumentation());
  }

  public static class JettyHttpClient9Instrumentation implements TypeInstrumentation {

    @Override
    public ElementMatcher<ClassLoader> classLoaderOptimization() {
      return hasClassesNamed("org.eclipse.jetty.client.api.Request");
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
      return named("org.eclipse.jetty.client.HttpClient");
    }

    @Override
    public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {

      return singletonMap(
          isMethod()
              .and(named("newRequest"))
              .and(takesArguments(1))
              .and(takesArgument(1, named("java.lang.String"))),
          JettyHttpClient9InstrumentationModule.class.getName() + "$JettyHttpClient9Advice");

    }
  }

  public static class JettyHttpClient9Advice {
    @Advice.OnMethodExit
    public static void addTracingInterceptor(@Advice.This Request jettyRequest) {
      List<JettyClient9TracingInterceptor> current = jettyRequest
          .getRequestListeners(JettyClient9TracingInterceptor.class);
      if (current.isEmpty()) {
        LOG.debug("Jetty9 Listener is already instrumented for request");
        return;
      }
      LOG.debug("Insturmenting new Jetty9 Listener  for request");
      JettyClient9TracingInterceptor jettyListener = new JettyClient9TracingInterceptor();
      jettyRequest.onRequestBegin(jettyListener)
          .onRequestFailure(jettyListener)
          .onResponseFailure(jettyListener)
          .onResponseSuccess(jettyListener);
    }
  }
}
