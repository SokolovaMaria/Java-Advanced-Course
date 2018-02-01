package ru.ifmo.ctddev.sokolova.hello;

/**
 * Created by maria on 16.04.17.
 */
import info.kgeorgiy.java.advanced.hello.HelloServer;
import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {

    private ExecutorService requestProcessor;
    private DatagramSocket serverSocket;

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
        HelloUDPServer server = new HelloUDPServer();
        server.start(port, threads);
        //server.close();
    }

    @Override
    public void start(int port, int threads) {
        try {
            serverSocket = new DatagramSocket(port);
            final int buffer_size = serverSocket.getReceiveBufferSize();
            requestProcessor = Executors.newFixedThreadPool(threads);
            requestProcessor.submit(new ReceiveRequests(serverSocket, buffer_size));
        } catch (IOException e) {
            System.out.println("Unable to bind to port: " + port);
        }
    }

    private class ReceiveRequests implements Runnable {

        private DatagramSocket serverSocket;
        int buffer_size;

        ReceiveRequests(DatagramSocket  serverSocket, int buffer_size) {
            this.serverSocket = serverSocket;
            this.buffer_size = buffer_size;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[buffer_size];
            DatagramPacket requestPacket = new DatagramPacket(buffer, 0, buffer_size);
            while (!Thread.interrupted()) {
                try {
                    serverSocket.receive(requestPacket);
                    String request = new String (requestPacket.getData(), 0, requestPacket.getLength());

                    String reply  = "Hello, " + request;
                    DatagramPacket replyPacket = new DatagramPacket(reply.getBytes(), 0, reply.length(),
                            requestPacket.getAddress(), requestPacket.getPort());
                    serverSocket.send(replyPacket);

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    @Override
    public void close() {
        requestProcessor.shutdownNow();
        serverSocket.close();
    }
}