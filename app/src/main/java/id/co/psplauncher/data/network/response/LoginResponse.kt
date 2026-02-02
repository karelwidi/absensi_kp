package id.co.psplauncher.data.network.response

data class LoginResponse (
    val success: Boolean,
    val jwt: String,
    val user: User
)
