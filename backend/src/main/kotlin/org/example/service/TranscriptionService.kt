package org.example.service

import io.ktor.client.*
import io.ktor.client.content.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.io.File

private val logger = LoggerFactory.getLogger(TranscriptionService::class.java)

class TranscriptionService(
    private val client: HttpClient = HttpClient(OkHttp),
    private val apiKey: String? = null
) {
    suspend fun transcribe(file: File, contentType: String? = null, maxRetries: Int = 3): String {
        val key = apiKey ?: System.getenv("DEEPGRAM_API_KEY") ?: error("DEEPGRAM_API_KEY not set")
        val mimeType = contentType ?: "audio/wav"
        var lastError: Exception? = null

        for (attempt in 1..maxRetries) {
            try {
                val response: HttpResponse = client.post("https://api.deepgram.com/v1/listen?model=nova-3&smart_format=true") {
                    headers {
                        append(HttpHeaders.Authorization, "Token $key")
                        append(HttpHeaders.ContentType, mimeType)
                    }
                    setBody(LocalFileContent(file, ContentType.parse(mimeType)))
                }

                if (response.status == HttpStatusCode.TooManyRequests) {
                    val retryAfter = response.headers["Retry-After"]?.toLongOrNull() ?: (2L shl attempt)
                    logger.warn("Rate limited (attempt $attempt/$maxRetries). Retrying in ${retryAfter}s")
                    delay(retryAfter * 1000)
                    continue
                }

                val body = response.bodyAsText()
                if (!response.status.isSuccess()) {
                    throw RuntimeException("Deepgram returned ${response.status}: $body")
                }

                val json = Json.parseToJsonElement(body).jsonObject
                val channels = json["results"]?.jsonObject?.get("channels")
                val alternatives = (channels as? JsonArray)?.firstOrNull()?.jsonObject?.get("alternatives")
                val transcript = (alternatives as? JsonArray)?.firstOrNull()?.jsonObject?.get("transcript")?.jsonPrimitive?.content ?: ""
                return transcript
            } catch (e: Exception) {
                lastError = e
                if (attempt < maxRetries) {
                    val wait = (2L shl attempt) * 1000
                    logger.warn("Request failed (attempt $attempt/$maxRetries): ${e.message}. Retrying in ${wait}ms")
                    delay(wait)
                }
            }
        }

        throw lastError ?: RuntimeException("Transcription failed after $maxRetries retries")
    }
}
