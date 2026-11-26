package app.switchback

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.switchback.ui.SwitchbackApp
import app.switchback.ui.theme.SwitchbackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwitchbackTheme {
                SwitchbackApp()
            }
        }
    }
}
