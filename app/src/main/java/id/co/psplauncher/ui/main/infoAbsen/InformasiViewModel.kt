package id.co.psplauncher.ui.main.infoAbsen

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.psplauncher.data.local.UserPreferences
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.data.network.absensi.AbsensiRepository
import id.co.psplauncher.data.network.model.SubmitAbsen
import id.co.psplauncher.data.network.response.SubmitAbsenResponse
import id.co.psplauncher.data.network.response.UploadPermissonResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class InformasiViewModel @Inject constructor(
    private val absensiRepository: AbsensiRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uploadPermissonResponse = MutableLiveData<Resource<UploadPermissonResponse>>()
    val uploadPermissonResponse: LiveData<Resource<UploadPermissonResponse>> get() = _uploadPermissonResponse

    private val _submitAbsenResponse = MutableLiveData<Resource<SubmitAbsenResponse>>()
    val submitAbsenResponse: LiveData<Resource<SubmitAbsenResponse>> get() = _submitAbsenResponse

    fun uploadPhoto(bitmap: Bitmap, reason: String) = viewModelScope.launch {
        _uploadPermissonResponse.value = Resource.Loading

        try {
            val filePart = withContext(Dispatchers.IO) {
                createImageMultipart(bitmap)
            }
            val reasonBody = reason.toRequestBody("text/plain".toMediaTypeOrNull())

            _uploadPermissonResponse.value = absensiRepository.uploadIjinPhoto(filePart, reasonBody)

        } catch (e: Exception) {
            _uploadPermissonResponse.value = Resource.Failure(true, null, null)
        }
    }

    fun submitAbsen(latitude: Double, longitude: Double, faceImageUrl: String, status: String) =
        viewModelScope.launch {
            _submitAbsenResponse.value = Resource.Loading

            try{
                val request = SubmitAbsen(
                    latitude = latitude,
                    longitude = longitude,
                    face_image_url = faceImageUrl,
                    status = status
                )
                _submitAbsenResponse.value = absensiRepository.submitAbsen(request)
            } catch (e: Exception){
                _submitAbsenResponse.value = Resource.Failure(true, null, null)
            }
        }

    suspend fun getUsername(): String {
        return userPreferences.getUserName()
    }

    private fun createImageMultipart(bitmap: Bitmap): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", "ijin_photo.jpg", requestBody)
    }
}