import java.io.*;
import java.net.*;

public class Client {
    private static final String SERVER_ADDRESS = "localhost"; // เปลี่ยนเป็นที่อยู่ IP ของเซิร์ฟเวอร์
    private static final int SERVER_PORT = 8080;
    private static final String DOWNLOAD_DIRECTORY = "/home/ice/os project/clientFile/";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            // รับรายชื่อไฟล์จากเซิร์ฟเวอร์
            displayFileList(in);

            String transferMethod = getUserInput(reader, "Enter the method of file transfer (copy/zero): ");
            String requestedFile = getUserInput(reader, "Enter the name of the file you want to download: ");
            
            out.writeUTF(transferMethod);
            out.writeUTF(requestedFile);

            long fileSize = in.readLong();
            if (fileSize > 0) {
                downloadFile(in, requestedFile, fileSize);
            } else {
                System.out.println("File not found on server.");
            }
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private static void displayFileList(DataInputStream in) throws IOException {
        int fileCount = in.readInt();
        System.out.println("Files available on server:");
        for (int i = 0; i < fileCount; i++) {
            String fileName = in.readUTF();
            System.out.println(fileName);
        }
    }

    private static String getUserInput(BufferedReader reader, String prompt) throws IOException {
        System.out.print(prompt);
        return reader.readLine();
    }

    private static void downloadFile(DataInputStream in, String requestedFile, long fileSize) {
        File outputFile = new File(DOWNLOAD_DIRECTORY + requestedFile);
        long startTime = System.currentTimeMillis();
        
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[4096];
            long remaining = fileSize;
            int read;
            while (remaining > 0 && (read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                fos.write(buffer, 0, read);
                remaining -= read;
            }
            System.out.println("File downloaded successfully!");
        } catch (IOException e) {
            System.err.println("Error during file download: " + e.getMessage());
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken for download: " + (endTime - startTime) + " ms");
    }
}