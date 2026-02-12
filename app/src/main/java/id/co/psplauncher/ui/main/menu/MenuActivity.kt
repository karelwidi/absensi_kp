package id.co.psplauncher.ui.main.menu

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import dagger.hilt.android.AndroidEntryPoint
import id.co.psplauncher.R
import id.co.psplauncher.Utils.handleApiError
import id.co.psplauncher.Utils.startNewActivity
import id.co.psplauncher.Utils.visible
import id.co.psplauncher.data.local.AbsenCache
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.data.network.model.AbsenHistory
import id.co.psplauncher.databinding.ActivityMenuBinding
import id.co.psplauncher.ui.main.historyDetail.HistoryDetailActivity
import id.co.psplauncher.ui.main.main.MainActivity
import id.co.psplauncher.ui.main.maps.MapsActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private val viewModel: MenuViewModel by viewModels()

    private val adapter by lazy {
        AbsenHistoryAdapter { historyItem ->
            navigateToDetail(historyItem)
        }
    }

    @Inject
    lateinit var absenCache: AbsenCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        absenCache.clearOldCache()

        setupUI()
        setupRecyclerView()
        startRealtimeClock()
        observeViewModel()

    }

    override fun onResume() {
        super.onResume()
        loadProfilePicture()
        viewModel.getAbsenHistory()
    }

    private fun loadProfilePicture() {
        val faceFile = absenCache.getTodayFacePhoto()

        binding.apply {
            if (faceFile != null && faceFile.exists()) {
                Glide.with(this@MenuActivity)
                    .load(faceFile)
                    .signature(ObjectKey(faceFile.lastModified()))
                    .placeholder(R.drawable.profile)
                    .error(R.drawable.profile)
                    .circleCrop()
                    .into(imgProfile)
            } else {
                imgProfile.setImageResource(R.drawable.profile)
            }
        }
    }

    private fun startRealtimeClock() {
        lifecycleScope.launch {
            while (true) {
                val jam = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                binding.tvActionTime.text = "$jam WIB"
                delay(1000)
            }
        }
    }

    private fun setupUI() {
        binding.apply {
            lifecycleScope.launch {
                tvUserName.text = viewModel.getUserName()
            }

            btnAbsen.setOnClickListener {
                startActivity(Intent(this@MenuActivity, MapsActivity::class.java))
                Toast.makeText(this@MenuActivity, "Fitur Absensi Berhasil", Toast.LENGTH_SHORT).show()
            }

            btnLogout.setOnClickListener {
                viewModel.delAccToken()
                startNewActivity(MainActivity::class.java)
                finish()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(this@MenuActivity)
            adapter = this@MenuActivity.adapter
        }
    }

    private fun navigateToDetail(historyItem: AbsenHistory) {
        val intent = Intent(this, HistoryDetailActivity::class.java).apply {
            putExtra("absen_data", historyItem)

            putExtra("face_photo_path", historyItem.facePhotoPath)
            putExtra("izin_photo_path", historyItem.izinPhotoPath)
        }
        startActivity(intent)
        Toast.makeText(this, "Detail: ${historyItem.status}", Toast.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        viewModel.absenHistoryResponse.observe(this) { resource ->
            binding.apply {
                when (resource) {
                    is Resource.Loading -> {
                        progressBar.visible(true)
                        rvHistory.visible(false)
                        EmptyState.visible(false)
                    }

                    is Resource.Success -> {
                        progressBar.visible(false)
                        val data = resource.value.data

                        if (data.isEmpty()) {
                            rvHistory.visible(false)
                            EmptyState.visible(true)
                        } else {
                            rvHistory.visible(true)
                            EmptyState.visible(false)

                            data.forEach { absen ->
                                absen.facePhotoFile = absenCache.getFacePhoto(absen.datetime)
                                absen.izinPhotoFile = absenCache.getIzinPhoto(absen.datetime)
                            }
                            adapter.submitList(data)
                        }
                    }

                    is Resource.Failure -> {
                        progressBar.visible(false)
                        rvHistory.visible(false)
                        EmptyState.visible(true)
                        handleApiError(root, resource)
                    }
                }
            }
        }
    }
}