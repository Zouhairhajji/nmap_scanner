/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.hotel.gatines.ping_ips;

/**
 *
 * @author zouhairhajji
 */
public class PingMain {

    public static void main(String[] args) throws Exception {

        NmapLib nmapLib = new NmapLib("192.168.0.0", 16);
        
        
        System.out.println(nmapLib.isReachable("192.168.27.5"));;
        
        
        nmapLib.findAllAddresses();
        
        System.out.println(nmapLib.findAllPorts("192.168.0.1", 20, 10024));
    }
}
