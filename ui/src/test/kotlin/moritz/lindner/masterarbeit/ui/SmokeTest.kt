package moritz.lindner.masterarbeit.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class SmokeTest {
    @OptIn(ExperimentalLayoutApi::class, ExperimentalJewelApi::class)
    @Test
    fun `app launches without crashing`() {
        val mainThread =
            Thread {
                assertDoesNotThrow {
                    main()
                }
            }
        mainThread.start()
        Thread.sleep(2000)
        mainThread.interrupt()
        mainThread.join(2000)
    }
}
