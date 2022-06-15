package io.quarkus.opentelemetry.runtime.tracing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.IdGenerator;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.quarkus.arc.Arc;
import io.quarkus.opentelemetry.runtime.tracing.vertx.OpenTelemetryVertxMetricsFactory;
import io.quarkus.opentelemetry.runtime.tracing.vertx.OpenTelemetryVertxTracingFactory;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.VertxOptions;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.tracing.TracingOptions;

@Recorder
public class TracerRecorder {
    /* STATIC INIT */
    public Consumer<VertxOptions> getVertxTracingOptions() {
        TracingOptions tracingOptions = new TracingOptions()
                .setFactory(new OpenTelemetryVertxTracingFactory());
        return vertxOptions -> vertxOptions.setTracingOptions(tracingOptions);
    }

    public Consumer<VertxOptions> getVertxTracingMetricsOptions() {
        MetricsOptions metricsOptions = new MetricsOptions()
                .setEnabled(true)
                .setFactory(new OpenTelemetryVertxMetricsFactory());
        return vertxOptions -> vertxOptions.setMetricsOptions(metricsOptions);
    }

    /* STATIC INIT */
    public RuntimeValue<SdkTracerProvider> createTracerProvider(
            String quarkusVersion,
            String serviceName,
            String serviceVersion,
            ShutdownContext shutdownContext) {
        BeanManager beanManager = Arc.container().beanManager();

        Instance<IdGenerator> idGenerator = beanManager.createInstance()
                .select(IdGenerator.class, Any.Literal.INSTANCE);

        SdkTracerProviderBuilder builder = SdkTracerProvider.builder();

        // Define ID Generator if present
        if (idGenerator.isResolvable()) {
            builder.setIdGenerator(idGenerator.get());
        }

        DelayedAttributes delayedAttributes = beanManager.createInstance()
                .select(DelayedAttributes.class, Any.Literal.INSTANCE).get();

        delayedAttributes.setAttributesDelegate(Resource.getDefault()
                .merge(Resource.create(
                        Attributes.of(
                                ResourceAttributes.SERVICE_NAME, serviceName,
                                ResourceAttributes.SERVICE_VERSION, serviceVersion,
                                ResourceAttributes.WEBENGINE_NAME, "Quarkus",
                                ResourceAttributes.WEBENGINE_VERSION, quarkusVersion)))
                .getAttributes());

        // Define Service Resource
        builder.setResource(Resource.create(delayedAttributes));

        LateBoundSampler lateBoundSampler = beanManager.createInstance()
                .select(LateBoundSampler.class, Any.Literal.INSTANCE).get();

        // Set LateBoundSampler
        builder.setSampler(lateBoundSampler);

        // Find all SpanExporter instances
        Instance<SpanExporter> allExporters = beanManager.createInstance()
                .select(SpanExporter.class, Any.Literal.INSTANCE);
        allExporters.forEach(spanExporter -> builder.addSpanProcessor(SimpleSpanProcessor.create(spanExporter)));

        // Find all SpanProcessor instances
        Instance<SpanProcessor> allProcessors = beanManager.createInstance()
                .select(SpanProcessor.class, Any.Literal.INSTANCE);
        allProcessors.forEach(builder::addSpanProcessor);

        SdkTracerProvider tracerProvider = builder.build();

        // Register shutdown tasks
        shutdownContext.addShutdownTask(() -> {
            tracerProvider.forceFlush();
            tracerProvider.shutdown();
        });

        return new RuntimeValue<>(tracerProvider);
    }

    /* RUNTIME INIT */
    public void setupResources(TracerRuntimeConfig config) {
        // Find all Resource instances
        Instance<Resource> allResources = CDI.current()
                .getBeanManager()
                .createInstance()
                .select(Resource.class, Any.Literal.INSTANCE);

        // Merge resource instances with env attributes
        Resource resource = allResources.stream()
                .reduce(Resource.empty(), Resource::merge)
                .merge(config.resourceAttributes
                        .map(TracerUtil::mapResourceAttributes)
                        .orElseGet(Resource::empty));

        // Update Delayed attributes to contain new runtime attributes if necessary
        if (resource.getAttributes().size() > 0) {
            DelayedAttributes delayedAttributes = CDI.current()
                    .select(DelayedAttributes.class).get();

            delayedAttributes.setAttributesDelegate(
                    delayedAttributes.toBuilder()
                            .putAll(resource.getAttributes())
                            .build());
        }
    }

    /* RUNTIME INIT */
    public void setupSampler(
            TracerRuntimeConfig config,
            List<String> dropNonApplicationUris,
            List<String> dropStaticResources) {

        LateBoundSampler lateBoundSampler = CDI.current().select(LateBoundSampler.class, Any.Literal.INSTANCE).get();
        Optional<Sampler> samplerBean = CDI.current()
                .select(Sampler.class, Any.Literal.INSTANCE)
                .stream()
                .filter(o -> !(o instanceof LateBoundSampler))
                .findFirst();

        // Define Sampler using bean if present
        if (samplerBean.isPresent()) {
            lateBoundSampler.setSamplerDelegate(samplerBean.get());
        } else {
            List<String> dropNames = new ArrayList<>();
            if (config.suppressNonApplicationUris) {
                dropNames.addAll(dropNonApplicationUris);
            }
            if (!config.includeStaticResources) {
                dropNames.addAll(dropStaticResources);
            }
            // Define Sampler using config
            lateBoundSampler.setSamplerDelegate(TracerUtil.mapSampler(config.sampler, dropNames));
        }
    }
}