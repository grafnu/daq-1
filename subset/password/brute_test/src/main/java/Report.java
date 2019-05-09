import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Report {
  String report = "Brute Test *";
  String reportFilename = "reports/report.txt";

  public void addText(String text) {
    report += text;
  }

  public void printReport() {
    System.out.println(report);
  }

  public void writeReport(String protocol) {
    switch (protocol) {
      case "telnet":
        reportFilename = "reports/telnetReport.txt";
        break;
      case "ssh":
        reportFilename = "reports/sshReport.txt";
        break;
    }
    try {
      String[] directory = reportFilename.split("/");
      String[] lines = report.split("\\*");

      File dir = new File(directory[directory.length - 2]);
      if (!dir.exists()) dir.mkdirs();
      BufferedWriter writer = new BufferedWriter(new FileWriter(reportFilename));
      for (String tmpLine : lines) {
        writer.write(tmpLine);
        writer.newLine();
      }
      writer.close();
    } catch (IOException e) {
      System.err.println("could not write report");
      System.out.println(e);
    }
  }
}