package id.co.psplauncher.data.network.absensi

import id.co.psplauncher.data.network.model.SubmitAbsen
import id.co.psplauncher.data.network.response.AbsensiHistoryResponse
import id.co.psplauncher.data.network.response.LocationValidationResponse
import id.co.psplauncher.data.network.response.SubmitAbsenResponse
import id.co.psplauncher.data.network.response.UploadFaceResponse
import id.co.psplauncher.data.network.response.UploadPermissonResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface AbsensiApi {

    @GET("absen/history")
    suspend fun getAbsenHistory(): Response<AbsensiHistoryResponse>

    @GET("location/check")
    suspend fun checkLocation(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double
    ): Response<LocationValidationResponse>

    @Multipart
    @POST("face/upload")
    suspend fun uploadFacePhoto(
        @Part file: MultipartBody.Part
    ): Response<UploadFaceResponse>

    @POST("absen/submit")
    @Headers("Content-Type: application/json")
    suspend fun submitAbsen(
        @Body request: SubmitAbsen
    ): Response<SubmitAbsenResponse>

    @Multipart
    @POST("izin/upload")
    suspend fun uploadIjinPhoto(
        @Part file: MultipartBody.Part,
        @Part("reason") reason: RequestBody
    ): Response<UploadPermissonResponse>
}