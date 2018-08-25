/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hotel.gatines.ping_ips;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.net.util.SubnetUtils;

/**
 *
 * @author zouhairhajji
 */
public class NmapLib {

    private String host;
    private Integer subDomain;
    private static SubnetUtils util;

    public NmapLib(String host, int subdomain) {
        this.host = host;
        this.subDomain = subdomain;
    }

    public synchronized String[] findAllAddresses() {
        this.util = new SubnetUtils(String.format("%s/%d", this.host, this.subDomain));
        return this.util.getInfo().getAllAddresses();
    }

    public List<Integer> findAllPorts(String hostname, Integer portMin, Integer portMax) throws InterruptedException {
        final List<Integer> ports = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(1500);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        
        IntStream.range(portMin, portMax)
                .parallel()
                .forEach(port -> {
                    executor.submit(() -> {
                        atomicInteger.addAndGet(1);
                        try {
                            Socket socket = new Socket();
                            socket.connect(new InetSocketAddress(hostname, port), 5000);
                            socket.close();
                            ports.add(port);
                        } catch (IOException ex) {
                        }
                    });
                });

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        while (!executor.isTerminated()) {
            System.out.println("> " + atomicInteger.get() + " : " + (atomicInteger.get() * 100 / (portMax-portMin)) + " %");
            Thread.sleep(1000);
        }

        return ports;
    }

    public Map<String, Boolean> pingAllAddresses() throws Exception {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        String[] hosts = this.findAllAddresses();
        ExecutorService executor = Executors.newFixedThreadPool(1000);
        Map<String, Boolean> result = new HashMap<>();
        Stream.of(hosts)
                .forEach(s -> {
                    executor.submit(() -> {
                        Boolean isReachable = this.isReachable(s);
                        result.put(s, isReachable);
                        Integer ordre = atomicInteger.addAndGet(1);
                        if (isReachable) {
                            System.out.println(String.format("%d / %d ) the hostname %s is %s", ordre, hosts.length, s, isReachable));
                        }
                    });
                });

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        while (!executor.isTerminated()) {
            System.out.println("> " + atomicInteger + " : " + (atomicInteger.get() * 100 / hosts.length) + " %");
            Thread.sleep(1000);
        }

        return result;
    }

    public Boolean isReachable(String host) {
        try {
            return InetAddress.getByName(host).isReachable(1000);
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

}
