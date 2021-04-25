package shield;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;



public class MyImpUtils {

    /**
     * Checks validity of postcode
     * @param postcode String
     * @return true if postcode passes all checks, else returns false
     */
    public static boolean checkValidPostcode (String postcode) {
        if (postcode.length() == 7) {
            return postcode.toUpperCase(Locale.ROOT).startsWith("EH") &&
                    Character.isDigit(postcode.charAt(2)) &&
                    postcode.charAt(3) == '_' &&
                    Character.isDigit(postcode.charAt(4)) &&
                    Character.isAlphabetic(postcode.charAt(5)) &&
                    Character.isAlphabetic(postcode.charAt(6));

        } else if (postcode.length() == 8) {
            return postcode.toUpperCase(Locale.ROOT).startsWith("EH") &&
                    postcode.substring(2, 4).matches("[0-9]+") &&
                    postcode.charAt(4) == '_' &&
                    Character.isDigit(postcode.charAt(5)) &&
                    Character.isAlphabetic(postcode.charAt(6)) &&
                    Character.isAlphabetic(postcode.charAt(7));
        } else return false;
    }
}
