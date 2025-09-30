package moritz.lindner.masterarbeit.ui.components.newproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.typography

@Composable
fun ProjectInfoStep(
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val state = rememberTextFieldState(projectName)

    LaunchedEffect(state.text) {
        onProjectNameChange(state.text.toString())
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Enter project name", style = JewelTheme.Companion.typography.h3TextStyle)

        TextField(
            state = state,
            enabled = true,
            readOnly = false,
        )

        DefaultButton(
            onClick = onNext,
            enabled = projectName.isNotBlank()
        ) {
            Text("Next")
        }
    }
}