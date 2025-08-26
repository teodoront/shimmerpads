package br.com.neto.shimmerpads


import android.content.Context
import android.media.SoundPool
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

// -------------------------------
// Modelo
// -------------------------------
private data class PianoKey(
    val id: String,          // "C4", "C#4", etc
    val isBlack: Boolean,
    val rawResName: String   // nome do arquivo em res/raw sem extensão, ex: "c4"
)

// Oitava de C4 a B4
private val octaveC4toB4 = listOf(
    PianoKey("C4",  false, "c4"),
    PianoKey("C#4", true,  "cs4"),
    PianoKey("D4",  false, "d4"),
    PianoKey("D#4", true,  "ds4"),
    PianoKey("E4",  false, "e4"),
    PianoKey("F4",  false, "f4"),
    PianoKey("F#4", true,  "fs4"),
    PianoKey("G4",  false, "g4"),
    PianoKey("G#4", true,  "gs4"),
    PianoKey("A4",  false, "a4"),
    PianoKey("A#4", true,  "as4"),
    PianoKey("B4",  false, "b4"),
)

// -------------------------------
// SoundPool helper
// -------------------------------
private class PianoSoundPool(
    private val context: Context,
    private val keys: List<PianoKey>
) {
    private val soundPool = SoundPool.Builder().setMaxStreams(12).build()
    private val soundIds = mutableMapOf<String, Int>() // id -> soundId

    suspend fun loadAll() = withContext(Dispatchers.IO) {
        keys.forEach { key ->
            val resId = context.resources.getIdentifier(key.rawResName, "raw", context.packageName)
            if (resId != 0) {
                val soundId = soundPool.load(context, resId, 1)
                soundIds[key.id] = soundId
            }
        }
    }

    fun play(keyId: String, volume: Float = 1f) {
        val soundId = soundIds[keyId] ?: return
        // leftVolume, rightVolume, priority, loop, rate
        soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}

// -------------------------------
// UI principal
// -------------------------------
@Composable
fun PianoScreenVertical(
    modifier: Modifier = Modifier,
    leftBarColor: Color = Color(0xFF8E44AD), // roxo da barra
    onKeyPressed: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    val piano = remember { PianoSoundPool(ctx, octaveC4toB4) }
    LaunchedEffect(Unit) { piano.loadAll() }
    DisposableEffect(Unit) { onDispose { piano.release() } }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(8.dp)
    ) {
        val totalWhite = octaveC4toB4.count { !it.isBlack } // 7 brancas
        val whiteAreaWidth = maxWidth - 18.dp // largura útil das teclas (ajuste fino)
        val whiteKeyHeight = (maxHeight - 16.dp) / totalWhite

        Row(Modifier.fillMaxSize()) {

            // --- Barra roxa à esquerda ---
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .fillMaxHeight()
                    .background(leftBarColor)
            )

            // --- Área das teclas com moldura preta grossa (como na imagem) ---
            Box(
                modifier = Modifier
                    .width(whiteAreaWidth)
                    .fillMaxHeight()
                    .padding(start = 6.dp, end = 2.dp)
                    .border(3.dp, Color.Black) // moldura externa
            ) {
                // Camada 1: teclas brancas (faixas horizontais)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    octaveC4toB4.filter { !it.isBlack }.forEach { key ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(whiteKeyHeight)
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White, Color(0xFFF1F1F1))
                                    )
                                )
                                .border(2.dp, Color(0xFF2A2A2A), RoundedCornerShape(2.dp))
                                .clickable { piano.play(key.id); onKeyPressed(key.id) }
                        )
                    }
                }

                // Camada 2: teclas pretas (sobrepostas da esquerda para a direita)
                // Posições relativas entre as brancas (em índices verticais):
                // C# entre C(0) e D(1): ~0.66 ; D# ~1.5 ; F# ~3.66 ; G# ~4.5 ; A# ~5.33
                val blackKeyHeight = whiteKeyHeight * 0.60f
                val blackKeyWidth = whiteAreaWidth * 0.68f // comprimento para dentro do branco
                val blackOffsets = listOf(
                    "C#4" to 0.66f,
                    "D#4" to 1.50f,
                    "F#4" to 3.66f,
                    "G#4" to 4.50f,
                    "A#4" to 5.33f,
                )

                blackOffsets.forEach { (id, relIndex) ->
                    val key = octaveC4toB4.first { it.id == id }
                    Box(
                        modifier = Modifier
                            .offset(
                                // parte preta sai da esquerda (dentro da moldura)
                                x = 6.dp, // pequeno recuo para a borda interna
                                // centraliza a tecla preta entre as brancas correspondentes
                                y = (whiteKeyHeight * relIndex) - (blackKeyHeight / 2)
                            )
                            .size(blackKeyWidth, blackKeyHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF111111), Color(0xFF2E2E2E))
                                )
                            )
                            .border(1.dp, Color.Black, RoundedCornerShape(2.dp))
                            .clickable { piano.play(key.id); onKeyPressed(key.id) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PianoScreenPreview() {
    MaterialTheme {
        PianoScreenVertical(
            onKeyPressed = { /* no-op para preview */ }
        )
    }
}
