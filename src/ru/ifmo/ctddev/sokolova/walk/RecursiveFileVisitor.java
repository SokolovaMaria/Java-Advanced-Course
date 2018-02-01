package ru.ifmo.ctddev.sokolova.walk;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by maria on 10.02.17.
 */

public class RecursiveFileVisitor extends SimpleFileVisitor<Path> {
    final private BufferedWriter out;

    public RecursiveFileVisitor (BufferedWriter out) {
        this.out = out;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        try (FileInputStream fis = new FileInputStream(path.toAbsolutePath().toString())) {
            out.write(hashToString(FNVHash(fis)) + " " + path.toString() + "\n");
        }
        catch (IOException e) {
            visitFileFailed(path, e);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        out.write("00000000 " + path.toString() + "\n");
        if (Files.notExists(path)) {
            System.out.println("File doesn't exist: " + path.toString());
        } else {
            System.out.println("File visit failed: " + path.toString());
        }
        return FileVisitResult.CONTINUE;
    }

    private static int FNVHash (InputStream in) throws IOException {
        int h = 0x811c9dc5;
        byte[] bytes = new byte[1024];
        int len;
        while ((len = in.read(bytes)) != -1) {
            for (int i = 0; i < len; i++) {
                h = (h * 0x01000193) ^ (bytes[i] & 0xff);
            }
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
