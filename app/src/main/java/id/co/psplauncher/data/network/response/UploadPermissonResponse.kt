package id.co.psplauncher.data.network.response

import androidx.camera.core.CameraUnavailableException

data class UploadPermissonResponse(
    val success: Boolean,
    val user_id: String?,
    val file_url: String,
    val reason: String
    )