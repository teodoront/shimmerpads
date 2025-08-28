package br.com.neto.shimmerpads.audio

import android.content.Context
import android.media.SoundPool

class AssetsSoundPool(
    private val context: Context,
    private val basePath: String,                    // ex.: "libraries/analog"
    private val keyToFile: Map<String, String>       // ex.: "C#4" -> "cs4.wav"
) {
    private val pool = SoundPool.Builder().setMaxStreams(16).build()
    private val soundIds = mutableMapOf<String, Int>()

    fun loadAll() {
        keyToFile.forEach { (keyId, fileName) ->
            try {
                context.assets.openFd("$basePath/$fileName").use { afd ->
                    soundIds[keyId] = pool.load(afd, 1)
                }
            } catch (_: Exception) {
                // arquivo não encontrado? ignora a tecla
            }
        }
    }

    fun play(keyId: String, volume: Float = 1f) {
        soundIds[keyId]?.let { pool.play(it, volume, volume, 1, 0, 1f) }
    }

    fun release() = pool.release()
}
