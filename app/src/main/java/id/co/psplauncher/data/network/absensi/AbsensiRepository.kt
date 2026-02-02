package id.co.psplauncher.data.network.absensi

import id.co.psplauncher.data.local.UserPreferences
import id.co.psplauncher.data.network.BaseRepository
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

}