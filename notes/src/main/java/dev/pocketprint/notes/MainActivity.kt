package dev.pocketprint.notes

import android.content.*
import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.pocketprint.contract.*
import dev.pocketprint.ui.*
import java.util.UUID
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    private val calls = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private var station: IPrintStation? = null
    private var bound = false
    private var active = false
    private var generation = 0
    private var connected by mutableStateOf(false)
    private var connecting by mutableStateOf(false)
    private var printProgress by mutableIntStateOf(0)
    private var printing by mutableStateOf(false)
    private var preview by mutableStateOf("")
    private var status by mutableStateOf("Connect to your print station to get started.")
    private val events = mutableStateListOf<String>()
    private fun event(text: String) {
        status = text; events.add(0, text); if (events.size > 8) events.removeAt(8)
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName,
            binder: IBinder) {
            station = IPrintStation.Stub.asInterface(binder); connected = true; connecting = false
            event("Connected · ready to print")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            station = null; connected = false; printing = false; event(
                "Connection lost · waiting for station")
        }

        override fun onBindingDied(name: ComponentName) {
            disconnect(); event("Station changed · reconnect to continue")
        }

        override fun onNullBinding(name: ComponentName) {
            disconnect(); event("Station unavailable")
        }
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state); enableEdgeToEdge()
        setContent {
            PrintTheme {
                var note by rememberSaveable {
                    mutableStateOf(
                        "Good ideas deserve a little paper.\n\nMake something worth sharing.")
                }
                Page("CLIENT", "A note worth printing.",
                    "Write here. Let your station take it from there.") {
                    Panel {
                        Eyebrow("YOUR CONNECTION")
                        Text(
                            if (connected) "●  Station connected" else if (connecting) "◌  Connecting…" else "○  Station offline",
                            color = if (connected) Green else Muted,
                            style = MaterialTheme.typography.titleMedium)
                        Text("Pocket Notes → Print Station", color = Muted,
                            style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = { if (bound) disconnect() else connect() },
                            modifier = Modifier.fillMaxWidth()) {
                            Text(if (bound) "Disconnect" else "Connect to station")
                        }
                    }
                    Panel {
                        Eyebrow("NEW NOTE")
                        OutlinedTextField(value = note, onValueChange = { note = it },
                            modifier = Modifier.fillMaxWidth(), minLines = 3,
                            placeholder = { Text("What would you like to print?") },
                            supportingText = { Text("${note.length} / 2,000 characters") })
                        Button(onClick = { send(note) }, enabled = connected && !printing,
                            modifier = Modifier
                             .fillMaxWidth()
                             .height(50.dp)) {
                            Text(if (printing) "Printing · $printProgress%" else "Print note  ↗")
                        }
                        if (printing) LinearProgressIndicator(progress = printProgress / 100f,
                            modifier = Modifier.fillMaxWidth())
                        Text(status, color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    if (preview.isNotEmpty()) Receipt(preview)
                    if (events.isNotEmpty()) Panel {
                        Eyebrow("ACTIVITY")
                        events.take(4).forEach {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = Muted)
                        }
                    }
                }
            }
        }
    }

    private fun connect() {
        if (bound) return
        try {
            bound = bindService(Intent().setComponent(
                ComponentName("dev.pocketprint.station", "dev.pocketprint.station.PrintService")),
                connection, BIND_AUTO_CREATE)
            connecting = bound; event(
                if (bound) "Finding your station…" else "Install Print Station, then connect again.")
        } catch (e: SecurityException) {
            event("Permission denied · both apps need the same signing certificate")
        }
    }

    private fun send(text: String) {
        val remote = station ?: return
        val session = generation
        val id = UUID.randomUUID().toString()
        printing = true; printProgress = 0; preview = ""
        fun update(block: () -> Unit) {
            main.post { if (active && session == generation) block() }
        }

        val callback = object : IPrintCallback.Stub() {
            override fun onProgress(jobId: String?,
                percent: Int) {
                update { printProgress = percent; status = "Printing your note · $percent%" }
            }

            override fun onCompleted(jobId: String?,
                result: String?) {
                update {
                    printing = false; preview = result.orEmpty(); event(
                    "Print complete · ${id.take(8)}")
                }
            }
        }
        calls.execute {
            try {
                if (remote.protocolVersion != 1) {
                    update {
                        printing = false; event("Unsupported station version")
                    }; return@execute
                }
                val result = remote.submit(id, text, callback)
                update {
                    if (result == 0) event("Job accepted · ${id.take(8)}") else {
                        printing = false; event(
                            if (result == 1) "Use between 1 and 2,000 characters." else "Station busy · try again shortly")
                    }
                }
            } catch (e: RemoteException) {
                update { disconnect(); event("Connection interrupted · reconnect to try again") }
            }
        }
    }

    private fun disconnect() {
        generation++; station = null; connected = false; connecting = false; printing = false
        if (bound) {
            unbindService(connection); bound = false
        }
        status = "Disconnected · connect when you’re ready"
    }

    override fun onStart() {
        super.onStart(); active = true
    }

    override fun onStop() {
        active = false; disconnect(); super.onStop()
    }

    override fun onDestroy() {
        calls.shutdownNow(); super.onDestroy()
    }
}
