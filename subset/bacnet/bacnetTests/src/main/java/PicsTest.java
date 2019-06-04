import com.google.common.collect.Multimap;
import com.serotonin.bacnet4j.LocalDevice;
import com.serotonin.bacnet4j.RemoteDevice;
import com.serotonin.bacnet4j.npdu.ip.IpNetwork;
import com.serotonin.bacnet4j.service.unconfirmed.WhoIsRequest;
import com.serotonin.bacnet4j.test.DaqTest.helper.Connection;
import helper.BacnetValidation;
import helper.BacnetPoints;
import helper.Csv;
import helper.Report;

import java.io.File;
import java.util.Map;

public class PicsTest {

  private Connection connection;
  private BacnetValidation validator;
  private BacnetPoints bacnetPoints = new BacnetPoints();
  private String passedTestReport = "RESULT pass protocol.bacnet.version\n";
  private String failedTestReport = "RESULT fail protocol.bacnet.version\n";
  private String reportAppendix = "";
  private Csv csv;

  // temp code.. waiting on daq capability to access auxiliary device conf from Docker container
  private String csvFileFail = "DAQ_PICS - Sheet1.csv";
  private String csvFilePass = "DAQ_PICS - Sheet1(passed).csv";

  private String csvExtension = ".csv";
  private boolean csvFileFound = false;
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
        String deviceMacAddress = getMacAddress(remoteDevice);
        bacnetPoints.get(localDevice);
        Multimap<String, Map<String, String>> bacnetPointsMap = bacnetPoints.getBacnetPointsMap();
        boolean csvExists = checkMacAddressCsv(deviceMacAddress);
        //                if(csvExists) {  // !!! UNCOMMENT THIS LINE ONCE FUNCTIONALITY IMPLEMENTED
        // !!!
        validatePics(bacnetPointsMap, deviceMacAddress);
        //                } // !!! UNCOMMENT THIS LINE ONCE FUNCTIONALITY IMPLEMENTED !!!
        generateReport(deviceMacAddress);
      }
    } catch (Exception e) {
      System.out.println("Error performing pics check: " + e.getMessage());
    }
  }

  private boolean checkMacAddressCsv(String deviceMacAddress) {
    String csvSheet = "csv/" + deviceMacAddress + "_pics" + csvExtension;
    File file = new File(csvSheet);
    if (fileExists(file)) {
      csvFileFound = true;
      return true;
    }
    csvFileFound = false;
    String errorMessage = deviceMacAddress + "_pics" + csvExtension + " file not found.";
    System.out.println(errorMessage);
    reportAppendix = errorMessage;
    return false;
  }

  private void validatePics(
      Multimap<String, Map<String, String>> bacnetPointsMap, String deviceMacAddress) {
    String csvSheet = "csv/" + deviceMacAddress + "_pics" + csvExtension;

    /*
     * Temporary code.. Waiting on daq capability to access auxiliary device conf from Docker container
     * This line has been added just for testing purpose.
     * DELETE THE LINE OF CODE  BELOW ONCE FUNCIONALITY IMPLEMENTED!
     */
    if (!csvFileFound) {
      csvSheet = "csv/" + csvFilePass;
    }

    csvFileFound = true;
    csv = new Csv(csvSheet);
    csv.readAndValidate(bacnetPointsMap);
  }

  private String getMacAddress(RemoteDevice remoteDevice) {
    return remoteDevice.getAddress().getMacAddress().toString().replaceAll("\\[|\\]", "");
  }

  private boolean fileExists(File file) {
    return file.exists();
  }

  private void generateReport(String deviceMacAddress) {
    Report report = new Report("tmp/" + deviceMacAddress + "_BacnetPICSTestReport.txt");
    Report appendix = new Report("tmp/" + deviceMacAddress + "_BacnetPICSTest_APPENDIX.txt");
    //        if(bacnetSupported && csvFileFound) { // !!! UNCOMMENT THIS LINE ONCE FUNCTIONALITY
    // IMPLEMENTED !!!
    if (bacnetSupported) { // !!! DELETE THIS LINE ONCE FUNCTIONALITY IMPLEMENTED !!!
      boolean reportText = csv.getTestResult();
      String reportAppendix = csv.getTestAppendices();
      if (reportText) {
        report.writeReport(passedTestReport);
      } else {
        report.writeReport(failedTestReport);
      }
      appendix.writeReport(reportAppendix);
    } else {
      report.writeReport(failedTestReport);
      appendix.writeReport(reportAppendix);
    }
  }
}
