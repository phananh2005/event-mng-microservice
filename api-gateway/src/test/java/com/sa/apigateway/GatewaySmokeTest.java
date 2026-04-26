package com.sa.apigateway;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class GatewaySmokeTest {

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void stubDownstream() {
        // auth-service stubs
        stubFor(post(urlPathEqualTo("/api/v1/auth/login"))
                .willReturn(okJson("{\"code\":1000,\"result\":{\"token\":\"fake-jwt\"}}")));

        stubFor(get(urlPathEqualTo("/api/v1/auth/verify"))
                .willReturn(ok("Email verified successfully")));

        // event-service stubs
        stubFor(get(urlPathEqualTo("/api/v1/events"))
                .willReturn(okJson("{\"code\":1000,\"result\":{\"content\":[],\"totalElements\":0}}")));

        stubFor(get(urlPathEqualTo("/api/v1/categories"))
                .willReturn(okJson("{\"code\":1000,\"result\":[]}")));

        // legacy monolith stub
        stubFor(get(urlPathEqualTo("/event-mng/actuator/health"))
                .willReturn(okJson("{\"status\":\"UP\"}")));
    }

    @Test
    void authLogin_shouldRouteToAuthService() {
        webTestClient.post().uri("/api/v1/auth/login")
                .bodyValue("{\"username\":\"u\",\"password\":\"p\"}")
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result.token").isEqualTo("fake-jwt");

        WireMock.verify(postRequestedFor(urlPathEqualTo("/api/v1/auth/login")));
    }

    @Test
    void authVerify_shouldRouteToAuthService() {
        webTestClient.get().uri("/api/v1/auth/verify?token=abc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Email verified successfully");

        WireMock.verify(getRequestedFor(urlPathEqualTo("/api/v1/auth/verify")));
    }

    @Test
    void eventsList_shouldRouteToEventService() {
        webTestClient.get().uri("/api/v1/events")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result.totalElements").isEqualTo(0);

        WireMock.verify(getRequestedFor(urlPathEqualTo("/api/v1/events")));
    }

    @Test
    void categoriesList_shouldRouteToEventService() {
        webTestClient.get().uri("/api/v1/categories")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result").isArray();

        WireMock.verify(getRequestedFor(urlPathEqualTo("/api/v1/categories")));
    }

    @Test
    void legacyMonolith_shouldRouteFallback() {
        webTestClient.get().uri("/event-mng/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");

        WireMock.verify(getRequestedFor(urlPathEqualTo("/event-mng/actuator/health")));
    }
}
