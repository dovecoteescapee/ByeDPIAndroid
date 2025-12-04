package io.github.dovecoteescapee.byedpi.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.preference.*
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.utility.findPreferenceNotNull
import androidx.appcompat.app.AlertDialog
import io.github.dovecoteescapee.byedpi.data.Command
import io.github.dovecoteescapee.byedpi.utility.HistoryUtils

class ByeDpiCommandLineSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var cmdHistoryUtils: HistoryUtils
    private lateinit var editTextPreference: EditTextPreference
    private lateinit var historyHeader: Preference
    private lateinit var clearButton: Preference
    private val historyPreferences = mutableListOf<Preference>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val uiScale = resources.displayMetrics.density
        val threshold = 3f

        val overlay = if (uiScale > threshold) R.style.Theme_ByeDPI_History_NoPaddingLeft
        else R.style.Theme_ByeDPI_History_PaddingLeft

        requireActivity().theme.applyStyle(overlay, true)
        setPreferencesFromResource(R.xml.byedpi_cmd_settings, rootKey)

        cmdHistoryUtils = HistoryUtils(requireContext())

        editTextPreference = findPreferenceNotNull("byedpi_cmd_args")
        historyHeader = findPreferenceNotNull("cmd_history_header")
        clearButton = findPreferenceNotNull("clear_cmd_args")

        editTextPreference.setOnPreferenceChangeListener { _, newValue ->
            val newCommand = newValue.toString()
            if (newCommand.isNotBlank()) cmdHistoryUtils.addCommand(newCommand)
            updateHistoryItems()
            true
        }

        clearButton.setOnPreferenceClickListener {
            editTextPreference.text = ""
            true
        }

        historyHeader.setOnPreferenceClickListener {
            showHistoryClearDialog()
            true
        }

        updateHistoryItems()
    }

    private fun updateHistoryItems() {
        historyPreferences.forEach { preference ->
            preferenceScreen.removePreference(preference)
        }

        historyPreferences.clear()
        val history = cmdHistoryUtils.getHistory()
        historyHeader.isVisible = history.isNotEmpty()

        if (history.isNotEmpty()) {
            history.sortedWith(compareByDescending<Command> { it.pinned }.thenBy { history.indexOf(it) })
                .forEachIndexed { _, command ->
                    val preference = createPreference(command)
                    historyPreferences.add(preference)
                    preferenceScreen.addPreference(preference)
                }
        }
    }

    private fun createPreference(command: Command) =
        Preference(requireContext()).apply {
            title = command.text
            summary = buildSummary(command)
            layoutResource = R.layout.history_item
            setOnPreferenceClickListener {
                showActionDialog(command)
                true
            }
        }

    private fun buildSummary(command: Command): String {
        val summary = StringBuilder()
        if (command.name != null) {
            summary.append(command.name)
        }
        if (command.pinned) {
            if (summary.isNotEmpty()) summary.append(" - ")
            summary.append(context?.getString(R.string.cmd_history_pinned))
        }
        return summary.toString()
    }

    private fun showHistoryClearDialog() {
        val options = arrayOf(
            getString(R.string.cmd_history_delete_unpinned),
            getString(R.string.cmd_history_delete_all),
        )

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.cmd_history_menu))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> deleteUnpinnedHistory()
                    1 -> deleteAllHistory()
                }
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }

    private fun deleteAllHistory() {
        cmdHistoryUtils.clearAllHistory()
        updateHistoryItems()
    }

    private fun deleteUnpinnedHistory() {
        cmdHistoryUtils.clearUnpinnedHistory()
        updateHistoryItems()
    }

    private fun showActionDialog(command: Command) {
        val options = arrayOf(
            getString(R.string.cmd_history_apply),
            if (command.pinned) getString(R.string.cmd_history_unpin) else getString(R.string.cmd_history_pin),
            getString(R.string.cmd_history_rename),
            getString(R.string.cmd_history_edit),
            getString(R.string.cmd_history_copy),
            getString(R.string.cmd_history_delete)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.cmd_history_menu))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> applyCommand(command.text)
                    1 -> if (command.pinned) unpinCommand(command.text) else pinCommand(command.text)
                    2 -> showRenameDialog(command)
                    3 -> showEditDialog(command)
                    4 -> copyToClipboard(command.text)
                    5 -> deleteCommand(command.text)
                }
            }
            .show()
    }

    private fun showRenameDialog(command: Command) {
        val input = EditText(requireContext()).apply {
            setText(command.name)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            addView(input)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.cmd_history_rename))
            .setView(container)
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                val newName = input.text.toString()
                if (newName != command.name) {
                    cmdHistoryUtils.renameCommand(command.text, newName)
                    updateHistoryItems()
                }
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }

    private fun showEditDialog(command: Command) {
        val input = EditText(requireContext()).apply {
            setText(command.text)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            addView(input)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.cmd_history_edit))
            .setView(container)
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                val newText = input.text.toString()
                if (newText.isNotBlank() && newText != command.text) {
                    cmdHistoryUtils.editCommand(command.text, newText)
                    if (editTextPreference.text == command.text) applyCommand(newText)
                    updateHistoryItems()
                }
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }

    private fun applyCommand(command: String) {
        editTextPreference.text = command
    }

    private fun pinCommand(command: String) {
        cmdHistoryUtils.pinCommand(command)
        updateHistoryItems()
    }

    private fun unpinCommand(command: String) {
        cmdHistoryUtils.unpinCommand(command)
        updateHistoryItems()
    }

    private fun deleteCommand(command: String) {
        cmdHistoryUtils.deleteCommand(command)
        updateHistoryItems()
    }

    private fun copyToClipboard(command: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Command", command)
        clipboard.setPrimaryClip(clip)
    }
}