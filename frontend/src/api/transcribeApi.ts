import type { components } from './schema.d.ts'

type TranscribeResponse = components["schemas"]["TranscriptionResponse"]

export async function sendAudioForTranscription(audioBlob: Blob): Promise<TranscribeResponse> {
    const formData = new FormData();
    formData.append('audio', audioBlob, 'recording.wav');

    const response = await fetch('/api/v1/transcribe', {
        method: 'POST',
        body: formData,
    });

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    return response.json();
}
