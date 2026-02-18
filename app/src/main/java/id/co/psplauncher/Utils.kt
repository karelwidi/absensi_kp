package id.co.psplauncher

import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import id.co.psplauncher.data.network.Resource
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    fun Activity.handleApiError(
        view: View,
        failure: Resource.Failure,
    ) {
        when {
            failure.isNetworkError -> {
                view.snackbar("Gagal koneksi. Silahkan check kembali koneksi jaringan anda")
            }

            failure.errorCode == 401 -> {

            }

            failure.errorCode == 400 -> {
                view.snackbar(failure.errorBody?.string().toString())
            }

            else -> {
                val error = failure.errorBody?.string().toString()
                view.snackbar(error)
            }
        }
    }

    fun View.snackbar(message: String) {
        val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view
        val layoutParams = FrameLayout.LayoutParams(snackbar.view.layoutParams)
        val snackTextView =
            snackbarView.findViewById<View>(com.google.android.material.R.id.snackbar_text) as TextView

        layoutParams.setMargins(10, 10, 10, 10)
        layoutParams.gravity = Gravity.BOTTOM
        snackbar.view.setPadding(10, 10, 10, 10)
        snackTextView.maxLines = 5
        snackbar.view.layoutParams = layoutParams
        snackbar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
        snackbar.show()
    }

    fun View.visible(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun <A : Activity> Activity.startNewActivity(activity: Class<A>) {
        Intent(this, activity).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    fun parseIsoDate(dateString: String) : Date?{
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.parse(dateString.take(19))
        }catch (e: Exception){
            null
        }
    }

    fun Date.toDisplayFormat(): String {
        val format = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("id", "ID"))
        return format.format(this)
    }



    fun formatCurrency(doubleValue: Double): String {
        val unusualSymbols = DecimalFormatSymbols()
        unusualSymbols.decimalSeparator = ','
        unusualSymbols.groupingSeparator = '.'

        val formatter = DecimalFormat("#,##0.##", unusualSymbols)
        formatter.groupingSize = 3
        return formatter.format(doubleValue)
    }

}