package org.example.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RequestHandler implements Runnable {
    private final Server server;
    private final Path rootPath = Paths.get(System.getProperty("user.dir"), "src", "server", "data");
    private final DataInputStream dis;
    private final DataOutputStream dos;

    RequestHandler(Socket socket, Server server) throws IOException {
        this.server = server;
        this.dis = new DataInputStream(socket.getInputStream());
        this.dos = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            String[] msg = dis.readUTF().split(" ");

            switch (msg[0]) {
                case "PUT":
                    handlePutRequest(msg);
                    break;
                case "GET":
                    handleGetRequest(msg);
                    break;
                case "DELETE":
                    handleDeleteRequest(msg);
                    break;
                case "EXIT":
                    server.stopServer();
                    break;
                default:
                    throw new IllegalStateException("Unexpected request: " + msg[0]);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void handlePutRequest(String[] msg) throws IOException {
        String fileName = msg.length > 1 ? msg[1] : generateFileName();
        Path filePath = rootPath.resolve(fileName);

        byte[] message = new byte[dis.readInt()];
        dis.readFully(message);

        if (!Files.exists(filePath)) {
            Files.write(filePath, message);
            int fileId = createId(fileName);
            dos.writeUTF("200 " + fileId);
            server.registerFile(fileId, fileName);
        } else {
            dos.writeUTF("403");
        }
    }

    private void handleGetRequest(String[] msg) throws IOException {
        Path filePath;
        switch (msg[1]) {
            case "BY_NAME":
                filePath = rootPath.resolve(msg[2]);
                sendFileResponse(filePath);
                break;
            case "BY_ID":
                int key = Integer.parseInt(msg[2]);
                if (server.containsFileId(key)) {
                    filePath = rootPath.resolve(server.getFileNameById(key));
                    sendFileResponse(filePath);
                } else {
                    dos.writeUTF("404");
                }
                break;
        }
    }

    private void handleDeleteRequest(String[] msg) throws IOException {
        Path filePath;
        switch (msg[1]) {
            case "BY_NAME":
                filePath = rootPath.resolve(msg[2]);
                if (Files.deleteIfExists(filePath)) {
                    dos.writeUTF("200");
                } else {
                    dos.writeUTF("404");
                }
                break;
            case "BY_ID":
                int key = Integer.parseInt(msg[2]);
                if (server.containsFileId(key)) {
                    filePath = rootPath.resolve(server.getFileNameById(key));
                    if (Files.deleteIfExists(filePath)) {
                        dos.writeUTF("200");
                    } else {
                        dos.writeUTF("404");
                    }
                } else {
                    dos.writeUTF("404");
                }
                break;
        }
    }

    private String generateFileName() {
        String currentName = "file_01";
        while (server.containsFileName(currentName)) {
            currentName = String.format("file_%02d", Integer.parseInt(currentName.split("_")[1]) + 1);
        }
        return currentName;
    }

    private static int createId(String s) {
        return s.chars().sum();
    }

    private void sendFileResponse(Path filePath) throws IOException {
        if (Files.exists(filePath)) {
            byte[] message = Files.readAllBytes(filePath);
            dos.writeUTF("200");
            dos.writeInt(message.length);
            dos.write(message);
        } else {
            dos.writeUTF("404");
        }
    }
}
