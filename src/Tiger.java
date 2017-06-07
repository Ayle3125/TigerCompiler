import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;

import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import parser.sym;
import control.CommandLine;
import control.Control;

//import java_cup.runtime.*;

public class Tiger
{
    
  public static void main(String[] args)
  {
    InputStream fstream;
    Parser parser;
    PushbackInputStream  pstream;

    // ///////////////////////////////////////////////////////
    // handle command line arguments
    CommandLine cmd = new CommandLine();
    String fname = cmd.scan(args);

    // /////////////////////////////////////////////
    // the straight-line interpreter (and compiler)    
    switch (Control.ConSlp.action){
    case NONE:
      System.exit(0);
      break;
    default:
      slp.Main slpmain = new slp.Main();
      if (Control.ConSlp.div) {
        slpmain.doit(slp.Samples.dividebyzero);
        System.exit(0);
      }
      slpmain.doit(slp.Samples.prog);
      System.exit(0);
    }

    
    if (fname == null) {
      cmd.usage();
      return;
    }

    // /////////////////////////////////////////////////////
    // it would be helpful to be able to test the lexer
    // independently.
    if (Control.ConLexer.test) {
      System.out.println("Testing the lexer. All tokens:");
      try {
        fstream = new BufferedInputStream(new FileInputStream(fname));
        pstream=new PushbackInputStream(fstream);
        
        Lexer lexer = new Lexer(fname, pstream);
        Token token = lexer.nextToken();
        while (token.kind != Token.Kind.TOKEN_EOF) {
          System.out.println(token.toString());
          token = lexer.nextToken();
        }
        
        /*
        java_cup.runtime.Symbol tok;

        do { 
           //tok=lexer.next_token();
           System.out.println(symnames[tok.sym] + " " + tok.left);
        } while (tok.sym != sym.EOF);
		*/
        fstream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.exit(1);
    }

    // /////////////////////////////////////////////////////////
    // normal compilation phases.
    try {
      fstream = new BufferedInputStream(new FileInputStream(fname));
      //parser = new Parser(new Lexer(fstream));
      pstream = new PushbackInputStream(fstream);
      parser = new Parser(fname, pstream);
      parser.parse();

      fstream.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    return;
  }

  static String symnames[] = new String[100];
  static {
     
     symnames[sym.MULT] = "MULT";
     symnames[sym.AND_AND] = "AND_AND";
     symnames[sym.LPAREN] = "LPAREN";
     symnames[sym.INT] = "INT";
     symnames[sym.MINUS] = "MINUS";
     symnames[sym.STATIC] = "STATIC";
     symnames[sym.RPAREN] = "RPAREN";
     symnames[sym.BOOLEAN_LITERAL] = "BOOLEAN_LITERAL";
     symnames[sym.NOT] = "NOT";
     symnames[sym.SEMICOLON] = "SEMICOLON";
     symnames[sym.LT] = "LT";
     symnames[sym.COMMA] = "COMMA";
     symnames[sym.CLASS] = "CLASS";
     symnames[sym.PLUS] = "PLUS";
     symnames[sym.MINUS] = "MINUS";
     symnames[sym.MAIN] = "MAIN";
     symnames[sym.IF] = "IF";
     symnames[sym.THIS] = "THIS";
     symnames[sym.DOT] = "DOT";
     symnames[sym.EOF] = "EOF";
     symnames[sym.COMMA] = "COMMA";
     symnames[sym.BOOLEAN] = "BOOLEAN";
     symnames[sym.RETURN] = "RETURN";
     symnames[sym.NEW] = "NEW";
     symnames[sym.error] = "error";
     symnames[sym.VOID] = "VOID";
     symnames[sym.EQ] = "EQ";
     symnames[sym.LBRACK] = "LBRACK";
     symnames[sym.LBRACE] = "LBRACE";
     symnames[sym.ELSE] = "ELSE";
     symnames[sym.SYSTEM_OUT_PRINTLN] = "SYSTEM_OUT_PRINTLN";
     symnames[sym.RBRACK] = "RBRACK";
     symnames[sym.WHILE] = "WHILE";
     symnames[sym.PUBLIC] = "PUBLIC";
     symnames[sym.RBRACE] = "RBRACE";
     symnames[sym.EXTENDS] = "EXTENDS";
     symnames[sym.STRING] = "STRING";
     symnames[sym.LENGTH] = "LENGTH";
     symnames[sym.INTEGER_LITERAL] = "INTEGER_LITERAL";
     symnames[sym.IDENTIFIER] = "IDENTIFIER";
   }
}
