package org.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import org.example.controller.transcriptionRoutes

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
        configureCors()
        configureContentNegotiation()
        configureRouting()
    }.start(wait = true)
}

fun Application.configureCors() {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }
}

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("hello world")
        }
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        get("/openapi.yaml") {
            val yaml = this::class.java.classLoader.getResource("openapi.yaml")?.readText()
            if (yaml != null) {
                call.respondText(yaml, ContentType.parse("text/yaml"))
            } else {
                call.respondText("Not Found", status = HttpStatusCode.NotFound)
            }
        }

        get("/swagger-ui") {
            val html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="utf-8" />
                <title>AutoTranscription API - Swagger UI</title>
                <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css" />
            </head>
            <body>
                <div id="swagger-ui"></div>
                <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                <script>
                    SwaggerUIBundle({
                        url: '/openapi.yaml',
                        dom_id: '#swagger-ui',
                    });
                </script>
            </body>
            </html>
            """.trimIndent()
            call.respondText(html, ContentType.Text.Html)
        }

        transcriptionRoutes()
    }
}
