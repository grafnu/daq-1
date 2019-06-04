package helper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.regex.Pattern;

public class PicsValidator {
  String read = "R";
  String write = "W";
  String optional = "O";
  String Supported = "TRUE";
  String formatProperty = "%-35s%-35s%-15s%-25s";

  Multimap<String, String> result = ArrayListMultimap.create();

  boolean testPassed = true;

  public void validate(
      String bacnetObjectType,
      String bacnetObjectProperty,
      String conformanceCode,
      String supported,
      Multimap bacnetPointsMap) {

    Set<String> mapKeySet = bacnetPointsMap.keySet();
    ArrayList<String> keys = getMapKeys(mapKeySet, bacnetObjectType);

    int counter = 0;
    // if bacnetObjectType is not found in bacnet device. Test failed
    if (keys.size() == 0
        && (conformanceCode.contains(read) || conformanceCode.equals(write))
        && !bacnetObjectProperty.equals("Property List")) {
      String appendix =
          String.format(
              formatProperty, bacnetObjectType, bacnetObjectProperty, conformanceCode, "FAILED");
      result.put(bacnetObjectType, appendix);
      testPassed = false;
      counter++;
    } else if (keys.size() == 0
        && conformanceCode.contains(optional)
        && !bacnetObjectProperty.equals("Property List")) {
      String appendix =
          String.format(
              formatProperty,
              bacnetObjectType,
              bacnetObjectProperty,
              conformanceCode,
              "PASSED/WARNING");
      result.put(bacnetObjectType, appendix);
      counter++;
    } else if (keys.size() == 0 && bacnetObjectProperty.equals("Property List")) {
      String appendix =
          String.format(
              formatProperty, bacnetObjectType, bacnetObjectProperty, conformanceCode, "PASSED");
      result.put(bacnetObjectType, appendix);
      counter++;
    }

    for (String key : keys) {
      // if bacnetObjectProperty if not found in bacnet device. Test failed
      String properties = bacnetPointsMap.get(key).toString();
      boolean bacnetObjectPropertyIsFound =
          Pattern.compile(Pattern.quote(bacnetObjectProperty), Pattern.CASE_INSENSITIVE)
              .matcher(properties)
              .find();
      if (!bacnetObjectPropertyIsFound
          && (conformanceCode.contains(read) || conformanceCode.equals(write))
          && supported.equals(Supported)
          && !bacnetObjectProperty.equals("Property List")) {
        String appendix =
            String.format(formatProperty, key, bacnetObjectProperty, conformanceCode, "FAILED");
        result.put(key, appendix);
        testPassed = false;
        counter++;
      } else if (!bacnetObjectPropertyIsFound
          && conformanceCode.contains(optional)
          && supported.equals(Supported)
          && !bacnetObjectProperty.equals("Property List")) {
        String appendix =
            String.format(
                formatProperty, key, bacnetObjectProperty, conformanceCode, "PASSED/WARNING");
        result.put(key, appendix);
        counter++;
      } else if (bacnetObjectPropertyIsFound
          && supported.equals(Supported)
          && !bacnetObjectProperty.equals("Property List")) {
        String appendix =
            String.format(formatProperty, key, bacnetObjectProperty, conformanceCode, "PASSED");
        result.put(key, appendix);
        counter++;
      } else if (bacnetObjectProperty.equals("Property List")) {
        String appendix =
            String.format(formatProperty, key, bacnetObjectProperty, conformanceCode, "PASSED");
        result.put(key, appendix);
        counter++;
      } else {
        String appendix =
            String.format(formatProperty, key, bacnetObjectProperty, conformanceCode, "FAILED");
        result.put(key, appendix);
        testPassed = false;
        counter++;
      }
    }

    if (counter == 0) {
      System.out.println(
          "for loop skipped -> "
              + bacnetObjectType
              + " - "
              + bacnetObjectProperty
              + " /// "
              + keys.size());
    }
  }

  private ArrayList<String> getMapKeys(Set<String> mapKeySet, String bacnetObjectType) {
    ArrayList<String> keys = new ArrayList<>();
    for (String key : mapKeySet) {
      if (key.contains(bacnetObjectType)) {
        keys.add(key);
      }
    }
    return keys;
  }

  public Multimap<String, String> getResultMap() {
    return result;
  }

  public boolean getRestult() {
    return this.testPassed;
  }
}
