package com.example.modern_editor.ui.screens.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modern_editor.ui.theme.ButtonText
import com.example.modern_editor.ui.theme.EditorSurface
import com.example.modern_editor.ui.theme.PrimaryText
import com.example.modern_editor.ui.theme.ScreenBackground
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(onLoaded: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(800)
        onLoaded()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(EditorSurface, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "K",
                        color = PrimaryText,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Kotlin Editor",
                    color = PrimaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            CircularProgressIndicator(color = ButtonText, modifier = Modifier.size(32.dp))
        }
    }
}
