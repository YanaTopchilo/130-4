/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplechat;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
/**
 *
 * @author kosti
 */
public class SimpleChat extends JFrame implements ISimpleChat {

    private JButton sendButton;
    private JButton exitButton;
    private JTextField inputText;
    private JTextArea outText;
    private Socket socket;
    private ServerSocket serverSocket;
    private int choice;

    public SimpleChat() {
        init();
        int choice = choices();
        if (choice == 0) {
            setTitle("Сервер");
            new Thread(() -> {
                try {
                    server();
                } catch (ChatException e) {
                    System.out.println(e.getMessage());
                }
            }).start();
        } else {
            setTitle("Клиент");
            new Thread(() -> {
                try {
                    client();
                } catch (ChatException e) {
                    System.out.println(e.getMessage());
                }
            }).start();
        }
        setVisible(true);
    }

    private void init() {
        setSize(600, 400);
        setLocation(600, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE );
        

        sendButton = new JButton("Отправить");
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (inputText.getText().trim().equals("")) {
                    JOptionPane.showMessageDialog(SimpleChat.this, "Введите текст для отправки");
                    return;
                }
                try {
                    sendMessage(inputText.getText());
                } catch (ChatException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        });
        exitButton = new JButton("Выход");
        exitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitButton.addActionListener(new ActionListener() {
           
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        JLabel lablename = new JLabel("Введите текст:");
        inputText = new JTextField(20);
        outText = new JTextArea();
        outText.setEditable(false);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout());
         northPanel.add(lablename );
         northPanel.add(inputText);
         northPanel.add(sendButton);
         add(northPanel, BorderLayout.NORTH);
         add(outText, BorderLayout.CENTER);
         add(exitButton, BorderLayout.SOUTH);   
    }

    private int choices() {
        Object[] choiceBut = {"Сервер", "Клиент"};
        choice = JOptionPane.showOptionDialog(this,
                new String[] {"Добро пожаловать в SimpleChat", 
                                            "Выберите режим:"},
                null, JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, choiceBut, null);
        return choice;
    }

    private String Adress() {
        String adress = "";
        while (true) {
            adress = JOptionPane.showInputDialog("Введите адрес сервера: ");
            if (adress == null) {
                System.exit(0);
            }
            if (adress.trim().equals("")) {
                JOptionPane.showMessageDialog(this, "Адрес сервера не указан");
                continue;
            }
            break;
        }
        return adress;
    }

    private int Port() {
        String portString = "";
        int portInt = 0;
        while (true) {
            portString = JOptionPane.showInputDialog("Введите порт сервера: ");
            if (portString == null) {
                System.exit(0);
            }
            if (portString.trim().equals("")) {
                JOptionPane.showMessageDialog(this, "Порт сервера не указан");
                continue;
            }
            try {
                portInt = Integer.parseInt(portString);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Неверный номер порта");
                continue;
            }
            break;
        }
        return portInt;
    }

    @Override
    public void client() throws ChatException {
        String adress = Adress();
        int port = Port();
        try {
            socket = new Socket(adress, port);
            System.out.println("Клиент подключен");
            getMessage();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,"Клиент не подключен. Возможно неккоректны адрес или порт сервера");                                    
            client();
        }
    }

    @Override
    public void server() throws ChatException {
        try {
            serverSocket = new ServerSocket(ISimpleChat.SERVER_PORT);
            socket = serverSocket.accept();
            System.out.println("Сервер подключен");
            getMessage();
        } catch (IOException e) {
            throw new ChatException(e.getMessage());
        }
    }

    @Override
    public void getMessage() throws ChatException {
        try (Scanner scanner = new Scanner(socket.getInputStream())) {
            while (scanner.hasNext()) {
               String message = scanner.nextLine();
                outText.append("Входящие сообщение:  " + message + "\n");
            }
        } catch (IOException e) {
            throw new ChatException(e.getMessage());
        }
    }

    @Override
    public void sendMessage(String message) throws ChatException {
        try {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.println(message);
            outText.append("Ваше сообщение: " + message + "\n");
            inputText.setText(null);
        } catch (IOException e) {
            throw new ChatException(e.getMessage());
        }
    }

    @Override
    public void close() throws ChatException {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.getOutputStream().close();
                socket.getInputStream().close();
                socket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            throw new ChatException(e.getMessage());
        }
    }
}    