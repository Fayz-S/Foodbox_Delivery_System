package shield;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MyTestUtils {
  /**
   * Generate Random Number
   * @param min lower bound
   * @param max upper bound
   * @return returns random number (int) between min and max
   */
  public static int generateRandomNumber(int min, int max) {
    Random rand = new Random();
    return rand.nextInt((max - min) + 1) + min;
  }
  /**
   * Generate Valid Postcode
   * @return String, random valid postcode
   */
  public static String generateValidPostcode() {
    return "EH" +
               generateRandomNumber(1, 99) +
               "_" +
               generateRandomNumber(1, 9) +
               (char) (new Random().nextInt(26) + 'A') +
               (char) (new Random().nextInt(26) + 'A');
  }
  /**
   * Generate Invalid Postcodes
   * @return List of String, random Invalid postcodes
   */
  public static List<String> generateInvalidPostcodes() {
    String validPostcode = generateValidPostcode();
    List<String> invalidPostcodes = new ArrayList<>();
    int offset = validPostcode.indexOf('_');
    String postcodeMissingSpace = validPostcode.replace("_", "");
    // Adding postcode with no space
    invalidPostcodes.add(postcodeMissingSpace);
    // Generating postcode with different letters in Area code
    String postcodeWrongArea = validPostcode;
    while (postcodeWrongArea.startsWith("EH")) {
      postcodeWrongArea = (char) (new Random().nextInt(26) + 'A') +
                              (char) (new Random().nextInt(26) + 'A') +
                              validPostcode.substring(2);
    }
    invalidPostcodes.add(postcodeWrongArea);

    // Swapping individually digits for letters and vice versa to test
    //  character check for each character in postcode excluding area
    for (int i = 2; i < postcodeMissingSpace.length(); i++) {
      String invalidChar = swapCharNumber(postcodeMissingSpace, i);
      StringBuilder invalidNoSpace = new StringBuilder(invalidChar);
      String postcodeSwappedOneChar = invalidNoSpace.insert(offset, '_').toString();
//      System.out.println(invalidPostcode);
      invalidPostcodes.add(postcodeSwappedOneChar);
    }
    return invalidPostcodes;
  }
  /**
   * swapCharNumber - swaps char to int or int to char
   * @param str String
   * @param position position of target element in string
   * @return returns string with element swapped in string
   */
  public static String swapCharNumber(String str, int position) {
    String returnStr = str;
    if (Character.isAlphabetic(str.charAt(position))) {
      StringBuilder newStr = new StringBuilder(str);
      newStr.setCharAt(position, String.valueOf(generateRandomNumber(0, 9)).charAt(0));
      returnStr = newStr.toString();

    } else if (Character.isDigit(str.charAt(position))) {
      StringBuilder newStr = new StringBuilder(str);
      char newChar = (char) (new Random().nextInt(26) + 'A');
      newStr.setCharAt(position, newChar);
      returnStr = newStr.toString();
    }

    return returnStr;
  }
  /**
   * formatForValidCHI - places a zero before any single digit number in String
   * @param num String
   * @return returns valid format for CHI
   */
  public static String formatForValidCHI(int num) {
    return (num < 10 && num > -10 ? "0" : "") + num;
  }
  /**
   * generateValidCHI
   * @return returns valid CHI
   */
  public static String generateValidCHI() {
    String day = formatForValidCHI(generateRandomNumber(1, 31));
    String month = formatForValidCHI(generateRandomNumber(1, 12));
    String year = formatForValidCHI(generateRandomNumber(0, 99));
    String remainder = String.valueOf(generateRandomNumber(1000, 9999));
    return day + month + year + remainder;

  }
}