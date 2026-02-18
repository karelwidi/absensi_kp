package id.co.psplauncher.ui.main.historyDetail

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Placeholder
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import id.co.psplauncher.R
import id.co.psplauncher.Utils.visible
import id.co.psplauncher.data.network.model.AbsenHistory
import id.co.psplauncher.databinding.ActivityHistoryDetailBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HistoryDetailActivity : AppCompatActivity() {

private lateinit var binding: ActivityHistoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding. inflate(layoutInflater)
        setContentView(binding.root)

        val absenHistory = intent.getSerializableExtra("absen_data") as? AbsenHistory
        val facePhotoPath = intent.getStringExtra("face_photo_path")
        val izinPhotoPath = intent.getStringExtra("izin_photo_path")

        if (absenHistory != null) {
            setupUI(absenHistory, facePhotoPath, izinPhotoPath)
        } else {
            finish()
        }
    }
    private fun setupUI(data: AbsenHistory, facePhotoPath: String?, izinPhotoPath: String?) {
        binding.apply {
            tvName.text = data.name ?: data.user_id
            tvTime.text = formatDateTime(data.datetime)
            tvLatitude.text = "${data.latitude}"
            tvLong.text = "${data.longitude}"

            loadImageFromFile(
                path = facePhotoPath,
                targetView = ivUser,
                placeholder = R.drawable.profile
            )

            loadImageFromFile(
                path = izinPhotoPath,
                targetView = ivBuktiFoto,
                placeholder = R.drawable.logo
            )
            btnBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun loadImageFromFile(path: String?, targetView: ImageView, placeholder: Int){
            val file = if (path != null) File(path) else null
            if (file != null && file.exists()) {
                targetView.visible(true)
                Glide.with(this)
                    .load(file)
                    .signature(ObjectKey(file.lastModified()))
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(targetView)
            }else{
                targetView.visible(false)
            }
    }

    private fun formatDateTime(datetime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }

            val outputFormat = SimpleDateFormat(
                "dd MMMM yyyy, HH:mm",
                Locale("id", "ID")
            ).apply {
                timeZone = TimeZone.getDefault()
            }

            val date = inputFormat.parse(datetime)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            datetime
        }
    }

}