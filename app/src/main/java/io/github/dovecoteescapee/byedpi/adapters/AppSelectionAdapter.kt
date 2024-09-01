package io.github.dovecoteescapee.byedpi.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppInfo

class AppSelectionAdapter(
    private val allApps: List<AppInfo>,
    private val onAppSelected: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppSelectionAdapter.ViewHolder>(), Filterable {

    private val filteredApps: MutableList<AppInfo> = allApps
        .filter { !it.appName.contains("com.") }
        .toMutableList()

    private val originalApps: List<AppInfo> = allApps
        .filter { !it.appName.contains("com.") }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appIcon: ImageView = view.findViewById(R.id.appIcon)
        val appName: TextView = view.findViewById(R.id.appName)
        val appCheckBox: CheckBox = view.findViewById(R.id.appCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = filteredApps[position]
        holder.appIcon.setImageDrawable(app.icon)
        holder.appName.text = app.appName
        holder.appCheckBox.isChecked = app.isSelected

        holder.itemView.setOnClickListener {
            app.isSelected = !app.isSelected
            holder.appCheckBox.isChecked = app.isSelected
            onAppSelected(app, app.isSelected)
        }
    }

    override fun getItemCount() = filteredApps.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""
                val filteredList = if (query.isEmpty()) {
                    originalApps
                } else {
                    originalApps.filter { it.appName.lowercase().contains(query) }
                }
                return FilterResults().apply { values = filteredList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val newFilteredApps = (results?.values as? List<AppInfo>) ?: originalApps
                filteredApps.clear()
                filteredApps.addAll(newFilteredApps)
                notifyDataSetChanged()
            }
        }
    }
}