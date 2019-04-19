package org.iproduct.iot.demo.resources;

import org.iproduct.iot.demo.udp.EventListener;
import org.iproduct.iot.demo.udp.UDPServer;
import org.iproduct.iot.demo.udp.UDPServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

@Path("api")
public class IotResource {
    private static final Logger log = LoggerFactory.getLogger(IotResource.class);

    private UDPServer server = UDPServerFactory.getServer();

    @GET
    @Path("hello")
    public String sayHello() {
        return "Hello, World!";
    }

    @GET
    @Path("events-text")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void eventTextStream(@Context SseEventSink eventSink, @Context Sse sse) {
        server.registerListener(new EventListener<String>() {
            @Override
            public void onEvent(String eventPayload) {

                final OutboundSseEvent event = sse.newEventBuilder()
                        .name("iot-event")
                        .data(String.class, eventPayload)
                        .build();
                if (!eventSink.isClosed()) {
                    eventSink.send(event);
                } else {
                    server.removeListener(this);
                }
            }
        });
    }

}