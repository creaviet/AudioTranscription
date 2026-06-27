import { describe, it, expect, vi } from 'vitest';
import { transcribeAudio } from './transcribeService';
import { sendAudioForTranscription } from '../api/transcribeApi';

vi.mock('../api/transcribeApi');

describe('transcribeAudio', () => {
  it('returns text when status is success', async () => {
    vi.mocked(sendAudioForTranscription).mockResolvedValueOnce({
      status: 'success',
      text: 'hello world',
    });

    const result = await transcribeAudio(new Blob());

    expect(result).toBe('hello world');
  });

  it('throws when status is not success', async () => {
    vi.mocked(sendAudioForTranscription).mockResolvedValueOnce({
      status: 'error',
      text: 'Something went wrong',
    });

    await expect(transcribeAudio(new Blob())).rejects.toThrow(
      'Something went wrong',
    );
  });

  it('throws generic error when transcript is missing', async () => {
    vi.mocked(sendAudioForTranscription).mockResolvedValueOnce({
      status: 'error',
      text: '',
    });

    await expect(transcribeAudio(new Blob())).rejects.toThrow(
      'Unknown error occurred',
    );
  });

  it('re-throws when the API call fails', async () => {
    vi.mocked(sendAudioForTranscription).mockRejectedValueOnce(
      new Error('Network failure'),
    );

    await expect(transcribeAudio(new Blob())).rejects.toThrow(
      'Network failure',
    );
  });
});
