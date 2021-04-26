package io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0;

import static io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0.JettyClient9Tracer.tracer;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JettyClient9TracingInterceptor implements Request.BeginListener,
    Request.QueuedListener, Request.FailureListener,
    Response.SuccessListener, Response.FailureListener, Response.CompleteListener {


  private static final Logger LOG = LoggerFactory.getLogger(JettyClient9TracingInterceptor.class);
  Context ctx;

  @Override
  public void onBegin(Request request) {
    LOG.debug("Trace ON-BEGIN called");
    LOG.debug("Has this.ctx? {}.. Has current? {}", this.ctx != null, !Context.current().equals(Context.root()) );

    if (this.ctx == null && !Context.current().equals(Context.root())) {
       LOG.debug("STRANGE CONTExT: {}", Context.current());
    }
    Context parentContext = this.ctx != null ? this.ctx : Context.current();
    if (!tracer().shouldStartSpan(parentContext)) {
      LOG.debug("onBegin -  Cannot start trace");
      return;
    }
    LOG.debug("onBegin - Strarting trace");
    Context context = tracer().startSpan(parentContext, request, request);
    Span span = Span.fromContext(context);
    tracer().onRequest(span, request);


    this.ctx = context;
    Scope scope = context.makeCurrent();
  }

  @Override
  public void onFailure(Request request, Throwable t) {
    LOG.debug("Failing trace, error in request", t);
    Context context = this.ctx != null ? this.ctx : Context.current();
    tracer().endExceptionally(context, t);
  }

  @Override
  public void onComplete(Result result) {
    //Do nothing here ATM
    LOG.debug("Request hit onComplete - done");
    Context context = this.ctx != null ? this.ctx : Context.current();
//    LOG.debug("current context {}", context);
    Span span = Span.fromContext(context);
    if (span.isRecording()) {
      tracer().end(context, result.getResponse());
      LOG.debug("Really ending span now!*************");
    } else {
      LOG.debug("Cannot end span, not recording! ************");
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
