package dgroomes.foreign_memory;

import java.nio.charset.StandardCharsets;

public class StringUtil {


    /**
     * Gets up to the first N bytes worth of characters from a string.
     *
     * @param str The input string
     * @param n   The maximum number of bytes to fit
     * @return The string formed by the first N bytes worth of characters, filled with ' ' to reach N bytes.
     */
    public static String getLimitedByteString(String str, int n) {
        assert str != null;
        assert !str.isEmpty();
        assert n > 0;

        StringBuilder resultBuilder = new StringBuilder();
        int bytesCount = 0;

        for (int i = 0; i < str.length(); i++) {
            String character = str.substring(i, i + 1);
            int charByteSize = character.getBytes(StandardCharsets.UTF_8).length;

            if (bytesCount + charByteSize <= n) {
                resultBuilder.append(character);
                bytesCount += charByteSize;
            } else {
                break;
            }
        }

        // Filling the remaining bytes with '.'
        while (bytesCount < n) {
            resultBuilder.append(' ');
            bytesCount++;
        }

        return resultBuilder.toString();
    }

    public static void main(String[] args) {
        // Testing the 'getLimitedByteString' method.

        // Strings that are exactly the byte size limit
        {
            var given = "abc";
            var found = getLimitedByteString(given, 3);
            var expected = "abc";
            assert expected.equals(found) : "Expected '%s' but found '%s'".formatted(expected, found);
        }

        // Strings that are less than the byte size limit
        {
            var given = "abc";
            var found = getLimitedByteString(given, 4);
            var expected = "abc ";
            assert expected.equals(found) : "Expected '%s' but found '%s'".formatted(expected, found);
        }

        // Strings that are greater than the byte size limit
        {
            var given = "abc";
            var found = getLimitedByteString(given, 2);
            var expected = "ab";
            assert expected.equals(found) : "Expected '%s' but found '%s'".formatted(expected, found);
        }

        // Strings with a unicode character that spans the byte size limit
        {
            var given = "a€c"; // Note: the '€' character is 3 bytes in UTF-8
            var found = getLimitedByteString(given, 2);
            var expected = "a ";
            assert expected.equals(found) : "Expected '%s' but found '%s'".formatted(expected, found);
        }
    }
}
