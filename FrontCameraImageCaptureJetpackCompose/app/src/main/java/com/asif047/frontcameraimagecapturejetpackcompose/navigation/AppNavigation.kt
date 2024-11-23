package com.asif047.frontcameraimagecapturejetpackcompose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.asif047.frontcameraimagecapturejetpackcompose.ui.CircularCameraApp
import com.asif047.frontcameraimagecapturejetpackcompose.ui.UploadImageScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "upload_image_screen"
    ) {
        composable("upload_image_screen") {
            UploadImageScreen(navController = navController)
        }
        composable("circular_camera_app") {
            CircularCameraApp(navController = navController)
        }
    }
}
