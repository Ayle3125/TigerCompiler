import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PushbackInputStream;

import static control.Control.ConAst.dumpAst;
//import static control.Control.ConAst.testFac;
import ast.Ast.Program;

import lexer.Lexer;
import lexer.Token;
import parser.Parser;
//import parser.sym;
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
    //System.out.println(Control.ConLexer.test);
    // /////////////////////////////////////////////
    // the straight-line interpreter (and compiler)    
    /*
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
      slpmain.doit(slp.Samples.prog);System.out.println("#");
      System.exit(0);
    }

   */
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
        pstream = new PushbackInputStream(fstream);
        //Lexer lexer = new Lexer(fstream);   
        Lexer lexer = new Lexer(fname, pstream);       
        
        Token token = lexer.nextToken();
        while (token.kind != Token.Kind.TOKEN_EOF) {
          System.out.println(token.toString());
          token = lexer.nextToken();
        }       
        
        java_cup.runtime.Symbol tok;
        /*
        do { 
           tok=lexer.next_token();
           System.out.println(symnames[tok.sym] + " " + tok.left);
        } while (tok.sym != sym.EOF);
		
		*/
        fstream.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      System.exit(1);
    }

    //Control.ConCodeGen.fileName = fname;

    Program.T theAst = null;
    
    // /////////////////////////////////////////////////////////
    // normal compilation phases.
    try {
      fstream = new BufferedInputStream(new FileInputStream(fname));
      //parser = new Parser(new Lexer(fstream));
      pstream = new PushbackInputStream(fstream);
      parser = new Parser(fname, pstream);
      theAst = parser.parse();

      fstream.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  
    // pretty printing the AST, if necessary
    if (dumpAst) {
    	ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
    	theAst.accept(pp);
    }


    // elaborate the AST, report all possible errors.
    elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
    theAst.accept(elab);

    return;
    
  }

}
