import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.net.telnet.TelnetClient;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class TelnetSocket implements Runnable{
	
	String host;
	TelnetClient telnetClient;
	OutputStream outputStream; 
	InputStream inputStream; 
	InetAddress ip;
	int connectionPort;
	Interrogator interrogator;
	MACHandler macHandler;
	String macAddress;
	int bytesRead = 0;
	byte[] messageByte = new byte[1024];
	Charset chars = Charset.forName("UTF-8");
	Map macDevices;
	Queue<String> rxQueue = new LinkedList();
	boolean debug = false;
	Thread gatherThread;
	Thread readThread;
	Thread checkThread;
	Gson gson = new Gson();
	File jsonFile = new File("resources/defaultPasswords.json");
	InputStream jsonStream = this.getClass().getResourceAsStream("/defaultPasswords.json");
	String[] jsonPasswords;
	String[] jsonUsernames;

	
	public TelnetSocket(String host, Map macDevices, int connectionPort) {
		this.connectionPort = connectionPort;
		this.macDevices = macDevices;
		this.host = host; 
		macHandler = new MACHandler();
	}
	
	public void getMACAddress(){
		
		try {
		
			macAddress = macHandler.runShellCommand("arp -a " + host);
			macAddress = macAddress.replace("-","");
			System.out.println("MAC ADDRESS : " + macAddress + "  "+  macDevices.get(macAddress.substring(0,6)));
			//001AEB96DDE0 AlliedTelesis R&D Center
		
			System.out.println(macDevices.get(macAddress.substring(0,6)));
			getJsonFile(macAddress.substring(0,6).toString());
			
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
			Report reportHandler = new Report();
			reportHandler.addText("test failed : manufacturer not found");
			reportHandler.writeReport("telnet");
		}
	}
	
	public void getJsonFile(String model) {
		try {
			JsonObject jsonObject = gson.fromJson(new InputStreamReader(jsonStream), JsonObject.class);
			JsonObject parent = jsonObject.getAsJsonObject(model);
			String usernames = parent.get("Usernames").getAsString();
			String passwords = parent.get("Passwords").getAsString();
			jsonUsernames = usernames.split(",");
			jsonPasswords = passwords.split(",");
			} catch (JsonSyntaxException e) {
				System.err.println("Json Files Syntax Error");
				System.err.println(e);
			} catch (JsonIOException e) {
				System.err.println("Json File Read Error");
				System.err.println(e);
		} 
	}
		
	public void connectTelnetClient() {
		telnetClient = null;
		telnetClient = new TelnetClient();
		System.out.println("Starting Telnet Connection");
		try {
			telnetClient.connect(host,connectionPort);
			System.out.println("Connected");
		}
		catch(Exception e){
			System.err.println(e);
		}
	}
	
	private String normalizeLineEnding(byte[] bytes, char endChar) {
		  
			List<Byte> bytesBuffer = new ArrayList<Byte>();

		    int countBreak = 0;
		    int countESC = 0;

		    for (int i = 0; i < bytes.length; i++) {
		      if (bytes[i] != 0) {
		        switch (bytes[i]) {
		          case 8:
		            // backspace \x08
		            break;
		          case 10:
		            // newLineFeed \x0A
		            countBreak++;
		            bytesBuffer.add((byte) endChar);
		            break;
		          case 13:
		            // carriageReturn \x0D
		            countBreak++;
		            bytesBuffer.add((byte) endChar);
		            break;
		          case 27:
		            // escape \x1B
		            countESC = 2;
		            break;
		          case 33:
		            // character:!
		            break;
		          default:
		            if (countESC == 0) {
		              if (countBreak > 1) {
		                int size = bytesBuffer.size();
		                for (int x = 0; x < countBreak - 1; x++) {
		                  bytesBuffer.remove(size - 1 - x);
		                }
		                countBreak = 0;
		              }
		              bytesBuffer.add(bytes[i]);
		            } else {
		              countESC--;
		            }
		            break;
		        }
		      }
		    }

		    String bytesString = "";

		    for (Byte byteBuffer : bytesBuffer) {
		      bytesString = bytesString + (char) (byte) byteBuffer;
		    }

		 return bytesString;
	 }
	
	public void readData() {
	    int bytesRead = 0;
	
	    inputStream = telnetClient.getInputStream();

	    while (telnetClient.isConnected()) {
	    	inputStream = telnetClient.getInputStream();
	    	
	      try {
	        byte[] buffer = new byte[1024];

	        bytesRead = inputStream.read(buffer);
	        if (bytesRead > 0) {
	          String rawData = normalizeLineEnding(buffer, '\n');
	          rxQueue.add(rawData);
	         } else {
	          try {
	            Thread.sleep(100);
	          } catch (InterruptedException e) {
	            System.err.println("InterruptedException readData:" + e.getMessage());
	          }
	        }
	      } catch (IOException e) {
	        System.err.println("Exception while reading socket:" + e.getMessage());
	      }
	    }
	}
	
	public void gatherData() {
			StringBuilder rxData = new StringBuilder();
		    String rxGathered = "";
		    int rxQueueCount = 0;
		    int rxTempCount = 0;
		    
		    while (telnetClient.isConnected()) {
		     try {
		    	  if (rxQueue.isEmpty()) {
		    		  Thread.sleep(100);
		    		  rxQueueCount++;
		    		  if (debug) {
		    			  System.out.println("rxQueue.isEmpty:" + rxQueueCount);
		    		  }
		    		  if (rxQueueCount > 70) {
		    			  rxQueueCount = 0;
		    			  writeData("\n");
		    		  }
		    	  } 
		    	  else {
		    		  rxQueueCount = 0;
		    		  String rxTemp = rxQueue.poll();
		    		  if (rxTemp.equals("")) {
		    			  Thread.sleep(100);
		    			  rxTempCount++;
		    			  if (debug) {
		    				  System.out.println("rxTemp.equals:" + rxTempCount);
		    			  }
		    		  } 
		    		  else {
		    			  rxQueueCount = 0;
		    			  rxTempCount = 0;
		    			  rxData.append(rxTemp);
		    			  rxGathered = rxData.toString();
		    			  System.out.println(
		    					  java.time.LocalTime.now()
		                    + "rxDataLen:"
		                    + rxGathered.length()
		                    + "rxData:"
		                    + rxGathered);
		      
		    			 
		    			  interrogator.receiveData(rxGathered);
		    			  rxData.delete(0,rxGathered.length());
		    		  	}
		          }
		      }
		     catch (InterruptedException e) {
		        System.err.println("InterruptedException gatherData:" + e.getMessage());
		      }
		     
		    }
	}
	
		public void writeData(String data)  {
		System.out.println(data);
		try {
			outputStream = telnetClient.getOutputStream();
			outputStream.write(data.getBytes());
			outputStream.flush();
		} catch (IOException e) {
			resetConnection();
			e.printStackTrace();
		}
}
		
	public void resetConnection() {
		connectTelnetClient();
		disconnect();
		connectTelnetClient();
	}
		
	public void checkConnection() {
		if(!telnetClient.isConnected()) {
			connectTelnetClient();
		}
	}
		
	public void disconnect() {
		try {
			telnetClient.disconnect();
		}
		catch(Exception e) {
			System.err.println(e);
		}
	}
	
	@Override
	public void run() {
		connectTelnetClient();
		getMACAddress();
		interrogator = new Interrogator(this, jsonUsernames, jsonPasswords, macAddress, macDevices.get(macAddress.substring(0, 6)).toString());
		Runnable readDataRunnable = 
				() ->{
			readData();
		};
		readThread = new Thread(readDataRunnable);
		
		readThread.start();
		
		Runnable gatherDataRunnable = 
				() ->{
					gatherData();
				};
				gatherThread = new Thread(gatherDataRunnable);
				
				gatherThread.start();
				
		Runnable checkConnectionRunnable = 
				() ->{
					checkConnection();
				};
				checkThread = new Thread(checkConnectionRunnable);
				checkThread.start();
		}
	}
