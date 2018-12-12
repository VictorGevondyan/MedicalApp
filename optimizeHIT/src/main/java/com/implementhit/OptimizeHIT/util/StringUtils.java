package com.implementhit.OptimizeHIT.util;

public class StringUtils {
	public static String capitalizeWords(String stringToCapitalize) {
		String[] words = stringToCapitalize.split(" ");
		StringBuilder sb = new StringBuilder();
		if (words[0].length() > 0) {
		    sb.append(Character.toUpperCase(words[0].charAt(0)) + words[0].subSequence(1, words[0].length()).toString());
		    for (int i = 1; i < words.length; i++) {
		        sb.append(" ");
		        sb.append(Character.toUpperCase(words[i].charAt(0)) + words[i].subSequence(1, words[i].length()).toString());
		    }
		}
		
		return sb.toString();
	}

	public static String userFriendlyICD(String code) {
		if (code.length() > 3) {
			return code.substring(0, 3) + "." + code.substring(3, code.length());
		}

		return code;
	}
}
