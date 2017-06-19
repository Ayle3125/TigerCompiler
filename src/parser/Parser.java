package parser;
import java.io.PushbackInputStream;
import java.util.LinkedList;

import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;
import util.Flist;
import ast.Ast.Class;
import ast.Ast.Dec;
import ast.Ast.Exp;
import ast.Ast.Exp.*;
import ast.Ast.MainClass;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm.T;
import ast.Ast.Stm;
import ast.Ast.Stm.*;
import ast.Ast.Stm.If;
import ast.Ast.Stm.While;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Type;

public class Parser
{
  Lexer lexer;
  Token current;

  public Parser(String fname, PushbackInputStream fstream)
  {
    lexer = new Lexer(fname, fstream);
    current = lexer.nextToken();
  }

  // /////////////////////////////////////////////
  // utility methods to connect the lexer
  // and the parser.

  private void advance()
  {
    current = lexer.nextToken();
  }

  private void eatToken(Kind kind)
  {
    if (kind == current.kind)
      advance();
    else {
      System.out.println("Expects: " + kind.toString());
      System.out.println("But got: " + current.kind.toString());
      System.out.println("line_num is :"+ current.lineNum);
      System.exit(1);
    }
  }

  private void error()
  {

    System.out.println("Syntax error: compilation aborting...line_num\n"+current.lineNum);
    System.exit(1);
    return;
  }

  // ////////////////////////////////////////////////////////////
  // below are method for parsing.

  // A bunch of parsing methods to parse expressions. The messy
  // parts are to deal with precedence and associativity.

  // ExpList -> Exp ExpRest*
  // ->
  // ExpRest -> , Exp
  private java.util.LinkedList<Exp.T> parseExpList()
  {
	LinkedList<Exp.T> list=new LinkedList<Exp.T>(); 
	  
    if (current.kind == Kind.TOKEN_RPAREN)
      return null;
    list.add(parseExp());
    
    while (current.kind == Kind.TOKEN_COMMER) {
      advance();
      list.add(parseExp());
    }
    
    return list;
  }

  // AtomExp -> (exp)
  // -> INTEGER_LITERAL
  // -> true
  // -> false
  // -> this
  // -> id
  // -> new int [exp]
  // -> new id ()

  private Exp.T parseAtomExp()
  {
	Exp.T exp;  
	  
    switch (current.kind) {
    case TOKEN_LPAREN:
      advance();
      exp = parseExp();
      
      eatToken(Kind.TOKEN_RPAREN);
      return exp;
    case TOKEN_NUM:
    	int num=Integer.parseInt(current.lexeme);
    	advance();
      
    	return new Num(num);
    case TOKEN_TRUE:
      advance();
      return new True();
    case TOKEN_FALSE:
        advance();
        return new False();      
    case TOKEN_THIS:
      advance();
      return new This();
    case TOKEN_ID:
    	String id=current.lexeme;
    	
      advance();
      return new Id(id);
      
      // -> new int [exp]
      // -> new id ()
    case TOKEN_NEW: {
      advance();
      switch (current.kind) {
      case TOKEN_INT: 
        advance();
        eatToken(Kind.TOKEN_LBRACK);
        exp = parseExp();
        
        eatToken(Kind.TOKEN_RBRACK);
        
        //public NewIntArray(T exp)
        return new NewIntArray(exp);
        
      case TOKEN_ID:
    	String idd = current.lexeme; 
    	  
        advance();
        eatToken(Kind.TOKEN_LPAREN);
        eatToken(Kind.TOKEN_RPAREN);
        
        // public NewObject(String id)
        return new NewObject(idd);
      default:
        error();
        return null;
      }
    }
    default:
      error();

      return null;

    }
  }

  // NotExp -> AtomExp
  // -> AtomExp .id (expList)
  // -> AtomExp [exp]
  // -> AtomExp .length

