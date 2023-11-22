package com.quarkusapp;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class DevicesResourceTest {

    @Test
    public void testGetDevicesEndpoint() {
        given()
          .when().get("/api/devices")
          .then()
             .statusCode(200);
    }

}