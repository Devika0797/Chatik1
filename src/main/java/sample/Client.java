package sample;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.util.ArrayList;
import java.util.List;

public class Client extends JFrame{
    private static String connectMessage;
    private static boolean authorized;

    private static Socket socket;

    private JTextArea chatArea;
    private JTextField inputField;

    private static DataInputStream inputStream;
    private static DataOutputStream outputStream;

    private File file;

    public static boolean isAuthorized () {
        return authorized;
    }

    public Client() {
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initGUI();
    }

    private void openConnection() throws IOException {

        socket = new Socket(ChatConstants.HOST, ChatConstants.PORT);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        long startTime = System.currentTimeMillis();

        // отклбчение через 120 сек
        new Thread(() -> {
            while(true) {
                if (isAuthorized())
                    break;
                if (System.currentTimeMillis() - startTime > 120000 && !isAuthorized()) {
                    chatArea.append("Authorization is not complete. Connection is closed");
                    closeConnection();
                    break;
                }
            }
        }).start();

        new Thread(() -> {
            try {
                //auth
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    if (strFromServer.equals(ChatConstants.AUTH_OK)) {
                        String[] parts = strFromServer.split("\\s+");
                        String nick = parts[1];
                        createFile(nick); // по нику
                        uploadHistory(nick);
                        break;
                    }
                    chatArea.append(strFromServer);
                    chatArea.append("\n");


                }
                //read
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    if (file != null) {
                        createHistory(file, strFromServer);
                    }
                    if (strFromServer.equals(ChatConstants.STOP_WORD)) {
                        break;
                    } else if (strFromServer.startsWith(ChatConstants.CLIENTS_LIST)) {
                        chatArea.append("Сейчас онлайн " + strFromServer);
                    } else {
                        chatArea.append(strFromServer);
                    }
                    chatArea.append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void createFile(String nick) {
        file = new File(nick + "_chatHistory.txt");
    }


    public  void createHistory (File file, String str) {
        try(DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file, true))) {
            dataOutputStream.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadHistory (String nick) throws IOException {
        List<String> lastMessage = new ArrayList<>();
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(nick + "_chatHistory.txt"))) {
            while (dataInputStream.available() != 0) {
                lastMessage.add(dataInputStream.readUTF());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (lastMessage.size() > ChatConstants.CHAT_HISTORY) {
            for (int i = lastMessage.size() - ChatConstants.CHAT_HISTORY; i < lastMessage.size(); i++) {
                chatArea.append(lastMessage.get(i) + "\n");
            }
        } else {
            for (int i = 0; i < lastMessage.size(); i++) {
                chatArea.append(lastMessage.get(i) + "\n");
            }
        }
    }


    private void closeConnection() {
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
    }

    private void initGUI() {
        setBounds(600, 300, 500, 500);
        setTitle("Клиент");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Message area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        //down pannel
        JPanel panel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        // inputField.setBounds(100, 100, 150, 30);
        panel.add(inputField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        panel.add(sendButton, BorderLayout.EAST);

        add(panel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        //close window
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
//                super.windowClosing(e);
                try {
                    outputStream.writeUTF(ChatConstants.STOP_WORD);
                    closeConnection();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
    }

    private void sendMessage() {
        if (!inputField.getText().trim().isEmpty()) {
            try {
                outputStream.writeUTF(inputField.getText());
                inputField.setText("");
                inputField.grabFocus();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Send error occured");
            }
        }
    }


    public static void main(String[] args) {

        SwingUtilities.invokeLater(Client::new);
    }



}
