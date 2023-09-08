package com.example.ssechat

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink

@SpringBootApplication
@EnableScheduling
class SsechatApplication

fun main(args: Array<String>) {
    runApplication<SsechatApplication>(*args)
}

@Component
class ClientPool(
    private val objectMapper: ObjectMapper,
) {
    fun add(room: String, sink: FluxSink<ServerSentEvent<String>>) {
        if (clients[room] == null) clients[room] = mutableListOf()
        clients[room]?.add(sink)
    }

    fun send(room: String, event: Any) {
        clients[room]?.forEach {
            it.next(
                ServerSentEvent.builder(objectMapper.writeValueAsString(event)).build(),
            )
        }
    }

    private var clients: MutableMap<String, MutableList<FluxSink<ServerSentEvent<String>>>> = mutableMapOf()
}

@RestController
class HomeController(
    private val clientPool: ClientPool,
) {
    @GetMapping(path = ["/stream/{room}"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamFlux(@PathVariable room: String): Flux<ServerSentEvent<String>> {
        return Flux.create {
            clientPool.add(room, it)
        }
    }

    @PostMapping("/{room}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun sendMessage(@PathVariable room: String, @RequestBody message: Map<String, String>): HttpStatus {
        clientPool.send(
            room,
            mapOf(
                "m" to message["msg"]!!,
                "f" to message["nick"]!!,
            ),
        )
        return HttpStatus.ACCEPTED
    }
}
