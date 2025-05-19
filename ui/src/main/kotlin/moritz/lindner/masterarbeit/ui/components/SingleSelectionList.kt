package moritz.lindner.masterarbeit.ui.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// TODO: switch to new component
@Composable
fun SingleSelectionList(onLayoutChange: (LayoutSelection) -> Unit) {
    var layouts by remember {
        mutableStateOf(
            listOf(
                LayoutSelection(
                    "Walker",
                    false,
                ),
                LayoutSelection(
                    "Walker Radial Tree",
                    false,
                ),
                LayoutSelection(
                    "Direct Angular Placement",
                    true,
                ),
            ),
        )
    }

    LazyRow {
        itemsIndexed(layouts) { i, item ->
            Text(item.name)
            RadioButton(
                selected = item.selected,
                onClick = {
                    layouts =
                        layouts.map {
                            if (it == item) {
                                it.copy(selected = true)
                            } else {
                                it.copy(selected = false)
                            }
                        }
                    onLayoutChange(item)
                },
            )
        }
    }
}
