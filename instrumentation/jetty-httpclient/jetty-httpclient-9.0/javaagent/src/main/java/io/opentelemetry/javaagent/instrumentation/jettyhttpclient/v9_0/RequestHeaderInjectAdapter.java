package io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0;

import io.opentelemetry.context.propagation.TextMapSetter;
import org.eclipse.jetty.client.api.Request;
import javax.annotation.Nullable;

public class RequestHeaderInjectAdapter implements TextMapSetter<Request> {

  public static final RequestHeaderInjectAdapter SETTER = new RequestHeaderInjectAdapter();

  @Override
  public void set(@Nullable Request request, String key, String value) {
    request.header(key, value);

  }
}
