package com.yflog.server;

import com.yflog.util.SimpleThreadPool;
import com.yflog.util.ThreadPool;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by vincent on 12/22/15.
 */
public class SimpleHttpServer {

    static ThreadPool<HttpRequestHandler> handlerThreadPool = new SimpleThreadPool<HttpRequestHandler>(20);
    static String basePath;
    static ServerSocket serverSocket;
    static int port = 8080;

    public static void setPort(int port) {
        if (port > 0) {
            SimpleHttpServer.port = port;
        }
    }

    public static void setBasePath(String basePath) {
        if (basePath != null && new File(basePath).exists() && new File(basePath).isDirectory()) {
            SimpleHttpServer.basePath = basePath;
        }
    }

    public static void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("start");
        Socket socket = null;
        while ((socket = serverSocket.accept()) != null) {
            System.out.println("request got");
            handlerThreadPool.submit(new HttpRequestHandler(socket));
        }
        serverSocket.close();
    }

    static class HttpRequestHandler implements Runnable {
        private Socket socket = null;

        public HttpRequestHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            String line = null;
            BufferedReader br = null;

            BufferedReader reader = null;
            PrintWriter out = null;
            InputStream in = null;

            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String header = reader.readLine();
                // calculate the absolute path according to the relative path
                String fileName = header.split(" ")[1];
                String filePath = basePath + header.split(" ")[1];
                out = new PrintWriter(socket.getOutputStream());

                System.out.println("header: " + header);
                System.out.println("filePath: " + filePath);
                System.out.println();

                File pendingFile = new File(filePath);
                String filePathLower = filePath.toLowerCase();

                if (!pendingFile.exists()) {
                    System.out.println(fileName + " not found in server");
                    out.println("HTTP/1.1 404 Not found");
                    out.println("Server: Yflog");
                    out.println("Content-Type: text/html");
                    out.println("");
                    out.println(fileName + " not found in server");

                }
                // process different resource differently
                else if (filePath.toLowerCase().endsWith(".jpg")
                        || filePathLower.endsWith(".png")
                        || filePathLower.endsWith(".ico")) {
                    // image resource
                    in = new FileInputStream(filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i = 0 ;
                    while ((i = in.read()) != -1) {
                        baos.write(i);
                    }

                    byte[] array = baos.toByteArray();
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Yflog");
                    out.println("Content-Type: image/jpeg");
                    out.println("");
                    socket.getOutputStream().write(array, 0, array.length);
                }
                else if (filePathLower.endsWith(".js")) {
                    // js resource
                    in = new FileInputStream(filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i = 0;
                    while ((i = in.read())!= -1) {
                        baos.write(i);
                    }

                    byte[] array = baos.toByteArray();
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Yflog");
                    out.println("Content-Type: application/javascript");
                    socket.getOutputStream().write(array, 0, array.length);
                }
                else if (filePathLower.endsWith(".css")) {
                    // css resource
                    in = new FileInputStream(filePath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int i = 0;
                    while ((i = in.read())!= -1) {
                        baos.write(i);
                    }

                    byte[] array = baos.toByteArray();
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Yflog");
                    out.println("Content-Type: text/css");
                    socket.getOutputStream().write(array, 0, array.length);
                }
                else {
                    File file = new File(filePath);
                    if (file.isDirectory()) {
                        file = new File(filePath + "/index.html");
                    }
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    out = new PrintWriter(socket.getOutputStream());
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: Yflog");
                    out.println("Content-Type: text/html; charset=UTF-8");
                    out.println("");

                    while ((line = br.readLine()) != null) {
                        out.println(line);
                    }
                }

                out.flush();
                System.out.println("flushed");

            } catch (IOException e) {
                System.out.println("exception ");
                if (out != null) {
                    out.println("HTTP/1.1 500");
                    out.println("");
                    out.flush();

                }
                e.printStackTrace();
            }
            finally {
                close(br, in, reader, out, socket);
                System.out.println("closed");
            }
        }

        private static void close(Closeable... closeables) {
            if (closeables != null) {
                for (Closeable closeable : closeables) {
                    try {
                        System.out.println("try close");
                        closeable.close();
                    } catch (Exception e) {
                        System.out.println("close failed");
                    }
                    System.out.println("close done");

                }
            }
        }
    }


}
