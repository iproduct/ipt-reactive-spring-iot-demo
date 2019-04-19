package org.iproduct.demos.spring.streamingdemos.udp;

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

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

@Service
public class UDPServer implements Runnable {
    static final int PORT = 4210;
    static final String IP_ADDRESS = "192.168.137.1";
    private static final Logger log = LoggerFactory.getLogger(UDPServer.class);
    private byte[] buffer = new byte[2000];
    private DatagramPacket inPacket =
            new DatagramPacket(buffer, buffer.length);
    private static DatagramSocket socket;
    private Thread thread;

    EmitterProcessor<String> emitter = EmitterProcessor.create();
    FluxSink<String> sink = emitter.sink();

    public UDPServer() {
    }

    @PostConstruct
    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    @PreDestroy
    public void destroy() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    public Flux<String> getEventEmitter() {
        return emitter;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(PORT, InetAddress.getByName(IP_ADDRESS));
            System.out.println("Server started on " + socket.getLocalSocketAddress());
            while (true) {
                socket.receive(inPacket);
                AddressInfo ai = new AddressInfo(inPacket.getAddress(), inPacket.getPort());
                String payload = DatagramUtility.getString(inPacket);
                log.info(ai + ": " + payload);
                sink.next(payload); //Stream data with backpressure support
            }
        } catch (SocketException e) {
            log.error("Can't open socket", e);
            sink.error(e);
        } catch (IOException e) {
            log.error("Communication error", e);
            sink.error(e);
        }
    }

    public static void main(String[] args) {
        UDPServer server = new UDPServer();
        server.start();
        server.getEventEmitter().subscribe(
                System.out::println,
                err -> log.error("Error:", err),
                () -> log.info("Done.")
        );
    }

}
