package helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class Csv {

  private PicsValidator picsValidator = new PicsValidator();
  private String csvFile = null;
  private String line = "";
  private String csvSplitBy = ",";
  private Multimap<String, Object> picsMap = ArrayListMultimap.create();
  private String[] csvColumnTitle = {
    "Bacnet_Object_Type", "Bacnet_Object_Property", "Conformance_Code", "Supported"
  };
  private boolean passedTest = false;
  private String appendixText = "";

  public Csv(String csvFile) {
    this.csvFile = csvFile;
  }

  public void readAndValidate(Multimap<String, Map<String, String>> bacnetPointsMap) {
    try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
      while ((line = br.readLine()) != null) {
        String[] value = line.split(csvSplitBy);
        if (!value[0].equals(csvColumnTitle[0])
            && !value[1].equals(csvColumnTitle[1])
            && !value[3].equals(csvColumnTitle[2])
            && !value[4].equals(csvColumnTitle[3])) {
          saveValuesToMap(value);
          validateLine(value[0], value[1], value[3], value[4], bacnetPointsMap);
        }
      }
      setTestResult(picsValidator.getRestult());
    } catch (IOException e) {
      String errorMessage = "Csv error: " + e.getMessage();
      System.out.println(errorMessage);
      setTestResult(false);
      setTestAppendices(errorMessage);
    }
  }

  private void validateLine(
      String bacnetObjectType,
      String bacnetObjectProperty,
      String conformanceCode,
      String supported,
      Multimap bacnetPointsMap) {
    try {
      picsValidator.validate(
          formatValue(bacnetObjectType),
          formatValue(bacnetObjectProperty),
          conformanceCode,
          supported,
          bacnetPointsMap);
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(
          "Error validating property: "
              + e.getMessage()
              + " "
              + bacnetObjectType
              + " "
              + bacnetObjectProperty);
    }
  }

  public boolean getTestResult() {
    return this.passedTest;
  }

  private void setTestResult(boolean result) {
    this.passedTest = result;
  }

  public String getTestAppendices() {
    Multimap<String, String> appendicesMap = picsValidator.getResultMap();

    for (Map.Entry appendix : appendicesMap.entries()) {
      System.out.println(appendix.getValue());
      appendixText += appendix.getValue() + "\n";
    }
    return appendixText;
  }

  private void setTestAppendices(String appendix) {
    this.appendixText = appendix;
  }

  private void saveValuesToMap(String[] values) {
    String bacnetObjectType = formatValue(values[0]);
    String bacnetObjectProperty = values[1];
    String conformanceCode = values[3];
    String supported = values[4];
    Map<String, String[]> bacnetObjectPropertyMap = new HashMap<>();
    String[] properties = {conformanceCode, supported};
    bacnetObjectPropertyMap.put(bacnetObjectProperty, properties);
    picsMap.put(bacnetObjectType, bacnetObjectPropertyMap);
  }

  private String formatValue(String value) {
    if (value.isEmpty() || value.trim().length() == 0) {
      return "";
    }
    String[] bacnetObjectTypes = {
      "Analog_Input, Analog_Output",
      "Analog_Value",
      "Binary_Input",
      "Binary_Output",
      "Binary_Value",
      "Calendar",
      "Device",
      "Event_Enrollment",
      "File",
      "Loop",
      "Multi-state_Input",
      "Multi-state_Value",
      "Program",
      "Notification",
      "Schedule",
      "Trend_Log"
    };

    value = value.replace("Bacnet_", "");
    value = value.replace("Analogue", "Analog");
    value = value.replace("_", " ");

    for (int count = 0; count < bacnetObjectTypes.length; count++) {
      String bacnetObjectType = bacnetObjectTypes[count];
      if (bacnetObjectType.contains(value)) {
        bacnetObjectType = bacnetObjectType.replaceAll("_", " ");
        return bacnetObjectType;
      }
    }
    return value;
  }

  // for debugging
  public Multimap getMap() {
    return picsMap;
  }

  // for debugging
  public void printCsvMap() {
    for (Map.Entry<String, Object> map : picsMap.entries()) {
      String bacnetObjectType = map.getKey();
      Map<String, String[]> bacnetObjectProperties = (Map<String, String[]>) map.getValue();
      System.out.println("\n" + bacnetObjectType.toUpperCase());
      for (Map.Entry<String, String[]> mapValue : bacnetObjectProperties.entrySet()) {
        String propertyName = mapValue.getKey();
        String[] propertyValue = mapValue.getValue();
        System.out.println(
            propertyName + " : [" + propertyValue[0] + ", " + propertyValue[1] + "]");
      }
    }
  }
}
