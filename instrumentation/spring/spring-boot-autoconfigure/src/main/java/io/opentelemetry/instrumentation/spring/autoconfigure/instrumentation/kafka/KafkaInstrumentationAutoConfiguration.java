/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.spring.autoconfigure.instrumentation.kafka;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.KafkaTelemetry;
import io.opentelemetry.instrumentation.spring.autoconfigure.internal.SdkEnabled;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@ConditionalOnBean(OpenTelemetry.class)
@ConditionalOnClass({KafkaTemplate.class, ConcurrentKafkaListenerContainerFactory.class})
@ConditionalOnProperty(name = "otel.instrumentation.kafka.enabled", matchIfMissing = true)
@Conditional(SdkEnabled.class)
@Configuration
public class KafkaInstrumentationAutoConfiguration {

  @Bean
  DefaultKafkaProducerFactoryCustomizer otelKafkaProducerFactoryCustomizer(
      OpenTelemetry openTelemetry) {
    KafkaTelemetry kafkaTelemetry = KafkaTelemetry.create(openTelemetry);
    return producerFactory -> producerFactory.addPostProcessor(kafkaTelemetry::wrap);
  }

  // static to avoid "is not eligible for getting processed by all BeanPostProcessors" warning
  @Bean
  static ConcurrentKafkaListenerContainerFactoryPostProcessor
      otelKafkaListenerContainerFactoryBeanPostProcessor(
          ObjectProvider<OpenTelemetry> openTelemetryProvider) {
    return new ConcurrentKafkaListenerContainerFactoryPostProcessor(openTelemetryProvider);
  }
}
