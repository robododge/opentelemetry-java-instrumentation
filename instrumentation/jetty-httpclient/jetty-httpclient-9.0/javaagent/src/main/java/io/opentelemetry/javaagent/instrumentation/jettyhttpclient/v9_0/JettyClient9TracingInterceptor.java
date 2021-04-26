package io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0;

import static io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0.JettyClient9Tracer.tracer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyClient9TracingInterceptor implements Request.BeginListener,
    Request.QueuedListener, Request.FailureListener,
    Response.SuccessListener, Response.FailureListener, Response.CompleteListener {


  private static final Logger LOG = LoggerFactory.getLogger(JettyClient9TracingInterceptor.class);
  private  Context ctx;

  public JettyClient9TracingInterceptor(Context ctx) {
    this.ctx = ctx;
  }

  @Override
  public void onBegin(Request request) {
    LOG.debug("Trace ON-BEGIN called");

    Context parentContext = this.ctx;

    if (parentContext != null) {
      if (!tracer().shouldStartSpan(parentContext)) {
        LOG.debug("onBegin -  Cannot start trace");
        return;
      }
      LOG.debug("onBegin - Strarting trace");
      Context context = tracer().startSpan(parentContext, request, request);
      LOG.debug("onBegin - Resetting context trace");
      this.ctx = context;

      LOG.debug("onBegin - setting context trace");
      Span span = Span.fromContext(context);
      tracer().onRequest(span, request);
    } else {
      LOG.warn("onBegin - could not find a context at all");
    }

  }

  @Override
  public void onFailure(Request request, Throwable t) {
    LOG.debug("Failing trace, error in request", t);
    Context context = this.ctx;
    tracer().endExceptionally(context, t);
  }

  @Override
  public void onComplete(Result result) {
    LOG.debug("Request hit onComplete - done");
    Context context = this.ctx ;
    if (context != null) {
      Span span = Span.fromContext(context);
      if (span.isRecording()) {
        tracer().end(context, result.getResponse());
        LOG.debug("Really ending span now!*************");
      } else {
        LOG.debug("Cannot end span, not recording! ************");
      }
    }else {
      LOG.warn("onComplete - could not find a context at all");
    }


  }

  @Override
  public void onSuccess(Response response) {
    LOG.debug("Trace RESPONSE success");
//
//    Context context = Context.current();
//    tracer().end(context,response);

  }

  @Override
  public void onFailure(Response response, Throwable t) {
    LOG.debug("Failing trace, error in response", t);
    Context context = this.ctx != null ? this.ctx : Context.current();
    tracer().endExceptionally(context, t);
  }

  @Override
  public void onQueued(Request request) {

    LOG.debug("Queued listener: !!!! onQueued()--------");
    LOG.debug("Request class:: {}", request.getClass());

  }
}
