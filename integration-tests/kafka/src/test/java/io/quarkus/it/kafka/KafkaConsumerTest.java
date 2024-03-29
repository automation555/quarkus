package io.quarkus.it.kafka;

import static org.hamcrest.Matchers.containsString;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.kafka.InjectKafkaCompanion;
import io.quarkus.test.kafka.KafkaCompanionResource;
import io.restassured.RestAssured;
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion;

@QuarkusTestResource(KafkaCompanionResource.class)
@QuarkusTest
public class KafkaConsumerTest {

    @InjectKafkaCompanion
    KafkaCompanion companion;

    @Test
    public void test() {
        companion.produce(Integer.class, String.class)
                .fromRecords(new ProducerRecord<>("test-consumer", 1, "hi world"));
        RestAssured.when().get("/kafka").then().body(Matchers.is("hi world"));
    }

    @Test
    public void metrics() {
        // Look for kafka consumer metrics (add .log().all() to examine what they are
        RestAssured.when().get("/q/metrics").then()
                .statusCode(200)
                .body(containsString("kafka_consumer_"));
    }
}
