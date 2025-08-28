package br.com.neto.shimmerpads

import android.content.Context
import android.media.*
import br.com.neto.shimmerpads.audio.PianoPlayer
import br.com.neto.shimmerpads.data.LoopInfo
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

class AudioTrackPianoPlayer(
    private val context: Context
) : PianoPlayer {

    private data class Voice(
        val track: AudioTrack,
        val sampleRate: Int,
        val loopStartFrames: Int,
        val loopEndFrames: Int,
        var currentVolume: Float = 0f        // <- guarda o volume atual
    )

    private val voices = mutableMapOf<String, Voice>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun loadPack(
        packName: String,
        keyToAsset: Map<String, LoopInfo>
    ) = withContext(Dispatchers.IO) {
        release() // limpa anterior
        keyToAsset.forEach { (keyId, info) ->
            val afd = context.assets.openFd(info.assetPath)
            val input = afd.createInputStream().buffered()
            val wav = WavReader.readPcm16(input) // -> data: ShortArray, sr, ch
            afd.close()

            val numChannels = wav.channels
            val format = AudioFormat.Builder()
                .setSampleRate(wav.sampleRate)
                .setChannelMask(
                    if (numChannels == 1) AudioFormat.CHANNEL_OUT_MONO
                    else AudioFormat.CHANNEL_OUT_STEREO
                )
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            val minSize = AudioTrack.getMinBufferSize(
                wav.sampleRate,
                if (numChannels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(format)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(max(minSize, wav.pcm.size * 2))
                .build()

            val bb = ByteBuffer.allocateDirect(wav.pcm.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            bb.asShortBuffer().put(wav.pcm)
            track.write(bb, bb.capacity(), AudioTrack.WRITE_BLOCKING)

            val startFrames = msToFrames(info.loopStartMs, wav.sampleRate)
            val endFrames   = msToFrames(info.loopEndMs.takeIf { it > 0 } ?: wav.durationMs, wav.sampleRate)
            track.setLoopPoints(startFrames, endFrames, -1)

            track.setVolume(0f)

            voices[keyId] = Voice(track, wav.sampleRate, startFrames, endFrames, currentVolume = 0f)
        }
    }

    override fun noteOn(keyId: String, velocity: Float) {
        val v = voices[keyId] ?: return
        v.track.stop()
        v.track.reloadStaticData()
        v.track.play()
        fadeTo(v, target = velocity.coerceIn(0f, 1f), durationMs = 60)
    }

    override fun noteOff(keyId: String) {
        val v = voices[keyId] ?: return
        fadeTo(v, target = 0f, durationMs = 80) {
            v.track.pause()
            v.track.flush()
        }
    }

    override fun release() {
        // cancela fades pendentes e libera tracks
        scope.coroutineContext.cancelChildren()
        voices.values.forEach { it.track.release() }
        voices.clear()
    }

    // util
    private fun msToFrames(ms: Int, sr: Int) = ((ms / 1000.0) * sr).toInt()

    private fun fadeTo(
        voice: Voice,
        target: Float,
        durationMs: Int,
        end: (() -> Unit)? = null
    ) {
        val steps = 12
        val start = voice.currentVolume
        val delta = (target - start) / steps

        scope.launch {
            repeat(steps) { i ->
                val newVol = (start + delta * i).coerceIn(0f, 1f)
                voice.track.setVolume(newVol)
                voice.currentVolume = newVol
                delay((durationMs / steps).toLong())
            }
            voice.track.setVolume(target)
            voice.currentVolume = target
            end?.invoke()
        }
    }
}
