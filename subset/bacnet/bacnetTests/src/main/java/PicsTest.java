import com.google.common.collect.Multimap;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import helper.*;

import java.util.Map;

public class PicsTest {

  private Connection connection;
  private BacnetValidation validator;
  private BacnetPoints bacnetPoints = new BacnetPoints();
  private String passedTestReport = "RESULT pass protocol.bacnet.pic\n";
  private String failedTestReport = "RESULT fail protocol.bacnet.pic\n";
  private String skippedTestReport = "RESULT skip protocol.bacnet.pic\n";
  private String reportAppendix = "";
  private Csv csv;
  private static LocalDevice localDevice;
  private String localIp = "";
  private String broadcastIp = "";
  boolean bacnetSupported = false;

  public PicsTest(String localIp, String broadcastIp) throws Exception {
    this.localIp = localIp;
    this.broadcastIp = broadcastIp;
    discoverDevices();
  }

  private void discoverDevices() throws Exception {
    connection = new Connection(broadcastIp, IpNetwork.DEFAULT_PORT, localIp);
    while (!connection.isTerminate()) {
      localDevice = connection.getLocalDevice();
      System.err.println("Sending whois...");
      localDevice.sendGlobalBroadcast(new WhoIsRequest());
      System.err.println("Waiting...");
      Thread.sleep(5000);
      System.err.println("Processing...");
      validator = new BacnetValidation(localDevice);
      bacnetSupported = validator.checkIfBacnetSupported();
      if (bacnetSupported) {
        performPicsChecks();
      } else {
        reportAppendix += "Bacnet device not found... Pics check cannot be performed.\n";
        System.out.println(reportAppendix);
        generateReport("");
      }
      connection.doTerminate();
    }
  }

  private void performPicsChecks() {
    try {
      for (RemoteDevice remoteDevice : localDevice.getRemoteDevices()) {
        FileManager fileManager = new FileManager();
        String deviceMacAddress = getMacAddress(remoteDevice);
        bacnetPoints.get(localDevice);
        Multimap<String, Map<String, String>> bacnetPointsMap = bacnetPoints.getBacnetPointsMap();
        boolean csvExists = fileManager.checkCsvForMacAddress(deviceMacAddress);
        if(csvExists) { reportAppendix = fileManager.getFileName(deviceMacAddress) +
                " file not found. Reverting to faux device pics";}
        validatePics(bacnetPointsMap, fileManager);
        generateReport(deviceMacAddress);
      }
    } catch (Exception e) {
      System.out.println("Error performing pics check: " + e.getMessage());
    }
  }

  private void validatePics(
          Multimap<String, Map<String, String>> bacnetPointsMap, FileManager fileManager) {
    String csvSheet = fileManager.getFilePath();
    csv = new Csv(csvSheet);
    csv.readAndValidate(bacnetPointsMap);
  }

  private String getMacAddress(RemoteDevice remoteDevice) {
    return remoteDevice.getAddress().getMacAddress().toString().replaceAll("\\[|\\]", "");
  }

  private void generateReport(String deviceMacAddress) {
    Report report = new Report("tmp/" + deviceMacAddress + "_BacnetPICSTestReport.txt");
    Report appendix = new Report("tmp/" + deviceMacAddress + "_BacnetPICSTest_APPENDIX.txt");
    if (bacnetSupported) {
      boolean reportText = csv.getTestResult();
      String reportAppendix = csv.getTestAppendices();
      if (reportText) {
        report.writeReport(passedTestReport);
      } else {
        report.writeReport(failedTestReport);
      }
      appendix.writeReport(reportAppendix);
    } else {
      report.writeReport(skippedTestReport);
      appendix.writeReport(reportAppendix);
    }
  }
}
