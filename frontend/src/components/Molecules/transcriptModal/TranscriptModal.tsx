import React, { useState } from "react";
import styles from './TranscriptModal.module.scss';

interface TranscriptModalProps {
    transcript: string;
    onAccept: (editedTranscript: string) => void;
    onReject: () => void;
    onRetry: () => void;
}

export default function TranscriptModal({ transcript, onAccept, onReject, onRetry }: TranscriptModalProps): React.JSX.Element {
    const [isEditing, setIsEditing] = useState(false);
    const [editedText, setEditedText] = useState(transcript);

    function handleAccept() {
        onAccept(isEditing ? editedText : transcript);
    }

    function handleEditToggle() {
        if (isEditing) {
            setEditedText(transcript);
        }
        setIsEditing((prev) => !prev);
    }

    return (
        <div className={styles.modalOverlay}>
            <div className={styles.modalCard}>
                <h2>Transcript Review</h2>

                {isEditing ? (
                    <textarea
                        className={styles.modalTextarea}
                        value={editedText}
                        onChange={(e) => setEditedText(e.target.value)}
                        rows={5}
                    />
                ) : (
                    <p className={styles.modalTranscript}>{transcript}</p>
                )}

                <div className={styles.modalActions}>
                    <button className={styles.btnAccept} onClick={handleAccept}>
                        {isEditing ? 'Save' : 'Accept'}
                    </button>
                    <button className={styles.btnReject} onClick={onReject}>
                        Reject
                    </button>
                    <button className={styles.btnEdit} onClick={handleEditToggle}>
                        {isEditing ? 'Cancel' : 'Edit'}
                    </button>
                    <button className={styles.btnRetry} onClick={onRetry}>
                        Retry
                    </button>
                </div>
            </div>
        </div>
    );
}
