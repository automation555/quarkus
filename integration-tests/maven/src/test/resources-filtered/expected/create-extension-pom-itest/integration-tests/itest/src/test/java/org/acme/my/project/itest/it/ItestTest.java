package org.acme.my.project.itest.it;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class ItestTest {

    @Test
    public void test() {
        Assertions.fail("Add some assertions to " + getClass().getName());
    }

}
