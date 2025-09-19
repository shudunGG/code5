package com.om.common.util;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**  
 * 判断网络连接状况.  
 *  
 */   
public class NetState {    
	static Logger log = Logger.getLogger(NetState.class.getName());
    public boolean isConnect(String ip){   
    	log.debug("enter NetState:"+ip);      
        boolean connect = false;    
        Runtime runtime = Runtime.getRuntime();    
        Process process;    
        try {    
        	log.debug("enter NetState:exec"+ip);
            process = runtime.exec("ping -c 2 " + ip);    
            InputStream is = process.getInputStream();     
            InputStreamReader isr = new InputStreamReader(is,"GBK");     
            BufferedReader br = new BufferedReader(isr);     
            String line = null;     
            StringBuffer sb = new StringBuffer();     
            while ((line = br.readLine()) != null) {     
                sb.append(line).append("\n");     
            }     
            log.debug("ping values:"+sb);      
            is.close();     
            isr.close();     
            br.close();     
     
            if (null != sb && !sb.toString().equals("")) {     
                String logString = "";     
                if (sb.toString().toUpperCase().indexOf("TTL") > 0) {     
                    // 网络畅通      
                    connect = true;    
                } else {     
                    // 网络不畅通      
                    connect = false;    
                }     
            }     
        } catch (Exception e) {    
            log.error(e.getMessage(), e);    
        }   
        
        return connect;    
    }    

    
	public boolean isAddressAvailable(String ip) {
		try {
			InetAddress address = InetAddress.getByName(ip);// ping this IP
			if (address.isReachable(3000)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static String getMac() {
		StringBuffer buf = new StringBuffer();
		try {
			Enumeration<NetworkInterface> el = NetworkInterface
					.getNetworkInterfaces();
			InetAddress ip = null;
			while (el.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) el.nextElement();
				byte[] mac = ni.getHardwareAddress();
				if (mac == null || mac.length == 0)
					continue;
				Enumeration<InetAddress> nii = ni.getInetAddresses();
				while (nii.hasMoreElements()) {
					ip = nii.nextElement();
					if (ip instanceof Inet6Address)
						continue;
					if (!ip.isReachable(3000))
						continue;
					for (int n=0;n<mac.length;n++) {
						byte b = mac[n];
						if (Integer.toHexString(0xFF & b).length() == 1)
							buf.append("0").append(Integer.toHexString(0xFF & b));
						else
							buf.append(Integer.toHexString(0xFF & b));
						if(n < mac.length - 1) buf.append("-");
					}
					buf.append("&");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buf.toString().toUpperCase();
	}

    public static void main(String[] args) {    
//        NetState netState = new NetState();    
//        System.out.println(netState.isConnect());    
        
        //NetState netState = new NetState();    
        //netState.isAddressAvailable("192.168.43.110");
    	
    	String mac = NetState.getMac();
		System.out.println("mac:"+mac);
    }    
}