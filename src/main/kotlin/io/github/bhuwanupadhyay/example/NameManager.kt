package io.github.bhuwanupadhyay.example

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import java.util.*

@Component
class NameHandler {

    fun findGivenName(req: ServerRequest): Mono<ServerResponse> {
        return Optional.ofNullable(req.pathVariable("given-name"))
                .map { t -> ok().bodyValue("{ \"givenName\": \"$t\"}") }
                .orElseGet { badRequest().build() }
    }

}

@Configuration
class NameRoutes(private val handler: NameHandler) {

    @Bean
    fun router() = router {
        accept(APPLICATION_JSON).nest {
            GET("/names/{given-name}", handler::findGivenName)
        }
    }

}
