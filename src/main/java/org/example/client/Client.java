package org.example.client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;


public class Client {

    private static final Path ROOT = Path.of(System.getProperty("user.dir")).resolve("src/client/data");

    private static final String ENTER_ACTION_MSG = "Enter action (1 - get a file, 2 - create a file, 3 - delete a file): ";
    private static final String GET_FILE_MSG = "Do you want to get the file by name or by id (1 - name, 2 - id): ";
    private static final String ENTER_FILENAME_MSG = "Enter filename: ";
    private static final String ENTER_ID_MSG = "Enter id: ";
    private static final String ENTER_FILE_TO_PUT_MSG = "Enter name of the file: ";
    private static final String ENTER_NAME_ON_SERVER_MSG = "Enter name of the file to be saved on server: ";
    private static final String ERROR_READING_FILE_MSG_PREFIX = "Error reading file: ";
    private static final String FILE_SAVED_RESPONSE_MSG_PREFIX = "Response says that file is saved! ID = ";
    private static final String UNABLE_TO_CREATE_FILE_MSG = "Unable to create a file";
    private static final String DELETE_FILE_MSG = "Do you want to delete the file by name or by id (1 - name, 2 - id): ";
    private static final String FILE_DOWNLOADED_MSG = "The file was downloaded! Specify a name for it: ";
    private static final String FILE_NOT_FOUND_MSG = "The response says that this file is not found!";
    private static final String FILE_DELETED_SUCCESSFULLY_MSG = "The response says that this file was deleted successfully!";

    private final SocketClient socketClient = new RealSocketClient();

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.print(ENTER_ACTION_MSG);

        switch (scanner.nextLine()) {
            case "1":
                getFile(scanner);
                break;
            case "2":
                putFile(scanner);
                break;
            case "3":
                deleteFile(scanner);
                break;
            case "exit":
                sendRequestToServer("EXIT", null);
                break;
            default:
                throw new IllegalStateException("Unexpected value:");
        }
    }
    void getFile(Scanner scanner) {
        System.out.print(GET_FILE_MSG);
        int choice = Integer.parseInt(scanner.nextLine());  // Parse the choice as an integer
        ServerResponse response = switch (choice) {
            case 1 -> {
                String fileName = prompt(scanner, ENTER_FILENAME_MSG);
                yield sendRequestToServer("GET BY_NAME", fileName);
            }
            case 2 -> {
                String fileId = prompt(scanner, ENTER_ID_MSG);
                yield sendRequestToServer("GET BY_ID", fileId);
            }
            default -> throw new IllegalStateException("Unexpected value.");
        };
        handleFileDownload(scanner, response);
    }



    void putFile(Scanner scanner) {
        String fileName = prompt(scanner, ENTER_FILE_TO_PUT_MSG);
        File file = ROOT.resolve(fileName).toFile();
        String nameOnServer = prompt(scanner, ENTER_NAME_ON_SERVER_MSG);

        byte[] message;
        try {
            message = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            System.out.println(ERROR_READING_FILE_MSG_PREFIX + e.getMessage());
            return;
        }

        ServerResponse response = sendFileToServer(nameOnServer, message);
        if ("200".equals(response.status)) {
            System.out.println(FILE_SAVED_RESPONSE_MSG_PREFIX + response.message);
        } else if ("403".equals(response.status)) {
            System.out.println(UNABLE_TO_CREATE_FILE_MSG);
        }
    }

    private void deleteFile(Scanner scanner) {
        System.out.print(DELETE_FILE_MSG);
        ServerResponse response = switch (scanner.nextInt()) {
            case 1 -> {
                String fileName = prompt(scanner, ENTER_FILENAME_MSG);
                yield sendRequestToServer("DELETE BY_NAME", fileName);
            }
            case 2 -> {
                String fileId = prompt(scanner, ENTER_ID_MSG);
                yield sendRequestToServer("DELETE BY_ID", fileId);
            }
            default -> throw new IllegalStateException("Unexpected value.");
        };
        handleDeleteResponse(response);
    }

    private void handleFileDownload(Scanner scanner, ServerResponse response) {
        if ("200".equals(response.status)) {
            System.out.print(FILE_DOWNLOADED_MSG);
            String fileName = scanner.nextLine();
            saveFile(ROOT.resolve(fileName), response.data);
        } else if ("404".equals(response.status)) {
            System.out.println(FILE_NOT_FOUND_MSG);
        }
    }

    private void handleDeleteResponse(ServerResponse response) {
        if ("200".equals(response.status)) {
            System.out.println(FILE_DELETED_SUCCESSFULLY_MSG);
        } else if ("404".equals(response.status)) {
            System.out.println(FILE_NOT_FOUND_MSG);
        }
    }

    private String prompt(Scanner scanner, String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    private void saveFile(Path path, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(data);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private ServerResponse sendRequestToServer(String command, String argument) {
        return socketClient.sendRequestToServer(command, argument);
    }

    private ServerResponse sendFileToServer(String nameOnServer, byte[] data) {
        return socketClient.sendRequestToServer("PUT", nameOnServer, data);
    }

}