  private Exp.T parseNotExp()
  {
	Exp.T exp;
	  
    exp = parseAtomExp();
    
    while (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {
      if (current.kind == Kind.TOKEN_DOT) {
        advance();
        // -> AtomExp .length
        if (current.kind == Kind.TOKEN_LENGTH) {
          advance();
          
          //public Length(T array)
          return new Length(exp);
        }
        
        String id;
        java.util.LinkedList<Exp.T> args;
        
        id = current.lexeme;
        
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        args = parseExpList();
        
        eatToken(Kind.TOKEN_RPAREN);
        
        // -> AtomExp .id (expList)
        // public Call(T exp, String id, java.util.LinkedList<T> args)
        return new Call(exp, id, args);
        
      } else {
    	Exp.T expp;
    	  
        advance();
        expp = parseExp();
        eatToken(Kind.TOKEN_RBRACK);
        
        // -> AtomExp [exp]
        // public ArraySelect(T array, T index)
        
        return new ArraySelect(exp, expp);
      }
    }
    return exp;

  }

  // TimesExp -> ! TimesExp
  // -> NotExp

  private Exp.T parseTimesExp()
  {
	Exp.T exp;
	
    while (current.kind == Kind.TOKEN_NOT) {
      advance();
      exp = parseNotExp();
      return new Not(exp);
      
    }
    exp = parseNotExp();
    
    //public Not(T exp)
    return exp;

  }

  // AddSubExp -> TimesExp * TimesExp
  // -> TimesExp

  private Exp.T parseAddSubExp()
  {
	Exp.T left=null, right=null;
	
    left = parseTimesExp();
    while (current.kind == Kind.TOKEN_TIMES) {
      advance();
      right = parseTimesExp();
    }
    if(right==null)
    {
    	return left;
    }
    else 
    {
    	return new Times(left, right);
	}
    
  }

  // LtExp -> AddSubExp + AddSubExp
  // -> AddSubExp - AddSubExp
  // -> AddSubExp

  private Exp.T parseLtExp()
  {
	Exp.T left=null, right=null;  
	
    left = parseAddSubExp();
    while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
    	if(current.kind==Kind.TOKEN_ADD)
    	{
    		advance();
        	right = parseAddSubExp();
        	return new Add(left, right);
        			
    	}
    	else if(current.kind==Kind.TOKEN_SUB)
    	{
	    		advance();
	        	right = parseAddSubExp();
	        	
	        	//public Sub(T left, T right)
	        	return new Sub(left, right);
    	}
    }
    
    return  left;

  }

  // AndExp -> LtExp < LtExp
  // -> LtExp

  private Exp.T parseAndExp()
  {
	Exp.T left=null, right=null;  
	  
    left = parseLtExp();
    
    while (current.kind == Kind.TOKEN_LT) {
      advance();
      right = parseLtExp();
    }
    
    //public Lt(T left, T right)
    if(right==null)
    {
    	return left;
    }
    else{
    		return new Lt(left, right);
		}
    
  }

  // Exp -> AndExp && AndExp
  // -> AndExp

  private Exp.T parseExp()
  {
	Exp.T left=null, right=null;
	
	left = parseAndExp();
	
    while (current.kind == Kind.TOKEN_AND) {
      advance();
      right = parseAndExp();
    }
    
    //public Add(T left, T right)
    if(right==null)
    {
    	return left;
    }
    else{
			return new And(left, right);
		}
       
  }

  // Statement -> { Statement* }
  // -> if ( Exp ) Statement else Statement
  // -> while ( Exp ) Statement
  // -> System.out.println ( Exp ) ;
  // -> id = Exp ;
  // -> id [ Exp ]= Exp ;

