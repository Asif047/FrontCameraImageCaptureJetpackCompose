package com.asif047.frontcameraimagecapturejetpackcompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asif047.frontcameraimagecapturejetpackcompose.domain.UploadImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModel() {

    var imageUrl: String? = null
        private set

    fun uploadImage(apiKey: String, imageFile: File, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
        val multipartBody = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)

        viewModelScope.launch {
            try {
                val response = uploadImageUseCase.invoke(apiKey, multipartBody)
                imageUrl = response.data.display_url // Assuming the API returns this field
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}
