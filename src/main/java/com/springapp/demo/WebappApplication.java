package com.springapp.demo;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import brave.sampler.Sampler;
import com.uber.jaeger.Configuration;
import com.uber.jaeger.samplers.ProbabilisticSampler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Encoding;
import zipkin.reporter.okhttp3.OkHttpSender;


@SpringBootApplication
public class WebappApplication {
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder.build();
	}

	@Bean
	public io.opentracing.Tracer jaegerTracer() { //create tracer with name spring-boot that samples all the requests
		return new Configuration("spring-boot",
				new Configuration.SamplerConfiguration(ProbabilisticSampler.TYPE, 1),
				new Configuration.ReporterConfiguration())
				.getTracer();  // Get the actual OpenTracing-compatible Tracer.
	}

	//@Bean
	public io.opentracing.Tracer zipkinTracer() {
		OkHttpSender okHttpSender = OkHttpSender.builder()
				.encoding(Encoding.JSON)
				.endpoint("http://localhost:9411/api/v1/spans")
				.build();
		AsyncReporter<Span> reporter = AsyncReporter.builder(okHttpSender).build();
		Tracing braveTracer = Tracing.newBuilder()
				.localServiceName("spring-boot")
				.reporter(reporter)
				.traceId128Bit(true)
				.sampler(Sampler.ALWAYS_SAMPLE)
				.build();
		return BraveTracer.create(braveTracer);
	}


	public static void main(String[] args) {
		SpringApplication.run(WebappApplication.class, args);
	}

}
