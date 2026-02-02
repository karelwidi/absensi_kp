package id.co.psplauncher.data.network.absensi

import id.co.psplauncher.data.network.response.AbsensiHistoryResponse
import id.co.psplauncher.data.network.response.LocationValidationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AbsensiApi {

    @GET("absen/history")
    suspend fun getAbsenHistory(): Response<AbsensiHistoryResponse>

    @GET("location/check")
    suspend fun checkLocation(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ): Response<LocationValidationResponse>

}