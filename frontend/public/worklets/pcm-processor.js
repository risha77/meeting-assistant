/**
 * pcm-processor.js
 * AudioWorkletProcessor that converts Float32 audio frames to PCM16
 * and posts them to the main thread for WebSocket transmission.
 * Loaded via AudioContext.audioWorklet.addModule('/worklets/pcm-processor.js')
 */
class PcmProcessor extends AudioWorkletProcessor {
  process(inputs) {
    const input = inputs[0];
    if (!input || !input[0]) return true;

    const float32 = input[0];
    const int16 = new Int16Array(float32.length);

    for (let i = 0; i < float32.length; i++) {
      const s = Math.max(-1, Math.min(1, float32[i]));
      int16[i] = s < 0 ? s * 0x8000 : s * 0x7fff;
    }

    // Transfer buffer to main thread (zero-copy)
    this.port.postMessage(int16.buffer, [int16.buffer]);
    return true;
  }
}

registerProcessor('pcm-processor', PcmProcessor);
