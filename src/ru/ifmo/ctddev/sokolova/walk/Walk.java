package ru.ifmo.ctddev.sokolova.walk;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
//to run tests: java -classpath ./src:./lib/hamcrest-core-1.3.jar:./lib/junit-4.11.jar:./lib/WalkTest.jar info.kgeorgiy.java.advanced.walk.MyTester Walk Walk

/**
 * Created by maria on 08.02.17.
 */
public class Walk {

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Incorrect number of args" + "\n");
            return;
        }
        try (BufferedReader in = new BufferedReader(new FileReader(args[0]))) {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(args[1]))) {
                while (true) {
                    String str = null;
                    try {
                        str = in.readLine();
                    }
                    catch (IOException e) {
                        System.out.println("Some error occured while reading input file: " + e.getMessage());
                    }
                    if (str == null) break;
                    Path path = Paths.get(str);

                    try (FileInputStream fis = new FileInputStream(path.toAbsolutePath().toString())) {
                        out.write(hashToString(FNVHash(fis)) + " " + path.toString() + "\n");
                    } catch (IOException e) {
                        System.out.println("Attempt to read file failed: " + e.getMessage());
                        out.write("00000000 " + path.toString() + "\n");
                    }
                }
            }
            catch (IOException e) {
                System.out.println("Error caused with output file: " + e.getMessage());
            }
        }
        catch (IOException e) {
            System.out.println("Error caused with input file: " + e.getMessage());
        }
    }

    private static int FNVHash (InputStream in) throws IOException {
        int h = 0x811c9dc5;
        int b;
        while ((b = in.read()) != -1) {
            h = (h * 0x01000193) ^ (b & 0xff);
        }
        return h;
    }

    private static String hashToString(int i) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(i).array();

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
