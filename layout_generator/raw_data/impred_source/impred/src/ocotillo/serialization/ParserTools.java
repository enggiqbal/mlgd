/**
 * Copyright © 2014-2016 Paolo Simonetto
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ocotillo.serialization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ocotillo.graph.Rules;

public class ParserTools {

    static public final String SPLIT_KEEP_DELIMITERS = "((?<=%1$s)|(?=%1$s))";

    /**
     * Reads all the lines in a text file.
     *
     * @param file the file.
     * @return the text lines.
     */
    public static List<String> readFileLines(File file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("The file " + file.getName() + "is not readable.");
        }
        return lines;
    }

    /**
     * Write the lines of a text file.
     *
     * @param lines the text lines.
     * @param file the file.
     */
    public static void writeFileLines(List<String> lines, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException ex) {
            System.err.println("The file " + file.getName() + "is not writable.");
        }
    }

    /**
     * Parses a string with escaped sequences. The escaped characters or
     * sequences must be provided as a regular expression. Even when multiple
     * sequences are provided (such as initial and final sequence), no checks
     * are performed to ensure that they are provided with the right syntax.
     */
    public static class EscapedString {

        /**
         * The original string.
         */
        public final String original;
        /**
         * The string where the escaped substrings have been substituted.
         */
        public final String withSubstitutions;
        /**
         * The character used as placeholder in the substitutions.
         */
        public final String substitutionChar;
        /**
         * The escaped strings that have been extracted.
         */
        public final List<String> extractedStrings;

        /**
         * Construct an escaped string parser.
         *
         * @param orginalString the string to be analysed.
         * @param escapeSequences the escape character or sequences.
         */
        public EscapedString(String orginalString, String escapeSequences) {
            original = orginalString;
            substitutionChar = findUnusedChar(original);
            extractedStrings = new ArrayList<>();

            String[] tokens = original.split(escapeSequences, -1);
            assert (tokens.length % 2 == 1) : "The String <" + original + "> have an uncorrect number of escape delimiters";

            String withSubst = "";
            for (int i = 0; i < tokens.length; i++) {
                if (i % 2 == 0) {
                    withSubst += tokens[i];
                } else {
                    withSubst += substitutionChar + extractedStrings.size() + substitutionChar;
                    extractedStrings.add(tokens[i]);
                }
            }
            withSubstitutions = withSubst;
        }

        /**
         * Returns a character which is not reserved nor used in the current
         * string.
         *
         * @param String the current string.
         * @return an unused, unreserved character.
         */
        private String findUnusedChar(String String) {
            char unusedChar = '§';
            while (String.contains(Character.toString(unusedChar))
                    || Rules.containsReservedCharacters(Character.toString(unusedChar))) {
                unusedChar++;
            }
            return Character.toString(unusedChar);
        }

        /**
         * Reverts the substitutions performed in a string or part of it.
         *
         * @param substitutedString the substituted string, or part of it.
         * @return the input string with reverted substitutions.
         */
        public String revertSubst(String substitutedString) {
            return revertSubst(substitutedString, "", "");
        }

        /**
         * Reverts the substitutions performed in a string or part of it.
         *
         * @param substitutedString the substituted string, or part of it.
         * @param uniqueDelim the delimited to be put before and after the
         * escaped string.
         * @return the input string with reverted substitutions.
         */
        public String revertSubst(String substitutedString, String uniqueDelim) {
            return revertSubst(substitutedString, uniqueDelim, uniqueDelim);
        }

        /**
         * Reverts the substitutions performed in a string or part of it.
         *
         * @param substitutedString the substituted string, or part of it.
         * @param initialDelim the delimited to be put before the escaped
         * string.
         * @param finalDelim the delimited to be put after the escaped string.
         * @return the input string with reverted substitutions.
         */
        public String revertSubst(String substitutedString, String initialDelim, String finalDelim) {
            String[] tokens = substitutedString.split(substitutionChar, -1);
            assert (tokens.length % 2 == 1) : "The String <" + substitutedString + "> have an uncorrect number of escape delimiters";

            String revertedString = "";
            for (int i = 0; i < tokens.length; i++) {
                if (i % 2 == 0) {
                    revertedString += tokens[i];
                } else {
                    int index = Integer.parseInt(tokens[i]);
                    revertedString += initialDelim + extractedStrings.get(index) + finalDelim;
                }
            }
            return revertedString;
        }
    }
}
