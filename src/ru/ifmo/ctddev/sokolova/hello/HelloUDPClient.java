package ru.ifmo.ctddev.sokolova.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by maria on 16.04.17.
 */
public class HelloUDPClient implements HelloClient {

    private static final int TIMEOUT = 50;
    public static final Charset CHARSET = Charset.forName("UTF-8");
    private ArrayList<Thread> workingThreads = new ArrayList<>();


    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String prefix = args[2];
        int requests = Integer.parseInt(args[3]);
        int threads = Integer.parseInt(args[4]);
        new HelloUDPClient().run(host, port, prefix, requests, threads);
    }

    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {
        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + host);
            return;
        }
        try {
            for (int i = 0; i < threads; i++) {
                final int threadIndex = i;
                workingThreads.add(new Thread(() -> {
                    try (DatagramSocket clientSocket = new DatagramSocket()) {
                        clientSocket.setSoTimeout(TIMEOUT);

                        for (int j = 0; j < requests; j++) {
                            String request = prefix + threadIndex + "_" + j;
                            DatagramPacket requestPacket = new DatagramPacket(request.getBytes(), request.getBytes().length, address, port);
                            clientSocket.send(requestPacket);

                            byte buffer[] = new byte[clientSocket.getReceiveBufferSize()];
                            DatagramPacket replyPacket = new DatagramPacket(buffer, buffer.length);
                            while (!Thread.interrupted()) {
                                try {
                                    clientSocket.receive(replyPacket);
                                    String received = new String(replyPacket.getData(), replyPacket.getOffset(), replyPacket.getLength());
                                    if (received.contains(request)) {
                                        System.out.println(request);
                                        System.out.println(received);
                                        break;
                                    }
                                } catch (SocketTimeoutException e) {
                                    try {
                                        clientSocket.send(requestPacket);
                                    } catch (IOException ignored) {}
                                }
                            }
                        }
                    } catch (IOException ignored) {}
                }));
                workingThreads.get(threadIndex).start();
            }
            for (Thread thread : workingThreads) {
                thread.join();
            }
        } catch (InterruptedException e) {
                for (Thread thread : workingThreads) {
                    thread.interrupt();
                }
         }
    }
}
