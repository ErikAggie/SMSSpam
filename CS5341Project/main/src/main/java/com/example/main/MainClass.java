package com.example.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainClass {

    private static final String directory = "C:\\Users\\erika\\OneDrive\\School\\Patterns\\Project\\";

    private static final File[] originalFiles =
            {  new File(directory + "Original Training Data.txt"),
               new File(directory + "Original Test Data.txt")};

    private static final File[] translatedFiles =
            {  new File(directory + "Updated Training Data.txt"),
                    new File(directory + "Updated Test Data.txt")};

    private static final Pattern startQuotePattern = Pattern.compile("^(spam|ham)\\t\"(.*)");
    private static final Pattern endQuotePattern = Pattern.compile("(.*)\"$");


    public static void main(String[] args) {

        processFile(originalFiles[0], translatedFiles[0]);
        processFile(originalFiles[1], translatedFiles[1]);
    }

    private static void processFile(File source, File destination) {
        try (
                BufferedReader reader = new BufferedReader(new FileReader(source));
                BufferedWriter writer = new BufferedWriter(new FileWriter(destination));
        ) {
            if (destination.exists()) {
                destination.delete();
            }


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
        // Replace all 'Â' 194 characters
        line = line.replaceAll("\u00C2", "");

        Matcher startQuoteMatcher = startQuotePattern.matcher(line);
        if ( startQuoteMatcher.matches()) {
            line = startQuoteMatcher.group(1) + "\t" + startQuoteMatcher.group(2);
            //System.out.println("Corrected (1) " + line);
        }

        Matcher endQuoteMatcher = endQuotePattern.matcher(line);
        if ( endQuoteMatcher.matches()) {
            line = endQuoteMatcher.group(1);
            //System.out.println("Corrected (2) " + line);
        }
        return line;
    }
}
