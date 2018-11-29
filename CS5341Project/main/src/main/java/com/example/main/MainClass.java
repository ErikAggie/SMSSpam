package com.example.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainClass {

    private static final String sourceDirectory = "C:\\Users\\erika\\OneDrive\\School\\Patterns\\Project\\";
    private static final String destDirectory = "C:\\Users\\erika\\SMSSpamRepo\\";

    private static final File[] originalFiles =
            {  new File(sourceDirectory + "Original Training Data.txt"),
               new File(sourceDirectory + "Original Test Data.txt")};

    private static final File[] translatedFiles =
            {  new File(destDirectory + "Training Data.txt"),
                    new File(destDirectory + "Test Data.txt")};

    private static final Pattern htmlCharacterPattern = Pattern.compile("(&.*?;)");

    private static final Map<String, String> htmlMappings = new HashMap<String, String>() {{
        put("&lt;", "<");
        put("&gt;", ">");
        put("&amp;", "&");
    }};

    private static final Map<String, String> otherReplacements = new HashMap<String, String>() {{
        // Unicode character 194 ('Â') shows up a lot with other (true) characters
        put("\u00C2", "");
        // Odd set of characters that ought to be a "'"
        put("\u00E2\u20AC\u02DC", "'");
        // Two characters that (I think) should be a pound symbol
        put("\u00C3\u00BA", "\u00A3");
        // A character combination for 1/4
        put("\u00C3\u00BC", "\u00BC");
        // A combo I think ought to be 'U'
        put("\u00C3\u0153", "U");
        // A combo I think ought to be '!'
        put("\u00e2\u20ac\u00a6", "!");
        // Something I don't think should be there: É/é followed by a space
        put("\u00c9", "");
        put("\u00e9 ", "");
        // No idea what this was supposed to be (!鈥┾??〨)
        put("!\u9225\u253e\\?\\?\u3028", "");
        // Not sure what this is, either: Ã¨n
        put("\u00c3\u00a8n", "");
        // Another unsure one: â€œ
        put("\u00e2\u20ac\u0153", "");
        // See above: â€“
        put("\u00e2\u20ac\u201c ", "");


        // Different "U/u" that I think is misinterpreted
        put("\u00dc", "U");
        put("\u00fc", "u");
        // Ought to be 'i'
        put("\u00ec", "i");
        // Ought to be '
        put("\u0091", "\'");
        put("\u0092", "\'");
        // Don't know about thes...
        put("\u0093", "");
        put("\u0096 ", "");
        // Guessing space...
        put("\u0094", " ");
    }};

    // Characters that are fine
    private static final Map<Integer, Object> charactersToIgnore = new TreeMap<Integer, Object>() {{
        put(0xa3, null);
        put(0xbc, null);
        put(0x2019, null);
        put(0x201d, null);
        put(0x2018, null);
        put(0x2026, null);
    }};

    public static void main(String[] args) {
        processFile(originalFiles[0], translatedFiles[0]);
        processFile(originalFiles[1], translatedFiles[1]);
    }

    private static void processFile(File source, File destination) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(source), StandardCharsets.UTF_8));
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(destination), StandardCharsets.UTF_8)
        ) {
            if (destination.exists()) {
                if ( !destination.delete()) {
                    System.out.println("Error returned when deleting existing version of " + destination.getAbsolutePath() + "; creating the file may error out.");
                }
            }

            writer.write("Spam/Ham	SMS Text\n");

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(processLine(line) + "\n");
            }
        } catch ( IOException e) {
            System.err.println("Error converting files: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }

    private static String processLine(String line) {
        // Fix line start

        // One line starts with a junk character...
        if ( !line.startsWith("ham\t") && !line.startsWith("spam\t")) {
            line = line.replaceAll("\uFEFF", "");
        }

        // Replace starting space with a tab (again, only one line, I believe)
        if ( line.startsWith("ham ")) {
            line = line.replaceFirst("ham ", "ham\t");
        }
        if ( line.startsWith("spam ")) {
            line = line.replaceFirst("spam ", "spam\t");
        }
        if ( !line.startsWith("ham\t") && !line.startsWith("spam\t")) {
            System.err.println("Failed to fix " + line);
        }

        // Take care of html codes. There aren't that many, so we'll be a bit brute-force
        Matcher htmlCharacterMatcher = htmlCharacterPattern.matcher(line);
        if (htmlCharacterMatcher.find()) {
            for ( String key : htmlMappings.keySet()) {
                line = line.replaceAll(key, htmlMappings.get(key));
            }
        }

        // Other replacements. Again we'll do it the brute-force way
        for ( String key : otherReplacements.keySet()) {
            line = line.replaceAll(key, otherReplacements.get(key));
        }

        for ( char character : line.toCharArray()) {
            int value = (int) character;
            if ( value > 150 && !charactersToIgnore.containsKey(value)) {
                System.out.println(character + " (" + Integer.toHexString(value) + ") in " + line);
            }
        }

        return line;
    }
}
