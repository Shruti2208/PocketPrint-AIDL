package dev.pocketprint.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

val Ink = Color(0xFF202744)
val Accent = Color(0xFF5059BB)
val Paper = Color(0xFFF6F4EF)
val Muted = Color(0xFF727589)
val Green = Color(0xFF26745B)

@Composable fun PrintTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Accent, background = Paper, surface = Color.White,
            onSurface = Ink, onBackground = Ink), content = content)
}

@Composable fun Page(label: String,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Paper, modifier = Modifier.fillMaxSize()) {
        Column(Modifier
         .safeDrawingPadding()
         .imePadding()
         .verticalScroll(rememberScrollState())
         .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("◈  POCKET PRINT    /    $label", color = Accent,
                style = MaterialTheme.typography.labelMedium)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.headlineLarge, color = Ink)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
            content()
            Text("TWO APPS. ONE CONNECTION.", color = Muted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
        }
    }
}

@Composable fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
        Column(Modifier
         .fillMaxWidth()
         .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable fun Eyebrow(text: String) {
    Text(text, color = Muted, style = MaterialTheme.typography.labelMedium)
}

@Composable fun Receipt(text: String) {
    Column(Modifier
     .fillMaxWidth()
     .background(Color(0xFFFFF9EB), RoundedCornerShape(16.dp))
     .padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("PRINT PREVIEW", color = Color(0xFF996A27),
            style = MaterialTheme.typography.labelSmall)
        Text(text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall,
            color = Ink)
    }
}
