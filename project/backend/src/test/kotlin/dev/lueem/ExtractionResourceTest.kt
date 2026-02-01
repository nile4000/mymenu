package dev.lueem

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import java.io.File

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
