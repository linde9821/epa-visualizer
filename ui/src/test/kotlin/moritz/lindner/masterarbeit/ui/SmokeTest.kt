package moritz.lindner.masterarbeit.ui

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class SmokeTest {
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
