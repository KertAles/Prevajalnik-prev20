JAVA	= java
JAVAC	= javac
RM	= 'rm' -fr

.PHONY	: all
all	:
	$(MAKE) -C prev/phase
	$(JAVAC) -cp .:../lib/antlr-4.8-complete.jar prev/Compiler.java

.PHONY	: clean
clean	:
	$(MAKE) -C prev/phase $@
	find . -name '*.class' -exec $(RM) -fr {} \;

