# Event Mapper Plugin Architecture

This package provides a plugin architecture for event mappers, making it easier to add support for new event log formats.

## Overview

The plugin architecture consists of:

1. `EventMapperPlugin` - An abstract base class that extends `EventLogMapper` with plugin metadata and format detection
2. `EventMapperRegistry` - A registry that discovers, registers, and provides access to event mapper plugins
3. Concrete plugin implementations in the `mappers` package
4. Integration with the `ExtendedPrefixAutomataBuilder` for automatic mapper selection

## Using Event Mapper Plugins

### Automatic Mapper Selection

The `ExtendedPrefixAutomataBuilder` can automatically select an appropriate mapper for a given log file:

```kotlin
val builder = ExtendedPrefixAutomataBuilder<Long>()
    .useAutoDetectMapper()
    .setFile(logFile)
    .build()
```

### Manual Mapper Selection

You can still manually select a specific mapper if needed:

```kotlin
val mapper = EventMapperRegistry.getPlugin("bpi2017-challenge") as EventMapperPlugin<Long>
val builder = ExtendedPrefixAutomataBuilder<Long>()
    .setEventLogMapper(mapper)
    .setFile(logFile)
    .build()
```

### Listing Available Mappers

To get a list of all available mappers:

```kotlin
val mappers = EventMapperRegistry.getAllPlugins()
mappers.forEach { plugin ->
    println("${plugin.id}: ${plugin.name} - ${plugin.description}")
}
```

## Creating a New Event Mapper Plugin

To add support for a new event log format:

1. Create a new class that extends `EventMapperPlugin<T>` (where `T` is the timestamp type)
2. Implement the required abstract properties and methods:
   - `id` - A unique identifier for the plugin
   - `name` - A human-readable name
   - `description` - A description of what log format the plugin supports
   - `map(xEvent, xTrace)` - Logic to convert XES events to domain events
   - `canHandle(log)` - Logic to determine if the plugin can handle a given log format
3. Register the plugin with the ServiceLoader by adding its fully qualified class name to:
   `META-INF/services/moritz.lindner.masterarbeit.epa.builder.plugin.EventMapperPlugin`

### Example Plugin Implementation

```kotlin
class MyCustomEventMapperPlugin : EventMapperPlugin<Long>() {
    override val id: String = "my-custom-format"
    override val name: String = "My Custom Format Mapper"
    override val description: String = "Maps events from my custom log format"

    override fun map(xEvent: XEvent, xTrace: XTrace): Event<Long> =
        Event(
            activity = Activity((xEvent.attributes["my-activity-attr"] as XAttributeLiteralImpl).value),
            timestamp = (xEvent.attributes["my-timestamp-attr"] as XAttributeTimestampImpl).value.time,
            caseIdentifier = (xTrace.attributes["my-case-attr"] as XAttributeLiteralImpl).value,
        )

    override fun canHandle(log: XLog): Boolean {
        if (log.isEmpty()) return false
        
        val firstTrace = log.first()
        if (firstTrace.isEmpty()) return false
        
        if (!firstTrace.attributes.containsKey("my-case-attr")) return false
        
        val firstEvent = firstTrace.first()
        return firstEvent.attributes.containsKey("my-activity-attr") && 
               firstEvent.attributes.containsKey("my-timestamp-attr")
    }
}
```

## Best Practices

1. Make your `canHandle` method robust - check for empty logs, traces, and missing attributes
2. Provide a descriptive `id`, `name`, and `description` to help users understand what your plugin does
3. Consider adding logging to help diagnose issues with your mapper
4. Handle edge cases gracefully, such as missing attributes or unexpected data formats
5. Add unit tests for your mapper to ensure it correctly handles the expected log format