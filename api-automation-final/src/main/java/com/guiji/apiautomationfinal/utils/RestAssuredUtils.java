package com.guiji.apiautomationfinal.utils;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.config.EncoderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class RestAssuredUtils {
    @Value("${request.log.enable}")
    private boolean logEnable;

    public Response sendRequest(String url, String methodType, Map<String, Object> headers, Object requestBody, String contentType) {
        RequestSpecification requestSpecification = RestAssured.given();
        if (logEnable) {
            requestSpecification.log().all();
            requestSpecification.contentType(ContentType.JSON);
        }
        requestSpecification.config(RestAssured.config().encoderConfig(EncoderConfig.encoderConfig()
                .encodeContentTypeAs("application/xml", ContentType.TEXT)));
        if (headers != null && !headers.isEmpty()) {
            requestSpecification.headers(headers);
        }
        if (contentType != null) {
            requestSpecification.contentType(contentType);
        }
        if (requestBody != null) {
            requestSpecification.body(requestBody);
        }
        methodType = methodType.toUpperCase();
        Response response = switch (methodType) {
            case "GET" -> requestSpecification.when().get(url);
            case "POST" -> requestSpecification.body(requestBody).post(url);
            case "PUT" -> requestSpecification.body(requestBody).put(url);
            case "PATCH" -> requestSpecification.body(requestBody).patch(url);
            default -> requestSpecification.body(requestBody).delete(url);
        };
        if (logEnable) {
            response.then().log().all();
        }
        return response;
    }

    public Response sendRequest(String url, String methodType, Map<String, Object> headers) {
        return sendRequest(url, methodType, headers, null, null);
    }

    public Response sendRequest(String url, String methodType) {
        return sendRequest(url, methodType, null, null, null);
    }

    public static Response postXml(String url, String xmlBody, Map<String, Object> headers) {
        EncoderConfig encoderConfig = EncoderConfig.encoderConfig().encodeContentTypeAs("application/xml", ContentType.TEXT);
        return RestAssured.given().log().all()
                .config(RestAssured.config().encoderConfig(encoderConfig))
                .headers(headers)
                .contentType(ContentType.XML)
                .body(xmlBody)
                .when()
                .post(url).then().log().all().extract().response();
    }
}