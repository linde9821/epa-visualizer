package moritz.lindner.masterarbeit.epa.builder.plugin

import org.deckfour.xes.model.XLog
import java.util.ServiceLoader

/**
 * Registry for event mapper plugins that handles discovery, registration, and retrieval of mappers.
 *
 * This class uses the Java ServiceLoader mechanism to automatically discover implementations
 * of [EventMapperPlugin] that are properly registered via the ServiceLoader SPI.
 */
object EventMapperRegistry {
    /**
     * Map of registered plugins by their ID.
     */
    private val plugins = mutableMapOf<String, EventMapperPlugin<*>>()

    /**
     * Initializes the registry by discovering plugins using ServiceLoader.
     */
    init {
        // Discover plugins using ServiceLoader
        ServiceLoader.load(EventMapperPlugin::class.java).forEach { plugin ->
            registerPlugin(plugin)
        }
    }

    /**
     * Registers a plugin with the registry.
     *
     * @param plugin The plugin to register.
     * @throws IllegalArgumentException if a plugin with the same ID is already registered.
     */
    fun registerPlugin(plugin: EventMapperPlugin<*>) {
        if (plugins.containsKey(plugin.id)) {
            throw IllegalArgumentException("Plugin with ID ${plugin.id} is already registered")
        }
        plugins[plugin.id] = plugin
    }

    /**
     * Gets a plugin by its ID.
     *
     * @param id The ID of the plugin to retrieve.
     * @return The plugin with the given ID, or null if no such plugin is registered.
     */
    fun getPlugin(id: String): EventMapperPlugin<*>? = plugins[id]

    /**
     * Gets all registered plugins.
     *
     * @return A list of all registered plugins.
     */
    fun getAllPlugins(): List<EventMapperPlugin<*>> = plugins.values.toList()

    /**
     * Finds a suitable plugin for the given log.
     *
     * This method tries each registered plugin to see if it can handle the given log.
     * It returns the first plugin that reports it can handle the log.
     *
     * @param log The XES log to find a plugin for.
     * @return A suitable plugin, or null if no plugin can handle the log.
     */
    fun findSuitablePlugin(log: XLog): EventMapperPlugin<*>? =
        plugins.values.firstOrNull { it.canHandle(log) }

    /**
     * Clears all registered plugins.
     * This is primarily useful for testing.
     */
    fun clearPlugins() {
        plugins.clear()
    }
}