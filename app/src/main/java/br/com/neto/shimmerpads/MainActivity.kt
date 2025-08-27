package br.com.neto.shimmerpads

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.core.graphics.toColorInt
import br.com.neto.shimmerpads.ui.layoult.PianoScreenVertical
import br.com.neto.shimmerpads.ui.theme.ShimmerpadsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ícones BRANCOS (estilo "dark") nas duas barras:
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark("#121212".toColorInt()),
            navigationBarStyle = SystemBarStyle.dark("#121212".toColorInt())
        )

        setContent {
            ShimmerpadsTheme {
                androidx.compose.material3.Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    PianoScreenVertical(
                        modifier = Modifier.padding(innerPadding),
                        onKeyPressed = { keyId -> println("Tecla: $keyId") }
                    )
                }
            }
        }
    }
}
