package id.co.psplauncher.ui.main.infoAbsen

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import dagger.hilt.android.AndroidEntryPoint
import id.co.psplauncher.R
import id.co.psplauncher.Utils.handleApiError
import id.co.psplauncher.Utils.visible
import id.co.psplauncher.data.local.AbsenCache
import id.co.psplauncher.data.local.UserPreferences
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.databinding.ActivityInformasiDetailBinding
import id.co.psplauncher.ui.main.success.SuccessActivity
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.jvm.java

@AndroidEntryPoint
class InformasiDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInformasiDetailBinding
    private val viewModel: InformasiViewModel by viewModels()

    @Inject
    lateinit var absenCache: AbsenCache

    @Inject
    lateinit var userPreferences: UserPreferences

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var faceImageUrl: String = ""
    private var ijinImageBitmap: Bitmap? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = uriToBitmap(it)
            if (bitmap != null) {
                ijinImageBitmap = bitmap
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformasiDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        faceImageUrl = intent.getStringExtra("face_image_url") ?: ""

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.apply {
            lifecycleScope.launch {
                tvName.text = viewModel.getUsername()
            }

            val faceFile = absenCache.getTodayFacePhoto()

            if (faceFile != null && faceFile.exists()) {
                Glide.with(this@InformasiDetailActivity)
                    .load(faceFile.absolutePath)
                    .signature(ObjectKey(faceFile.lastModified()))
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(ivProfile)
            } else {
                ivProfile.setImageResource(R.drawable.profile)
            }

            val currentTime =
                SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
            tvWaktu.text = currentTime

            tvLatitude.text = "${String.format("%.6f", latitude)}"
            tvLongitude.text = "${String.format("%.6f", longitude)}"

            btnBack.setOnClickListener {
                finish()
            }

            btnBrowse.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }

            btnAbsen.setOnClickListener {
                handleSubmit()
            }
        }
    }

    private fun handleSubmit() {
        if (ijinImageBitmap != null) {
            val reason = binding.etReason.text.toString().trim()
            if (reason. isEmpty()) {
                binding.etReason.error = "Alasan ijin harus diisi"
                return
            }

            viewModel.uploadPhoto(ijinImageBitmap!!, reason)
        } else {
            viewModel.submitAbsen(latitude, longitude, faceImageUrl, "masuk")
        }
    }

    private fun observeViewModel() {
        viewModel.uploadPermissonResponse.observe(this) { resource ->
            binding.apply {
                when (resource) {
                    is Resource.Loading -> {
                        setLoadingState(true)
                        btnAbsen.text = "Mengupload Ijin..."
                    }
                    is Resource.Success -> {
                        val response = resource.value
                        if (response.success) {
                            ijinImageBitmap?.let { absenCache.saveIzinPhoto(it) }
                            viewModel.submitAbsen(latitude, longitude, faceImageUrl, "ijin")
                        } else {
                            setLoadingState(false)
                            Toast.makeText(this@InformasiDetailActivity, "Gagal upload ijin", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is Resource.Failure -> {
                        setLoadingState(false)
                        Toast.makeText(this@InformasiDetailActivity, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.submitAbsenResponse.observe(this) { resource ->
            binding.apply {
                when (resource) {
                    is Resource.Loading -> {
                        setLoadingState(true)
                        btnAbsen.text = "Mengirim Data..."
                    }
                    is Resource.Success -> {
                        setLoadingState(false)
                        val response = resource.value

                        if (response.success) {
                            Toast.makeText(this@InformasiDetailActivity, "Absen Berhasil!", Toast.LENGTH_SHORT).show()
                            navigateToSuccess(response.data?.datetime)
                        } else {
                            Toast.makeText(this@InformasiDetailActivity, "Absen Gagal!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is Resource.Failure -> {
                        setLoadingState(false)
                        handleApiError(root, resource)
                    }
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.visible(isLoading)
        binding.btnAbsen.isEnabled = !isLoading
        binding.btnAbsen.text = if (isLoading) "Mohon Tunggu..." else "Absen Sekarang"
    }

    private fun navigateToSuccess(serverTime: String?) {
        val userName = binding.tvName.text.toString()
        val finalTime = serverTime ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val intent = Intent(this, SuccessActivity::class.java).apply {
            putExtra("nama", userName)
            putExtra("datetime", finalTime)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}