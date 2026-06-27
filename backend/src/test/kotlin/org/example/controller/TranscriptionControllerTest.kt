package org.example.controller

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.example.service.TranscriptionService
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TranscriptionControllerTest {

    @Test
    fun `POST api transcribe returns 200 with transcript`() = testApplication {
        application {
            this.install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                this.json(Json { ignoreUnknownKeys = true })
            }
            val engine = MockEngine { _ ->
                respond(
                    content = """{"results":{"channels":[{"alternatives":[{"transcript":"hello world"}]}]}}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            val service = TranscriptionService(HttpClient(engine), apiKey = "test-key")
            routing { transcriptionRoutes(service) }
        }

        val response = client.post("/api/v1/transcribe") {
            setBody(MultiPartFormDataContent(formData {
                append("audio", ByteArray(100), Headers.build {
                    append(HttpHeaders.ContentType, "audio/wav")
                    append(HttpHeaders.ContentDisposition, "form-data; name=\"audio\"; filename=\"test.wav\"")
                })
            }))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("""{"text":"hello world","status":"success"}""", response.bodyAsText())
    }

    @Test
    fun `POST api transcribe returns 400 when no audio file`() = testApplication {
        application {
            this.install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                this.json(Json { ignoreUnknownKeys = true })
            }
            val service = TranscriptionService(HttpClient(MockEngine { _ -> respond("") }), apiKey = "test-key")
            routing { transcriptionRoutes(service) }
        }

        val response = client.post("/api/v1/transcribe") {
            setBody(MultiPartFormDataContent(formData {}))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(true, response.bodyAsText().contains("No audio file provided"))
    }

    @Test
    fun `POST api transcribe returns 500 on service failure`() = testApplication {
        application {
            this.install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                this.json(Json { ignoreUnknownKeys = true })
            }
            val service = TranscriptionService(HttpClient(MockEngine { _ ->
                respond("error", status = HttpStatusCode.InternalServerError)
            }), apiKey = "test-key")
            routing { transcriptionRoutes(service) }
        }

        val response = client.post("/api/v1/transcribe") {
            setBody(MultiPartFormDataContent(formData {
                append("audio", ByteArray(100), Headers.build {
                    append(HttpHeaders.ContentType, "audio/wav")
                    append(HttpHeaders.ContentDisposition, "form-data; name=\"audio\"; filename=\"test.wav\"")
                })
            }))
        }

        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals(true, response.bodyAsText().contains("Transcription failed"))
    }
}
