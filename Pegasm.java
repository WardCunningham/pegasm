import java.io.*;
import java.util.*;

public class Pegasm {

	BufferedReader input;
	Deque <String> parsed = new ArrayDeque();

	void setInput(BufferedReader input) {
		this.input = input;
		parsed.addFirst("");
	}

	// Usage: java Pegasm input-file

    public static void main(String[] args) {
    	Parser parser = null;
    	BufferedReader input = null;

    	try {
	    	input = new BufferedReader(new FileReader(args[0]));
	    } catch (FileNotFoundException e) {
    		error("can't open " + args[0]);
    	}

        parser = new Parser();
        parser.setInput(input);

    	try {
			if (parser.Grammar()) {
				print("match");
			} else {
				print("no match");
			}		
    	} catch (IOException e) {
    		error("can't read " + args[0]);
    	}
    }

    static void error (String message) {
    	System.err.println(message);
    	System.exit(-1);
    }

    static void print (String message) {
    	System.out.println(message);
    }

    // input matching utilities

    void mark(String nonterminal) throws IOException {
    	input.mark(10000);
    	parsed.addFirst(parsed.peekFirst());
    	print("----------------------------------");
    	print(parsed.peekFirst()+" "+nonterminal);
    }

    void reset() throws IOException {
    	input.reset();
    	parsed.removeFirst();
    }

    boolean dot() throws IOException {
    	input.mark(1);
    	int ch = input.read();
    	if (ch >= 0) {
    		parsed.addFirst(parsed.removeFirst()+((char)ch));
    		return true;
    	} else {
    		input.reset();
    		return false;
    	}
    }

    boolean match(String token) throws IOException {
    	input.mark(token.length());
    	for (int i = 0; i < token.length(); i++){
    		char want = token.charAt(i);
    		int have = input.read();
    		if ((int)want != have) {
    			input.reset();
    			return false;
    		}
    	}
    	parsed.addFirst(parsed.removeFirst()+token);
    	return true;
    }
}


// Production rules
// return true if match with input consumed
// return false otherwise with input unchanged

class Parser extends Pegasm {

    // Grammar <- Spacing Definition+ EndOfFile
    boolean Grammar() throws IOException {
    	mark("Grammar");
    	if (Spacing()) {
    		return true;
    	} else {
    		reset();
    		return false;
    	}
    }

	// Spacing <- (Space / Comment)*
    boolean Spacing() throws IOException {
    	mark("Spacing");
    	while (Space() || Comment()) {}
    	return true;
    }

    // Comment <- ’#’ (!EndOfLine .)* EndOfLine
    boolean Comment() throws IOException {
    	mark("Comment");
    	if (match("#") && Comment_Any() && EndOfLine()) {
    		return true;
    	} else {
    		reset();
    		return false;
    	}
    }
    boolean Comment_Any() throws IOException {
    	mark("Comment_Any");
    	while (!EndOfLine() && dot()) {}
    	return true;
    }

    // EndOfLine <- ’\r\n’ / ’\n’ / ’\r’
    boolean EndOfLine() throws IOException {
    	mark("EndOfLine");
    	if (match("\r\n") || match("\n") || match("\r")) {
    		return true;
    	} else {
    		reset();
    		return false;
    	}
    }

    // Space <- ’ ’ / ’\t’ / EndOfLine
    boolean Space() throws IOException {
    	mark("Space");
    	if (match(" ") || match("\t") || EndOfLine()) {
    		return true;
    	} else {
    		reset();
    		return false;
    	}
    }
}

