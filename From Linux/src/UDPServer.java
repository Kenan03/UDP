import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;
import java.util.StringTokenizer;

public class UDPServer {
    public static void main(String[] args) throws Exception {

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException exception) {
            System.out.println("The port number must be an integer!");
            System.exit(1);
        }

        System.out.println("Hello! This application is a chat of two people");
        System.out.print("1. Set user name: '@name Kenan'\n" + "2. Start a chatting with default name: '@hello'\n" + "3. Exit: '@quit'\nSelect action: ");
        Scanner console = new Scanner(System.in);
        String clientName = "";
        String choose = console.nextLine();

        switch (choose){
            case "@hello":
                clientName = "User_2";
                break;
            case "@quit":
                System.out.println("Thanks for contacting!");
                System.exit(1);
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

        DatagramSocket serverSocket = new DatagramSocket(port);
        ReadMessage readmes = new ReadMessage(serverSocket, clientName);
        readmes.start();
    }
}

class GameGuessNumber{
    private DatagramSocket serverSocket;
    private InetAddress IPAddress;
    private int port;
    private byte[] sendData;
    private String clientName;
    public GameGuessNumber(DatagramSocket serverSocket, InetAddress ipAddress, int port, String clientName){
        this.serverSocket = serverSocket;
        this.sendData = new byte[1024];
        this.IPAddress = ipAddress;
        this.port = port;
        this.clientName = clientName;
    }
    public void number(int midrange, String toGuess) {
        try{
            String capitalizedSentence;
            if(toGuess == "@quit") {
                this.serverSocket.close();
                System.exit(1);
            }
            if(toGuess != "") {
                capitalizedSentence = clientName + ": " + toGuess;
                System.out.println("Thanks for game, good bye!");
                sendData = capitalizedSentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(this.sendData, this.sendData.length, this.IPAddress, this.port);
                this.serverSocket.send(sendPacket);
                this.serverSocket.close();
                System.exit(1);
            }
            else {
                capitalizedSentence = clientName + ": " + "it's number '>', '<' или '=' " + midrange + "? " + "Enter '+' if more, '-' if less и '=' if equals: ";
            }
            sendData = capitalizedSentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(this.sendData, this.sendData.length, this.IPAddress, this.port);
            this.serverSocket.send(sendPacket);

        }
        catch (Exception e) {
            System.out.println("Error" + e);
        }

    }
}

class WriterMessageServer extends Thread{
    private DatagramSocket serverSocket;
    private InetAddress IPAddress;
    private int port;
    private byte[] sendData;
    private String clientName;
    public WriterMessageServer(DatagramSocket serverSocket, InetAddress ipAddress, int port, String clientName) {
        this.serverSocket = serverSocket;
        this.sendData = new byte[1024];
        this.IPAddress = ipAddress;
        this.port = port;
        this.clientName = clientName;
    }
    public void run() {
        while(true){
            try {
                BufferedReader inFromServerUser = new BufferedReader(new InputStreamReader(System.in));
                String capitalizedSentence;
                capitalizedSentence = clientName + ": " + inFromServerUser.readLine();
                sendData = capitalizedSentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(this.sendData, this.sendData.length, this.IPAddress, this.port);
                this.serverSocket.send(sendPacket);
                if(capitalizedSentence.equals(clientName + ": " + "@quit"))
                {
                    System.out.println("Thanks for chatting, good bye!");
                    System.exit(1);
                    this.serverSocket.close();
                }
            } catch (Exception e) {
                System.out.println("Error" + e);
            }
        }
    }
}


class ReadMessage extends Thread{
    private DatagramSocket serverSocket;
    private byte[] receiveData;
    private String clientName1;
    public ReadMessage(DatagramSocket serverSocket, String clientName1){
        this.serverSocket = serverSocket;
        this.receiveData = new byte[1024];
        this.clientName1 = clientName1;
    }
    public void run() {
        int flag = 1;
        int min = 0;
        int max = 100;
        int midrange = Math.round((min + max) / 2);
        while (true) {
            try {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                this.serverSocket.receive(receivePacket);
                String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                if(sentence.contains("@User_game")) {
                    System.out.println(sentence);
                    if(sentence.contains("@quit"))
                    {
                        System.out.println("Your partner has left the chat, to leave also write: @quit");
                        GameGuessNumber game = new GameGuessNumber(serverSocket, IPAddress, port, clientName1);
                        game.number(midrange, "@quit");
                    }
                    if (sentence.contains("=")) {
                        GameGuessNumber game = new GameGuessNumber(serverSocket, IPAddress, port, clientName1);
                        game.number(midrange, "Great! Your number is " + midrange + ". Thanks for game ;)");
                    }
                    else if (sentence.contains("+")) {
                        min = midrange;//уменьшаем диапазон:
                        midrange = Math.round((min + max) / 2);//находим новую середину диапазона:
                        if (min == midrange && midrange != 1000) {//если округление сравнило середину с нижней границей, увеличиваем середину на 1:
                            midrange += 1;
                        }
                        GameGuessNumber game = new GameGuessNumber(serverSocket, IPAddress, port, clientName1);
                        game.number(midrange, "");
                    }
                    else if (sentence.contains("-")) {
                        max = midrange;
                        midrange = Math.round((min + max) / 2);
                        GameGuessNumber game = new GameGuessNumber(serverSocket, IPAddress, port, clientName1);
                        game.number(midrange, "");
                    }
                    else{
                        GameGuessNumber game = new GameGuessNumber(serverSocket, IPAddress, port, clientName1);
                        game.number(midrange, "");
                    }
                    flag = 0;
                }
                else if(sentence.contains("@quit"))
                {
                    System.out.println("Your partner has left the chat, to leave also write: @quit");
                }
                else if(flag == 1) {
                    WriterMessageServer write = new WriterMessageServer(this.serverSocket, IPAddress, port, clientName1);
                    write.start();
                    flag = 0;
                    System.out.println(sentence);
                }
                else{
                    System.out.println(sentence);
                }

            } catch (Exception e) {
                System.out.println("Error " + e);
            }
        }
    }
}