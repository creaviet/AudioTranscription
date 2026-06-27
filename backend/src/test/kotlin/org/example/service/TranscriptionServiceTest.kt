package org.example.service

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TranscriptionServiceTest {

    @Test
    fun `returns transcript on successful response`(): Unit = runBlocking {
        val engine = MockEngine { _ ->
            respond(
                content = """{"results":{"channels":[{"alternatives":[{"transcript":"hello world"}]}]}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val service = TranscriptionService(HttpClient(engine), apiKey = "test-key")
        val file = File.createTempFile("test", ".wav").also { it.writeBytes(ByteArray(100)) }

        val result = service.transcribe(file)

        assertEquals("hello world", result)
        file.delete()
    }

    @Test
    fun `returns empty string when transcript field is missing`(): Unit = runBlocking {
        val engine = MockEngine { _ ->
            respond(
                content = """{"results":{"channels":[{"alternatives":[{}]}]}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val service = TranscriptionService(HttpClient(engine), apiKey = "test-key")
        val file = File.createTempFile("test", ".wav").also { it.writeBytes(ByteArray(100)) }

        val result = service.transcribe(file)

        assertEquals("", result)
        file.delete()
    }

    @Test
    fun `retries on 429 and succeeds on second attempt`(): Unit = runBlocking {
        val attempts = mutableListOf<Int>()
        val engine = MockEngine { _ ->
            val attempt = attempts.size + 1
            attempts.add(attempt)
            if (attempt == 1) {
                respond(
                    content = "Rate limited",
                    status = HttpStatusCode.TooManyRequests,
                    headers = headersOf("Retry-After", listOf("0"))
                )
            } else {
                respond(
                    content = """{"results":{"channels":[{"alternatives":[{"transcript":"retried"}]}]}}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }
        val service = TranscriptionService(HttpClient(engine), apiKey = "test-key")
        val file = File.createTempFile("test", ".wav").also { it.writeBytes(ByteArray(100)) }

        val result = service.transcribe(file, maxRetries = 3)

        assertEquals("retried", result)
        assertEquals(2, attempts.size)
        file.delete()
    }

    @Test
    fun `throws after exhausting all retries`(): Unit = runBlocking {
        val engine = MockEngine { _ ->
            respond(
                content = "Server error",
                status = HttpStatusCode.InternalServerError
            )
        }
        val service = TranscriptionService(HttpClient(engine), apiKey = "test-key")
        val file = File.createTempFile("test", ".wav").also { it.writeBytes(ByteArray(100)) }

        val ex = assertFailsWith<RuntimeException> {
            service.transcribe(file, maxRetries = 3)
        }

        assertContains(ex.message!!, "Deepgram returned 500")
        file.delete()
    }

    @Test
    fun `fails when api key is missing`(): Unit = runBlocking {
        val engine = MockEngine { _ ->
            respond("")
        }
        val service = TranscriptionService(HttpClient(engine))
        val file = File.createTempFile("test", ".wav").also { it.writeBytes(ByteArray(100)) }

        assertFailsWith<Exception> {
            service.transcribe(file)
        }
        file.delete()
    }
}
