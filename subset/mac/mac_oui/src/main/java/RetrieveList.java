import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class RetrieveList {
	String macAddress;
	Map<String, String> macDevices = new HashMap<String, String>();
	static final int minimumMACAddressLength = 5;
	
	
	 RetrieveList(String macAddress){
		this.macAddress = macAddress;
		readLocalFile();
		startMacOuiTest();
	}
	
	public void readLocalFile(){
		try {
			  System.out.println("Reading local file...");
			  InputStream url = this.getClass().getResourceAsStream("/macList.txt");
			  StringBuilder resultStringBuilder = new StringBuilder();
			  BufferedReader br = new BufferedReader(new InputStreamReader(url));
			  String line;
			  while ((line = br.readLine()) != null) {
				resultStringBuilder.append(line).append("\n");
		        String[] words = new String[2];
		        String macAddress;
		        String manufacturer;
		        if (line.length() > minimumMACAddressLength) {
		          macAddress = line.substring(0, 6);
		          manufacturer = line.substring(7, line.length());
		          if (manufacturer.length() > 0) {
		        	  macDevices.put(macAddress, manufacturer);
		          }
		        }
			  }
			} catch (Exception e) {
				System.out.println(e);
				System.err.println("Can not read local file");
			}
	}
	
	public void startMacOuiTest() {
		MacLookup macLookup = new MacLookup(macDevices, macAddress);
		Thread macThread = new Thread(macLookup);
		macThread.start();
	}

}