
package io.quarkus.it.kubernetes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.builder.Version;
import io.quarkus.kubernetes.spi.CustomProjectRootBuildItem;
import io.quarkus.test.ProdBuildResults;
import io.quarkus.test.ProdModeTestResults;
import io.quarkus.test.QuarkusProdModeTest;

public class KubernetesWithInputStatefulSetResourcesTest {

    static final String APP_NAME = "kubernetes-with-input-statefulset-resource";

    @RegisterExtension
    static final QuarkusProdModeTest config = new QuarkusProdModeTest()
            .withApplicationRoot((jar) -> jar.addClasses(GreetingResource.class))
            .setApplicationName(APP_NAME)
            .setApplicationVersion("0.1-SNAPSHOT")
            .withConfigurationResource("kubernetes-with-statefulset-resource.properties")
            .setLogFileName("k8s.log")
            .addCustomResourceEntry(Path.of("src", "main", "kubernetes", "kubernetes.yml"),
                    "manifests/custom-deployment/kubernetes-with-stateful.yml")
            .setForcedDependencies(
                    Collections.singletonList(new AppArtifact("io.quarkus", "quarkus-kubernetes", Version.getVersion())))
            .addBuildChainCustomizerEntries(
                    new QuarkusProdModeTest.BuildChainCustomizerEntry(
                            KubernetesWithCustomResourcesTest.CustomProjectRootBuildItemProducerProdMode.class,
                            Collections.singletonList(CustomProjectRootBuildItem.class), Collections.emptyList()));

    @ProdBuildResults
    private ProdModeTestResults prodModeTestResults;

    @Test
    public void assertGeneratedResources() throws IOException {
        final Path kubernetesDir = prodModeTestResults.getBuildDir().resolve("kubernetes");
        assertThat(kubernetesDir)
                .isDirectoryContaining(p -> p.getFileName().endsWith("kubernetes.json"))
                .isDirectoryContaining(p -> p.getFileName().endsWith("kubernetes.yml"));
        List<HasMetadata> kubernetesList = DeserializationUtil
                .deserializeAsList(kubernetesDir.resolve("kubernetes.yml"));

        assertThat(kubernetesList).filteredOn(i -> i instanceof StatefulSet).singleElement().satisfies(i -> {
            assertThat(i).isInstanceOfSatisfying(StatefulSet.class, s -> {
                assertThat(s.getMetadata()).satisfies(m -> {
                    assertThat(m.getName()).isEqualTo(APP_NAME);
                });

                assertThat(s.getSpec()).satisfies(statefulSetSpec -> {
                    assertThat(statefulSetSpec.getServiceName()).isEqualTo(APP_NAME);
                    assertThat(statefulSetSpec.getReplicas()).isEqualTo(42);
                    assertThat(statefulSetSpec.getTemplate()).satisfies(t -> {
                        assertThat(t.getSpec()).satisfies(podSpec -> {
                            assertThat(podSpec.getTerminationGracePeriodSeconds()).isEqualTo(10);
                            assertThat(podSpec.getContainers()).allMatch(c -> APP_NAME.equals(c.getName()));
                        });
                    });
                    assertThat(statefulSetSpec.getSelector()).satisfies(ls -> {
                        assertThat(ls.getMatchLabels()).containsEntry("app.kubernetes.io/name", APP_NAME);
                        assertThat(ls.getMatchLabels()).containsEntry("custom-label", "my-label");
                    });
                });
            });
        });
    }
}
