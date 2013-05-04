import java.io.*;
import java.util.*;

public class Pegasm {

	char[] input = new char[1000000];
	int pos = 0; // next char
	int end = 0; // pos of eof

	void load(String file) {
		try {
			Reader reader = new FileReader(file);
			end = reader.read(input, 0, 1000000);
		} catch (Exception e) {
			error("can't load file: " + e);
		}
	}

	// Usage: java Pegasm input-file
	// Build: ~/Library/Application\ Support/Sublime\ Text\ 2/Packages/User/pegasm.sublime-build

    public static void main(String[] args) {

        Parser parser = new Parser();
        parser.load(args[0]);

		if (parser.Grammar()) {
			print("done, match");
		} else {
			print("done, no match");
		}
    }

    static void error (String message) {
    	System.err.println(message);
    	System.exit(-1);
    }

    static void print (String message) {
    	System.out.println(message);
    }

	// parse tracing

    void mark(String nonterminal) {
    	// print("----------------------------------");
    	print(matched()+" {{"+nonterminal+"-"+pos+"}} "+matching());
    }

    int back() {
    	for (int i=0; i<100; i++) {
    		if (i>=pos || input[pos-i-1] == '\n') {
    			return i;
    		}
    	}
    	return 100;
    }

    int ahead() {
    	for (int i=0; i<100; i++) {
    		if (pos+i >= end || input[pos+i] == '\n') {
    			return i;
    		}
    	}
    	return 100;
    }

    String matched() {
    	return new String(input, pos-back(), back());
    }

    String matching() {
    	return new String(input, pos, ahead());
    }

    // input matchers

    boolean dot() {
    	if (pos<end) {
    		pos++;
    		return true;
    	} else {
    		return false;
    	}
    }

    boolean match(String token) {
    	int at = pos;
    	if (end-pos < token.length()) {
    		return false;
    	}
    	for (int i = 0; i < token.length(); i++){
    		char want = token.charAt(i);
    		char have = input[pos++];
    		if (want != have) {
    			pos = at;
    			return false;
    		}
    	}
    	return true;
    }

    boolean range(char from, char to) {
    	int at = pos;
    	if (pos == end) {
    		return false;
    	}
    	char have = input[pos++];
    	if (from <= have && to >= have) {
    		return true;
    	} else {
    		pos = at;
    		return false;
    	}
    }
}