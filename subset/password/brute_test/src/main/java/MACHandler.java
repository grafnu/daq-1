import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MACHandler {
	
	public static String runShellCommand(String command) throws IOException {
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = input.readLine()) != null) {
            if (!line.trim().equals("")) {
                line = line.substring(1);
                String macAddress = getMACAddressFromCommand(line);
                if (macAddress.isEmpty() == false) {
                    return macAddress;
                }
            }
         }
        return null;
    }

    public static String getMACAddressFromCommand(String macLine) {
        String lineContents[] = macLine.split("   ");
        for (String string : lineContents) {
            if (string.trim().length() == 17) {
                return string.trim().toUpperCase();
            }
        }
        return "";
    }
}
