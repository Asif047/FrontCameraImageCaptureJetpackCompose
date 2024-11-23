package com.asif047.frontcameraimagecapturejetpackcompose.api

import com.asif047.frontcameraimagecapturejetpackcompose.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface APIService {
    @Multipart
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") key: String,
        @Part image: MultipartBody.Part
    ): UploadResponse
}
