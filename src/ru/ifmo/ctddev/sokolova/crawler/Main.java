package ru.ifmo.ctddev.sokolova.crawler;

import info.kgeorgiy.java.advanced.crawler.*;
import ru.ifmo.ctddev.sokolova.walk.RecursiveFileVisitor;

import java.io.IOException;
public class Main {
    public static void main(String srgs[]) {
        try {
            Crawler crawler = new WebCrawler(new CachingDownloader(), 10, 10, 10);
            Thread t1 = new Thread(()-> {
                Result links = crawler.download("http://neerc.ifmo.ru/~os/", 3);
                System.out.println("From Thread1: " + links.getDownloaded().size());
                for (int i = 0; i < links.getDownloaded().size(); i++) {
                    System.out.println("1: " + links.getDownloaded().get(i));
                }
            });
            Thread t2 = new Thread(()-> {
                Result links = crawler.download("http://neerc.ifmo.ru/~os/", 2);
                System.out.println("From Thread2: " + links.getDownloaded().size());
                for (int i = 0; i < links.getDownloaded().size(); i++) {
                    System.out.println("2: " + links.getDownloaded().get(i));
                }
            });
            t1.start();
            t2.start();

            try {
                t1.join();
                t2.join();
            } catch (InterruptedException e){}
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }


}
