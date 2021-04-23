package io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0;

import static io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0.RequestHeaderInjectAdapter.SETTER;

import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.instrumentation.api.tracer.HttpClientTracer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import io.opentelemetry.instrumentation.api.tracer.net.NetPeerAttributes;
import org.eclipse.jetty.http.HttpFields;
import java.net.URI;
import java.net.URISyntaxException;

public class JettyClient9Tracer extends HttpClientTracer<Request, Request, Response> {

  private static final JettyClient9Tracer TRACER = new JettyClient9Tracer();

  public static final JettyClient9Tracer tracer() { return TRACER; }

  private JettyClient9Tracer() {
    super(NetPeerAttributes.INSTANCE);
  }

  @Override
  protected String getInstrumentationName() { return "io.opentelemetry.javaagent.jetty-httpclient-9.0"; }

  @Override
  protected String method(Request request) { return request.getMethod().toString(); }

  @Override
  protected @Nullable URI url(Request request) throws URISyntaxException {  return request.getURI() ;}

  @Override
  protected @Nullable Integer status(Response response) { return response.getStatus(); }

  @Override
  protected @Nullable String requestHeader(Request request, String name) {
    HttpFields headers = request.getHeaders();
    return extractHeader(headers, name);

  }

  @Override
  protected @Nullable String responseHeader(Response response, String name) {
    HttpFields headers = response.getHeaders();
    return extractHeader(headers, name);
  }

  @Override
  protected TextMapSetter<Request> getSetter() { return SETTER; }

  private String extractHeader(HttpFields headers, String name) {

    String headerVal = null;
    if (headers != null) {
      headerVal = headers.containsKey(name) ? headers.get(name) : null;
    }
    return headerVal;

  }
}
