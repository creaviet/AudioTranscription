import React from "react";
import styles from './SoundWaveAnimation.module.scss';

export default function SoundWaveAnimation(): React.JSX.Element {
    return (
        <div className={styles.soundWave}>
            <span className={styles.bar} />
            <span className={styles.bar} />
            <span className={styles.bar} />
            <span className={styles.bar} />
            <span className={styles.bar} />
            <span className={styles.bar} />
            <span className={styles.bar} />
            <span className={styles.bar} />
        </div>
    );
}
