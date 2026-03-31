package io.github.dovecoteescapee.byedpi.fragments

import android.app.Dialog
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.dovecoteescapee.byedpi.R

class AppPickerDialogFragment : DialogFragment() {

    companion object {
        const val RESULT_KEY = "app_picker_result"
        const val RESULT_PACKAGE = "package_name"
        const val RESULT_APP_NAME = "app_name"
    }

    data class AppInfo(
        val name: String,
        val packageName: String,
        val icon: Drawable
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_app_picker, null)

        val searchEdit = view.findViewById<EditText>(R.id.search_edit)
        val recyclerView = view.findViewById<RecyclerView>(R.id.app_list)

        val apps = loadApps()
        val adapter = AppAdapter(apps.toMutableList()) { app ->
            parentFragmentManager.setFragmentResult(RESULT_KEY, Bundle().apply {
                putString(RESULT_PACKAGE, app.packageName)
                putString(RESULT_APP_NAME, app.name)
            })
            dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        searchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.lowercase() ?: ""
                adapter.filter(apps, query)
            }
        })

        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.auto_connect_app_picker_title)
            .setView(view)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun loadApps(): List<AppInfo> {
        val pm = requireContext().packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos: List<ResolveInfo> = pm.queryIntentActivities(intent, 0)
        val myPackage = requireContext().packageName

        return resolveInfos
            .filter { it.activityInfo.packageName != myPackage }
            .map { ri ->
                AppInfo(
                    name = ri.loadLabel(pm).toString(),
                    packageName = ri.activityInfo.packageName,
                    icon = ri.loadIcon(pm)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }
    }

    private class AppAdapter(
        private var items: MutableList<AppInfo>,
        private val onClick: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.app_icon)
            val name: TextView = view.findViewById(R.id.app_name)
            val packageName: TextView = view.findViewById(R.id.app_package)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.name.text = app.name
            holder.packageName.text = app.packageName
            holder.itemView.setOnClickListener { onClick(app) }
        }

        override fun getItemCount(): Int = items.size

        fun filter(allApps: List<AppInfo>, query: String) {
            items.clear()
            if (query.isEmpty()) {
                items.addAll(allApps)
            } else {
                items.addAll(allApps.filter {
                    it.name.lowercase().contains(query) ||
                        it.packageName.lowercase().contains(query)
                })
            }
            notifyDataSetChanged()
        }
    }
}
