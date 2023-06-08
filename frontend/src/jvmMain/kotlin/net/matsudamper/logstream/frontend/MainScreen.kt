package net.matsudamper.logstream.frontend

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp

public data class MainScreenUiState(
    val readyCaKey: Boolean,
    val ipv4Address: String?,
    val listener: Listener,
    val runningServer: Boolean,
    val logs: List<List<String>>,
) {

    @Immutable
    public interface Listener {
        public fun onClickSettings()
        public fun onResume()
        public fun onClickStartServerButton()
        public fun onClickShowSettingButton()
        public fun onClickStopServer()
    }
}

@Composable
public fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: MainScreenUiState,
) {
    LaunchedEffect(Unit) {
        uiState.listener.onResume()
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(modifier = Modifier.height(48.dp)) {
                Spacer(Modifier.width(12.dp))
                Text("LogStream")
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(uiState.ipv4Address.orEmpty())
                    if (uiState.runningServer) {
                        IconButton(
                            modifier = Modifier.semantics {
                                contentDescription = "Stop Server"
                            },
                            onClick = {
                                uiState.listener.onClickStopServer()
                            },
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .border(
                                        BorderStroke(2.dp, Color.Red),
                                        shape = RectangleShape,
                                    ),
                            )
                        }

                    } else if (uiState.readyCaKey) {
                        IconButton(
                            onClick = {
                                uiState.listener.onClickStartServerButton()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                tint = Color.Green,
                                contentDescription = "Start Server",
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                uiState.listener.onClickShowSettingButton()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Show Setting",
                                tint = Color.Red,
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomBar(
                modifier = Modifier.height(32.dp),
                onClickSettings = {
                    uiState.listener.onClickSettings()
                },
            )
        },
    ) {
        var columSizeList by remember { mutableStateOf(mapOf<Int, Int>()) }
        val density = LocalDensity.current
        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .padding(it)
                .horizontalScroll(rememberScrollState()),
        ) {
            itemsIndexed(uiState.logs) { rowIndex, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEachIndexed { columnIndex, column ->
                        Row(
                            modifier = Modifier
                                .background(if (columnIndex % 2 == 0) Color.LightGray else Color.Transparent),
                        ) {
                            Text(
                                modifier = Modifier
                                    .then(
                                        run {
                                            val size = columSizeList[columnIndex]
                                            if (size != null) {
                                                Modifier.width(
                                                    with(density) { size.toDp() }
                                                        .coerceAtLeast(50.dp),
                                                )
                                            } else {
                                                Modifier
                                            }
                                        },
                                    )
                                    .onSizeChanged {
                                        if (columSizeList[columnIndex] != null) return@onSizeChanged
                                        columSizeList = columSizeList.plus(columnIndex to it.width)
                                    },
                                text = column,
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    onClickSettings: () -> Unit,
) {
    Row(modifier = modifier) {
        Surface(
            modifier = Modifier.weight(1f)
                .fillMaxHeight(),
            color = Color.LightGray,
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
            ) {

            }
        }

        IconButton(
            modifier = Modifier
                .fillMaxHeight()
                .semantics {
                    contentDescription = "Open Setting"
                },
            onClick = { onClickSettings() },
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
        }
    }
}
