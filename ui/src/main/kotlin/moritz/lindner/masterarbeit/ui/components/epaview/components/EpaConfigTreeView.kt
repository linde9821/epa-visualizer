package moritz.lindner.masterarbeit.ui.components.epaview.components

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.foundation.lazy.tree.TreeBuilder
import org.jetbrains.jewel.foundation.lazy.tree.buildTree
import org.jetbrains.jewel.foundation.lazy.tree.rememberTreeState
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.LazyTree
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.typography

//@OptIn(ExperimentalJewelApi::class)
//@Composable
//fun EpaConfigTreeView(
//    viewModel: ProjectStateManager,
//    onConfigClick: (EpaViewConfig) -> Unit = {},
//    onConfigDoubleClick: (EpaViewConfig) -> Unit = {},
//    modifier: Modifier = Modifier.Companion
//) {
//    val rootConfig by viewModel.rootConfig.collectAsState()
//    val allConfigs by viewModel.allConfigs.collectAsState()
//
//    // Rebuild tree when rootConfig or any config changes
//    val tree by remember(rootConfig, allConfigs.size) {
//        derivedStateOf { buildEpaTree(rootConfig) }
//    }
//
//    val treeState = rememberTreeState()
//
//    // Clean up open nodes when the tree changes
//    LaunchedEffect(tree) {
//        treeState.openNodes.forEach(treeState::toggleNode)
//    }
//
//    val borderColor = if (JewelTheme.Companion.isDark) {
//        JewelTheme.Companion.colorPalette.grayOrNull(3) ?: Color(0xFF393B40)
//    } else {
//        JewelTheme.Companion.colorPalette.grayOrNull(12) ?: Color(0xFFEBECF0)
//    }
//
//    Box(modifier.border(1.dp, borderColor, RoundedCornerShape(2.dp))) {
//        if (rootConfig != null) {
//            LazyTree(
//                tree = tree,
//                treeState = treeState,
//                modifier = Modifier.Companion.fillMaxSize().focusable(),
//                onElementClick = { s ->
//                    onConfigClick(s.data)
//                },
//                onElementDoubleClick = { onConfigDoubleClick(it.data) },
//            ) { element ->
//                val config = element.data
//
//                Row(
//                    modifier = Modifier.Companion.fillMaxWidth().padding(2.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment = Alignment.Companion.CenterVertically
//                ) {
//                    Text(
//                        text = config.name,
//                        style = JewelTheme.Companion.typography.medium
//                    )
//
//                    // Show child count
//                    if (config.children.isNotEmpty()) {
//                        Text(
//                            text = "(${config.children.size})",
//                            style = JewelTheme.Companion.typography.small,
//                            color = JewelTheme.Companion.contentColor.copy(alpha = 0.6f)
//                        )
//                    }
//                }
//            }
//        } else {
//            Box(
//                modifier = Modifier.Companion.fillMaxSize(),
//                contentAlignment = Alignment.Companion.Center
//            ) {
//                Text("No EPA loaded")
//            }
//        }
//    }
//}