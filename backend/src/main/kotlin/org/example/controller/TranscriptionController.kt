package org.example.controller

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.content.*
import kotlinx.serialization.Serializable
import org.example.service.TranscriptionService
import org.slf4j.LoggerFactory
import java.io.File

private val logger = LoggerFactory.getLogger("TranscriptionController")

@Serializable
data class TranscriptionResponse(val text: String, val status: String)

fun Route.transcriptionRoutes(transcriptionService: TranscriptionService = TranscriptionService()) {
    var audioFile: File? = null

    post("/api/v1/transcribe") {
        try {
            val multipart = call.receiveMultipart()

            var audioContentType: String? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        if (part.name == "audio") {
                            audioContentType = part.contentType?.toString()
                            audioFile = File.createTempFile("audio_", ".wav")
                            audioFile?.outputStream()?.use { output ->
                                part.streamProvider().use { input ->
                                    input.copyTo(output)
                                }
                            }
                            logger.info("Audio file saved: ${audioFile?.absolutePath}")
                        }
                    }
                    else -> {}
                }
                part.release()
            }

            if (audioFile == null || !audioFile.exists()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    TranscriptionResponse("No audio file provided", "error")
                )
                return@post
            }

            val transcript = transcriptionService.transcribe(audioFile, audioContentType)

            call.respond(
                HttpStatusCode.OK,
                TranscriptionResponse(transcript, "success")
            )
        } catch (e: Exception) {
            logger.error("Transcription error", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                TranscriptionResponse("Transcription failed: ${e.message}", "error")
            )
        } finally {
            audioFile?.delete()
        }
    }
}
