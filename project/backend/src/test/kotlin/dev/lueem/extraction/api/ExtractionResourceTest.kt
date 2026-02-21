package dev.lueem.extraction.api

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Test

@QuarkusTest
class ExtractionResourceTest {

    @Test
    fun testExtractEndpointWithoutFile() {
        given()
            .`when`().post("/api/extract")
            .then()
            .statusCode(400)
    }
}
