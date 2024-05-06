/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.tooling.ignore;

import io.opentelemetry.javaagent.tooling.util.Trie;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import java.util.concurrent.atomic.AtomicInteger;

public class IgnoredTypesMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {

  private final Trie<IgnoreAllow> ignoredTypes;

  public IgnoredTypesMatcher(Trie<IgnoreAllow> ignoredTypes) {
    this.ignoredTypes = ignoredTypes;
  }

  private static final AtomicInteger igoreKoalaCount = new AtomicInteger(0);

  @Override
  public boolean matches(TypeDescription target) {
    String name = target.getActualName();

    IgnoreAllow ignored = ignoredTypes.getOrNull(name);

    int koalaCount = igoreKoalaCount.get();
    boolean foundKoala = false;

    if (koalaCount < 1000) {
      if (  name.contains("koala")) {
        igoreKoalaCount.incrementAndGet();
        foundKoala = true;
      }
    }
    if (ignored == IgnoreAllow.ALLOW) {
      if (foundKoala) {
        System.err.printf("Found koala in ignores checking '%s' MATCH=true\n", name);
      }
      return false;
    } else if (ignored == IgnoreAllow.IGNORE) {
      if (foundKoala) {
        System.err.printf("Found koala in ignores checking '%s' MATCH=false\n", name);
      }
      return true;
    }

    // bytecode proxies typically have $$ in their name
    if (name.contains("$$") && !name.contains("$$Lambda$")) {
      // allow scala anonymous classes
      return !name.contains("$$anon$");
    }

    if (name.contains("$JaxbAccessor")
        || name.contains("CGLIB$$")
        || name.contains("javassist")
        || name.contains(".asm.")
        || name.contains("$__sisu")
        || name.contains("$$EnhancerByProxool$$")
        // glassfish ejb proxy
        // We skip instrumenting these because some instrumentations e.g. jax-rs instrument methods
        // that are annotated with @Path in an interface implemented by the class. We don't really
        // want to instrument these methods in generated classes as this would create spans that
        // have the generated class name in them instead of the actual class that handles the call.
        || name.contains("__EJB31_Generated__")) {
      return true;
    }

    if (name.startsWith("com.mchange.v2.c3p0.") && name.endsWith("Proxy")) {
      return true;
    }

    return false;
  }
}
