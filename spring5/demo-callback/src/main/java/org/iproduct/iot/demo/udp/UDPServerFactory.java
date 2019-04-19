package org.iproduct.iot.demo.udp;

public class UDPServerFactory {
    private static UDPServer server;

    public static UDPServer getServer() {
        if (server == null) {
            server = new UDPServer();
            server.start();
        }
        return server;
    }
}