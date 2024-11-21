package com.asif047.frontcameraimagecapturejetpackcompose.ui

import android.graphics.Bitmap
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CircularCameraApp() {
    val context = LocalContext.current
    var capturedImage = remember { mutableStateOf<Bitmap?>(null) }
    var previewView: PreviewView? = remember { null }
    val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (capturedImage.value == null) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { context ->
                        PreviewView(context).apply {
                            previewView = this
                            post { initializeCamera(this, context, cameraExecutor) }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                captureImage(context, previewView, cameraExecutor) { bitmap ->
                    capturedImage.value = bitmap
                }
            }) {
                Text("Capture Image")
            }
        } else {

            if(capturedImage.value != null) {
                Image(
                    bitmap = capturedImage.value!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                        .clip(CircleShape)
                )
            }


            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { capturedImage.value = null }) {
                Text("Retake")
            }
        }
    }
}


private fun initializeCamera(
    previewView: PreviewView,
    context: android.content.Context,
    executor: ExecutorService
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = androidx.camera.core.Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        preview.setSurfaceProvider(previewView.surfaceProvider)
        cameraProvider.bindToLifecycle(
            context as androidx.lifecycle.LifecycleOwner,
            cameraSelector,
            preview
        )
    }, ContextCompat.getMainExecutor(context))
}


private fun captureImage(
    context: android.content.Context,
    previewView: PreviewView?,
    executor: ExecutorService,
    onImageCaptured: (Bitmap) -> Unit
) {
    val imageCapture = androidx.camera.core.ImageCapture.Builder().build()
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

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
                    // Ensure this block runs on the main thread
                    ContextCompat.getMainExecutor(context).execute {
                        val bitmap = previewView?.bitmap
                        bitmap?.let { onImageCaptured(it) }
                    }
                    imageProxy.close()
                }

                override fun onError(exception: androidx.camera.core.ImageCaptureException) {
                    exception.printStackTrace()
                }
            }
        )
    }, ContextCompat.getMainExecutor(context))
}


