package moritz.lindner.masterarbeit.epa.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CsvFileTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `writes header, mixed-type rows and quotes special characters`() {
        val file = File(tempDir, "out.csv")

        writeCsv(file) {
            writeRow("name", "count", "ratio")
            writeRow("a,b", 3, 0.5)
            writeRows(listOf(listOf("x", null, 1L)))
        }

        assertThat(file.readText()).isEqualTo(
            "name,count,ratio\r\n\"a,b\",3,0.5\r\nx,,1\r\n"
        )
    }
}
