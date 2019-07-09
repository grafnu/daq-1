import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Report {
    String report = "security.passwords Test\n";
    File reportFile;

    public void addText(String text) {
        report += text + '\n';
    }

    public void printReport() {
        System.out.println(report);
    }

    public void writeReport(String protocol) {
        switch (protocol) {
            case "telnet":
                reportFile = new File("reports/telnetReport.txt");
                break;
            case "ssh":
                reportFile = new File("reports/sshReport.txt");
                break;
        }
        try {
            reportFile.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportFile))) {
                writer.write(report);
            }
        } catch (IOException e) {
            System.err.println("could not write report");
            System.out.println(e);
        }
    }
}
