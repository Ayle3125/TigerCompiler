/*
 * JFlex specification for MiniJava.
 */



package lexer;

import java_cup.runtime.*;
import parser.sym;

%%

%public
%final
%class Lexer
%cup
%line
%column

%{
  // note that these Symbol constructors are abusing the Symbol
  // interface to use Symbol's left and right fields as line and column
  // fields instead
  private Symbol symbol(int type) {
    return new Symbol(type, yyline+1, yycolumn+1);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline+1, yycolumn+1, value);
  }
/*
  // print out a symbol (aka token) nicely
  public String symbolToString(Symbol s) {
    switch (s.sym) {
        case sym.CLASS:                 return "CLASS";
        case sym.PUBLIC:                return "PUBLIC";
        case sym.STATIC:                return "STATIC";
        case sym.EXTENDS:               return "EXTENDS";
        case sym.MAIN:                  return "MAIN";  
        case sym.RETURN:                return "RETURN";
        case sym.STRING:                return "STRING";
        case sym.VOID:                  return "VOID";
        case sym.INT:                   return "INT";
        case sym.BOOLEAN:               return "BOOLEAN";
        case sym.IF:                    return "IF";
        case sym.ELSE:                  return "ELSE";
        case sym.WHILE:                 return "WHILE"; 
        case sym.SYSTEM_OUT_PRINTLN:    return "SYSTEM_OUT_PRINTLN";
        case sym.LENGTH:                return "LENGTH";
        case sym.THIS:                  return "THIS";
        case sym.NEW:                   return "NEW";
        case sym.LPAREN:                return "LPAREN";
        case sym.RPAREN:                return "RPAREN";
        case sym.LBRACE:                return "LBRACE";
        case sym.RBRACE:                return "RBRACE";
        case sym.LBRACK:                return "LBRACK";
        case sym.RBRACK:                return "RBRACK";        
        case sym.SEMICOLON:             return "SEMICOLON";
        case sym.COMMA:                 return "COMMA";
        case sym.DOT:                   return "DOT";
        case sym.EQ:                    return "EQ";
        case sym.LT:                    return "LT";
        case sym.NOT:                   return "NOT";
        case sym.AND_AND:               return "AND_AND";
        case sym.PLUS:                  return "PLUS";
        case sym.MINUS:                 return "MINUS";
        case sym.MULT:                  return "MULT";
        case sym.BOOLEAN_LITERAL:       return "BOOLEAN("+ s.value.toString() + ")";
        case sym.IDENTIFIER:            return "ID(" + (String)s.value + ")";
        case sym.INTEGER_LITERAL:       return "INTEGER(" + String.valueOf(s.value) +")";
        case sym.EOF:                   return "<EOF>";
        case sym.error:                 return "<ERROR>";
        default:                        return "<UNEXPECTED TOKEN " + s.toString() + ">";
    }
  }
  */
%}

/*comentarios*/
    Comment = {NormalComment} | {EoLComment} | {DocComment}    
    NormalComment = "/*" + [^*] ~"*/" | "/*" "*" + "/"
    EoLComment    = "//" + {NewChar}* + {EndLine}?
    DocComment    = "/*" "*" + [^*] ~"*/"

/*identifiers*/
    Identifier = [a-zA-Z][a-zA-Z0-9_]*

/*integer*/
    IntegerLiteral = 0 | [1-9][0-9]*

/* Helper definitions */
//   letter = [a-zA-Z]
//    digit = [0-9]
    white = {EndLine} | [ \t\f]
    EndLine =  \n | \r\n
    NewChar = [^\r\n]

%%

/* Token definitions */

/* reserved words */
/* (put here so that reserved words take precedence over identifiers) */
    "boolean"                   {return symbol(sym.BOOLEAN); }
    "class"                     {return symbol(sym.CLASS);   }
    "extends"                   {return symbol(sym.EXTENDS); }
    "new"                       {return symbol(sym.NEW);     }
    "int"                       {return symbol(sym.INT);    } 
    "if"                        {return symbol(sym.IF);      }
    "else"                      {return symbol(sym.ELSE);    }
    "public"                    {return symbol(sym.PUBLIC);  }
    "return"                    {return symbol(sym.RETURN);  }
    "void"                      {return symbol(sym.VOID);    }
    "static"                    {return symbol(sym.STATIC);  }
    "while"                     {return symbol(sym.WHILE);   }
    "this"                      {return symbol(sym.THIS);    }
  
/*boolean literals*/
    "true"                      {return symbol(sym.BOOLEAN_LITERAL, new Boolean(true));}
    "false"                     {return symbol(sym.BOOLEAN_LITERAL, new Boolean(true));}

/*separators*/
    "("                         {return symbol(sym.LPAREN);   }
    ")"                         {return symbol(sym.RPAREN);   }
    "{"                         {return symbol(sym.LBRACE);   }
    "}"                         {return symbol(sym.RBRACE);   }
    "["                         {return symbol(sym.LBRACK);   }
    "]"                         {return symbol(sym.RBRACK);   }
    ";"                         {return symbol(sym.SEMICOLON);}
    ","                         {return symbol(sym.COMMA);    }
    "."                         {return symbol(sym.DOT);      }

/* operators */
    "="                         {return symbol(sym.EQ);       }
    "<"                         {return symbol(sym.LT);       }
    "!"                         {return symbol(sym.NOT);      }
    "&&"                        {return symbol(sym.AND_AND);  }
    "+"                         {return symbol(sym.PLUS);     }
    "-"                         {return symbol(sym.MINUS);    }
    "*"                         {return symbol(sym.MULT);     }

/*comments*/
    {Comment}+                  {/*ignore*/}

/*whitespace*/
    {white}                {/*ignore*/}

/*others*/
    "String"                    {return symbol(sym.STRING);  }
    "main"                      {return symbol(sym.MAIN);    }
    "length"                    {return symbol(sym.LENGTH);  }
    "System.out.println"        {return symbol(sym.SYSTEM_OUT_PRINTLN); }

/* identifiers */
    {Identifier}                        {return symbol(sym.IDENTIFIER, yytext()); }

/*int literals*/
    {IntegerLiteral}                {return symbol(sym.INTEGER_LITERAL, new Integer(yytext())); }


