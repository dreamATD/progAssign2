#  Link & Network Layer Forwarding

By Liu Tianyi (516030910565)

## About my code

### Switch

What switches need to do to handle the coming packet is:	

1. Clear the time out entry from the switch table.
2. Add a new entry to the switch table containing information learned from the coming packet.
3. Send packet to the corresponding interface according to the switch table. If there is no record about the exit of the destination MAC address, broadcast it to each interface except the entrance.

Here is some details of my implementation: I create new classes - SwitchEntry and SwitchTable - in order to maintain the switch table and entries. The time I check and delete the timeout entries is before I get a packet and look for the interface to send out.

### Router

What routers need to do to handle the coming packet is:

1. Check whether its type is IPv4.
2. Calculate the checksum and compare to the record in the header of the payload.
3. Check, modify the TTL and discard the timeout packet.
4. Look for the corresponding interface to send out.
5. Modify the header of the packet and send out the new packet.

Here is some details: To find the exit interface, I check each entry of the table and see if it has the longest prefix matching to the destination IP address, which means, I need to find the mask with the smallest absolute value. In addition, because of the modification of TTL, it's necessary to refresh the checksum of the payload in the packet.

## Presentation

Here is the test under the topology trangle_with_sw.topo

```shell
stu@stu-VirtualBox:~/progAssign2$ sudo python ./run_mininet.py topos/triangle_with_sw.topo -a
[sudo] password for stu: 
*** Loading topology file topos/triangle_with_sw.topo
*** Writing IP file ./ip_config
*** Writing rtable file rtable.r1
Reach r2 from r1 via r1.2 and r2.2
Reach r3 from r1 via r1.3 and r3.3
*** Writing rtable file rtable.r2
Reach r1 from r2 via r2.2 and r1.2
Reach r3 from r2 via r2.3 and r3.2
*** Writing rtable file rtable.r3
Reach r1 from r3 via r3.3 and r1.3
Reach r2 from r3 via r3.2 and r2.3
*** Creating network
*** Adding controller
*** Adding hosts:
h1 h2 h3 
*** Adding switches:
r1 r2 r3 s1 s2 s3 
*** Adding links:
(h1, s1) (h2, s2) (h3, s3) (r1, r2) (r1, r3) (r1, s1) (r2, r3) (r2, s2) (r3, s3) 
*** Configuring hosts
h1 h2 h3 
*** Starting controller
*** Starting 6 switches
r1 r2 r3 s1 s2 s3 
*** Configuring routing for h1
*** Configuring routing for h2
*** Configuring routing for h3
*** Writing ARP cache file ./arp_cache
*** Configuring ARP for h1
*** Configuring ARP for h2
*** Configuring ARP for h3
*** Starting CLI:
mininet> h2 ifconfig
h2-eth0   Link encap:Ethernet  HWaddr 00:00:00:00:00:02  
          inet addr:10.0.2.102  Bcast:10.0.2.255  Mask:255.255.255.0
          inet6 addr: fe80::200:ff:fe00:2/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:99 errors:0 dropped:6 overruns:0 frame:0
          TX packets:12 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:18680 (18.6 KB)  TX bytes:996 (996.0 B)

lo        Link encap:Local Loopback  
          inet addr:127.0.0.1  Mask:255.0.0.0
          inet6 addr: ::1/128 Scope:Host
          UP LOOPBACK RUNNING  MTU:65536  Metric:1
          RX packets:0 errors:0 dropped:0 overruns:0 frame:0
          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:0 
          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)

mininet> h1 ping -c 10 10.0.2.102
PING 10.0.2.102 (10.0.2.102) 56(84) bytes of data.
64 bytes from 10.0.2.102: icmp_seq=1 ttl=62 time=229 ms
64 bytes from 10.0.2.102: icmp_seq=1 ttl=62 time=279 ms (DUP!)
64 bytes from 10.0.2.102: icmp_seq=2 ttl=62 time=170 ms
64 bytes from 10.0.2.102: icmp_seq=3 ttl=62 time=170 ms
64 bytes from 10.0.2.102: icmp_seq=4 ttl=62 time=171 ms
64 bytes from 10.0.2.102: icmp_seq=5 ttl=62 time=150 ms
64 bytes from 10.0.2.102: icmp_seq=6 ttl=62 time=159 ms
64 bytes from 10.0.2.102: icmp_seq=7 ttl=62 time=179 ms
64 bytes from 10.0.2.102: icmp_seq=8 ttl=62 time=179 ms
64 bytes from 10.0.2.102: icmp_seq=9 ttl=62 time=139 ms
64 bytes from 10.0.2.102: icmp_seq=10 ttl=62 time=159 ms

--- 10.0.2.102 ping statistics ---
10 packets transmitted, 10 received, +1 duplicates, 0% packet loss, time 9064ms
rtt min/avg/max/mdev = 139.971/180.899/279.697/38.116 ms
mininet> 
```



![1543219588331](C:\Users\69029\AppData\Roaming\Typora\typora-user-images\1543219588331.png)