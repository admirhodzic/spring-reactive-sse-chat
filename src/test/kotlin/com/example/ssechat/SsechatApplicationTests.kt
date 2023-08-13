package com.example.ssechat

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseWebTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun contextLoads() {
    }
}

class HomeControllerTest : BaseWebTest() {
    @Test
    fun indexReturnsOk() {
        webTestClient
            .get()
            .uri("")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.TEXT_HTML)
            .expectBody(String::class.java)
            .value {
                assert(it.contains("chat"))
            }
    }
}
