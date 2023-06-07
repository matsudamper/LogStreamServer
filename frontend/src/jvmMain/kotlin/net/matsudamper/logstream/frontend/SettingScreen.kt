package net.matsudamper.logstream.frontend

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

public data class SettingScreenUiState(
    val caKeyPath: TextFieldValue,
    val listener: Listener,
) {

    public interface Listener {
        public fun onClickBack()
        public fun onChangeCaKeyPath(text: TextFieldValue)
        public fun onClickCaKeyGenerateButton()
        public fun onClickCaKeyPathSelectButton()
    }
}

@Composable
public fun SettingScreen(
    modifier: Modifier,
    uiState: SettingScreenUiState,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                modifier = Modifier.height(38.dp),
            ) {
                IconButton(
                    modifier = Modifier.semantics {
                        contentDescription = "Back"
                    },
                    onClick = {
                        uiState.listener.onClickBack()
                    },
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(24.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text("CA Key Path")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextField(
                        modifier = Modifier.weight(1f),
                        value = uiState.caKeyPath,
                        onValueChange = {
                            uiState.listener.onChangeCaKeyPath(it)
                        },
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = {
                            uiState.listener.onClickCaKeyGenerateButton()
                        }
                    ) {
                        Text("生成")
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = {
                            uiState.listener.onClickCaKeyPathSelectButton()
                        }
                    ) {
                        Text("...")
                    }
                }
            }
        }
    }
}