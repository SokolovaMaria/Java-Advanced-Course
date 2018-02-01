package ru.ifmo.ctddev.sokolova.walk;

import java.io.*;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by maria on 30.04.17.
 */
public class Cheat_primality {

    public static  BufferedReader in;
    public static StringTokenizer tokenizer;


    public static String getNext() throws IOException {
        while(tokenizer == null || !tokenizer.hasMoreElements()) {
            tokenizer = new StringTokenizer(in.readLine());
        }
        return tokenizer.nextToken();
    }

    public static int nextInt() throws IOException {
        return Integer.parseInt(getNext());
    }

    public static BigInteger nextBigInteger() throws IOException {
        return BigInteger.valueOf(Long.parseLong(getNext()));
    }

    public static void main(String[] args) throws IOException {
        in = new BufferedReader(new InputStreamReader(System.in));
        int n = nextInt();
        for (int i = 0; i < n; i++) {
            if (nextBigInteger().isProbablePrime(5)) {
            System.out.println("YES");
            } else {
                System.out.println("NO");
            }
        }
        in.close();
    }

}



