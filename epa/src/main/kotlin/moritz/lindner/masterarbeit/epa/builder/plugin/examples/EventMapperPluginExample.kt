package moritz.lindner.masterarbeit.epa.builder.plugin.examples

import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomataBuilder
import moritz.lindner.masterarbeit.epa.builder.plugin.EventMapperRegistry
import java.io.File

/**
 * Example demonstrating how to use the event mapper plugin architecture.
 *
 * This class provides static methods that show different ways to use the plugin system:
 * - Automatic mapper selection
 * - Manual mapper selection
 * - Listing available mappers
 */
object EventMapperPluginExample {
    /**
     * Demonstrates automatic mapper selection based on log format.
     *
     * @param logFile The XES log file to process.
     */
    fun buildWithAutoDetection(logFile: File) {
        println("Building EPA with auto-detected mapper...")
        
        val epa = ExtendedPrefixAutomataBuilder<Long>()
            .useAutoDetectMapper()
            .setFile(logFile)
            .build()
            
        println("EPA built successfully with ${epa.states.size} states and ${epa.transitions.size} transitions")
    }
    
    /**
     * Demonstrates manual mapper selection by ID.
     *
     * @param logFile The XES log file to process.
     * @param mapperId The ID of the mapper to use.
     */
    fun buildWithSpecificMapper(logFile: File, mapperId: String) {
        println("Building EPA with specific mapper: $mapperId")
        
        val plugin = EventMapperRegistry.getPlugin(mapperId)
            ?: throw IllegalArgumentException("No mapper found with ID: $mapperId")
            
        println("Using mapper: ${plugin.name} - ${plugin.description}")
        
        @Suppress("UNCHECKED_CAST")
        val epa = ExtendedPrefixAutomataBuilder<Long>()
            .setEventLogMapper(plugin as moritz.lindner.masterarbeit.epa.builder.EventLogMapper<Long>)
            .setFile(logFile)
            .build()
            
        println("EPA built successfully with ${epa.states.size} states and ${epa.transitions.size} transitions")
    }
    
    /**
     * Lists all available event mapper plugins.
     */
    fun listAvailableMappers() {
        println("Available event mapper plugins:")
        
        EventMapperRegistry.getAllPlugins().forEach { plugin ->
            println("- ${plugin.id}: ${plugin.name}")
            println("  ${plugin.description}")
            println()
        }
    }
    
    /**
     * Main method to run the examples.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        // List all available mappers
        listAvailableMappers()
        
        // If a log file path is provided as an argument, run the examples with it
        if (args.isNotEmpty()) {
            val logFile = File(args[0])
            if (logFile.exists()) {
                // Try auto-detection
                buildWithAutoDetection(logFile)
                
                // Try with a specific mapper (sample mapper as an example)
                buildWithSpecificMapper(logFile, "sample")
            } else {
                println("Log file not found: ${args[0]}")
            }
        } else {
            println("No log file provided. Run with a path to an XES log file as an argument to see the full example.")
        }
    }
}