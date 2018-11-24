package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.MACAddress;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

public class SwitchTable {
    private LinkedList<SwitchEntry> entries;

    public SwitchTable() {
        entries = new LinkedList<>();
    }

    public void addEntry(SwitchEntry entry) {
        Iterator<SwitchEntry> iter = entries.iterator();
        while (iter.hasNext()) {
            if (iter.next().getAddr().equals(entry.getAddr())) {
                iter.remove();
                break;
            }
        }
        entries.add(entry);
    }

    protected void refresh() {
        while (!entries.isEmpty()) {
            long earliestTime = System.currentTimeMillis() - 15000;
            if (entries.get(0).getTime() < earliestTime)
                entries.remove(0);
            else break;
        }
    }

    protected String searchOutIFace(MACAddress outAddr) {
        Iterator<SwitchEntry> iter = entries.iterator();
        while (iter.hasNext()) {
            SwitchEntry entry = iter.next();
            if (entry.getAddr().equals(outAddr)) return entry.getInterfaceName();
        }
        return null;
    }

}
