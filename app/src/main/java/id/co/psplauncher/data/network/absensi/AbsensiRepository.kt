package id.co.psplauncher.data.network.absensi

import id.co.psplauncher.data.local.UserPreferences
import id.co.psplauncher.data.network.BaseRepository
import id.co.psplauncher.data.network.model.SubmitAbsen
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class AbsensiRepository @Inject constructor(
    private val api: AbsensiApi,
    private val userPreferences: UserPreferences
) : BaseRepository(){
    suspend fun getAbsenHistory() = safeApiCall({
        api.getAbsenHistory()
    }, userPreferences)

    suspend fun checkLocation(latitude: Double, longitude: Double) = safeApiCall({
        api.checkLocation(latitude, longitude)
    }, userPreferences)

    suspend fun uploadFacePhoto(file: MultipartBody.Part) = safeApiCall({
        api.uploadFacePhoto(file)
    }, userPreferences)

    suspend fun submitAbsen(request: SubmitAbsen) = safeApiCall({
        api.submitAbsen(request)
    }, userPreferences)

    suspend fun uploadIjinPhoto(file: MultipartBody.Part, reason: RequestBody) = safeApiCall({
        api.uploadIjinPhoto(file, reason)
    }, userPreferences)

}