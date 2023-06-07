package net.matsudamper.logstream.frontend

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public class RootNavController {
    private val _currentScreen: MutableStateFlow<NavigationElement> = MutableStateFlow(
        NavigationElement.Main
    )
    public val currentScreen: StateFlow<NavigationElement> = _currentScreen.asStateFlow()
    public fun navigate(element: NavigationElement) {
        _currentScreen.value = element
    }
}
