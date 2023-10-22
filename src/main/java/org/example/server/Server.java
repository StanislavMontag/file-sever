package org.example.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

public class Server {
    private static final int SERVER_PORT = 23456;
    private static final Path ROOT_PATH = Paths.get(System.getProperty("user.dir"), "src", "server", "data");

    private final ConcurrentHashMap<Integer, String> fileRegistry;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ServerStateStorage stateStorage;

    private volatile boolean isRunning = true;

    public Server(ServerStateStorage stateStorage) {
        this.stateStorage = stateStorage;
        this.fileRegistry = new ConcurrentHashMap<>(stateStorage.loadServerState());
    }

    public static void main(String[] args) {
        ServerStateStorage stateStorage = new ServerStateStorage(ROOT_PATH.resolve("server_state.ser"));
        new Server(stateStorage).startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            while (isRunning) {
                Socket socket = serverSocket.accept();
                threadPool.submit(new RequestHandler(socket, this));
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
        }
    }

    public void stopServer() {
        isRunning = false;
        stateStorage.saveServerState(fileRegistry);
        threadPool.shutdown();
        System.exit(0);
    }


    public String getFileNameById(int id) {
        return fileRegistry.get(id);
    }

    public boolean containsFileId(int id) {
        return fileRegistry.containsKey(id);
    }

    public void registerFile(int id, String fileName) {
        fileRegistry.put(id, fileName);
    }

    public boolean containsFileName(String fileName) {
        return fileRegistry.containsValue(fileName);
    }
}
