package com.example.modern_editor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.GutterText
import com.example.modern_editor.ui.theme.PrimaryText

/**
 * Shared icon+label row used by AppDrawer, FileMenu, and OptionsMenu overlays.
 */
@Composable
fun MenuRow(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) ButtonText else GutterText
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            color = if (enabled) PrimaryText else GutterText,
            fontWeight = FontWeight.Medium
        )
    }
}
