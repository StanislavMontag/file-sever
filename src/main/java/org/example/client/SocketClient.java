package org.example.client;

public interface SocketClient {
    ServerResponse sendRequestToServer(String command, String argument);
    ServerResponse sendRequestToServer(String command, String argument, byte[] data);
}

