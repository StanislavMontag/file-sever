package org.example.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServerTest {

    private Server server;

    @BeforeEach
    public void setup() {
        ServerStateStorage mockStorage = Mockito.mock(ServerStateStorage.class);
        when(mockStorage.loadServerState()).thenReturn(new ConcurrentHashMap<>());
        server = new Server(mockStorage);
    }

    @Test
    public void testRegisterFile() {
        assertFalse(server.containsFileId(1));
        assertFalse(server.containsFileName("test.txt"));

        server.registerFile(1, "test.txt");

        assertTrue(server.containsFileId(1));
        assertTrue(server.containsFileName("test.txt"));
    }

    @Test
    public void testContainsFileId() {
        server.registerFile(1, "test.txt");
        assertTrue(server.containsFileId(1));
        assertFalse(server.containsFileId(2));
    }

    @Test
    public void testGetFileNameById() {
        server.registerFile(1, "test.txt");
        assertEquals("test.txt", server.getFileNameById(1));
        assertNull(server.getFileNameById(2));
    }
}
