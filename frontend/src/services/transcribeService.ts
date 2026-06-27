import { sendAudioForTranscription } from "../api/transcribeApi";

export async function transcribeAudio(audioBlob: Blob): Promise<string> {
    const data = await sendAudioForTranscription(audioBlob);

    if (data.status === 'success') {
        return data.text!;
    }

    throw new Error(data.text || 'Unknown error occurred');
}
