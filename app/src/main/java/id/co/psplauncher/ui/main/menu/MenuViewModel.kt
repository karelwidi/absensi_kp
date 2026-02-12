package id.co.psplauncher.ui.main.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.co.psplauncher.data.local.UserPreferences
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.data.network.absensi.AbsensiRepository
import id.co.psplauncher.data.network.response.AbsensiHistoryResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val absenRepository: AbsensiRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _absenHistoryResponse = MutableLiveData<Resource<AbsensiHistoryResponse>>()
    val absenHistoryResponse: LiveData<Resource<AbsensiHistoryResponse>> get() = _absenHistoryResponse

    fun getAbsenHistory() = viewModelScope.launch {
        _absenHistoryResponse.value = Resource.Loading
        _absenHistoryResponse.value = absenRepository.getAbsenHistory()
    }

    suspend fun getUserName() = userPreferences.getUserName()

    fun delAccToken() = viewModelScope.launch {
        userPreferences.clearAccessToken()
    }
}