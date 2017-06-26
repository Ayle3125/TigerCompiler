package ast;

import java.util.LinkedList;

public class Ast
{
    
    // ///////////////////////////////////////////////////////////
    // type
    public static class Type
    {
        public static abstract class T implements ast.Acceptable
        {
            // boolean: -1
            // int: 0
            // int[]: 1
            // class: 2
            // Such that one can easily tell who is who
            public abstract int getNum();
            public Integer lineNum; // on which line of the source file this appears
        }
        
        // boolean
        public static class Boolean extends T
        {
            public Boolean(Integer num)
            {
                lineNum = num;
            }
            
            @Override
            public String toString()
            {
                return "@boolean";
            }
            
            @Override
            public int getNum()
            {
                return -1;
            }
            
            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }
        
        // class
        public static class ClassType extends T
        {
            public String id;
            
            public ClassType(Integer num, String id)
            {
                lineNum = num;
                this.id = id;
            }
            
            @Override
            public String toString()
            {
                return this.id;
            }
            
            @Override
            public int getNum()
            {
                return 2;
            }
            
            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }
        
        // int
        public static class Int extends T
        {
            public Int(Integer num)
            {
                lineNum = num;
            }
            
            @Override
            public String toString()
            {
                return "@int";
            }
            
            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
            
            @Override
            public int getNum()
            {
                return 0;
            }
        }
        
        // int[]
        public static class IntArray extends T
        {
            public IntArray(Integer num)
            {
                lineNum = num;
            }
            
            @Override
            public String toString()
            {
                return "@int[]";
            }
            
            @Override
            public int getNum()
            {
                return 1;
            }
            
            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }
        
    }
    
    // ///////////////////////////////////////////////////
    // dec
    public static class Dec
    {
        public static abstract class T implements ast.Acceptable
        {
            public Integer lineNum; // on which line of the source file this appears
        }
        
        public static class DecSingle extends T
        {
            public Type.T type;
            public String id;
            
            public DecSingle(Type.T type, String id)
            {
                this.type = type;
                this.id = id;
            }
            
            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }
    }
    
    // /////////////////////////////////////////////////////////
    // expression
    public static class Exp
    {
        public static abstract class T implements ast.Acceptable
        {
            public T(Integer num){
                this.lineNum=num;
            }
            public Integer lineNum; // on which line of the source file this appears
        }
        
        // +
        public static class Add extends T
        {
            public T left;
            public T right;
            
