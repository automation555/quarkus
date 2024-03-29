package io.quarkus.test.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Annotation that indicates that this test should be run using a native image,
 * rather than in the JVM.
 *
 * The standard usage pattern is expected to be a base test class that runs the
 * tests using the JVM version of Quarkus, with a subclass that extends the base
 * test and is annotated with this annotation to perform the same checks against
 * the native image.
 *
 * Note that it is not possible to mix JVM and native image tests in the same test
 * run, it is expected that the JVM tests will be standard unit tests that are
 * executed by surefire, while the native image tests will be integration tests
 * executed by failsafe.
 * This also means that injection of beans into a test class using {@code @Inject} is not supported
 * in native image tests. Such injection is only possible in tests injected with
 * {@link @QuarkusTest} so the test class structure must take this into account.
 *
 * @deprecated Use {@link @QuarkusIntegrationTest} instead.
 */
@Target(ElementType.TYPE)
@ExtendWith({ DisabledOnNativeImageCondition.class, QuarkusTestExtension.class, NativeTestExtension.class })
@Retention(RetentionPolicy.RUNTIME)
@Deprecated(since = "2.8.0.Final", forRemoval = true)
public @interface NativeImageTest {
}
