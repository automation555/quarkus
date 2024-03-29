package io.quarkus.resteasy.links.deployment;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import io.quarkus.resteasy.links.runtime.hal.HalServerResponseFilter;
import io.quarkus.resteasy.links.runtime.hal.ResteasyHalService;

final class LinksProcessor {
    @BuildStep
    void addHalSupport(Capabilities capabilities, BuildProducer<ResteasyJaxrsProviderBuildItem> jaxRsProviders,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        boolean isHalSupported = capabilities.isPresent(Capability.HAL);
        if (isHalSupported) {
            if (!capabilities.isPresent(Capability.RESTEASY_JSON_JSONB)
                    && !capabilities.isPresent(Capability.RESTEASY_JSON_JACKSON)) {
                throw new IllegalStateException("Cannot generate HAL endpoints without "
                        + "either 'quarkus-resteasy-jsonb' or 'quarkus-resteasy-jackson'");
            }

            jaxRsProviders.produce(
                    new ResteasyJaxrsProviderBuildItem(HalServerResponseFilter.class.getName()));

            additionalBeans.produce(AdditionalBeanBuildItem.builder()
                    .addBeanClass(ResteasyHalService.class).setUnremovable().build());
        }
    }
}
