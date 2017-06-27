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
    
    static Tiger tiger;
    static CommandLine cmd;
    static InputStream fstream;
    static PushbackInputStream  pstream;
    public ast.Ast.Program.T theAst;
    
    // lex and parse
    public void lexAndParse(String fname)
    {
        Parser parser;
        try {
            fstream = new BufferedInputStream(new FileInputStream(fname));
            pstream = new PushbackInputStream(fstream);
            parser = new Parser(fname, pstream); 
            
            theAst = parser.parse();
            
            fstream.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return;
    }
    
    public void compile(String fname) {
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
        // /////////////////////////////////////////////////////
        // normal test
        if (fname == null) {
            cmd.usage();
            return;
        }
        Control.ConCodeGen.fileName = fname;
        
        // /////////////////////////////////////////////////////
        // it would be helpful to be able to test the lexer independently.
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

                /*
                java_cup.runtime.Symbol tok;
        do { 
           tok=lexer.next_token();
           System.out.println(symnames[tok.sym] + " " + tok.left);
        } while (tok.sym != sym.EOF);
        
                 */
                fstream.close();
                System.out.println("Testing the lexer is finished.\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        // /////////////////////////////////////////////////////////
        // normal compilation phases.
        theAst = null;

        control.CompilerPass lexAndParsePass = new control.CompilerPass("Lex and parse", tiger, fname);
        lexAndParsePass.doitName("lexAndParse");

        // pretty printing the AST, if necessary
        if (dumpAst) {
            ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
            control.CompilerPass ppAstPass = new control.CompilerPass("Pretty printing the AST", theAst, pp);
            ppAstPass.doit();
        }

        // elaborate the AST, report all possible errors.
        elaborator.ElaboratorVisitor elab = new elaborator.ElaboratorVisitor();
        control.CompilerPass elabAstPass = new control.CompilerPass("Elaborating the AST", theAst, elab);
        elabAstPass.doit();

        // optimize the AST
        ast.optimizations.Main optAstPasses = new ast.optimizations.Main();
        control.CompilerPass optAstPass = new control.CompilerPass("Optimizing the AST", optAstPasses, theAst);
        optAstPass.doit();
        theAst = optAstPasses.program;
        
        // ////////////////////////////////////////////////////////////////////////
        // code generation
        switch (control.Control.ConCodeGen.codegen) {
        case Bytecode:
            codegen.bytecode.TranslateVisitor trans = new codegen.bytecode.TranslateVisitor();
            theAst.accept(trans);
            codegen.bytecode.Ast.Program.T bytecodeAst = trans.program;
            codegen.bytecode.PrettyPrintVisitor ppbc = new codegen.bytecode.PrettyPrintVisitor();
            bytecodeAst.accept(ppbc);
            break;
        case C:
            codegen.C.TranslateVisitor transC = new codegen.C.TranslateVisitor();
            theAst.accept(transC);
            codegen.C.Ast.Program.T cAst = transC.program;
            codegen.C.PrettyPrintVisitor ppc = new codegen.C.PrettyPrintVisitor();
            cAst.accept(ppc);
            break;
        default:
            
        }
        
    }

    public void assemble(String str)
    {
    }

    public void link(String str)
    {
    }

    public void compileAndLink(String fname) {
        // compile
        control.CompilerPass compilePass = new control.CompilerPass("Compile", tiger, fname);
        compilePass.doitName("compile");

        // assembling
        control.CompilerPass assemblePass = new control.CompilerPass("Assembling", tiger, fname);
        assemblePass.doitName("assemble");

        // linking
        control.CompilerPass linkPass = new control.CompilerPass("Linking", tiger, fname);
        linkPass.doitName("link");

        return;
    }
    
    public static void main(String[] args)
    {
        // ///////////////////////////////////////////////////////
        // handle command line arguments
        tiger = new Tiger();
        cmd = new CommandLine();
        String fname = "";
        fname = cmd.scan(args);

        control.CompilerPass tigerAll = new control.CompilerPass("Tiger", tiger, fname);
        tigerAll.doitName("compileAndLink");
        
        return;
        
    }
    
}
