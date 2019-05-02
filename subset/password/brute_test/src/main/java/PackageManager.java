import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PackageManager {
	
	String host; 
	int connectionPort; 
	SshSocket sshSocket = null; 
	TelnetSocket telnetSocket = null; 
	String protocol; 
	Map<String,String> macDevices = new HashMap<String,String>();
	boolean debug = false;
	
	
	public void readFile(){
	
		
//		try {
//			System.out.println("Attempting to read web file...");
//			
//			InputStream url = new URL("https://svn.nmap.org/nmap/nmap-mac-prefixes").openStream();
//		//	InputStream url = this.getClass().getResourceAsStream("resources/macList.txt");
//			StringBuilder resultStringBuilder = new StringBuilder();
//		    try (BufferedReader br= new BufferedReader(new InputStreamReader(url))){
//		        String line;
//		        while ((line = br.readLine()) != null) {
//		            resultStringBuilder.append(line).append("\n");
//		           // System.out.println(line);
//		            String[] words = new String[2];
//		            String macAddress;
//		            String manufacturer;
//		            if(line.length() > 5) {
//		            	macAddress = line.substring(0, 6);
//		            	manufacturer = line.substring(7, line.length());
//			            if(manufacturer.length() > 0) {
//			            	macDevices.put(macAddress, manufacturer);
//			            }
//		            }
//		         }
//		      //  if(debug) {
//		        	//macDevices.forEach((k, v) -> System.out.println((k + ":" + v)));
//		      //  }
//		    }
//		  } catch (IOException e1) {
		//	System.out.println("Package Manager Error :" + e1);
			System.out.println("Attempting to read local file...");
			  try{
			InputStream url = this.getClass().getResourceAsStream("/macList.txt");
			StringBuilder resultStringBuilder = new StringBuilder();
			BufferedReader br= new BufferedReader(new InputStreamReader(url));
		        String line;
		        while ((line = br.readLine()) != null) {
		            resultStringBuilder.append(line).append("\n");
		            String[] words = new String[2];
		            String macAddress;
		            String manufacturer;
		            if(line.length() > 5) {
		            	macAddress = line.substring(0, 6);
		            	manufacturer = line.substring(7, line.length());
			            if(manufacturer.length() > 0) {
			            	macDevices.put(macAddress, manufacturer);
			            }
		            }
		         }
		      //  if(debug) {
		        	//macDevices.forEach((k, v) -> System.out.println((k + ":" + v)));
		      //  }
		    }
		    catch(Exception e) {
		    	System.out.println(e);
		    	System.err.println("Cannot read local file");
		    	
		    }
			
	//	}
	}	
	
	public PackageManager(String protocol, String host, int connectionPort) {
		try {
			System.out.println("Package manager started...");
			this.host = host; 
			this.connectionPort = connectionPort;
			
			readFile();
			
			switch(protocol) {
			case "ssh":
				setUpSshConnection();
				break;
			
			case "telnet":
				setUpTelnetConnection();
				break;
			}
		}
		catch (Exception e) {
			System.out.println("PackageManager CONSTRUCTOR:"+e.getMessage());
		}
	}
	
	public void setUpSshConnection() {
		sshSocket = new SshSocket(host,macDevices,connectionPort);
		Thread sshThread = new Thread(sshSocket);
		sshThread.start();
	}
	
	public void setUpTelnetConnection() {
		try {
		telnetSocket = new TelnetSocket(host, macDevices,connectionPort);
		Thread telnetThread = new Thread(telnetSocket);
		telnetThread.start();
		}
		catch (Exception e) {
			System.out.println("setupTelnetConnection:"+e.getMessage());
		}
	}
}
