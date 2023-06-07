package net.matsudamper.logstream.frontend

public sealed interface NavigationElement {
    public object Main : NavigationElement
    public object Setting : NavigationElement
}
