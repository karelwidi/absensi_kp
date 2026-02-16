package id.co.psplauncher.data.network.response

import id.co.psplauncher.data.network.model.AbsenData

data class SubmitAbsenResponse(
    val success: Boolean,
    val data: AbsenData?
)