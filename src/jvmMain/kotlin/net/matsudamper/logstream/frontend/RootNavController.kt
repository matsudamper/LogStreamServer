package net.matsudamper.logstream.frontend

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

public class RootNavController {
    private val _currentScreen: MutableStateFlow<NavigationElement> = MutableStateFlow(
        NavigationElement.Main,
    )
    public val currentScreen: StateFlow<NavigationElement> = _currentScreen.asStateFlow()
    public fun navigate(element: NavigationElement) {
        _currentScreen.value = element
    }

    private val _dialogFlow: MutableStateFlow<List<Dialog>> = MutableStateFlow(listOf())
    public val dialogFlow: StateFlow<List<Dialog>> = _dialogFlow.asStateFlow()

    public fun dialog(title: String, message: String) {
        val dialog = Dialog(title, message)
        _dialogFlow.update {
            it.plus(dialog)
        }
    }

    public fun consumeDialog(dialog: Dialog) {
        _dialogFlow.update {
            it.minus(dialog)
        }
    }

    public data class Dialog(
        val title: String,
        val message: String,
    )
}
