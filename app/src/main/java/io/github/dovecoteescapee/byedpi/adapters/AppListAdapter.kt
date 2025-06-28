package io.github.dovecoteescapee.byedpi.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.dovecoteescapee.byedpi.databinding.ListItemAppBinding
import io.github.dovecoteescapee.byedpi.models.AppInfo

class AppListAdapter(
    private val apps: List<AppInfo>,
    private val onAppSelected: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ListItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val appInfo = apps[position]
        holder.bind(appInfo)
    }

    override fun getItemCount(): Int = apps.size

    inner class AppViewHolder(private val binding: ListItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(appInfo: AppInfo) {
            binding.appNameTextView.text = appInfo.name
            binding.appIconImageView.setImageDrawable(appInfo.icon)
            binding.appSelectedCheckBox.isChecked = appInfo.isSelected

            binding.appSelectedCheckBox.setOnCheckedChangeListener { _, isChecked ->
                appInfo.isSelected = isChecked
                onAppSelected(appInfo, isChecked)
            }
            // Also handle click on the item itself to toggle checkbox
            itemView.setOnClickListener {
                binding.appSelectedCheckBox.isChecked = !binding.appSelectedCheckBox.isChecked
                // The listener on the checkbox will handle the rest
            }
        }
    }
}
