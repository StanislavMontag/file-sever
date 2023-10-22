package org.example.client;

public class ServerResponse {
    String status;
    byte[] data;
    String message;

    public ServerResponse(String status, byte[] data) {
        this.status = status;
        this.data = data;
        this.message = (data == null) ? "" : new String(data);
    }
}
