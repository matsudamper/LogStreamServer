package net.matsudamper.logstream.frontend

import java.io.File
import java.net.Inet4Address
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.matsudamper.logstream.backend.KeyUtil
import net.matsudamper.logstream.frontend.storage.Config

public class MainScreenViewModel(
    private val rootNavController: RootNavController,
    private val rootStore: RootStore,
    coroutineScope: CoroutineScope,
) : CoroutineScope by coroutineScope {
    private val viewModelStateFlow = MutableStateFlow(ViewModelState())
    public val uiStateFlow: StateFlow<MainScreenUiState> = MutableStateFlow(
        MainScreenUiState(
            ipv4Address = "",
            readyCaKey = false,
            runningServer = false,
            logs = listOf(),
            listener = object : MainScreenUiState.Listener {
                override fun onClickSettings() {
                    rootNavController.navigate(NavigationElement.Setting)
                }

                override fun onResume() {
                    val config = rootStore.getConfig()
                    val ipV4 = Inet4Address.getLocalHost()
                    viewModelStateFlow.update { viewModelState ->
                        viewModelState.copy(
                            config = config,
                            ipV4Address = ipV4.hostAddress,
                        )
                    }
                }

                override fun onClickShowSettingButton() {
                    rootNavController.navigate(NavigationElement.Setting)
                }

                override fun onClickStopServer() {
                    launch {
                        rootStore.stopServer()
                        viewModelStateFlow.update { viewModelState ->
                            viewModelState.copy(
                                runningServer = false,
                            )
                        }
                    }
                }

                override fun onClickStartServerButton() {
                    val ipV4Address = viewModelStateFlow.value.ipV4Address ?: return
                    val caKeyStore = KeyUtil.restoreCaKeyStore(
                        file = File(rootStore.getConfig().caKeyPath),
                        pass = Constant.filePass,
                    ) ?: return
                    rootStore.startServer(
                        caKeyStore = caKeyStore,
                        filePass = Constant.filePass,
                        host = ipV4Address,
                    )
                    viewModelStateFlow.update { viewModelState ->
                        viewModelState.copy(
                            runningServer = true,
                        )
                    }
                }
            },
        ),
    ).also { mutableStateFlow ->
        launch {
            viewModelStateFlow
                .mapNotNull { it.config?.caKeyPath }
                .stateIn(this)
                .collect { caKeyPath ->
                    val exists = File(caKeyPath).exists()
                    mutableStateFlow.update { stateFlow ->
                        stateFlow.copy(
                            readyCaKey = exists,
                        )
                    }
                }
        }
        launch {
            viewModelStateFlow.collect {
                mutableStateFlow.update { stateFlow ->
                    stateFlow.copy(
                        ipv4Address = it.ipV4Address,
                        runningServer = it.runningServer,
                    )
                }
            }
        }
        launch {
            rootStore.logFlow.collect { logs ->
                mutableStateFlow.update { stateFlow ->
                    stateFlow.copy(
                        logs = logs.map {
                            it.logs
                        }.reversed()
                    )
                }
            }
        }
    }.asStateFlow()

    private data class ViewModelState(
        val config: Config? = null,
        val ipV4Address: String? = null,
        val runningServer: Boolean = false,
    )
}
