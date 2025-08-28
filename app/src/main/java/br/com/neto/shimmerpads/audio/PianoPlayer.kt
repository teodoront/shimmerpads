package br.com.neto.shimmerpads.audio

import br.com.neto.shimmerpads.data.LoopInfo

interface PianoPlayer {
    suspend fun loadPack(packName: String, keyToAsset: Map<String, LoopInfo>)
    fun noteOn(keyId: String, velocity: Float = 1f)
    fun noteOff(keyId: String)
    fun release()
}