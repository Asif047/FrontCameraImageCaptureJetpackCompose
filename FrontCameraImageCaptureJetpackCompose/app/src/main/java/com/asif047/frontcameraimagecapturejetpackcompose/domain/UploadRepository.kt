package com.asif047.frontcameraimagecapturejetpackcompose.domain

import com.asif047.frontcameraimagecapturejetpackcompose.model.UploadResponse
import okhttp3.MultipartBody

interface UploadRepository {
    suspend fun uploadImage(apiKey: String, image: MultipartBody.Part): UploadResponse
}
