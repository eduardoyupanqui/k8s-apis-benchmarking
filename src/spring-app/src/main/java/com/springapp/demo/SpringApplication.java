package com.springapp.demo;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringApplication {

	public static void main(String[] args) {
		org.springframework.boot.SpringApplication.run(SpringApplication.class, args);
	}

	@Value("${otlpEndpoint}")
	private String otlpEndpoint;
	@Bean
	public OpenTelemetry openTelemetry() {
		Resource resource = Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME, "spring-app").put(ResourceAttributes.SERVICE_VERSION, "0.1.0").build();

		OtlpGrpcSpanExporter otlpGrpcExporter = OtlpGrpcSpanExporter.builder()
				.setEndpoint("http://" + otlpEndpoint)
				.build();

		SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
				//.addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
				.addSpanProcessor(BatchSpanProcessor.builder(otlpGrpcExporter).build())
				.setResource(resource)
				.build();

		OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
				.setTracerProvider(sdkTracerProvider)
				.setPropagators(ContextPropagators.create(TextMapPropagator.composite(W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance())))
				.buildAndRegisterGlobal();

		return openTelemetry;
	}
}
