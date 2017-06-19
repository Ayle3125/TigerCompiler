package lexer;

import static control.Control.ConLexer.dump;
import java.io.PushbackInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sun.org.apache.regexp.internal.recompile;

import lexer.Token.Kind;

public class Lexer
{
  String fname; // the input file name to be compiled
  
  PushbackInputStream fstream; // input stream for the above file
  int line_num = 1;
  
  Map<String, Kind> map = new HashMap<String, Kind>(); 
    
  public Lexer(String fname, PushbackInputStream fstream)
  {
    this.fname = fname;
    this.fstream = fstream;
   
    map.put("boolean", Kind.TOKEN_BOOLEAN);
    map.put("class", Kind.TOKEN_CLASS);
    map.put("else", Kind.TOKEN_ELSE);
    map.put("extends", Kind.TOKEN_EXTENDS);
    map.put("false", Kind.TOKEN_FALSE);
    map.put("if", Kind.TOKEN_IF);
    map.put("int", Kind.TOKEN_INT);
    map.put("length", Kind.TOKEN_LENGTH);
    map.put("main", Kind.TOKEN_MAIN);
    map.put("new", Kind.TOKEN_NEW);
    map.put("out", Kind.TOKEN_OUT);
    map.put("println", Kind.TOKEN_PRINTLN);
    map.put("public", Kind.TOKEN_PUBLIC);
    map.put("return", Kind.TOKEN_RETURN);
    map.put("static", Kind.TOKEN_STATIC);
    map.put("String", Kind.TOKEN_STRING);
    map.put("System", Kind.TOKEN_SYSTEM);
    map.put("this", Kind.TOKEN_THIS);
    map.put("true", Kind.TOKEN_TRUE);
    map.put("void", Kind.TOKEN_VOID);
    map.put("while", Kind.TOKEN_WHILE);
    
  }
  
  // When called, return the next token (refer to the code "Token.java")
  // from the input stream.
  // Return TOKEN_EOF when reaching the end of the input stream.
  private Token nextTokenInternal() throws Exception
  {
    int c = this.fstream.read();
    if (-1 == c)
      // The value for "lineNum" is now "null",
      // you should modify this to an appropriate
      // line number for the "EOF" token.

      return new Token(Kind.TOKEN_EOF, line_num);

    // skip all kinds of "blanks"
    while (' ' == c || '\t' == c || '\n' == c) {

        if('\n'==c)
      	  this.line_num ++;
        
        c = this.fstream.read();
    }
    if (-1 == c)
      return new Token(Kind.TOKEN_EOF, line_num);

    switch (c) {
    case '+':
    	return new Token(Kind.TOKEN_ADD, line_num);
    case '-':
    	return new Token(Kind.TOKEN_SUB, line_num);
    case '*':
    	return new Token(Kind.TOKEN_TIMES, line_num);
    case '/':
    {
    	c=this.fstream.read();
    	
    	if(c=='/'){
    		c = this.fstream.read();
    		while(c!='\n')
    		{
    			c = this.fstream.read();
    		}
    		line_num++;
    		return new Token(Kind.TOKEN_COMMENT_single, (line_num-1));
    	}
    	else if(c=='*')
    	{
    		c=this.fstream.read();
    		
    		while (c!='/') {
    			c=this.fstream.read();
    			if(c=='\n')
    				line_num++;
			}
    		
    		return new Token(Kind.TOKEN_COMMENT_multi, line_num);
    	}
    	else{
    		this.fstream.unread(c);
    		return new Token(Kind.TOKEN_DIV, line_num);
    	}
    }
    case '{':
    	return new Token(Kind.TOKEN_LBRACE, line_num);
    case '}':
    	return new Token(Kind.TOKEN_RBRACE, line_num);
    case '(':
    	return new Token(Kind.TOKEN_LPAREN, line_num);
    case ')':
    	return new Token(Kind.TOKEN_RPAREN, line_num);
    case '[':
    	return new Token(Kind.TOKEN_LBRACK, line_num);
    case ']':
    	return new Token(Kind.TOKEN_RBRACK, line_num);
    case '&':
    {
    	c = this.fstream.read();
    	if(c=='&')
    		return new Token(Kind.TOKEN_AND, line_num);
    	return null;
    }
    
    case '>':
    	return new Token(Kind.TOKEN_LT, line_num);
    case '<':
    	return new Token(Kind.TOKEN_LT, line_num);
    case '!':
    	return new Token(Kind.TOKEN_NOT, line_num);
    case ',':
    	return new Token(Kind.TOKEN_COMMER, line_num);
    case '.':
    	return new Token(Kind.TOKEN_DOT, line_num);
    case '=':
    	return new Token(Kind.TOKEN_ASSIGN, line_num);
    case ';':
    	return new Token(Kind.TOKEN_SEMI, line_num);
    default:
      // Lab 1, exercise 2: supply missing code to
      // lex other kinds of tokens.
      // Hint: think carefully about the basic
      // data structure and algorithms. The code
      // is not that much and may be less than 50 lines. If you
      // find you are writing a lot of code, you
      // are on the wrong way.
      //new Todo();
    	
    	/*digital */	
    	String temp="";
    	while(c>='0'&&c<='9')
    	{
    		temp+=(char)c;
    		c=this.fstream.read();
    	}
    	    	
    	if(!temp.equals(""))
    	{
    		this.fstream.unread(c);
    		return new Token(Kind.TOKEN_NUM, line_num, temp);
    	}
    	
    	/* number  */
    	temp="";
    	while((c>='a'&&c<='z')||(c>='A'&&c<='Z')||(c>='0'&&c<='9')||(c=='_'))
    	{
    		temp+=(char)c;
    		c=this.fstream.read();
    	}
    	
    	if(!temp.equals(""))
    	{
    		 this.fstream.unread(c);
    		 
    		 if(map.containsKey(temp))
    		 {
    			 return new Token(map.get(temp),line_num,temp);
    		 }else{
    			 return new Token(Kind.TOKEN_ID,line_num,temp);
    		 }		 
    	}
    	
      return null;
    }
  }

  public Token nextToken()
  {
    Token t = null;

    try {
      t = this.nextTokenInternal();

      while(t.kind==Kind.TOKEN_COMMENT_multi||t.kind==Kind.TOKEN_COMMENT_single)
      {
    	  t = this.nextTokenInternal();
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    if (dump)
      System.out.println(t.toString());
    return t;
  }
}
