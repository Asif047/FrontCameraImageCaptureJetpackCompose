package com.asif047.frontcameraimagecapturejetpackcompose.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.io.File

@Composable
fun UploadImageScreen(navController: NavController) {
    // Mutable state to hold the captured image path
    var capturedImagePath by remember { mutableStateOf<String?>(null) }

    // Observe the SavedStateHandle for the "capturedImagePath"
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    savedStateHandle?.getStateFlow<String?>("capturedImagePath", null)?.collectAsState()?.value?.let { path ->
        if (path != capturedImagePath) {
            capturedImagePath = path // Update the local state when the path changes
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Image Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clickable { navController.navigate("circular_camera_app") }
        ) {
            if (capturedImagePath == null) {
                // Placeholder image
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(text = "Tap to capture image")
                }
            } else {
                // Display captured image
                val bitmap = BitmapFactory.decodeFile(capturedImagePath)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Upload Button
        Button(
            onClick = {
                // TODO: Trigger upload logic using the capturedImagePath
            },
            enabled = capturedImagePath != null
        ) {
            Text(text = "Upload Image")
        }
    }
}

