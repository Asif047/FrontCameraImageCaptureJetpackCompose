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
import com.asif047.frontcameraimagecapturejetpackcompose.viewmodel.UploadViewModel
import java.io.File

@Composable
fun UploadImageScreen(navController: NavController, uploadViewModel: UploadViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    // Mutable state to hold the captured image path
    var capturedImagePath by remember { mutableStateOf<String?>(null) }

    // Mutable state for UI feedback
    var isUploading by remember { mutableStateOf(false) }
    var uploadErrorMessage by remember { mutableStateOf<String?>(null) }

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

        // Show upload error message if any
        uploadErrorMessage?.let {
            Text(
                text = "Error: $it",
                color = androidx.compose.ui.graphics.Color.Red
            )
        }

        // Upload Button
        Button(
            onClick = {
                capturedImagePath?.let { path ->
                    isUploading = true
                    uploadViewModel.uploadImage(
                        apiKey = "c76b89c86f05efb767fb03a226cbefb5", // Replace with actual API key
                        imageFile = File(path),
                        onSuccess = {
                            isUploading = false
                            //navController.navigate("upload_success_screen") // Navigate on success
                        },
                        onError = { errorMessage ->
                            isUploading = false
                            uploadErrorMessage = errorMessage
                        }
                    )
                }
            },
            enabled = capturedImagePath != null && !isUploading
        ) {
            Text(text = if (isUploading) "Uploading..." else "Upload Image")
        }
    }
}


