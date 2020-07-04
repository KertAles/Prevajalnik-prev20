lexer grammar PrevLexer;

@header {
	package prev.phase.lexan;
	import prev.common.report.*;
}

@members {
    @Override
	public LexAn.PrevToken nextToken() {
		return (LexAn.PrevToken) super.nextToken();
	}
}

VOIDCONST: ('none');
INTCONST: ([1-9][0-9]* | '0');
FLOATCONST: (('1'..'9')('0'..'9')* | '0')('.')('0'..'9')*;
BOOLCONST: ('true' | 'false');
CHARCONST: ('\'')((' '..'&') | ('('..'~') | ('\\\''))('\'');
STRINGCONST: ('"')((' '..'!') | ('#'..'~') | ('\\"'))+('"');
POINTERCONST: ('nil');
  
TYPE_KEY: ('boolean' | 'char' | 'integer' | 'void');

TYP: ('typ');
VAR: ('var');
FUN: ('fun');

WHILE: ('while');
DO: ('do');

IF: ('if');
THEN: ('then');
ELSE: ('else');         

NEW : ('new');
DEL:  ('del');

EXCL: ('!');
STREHA: ('^');
PLUSMIN: ('+' | '-');
MULSYM: ('*' | '/' | '%' );
RELSYM: ('==' | '!=' | '<' | '>' | '<=' | '>=' );
CONSYM: ('&');
DISSYM: ('|');

WHERE: ('where');

EQUALS: ('=');
DOT: ('.');
COMMA: (',');
COLON: (':');
SEMICOL: (';');

LBRACK: ('{');
RBRACK: ('}');
LBRACE: ('(');
RBRACE: (')');
LSQBRA: ('[');
RSQBRA: (']');

ID: (('A'..'Z' | 'a'..'z' | '_')('A'..'Z' | 'a'..'z' | '_' | '0'..'9')*);  

COMMENT: ('#')(' '..'~')*(('\n') | '\r') -> skip;
TAB: '\t' {	setCharPositionInLine((getCharPositionInLine() / 8 + 1) * 8); } -> skip;
WHITESPACE: (' '|'\n'|'\r') -> skip;


