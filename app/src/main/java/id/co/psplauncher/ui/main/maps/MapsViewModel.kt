package id.co.psplauncher.ui.main.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.data.network.absensi.AbsensiRepository
import id.co.psplauncher.data.network.response.LocationValidationResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel

class MapsViewModel @Inject constructor(
    private val absensiRepository: AbsensiRepository
) : ViewModel() {

    private val _locationValidationResponse= MutableLiveData<Resource<LocationValidationResponse>>()
    val locationValidationResponse: LiveData<Resource<LocationValidationResponse>> get() = _locationValidationResponse

    fun checkLocation(latitude: Double, longitude: Double) = viewModelScope.launch {
        _locationValidationResponse.value = Resource.Loading
        _locationValidationResponse.value = absensiRepository.checkLocation(latitude, longitude)

    }
}