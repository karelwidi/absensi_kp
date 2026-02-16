package id.co.psplauncher.data.local

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AbsenCache @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs = context.getSharedPreferences("absen_cache", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val photoDir: File
        get() {
            val dir = File(context.filesDir, "absen_photos")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }


    fun saveFacePhoto(bitmap: Bitmap): String {
        val today = dateFormat.format(Date())
        val fileName = "face_$today.jpg"
        val file = File(photoDir, fileName)

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            Log.d("AbsenCache", "Face photo saved: ${file.absolutePath}")

            prefs.edit().putString("face_photo_path_$today", file.absolutePath).apply()

            return file.absolutePath
        } catch (e: Exception) {
            Log.e("AbsenCache", "Error saving face photo: ${e.message}")
            return ""
        }
    }

    fun getFacePhoto(datetime: String): File? {
        val dateKey = parseDateFromDatetime(datetime)
        val path = prefs.getString("face_photo_path_$dateKey", null)

        return if (path != null) {
            val file = File(path)
            if (file.exists()) file else null
        } else {
            null
        }
    }

    fun getTodayFacePhoto(): File? {
        val today = dateFormat.format(Date())
        val path = prefs.getString("face_photo_path_$today", null)

        return if (path != null) {
            val file = File(path)
            if (file.exists()) file else null
        } else {
            null
        }
    }

    fun saveIzinPhoto(bitmap: Bitmap): String {
        val today = dateFormat.format(Date())
        val fileName = "izin_$today.jpg"
        val file = File(photoDir, fileName)

        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Log.d("AbsenCache", "Izin photo saved: ${file.absolutePath}")

            prefs.edit().putString("izin_photo_path_$today", file.absolutePath).apply()

            return file.absolutePath
        } catch (e: Exception) {
            Log.e("AbsenCache", "Error saving izin photo: ${e.message}")
            return ""
        }
    }

    fun getIzinPhoto(datetime: String): File? {
        val dateKey = parseDateFromDatetime(datetime)
        val path = prefs.getString("izin_photo_path_$dateKey", null)

        return if (path != null) {
            val file = File(path)
            if (file.exists()) file else null
        } else {
            null
        }
    }

    fun getTodayIzinPhoto(): File? {
        val today = dateFormat.format(Date())
        val path = prefs.getString("izin_photo_path_$today", null)

        return if (path != null) {
            val file = File(path)
            if (file.exists()) file else null
        } else {
            null
        }
    }

    private fun parseDateFromDatetime(datetime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(datetime)
            dateFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateFormat.format(Date())
        }
    }

    fun clearOldCache() {
        val today = dateFormat.format(Date())

        val allKeys = prefs.all.keys
        allKeys.forEach { key ->
            if ((key.startsWith("face_photo_path_") || key.startsWith("izin_photo_path_"))
                && !key.endsWith(today)
            ) {
                val path = prefs.getString(key, null)
                if (path != null) {
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                        Log.d("AbsenCache", "Deleted old photo: ${file.name}")
                    }
                }
                prefs.edit().remove(key).apply()
            }
        }

        photoDir.listFiles()?.forEach { file ->
            if (!file.name.contains(today)) {
                file.delete()
                Log.d("AbsenCache", "Deleted orphan photo: ${file.name}")
            }
        }
    }
}