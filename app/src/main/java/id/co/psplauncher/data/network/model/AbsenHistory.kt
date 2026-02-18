package id.co.psplauncher.data.network.model

import java.io.File
import java.io.Serializable

data class AbsenHistory (
    val id: String,
    val user_id: String,
    val name: String?,
    val latitude: Double,
    val longitude:  Double,
    val faceUrl: String,
    val status: String,
    val datetime: String,
    @Transient var facePhotoFile: File? = null,
    @Transient var izinPhotoFile: File? = null
) : Serializable{
    var facePhotoPath: String?  = null
        get() = facePhotoFile?.absolutePath

    var izinPhotoPath: String? = null
        get() = izinPhotoFile?.absolutePath
}