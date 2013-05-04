import java.io.*;
import java.util.*;

// Production rules
// return true if match with input consumed
// return false otherwise with input unchanged

class Parser extends Pegasm {

    // Grammar <- Spacing Definition+ EndOfFile
    boolean Grammar() {
    	int at = pos;
        mark("Grammar");
    	if (Spacing() && Many_Definition() && EndOfFile()) {
    		return true;
    	} else {
    		pos = at;
    		return false;
    	}
    }
    // Definition+
    boolean Many_Definition() {
    	int at = pos;
        mark("Definition+");
    	if (Definition()) {
    		while (Definition()) {}
    		return true;
    	} else {
    		pos = at;
    		return false;
    	}
    }

    // Definition <- Identifier LEFTARROW Expression
    boolean Definition() {
    	int at = pos;
        mark("Definition");
    	if (Identifier() && LEFTARROW() && Expression()) {
    		return true;
    	} else {
    		pos = at;
    		return false;
    	}
    }

    // Expression <- Sequence (SLASH Sequence)*
    boolean Expression() {
    	int at = pos;
        mark ("Expression");
    	if (false) {
    		return true;
    	} else {
    		pos = at;
    		return false;
    	}
    }

    // Identifier <- IdentStart IdentCont* Spacing
    boolean Identifier() {
    	int at = pos;
        mark("Identifier");
    	if (IdentStart() && IdentCont_Any() && Spacing()) {
			return true;
    	} else {
    		pos = at;
    		return false;
    	}
    }
    boolean IdentCont_Any() {
    	while (IdentCont()) {}
    	return true;
    }

	// IdentStart <- [a-zA-Z_]
	boolean IdentStart() {
		int at = pos;
        mark("IdentStart");
		if (range('a','z') || range('A','Z') || match("_")) {
			return true;
		} else {
			pos = at;
			return false;
		}
	}

	// IdentCont <- IdentStart / [0-9]
	boolean IdentCont() {
		int at = pos;
        mark("IdentCont");
		if (IdentStart() || range('0','9')) {
			return true;
		} else {
			pos = at;
			return false;
		}
	}

    // LEFTARROW <- ’<-’ Spacing
    boolean LEFTARROW() {
        int at = pos;
        mark("LEFTARROW");
    	if (match("<-") && Spacing()) {
    		return true;
    	} else {
    		pos = at;
    		return false;
    	}
    }

	// Spacing <- (Space / Comment)*
    boolean Spacing() {
        mark("Spacing");
    	while (Space() || Comment()) {}
    	return true;
    }

    // Comment <- ’#’ (!EndOfLine .)* EndOfLine
    boolean Comment() {
    	int at = pos;
        mark("Comment");
    	if (match("#") && Comment_Any() && EndOfLine()) {
    		return true;
    	} else {
    		pos = at;
    		return false;
    	}
    }
    // (!EndOfLine .)*
    boolean Comment_Any() {
    	while (Not_EndOfLine() && dot()) {}
    	return true;
    }

    boolean Not_EndOfLine() {
        int at = pos;
        if (!EndOfLine()) {
            pos = at;
            return true;
        } else {
            pos = at;
            return false;
        }
    }

    // Space <- ’ ’ / ’\t’ / EndOfLine
    boolean Space() {
    	int at = pos;
        mark("Space");
    	if (match(" ") || match("\t") || EndOfLine()) {
    		return true;
    	} else {
    		pos = at;
    		return false;
    	}
    }

    // EndOfLine <- ’\r\n’ / ’\n’ / ’\r’
    boolean EndOfLine() {
    	int at = pos;
        mark("EndOfLine");
    	if (match("\r\n") || match("\n") || match("\r")) {
    		return true;
    	} else {
    		pos = at;
    		return false;
    	}
    }

    // EndOfFile <- !.
    boolean EndOfFile() {
    	int at = pos;
        mark("EndOfFile");
    	if (!dot()) {
    		return true;
    	} else {
    		pos = at;
    		return false;
    	}
    }
}

