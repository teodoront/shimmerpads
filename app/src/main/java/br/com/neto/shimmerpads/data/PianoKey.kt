package br.com.neto.shimmerpads.data

data class PianoKey(
    val id: String,          // "C4", "C#4", etc
    val isBlack: Boolean,
    val rawResName: String   // nome do arquivo em res/raw sem extensão, ex: "c4"
)
