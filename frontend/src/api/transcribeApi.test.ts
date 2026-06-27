import { describe, it, expect, vi } from 'vitest';
import { sendAudioForTranscription } from './transcribeApi';

describe('sendAudioForTranscription', () => {
  it('returns parsed JSON on successful response', async () => {
    const mockResponse = { status: 'success', text: 'hello world' };
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify(mockResponse), { status: 200 }),
    );

    const blob = new Blob(['fake audio data'], { type: 'audio/wav' });
    const result = await sendAudioForTranscription(blob);

    expect(result).toEqual(mockResponse);
    expect(globalThis.fetch).toHaveBeenCalledWith('/api/v1/transcribe', {
      method: 'POST',
      body: expect.any(FormData),
    });
  });

  it('throws on HTTP error status', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response('Internal Server Error', { status: 500 }),
    );

    const blob = new Blob(['fake'], { type: 'audio/wav' });

    await expect(sendAudioForTranscription(blob)).rejects.toThrow(
      'HTTP error! status: 500',
    );
  });

  it('throws on network failure', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValueOnce(
      new Error('Network error'),
    );

    const blob = new Blob(['fake'], { type: 'audio/wav' });

    await expect(sendAudioForTranscription(blob)).rejects.toThrow(
      'Network error',
    );
  });
});
