package ru.ifmo.ctddev.sokolova.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by maria on 10.02.17.
 */
public class RecursiveWalk {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Incorrect number of arguments" + "\n");
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
                        System.out.println("Error occurred while reading input file: " + e.getMessage());
                    }
                    if (str == null) break;
                    Path path;
                    try {
                      path = Paths.get(str);
                    } catch (InvalidPathException e) {
                        out.write("00000000 " + str + "\n");
                        System.out.println(("File doesn't exist: ").concat(str));
                        return;
                    }
                    Files.walkFileTree(path, new RecursiveFileVisitor(out));
                }
            }
            catch (IOException e) {
                System.out.println("Smth wrong with output file: " + e.getMessage());
            }
        }
        catch (IOException e) {
            System.out.println("Smth wrong with input file: " + e.getMessage());
        }
    }
}
