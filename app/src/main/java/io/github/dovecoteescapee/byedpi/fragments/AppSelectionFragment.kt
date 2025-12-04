package io.github.dovecoteescapee.byedpi.fragments

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.adapters.AppSelectionAdapter
import io.github.dovecoteescapee.byedpi.data.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSelectionFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: AppSelectionAdapter
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.app_selection_layout, container, false)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        progressBar = view.findViewById(R.id.progressBar)

        setupRecyclerView()
        setupSearchView()

        loadApps()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView.adapter = null
        searchView.setOnQueryTextListener(null)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
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

    private fun loadApps() {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val apps = withContext(Dispatchers.IO) {
                getInstalledApps()
            }

            adapter = AppSelectionAdapter(requireContext(), apps)
            recyclerView.adapter = adapter
            progressBar.visibility = View.GONE
            searchView.visibility = View.VISIBLE
        }
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm = requireContext().packageManager
        val installedApps = pm.getInstalledApplications(0)
        val selectedApps = prefs.getStringSet("selected_apps", setOf()) ?: setOf()

        return installedApps
            .filter { it.packageName != requireContext().packageName }
            .map { createAppInfo(it, pm, selectedApps) }
            .sortedWith(compareBy({ !it.isSelected }, { it.appName.lowercase() }))
    }

    private fun createAppInfo(
        appInfo: ApplicationInfo,
        pm: PackageManager,
        selectedApps: Set<String>
    ): AppInfo {
        val appName = try {
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            appInfo.packageName
        }

        val appIcon = try {
            pm.getApplicationIcon(appInfo.packageName)
        } catch (_: Exception) {
            pm.defaultActivityIcon
        }

        return AppInfo(
            appName,
            appInfo.packageName,
            appIcon,
            selectedApps.contains(appInfo.packageName)
        )
    }
}