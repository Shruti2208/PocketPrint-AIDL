package dev.pocketprint.station

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.pocketprint.ui.Eyebrow
import dev.pocketprint.ui.Green
import dev.pocketprint.ui.Muted
import dev.pocketprint.ui.Page
import dev.pocketprint.ui.Panel
import dev.pocketprint.ui.PrintTheme
import dev.pocketprint.ui.Receipt

class MainActivity : ComponentActivity() {
    private var preview by mutableStateOf("")
    private val preferences by lazy { getSharedPreferences("station", MODE_PRIVATE) }
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> refresh() }
    private fun refresh() {
        preview = preferences.getString("preview", "").orEmpty()
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state); enableEdgeToEdge(); refresh()
        setContent {
            PrintTheme {
                Page("STATION", "Small notes.\nMade tangible.",
                    "Your companion station for a little everyday print.") {
                    Panel {
                        Eyebrow("PRINT STATION")
                        Text("●  Available on this device", color = Green,
                            style = MaterialTheme.typography.titleMedium)
                        Text("Connect from Pocket Notes to send your next print.", color = Muted,
                            style = MaterialTheme.typography.bodyMedium)
                        HorizontalDivider()
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("MODE", style = MaterialTheme.typography.labelSmall, color = Muted)
                            Text("Simulated printing", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Eyebrow("LATEST PRINT")
                        if (preview.isEmpty()) Panel {
                            Text("Your first print starts here.",
                                style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Open Pocket Notes, connect to this station and send a note. Your finished print will appear here.",
                                color = Muted)
                        } else Receipt(preview)
                        OutlinedButton(onClick = { refresh() },
                            modifier = Modifier.fillMaxWidth()) { Text("Refresh last print") }
                    }
                    Panel {
                        Eyebrow("FROM NOTE TO PAPER")
                        Text("01   Write in Pocket Notes",
                            style = MaterialTheme.typography.bodyMedium)
                        Text("02   Send to this station",
                            style = MaterialTheme.typography.bodyMedium)
                        Text("03   Watch your print arrive",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart(); refresh(); preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onStop() {
        preferences.unregisterOnSharedPreferenceChangeListener(listener); super.onStop()
    }
}
