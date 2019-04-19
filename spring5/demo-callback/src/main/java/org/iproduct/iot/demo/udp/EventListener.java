package org.iproduct.iot.demo.udp;

public interface EventListener<T> {
    void onEvent(T event);
}
