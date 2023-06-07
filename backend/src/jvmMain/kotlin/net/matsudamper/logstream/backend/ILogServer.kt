package net.matsudamper.logstream.backend

public interface ILogServer {
    public fun receiveLog(log: Log)
}