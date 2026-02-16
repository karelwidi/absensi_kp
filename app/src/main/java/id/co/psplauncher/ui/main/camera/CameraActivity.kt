package id.co.psplauncher.ui.main.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import id.co.psplauncher.Utils.handleApiError
import id.co.psplauncher.Utils.visible
import id.co.psplauncher.data.local.AbsenCache
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.databinding.ActivityCameraBinding
import id.co.psplauncher.ui.main.infoAbsen.InformasiDetailActivity
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private val viewModel: CameraViewModel by viewModels()

    private lateinit var cameraExecutor: ExecutorService

    private var imageCapture: ImageCapture? = null
    private var isPhotoTaken = false

    @Inject
    lateinit var absenCache: AbsenCache

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        checkPermissionAndStart()
        setupUI()
        observeViewModel()
    }

    private fun checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setupUI() {
        binding.apply {
            btnCapture.setOnClickListener {
                if (!isPhotoTaken) {
                    takePhoto()
                }
            }

            btnBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewPre.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, FaceAnalyzer())
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal memuat kamera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private inner class FaceAnalyzer : ImageAnalysis.Analyzer {
        val opts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        val detector = FaceDetection.getClient(opts)

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image

            if (mediaImage != null && !isPhotoTaken) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                detector.process(image)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            Log.d("FaceDetection", "Wajah ditemukan: ${faces.size}")
                            isPhotoTaken = true
                            runOnUiThread {
                                binding.tvInfo.text = "Wajah terdeteksi! Mengambil gambar..."
                                takePhoto()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        isPhotoTaken = true
        binding.progressBar.visible(true)
        binding.tvInfo.text = "Memproses gambar..."
        binding.btnCapture.isEnabled = false

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = processImageProxy(image)
                    image.close()

                    if (bitmap != null) {
                        saveToCache(bitmap)
                        viewModel.uploadFacePhoto(bitmap)
                    } else {
                        resetCameraState("Gagal memproses gambar")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    resetCameraState("Gagal mengambil foto: ${exception.message}")
                }
            }
        )
    }

    private fun resetCameraState(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        binding.progressBar.visible(false)
        binding.tvInfo.text = "Silakan coba lagi"
        binding.btnCapture.isEnabled = true
        isPhotoTaken = false
    }

    private fun saveToCache(bitmap: Bitmap) {
        Executors.newSingleThreadExecutor().execute {
            val savePath = absenCache.saveFacePhoto(bitmap)
            if (savePath.isNotEmpty()) {
                Log.d("AbsenCache", "Foto tersimpan di: $savePath")
            }
        }
    }

    private fun processImageProxy(image: ImageProxy): Bitmap? {
        val buffer: ByteBuffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

        val matrix = Matrix()
        matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())

        matrix.postScale(-1f, 1f)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun observeViewModel() {
        viewModel.uploadFaceResponse.observe(this) { resource ->
            binding.apply {
                when (resource) {
                    is Resource.Loading -> {
                        progressBar.visible(true)
                        tvInfo.text = "Sedang mengupload..."
                    }
                    is Resource.Success -> {
                        progressBar.visible(false)
                        val response = resource.value

                        if (response.success) {
                            Toast.makeText(this@CameraActivity, "Upload Berhasil", Toast.LENGTH_SHORT).show()
                            navigateToNextScreen(response.img_url)
                        } else {
                            resetCameraState("Upload Gagal")
                        }
                    }
                    is Resource.Failure -> {
                        progressBar.visible(false)
                        resetCameraState("Koneksi Gagal")
                        handleApiError(root, resource)
                    }
                }
            }
        }
    }
    private fun navigateToNextScreen(faceUrl: String?) {
        val lat = intent.getDoubleExtra("latitude", 0.0)
        val long = intent.getDoubleExtra("longitude", 0.0)

        val intent = Intent(this, InformasiDetailActivity::class.java).apply {
            putExtra("latitude", lat)
            putExtra("longitude", long)
            putExtra("face_image_url", faceUrl)
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}