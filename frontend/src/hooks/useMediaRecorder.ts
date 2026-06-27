import { useRef, useState, useCallback } from "react";

interface UseMediaRecorderReturn {
    isRecording: boolean;
    error: string | null;
    startRecording: () => Promise<void>;
    stopRecording: () => Promise<Blob>;
}

function getSupportedMimeType(): string {
    const types = [
        'audio/webm;codecs=opus',
        'audio/webm',
        'audio/ogg;codecs=opus',
        'audio/mp4',
        'audio/wav',
    ];
    return types.find(t => MediaRecorder.isTypeSupported(t)) ?? '';
}

export function useMediaRecorder(): UseMediaRecorderReturn {
    const mediaRecorderRef = useRef<MediaRecorder | null>(null);
    const audioChunksRef = useRef<Blob[]>([]);
    const mimeTypeRef = useRef<string>('');
    const [isRecording, setIsRecording] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const startRecording = useCallback(async () => {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            const mimeType = getSupportedMimeType();
            mimeTypeRef.current = mimeType;
            const mediaRecorder = mimeType
                ? new MediaRecorder(stream, { mimeType })
                : new MediaRecorder(stream);
            mediaRecorderRef.current = mediaRecorder;
            audioChunksRef.current = [];

            mediaRecorder.ondataavailable = (event) => {
                audioChunksRef.current.push(event.data);
            };

            mediaRecorder.onstart = () => {
                setIsRecording(true);
                setError(null);
            };

            mediaRecorder.start();
        } catch (error) {
            setError(
                error instanceof Error
                    ? error.message
                    : 'Failed to access microphone',
            );
        }
    }, []);

    const stopRecording = useCallback(() => {
        return new Promise<Blob>((resolve, reject) => {
            const recorder = mediaRecorderRef.current;
            if (!recorder) {
                reject(new Error('No active recording'));
                return;
            }

            recorder.onstop = () => {
                const audioBlob = new Blob(audioChunksRef.current, {
                    type: mimeTypeRef.current || 'audio/webm',
                });
                recorder.stream.getTracks().forEach((track) => track.stop());
                setIsRecording(false);
                resolve(audioBlob);
            };

            recorder.stop();
        });
    }, []);

    return { isRecording, error, startRecording, stopRecording };
}
