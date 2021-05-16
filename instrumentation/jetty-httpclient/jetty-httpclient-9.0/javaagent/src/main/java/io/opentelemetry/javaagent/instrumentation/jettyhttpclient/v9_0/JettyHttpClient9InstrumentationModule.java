/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0;

import static io.opentelemetry.javaagent.instrumentation.api.Java8BytecodeBridge.currentContext;
import static io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0.JettyClient9Tracer.tracer;
import static io.opentelemetry.javaagent.tooling.bytebuddy.matcher.ClassLoaderMatcher.hasClassesNamed;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import com.google.auto.service.AutoService;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
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
              .and(takesArgument(0, java.net.URI.class)),
          JettyHttpClient9InstrumentationModule.class.getName() + "$JettyHttpClient9Advice");

    }

  }

  public static class JettyHttpClient9Advice {

    @Advice.OnMethodEnter()
    public static void addTracingEnter(
        @Advice.Local("otelContext") Context context,
        @Advice.Argument(2) Request jettyReqWrong){
      System.out.println("OnMethodEnter ENTERING JETTY ADVICE my02!!! ");
      System.out.println("Jetty req WRONG:"+jettyReqWrong);
      Context parentContext = currentContext();
      if (!tracer().shouldStartSpan(parentContext)) {
        return;
      }
      context = parentContext;
    }


    @Advice.OnMethodExit( suppress = Throwable.class)
    public static void addTracingInterceptor(
        @Advice.Return Request jettyRequest,
        @Advice.Local("otelContext") Context context) {

      System.out.println("OnMethodExit ENTERING JETTY ADVICE!!! ");

      if (context == null) {
        System.out.println("0b -Cannot proceed tracing without a context! ");
        return;
      }

      List<JettyClient9TracingInterceptor> current = jettyRequest
          .getRequestListeners(JettyClient9TracingInterceptor.class);
      if (!current.isEmpty()) {
        System.out.println("0c - A tracing interceptor is already in place for this request! ");
        return;
      }

      // I think here I don't need attach context to current thread because context is passed explicitly
      try (Scope scope = context.makeCurrent() ) {
        System.out.println("0d - Starting a new tracer on request! ");
        JettyClient9TracingInterceptor jettyListener = new JettyClient9TracingInterceptor(context);
        jettyRequest.onRequestBegin(jettyListener)
            .onRequestFailure(jettyListener)
            .onResponseFailure(jettyListener)
            .onResponseSuccess(jettyListener);

      }


    }
  }
}
