package com.asif047.frontcameraimagecapturejetpackcompose.domain

import com.asif047.frontcameraimagecapturejetpackcompose.model.UploadResponse
import okhttp3.MultipartBody
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val repository: UploadRepository
) {
    suspend fun invoke(apiKey: String, image: MultipartBody.Part): UploadResponse {
        return repository.uploadImage(apiKey, image)
    }
}
