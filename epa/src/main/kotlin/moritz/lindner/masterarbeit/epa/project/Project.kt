package moritz.lindner.masterarbeit.epa.project

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2017OfferChallengeEventMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.BPI2018ChallengeMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.EventLogMapper
import moritz.lindner.masterarbeit.epa.construction.builder.xes.SampleEventMapper
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime

/**
 * Represents a project containing an XES file and its corresponding EPA.
 *
 * A project is stored as a folder structure containing:
 * - project.json (metadata)
 * - epas/unfiltered.json (unfiltered EPA)
 *
 * @param name The display name of the project
 * @param projectFolder Optional description of the project
 * @param createdAt When the project was created
 * @param xesFilePath Path to the XES file (can be anywhere on the
 *    filesystem)
 * @param unfilteredEpaPath Path to the unfiltered EPA json file (relative
 *    to project root)
 */
@Serializable
data class Project(
    val name: String,
    val projectFolder: String,
    val createdAt: String,
    val xesFilePath: String,
    val mapperName: String,
    val unfilteredEpaPath: String = "epas/unfiltered.json.gz",
) {

    companion object {
        const val PROJECT_METADATA_FILE = "project.json"
        const val SOURCE_DIR = "source"

        /**
         * Creates a new project with current timestamp, copies the XES file, and
         * saves metadata
         */
        fun create(
            name: String,
            projectFolder: String,
            xesFilePath: String,
            mapper: EventLogMapper<*>
        ): Project {
            val now = LocalDateTime.now().toString()
            val projectRoot = Path.of(projectFolder).toAbsolutePath().normalize()
            val sourceXesPath = Path.of(xesFilePath)

            // Validate source XES file exists
            require(Files.exists(sourceXesPath)) { "XES file not found: $xesFilePath" }
            // Create project folder structure
            require(!Files.exists(projectRoot)) { "There is already a directory at $projectRoot. Please choose another location." }

            Files.createDirectories(projectRoot)
            Files.createDirectories(projectRoot.resolve(SOURCE_DIR))

            val originalName = "${sourceXesPath.toFile().name}"

            // Copy XES file to project
            val targetXesPath = projectRoot.resolve(SOURCE_DIR).resolve(originalName)
            Files.copy(sourceXesPath, targetXesPath, StandardCopyOption.REPLACE_EXISTING)

            // Create project with relative path to copied XES file
            val project = Project(
                name = name,
                projectFolder = projectFolder,
                createdAt = now,
                xesFilePath = "$SOURCE_DIR/$originalName",
                mapperName = mapper.name
            )

            // Save project metadata
            project.saveMetadata()

            return project
        }

        /** Loads a project from its folder by reading project.json */
        fun loadFromFolder(projectRoot: Path): Project {
            val metadataPath = projectRoot.resolve(PROJECT_METADATA_FILE)
            if (!Files.exists(metadataPath)) {
                throw IllegalArgumentException("No project.json found in $projectRoot.")
            }

            val json = Json { ignoreUnknownKeys = true }
            val jsonContent = Files.readString(metadataPath)
            val project = json.decodeFromString<Project>(jsonContent)

            if (!Files.exists(project.getXesFilePath())) {
                throw IllegalArgumentException("Source event log not found: ${project.getXesFilePath()}")
            }

            return project
        }
    }

    /** Saves only the project metadata (project.json) to the specified folder */
    fun saveMetadata() {
        val projectRoot = getProjectRoot()
        Files.createDirectories(projectRoot)

        val json = Json { prettyPrint = true }
        val jsonContent = json.encodeToString(this)
        Files.writeString(getMetadataPath(projectRoot), jsonContent)
    }

    /** Returns the creation date as LocalDateTime */
    fun getCreatedAt(): LocalDateTime = LocalDateTime.parse(createdAt)

    /** Gets the XES file path (absolute) */
    fun getXesFilePath(): Path {
        val xesPath = Path.of(xesFilePath)
        return if (xesPath.isAbsolute) {
            xesPath
        } else {
            getProjectRoot().resolve(xesFilePath)
        }
    }

    /** Gets the project root as a Path */
    fun getProjectRoot(): Path = Path.of(projectFolder)

    /** Gets the full path to the unfiltered EPA file within the project */
    fun getUnfilteredEpaPath(): Path = getProjectRoot().resolve(unfilteredEpaPath)

    /** Gets the full path to the project metadata file */
    fun getMetadataPath(projectRoot: Path): Path = projectRoot.resolve(PROJECT_METADATA_FILE)

    /** Creates a copy of this project with updated name */
    fun withName(newName: String): Project = copy(name = newName)

    /** Creates a copy of this project with updated XES file path */
    fun withXesFilePath(newXesFilePath: String): Project = copy(xesFilePath = newXesFilePath)

    /** Gets the mapper name for this project */
    fun getMapper(): EventLogMapper<*> {
        val mappers = listOf(
            BPI2017ChallengeEventMapper(),
            BPI2017OfferChallengeEventMapper(),
            BPI2018ChallengeMapper(),
            SampleEventMapper()
        ).associateBy { it.name }

        return mappers[mapperName] ?: throw IllegalArgumentException("Unknown mapper name: $mapperName")
    }

    fun withMapper(mapper: EventLogMapper<*>): Project = copy(mapperName = mapper.name)
}