package moritz.lindner.masterarbeit.ui.common

import moritz.lindner.masterarbeit.buildconfig.BuildConfig
import moritz.lindner.masterarbeit.ui.common.Constants.APPLICATION_NAME
import javax.swing.JOptionPane
import javax.swing.JOptionPane.showMessageDialog

object AboutPanel {
    fun showAboutDialog() {
        showMessageDialog(
            null,
            """
        $APPLICATION_NAME
        Version: ${BuildConfig.APP_VERSION}
        
        Interactive Visualization of Extended Prefix Automaton
        
        A tool for analyzing trace variants in large, complex event logs using 
        Extended Prefix Automata (EPA) and different semantic and hierarchical tree layouts.
        This visualization approach encodes thousands of trace variants while 
        minimizing visual clutter, supporting interactive filtering and analysis.
        
        Built as part of a Master's Thesis at Humboldt-Universität zu Berlin.
        
        This application and the master thesis is currently under development and not finished.
        
        Built with Kotlin and Compose Desktop
        Process Mining • Event Log Visualization • Variant Analysis
        
        Author: Moritz Lindner
        Supervisor: Prof. Dr. Jan Mendling
        
        GitHub: https://github.com/linde9821/epa-visualizer
        
        © 2025 Moritz Lindner
        """.trimIndent(),
            "About $APPLICATION_NAME",
            JOptionPane.INFORMATION_MESSAGE
        )
    }
}