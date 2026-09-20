package moritz.lindner.masterarbeit.ui.common

import moritz.lindner.masterarbeit.buildconfig.BuildConfig
import moritz.lindner.masterarbeit.ui.common.Constants.APPLICATION_NAME_LONG
import javax.swing.JOptionPane
import javax.swing.JOptionPane.showMessageDialog

object AboutPanel {
    fun showAboutDialog() {
        showMessageDialog(
            null,
            """
        $APPLICATION_NAME_LONG
        Version: ${BuildConfig.APP_VERSION}
        
        Interactive Visualization of Extended Prefix Automaton
        
        A tool for analyzing trace variants in large, complex event logs using 
        enhanced Extended Prefix Automata (EPA*) and different semantic and hierarchical tree layouts.
        This visualization approach encodes thousands of trace variants while 
        minimizing visual clutter, supporting interactive filtering and analysis.
        
        Built as part of a Master's Thesis at Humboldt-Universität zu Berlin.
        
        This application.
        
        Built with Kotlin and Compose Desktop
        Process mining • Variant Analysis • Visual analytics • Extended Prefix Automata • Filter • Layout
        
        Author: Moritz Lindner
        Supervisor: Prof. Dr. Jan Mendling
        
        GitHub: https://github.com/linde9821/epa-visualizer
        
        © 2025-2026 Moritz Lindner
        """.trimIndent(),
            "About $APPLICATION_NAME_LONG",
            JOptionPane.INFORMATION_MESSAGE
        )
    }
}