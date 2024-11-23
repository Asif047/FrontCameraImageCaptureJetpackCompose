package com.asif047.frontcameraimagecapturejetpackcompose.data

import com.asif047.frontcameraimagecapturejetpackcompose.api.APIService
import com.asif047.frontcameraimagecapturejetpackcompose.domain.UploadRepository
import com.asif047.frontcameraimagecapturejetpackcompose.model.UploadResponse
import okhttp3.MultipartBody
import javax.inject.Inject

class UploadRepositoryImpl @Inject constructor(
    private val apiService: APIService
) : UploadRepository {

    override suspend fun uploadImage(apiKey: String, image: MultipartBody.Part): UploadResponse {
        return apiService.uploadImage(apiKey, image)
    }
}
