import React, { useState } from "react";
import { useMediaRecorder } from "../../../hooks/useMediaRecorder";
import { transcribeAudio } from "../../../services/transcribeService";
import TranscriptModal from "../../Molecules/transcriptModal/TranscriptModal.tsx";
import SoundWaveAnimation from "../../Atoms/soundWaveAnimation/SoundWaveAnimation.tsx";

import styles from './AudioRecrder.module.scss';

export default function AudioRecorder(): React.JSX.Element {
    const { isRecording, error: micError, startRecording, stopRecording } = useMediaRecorder();

    const [transcript, setTranscript] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [showPopup, setShowPopup] = useState(false);
    const [acceptedTranscript, setAcceptedTranscript] = useState('');

    async function handleStartRecording() {
        setTranscript('');
        setAcceptedTranscript('');
        setError(null);
        await startRecording();
    }

    async function handleStopRecording() {
        try {
            const audioBlob = await stopRecording();
            setIsLoading(true);
            const text = await transcribeAudio(audioBlob);
            setTranscript(text);
            setShowPopup(true);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Transcription failed');
        } finally {
            setIsLoading(false);
        }
    }

    function handleAccept(editedTranscript: string) {
        setAcceptedTranscript(editedTranscript);
        setTranscript('');
        setShowPopup(false);
    }

    function handleReject() {
        setTranscript('');
        setShowPopup(false);
    }

    async function handleRetry() {
        setShowPopup(false);
        setIsLoading(false);
        setError(null);
        await handleStartRecording();
    }

    const displayError = error || micError;

    return (
        <div className={styles.container}>
            <h1>Voize</h1>
            <p>Record your voice and get instant transcriptions</p>

            <div className={styles.controls}>
                {!isRecording && !showPopup && (
                    <button
                        onClick={handleStartRecording}
                        disabled={isRecording || isLoading}
                        className={`${styles.btn} ${styles.btnPrimary}`}
                    >
                        Start Recording
                    </button>
                )}
                {isRecording && (
                    <button
                        onClick={handleStopRecording}
                        disabled={isLoading}
                        className={`${styles.btn} ${styles.btnSecondary}`}
                    >
                        <SoundWaveAnimation />
                    </button>
                )}
            </div>

            {isLoading && (
                <div className={`${styles.status} ${styles.statusLoading}`}>
                    <div className={styles.spinner}></div>
                    <p>Transcribing audio...</p>
                </div>
            )}

            {displayError && (
                <div className={`${styles.status} ${styles.statusError}`}>
                    <p>Error: {displayError}</p>
                </div>
            )}

            {acceptedTranscript && (
                <div className={styles.transcriptContainer}>
                    <h2>Transcript</h2>
                    <p className={styles.transcript}>{acceptedTranscript}</p>
                </div>
            )}

            {showPopup && (
                <TranscriptModal
                    transcript={transcript}
                    onAccept={handleAccept}
                    onReject={handleReject}
                    onRetry={handleRetry}
                />
            )}
        </div>
    );
}

