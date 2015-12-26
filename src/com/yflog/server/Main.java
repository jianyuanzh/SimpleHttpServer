package com.yflog.server;

import java.io.IOException;

/**
 * Created by vincent on 15-12-26.
 */
public class Main {
    public static void main(String[] args) {
        String path = "simpleWeb";
        SimpleHttpServer.setBasePath(path);
        SimpleHttpServer.setPort(8099);
        try {
            SimpleHttpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
