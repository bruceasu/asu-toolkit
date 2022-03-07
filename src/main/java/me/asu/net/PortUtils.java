package me.asu.net;

import java.io.IOException;
import java.net.ServerSocket;


public class PortUtils {

    public static int getFreeSocketPort() {
        byte port = 0;

        try {
            ServerSocket s = new ServerSocket(0);
            int port1 = s.getLocalPort();
            s.close();
            return port1;
        } catch (IOException var2) {
            return port;
        }
    }
}
