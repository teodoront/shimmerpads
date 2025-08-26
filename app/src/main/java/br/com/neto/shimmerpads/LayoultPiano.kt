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
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
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
    PianoKey("C4", false, "c4"),
    PianoKey("C#4", true, "cs4"),
    PianoKey("D4", false, "d4"),
    PianoKey("D#4", true, "ds4"),
    PianoKey("E4", false, "e4"),
    PianoKey("F4", false, "f4"),
    PianoKey("F#4", true, "fs4"),
    PianoKey("G4", false, "g4"),
    PianoKey("G#4", true, "gs4"),
    PianoKey("A4", false, "a4"),
    PianoKey("A#4", true, "as4"),
    PianoKey("B4", false, "b4"),
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
    leftBarColor: Color = Color(0xFF8E44AD),
    onKeyPressed: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    val piano = remember { PianoSoundPool(ctx, octaveC4toB4) }
    LaunchedEffect(Unit) { piano.loadAll() }
    DisposableEffect(Unit) { onDispose { piano.release() } }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        val totalWhite = octaveC4toB4.count { !it.isBlack } // 7
        val whiteAreaWidth = maxWidth - 22.dp
        val whiteKeyHeight = (maxHeight - 24.dp) / totalWhite
        val inset = 10.dp // respiro interno do “módulo teclado”

        Row(Modifier.fillMaxSize()) {

            // Barra roxa fixa (com cantos arredondados sutis)
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(leftBarColor, leftBarColor.copy(alpha = .9f))
                        )
                    )
                    .shadow(4.dp, RoundedCornerShape(8.dp))
            )

            Spacer(Modifier.width(12.dp))

            // "Módulo" do teclado com moldura + sombra
            Box(
                modifier = Modifier
                    .width(whiteAreaWidth)
                    .fillMaxHeight()
                    .shadow(10.dp, RoundedCornerShape(14.dp))
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF9F9F9))
                    .border(2.dp, Color(0xFF1D1D1D), RoundedCornerShape(14.dp))
                    .padding(inset)
            ) {
                // Camada 1: teclas brancas (faixas)
                Column(Modifier.fillMaxSize()) {
                    octaveC4toB4.filter { !it.isBlack }.forEach { key ->
                        WhiteKeyStyled(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(whiteKeyHeight),
                            onPress = {
                                piano.play(key.id)
                                onKeyPressed(key.id)
                            }
                        )
                    }
                }

                // Camada 2: teclas pretas sobrepostas (posicionadas entre as brancas)
                val blackKeyHeight = whiteKeyHeight * 0.64f
                val blackKeyWidth = whiteAreaWidth * 0.64f

                // Índices corretos no layout vertical (entre C-D, D-E, F-G, G-A, A-B)
                val blackBoundaries = listOf(
                    "C#4" to 1,
                    "D#4" to 2,
                    "F#4" to 3, // <- ajuste que você fez
                    "G#4" to 5,
                    "A#4" to 6
                )

                blackBoundaries.forEach { (id, boundaryIndex) ->
                    BlackKeyStyled(
                        modifier = Modifier
                            .offset(
                                x = 0.dp,
                                y = whiteKeyHeight * boundaryIndex - (blackKeyHeight / 2)
                            )
                            .size(blackKeyWidth, blackKeyHeight),
                        onPress = {
                            piano.play(id)
                            onKeyPressed(id)
                        }
                    )
                }
            }
        }
    }
}

// ------ Tecla branca estilizada ------
@Composable
private fun WhiteKeyStyled(
    modifier: Modifier,
    onPress: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    if (!pressed)
                        listOf(Color(0xFFFFFFFF), Color(0xFFF1F1F1), Color(0xFFE7E7E7))
                    else
                        listOf(Color(0xFFEDEDED), Color(0xFFE4E4E4), Color(0xFFDADADA))
                )
            )
            .border(1.dp, Color(0xFF2B2B2B), shape)
            .shadow(if (pressed) 1.dp else 3.dp, shape)
            .clickable(interactionSource = interaction, indication = null) { onPress() }
    )
}

// ------ Tecla preta estilizada ------
@Composable
private fun BlackKeyStyled(
    modifier: Modifier,
    onPress: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(6.dp)

    Box(
        contentAlignment = Alignment.Center, // ✅ isso é do Box
        modifier = modifier
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    if (!pressed)
                        listOf(Color(0xFF2F2F2F), Color(0xFF0E0E0E))
                    else
                        listOf(Color(0xFF3A3A3A), Color(0xFF1A1A1A))
                )
            )
            .border(1.dp, Color.Black, shape)
            .shadow(if (pressed) 4.dp else 8.dp, shape)
            .clickable(interactionSource = interaction, indication = null) { onPress() }
    ) {
        // aqui dentro você pode colocar algo, tipo highlight ou símbolo
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
