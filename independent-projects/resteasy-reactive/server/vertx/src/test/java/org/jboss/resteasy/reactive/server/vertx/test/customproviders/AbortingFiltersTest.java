package org.jboss.resteasy.reactive.server.vertx.test.customproviders;

import static org.hamcrest.Matchers.containsString;

import io.restassured.RestAssured;
import java.util.function.Supplier;
import org.jboss.resteasy.reactive.server.vertx.test.framework.ResteasyReactiveUnitTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class AbortingFiltersTest {

    @RegisterExtension
    static ResteasyReactiveUnitTest test = new ResteasyReactiveUnitTest()
            .setArchiveProducer(new Supplier<>() {
                @Override
                public JavaArchive get() {
                    return ShrinkWrap.create(JavaArchive.class)
                            .addClasses(CustomContainerRequestFilter.class, CustomFiltersResource.class, ResponseFilter.class,
                                    OptionalRequestFilter.class, IllegalStateExceptionMapper.class);
                }
            });

    @Test
    public void testFilters() {
        RestAssured.given().header("some-input", "bar").get("/custom/req")
                .then().statusCode(200).body(containsString("/custom/req-bar")).extract().headers();
    }

    @Test
    public void testOptionalFilter() {
        RestAssured.given().header("optional-input", "abort").get("/custom/req")
                .then().statusCode(204);
    }

    @Test
    public void testResponseFilter() {
        RestAssured.given().header("response-input", "abort").get("/custom/req")
                .then().statusCode(202).body(containsString("abort"));
    }
}
