package org.iproduct.iot.demo.udp;

// UDPServer.java
// Chat server using UDP communication protocol. Works with multiple clients.
// (c) Copyright IPT - Intellectual Products & Technologies Ltd., 2004-2006.
// All rights reserved. This software program can be compiled and modified only as a part of the 
// "Programming in Java" course provided by IPT - Intellectual Products & Technologies Ltd.,
// for educational purposes only, and provided that this copyright notice is kept unchanged 
// with the program. The program is provided "as is", without express or implied warranty of any 
// kind, including any implied warranty of merchantability, fitness for a particular purpose or 
// non-infringement. Should the Source Code or any resulting software prove defective, the user
// assumes the cost of all necessary servicing, repair, or correction. In no event shall 
// IPT - Intellectual Products & Technologies Ltd. be liable to any party under any legal theory 
// for direct, indirect, special, incidental, or consequential damages, including lost profits, 
// business interruption, loss of business information, or any other pecuniary loss, or for
// personal injuries, arising out of the use of this source code and its documentation, or arising 
// out of the inability to use any resulting program, even if IPT - Intellectual Products & 
// Technologies Ltd. has been advised of the possibility of such damage. 
// Contact information: www.iproduct.org, e-mail: office@iproduct.org 

import org.iproduct.iot.demo.jersey.BootstrapJersey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Model;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.*;

@ApplicationScoped
@Model
public class UDPServer {
    static final int PORT = 4210;
    static final String IP_ADDRESS = "192.168.137.1";
    private static final Logger log = LoggerFactory.getLogger(BootstrapJersey.class);
    private byte[] buffer = new byte[2000];
    private DatagramPacket inPacket =
            new DatagramPacket(buffer, buffer.length);
    private static DatagramSocket socket;
    private Thread thread;
    private volatile CompletableFuture<String> currentEvent;

    public UDPServer() {
    }

    public void start() {
        updateEvents().exceptionally(ex -> {
            log.error("Error: ", ex);
            return null;
        });
    }

    public CompletableFuture<String> getCurrentEvent() {
        return currentEvent;
    }

    protected CompletableFuture<Void> updateEvents() {
        return CompletableFuture.runAsync(() -> {
            while (true) {
                currentEvent = getNextEvent();
                currentEvent.join();
            }
        });
    }

    protected CompletableFuture<String> getNextEvent() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (socket == null) {
                    socket = new DatagramSocket(PORT, InetAddress.getByName(IP_ADDRESS));
                    System.out.println("UDP Server started on " + socket.getLocalSocketAddress());
                }
                socket.receive(inPacket);
            } catch (SocketException ex) {
                log.error("Can't open socket", ex);
                throw new CompletionException(ex);
            } catch (IOException ex) {
                log.error("Communication error", ex);
                throw new CompletionException(ex);
            }
            return DatagramUtility.getString(inPacket);
        });
    }

}