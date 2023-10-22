package org.example.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ServerStateStorageTest {

    @TempDir
    Path tempDir;
    private ServerStateStorage storage;

    @BeforeEach
    public void setUp() {
        storage = new ServerStateStorage(tempDir.resolve("test.ser"));
    }

    @Test
    public void testSaveAndLoadServerState() {
        ConcurrentHashMap<Integer, String> initialData = new ConcurrentHashMap<>();
        initialData.put(1, "test.txt");
        initialData.put(2, "demo.txt");
        storage.saveServerState(initialData);

        ConcurrentHashMap<Integer, String> loadedData = storage.loadServerState();
        assertEquals(initialData, loadedData);
    }

    @Test
    public void testLoadServerStateWithNoFile() {
        ConcurrentHashMap<Integer, String> loadedData = storage.loadServerState();
        assertTrue(loadedData.isEmpty());
    }
}
