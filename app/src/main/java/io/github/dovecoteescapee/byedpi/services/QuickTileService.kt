package io.github.dovecoteescapee.byedpi.services

import android.net.VpnService
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import io.github.dovecoteescapee.byedpi.data.*
import io.github.dovecoteescapee.byedpi.utility.getPreferences
import io.github.dovecoteescapee.byedpi.utility.mode

@RequiresApi(Build.VERSION_CODES.N)
class QuickTileService : TileService() {

    companion object {
        private const val TAG = "QuickTileService"
    }

    private var appTile: Tile? = null

    override fun onTileAdded() {
        super.onTileAdded()
        Log.i(TAG, "Tile added")
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        Log.i(TAG, "Tile removed")
    }

    override fun onStartListening() {
        super.onStartListening()
        appTile = qsTile
        updateStatus()
    }

    override fun onStopListening() {
        super.onStopListening()
        appTile = null
    }

    override fun onClick() {
        super.onClick()
        handleClick()
    }

    private fun handleClick() {
        val (status) = appStatus

        when (status) {
            AppStatus.Halted -> {
                val mode = getPreferences().mode()

                if (mode == Mode.VPN && VpnService.prepare(this) != null) {
                    return
                }

                ServiceManager.start(this, mode)
                setState(Tile.STATE_ACTIVE)
            }
            AppStatus.Running -> {
                ServiceManager.stop(this)
                setState(Tile.STATE_INACTIVE)
            }
        }

        Log.i(TAG, "Toggle tile")
    }

    private fun updateStatus() {
        val (status) = appStatus

        if (status == AppStatus.Running) {
            setState(Tile.STATE_ACTIVE)
        } else {
            setState(Tile.STATE_INACTIVE)
        }
    }

    private fun setState(newState: Int) {
        appTile?.apply {
            state = newState
            updateTile()
        }
    }
}
