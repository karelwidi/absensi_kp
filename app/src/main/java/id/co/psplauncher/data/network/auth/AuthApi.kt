package id.co.psplauncher.data.network.auth

import id.co.psplauncher.data.network.model.ModelLogin
import id.co.psplauncher.data.network.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {

    @Headers("Content-Type: application/json")
    @POST("auth/login")
    suspend fun login(
        @Body info: ModelLogin
    ): Response<LoginResponse>

}