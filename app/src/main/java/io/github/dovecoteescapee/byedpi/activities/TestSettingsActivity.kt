package io.github.dovecoteescapee.byedpi.activities

import android.os.Bundle
import android.view.MenuItem
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.fragments.ProxyTestSettingsFragment

class TestSettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.test_settings, ProxyTestSettingsFragment())
            .commit()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            onBackPressedDispatcher.onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}