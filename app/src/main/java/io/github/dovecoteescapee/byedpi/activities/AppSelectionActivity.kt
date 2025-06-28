package io.github.dovecoteescapee.byedpi.activities

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.dovecoteescapee.byedpi.adapters.AppListAdapter
import io.github.dovecoteescapee.byedpi.databinding.ActivityAppSelectionBinding
import io.github.dovecoteescapee.byedpi.models.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private lateinit var appListAdapter: AppListAdapter
    private val appList = mutableListOf<AppInfo>()
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val TAG = "AppSelectionActivity"
        const val PREFS_NAME = "byedpi_app_selection_prefs"
        const val KEY_SELECTED_APPS = "selected_apps"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(io.github.dovecoteescapee.byedpi.R.string.title_activity_app_selection)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        binding.appsRecyclerView.layoutManager = LinearLayoutManager(this)
        appListAdapter = AppListAdapter(appList) { appInfo, isSelected ->
            // Update SharedPreferences when selection changes
            val selectedApps = sharedPreferences.getStringSet(KEY_SELECTED_APPS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            if (isSelected) {
                selectedApps.add(appInfo.packageName)
            } else {
                selectedApps.remove(appInfo.packageName)
            }
            sharedPreferences.edit().putStringSet(KEY_SELECTED_APPS, selectedApps).apply()
            Log.d(TAG, "Selected apps updated: $selectedApps")
        }
        binding.appsRecyclerView.adapter = appListAdapter

        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        GlobalScope.launch(Dispatchers.IO) {
            val pm = packageManager
            val installedApplications = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val previouslySelectedApps = sharedPreferences.getStringSet(KEY_SELECTED_APPS, emptySet()) ?: emptySet()
            val currentAppList = mutableListOf<AppInfo>()

            for (appInfo in installedApplications) {
                // Filter out system apps if desired, for now, list all launchable apps
                if (pm.getLaunchIntentForPackage(appInfo.packageName) != null) {
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    val appIcon = pm.getApplicationIcon(appInfo)
                    val packageName = appInfo.packageName
                    val isSelected = previouslySelectedApps.contains(packageName)
                    currentAppList.add(AppInfo(appName, packageName, appIcon, isSelected))
                }
            }
            // Sort apps by name
            currentAppList.sortBy { it.name.lowercase() }

            withContext(Dispatchers.Main) {
                appList.clear()
                appList.addAll(currentAppList)
                appListAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
