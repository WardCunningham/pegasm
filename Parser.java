import java.io.*;
import java.util.*;

// Production rules
// return true if match with input consumed
// return false otherwise with input unchanged

public class Parser extends Pegasm {

    // Grammar <- Spacing Definition+ EndOfFile
    boolean Grammar() { int at = pos; return be (Spacing() && Grammar_Many() && EndOfFile(), at); }
    boolean Grammar_Many() { int at = pos; return be (Definition() && Grammar_Any(), at); }
    boolean Grammar_Any() { while (Definition()) {} return true; }

    // Definition <- Identifier LEFTARROW Expression
    boolean Definition() { int at = pos; return be (Identifier() && LEFTARROW() && Expression(), at); }

    // Expression <- Sequence (SLASH Sequence)*
    boolean Expression() { int at = pos; return be (Sequence() && Expression_Any(), at); }
    boolean Expression_Any() { while (SLASH() && Sequence()) {} return true; }

    // Sequence <- Prefix*
    boolean Sequence() { int at = pos; return be (Sequence_Any(), at); }
    boolean Sequence_Any() { while (Prefix()) {} return true; }

    // Prefix <- (AND / NOT)? Suffix
    boolean Prefix() { int at = pos; return be (Prefix_Opt() && Suffix(), at); }
    boolean Prefix_Opt() { if (AND() || NOT()) {}; return true; }

    // Suffix <- Primary (QUESTION / STAR / PLUS)?
    boolean Suffix() { int at = pos; return be (Primary() && Suffix_Opt(), at); }
    boolean Suffix_Opt() { if (QUESTION() || STAR() || PLUS()) {}; return true; }

    // Primary <- Identifier !LEFTARROW / OPEN Expression CLOSE / Literal / Class / DOT
    boolean Primary() {int at = pos; return be (Identifier() && Primary_Not() || OPEN() && Expression() && CLOSE() || Literal() || Klass() || DOT(), at); }
    boolean Primary_Not() { int at = pos; boolean b = !LEFTARROW(); pos = at; return b; }

    // Identifier <- IdentStart IdentCont* Spacing
    boolean Identifier() { int at = pos; return be (IdentStart() && Identifier_Any() && Spacing(), at); }
    boolean Identifier_Any() { while (IdentCont()) {} return true; }

	// IdentStart <- [a-zA-Z_]
	private boolean IdentStart() { int at = pos; return be (range('a','z') || range('A','Z') || match("_"), at); }

	// IdentCont <- IdentStart / [0-9]
	private boolean IdentCont() { int at = pos; return be (IdentStart() || range('0','9'), at); }

    // Literal <- [’] (![’] Char)* [’] Spacing / ["] (!["] Char)* ["] Spacing
    boolean Literal() { int at = pos; return be (chars("\'") && Literal_Any1() && chars("\'") && Spacing() || chars("\"") && Literal_Any2() && chars("\"") && Spacing(), at); }
    boolean Literal_Any1() { while (Literal_Not1() && Char()) {} return true;}
    boolean Literal_Any2() { while (Literal_Not2() && Char()) {} return true;}
    boolean Literal_Not1() { int at = pos; boolean b = !chars("\'"); pos = at; return b; }
    boolean Literal_Not2() { int at = pos; boolean b = !chars("\""); pos = at; return b; }

    // Klass <- ’[’ (!’]’ Range)* ’]’ Spacing
    boolean Klass() { int at = pos; return be (match("[") && Klass_Any() && match("]") && Spacing(), at); }
    boolean Klass_Any() { while (Klass_Not() && Range()) {}; return true; }
    boolean Klass_Not() { int at = pos; boolean b = !match("]"); pos = at; return b; }

    // Range <- Char ’-’ Char / Char
    boolean Range() { int at = pos; return be (Char() && match("-") && Char() || Char(), at);}

    // Char <- ’\\’ [nrt’"\[\]\\] / ’\\’ [0-2][0-7][0-7] / ’\\’ [0-7][0-7]? / !’\\’ .
    boolean Char() { int at = pos; return be ( match("\\") && chars("nrt'\"[]\\") || match("\\") && range('0','2') && range('0','7') && range('0','7') || match("\\") && range('0','7') && range('0','7') || Char_Not() && dot(), at); }
    boolean Char_Not() { int at = pos; boolean b = !match("\\"); pos = at; return b; }

    // LEFTARROW <- ’<-’ Spacing
    boolean LEFTARROW() { int at = pos; return be (match("<-") && Spacing(), at); }

    // SLASH <- ’/’ Spacing
    boolean SLASH() { int at = pos; return be (match("/") && Spacing(), at); }

    // AND <- ’&’ Spacing
    boolean AND() { int at = pos; return be (match("&") && Spacing(), at); }

    // NOT <- ’!’ Spacing
    boolean NOT() { int at = pos; return be (match("!") && Spacing(), at); }

    // QUESTION <- ’?’ Spacing
    boolean QUESTION() { int at = pos; return be (match("?") && Spacing(), at); }

    // STAR <- ’*’ Spacing
    boolean STAR() { int at = pos; return be (match("*") && Spacing(), at); }

    // PLUS <- ’+’ Spacing
    boolean PLUS() { int at = pos; return be (match("+") && Spacing(), at); }

    // OPEN <- ’(’ Spacing
    boolean OPEN() { int at = pos; return be (match("(") && Spacing(), at); }

    // CLOSE <- ’)’ Spacing
    boolean CLOSE() { int at = pos; return be (match(")") && Spacing(), at); }

    // DOT <- ’.’ Spacing
    boolean DOT() { int at = pos; return be (match(".") && Spacing(), at); }

	// Spacing <- (Space / Comment)*
    private boolean Spacing() { while (Space() || Comment()) {} return true; }

    // Comment <- ’#’ (!EndOfLine .)* EndOfLine
    boolean Comment() { int at = pos; return be (match("#") && Comment_Any() && EndOfLine(), at); }
    boolean Comment_Any() { while (Comment_Not() && dot()) {} return true; }
    boolean Comment_Not() { int at = pos; boolean b = !EndOfLine(); pos = at; return b; }

    // Space <- ’ ’ / ’\t’ / EndOfLine
    private boolean Space() { int at = pos; return be (match(" ") || match("\t") || EndOfLine(), at); }

    // EndOfLine <- ’\r\n’ / ’\n’ / ’\r’
    private boolean EndOfLine() { int at = pos; return be (match("\r\n") || match("\n") || match("\r"), at); }

    // EndOfFile <- !.
    boolean EndOfFile() { int at = pos; return be (EndOfFile_Not(), at); }
    boolean EndOfFile_Not() { int at = pos; boolean b = !dot(); pos = at; return b; }
}

