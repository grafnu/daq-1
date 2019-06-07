package helper;

import java.io.File;

public class FileManager {

    private String filePath = "";
    private String csvExtension = ".csv";

    // temp code.. waiting on daq capability to access auxiliary device conf from Docker container
    private String csvFileFail = "faux_device_fail";
    private String csvFilePass = "faux_device_pass";

    public boolean checkCsvForMacAddress(String deviceMacAddress) {
        String csvFolder = getAbsolutePath();
        File[] listFiles = new File(csvFolder + "tmp").listFiles();
        String threeDigitMacAddress = getFirstThreeDigitOfMacAddress(deviceMacAddress);
        for (int i = 0; i < listFiles.length; i++) {
            if (listFiles[i].isFile()) {
                String fileName = listFiles[i].getName();
                if (fileName.startsWith(threeDigitMacAddress)
                        && fileName.endsWith(csvExtension)) {
                    System.out.println("file found: " + fileName + "\n");
                    setFilePath(fileName);
                    return true;
                }
            }
        }
        String errorMessage = getFileName(deviceMacAddress) + " file not found. Reverting to faux device pics.\n";
        System.err.println(errorMessage);
        setFilePath(getFileName(csvFileFail));
        return false;
    }

    private String getFirstThreeDigitOfMacAddress(String deviceMacAddress) {
        String[] macAddress_arr = deviceMacAddress.split(",");
        String threeDigitMacAddress = "";
        for (int count=0; count < macAddress_arr.length; count++){
            if (count > 2){ break; }
            threeDigitMacAddress += macAddress_arr[count] + ",";
        }
        return threeDigitMacAddress;
    }

    public String getFileName(String deviceMacAddress) {
        return deviceMacAddress + "_pics" + csvExtension;
    }

    private void setFilePath(String fileName) {
        String absolute_path = getAbsolutePath();
        this.filePath = absolute_path + "tmp/" + fileName;
    }

    public String getFilePath() {
        return this.filePath;
    }

    private String getAbsolutePath() {
        String absolute_path = "";
        String system_path = System.getProperty("user.dir");
        String[] path_arr = system_path.split("/");
        for (int count = 0; count < path_arr.length; count++) {
            if (path_arr[count].equals("bacnetTests")) {
                break;
            }
            absolute_path += path_arr[count] + "/";
        }
        return absolute_path;
    }
}
