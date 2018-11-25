package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.MACAddress;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class Router extends Device
{	
	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
	private ArpCache arpCache;
	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Router(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache();
	}
	
	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable()
	{ return this.routeTable; }
	
	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile)
	{
		if (!routeTable.load(routeTableFile, this))
		{
			System.err.println("Error setting up routing table from file "
					+ routeTableFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static route table");
		System.out.println("-------------------------------------------------");
		System.out.print(this.routeTable.toString());
		System.out.println("-------------------------------------------------");
	}
	
	/**
	 * Load a new ARP cache from a file.
	 * @param arpCacheFile the name of the file containing the ARP cache
	 */
	public void loadArpCache(String arpCacheFile)
	{
		if (!arpCache.load(arpCacheFile))
		{
			System.err.println("Error setting up ARP cache from file "
					+ arpCacheFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static ARP cache");
		System.out.println("----------------------------------");
		System.out.print(this.arpCache.toString());
		System.out.println("----------------------------------");
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

		/*
		* Check the type of the package
		* */
		if (etherPacket.getEtherType() != Ethernet.TYPE_IPv4) return;
		IPv4 payload = (IPv4) etherPacket.getPayload();

		/*
		* Check the checksum.
		* */
		int optionsLength = 0;

		byte[] data = new byte[payload.getTotalLength()];
		ByteBuffer bb = ByteBuffer.wrap(data);

		bb.put((byte) (((payload.getVersion() & 0xf) << 4) | (payload.getHeaderLength() & 0xf)));
		bb.put(payload.getDiffServ());
		bb.putShort(payload.getTotalLength());
		bb.putShort(payload.getIdentification());
		bb.putShort((short) (((payload.getFlags() & 0x7) << 13) | (payload.getFragmentOffset() & 0x1fff)));
		bb.put(payload.getTtl());
		bb.put(payload.getProtocol());
		bb.putShort((short) 0);
		bb.putInt(payload.getSourceAddress());
		bb.putInt(payload.getDestinationAddress());
		if (payload.getOptions() != null)
			bb.put(payload.getOptions());

		bb.rewind();
		int accumulation = 0;
		for (int i = 0; i < payload.getHeaderLength() * 2; ++i) {
			accumulation += 0xffff & bb.getShort();
		}
		accumulation = ((accumulation >> 16) & 0xffff)
				+ (accumulation & 0xffff);
		short checksum = payload.getChecksum();
		System.out.println("Checksum: " + accumulation + " " + checksum + " " + ((accumulation ^ checksum) & 0xffff));
		if (((accumulation ^ checksum) & 0xffff) != 0xffff) return;

		/*
		* Subtract TTL.
		* */
		((IPv4) etherPacket.getPayload()).setTtl((byte) (payload.getTtl() - 1));
		if (payload.getTtl() == 0) return;

		/*
		* Check whether the destination IP address equals the router's interface
		* */
		int srcIP = payload.getSourceAddress();
		int dstIP = payload.getDestinationAddress();
		for (Map.Entry<String, Iface> entry: interfaces.entrySet()) {
			if (entry.getValue().getIpAddress() == dstIP) return;
		}

		/*
		* Forwarding packet
		* */
		RouteEntry routeEntry = routeTable.lookup(dstIP);
//		System.out.println(routeEntry);
		if (routeEntry == null) return;
		Iface outIface = routeEntry.getInterface();
		byte[] nsrcMAC = outIface.getMacAddress().toBytes();
		byte[] ndstMAC = arpCache.lookup(dstIP).getMac().toBytes();
		etherPacket.setDestinationMACAddress(ndstMAC);
		etherPacket.setSourceMACAddress(nsrcMAC);

		etherPacket.getPayload().resetChecksum();
		etherPacket.getPayload().serialize();

//		System.out.println(etherPacket.toString());
		sendPacket(etherPacket, outIface);
		
		/********************************************************************/
	}
}
