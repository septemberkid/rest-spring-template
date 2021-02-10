package leaf.util;

public class Lvc {
    public static boolean isInteger(String input) {
        return input.matches("\\d+");
    }
    public static boolean isInteger(String input,int minLength,int maxLength) {
        return input.matches("\\d{"+minLength+","+maxLength+"}");
    }
    public static boolean isDate(String input) {
        return input.matches("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))");
    }
    public static boolean isWord(String input) {
        return input.matches("[A-Za-z]+");
    }
    public static boolean isWord(String input,int minLength,int maxLength) {
        return input.matches("[A-Za-z]{"+minLength+","+maxLength+"}");
    }
    public static boolean isWords(String input) {
        return input.matches("[A-Za-z\\s]+");
    }
    public static boolean isWords(String input,int minLength,int maxLength) {
        return input.matches("[A-Za-z\\s]{"+minLength+","+maxLength+"}");
    }

}
