package org.iproduct.iot.demo.jersey;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.iproduct.iot.demo.resources.IotResource;
import org.jboss.weld.environment.se.Weld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

public class BootstrapJersey {
    private static final Logger log = LoggerFactory.getLogger(BootstrapJersey.class);

    public static void main(final String[] args) throws IOException, InterruptedException, ExecutionException {
        Weld weld = new Weld();
        weld.initialize();
        URI baseUri = UriBuilder.fromUri("http://localhost/").port(8080).build();
        ResourceConfig config = new ResourceConfig(IotResource.class);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
        try {
            server.start();
            log.info("Press any key to stop the server...");
            System.in.read();
        } finally {
            log.info("Stopping server...");
            // stop the server
            server.shutdownNow();
            weld.shutdown();
            log.info("Stopped server...");
        }
    }
}
