package io.opentelemetry.javaagent.instrumentation.jettyhttpclient.v9_0;

import io.opentelemetry.context.propagation.TextMapSetter;
import javax.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHeaderInjectAdapter implements TextMapSetter<Request> {

  public static final RequestHeaderInjectAdapter SETTER = new RequestHeaderInjectAdapter();

  private static final Logger LOG = LoggerFactory.getLogger(RequestHeaderInjectAdapter.class);


  @Override
  public void set(@Nullable Request request, String key, String value) {
    LOG.debug("Context adding key:value: {}: {}",key, value);
    request.header(key, value);

  }
}
