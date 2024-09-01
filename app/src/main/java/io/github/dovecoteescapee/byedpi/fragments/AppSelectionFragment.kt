package io.github.dovecoteescapee.byedpi.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.adapters.AppSelectionAdapter
import io.github.dovecoteescapee.byedpi.data.AppInfo

class AppSelectionFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: AppSelectionAdapter
    private var allApps: List<AppInfo> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_selection, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)

        setupRecyclerView()
        setupSearchView()

        return view
    }

    private fun setupRecyclerView() {
        allApps = getInstalledApps()
        adapter = AppSelectionAdapter(allApps) { app, isChecked ->
            updateSelectedApps(app.packageName, isChecked)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm = requireContext().packageManager
        val installedApps = pm.getInstalledApplications(0)
        val selectedApps = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getStringSet("selected_apps", setOf()) ?: setOf()

        return installedApps
            .filter { it.packageName != requireContext().packageName }
            .map {
                AppInfo(
                    pm.getApplicationLabel(it).toString(),
                    it.packageName,
                    pm.getApplicationIcon(it.packageName),
                    selectedApps.contains(it.packageName)
                )
            }
            .sortedWith(compareBy({ !it.isSelected }, { it.appName.lowercase() }))
    }

    private fun updateSelectedApps(packageName: String, isSelected: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val selectedApps = prefs.getStringSet("selected_apps", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        if (isSelected) {
            selectedApps.add(packageName)
        } else {
            selectedApps.remove(packageName)
        }

        prefs.edit().putStringSet("selected_apps", selectedApps).apply()
    }
}