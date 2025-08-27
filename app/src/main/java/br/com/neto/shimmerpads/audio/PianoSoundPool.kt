package br.com.neto.shimmerpads.audio

import android.content.Context
import android.media.SoundPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * keyToResName: mapa do ID lógico da tecla -> nome do recurso em res/raw (sem extensão)
 * Ex.: "C4" -> "c4"
 */
class PianoSoundPool(
    private val context: Context,
    private val keyToResName: Map<String, String>
) {
    private val pool = SoundPool.Builder().setMaxStreams(12).build()
    private val soundIds = mutableMapOf<String, Int>() // keyId -> soundId

    suspend fun loadAll() = withContext(Dispatchers.IO) {
        keyToResName.forEach { (keyId, rawName) ->
            val resId = context.resources.getIdentifier(rawName, "raw", context.packageName)
            if (resId != 0) {
                soundIds[keyId] = pool.load(context, resId, 1)
            }
        }
    }

    fun play(keyId: String, volume: Float = 1f) {
        soundIds[keyId]?.let { pool.play(it, volume, volume, 1, 0, 1f) }
    }

    fun release() = pool.release()
}