  private Stm.T parseStatement()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a statement.
    // new util.Todo();
	  
	  
	  if(current.kind==Kind.TOKEN_LBRACE)
	  {
		  LinkedList<Stm.T> list=new LinkedList<Stm.T>();
		  
		  eatToken(Kind.TOKEN_LBRACE);
		  while(current.kind != Kind.TOKEN_RBRACE)
		  {
			  list.add(parseStatement());
		  }
		  
		  eatToken(Kind.TOKEN_RBRACE);
		  
		  return new Block(list);
	  }
	  else if(current.kind== Kind.TOKEN_IF)
	  {
		  Exp.T condition;
		  T thenn, elsee;
		  
		  eatToken(Kind.TOKEN_IF);
		  eatToken(Kind.TOKEN_LPAREN);
		  condition = parseExp();
		  
		  eatToken(Kind.TOKEN_RPAREN);
		  thenn = parseStatement();
		  eatToken(Kind.TOKEN_ELSE);
		  elsee = parseStatement();
		  
		  //public If(Exp.T condition, T thenn, T elsee)
		  return new If(condition, thenn, elsee);
		  
	  }else if(current.kind == Kind.TOKEN_WHILE){
		  
		  Exp.T condition;
		  T body;
		  
		  eatToken(Kind.TOKEN_WHILE);
		  eatToken(Kind.TOKEN_LPAREN);
		  condition = parseExp();
		  
		  eatToken(Kind.TOKEN_RPAREN);
		  body = parseStatement();
		  
		  //public While(Exp.T condition, T body)		  
		  return new While(condition, body);
		  
		  /*
		  return new While
		  (
				  new Lt(new Id("i"), new Id("n")),
				  new ast.Ast.Stm.Block
				  (
						  new util.Flist<Stm.T>().list
						  (
								  new Assign("sum", new Add(new Id("sum"), new Id("i"))),
								  new Assign("i", new Add(new Id("i"), new Num(1)))
						  )  
				  )
				  //new Assign("sum", new Add(new Id("sum"), new Id("i")))
		  );
		  */
	  }else if(current.kind== Kind.TOKEN_SYSTEM){
		  Exp.T exp;
		  
		  eatToken(Kind.TOKEN_SYSTEM);
		  eatToken(Kind.TOKEN_DOT);
		  eatToken(Kind.TOKEN_OUT);
		  eatToken(Kind.TOKEN_DOT);
		  eatToken(Kind.TOKEN_PRINTLN);
		  eatToken(Kind.TOKEN_LPAREN);
		  exp = parseExp();
		  
		  eatToken(Kind.TOKEN_RPAREN);
		  eatToken(Kind.TOKEN_SEMI);
		  
	      //public Print(Exp.T exp)
		  return new Print(exp);
		  
		  /*return new Print
		     (
		    	new Call	
		    	(
		          new NewObject("Fac"), "ComputeFac",
		          new util.Flist<Exp.T>().list(new Num(10))
		        )
		     );
		   */
		  
	}
	else if(current.kind==Kind.TOKEN_ID)
	{
		/*  // -> id = Exp ;
			// -> id [ Exp ]= Exp ;  */		
		String id;
		id = current.lexeme;
		if(new_token != null)
		{
			current = new_token;
			tmp_token = null;
			new_token = null;
		}
		else
			eatToken(Kind.TOKEN_ID);
		
		if(current.kind==Kind.TOKEN_ASSIGN)
		{
			Exp.T exp;
			eatToken(Kind.TOKEN_ASSIGN);
			exp = parseExp();
			eatToken(Kind.TOKEN_SEMI);
			
			//public Assign(String id, Exp.T exp)
			return new Assign(id, exp);
		}else if(current.kind==Kind.TOKEN_LBRACK)
		{
			Exp.T index, exp;
			
			eatToken(Kind.TOKEN_LBRACK);
			index = parseExp();
			
			eatToken(Kind.TOKEN_RBRACK);
			eatToken(Kind.TOKEN_ASSIGN);
			exp = parseExp();
			
			eatToken(Kind.TOKEN_SEMI);
			
			//public AssignArray(String id, Exp.T index, Exp.T exp)
			return new AssignArray(id, index, exp);
		}
		
		
	}
	  /*  // -> id = Exp ;
         // -> id [ Exp ]= Exp ;
	   * */
	  
