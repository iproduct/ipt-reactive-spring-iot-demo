package org.iproduct.iot.demo.resources;

import org.iproduct.iot.demo.udp.EventListener;
import org.iproduct.iot.demo.udp.UDPServer;
import org.iproduct.iot.demo.udp.UDPServerFactory;
import org.json.simple.JSONObject;
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
    public void eventJsonStreamAsync(@Context SseEventSink eventSink, @Context Sse sse) {
        while(!eventSink.isClosed()) {
            server.getCurrentEvent()
                    .thenApply(payload ->
                            sse.newEventBuilder()
                                    .name("iot-event")
                                    .data(String.class, payload)
                                    .build()
                    ).thenAccept(event -> eventSink.send(event))
                    .exceptionally(ex -> {
                        log.error("Error:", ex);
                        return null;
                    }).join();
        }
    }

    @GET
    @Path("events-json")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void eventJsonStream(@Context SseEventSink eventSink, @Context Sse sse) {
        while(!eventSink.isClosed()) {
            server.getCurrentEvent()
                    .thenApply(textPayload -> {
                        String[] tokens = textPayload.split("\\s+");
                        JSONObject obj = new JSONObject();
                        obj.put("time", Long.parseLong(tokens[1]));
                        obj.put("button", Integer.parseInt(tokens[21]));
                        obj.put("distance", Integer.parseInt(tokens[23]));

                        obj.put("roll", Float.parseFloat(tokens[3]));
                        obj.put("pitch", Float.parseFloat(tokens[5]));
                        obj.put("yaw", Float.parseFloat(tokens[7]));

                        JSONObject accel = new JSONObject();
                        accel.put("x", Integer.parseInt(tokens[9]));
                        accel.put("y", Integer.parseInt(tokens[10]));
                        accel.put("z", Integer.parseInt(tokens[11]));
                        obj.put("accel", accel);

                        JSONObject gyro = new JSONObject();
                        gyro.put("x", Integer.parseInt(tokens[13]));
                        gyro.put("y", Integer.parseInt(tokens[14]));
                        gyro.put("z", Integer.parseInt(tokens[15]));
                        obj.put("compass", gyro);

                        JSONObject compass = new JSONObject();
                        compass.put("x", Integer.parseInt(tokens[17]));
                        compass.put("y", Integer.parseInt(tokens[18]));
                        compass.put("z", Integer.parseInt(tokens[19]));
                        obj.put("gyro", compass);

                        return obj.toJSONString();

                    }).thenApply(payload ->
                            sse.newEventBuilder()
                                    .name("iot-event")
                                    .data(String.class, payload)
                                    .build()
                    ).thenAccept(event -> eventSink.send(event))
                    .exceptionally(ex -> {
                        log.error("Error:", ex);
                        return null;
                    }).join();
        }
    }

}