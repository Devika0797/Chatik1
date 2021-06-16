package sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * обслуживает клиента,  отвечает за связь между клиентом и сервером
 */
public class ClientHandler {

    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    private Server server;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    // дз 4.2 На серверной стороне сетевого чата реализовать управление потоками через ExecutorService.
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.name = "";

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        LOGGER.info("Authentification...");
                        authentification(); //авторизация как в чатике
                        readMessages(); //чтение сообзений
                        //closeConnection();
                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            LOGGER.info("Close connection");
                            closeConnection();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (IOException e) {
            LOGGER.warn("Проблема при создании клинета.");
        }
    }

    private void closeConnection() throws IOException {
        server.unsubscribe(this);
        server.broadcastMessage(name + " вышел из чата");
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }


    private void authentification() throws IOException, SQLException{
        // auth login pass
        while (true){
            String message = inputStream.readUTF();
            if (message.startsWith(ChatConstants.AUTH)) {
                String[] parts = message.split("\\s+"); // разбиваеь строчку по пробелам
                String nick = server.getAuthService().getNickByLoginAndPass(parts[1], parts[2]);
                if (nick != null) {
                    if (!server.isNickBusy(nick)){ //проверим, что такого нет
                        sendMsg(ChatConstants.AUTH_OK + " " + nick);
                        name = nick;
                        server.subscribe(this);
                        server.broadcastMessage(name + " вошел в чат");
                        return;
                    } else {
                        sendMsg("Логин уже испульзуется");
                    }
                } else {
                    sendMsg("Неверные логин/пароль");

                }
            }
        }
    }

    public void sendMsg(String message) {
        try {
            outputStream.writeUTF(message);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void readMessages() throws IOException {
        while (true) {
            String messageFromClient = inputStream.readUTF();
            LOGGER.trace("от " + name + ": " + messageFromClient);

            if (messageFromClient.startsWith(ChatConstants.STOP_WORD)) {
                return;
            } else if (messageFromClient.startsWith(ChatConstants.PRIVATE_MESSAGE)) {
                server.sendPrivateMessage(messageFromClient, name);
            } else if (messageFromClient.startsWith(ChatConstants.SEND_TO_LIST)) {
                String[] splittedStr = messageFromClient.split("\\s+");
                List<String> nicknames = new ArrayList<>();
                for (int i = 1; i < splittedStr.length - 1; i++) {
                    nicknames.add(splittedStr[i]);
                }
            } else if (messageFromClient.startsWith(ChatConstants.CLIENTS_LIST)) {
                server.broadcastClients();
            } else if (messageFromClient.startsWith(ChatConstants.CHANGE_NICK)) {
                String[] splittedStr = messageFromClient.split("\\s+");
                if(!server.isNickBusy(splittedStr[1])){
                    String oldName = name;
                    name = splittedStr[1];
                    server.broadcastMessage(oldName + " сменил ник на " + name);
                } else {
                    sendMsg("Ник уже занят");
                }
            } else
                server.broadcastMessage(name + ": " + messageFromClient);// всем клиентам;
        }
    }

}
