package id.co.psplauncher.ui.main.success

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import id.co.psplauncher.databinding.ActivitySuccessBinding
import id.co.psplauncher.ui.main.main.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.jvm.java

class SuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nama = intent.getStringExtra("nama") ?: "user"
        val datetime = intent.getStringExtra("datetime") ?: getCurrentTimestamp()

        setupUI(nama, datetime)
        disableBackButton()
    }

    private fun setupUI(nama: String, datetime: String) {
        binding.apply {
            tvName.text = nama
            val (jam, tanggal) =parseDatetime(datetime)
            tvDate.text = tanggal
            tvTime.text = jam

            btnBack.setOnClickListener {
                navigateToHome()
            }
            btnHome.setOnClickListener {
                navigateToHome()
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun disableBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })
    }

    private fun parseDatetime(datetime: String): Pair<String, String> {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm: ss", Locale.getDefault())
            val date = inputFormat.parse(datetime) ?: Date()
            val jamFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val tanggalFormat = SimpleDateFormat("EEEE, d MMMM yyyy",Locale("id","ID"))
            Pair(jamFormat.format(date), tanggalFormat.format(date))
        } catch (e: Exception) {
            val now = Date()
            val jamFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val tanggalFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
            Pair(jamFormat.format(now), tanggalFormat.format(now))
        }
    }
    private fun getCurrentTimestamp(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return format.format(Date())
    }
}