	  return null;

  }

  // Statements -> Statement Statements
  // ->

  private LinkedList<Stm.T> parseStatements()
  {

	LinkedList<Stm.T> list = new LinkedList<Stm.T>();
	
    while (current.kind == Kind.TOKEN_LBRACE || current.kind == Kind.TOKEN_IF
        || current.kind == Kind.TOKEN_WHILE
        || current.kind == Kind.TOKEN_SYSTEM || current.kind == Kind.TOKEN_ID) {
    		
    	list.add(parseStatement());
    }
    return list;

  }

  // Type -> int []
  // -> boolean
  // -> int
  // -> id

  private Type.T parseType()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a type.
	//  System.out.println("294");
    //new util.Todo();
	ast.Ast.Type.T type;  
	  
	if(current.kind==Kind.TOKEN_INT)
	{
		  eatToken(Kind.TOKEN_INT);
		  if(current.kind==Kind.TOKEN_LBRACK)
		  {
			  eatToken(Kind.TOKEN_LBRACK);
			  eatToken(Kind.TOKEN_RBRACK);
			  return new Type.IntArray();
		  }
		  return new Type.Int();
	}
	else if(current.kind== Kind.TOKEN_BOOLEAN) 
	{
		  eatToken(Kind.TOKEN_BOOLEAN);
		  return new Type.Boolean();
		  
	}
	else if(current.kind==Kind.TOKEN_ID)
	{
		String id = current.lexeme;
		eatToken(Kind.TOKEN_ID);
		return new Type.ClassType(id);	
	}
	  return null;
  }

  // VarDecl -> Type id ;
  private Dec.T parseVarDecl()
  {
    // to parse the "Type" nonterminal in this method, instead of writing
    // a fresh one.
	// int a;  
	  
	Type.T type;
	String id;

	if(tmp_token!=null)  //tree a;
	{
		new_token = current;
		current = tmp_token;
		type = parseType();
		current = new_token;
		id = current.lexeme;
			
		eatToken(Kind.TOKEN_ID);
		//eatToken(Kind.TOKEN_SEMI);
		//advance();
		
		tmp_token = null;
		new_token = null;
		return new Dec.DecSingle(type, id);
	}
    //int s;
	type = parseType();
	id = current.lexeme;
	eatToken(Kind.TOKEN_ID);
	eatToken(Kind.TOKEN_SEMI);
	return new Dec.DecSingle(type, id);
    
    
    
    /*
    type = parseType();   
    if(current.kind==Kind.TOKEN_ID)
    {
    	id = current.lexeme;
    	
	    eatToken(Kind.TOKEN_ID);
	    eatToken(Kind.TOKEN_SEMI);
	    return new Dec.DecSingle(type, id);
    }
    else if(current.kind==Kind.TOKEN_ASSIGN) //aux01 = this.Init(sz);
    {
    	eatToken(Kind.TOKEN_ASSIGN);
    	parseExp();
    	eatToken(Kind.TOKEN_SEMI);
	}
    else if (current.kind==Kind.TOKEN_LBRACK) {//number[0] = 20 ;
		eatToken(Kind.TOKEN_LBRACK);
		eatToken(Kind.TOKEN_NUM);
		eatToken(Kind.TOKEN_RBRACK);
		eatToken(Kind.TOKEN_ASSIGN);
		parseExp();
		eatToken(Kind.TOKEN_SEMI);
    	
	}
    */

  }

  // VarDecls -> VarDecl VarDecls
  // ->

  
  Token tmp_token=null,new_token = null;
  
  private LinkedList<Dec.T> parseVarDecls()
  {
	LinkedList<Dec.T> list=new LinkedList<Dec.T>();
	  
    while (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
    	
    	if(current.kind == Kind.TOKEN_ID)
    	{
    		tmp_token = current;
    		advance();
    		if(current.kind == Kind.TOKEN_ID) //tree a;
    		{
    			list.add(parseVarDecl());
    			continue;
    		}
    		else 
    		{
    			return list;
			}
    	}
    	
    	list.add(parseVarDecl());
    }
    return list;

  }

  // FormalList -> Type id FormalRest*
  // ->
  // FormalRest -> , Type id

  private LinkedList<Dec.T> parseFormalList()
  {
	LinkedList<Dec.T> list= new LinkedList<Dec.T>();
	Type.T type;
	String id;
	//public DecSingle(Type.T type, String id)
	
    if (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
        || current.kind == Kind.TOKEN_ID) {
    	
    	type = parseType();
    	id = current.lexeme;
      
    	eatToken(Kind.TOKEN_ID);
    	list.add(new Dec.DecSingle(type, id));
    	
    	
      while (current.kind == Kind.TOKEN_COMMER) {
        advance();
        type = parseType();
        id = current.lexeme;
        
        eatToken(Kind.TOKEN_ID);
        list.add(new Dec.DecSingle(type, id));
      }
    }
    return list;

  }

  // Method -> public Type id ( FormalList )
  // { VarDecl* Statement* return Exp ;}

  private Method.T parseMethod()
  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a method.
	// System.out.println("344");
	//   new util.Todo();

	Type.T retType;
	String id;
	LinkedList<Dec.T> formals; 
	LinkedList<Dec.T> locals;
	LinkedList<Stm.T> stms;
	Exp.T retExp;  
	  
	eatToken(Kind.TOKEN_PUBLIC);
	
	retType = parseType();
	id=current.lexeme;

	eatToken(Kind.TOKEN_ID);
	eatToken(Kind.TOKEN_LPAREN);
	formals = parseFormalList();
	
	eatToken(Kind.TOKEN_RPAREN);
	eatToken(Kind.TOKEN_LBRACE);
	locals = parseVarDecls();
	
	//??
	if(current.kind == Kind.TOKEN_ASSIGN && tmp_token!=null)
	{
		  new_token = current;
		  current = tmp_token;
		 
	}
	
	stms = parseStatements();
	eatToken(Kind.TOKEN_RETURN);
	retExp = parseExp();
	
	eatToken(Kind.TOKEN_SEMI);
	eatToken(Kind.TOKEN_RBRACE);
	  
	  
	return new Method.MethodSingle(retType, id, formals, locals, stms, retExp);

  }

  // MethodDecls -> MethodDecl MethodDecls
  // ->

  private LinkedList<Method.T> parseMethodDecls()
  {
	  LinkedList<Method.T> list =new LinkedList<Method.T>();
	  
	  while (current.kind == Kind.TOKEN_PUBLIC) {
		  list.add(parseMethod());
	  }
    return list;

  }

  // ClassDecl -> class id { VarDecl* MethodDecl* }
  // -> class id extends id { VarDecl* MethodDecl* }

  private Class.T parseClassDecl()
  {
	String id=null, extend=null;
	LinkedList<Dec.T> decs=null;
	LinkedList<ast.Ast.Method.T> method=null;
	  
    eatToken(Kind.TOKEN_CLASS);
    id = current.lexeme;
    
    eatToken(Kind.TOKEN_ID);
    if (current.kind == Kind.TOKEN_EXTENDS) {
      eatToken(Kind.TOKEN_EXTENDS);
      extend = current.lexeme;
      
      eatToken(Kind.TOKEN_ID);
    }
    eatToken(Kind.TOKEN_LBRACE);
    
    decs   = parseVarDecls();
    
    
    method = parseMethodDecls();

    eatToken(Kind.TOKEN_RBRACE);
    
    return new ast.Ast.Class.ClassSingle(id, extend, decs, method);

  }

  // ClassDecls -> ClassDecl ClassDecls
  // ->

  private LinkedList<Class.T> parseClassDecls()
  {
	LinkedList<Class.T> list = new LinkedList<>();
	
    while (current.kind == Kind.TOKEN_CLASS) {
      list.add(parseClassDecl());
    }
    return list;

  }

  // MainClass -> class id
  // {
  // public static void main ( String [] id )
  // {
  // Statement
  // }
  // }
  private MainClass.T parseMainClass()

  {
    // Lab1. Exercise 4: Fill in the missing code
    // to parse a main class as described by the
    // grammar above.
    //new util.Todo();
/*
 * class LinearSearch{
    public static void main(String[] a){
	System.out.println(new LS().Start(10));
    }
	}
 * 
 * */
	eatToken(Kind.TOKEN_CLASS);
	String id = current.lexeme;
	
	eatToken(Kind.TOKEN_ID);
	eatToken(Kind.TOKEN_LBRACE);
	eatToken(Kind.TOKEN_PUBLIC);
	eatToken(Kind.TOKEN_STATIC);
	eatToken(Kind.TOKEN_VOID);
	eatToken(Kind.TOKEN_MAIN);
	eatToken(Kind.TOKEN_LPAREN);	
	eatToken(Kind.TOKEN_STRING);
	eatToken(Kind.TOKEN_LBRACK);
	eatToken(Kind.TOKEN_RBRACK);
	String arg = current.lexeme;
	eatToken(Kind.TOKEN_ID);
	eatToken(Kind.TOKEN_RPAREN);
	
	eatToken(Kind.TOKEN_LBRACE);
	
	Stm.T stm = parseStatement();
	eatToken(Kind.TOKEN_RBRACE);
	eatToken(Kind.TOKEN_RBRACE);
	
	return new MainClassSingle(id, arg, stm);
  }

  // Program -> MainClass ClassDecl*
  private ast.Ast.Program.T parseProgram()
  {
	  /*MainClass.T mainClass, LinkedList<Class.T> classes
	   * */
	  
	  MainClass.T mainClass =parseMainClass();
	  LinkedList<Class.T> classes=parseClassDecls();
	  
	  eatToken(Kind.TOKEN_EOF);
    
    return new ProgramSingle(mainClass,classes);
  }

  public ast.Ast.Program.T parse()
  {

	  ast.Ast.Program.T t = parseProgram();
	  
	  return t;
  }
}