            public Add(Integer num, T left, T right)
            {
                super(num);
                this.left = left;
                this.right = right;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // and
        public static class And extends T
        {
            public T left;
            public T right;
            
            public And(Integer num, T left, T right)
            {
                super(num);
                this.left = left;
                this.right = right;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // ArraySelect
        public static class ArraySelect extends T
        {
            public T array;
            public T index;
            
            public ArraySelect(Integer num, T array, T index)
            {
                super(num);
                this.array = array;
                this.index = index;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // Call
        public static class Call extends T
        {
            public T exp;
            public String id;
            public java.util.LinkedList<T> args;
            public String type; // type of first field "exp"
            public java.util.LinkedList<Type.T> at; // arg's type
            public Type.T rt;
            
            public Call(Integer num, T exp, String id, java.util.LinkedList<T> args)
            {
                super(num);
                this.exp = exp;
                this.id = id;
                this.args = args;
                this.type = null;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // False
        public static class False extends T
        {
            public False(Integer num)
            {
                super(num);
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // Id
        public static class Id extends T
        {
            public String id; // name of the id
            public Type.T type; // type of the id
            public boolean isField; // whether or not this is a class field
            public boolean isLocal;
            
            public Id(Integer num, String id)
            {
                super(num);
                this.id = id;
                this.type = null;
                this.isField = false;
            }
            
            public Id(int num, String id, Type.T type, boolean isField)
            {
                super(num);
                this.id = id;
                this.type = type;
                this.isField = isField;
                this.isLocal = false;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // length
        public static class Length extends T
        {
            public T array;
            
            public Length(Integer num, T array)
            {
                super(num);
                this.array = array;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // <
        public static class Lt extends T
        {
            public T left;
            public T right;
            
            public Lt(Integer num, T left, T right)
            {
                super(num);
                this.left = left;
                this.right = right;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // new int [e]
        public static class NewIntArray extends T
        {
            public T exp;
            
            public NewIntArray(Integer num, T exp)
            {
                super(num);
                this.exp = exp;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // new A();
        public static class NewObject extends T
        {
            public String id;
            
            public NewObject(Integer num, String id)
            {
                super(num);
                this.id = id;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // !
        public static class Not extends T
        {
            public T exp;
            
            public Not(Integer num, T exp)
            {
                super(num);
                this.exp = exp;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // number
        public static class Num extends T
        {
            public int num;
            
            public Num(Integer num, int number)
            {
                super(num);
                this.num = number;
            }
            

            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // -
        public static class Sub extends T
        {
            public T left;
            public T right;
            
            public Sub(Integer num, T left, T right)
            {
                super(num);
                this.left = left;
                this.right = right;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // this
        public static class This extends T
        {
            public This(Integer num)
            {
                super(num);
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // *
        public static class Times extends T
        {
            public T left;
            public T right;
            
            public Times(Integer num, T left, T right)
            {
                super(num);
                this.left = left;
                this.right = right;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
        // True
        public static class True extends T
        {
            public True(Integer num)
            {
                super(num);
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
    }// end of expression
    
    // /////////////////////////////////////////////////////////
    // statement
    public static class Stm
    {
        public static abstract class T implements ast.Acceptable
        {
            public Integer lineNum; // on which line of the source file this appears
        }
        
        // assign
        public static class Assign extends T
        {
            public String id;
            public Exp.T exp;
            public Type.T type; // type of the id
            public boolean isField;
            public boolean isLocal;
            
            public Assign(String id, Exp.T exp)
            {
                this.id = id;
                this.exp = exp;
                this.type = null;
                this.isField = false;
                this.isLocal = false;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
            }
        }
        
        // assign-array
        public static class AssignArray extends T
        {
            public String id;
            public Exp.T index;
            public Exp.T exp;
            public boolean isField;
            public boolean isLocal;
            
            public AssignArray(String id, Exp.T index, Exp.T exp)
            {
                this.id = id;
                this.index = index;
                this.exp = exp;
                this.isField = false;
                this.isLocal = false;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
            }
        }
        
        // block
        public static class Block extends T
        {
            public java.util.LinkedList<T> stms;
            
            public Block(java.util.LinkedList<T> stms)
            {
                this.stms = stms;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
            }
        }
        
        // if
        public static class If extends T
        {
            public Exp.T condition;
            public T thenn;
            public T elsee;
            
            public If(Exp.T condition, T thenn, T elsee)
            {
                this.condition = condition;
                this.thenn = thenn;
                this.elsee = elsee;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
            }
        }
        
        // Print
        public static class Print extends T
        {
            public Exp.T exp;
            
            public Print(Exp.T exp)
            {
                this.exp = exp;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
            }
        }
        
        // while
        public static class While extends T
        {
            public Exp.T condition;
            public T body;
            
            public While(Exp.T condition, T body)
            {
                this.condition = condition;
                this.body = body;
            }
            
            @Override
            public void accept(ast.Visitor v)
            {
                v.visit(this);
            }
        }
        
    }// end of statement
    
    // /////////////////////////////////////////////////////////
    // method
    public static class Method
    {
        public static abstract class T implements ast.Acceptable
        {
        }
        
        public static class MethodSingle extends T
        {
            public Type.T retType;
            public String id;
            public LinkedList<Dec.T> formals;
            public LinkedList<Dec.T> locals;
            public LinkedList<Stm.T> stms;
            public Exp.T retExp;
            
            public MethodSingle(Type.T retType, String id,
                    LinkedList<Dec.T> formals, LinkedList<Dec.T> locals,
                    LinkedList<Stm.T> stms, Exp.T retExp)
            {
                this.retType = retType;
                this.id = id;
                this.formals = formals;
                this.locals = locals;
                this.stms = stms;
                this.retExp = retExp;
            }
            
            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }
    }
    
    // class
    public static class Class
    {
        public static abstract class T implements ast.Acceptable
        {
        }
        
        public static class ClassSingle extends T
        {
            public String id;
            public String extendss; // null for non-existing "extends"
            public java.util.LinkedList<Dec.T> decs;
            public java.util.LinkedList<ast.Ast.Method.T> methods;
            
            public ClassSingle(String id, String extendss,
                    java.util.LinkedList<Dec.T> decs,
                    java.util.LinkedList<ast.Ast.Method.T> methods)
            {
                this.id = id;
                this.extendss = extendss;
                this.decs = decs;
                this.methods = methods;
            }
            
            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
            }
        }
    }
    
    // main class
    public static class MainClass
    {
        public static abstract class T implements ast.Acceptable
        {
        }
        
        public static class MainClassSingle extends T
        {
            public String id;
            public String arg;
            public Stm.T stm;
            
            public MainClassSingle(String id, String arg, Stm.T stm)
            {
                this.id = id;
                this.arg = arg;
                this.stm = stm;
            }
            
            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
    }
    
    // whole program
    public static class Program
    {
        public static abstract class T implements ast.Acceptable
        {
        }
        
        public static class ProgramSingle extends T
        {
            public MainClass.T mainClass;
            public LinkedList<Class.T> classes;
            
            public ProgramSingle(MainClass.T mainClass, LinkedList<Class.T> classes)
            {
                this.mainClass = mainClass;
                this.classes = classes;
            }
            
            @Override
            public void accept(Visitor v)
            {
                v.visit(this);
                return;
            }
        }
        
    }
}
