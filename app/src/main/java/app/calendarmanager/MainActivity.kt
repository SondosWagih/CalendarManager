package app.calendarmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import app.calendarmanager.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as CalendarApp).container
        setContent {
            AppTheme {
                Surface {
                    // Phase 1 placeholder: proves DB + device id work. Real navigation arrives in Phase 2.
                    val id by produceState("…") { value = container.deviceIdProvider.getOrCreate() }
                    Text("Device: $id")
                }
            }
        }
    }
}
