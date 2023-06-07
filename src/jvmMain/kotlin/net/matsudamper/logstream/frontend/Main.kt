package net.matsudamper.logstream.frontend

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.system.exitProcess

public fun main(args: Array<String>) {
    System.setProperty("logback.configurationFile", "logback.xml")

    val rootStore = RootStore()
    application {
        val rootNavController by remember { mutableStateOf(RootNavController()) }

        Window(onCloseRequest = { exitProcess(0) }) {
            
            val navigationElement by rootNavController.currentScreen.collectAsState()

            val saveableStateHolder = rememberSaveableStateHolder()
            val coroutineScope = rememberCoroutineScope()
            val mainScreenViewModel = remember {
                MainScreenViewModel(
                    rootNavController = rootNavController,
                    rootStore = rootStore,
                    coroutineScope = coroutineScope,
                )
            }
            when (navigationElement) {
                NavigationElement.Main -> {
                    saveableStateHolder.SaveableStateProvider("Main") {
                        MainScreen(
                            modifier = Modifier.fillMaxSize(),
                            uiState = mainScreenViewModel.uiStateFlow.collectAsState().value,
                        )
                    }
                }

                NavigationElement.Setting -> {
                    val settingCoroutineScope = rememberCoroutineScope()
                    val settingViewModel = remember(settingCoroutineScope) {
                        SettingViewModel(
                            rootNavController = rootNavController,
                            coroutineScope = settingCoroutineScope,
                            rootStore = rootStore,
                        )
                    }
                    SettingScreen(
                        modifier = Modifier.fillMaxSize(),
                        uiState = settingViewModel.uiStateFlow.collectAsState().value,
                    )
                }
            }
        }
    }
}
