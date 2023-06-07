package net.matsudamper.logstream.frontend

import androidx.compose.ui.text.input.TextFieldValue
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.matsudamper.logstream.backend.KeyUtil

public class SettingViewModel(
    private val rootNavController: RootNavController,
    private val rootStore: RootStore,
    coroutineScope: CoroutineScope,
) : CoroutineScope by coroutineScope {
    private val viewModelStateFlow = MutableStateFlow(ViewModelState())
    private val uiStateListener by lazy {
        object : SettingScreenUiState.Listener {
            private val caKeyPathBuffer = Channel<String>(
                capacity = Channel.RENDEZVOUS,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )

            init {
                launch {
                    caKeyPathBuffer.receiveAsFlow().collect { text ->
                        delay(1000)
                        rootStore.setConfig { config ->
                            config.copy(
                                caKeyPath = text,
                            )
                        }
                    }
                }
            }

            override fun onClickBack() {
                rootNavController.navigate(NavigationElement.Main)
            }

            override fun onChangeCaKeyPath(text: TextFieldValue) {
                caKeyPathBuffer.trySend(text.text)
                viewModelStateFlow.update {
                    it.copy(
                        caKeyPath = text,
                    )
                }
            }

            override fun onClickCaKeyGenerateButton() {
                launch {
                    val keyStore = KeyUtil.createCaKeyStore(Constant.keyAlias, Constant.keyPass)
                    keyStore.store(File(Constant.keyFileName).outputStream(), Constant.filePass.toCharArray())
                    rootStore.setConfig {
                        it.copy(
                            caKeyPath = Constant.keyFileName,
                        )
                    }
                }
            }

            override fun onClickCaKeyPathSelectButton() {

            }
        }
    }
    public val uiStateFlow: StateFlow<SettingScreenUiState> = MutableStateFlow(
        SettingScreenUiState(
            caKeyPath = TextFieldValue(),
            listener = uiStateListener,
        ),
    ).also { mutableUiStateFlow ->
        launch {
            viewModelStateFlow.collect {
                mutableUiStateFlow.value = mutableUiStateFlow.value.copy(
                    caKeyPath = it.caKeyPath,
                )
            }
        }
    }.asStateFlow()

    init {
        viewModelStateFlow.update {
            it.copy(
                caKeyPath = TextFieldValue(rootStore.getConfig().caKeyPath),
            )
        }
    }

    private data class ViewModelState(
        val caKeyPath: TextFieldValue = TextFieldValue(),
    )
}
