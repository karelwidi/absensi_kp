package id.co.psplauncher.data.network.response

import id.co.psplauncher.data.network.model.AbsenData
import id.co.psplauncher.data.network.model.AbsenHistory

data class AbsensiHistoryResponse (
    val success: Boolean,
    val data: List<AbsenHistory>


)