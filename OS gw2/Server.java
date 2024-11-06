import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Server {
    private static final int PORT = 8080;
    private static final String DIRECTORY = "/home/ice/os project/serverFile/";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            // ส่งรายชื่อไฟล์ที่มีอยู่ใน DIRECTORY
            sendFileList(out);

            String transferMethod = in.readUTF();
            String requestedFile = in.readUTF();
            File fileToSend = new File(DIRECTORY, requestedFile);

            if (fileToSend.exists() && fileToSend.isFile()) {
                out.writeLong(fileToSend.length());
                long startTime = System.currentTimeMillis();

                if ("copy".equalsIgnoreCase(transferMethod)) {
                    copyFile(out, fileToSend);
                } else {
                    zeroCopyFile(out, fileToSend);
                }

                long endTime = System.currentTimeMillis();
                System.out.println("Time taken for file transfer: " + (endTime - startTime) + " ms");
            } else {
                out.writeLong(0);
                System.out.println("Requested file not found: " + fileToSend.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error in client communication: " + e.getMessage());
        }
    }

    private static void sendFileList(DataOutputStream out) throws IOException {
        File dir = new File(DIRECTORY);
        File[] files = dir.listFiles();
        if (files != null) {
            out.writeInt(files.length);
            for (File file : files) {
                out.writeUTF(file.getName());
            }
        } else {
            out.writeInt(0);
        }
    }

    private static void copyFile(DataOutputStream out, File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("File copied successfully!");
    }

    private static void zeroCopyFile(DataOutputStream out, File file) throws IOException {
        try (FileChannel fileChannel = new FileInputStream(file).getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            while (fileChannel.read(buffer) != -1) {
                buffer.flip();
                out.write(buffer.array(), 0, buffer.limit());
                buffer.clear();
            }
        }
        System.out.println("File sent using zero copy!");
    }
}