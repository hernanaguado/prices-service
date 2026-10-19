package com.hernanaguado.prices;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PricesApiIT {

    private static final int PRODUCT_ID = 35455;
    private static final int BRAND_ID = 1;
    private static final String PRICES_PATH = "/prices";
    private static final String APPLICATION_DATE_PARAM = "applicationDate";
    private static final String PRODUCT_ID_PARAM = "productId";
    private static final String BRAND_ID_PARAM = "brandId";
    private static final String EUR = "EUR";
    private static final String PROBLEM_JSON = "application/problem+json";

    @LocalServerPort
    private int port;

    private RequestSpecification spec;

    @BeforeEach
    void setUp() {
        spec = new RequestSpecBuilder()
                .setPort(port)
                // Decimals are read as BigDecimal so prices are compared exactly (35.50, not the float 35.5).
                // Integers are still read as Integer.
                .setConfig(RestAssuredConfig.config().jsonConfig(
                        JsonConfig.jsonConfig().numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL)))
                .build();
    }

    @ParameterizedTest(name = "Test {0}: {1} -> priceList {2}, price {5}")
    @CsvSource({
            "1, 2020-06-14T10:00:00, 1, 2020-06-14T00:00:00, 2020-12-31T23:59:59, 35.50",
            "2, 2020-06-14T16:00:00, 2, 2020-06-14T15:00:00, 2020-06-14T18:30:00, 25.45",
            "3, 2020-06-14T21:00:00, 1, 2020-06-14T00:00:00, 2020-12-31T23:59:59, 35.50",
            "4, 2020-06-15T10:00:00, 3, 2020-06-15T00:00:00, 2020-06-15T11:00:00, 30.50",
            "5, 2020-06-16T21:00:00, 4, 2020-06-15T16:00:00, 2020-12-31T23:59:59, 38.95"
    })
    void returnsTheApplicablePrice(int testCase, String applicationDate, int priceList,
                                   String startDate, String endDate, BigDecimal price) {
        given(spec)
                .queryParam(APPLICATION_DATE_PARAM, applicationDate)
                .queryParam(PRODUCT_ID_PARAM, PRODUCT_ID)
                .queryParam(BRAND_ID_PARAM, BRAND_ID)
        .when()
                .get(PRICES_PATH)
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(PRODUCT_ID_PARAM, equalTo(PRODUCT_ID))
                .body(BRAND_ID_PARAM, equalTo(BRAND_ID))
                .body("priceList", equalTo(priceList))
                .body("startDate", equalTo(startDate))
                .body("endDate", equalTo(endDate))
                .body("price", comparesEqualTo(price))
                .body("currency", equalTo(EUR));
    }

    @ParameterizedTest(name = "applicationDate={0}, productId={1}, brandId={2}")
    @CsvSource({
            "2020-06-14T10:00:00, 99999, 1, No applicable price for productId 99999 and brandId 1 at 2020-06-14T10:00:00",
            "2020-06-14T10:00:00, 35455, 2, No applicable price for productId 35455 and brandId 2 at 2020-06-14T10:00:00",
            "2019-01-01T00:00:00, 35455, 1, No applicable price for productId 35455 and brandId 1 at 2019-01-01T00:00:00"
    })
    void returnsNotFoundWhenNoPriceApplies(String applicationDate, long productId, long brandId, String detail) {
        given(spec)
                .queryParam(APPLICATION_DATE_PARAM, applicationDate)
                .queryParam(PRODUCT_ID_PARAM, productId)
                .queryParam(BRAND_ID_PARAM, brandId)
        .when()
                .get(PRICES_PATH)
        .then()
                .statusCode(404)
                .contentType(PROBLEM_JSON)
                .body("status", equalTo(404))
                .body("title", equalTo("Not Found"))
                .body("detail", equalTo(detail));
    }

    @ParameterizedTest(name = "applicationDate={0}, productId={1}, brandId={2}")
    @CsvSource(nullValues = "null", value = {
            "null,                35455, 1,    applicationDate",
            "2020-06-14T10:00:00, null,  1,    productId",
            "2020-06-14T10:00:00, 35455, null, brandId",
            "2020-06-14 10:00,    35455, 1,    applicationDate",
            // Short English format (with the narrow no-break space before PM): only ISO-8601 is accepted
            "'6/14/20, 4:00 PM', 35455, 1, applicationDate",
            "2020-06-14T10:00:00, abc,   1,    productId",
            "2020-06-14T10:00:00, 0,     1,    productId",
            "2020-06-14T10:00:00, 35455, -1,   brandId"
    })
    void returnsBadRequestForInvalidParameters(String applicationDate, String productId, String brandId,
                                               String invalidParameter) {
        RequestSpecification request = given(spec);
        if (applicationDate != null) {
            request.queryParam(APPLICATION_DATE_PARAM, applicationDate);
        }
        if (productId != null) {
            request.queryParam(PRODUCT_ID_PARAM, productId);
        }
        if (brandId != null) {
            request.queryParam(BRAND_ID_PARAM, brandId);
        }

        request
        .when()
                .get(PRICES_PATH)
        .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("status", equalTo(400))
                .body("title", equalTo("Bad Request"))
                .body("detail", containsString(invalidParameter))
                .body("detail", not(containsString("getApplicablePrice")));
    }

    @Test
    void reportsEveryValidationErrorInEnglish() {
        given(spec)
                .queryParam(APPLICATION_DATE_PARAM, "2020-06-14T10:00:00")
                .queryParam(PRODUCT_ID_PARAM, 0)
                .queryParam(BRAND_ID_PARAM, -1)
        .when()
                .get(PRICES_PATH)
        .then()
                .statusCode(400)
                .contentType(PROBLEM_JSON)
                .body("detail", equalTo(
                        "brandId: must be greater than or equal to 1; productId: must be greater than or equal to 1"));
    }
}
