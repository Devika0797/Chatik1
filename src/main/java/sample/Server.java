package sample;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * само приложение
 */
public class Server {

    private List <ClientHandler> clients;
    private AuthService authService;


    public Server() {
        try(ServerSocket server = new ServerSocket(ChatConstants.PORT)) {
            authService = new DatabaseAuthService() {
                @Override
                public String getNickByLoginAndPass(String login, String pass) {
                    return null;
                }
            };

            authService.start();
            clients = new ArrayList<>();
            while (true) {
                System.out.println("Сервер ожидает подключения");
                Socket socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }
    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }
    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }
    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    // отправка сообщений

    public void broadcastMessage(String message) throws IOException {
        clients.forEach(client -> client.sendMsg(message));
    }

    public synchronized boolean sendPrivateMessage (String message, String from) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getName().equals(clientHandler)) {
                clientHandler.sendMsg(message);
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastClients() {
        String clientsMessage = ChatConstants.CLIENTS_LIST + " " +  " " +
                clients.stream()
                        .map(c -> c.getName())
                        .collect(Collectors.joining(" "));
        clients.forEach(c -> c.sendMsg(clientsMessage));
    }


}
