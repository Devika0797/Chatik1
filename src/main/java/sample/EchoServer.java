package sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
    public static void main(String[] args) {
        Socket socket = null;
        try (ServerSocket serverSocket = new ServerSocket(ChatConstants.PORT)) {
            System.out.println("Server started. Wait for connection...");
            socket = serverSocket.accept();
            System.out.println("Client connected!");
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            while (true) {
                String string = inputStream.readUTF();
                if (string.equals(ChatConstants.STOP_WORD)) {
                    break;
                }
                System.out.println("Message received " + string);
                outputStream.writeUTF("Echo : " + string);
            }
            System.out.println("Server shutting down");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
