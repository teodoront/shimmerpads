package br.com.neto.shimmerpads.data

data class LoopInfo(
    val assetPath: String,        // ex: "libraries/analog/c4.wav"
    val loopStartMs: Int,         // ponto de loop em ms (ou 0 se loopar o arquivo todo)
    val loopEndMs: Int
)
