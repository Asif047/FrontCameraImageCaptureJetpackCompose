package com.asif047.frontcameraimagecapturejetpackcompose.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private fun initializeCamera(
    previewView: PreviewView,
    context: android.content.Context,
    executor: ExecutorService
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        // Unbind all use cases to ensure no conflicts
        cameraProvider.unbindAll()

        val preview = androidx.camera.core.Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        preview.setSurfaceProvider(previewView.surfaceProvider)

        // Bind the preview use case
        cameraProvider.bindToLifecycle(
            context as androidx.lifecycle.LifecycleOwner,
            cameraSelector,
            preview
        )
    }, ContextCompat.getMainExecutor(context))
}

private fun captureImage(
    context: android.content.Context,
    executor: ExecutorService,
    onImageCaptured: (Bitmap) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val imageCapture = androidx.camera.core.ImageCapture.Builder().build()

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        // Unbind previous use cases before adding new ones
        cameraProvider.unbindAll()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        // Bind ImageCapture use case
        cameraProvider.bindToLifecycle(
            context as androidx.lifecycle.LifecycleOwner,
            cameraSelector,
            imageCapture
        )

        // Capture the image
        imageCapture.takePicture(
            executor,
            object : androidx.camera.core.ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: androidx.camera.core.ImageProxy) {
                    val bitmap = imageProxy.toBitmap()
                    imageProxy.close()
                    bitmap?.let { onImageCaptured(it) }
                }

                override fun onError(exception: androidx.camera.core.ImageCaptureException) {
                    exception.printStackTrace()
                }
            }
        )
    }, ContextCompat.getMainExecutor(context))
}

// Convert ImageProxy to Bitmap
private fun androidx.camera.core.ImageProxy.toBitmap(): Bitmap? {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}


@Composable
fun CircularCameraApp(navController: NavController) {
    val context = LocalContext.current
    val capturedImage = remember { mutableStateOf<Bitmap?>(null) }
    val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (capturedImage.value == null) {
            // Camera preview
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { context ->
                        PreviewView(context).apply {
                            post { initializeCamera(this, context, cameraExecutor) }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                captureImage(context, cameraExecutor) { bitmap ->
                    capturedImage.value = bitmap // Trigger recomposition
                }
            }) {
                Text("Capture Image")
            }
        } else {
            // Display captured image
            capturedImage.value?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                capturedImage.value = null
            }) {
                Text("Retake")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                capturedImage.value?.let { bitmap ->
                    val file = saveBitmapToFile(context, bitmap)
                    navController.previousBackStackEntry?.savedStateHandle?.set("capturedImagePath", file.absolutePath)
                    navController.popBackStack()
                }
            }) {
                Text("Done")
            }
        }
    }
}





// Save the captured image to a file
private fun saveBitmapToFile(context: android.content.Context, bitmap: Bitmap): java.io.File {
    val file = java.io.File(context.cacheDir, "captured_image_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    }
    return file
}


