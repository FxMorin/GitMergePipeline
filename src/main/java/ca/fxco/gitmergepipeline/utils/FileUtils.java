package ca.fxco.gitmergepipeline.utils;

import java.util.regex.Pattern;

public class FileUtils {

    /**
     * Converts a glob pattern to a regular expression pattern.
     *
     * @param glob The glob pattern to convert
     * @return The equivalent regular expression pattern
     */
    public static String convertGlobToRegex(String glob) {
        StringBuilder regex = new StringBuilder("^");
        boolean escaping = false;

        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);

            if (escaping) {
                // If we're escaping, just add the character
                regex.append(Pattern.quote(String.valueOf(c)));
                escaping = false;
            } else {
                switch (c) {
                    case '\\':
                        escaping = true;
                        break;
                    case '*':
                        if (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
                            // ** matches any number of directories
                            regex.append(".*");
                            i++; // Skip the next *
                        } else {
                            // * matches any number of characters except /
                            regex.append("[^/]*");
                        }
                        break;
                    case '?':
                        // ? matches a single character except /
                        regex.append("[^/]");
                        break;
                    case '.', '(', ')', '+', '|', '^', '$', '@', '%':
                        // Escape special regex characters
                        regex.append("\\").append(c);
                        break;
                    case '{':
                        // {a,b,c} matches any of a, b, or c
                        regex.append("(?:");
                        break;
                    case '}':
                        regex.append(")");
                        break;
                    case ',':
                        regex.append("|");
                        break;
                    case '[':
                        // Character class
                        regex.append("[");
                        if (i + 1 < glob.length() && glob.charAt(i + 1) == '!') {
                            regex.append("^");
                            i++;
                        } else if (i + 1 < glob.length() && glob.charAt(i + 1) == '^') {
                            regex.append("\\^");
                            i++;
                        }
                        break;
                    default:
                        regex.append(c);
                }
            }
        }

        regex.append("$");
        return regex.toString();
    }
}
