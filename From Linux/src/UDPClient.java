import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;
import java.util.StringTokenizer;

public class UDPClient {

    public static boolean addressIP(String ip) {
        boolean ipadress = false;

        try {
            InetAddress IpAdd = InetAddress.getByName("localhost");
            if(IpAdd.getHostAddress().equals(ip)) {
                ipadress = true;
            }
        } catch (UnknownHostException e) {
            ipadress = false;
        }
        return ipadress;
    }

    public static void main(String[] args) throws UnknownHostException, SocketException {
        InetAddress ipAddress = null;
        boolean answergame = false;

        if(addressIP(args[0])) {
            ipAddress = InetAddress.getByName(args[0]);
        }
        else{
            System.out.println("IP address is wrong");
            System.exit(1);
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException exception){
            System.out.println("The port number must be an integer!");
            System.exit(1);
        }

        System.out.println("Hello! This application is a chat of two people");
        System.out.print("1. Set user name: '@name Kenan'\n" + "2. Start a chatting with default name: '@hello'\n" + "3. Play game: '@game'\n"+ "4. Exit: '@quit'\nSelect action: ");
        Scanner console = new Scanner(System.in);
        String clientName = "";
        String choose = console.nextLine();
        int flag = 0;

        switch (choose){
            case "@hello":
                clientName = "User_1";
                flag = 0;
                break;
            case "@quit":
                System.out.println("Thanks for contacting!");
                System.exit(1);
                break;
            case "@game":
                System.out.println("I have a game for you! " + "You guess the number and i have to guess, let's play? - yes/no");
                Scanner game = new Scanner(System.in);
                String answer= game.nextLine();
                while(true) {
                    if (answer.equals("yes")) {
                        System.out.print("Let's go! ");
                        answergame = true;
                        clientName = "@User_game";
                        break;
                    }
                    else if (answer.equals("no")) {
                        System.out.println("Ok, see you next time! ");
                        break;
                    }
                    else {
                        System.out.println("Please white answer: yes/no");
                        answer= game.nextLine();
                    }
                }
                break;
            default:
                StringTokenizer stringTokenizer = new StringTokenizer(choose);
                if (Objects.equals(stringTokenizer.nextToken(), "@name")) {
                    clientName = stringTokenizer.nextToken();
                } else {
                    System.out.println("Incorrect input!");
                    System.exit(1);
                }
                break;
        }
        DatagramSocket clientSocket = new DatagramSocket();

        if(answergame){
            NumberReadMesssage gameread = new NumberReadMesssage(clientSocket);
            GuessNumberGame gamewrite = new GuessNumberGame(clientSocket, clientName, ipAddress, port);
            gameread.start();
            gamewrite.start();
        }
        else if(flag == 0){
            ReadMesssage readmes = new ReadMesssage(clientSocket);
            WriteMesssage writemes = new WriteMesssage(clientSocket, clientName, ipAddress, port, 0);
            readmes.start();
            writemes.start();
        }
        else {
            ReadMesssage readmes = new ReadMesssage(clientSocket);
            WriteMesssage writemes = new WriteMesssage(clientSocket, clientName, ipAddress, port, 1);
            readmes.start();
            writemes.start();
        }
    }
}

class GuessNumberGame extends Thread{
    private InetAddress ipAddress;
    private DatagramSocket clientSocket;
    private byte[] sendData;
    private String clientName;
    private int port;
    public GuessNumberGame(DatagramSocket clientSocket, String clientName, InetAddress ipAddress, int port){
        this.clientSocket = clientSocket;
        this.sendData = new byte[1024];
        this.clientName = clientName;
        this.ipAddress = ipAddress;
        this.port = port;
    }
    public void run() {
        System.out.println("Write any number from 0 - 100: ");
        while(true) {
            try {
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                String sentence;
                sentence = clientName + ": " + inFromUser.readLine();
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
                clientSocket.send(sendPacket);
                if (sentence.equals(clientName + ": " + "@quit")) {
                    System.out.println("Thanks for game, good bye!");
                    System.exit(1);
                    this.clientSocket.close();
                }
            } catch (Exception e) {
                System.out.println("Error");
            }
        }
    }
}

class NumberReadMesssage extends Thread {
    private DatagramSocket clientSocket;
    private byte[] receiveData;
    public NumberReadMesssage(DatagramSocket clientSocket) {
        this.clientSocket = clientSocket;
        this.receiveData = new byte[1024];
    }
    public void run() {
        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(this.receiveData, this.receiveData.length);
                this.clientSocket.receive(receivePacket);
                String modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if(modifiedSentence.contains("Отлично! Ваше число"))
                {
                    System.out.println(modifiedSentence);
                    System.out.println("Your partner has left the chat, to leave also write: @quit");
                }
                else {
                    System.out.println(modifiedSentence);
                }
            } catch (Exception e) {
                System.out.println("Error");
            }
        }
    }
}



class WriteMesssage extends Thread {
    private InetAddress ipAddress;
    private DatagramSocket clientSocket;
    private byte[] sendData;
    private String clientName;
    private int port;
    private int flag;
    public WriteMesssage(DatagramSocket clientSocket, String clientName, InetAddress ipAddress, int port, int flag) {
        this.clientSocket = clientSocket;
        this.sendData = new byte[1024];
        this.clientName = clientName;
        this.ipAddress = ipAddress;
        this.port = port;
        this.flag = flag;
    }
    public void run(){
        while(true) {
            try {
                if(flag == 0)
                {
                    String sentence;
                    sentence = clientName + ": Hello";
                    sendData = sentence.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
                    clientSocket.send(sendPacket);
                    flag = 1;
                }
                BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
                String sentence;
                sentence = clientName + ": " + inFromUser.readLine();
//                ipAddress = InetAddress.getByName("localhost");
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
                clientSocket.send(sendPacket);
                if (sentence.equals(clientName + ": " + "@quit")) {
                    System.out.println("Thanks for chatting, good bye!");
                    System.exit(1);
                    this.clientSocket.close();
                }
            } catch (Exception e) {
                System.out.println("Error" + e);
            }
        }
    }
}

class ReadMesssage extends Thread {
    private DatagramSocket clientSocket;
    private byte[] receiveData;
    public ReadMesssage(DatagramSocket clientSocket) {
        this.clientSocket = clientSocket;
        this.receiveData = new byte[1024];
    }
    public void run() {
        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(this.receiveData, this.receiveData.length);
                this.clientSocket.receive(receivePacket);
                String modifiedSentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if(modifiedSentence.contains("@quit"))
                {
                    System.out.println("Your partner has left the chat, to leave also write: @quit");
                }
                else {
                    System.out.println(modifiedSentence);
                }
            } catch (Exception e) {
                System.out.println("Error");
            }
        }
    }
}