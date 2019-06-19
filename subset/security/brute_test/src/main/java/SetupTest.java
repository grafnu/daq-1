import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SetupTest {

  String host;
  int connectionPort;
  String macAddress;
  SshSocket sshSocket = null;
  TelnetSocket telnetSocket = null;
  String protocol;
  Map<String, String> macDevices = new HashMap<String, String>();
  boolean debug = false;
  static final int minimumMACAddressLength = 5;
  
  public void readLocalFile() {
	try {
	  System.out.println("Reading local file...");
	  InputStream inputStream = this.getClass().getResourceAsStream("/macList.txt");
	  StringBuilder resultStringBuilder = new StringBuilder();
	  BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
	  String line;
	  while ((line = br.readLine()) != null) {
		resultStringBuilder.append(line).append("\n");
        	String macAddress;
        	String manufacturer;
        	if (line.length() > minimumMACAddressLength) {
          		macAddress = line.substring(0, 6);
          		manufacturer = line.substring(7);
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

  public SetupTest(String protocol, String host, int connectionPort, String macAddress) {
    try {
      System.out.println("Package manager started...");
      this.host = host;
      this.connectionPort = connectionPort;
      this.macAddress = macAddress;

      readLocalFile();

      switch (protocol) {
        case "ssh":
          setUpSshConnection();
          break;

        case "telnet":
          setUpTelnetConnection();
          break;
      }
    } catch (Exception e) {
      System.out.println("PackageManager CONSTRUCTOR:" + e.getMessage());
    }
  }

  public void setUpSshConnection() {
    sshSocket = new SshSocket(host, macDevices, connectionPort, macAddress);
    Thread sshThread = new Thread(sshSocket);
    sshThread.start();
  }

  public void setUpTelnetConnection() {
   telnetSocket = new TelnetSocket(host, macDevices, connectionPort, macAddress);
   Thread telnetThread = new Thread(telnetSocket);
   telnetThread.start();
   
  }
}
