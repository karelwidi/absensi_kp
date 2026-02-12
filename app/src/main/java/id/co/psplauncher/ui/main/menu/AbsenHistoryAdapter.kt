package id.co.psplauncher.ui.main.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.co.psplauncher.data.network.model.AbsenHistory
import id.co.psplauncher.databinding.ItemAbsenRiwayatBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AbsenHistoryAdapter(
    private val onItemClick: (AbsenHistory) -> Unit
) : ListAdapter<AbsenHistory, AbsenHistoryAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemAbsenRiwayatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AbsenHistory) {
            binding.apply {
                val date = parseDate(item.datetime)

                tvHistoryDate.text = date?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                } ?: "-"

                tvClockInTime.text = date?.let {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                } ?: "-"

                tvHistoryName.text = item.user_id

                root.setOnClickListener { onItemClick(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAbsenRiwayatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun parseDate(datetime: String): Date? {
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(datetime)
        } catch (e: Exception) {
            null
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AbsenHistory>() {
        override fun areItemsTheSame(oldItem: AbsenHistory, newItem: AbsenHistory): Boolean {
            return oldItem.datetime == newItem.datetime
        }

        override fun areContentsTheSame(oldItem: AbsenHistory, newItem: AbsenHistory): Boolean {
            return oldItem == newItem
        }
    }
}