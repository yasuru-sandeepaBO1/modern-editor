package com.example.modern_editor.ui.components

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.R
import com.example.modern_editor.ui.theme.ButtonSurface
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.HeaderSurface
import com.example.modern_editor.ui.theme.InactiveSurface
import com.example.modern_editor.ui.theme.PrimaryText

private enum class DrawerDialog { ABOUT, PRIVACY, HELP }

/** Slides in from the LEFT edge — opened by Home's hamburger icon. */
@Composable
fun AppDrawer(
    open: Boolean,
    onDismiss: () -> Unit,
    onSettings: () -> Unit
) {
    var activeDialog by remember { mutableStateOf<DrawerDialog?>(null) }

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
                    activeDialog = DrawerDialog.ABOUT
                }
                MenuRow(icon = Icons.Filled.Shield, label = "Privacy Policy") {
                    activeDialog = DrawerDialog.PRIVACY
                }
                MenuRow(icon = Icons.Filled.HelpOutline, label = "Help") {
                    activeDialog = DrawerDialog.HELP
                }
                HorizontalDivider(color = InactiveSurface, modifier = Modifier.padding(vertical = 8.dp))
                ExitRow(onDismiss = onDismiss)
            }
        }
    }

    when (activeDialog) {
        DrawerDialog.ABOUT -> AboutDialog(onDismiss = { activeDialog = null })
        DrawerDialog.PRIVACY -> PrivacyPolicyDialog(onDismiss = { activeDialog = null })
        DrawerDialog.HELP -> HelpDialog(onDismiss = { activeDialog = null })
        null -> {}
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HeaderSurface,
        titleContentColor = PrimaryText,
        title = {
            Text("About", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_dark),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = PrimaryText, fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                            append("Kotlin")
                        }
                        withStyle(SpanStyle(color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                            append("++")
                        }
                        withStyle(SpanStyle(color = Color(0xFF06B6D4), fontWeight = FontWeight.Bold, fontSize = 22.sp)) {
                            append(" Lite")
                        }
                    },
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Version 1.0.0",
                    color = ButtonText,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "A native Android text editor built with Kotlin and Jetpack Compose, " +
                            "featuring syntax highlighting for Kotlin and Markdown, crash recovery, " +
                            "and delta-based version control.",
                    color = ButtonText,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = InactiveSurface)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Developed by",
                        color = ButtonText,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        text = "Yasuru Sandeepa",
                        color = PrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Induwara Uthsara",
                        color = PrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "© 2025 Kotlin++ Lite. All rights reserved.",
                    color = ButtonText.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryText)
            ) { Text("Close") }
        }
    )
}

@Composable
private fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HeaderSurface,
        titleContentColor = PrimaryText,
        title = {
            Text("Privacy Policy", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PolicySection(
                    title = "Data Storage",
                    body = "Kotlin++ Lite stores your files only in locations you explicitly choose on your device. " +
                            "No files or content are uploaded to external servers."
                )
                PolicySection(
                    title = "No Data Collection",
                    body = "We do not collect, track, or transmit any personal information, usage analytics, " +
                            "or telemetry data. The app operates entirely offline."
                )
                PolicySection(
                    title = "Crash Recovery",
                    body = "The crash recovery feature saves a local backup of your unsaved work to your device's " +
                            "app-private storage. This data never leaves your device and is automatically " +
                            "removed once you restore or discard it."
                )
                PolicySection(
                    title = "Version History",
                    body = "File version snapshots are stored locally on your device. They are not synced, " +
                            "shared, or backed up to any cloud service."
                )
                PolicySection(
                    title = "Permissions",
                    body = "The app requests file access permissions solely to open and save files you select. " +
                            "No other device permissions are required."
                )
                PolicySection(
                    title = "Third-Party Services",
                    body = "Kotlin++ Lite does not integrate with any third-party services, ad networks, " +
                            "or analytics platforms."
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Last updated: August 2025",
                    color = ButtonText.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryText)
            ) { Text("Close") }
        }
    )
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = PrimaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = body,
            color = ButtonText,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HeaderSurface,
        titleContentColor = PrimaryText,
        title = {
            Text("Help", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Feel free to watch our demo video and see all features in action.",
                    color = ButtonText,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
                Text(
                    text = "Thank you for using Kotlin++ Lite!",
                    color = PrimaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = PrimaryText)
            ) { Text("Close") }
        }
    )
}

@Composable
private fun ExitRow(onDismiss: () -> Unit) {
    val context = LocalContext.current
    MenuRow(icon = Icons.AutoMirrored.Filled.Logout, label = "Exit") {
        onDismiss()
        (context as? Activity)?.finishAffinity()
    }
}
