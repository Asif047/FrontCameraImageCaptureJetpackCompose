package com.asif047.frontcameraimagecapturejetpackcompose.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.core.view.doOnLayout

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onCaptureImage: (Bitmap, File) -> Unit // Callback to handle captured image
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // State holders
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val outputDirectory = remember { context.cacheDir } // Save images in cache directory

    // `PreviewView` instance for displaying the camera feed
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    // Setup CameraX
    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()

        val previewUseCase = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageCaptureUseCase = ImageCapture.Builder()
            .setTargetRotation(Surface.ROTATION_0) // Default rotation
            .build()

        // Update rotation dynamically when PreviewView is ready
        previewView.doOnLayout {
            val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
            imageCaptureUseCase.targetRotation = rotation
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                previewUseCase,
                imageCaptureUseCase
            )
            preview = previewUseCase
            imageCapture = imageCaptureUseCase
        } catch (exc: Exception) {
            Log.e("CameraPreview", "Use case binding failed", exc)
        }
    }


    Box(modifier = modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { PreviewView(context) },
            modifier = Modifier.fillMaxSize()
        )

        // Circular overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = true
                    shape = androidx.compose.foundation.shape.CircleShape
                }
                .background(Color.Transparent.copy(alpha = 0.5f))
        )

        // Capture button
        androidx.compose.material3.Button(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val photoFile = File(outputDirectory, "captured_${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture?.takePicture(
                        outputOptions,
                        cameraExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraPreview", "Photo capture failed: ${exception.message}", exception)
                            }

                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                // Load bitmap
                                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                                onCaptureImage(bitmap, photoFile)
                            }
                        }
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(text = "Capture")
        }
    }
}
