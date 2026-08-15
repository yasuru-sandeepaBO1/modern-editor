package com.example.modern_editor.ui.components

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.modern_editor.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.PrimaryText

private data class DrawerInfoItem(val title: String, val body: String)

/** Slides in from the LEFT edge — opened by Home's hamburger icon. */
@Composable
fun AppDrawer(
    open: Boolean,
    onDismiss: () -> Unit,
    onSettings: () -> Unit
) {
    var infoDialog by remember { mutableStateOf<DrawerInfoItem?>(null) }

    BackHandler(enabled = open, onBack = onDismiss)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        AnimatedVisibility(visible = open, enter = fadeIn(), exit = fadeOut()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(onClick = onDismiss)
            )
        }

        AnimatedVisibility(
            visible = open,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(HeaderSurface)
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo_dark),
                        contentDescription = "Kotlin++ Lite Logo",
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(text = "Kotlin++ Lite", color = PrimaryText, fontWeight = FontWeight.Bold)
                    Text(text = "v1.0.0", color = ButtonText, fontSize = 10.sp)
                }
                HorizontalDivider(color = InactiveSurface)
                Spacer(Modifier.height(8.dp))
                MenuRow(icon = Icons.Filled.Settings, label = "Settings") {
                    onDismiss()
                    onSettings()
                }
                HorizontalDivider(color = InactiveSurface, modifier = Modifier.padding(vertical = 8.dp))
                MenuRow(icon = Icons.Filled.Info, label = "About") {
                    infoDialog = DrawerInfoItem(
                        "About",
                        "Kotlin++ Lite v1.0.0 — a native Android text editor for Kotlin and Markdown."
                    )
                }
                MenuRow(icon = Icons.Filled.Shield, label = "Privacy Policy") {
                    infoDialog = DrawerInfoItem(
                        "Privacy Policy",
                        "Kotlin++ Lite stores files only where you choose to save them. No data is collected or transmitted."
                    )
                }
                MenuRow(icon = Icons.Filled.Mail, label = "Contact Us") {
                    infoDialog = DrawerInfoItem("Contact Us", "Reach out at support@kotlineditor.app.")
                }
                HorizontalDivider(color = InactiveSurface, modifier = Modifier.padding(vertical = 8.dp))
                ExitRow(onDismiss = onDismiss)
            }
        }
    }

    infoDialog?.let { item ->
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            containerColor = HeaderSurface,
            titleContentColor = PrimaryText,
            textContentColor = ButtonText,
            title = { Text(item.title) },
            text = { Text(item.body) },
            confirmButton = {
                TextButton(
                    onClick = { infoDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = PrimaryText)
                ) { Text("Close") }
            }
        )
    }
}

@Composable
private fun ExitRow(onDismiss: () -> Unit) {
    val context = LocalContext.current
    MenuRow(icon = Icons.AutoMirrored.Filled.Logout, label = "Exit") {
        onDismiss()
        (context as? Activity)?.finishAffinity()
    }
}
