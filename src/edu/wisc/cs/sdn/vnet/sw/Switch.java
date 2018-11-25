package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import net.floodlightcontroller.packet.MACAddress;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Aaron Gember-Jacobson
 */
public class Switch extends Device
{	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */

	SwitchTable table;

	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);
		table = new SwitchTable();
	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
                etherPacket.toString().replace("\n", "\n\t"));
		
		/********************************************************************/
		/* TODO: Handle packets                                             */

		MACAddress srcMac = etherPacket.getSourceMAC();
		MACAddress dstMac = etherPacket.getDestinationMAC();
		String inIfaceName = inIface.getName();

		/*
		* Remove timeout entries
		* */
		table.refresh();

		/*
		* Add a new entry to the entries.
		* */
		SwitchEntry entry = new SwitchEntry(srcMac, inIface.getName(), System.currentTimeMillis());
		table.addEntry(entry);

		/*
		* Deliver the packet.
		* */
		String outIfaceName = table.searchOutIFace(dstMac);
		Iface outIface = interfaces.get(outIfaceName);
		if (outIfaceName != null) sendPacket(etherPacket, outIface);
		else for (Map.Entry<String, Iface> mapEntry: interfaces.entrySet()) {
			sendPacket(etherPacket, mapEntry.getValue());
		}

		/********************************************************************/
	}
}
