import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomata
import moritz.lindner.masterarbeit.epa.builder.BPI2017ChallengeEventMapper
import moritz.lindner.masterarbeit.epa.builder.ExtendedPrefixAutomateBuilder
import moritz.lindner.masterarbeit.epa.visitor.StatisticsVisitor
import java.io.File

@Composable
fun App() {

    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf("Build EPA") }
    var epa by remember { mutableStateOf<ExtendedPrefixAutomata<Long>?>(null) }

    val file = File("./ui/src/main/resources/eventlogs/BPI Challenge 2017.xes.gz")
    val mapper = BPI2017ChallengeEventMapper()

    val epaBuilder = ExtendedPrefixAutomateBuilder<Long>()
        .setFile(file)
        .setEventLogMapper(mapper)

    MaterialTheme {

        if (epa != null) {
            BasicText(
                text = "epa: ${epa?.states?.size} states",
            )
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    val statistics = StatisticsVisitor<Long>(epa!!)
                    epa!!.acceptDepthFirst(statistics)
                    text = "${statistics.report()}"
                }
            }) {
                Text("Create statistics")
            }
        }else {
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    text = "Start Constructing"
                    epa = epaBuilder.build()
                    text = "End Constructing"
                }
            }) {
                Text(text)
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
