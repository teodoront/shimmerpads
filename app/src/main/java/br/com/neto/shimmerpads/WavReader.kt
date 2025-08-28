package br.com.neto.shimmerpads

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavReader {
    data class Wav(val pcm: ShortArray, val sampleRate: Int, val channels: Int, val durationMs: Int)

    fun readPcm16(input: InputStream): Wav {
        val bytes = input.readBytes()
        val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        require(bb.int == 0x46464952) { "Not RIFF" } // "RIFF"
        bb.int // file size
        require(bb.int == 0x45564157) { "Not WAVE" } // "WAVE"

        var fmtFound = false
        var dataPos = -1
        var dataLen = -1
        var sr = 44100
        var ch = 1
        var bits = 16

        while (bb.remaining() >= 8) {
            val id = bb.int
            val size = bb.int
            when (id) {
                0x20746D66 -> { // "fmt "
                    val audioFormat = bb.short.toInt() and 0xFFFF
                    ch = bb.short.toInt() and 0xFFFF
                    sr = bb.int
                    bb.int // byte rate
                    bb.short // block align
                    bits = bb.short.toInt() and 0xFFFF
                    if (size > 16) bb.position(bb.position() + (size - 16))
                    require(audioFormat == 1 && bits == 16) { "Only PCM16 supported" }
                    fmtFound = true
                }
                0x61746164 -> { // "data"
                    dataPos = bb.position()
                    dataLen = size
                    bb.position(bb.position() + size)
                }
                else -> bb.position(bb.position() + size)
            }
        }
        require(fmtFound && dataPos >= 0 && dataLen > 0)

        val samples = dataLen / 2
        val sb = ByteBuffer.wrap(bytes, dataPos, dataLen).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
        val pcm = ShortArray(samples)
        sb.get(pcm)
        val durationMs = (samples.toDouble() / (sr * ch) * 1000).toInt()
        return Wav(pcm, sr, ch, durationMs)
    }
}
