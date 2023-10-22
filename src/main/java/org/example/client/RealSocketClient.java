package org.example.client;

import java.io.*;
import java.net.Socket;

public class RealSocketClient implements SocketClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 23456;
    private static final String REQUEST_MSG = "The request was sent.";

    @Override
    public ServerResponse sendRequestToServer(String command, String argument) {
        return sendRequestToServer(command, argument, null);
    }

    @Override
    public ServerResponse sendRequestToServer(String command, String argument, byte[] data) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {

            output.writeUTF(command + (argument == null ? "" : " " + argument));
            if (data != null) {
                output.writeInt(data.length);
                output.write(data);
                output.flush();
            }

            System.out.println(REQUEST_MSG);

            String status = input.readUTF();
            int length = input.readInt();
            byte[] receivedData = new byte[length];
            input.readFully(receivedData);

            return new ServerResponse(status, receivedData);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return new ServerResponse("500", null);
        }
    }
}
