package id.co.psplauncher.ui.main.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import id.co.psplauncher.R
import id.co.psplauncher.Utils.visible
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.databinding.ActivityMapsBinding
import id.co.psplauncher.ui.main.camera.CameraActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val viewModel: MapsViewModel by viewModels()
    private var currentLocation: Location? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isGranted) {
            checkGpsAndStartLocation()
        } else {
            Toast.makeText(this, "Izin lokasi diperlukan untuk absensi", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initLocationCallback()
        setupMap()
        setupButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        checkGpsAndStartLocation()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun setupMap() {
        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(18.0)
        }
    }

    private fun setupButtons() {
        binding.apply {
            btnBack.setOnClickListener { finish() }

            btnEnableLocation.setOnClickListener {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }

            btnNext.setOnClickListener {
                currentLocation?.let { location ->
                    viewModel.checkLocation(location.latitude, location.longitude)
                } ?: Toast.makeText(this@MapsActivity, "Sedang mencari lokasi...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkGpsAndStartLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (isGpsEnabled) {
            binding.cardWarning.visible(false)
            binding.mapView.visible(true)
            checkPermissionAndRequestUpdates()
        } else {
            binding.cardWarning.visible(true)
            binding.mapView.visible(false)
            binding.progressBar.visible(false)
            binding.btnNext.isEnabled = false
        }
    }

    private fun checkPermissionAndRequestUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    private fun initLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {

                result.lastLocation?.let { location ->
                    updateMapUI(location)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        binding.progressBar.visible(true)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(1000)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    private fun updateMapUI(location: Location) {
        currentLocation = location
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        binding.apply {
            progressBar.visible(false)
            mapView.visible(true)
            cardWarning.visible(false)

            btnNext.isEnabled = true
            btnNext.backgroundTintList = ContextCompat.getColorStateList(this@MapsActivity, R.color.orange_primary)

            mapView.overlays.clear()
            val marker = Marker(mapView).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Lokasi Anda"
                icon = ContextCompat.getDrawable(this@MapsActivity, R.drawable.pin_maps)
            }
            mapView.overlays.add(marker)
            mapView.controller.animateTo(geoPoint)
        }
    }

    private fun observeViewModel() {
        viewModel.locationValidationResponse.observe(this) { resource ->
            binding.apply {
                when (resource) {
                    is Resource.Loading -> {
                        progressBar.visible(true)
                        btnNext.isEnabled = false
                    }
                    is Resource.Success -> {
                        progressBar.visible(false)
                        btnNext.isEnabled = true

                        val message = resource.value.message ?: "Lokasi valid"
                        navigateToCamera(message)
                    }
                    is Resource.Failure -> {
                        progressBar.visible(false)
                        btnNext.isEnabled = true
                        Toast.makeText(this@MapsActivity, "Lokasi tidak sesuai / Gagal memuat", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun navigateToCamera(message: String) {
        currentLocation?.let { location ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            val intent = Intent(this, CameraActivity::class.java).apply {
                putExtra("latitude", location.latitude)
                putExtra("longitude", location.longitude)
            }
            startActivity(intent)
        }
    }
}