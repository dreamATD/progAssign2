package edu.wisc.cs.sdn.vnet.sw;

import edu.wisc.cs.sdn.vnet.Iface;
import net.floodlightcontroller.packet.MACAddress;

public class SwitchEntry {
    private MACAddress addr;
    private String interfaceName;
    private long time;

    public SwitchEntry(MACAddress a, String i, long t) {
        addr = a;
        interfaceName = i;
        time = t;
    }

    public MACAddress getAddr() {
        return addr;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public long getTime() {
        return time;
    }

}
