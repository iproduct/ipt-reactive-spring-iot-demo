package org.iproduct.iot.demo.udp;

import java.net.InetAddress;

class AddressInfo {
    public InetAddress address;
    public int port;

    public AddressInfo(InetAddress a, int p) {
        address = a;
        port = p;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AddressInfo{");
        sb.append(address);
        sb.append(":").append(port);
        return sb.toString();
    }
}