package org.example.server;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerStateStorage {

    private final Path storagePath;

    public ServerStateStorage(Path storagePath) {
        this.storagePath = storagePath;
    }

    public void saveServerState(ConcurrentHashMap<Integer, String> fileRegistry) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(storagePath.toFile()))) {
            oos.writeObject(fileRegistry);
        } catch (IOException e) {
            System.err.println("Error saving server state: " + e.getMessage());
        }
    }

    public ConcurrentHashMap<Integer, String> loadServerState() {
        if (!storagePath.toFile().exists()) {
            return new ConcurrentHashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(storagePath.toFile()))) {
            Object obj = ois.readObject();
            if (obj instanceof ConcurrentHashMap<?, ?> loadedMap) {
                ConcurrentHashMap<Integer, String> resultMap = new ConcurrentHashMap<>();
                for (Map.Entry<?, ?> entry : loadedMap.entrySet()) {
                    if (entry.getKey() instanceof Integer && entry.getValue() instanceof String) {
                        resultMap.put((Integer) entry.getKey(), (String) entry.getValue());
                    }
                }
                return resultMap;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading server state: " + e.getMessage());
        }

        return new ConcurrentHashMap<>();
    }

}
