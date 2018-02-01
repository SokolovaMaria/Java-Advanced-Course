package ru.ifmo.ctddev.sokolova.crawler;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by maria on 02.04.17.
 */
public class WebCrawler implements Crawler {

    private Downloader downloader;
    private int perHost;

    private ExecutorService downloadThreadPool;
    private ExecutorService extractThreadPool;

    private Set<String> alreadyDownloaded;

    private Map<String, DownloadGuard> activeHosts;
    private Map<String, IOException> exceptions;

    /**
     * {@link WebCrawler} constructor
     *
     * @param downloader allows to download pages and extract links from them
     * @param downloaders - the maximum number of pages, being downloaded simultaneously
     * @param extractors - the maximum number of pages, from which links can be extracted simultaneously
     * @param perHost - the maximum number of pages, being downloaded from one host
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        downloadThreadPool = Executors.newFixedThreadPool(downloaders);
        extractThreadPool = Executors.newFixedThreadPool(extractors);

        alreadyDownloaded = Collections.newSetFromMap(new ConcurrentHashMap<>());
        activeHosts = new ConcurrentHashMap<>();
        exceptions = new ConcurrentHashMap<>();
    }

    /**
     * Downloads pages, starting with the page at this url, recursively descending to the given depth
     *
     * @param url - url of currently downloadable page
     * @param depth - current depth of downloading
     * @return - {@link Result}
     */
    @Override
    public Result download(String url, int depth) {
        Phaser phaser = new Phaser(1);
        DownloadThread downloadThread = new DownloadThread(phaser, 1, depth, url);

        try {
            phaser.register();

            String host = URLUtils.getHost(url);
            activeHosts.putIfAbsent(host, new DownloadGuard(downloadThreadPool, perHost));
            activeHosts.get(host).guardedDownloadRun(downloadThread);

        } catch (IOException e) {
            exceptions.put(url, e);
            phaser.arrive();
        }

        phaser.arriveAndAwaitAdvance();
        List<String> downloadedPages = alreadyDownloaded.stream().filter(page -> !exceptions.containsKey(page)).collect(Collectors.toList());
        return new Result(downloadedPages, exceptions);
    }

    private class DownloadThread implements Runnable {
        private Phaser phaser;
        private int currentDepth;
        private int maxDepth;
        private String url;

        private DownloadThread(Phaser phaser, int currentDepth, int maxDepth, String url) {
            this.phaser = phaser;
            this.currentDepth = currentDepth;
            this.maxDepth = maxDepth;
            this.url = url;
        }

        @Override
        public void run() {
            try {
                if (!alreadyDownloaded.add(url)) {
                    return;
                }
                Document document = downloader.download(url);
                if (this.currentDepth == maxDepth) {
                    return;
                }
                ExtractThread extractThread = new ExtractThread(document, url, currentDepth, maxDepth, phaser);
                phaser.register();
                extractThreadPool.submit(extractThread);
            } catch (IOException e) {
                exceptions.put(url, e);
            } finally {
                phaser.arrive();
            }
        }
    }


    private class DownloadGuard {

        private ExecutorService service;
        private Semaphore semaphore;

        private DownloadGuard(ExecutorService service, int max) {
            this.service = service;
            this.semaphore = new Semaphore(max);
        }

        private void guardedDownloadRun(Runnable downloadTask) {
            Runnable guardedTask =() -> {
                try {
                    semaphore.acquire();
                    downloadTask.run();
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                } finally {
                    semaphore.release();
                }
            };
            service.submit(guardedTask);
        }
    }

    private class ExtractThread implements Runnable {
        private Document document;
        private String url;
        private int currentDepth;
        private int maxDepth;
        private Phaser phaser;

        private ExtractThread(Document document, String url, int currentDepth, int maxDepth, Phaser phaser) {
            this.document = document;
            this.url = url;
            this.currentDepth = currentDepth;
            this.maxDepth = maxDepth;
            this.phaser = phaser;
        }

        @Override
        public void run() {
            try {
                List<String> links = document.extractLinks();

                for (String link : links) {
                    DownloadThread downloadThread = new DownloadThread(phaser, currentDepth + 1, maxDepth, link);

                    phaser.register();
                    String host = URLUtils.getHost(url);
                    activeHosts.putIfAbsent(host, new DownloadGuard(downloadThreadPool, perHost));
                    activeHosts.get(host).guardedDownloadRun(downloadThread);
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                phaser.arrive();
            }
        }
    }
    public void close() {
        downloadThreadPool.shutdownNow();
        extractThreadPool.shutdownNow();
    }
}
