package id.co.psplauncher.data.network.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File


data class AbsenData (
    val _id: String,
    val user_id: String,
    val nama: String?,
    val latitude: Double,
    val longitude:  Double,
    val face_image_url: String,
    val status: String,
    val datetime: String,
)