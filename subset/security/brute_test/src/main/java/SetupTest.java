import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SetupTest {

  String host;
  int connectionPort;
  String macAddress;
  SshSocket sshSocket = null;
  Map<String, String> macDevices = new HashMap<String, String>();
  boolean debug = false;
  static final int minimumMACAddressLength = 5;
  private static final int addressStartPosition = 0;
  private static final int addressEndPosition = 6;
  private static final int manufacturerNamePosition = 7;

  public void readMacList() {
    try {
      InputStream inputStream = this.getClass().getResourceAsStream("/macList.txt");
      StringBuilder resultStringBuilder = new StringBuilder();
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      while ((line = br.readLine()) != null) {
        resultStringBuilder.append(line).append("\n");
        String macAddress;
        String manufacturer;
        if (line.length() > minimumMACAddressLength) {
          macAddress = line.substring(addressStartPosition, addressEndPosition);
          manufacturer = line.substring(manufacturerNamePosition);
          if (manufacturer.length() > addressStartPosition) {
            macDevices.put(macAddress, manufacturer);
          }
        }
      }
    } catch (Exception e) {
      System.out.println(e);
      System.err.println("Can not read local file");
    }
  }

  public SetupTest(String host, int connectionPort, String macAddress) {
    try {
      System.out.println("Package manager started...");
      this.host = host;
      this.connectionPort = connectionPort;
      this.macAddress = macAddress;

      readMacList();
      setUpSshConnection();
    } catch (Exception e) {
      System.out.println("PackageManager CONSTRUCTOR:" + e.getMessage());
    }
  }

  public void setUpSshConnection() {
    sshSocket = new SshSocket(host, macDevices, connectionPort, macAddress);
    Thread sshThread = new Thread(sshSocket);
    sshThread.start();
  }
}
