JAVA		= java
ANTLR		= org.antlr.v4.Tool
ANTLRDIR	= ../../../lib/antlr-4.8-complete.jar
RM		= 'rm' -fr

parser	: synan/PrevParser.g4 lexan/PrevLexer.g4
	@# Lexer and parser must be constructed simultaneously.
	$(JAVA) -cp $(ANTLRDIR):$(CLASSPATH) $(ANTLR) -no-listener -no-visitor -lib lexan $^

lexer	: lexan/PrevLexer.g4
	@# Construction of lexer only.
	$(JAVA) -cp $(ANTLRDIR):$(CLASSPATH) $(ANTLR) $<

.PHONY	: clean
clean	:
	$(RM) *~
	$(RM) lexan/Prev*.java lexan/*interp lexan/*tokens
	$(RM) synan/Prev*.java synan/*interp synan/*tokens
