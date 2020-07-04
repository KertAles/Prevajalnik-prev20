package prev.phase.lexan;

import java.io.*;
import java.util.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;

import prev.common.logger.*;
import prev.common.report.*;
import prev.phase.*;

/**
 * Lexical analysis phase.
 */
public class LexAn extends Phase {

	/** The ANTLR lexer that actually performs lexical analysis. */
	public final PrevLexer lexer;

	/**
	 * Phase construction: sets up logging and the ANTLR lexer.
	 */
	public LexAn() {
		super("lexan");

		String path = System.getProperty("user.dir");
		path = path.substring(0, path.length() - 3);
		path = path + "src/prev/phase/lexan/";
		
		String srcFileName = prev.Compiler.cmdLineArgValue("--src-file-name");
		String basicFileName = path + "basicFunctions.p20";
		String tempFileName = path + "temp.p20";
		
		
		try {
            File tempFile = new File(tempFileName);
            tempFile.createNewFile();
            
            OutputStream out = new FileOutputStream(tempFileName);
            byte[] buf = new byte[100];
            
            Vector<String> files = new Vector<String>();
            files.add(basicFileName);
            files.add(srcFileName);
            
            for (String file : files) {
                InputStream in = new FileInputStream(file);
                int b = 0;
                while ( (b = in.read(buf)) >= 0)
                    out.write(buf, 0, b);
                in.close();
            }
            
            out.close();
                
        } 
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } 
		
		try {
			lexer = new PrevLexer(CharStreams.fromFileName(tempFileName));
			lexer.setTokenFactory(new PrevTokenFactory());
		} catch (IOException __) {
			throw new Report.Error("Cannot open file '" + srcFileName + "'.");
		}
		
		
        File file = new File(tempFileName); 
        
        file.delete();
	}

	/**
	 * A customized token that is 'locatable' and 'loggable'.
	 */
	@SuppressWarnings("serial")
	public class PrevToken extends CommonToken implements Locatable, Loggable {

		/** The location of this token. */
		private final Location location;

		public PrevToken(int type, String text) {
			super(type, text);
			setLine(0);
			setCharPositionInLine(0);
			location = new Location(getLine(), getCharPositionInLine(), getLine(),
					getCharPositionInLine() + getText().length() - 1);
		}

		public PrevToken(Pair<TokenSource, CharStream> source, int type, int channel, int start, int stop) {
			super(source, type, channel, start, stop);
			setCharPositionInLine(getCharPositionInLine() - getText().length() + 1);
			location = new Location(getLine(), getCharPositionInLine(), getLine(),
					getCharPositionInLine() + getText().length() - 1);
		}

		@Override
		public Location location() {
			return location;
		}

		@Override
		public void log(Logger logger) {
			if (logger == null)
				return;
			logger.begElement("term");
			if (getType() == -1) {
				logger.addAttribute("token", "EOF");
				logger.addAttribute("lexeme", "");
			} else {
				logger.addAttribute("token", PrevLexer.ruleNames[getType() - 1]);
				logger.addAttribute("lexeme", getText());
				location.log(logger);
			}
			logger.endElement();
		}

	}

	/**
	 * A customized token factory which logs tokens.
	 */
	private class PrevTokenFactory implements TokenFactory<Token> {

		@Override
		public Token create(int type, String text) {
			PrevToken token = new PrevToken(type, text);
			token.log(logger);
			return token;
		}

		@Override
		public Token create(Pair<TokenSource, CharStream> source, int type, String text, int channel, int start,
				int stop, int line, int charPositionInLine) {
			PrevToken token = new PrevToken(source, type, channel, start, stop);
			token.log(logger);
			return token;
		}
	}

}
