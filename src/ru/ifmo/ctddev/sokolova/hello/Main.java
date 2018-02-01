package ru.ifmo.ctddev.sokolova.hello;

/**
 * Created by maria on 17.04.17.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        HelloUDPServer server = new HelloUDPServer();
        server.start(2539, 2);
        Thread.sleep(100);
        HelloUDPClient client = new HelloUDPClient();
        client.run("192.168.0.103", 4444, "cucumber", 5, 2);
    }
}
