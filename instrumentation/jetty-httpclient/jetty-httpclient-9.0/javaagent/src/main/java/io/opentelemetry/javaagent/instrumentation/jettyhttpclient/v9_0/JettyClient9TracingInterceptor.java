package io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0;

import static io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0.JettyClient9Tracer.tracer;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

public class JettyClient9TracingInterceptor implements Request.BeginListener, Request.FailureListener,
    Response.SuccessListener, Response.FailureListener, Response.CompleteListener{

//  @Override
//  public Response intercept(Chain chain) throws IOException {
//    Context parentContext = Context.current();
//    if (!tracer().shouldStartSpan(parentContext)) {
//      return chain.proceed(chain.request());
//    }
//
//    Request.Builder requestBuilder = chain.request().newBuilder();
//    Context context = tracer().startSpan(parentContext, chain.request(), requestBuilder);
//
//    Response response;
//    try (Scope ignored = context.makeCurrent()) {
//      response = chain.proceed(requestBuilder.build());
//    } catch (Exception e) {
//      tracer().endExceptionally(context, e);
//      throw e;
//    }
//    tracer().end(context, response);
//    return response;
//  }

  private static final Logger LOG = LoggerFactory.getLogger(JettyClient9TracingInterceptor.class);

  @Override
  public void onBegin(Request request) {
    Context parentContext = Context.current();
    if (!tracer().shouldStartSpan(parentContext)) {
      return;
    }
    LOG.debug("Strarting trace");
    Context context = tracer().startSpan(parentContext, request, request);
    Scope scope = context.makeCurrent();
  }

  @Override
  public void onFailure(Request request, Throwable t) {
    LOG.debug("Failing trace, error in request", t);
    Context context = Context.current();
    tracer().endExceptionally(context, t);
  }

  @Override
  public void onComplete(Result result) {
    //Do nothing here ATM
    LOG.debug("Request hit onComplete - done");
  }

  @Override
  public void onSuccess(Response response) {
    LOG.debug("Trace success, ending now");

    Context context = Context.current();
    tracer().end(context,response);
  }

  @Override
  public void onFailure(Response response, Throwable t) {
    LOG.debug("Failing trace, error in response", t);

    Context context = Context.current();
    tracer().endExceptionally(context, t);
  }
}
