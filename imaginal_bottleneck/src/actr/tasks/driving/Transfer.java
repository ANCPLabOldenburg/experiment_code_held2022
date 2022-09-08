package actr.tasks.driving;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

public class Transfer {

    Socket client;
    ServerSocket serverSocket;

    String test(Env env) throws IOException {
        try {
            String nachricht = readMsg(client);
            //System.out.print("empfangene Nachricht: " + nachricht);
            return nachricht;
        } catch (IOException ie) {
            ie.printStackTrace();
            return "error";
        }
    }

    void startup() throws IOException{
        try{
        int port = 2468;
            serverSocket = new ServerSocket(port);
            //client = warteAufAnmeldung(serverSocket);
            System.out.println("connection");
        }catch(IOException ie){
            ie.printStackTrace();
        }
    }

    Socket warteAufAnmeldung(ServerSocket serverSocket) throws IOException {
        Socket socket = serverSocket.accept(); // blockiert, bis sich ein Client angemeldet hat
        return socket;
    }

    String readMsg(Socket socket) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        char[] buffer = new char[200];
        int anzahlZeichen = bufferedReader.read(buffer, 0, 200); // blockiert bis Nachricht empfangen
        String nachricht = new String(buffer, 0, anzahlZeichen);
        return nachricht;
    }

    void sendMsg(Socket socket, String nachricht) throws IOException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        printWriter.print(nachricht);
        printWriter.flush();
    }
}