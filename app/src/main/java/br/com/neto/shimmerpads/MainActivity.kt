package br.com.neto.shimmerpads

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import br.com.neto.shimmerpads.ui.theme.ShimmerpadsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShimmerpadsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PianoScreenVertical(
                        modifier = Modifier.padding(innerPadding),
                        onKeyPressed = { keyId ->
                            println("Tecla pressionada: $keyId")
                        }
                    )
                }
            }
        }
    }
}
