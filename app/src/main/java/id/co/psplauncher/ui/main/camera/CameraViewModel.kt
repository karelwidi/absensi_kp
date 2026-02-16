package id.co.psplauncher.ui.main.camera

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.data.network.absensi.AbsensiRepository
import id.co.psplauncher.data.network.response.UploadFaceResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val absensiRepository: AbsensiRepository,
) : ViewModel() {

    private var _uploadFaceResponse= MutableLiveData<Resource<UploadFaceResponse>>()
    val uploadFaceResponse: LiveData<Resource<UploadFaceResponse>> get() = _uploadFaceResponse

    fun uploadFacePhoto(bitmap: Bitmap) = viewModelScope.launch {
        _uploadFaceResponse.value = Resource.Loading
        try {

            val multipartBody = withContext(Dispatchers.IO) {
                bitmapToMultipartBody(bitmap, "face_photo.jpg")
            }
            _uploadFaceResponse.value = absensiRepository.uploadFacePhoto(multipartBody)
        } catch (e: Exception) {
            _uploadFaceResponse.value = Resource.Failure(
                isNetworkError = true,
                errorCode = null,
                errorBody = null)
        }
    }

    private fun bitmapToMultipartBody(bitmap: Bitmap, fileName: String): MultipartBody.Part {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()

        val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())

        return MultipartBody.Part.createFormData("file", fileName, requestBody)
    }
}
