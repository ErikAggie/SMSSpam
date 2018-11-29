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

    public static void main(String[] args) {
        processFile(originalFiles[0], translatedFiles[0]);
        processFile(originalFiles[1], translatedFiles[1]);
    }

    private static void processFile(File source, File destination) {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(source), StandardCharsets.UTF_8));
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(destination), StandardCharsets.UTF_8);
        ) {
            if (destination.exists()) {
                destination.delete();
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
            String oldLine = line;
            for ( String key : htmlMappings.keySet()) {
                line = line.replaceAll(key, htmlMappings.get(key));
            }
        }

        // Get rid fo all 'Â' (unicode 194) characters
        line = line.replaceAll("\u00C2", "");
        // Lots of "&amp;"s in there
        line = line.replaceAll("&amp;", "&");
        return line;
    }
}
