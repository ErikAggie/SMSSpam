package com.example.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainClass {

    private static final String directory = "C:\\Users\\erika\\OneDrive\\School\\Patterns\\Project\\";

    private static final File[] originalFiles =
            {  new File(directory + "Original Training Data.txt"),
               new File(directory + "Original Test Data.txt")};

    private static final File[] translatedFiles =
            {  new File(directory + "Training Data.txt"),
                    new File(directory + "Test Data.txt")};

    private static final Pattern startJunk = Pattern.compile("^.*(spam|ham)(\\t.*)");
    private static final Pattern endQuotePattern = Pattern.compile("(.*)\"$");


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
        if ( !line.startsWith("ham\t") && !line.startsWith("spam\t")) {
            // One line starts with a junk character...
            line = line.replaceAll("\uFEFF", "");
        }
        if ( line.startsWith("ham ")) {
            line = line.replaceFirst("ham ", "ham\t");
        }
        if ( line.startsWith("spam ")) {
            line = line.replaceFirst("spam ", "spam\t");
        }
        if ( !line.startsWith("ham\t") && !line.startsWith("spam\t")) {
            System.err.println("Failed to fix " + line);
        }
        // Replace all 'Â' (unicode 194) characters
        line = line.replaceAll("\u00C2", "");
        // Lots of "&amp;"s in there
        line = line.replaceAll("&amp;", "&");

        /*Matcher startQuoteMatcher = startQuotePattern.matcher(line);
        if ( startQuoteMatcher.matches()) {
            line = startQuoteMatcher.group(1) + "\t" + startQuoteMatcher.group(2);
            //System.out.println("Corrected (1) " + line);
        }

        Matcher endQuoteMatcher = endQuotePattern.matcher(line);
        if ( endQuoteMatcher.matches()) {
            line = endQuoteMatcher.group(1);
            //System.out.println("Corrected (2) " + line);
        }*/
        return line;
    }
}
