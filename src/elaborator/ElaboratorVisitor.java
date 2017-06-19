package elaborator;

import java.util.LinkedList;

import ast.Ast.Class;
import ast.Ast.Exp.False;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.Length;
import ast.Ast.Exp.Lt;
import ast.Ast.Exp.NewIntArray;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Not;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Times;
import ast.Ast.Exp.True;
import ast.Ast.MainClass;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.And;
import ast.Ast.Exp.ArraySelect;
import ast.Ast.Exp.Call;
import ast.Ast.Method;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type;
import ast.Ast.Type.ClassType;
import control.Control.ConAst;

public class ElaboratorVisitor implements ast.Visitor
{
    public ClassTable classTable; // symbol table for class
    public MethodTable methodTable; // symbol table for each method
    public String currentClass; // the class name being elaborated
    public Type.T type; // type of the expression being elaborated
    
    public ElaboratorVisitor()
    {
        this.classTable = new ClassTable();
        this.methodTable = new MethodTable();
        this.currentClass = null;
        this.type = null;
    }
    
    private void error(int line, String errMsg)
    {
        if ( errMsg.equals("") ) {
            errMsg = "Type mismatch.";
        }
        System.out.println("Line "+ String.valueOf(line) + " Error:" + errMsg);
        System.exit(1);

        return;
    }
    
    public int getLineNumber() {
        return this.type.lineNum;
    }
    
    // /////////////////////////////////////////////////////
    // expressions
    @Override
    public void visit(Add e) // Add(T left, T right)
    {
        Type.T type;
        e.left.accept(this);
        type = this.type;
        e.right.accept(this);
        
        if(!type.toString().equals(this.type.toString())){
            error(getLineNumber(), "");
        }
        if(!this.type.toString().equals("@int")) {
            error(getLineNumber(), "There is no such type.");
        }

        return;
    }
    
    @Override
    public void visit(And e) //And(T left, T right)
    {
        Type.T type;  
        e.left.accept(this);
        type = this.type;
        e.right.accept(this);
        if(!this.type.toString().equals("@boolean")|| !this.type.toString().equals(type.toString()))
            error(getLineNumber(),"");
        
        return;
    }
    
    @Override
    public void visit(ArraySelect e) //ArraySelect(T array, T index)
    {
        Type.T type;
        e.array.accept(this);
        type = this.type;
        
        e.index.accept(this);
        if(!this.type.toString().equals("@int")){
            error(getLineNumber(), "The index of Arrey must be INT.");
        }
        this.type = new Type.Int(e.lineNum);
        return;
    }
    
    @Override
    public void visit(Call e)
    {
        Type.T left_type;
        Type.ClassType type = null;
        
        e.exp.accept(this);
        left_type = this.type;
        if (left_type instanceof ClassType) {
            type = (ClassType) left_type;
            e.type = type.id;
        } else {
            error( getLineNumber(), "");
        }
        
        MethodType mty = this.classTable.getm(type.id, e.id);
        java.util.LinkedList<Type.T> argsty = new LinkedList<Type.T>();
        for (Exp.T a : e.args) {
            a.accept(this);
            argsty.addLast(this.type);
        }
        if (mty.argsType.size() != argsty.size()) {
            error( getLineNumber(), "");
        }
        for (int i = 0; i < argsty.size(); i++) {
            Dec.DecSingle dec = (Dec.DecSingle) mty.argsType.get(i);
            if ( !dec.type.toString().equals(argsty.get(i).toString())) {
                error( getLineNumber(), "");
            }
        }
        this.type = mty.retType;
        e.at = argsty;
        e.rt = this.type;
        return;
    }
    
    @Override
    public void visit(False e)
    {
        this.type = new Type.Boolean(e.lineNum);
        return;
    }
    
    @Override
    public void visit(Id e)
    {
        // first look up the id in method table
        Type.T type = this.methodTable.get(e.id);
        // if search failed, then s.id must be a class field.
        if (type == null) {
            type = this.classTable.get(this.currentClass, e.id);
            // mark this id as a field id, this fact will be
            // useful in later phase.
            e.isField = true;
        }
        if (type == null) {
            error( getLineNumber(), "");
        }
        this.type = type;
        // record this type on this node for future use.
        e.type = type;
        return;
    }
    
    @Override
    public void visit(Length e)
    {
        e.array.accept(this);
        this.type = new Type.Int(e.lineNum); 
    }
    
    @Override
    public void visit(Lt e)
    {
        e.left.accept(this);
        Type.T ty = this.type;
        e.right.accept(this);
        if (!this.type.toString().equals(ty.toString())) {
            error( getLineNumber(), "");
        }
        this.type = new Type.Boolean(e.lineNum);
        return;
    }
    
    @Override
    public void visit(NewIntArray e)
    {
        e.exp.accept(this);
        this.type = new Type.IntArray(e.lineNum);
        return;
    }
    
    @Override
    public void visit(NewObject e)
    {
        this.type = new Type.ClassType(e.lineNum, e.id);
        return;
    }
    
    @Override
    public void visit(Not e)
    {
        e.exp.accept(this);
        this.type = new Type.Boolean(e.lineNum);
    }
    
    @Override
    public void visit(Num e)
    {
        this.type = new Type.Int(e.lineNum);
        return;
    }
    
    @Override
    public void visit(Sub e)
    {
        e.left.accept(this);
        Type.T leftty = this.type;
        e.right.accept(this);
        if (!this.type.toString().equals(leftty.toString())) {
            error( getLineNumber(), "");
        }
        this.type = new Type.Int(e.lineNum);
        return;
    }
    
    @Override
    public void visit(This e)
    {
        this.type = new Type.ClassType(e.lineNum, this.currentClass);
        return;
    }
    
    @Override
    public void visit(Times e)
    {
        e.left.accept(this);
        Type.T leftty = this.type;
        e.right.accept(this);
        if (!this.type.toString().equals(leftty.toString())) {
            error( getLineNumber(), "");
        }
        this.type = new Type.Int(e.lineNum);
        return;
    }
    
    @Override
    public void visit(True e)
    {
        this.type = new Type.Boolean(e.lineNum);
        return;
    }
    
    // statements
    @Override
    public void visit(Assign s)
    {
        // first look up the id in method table
        Type.T type = this.methodTable.get(s.id);
        // if search failed, then s.id must
        if (type == null) {
            type = this.classTable.get(this.currentClass, s.id);
        }
        if (type == null) {
            error( getLineNumber(), "");
        }
        s.exp.accept(this);
        s.type = type;
        if ( !this.type.toString().equals(type.toString()) ) {
            error( getLineNumber(), "");
        }
        return;
    }
    
    @Override
    public void visit(AssignArray s) //AssignArray(String id, Exp.T index, Exp.T exp)
    {
        Type.T type=this.methodTable.get(s.id);
        
        if(type == null) {
            type = this.classTable.get(this.currentClass, s.id);
        }
        if(type == null) {
            error( getLineNumber(), "");
        }
        
        s.index.accept(this);
        if(!this.type.toString().equals("@int")) {
            error( getLineNumber(), "\"The index of Arrey must be INT.\"");
        }
        
        s.exp.accept(this);
        if(type.toString().equals("@int[]"))
        {
            if(!this.type.toString().equals("@int")) {
                error( getLineNumber(), "");
            }
        }
        return;
    }
    
    @Override
    public void visit(Block s)
    {
        for (Stm.T stm : s.stms) {
            stm.accept(this);
        }
    }
    
    @Override
    public void visit(If s) //If(Exp.T condition, T then, T else)
    {
        s.condition.accept(this);
        if (!this.type.toString().equals("@boolean")) {
            error( getLineNumber(), "");
        }
        s.thenn.accept(this);
        s.elsee.accept(this);
        return;
    }
    
    @Override
    public void visit(Print s)
    {
        s.exp.accept(this);
        if (!this.type.toString().equals("@int")) {
            error( getLineNumber(), "");
        }
        return;
    }
    
    @Override
    public void visit(While s) //While(Exp.T condition, T body)
    {
        s.condition.accept(this);
        
        if(!this.type.toString().equals("@boolean"))
        {
            error( getLineNumber(), "The condition of WHILE must be boolean.");
        }
        s.body.accept(this);   
    }
    
    // type
    @Override
    public void visit(Type.Boolean t)
    {
       error( getLineNumber(), "Boolean type.");
       return;
    }
    
    @Override
    public void visit(Type.ClassType t)
    {
        error( getLineNumber(), "ClassType type.");
        return;
    }
    
    @Override
    public void visit(Type.Int t)
    {
        error( getLineNumber(), "Int type.");
        return;
    }
    
    @Override
    public void visit(Type.IntArray t)
    {
        error( getLineNumber(), "IntArray type.");
        return;
    }
    
    // dec
    @Override
    public void visit(Dec.DecSingle d)
    {
        error( getLineNumber(), "DecSingle type.");
        return;
    }
    
    /*
      method:
      public Type.T retType;
      public String id;
      public LinkedList<Dec.T> formals;
      public LinkedList<Dec.T> locals;
      public LinkedList<Stm.T> stms;
      public Exp.T retExp;
    */
    @Override
    public void visit(Method.MethodSingle m)
    {
        // construct the method table
        this.methodTable.put(m.formals, m.locals);
        
        if (ConAst.elabMethodTable)
            this.methodTable.dump();
        
        for (Stm.T s : m.stms)
            s.accept(this);
        m.retExp.accept(this);
        return;
    }
    
    // class
    @Override
    public void visit(Class.ClassSingle c)
    {
        this.currentClass = c.id;
        
        for (Method.T m : c.methods) {
            m.accept(this);
        }
        return;
    }
    
    // main class
    @Override
    public void visit(MainClass.MainClassSingle c)
    {
        this.currentClass = c.id;
        // "main" has an argument "arg" of type "String[]", but
        // one has no chance to use it. So it's safe to skip it...
        
        c.stm.accept(this);
        return;
    }
    
    // ////////////////////////////////////////////////////////
    // step 1: build class table
    // class table for Main class
    private void buildMainClass(MainClass.MainClassSingle main)
    {
        this.classTable.put(main.id, new ClassBinding(null));
    }
    
    // class table for normal classes
    private void buildClass(ClassSingle c)
    {
        this.classTable.put(c.id, new ClassBinding(c.extendss));
        for (Dec.T dec : c.decs) {
            Dec.DecSingle d = (Dec.DecSingle) dec;
            this.classTable.put(c.id, d.id, d.type);
        }
        for (Method.T method : c.methods) {
            MethodSingle m = (MethodSingle) method;
            this.classTable.put(c.id, m.id, new MethodType(m.retType, m.formals));
        }
    }
    
    // step 1: end
    // ///////////////////////////////////////////////////
    
    // program
    @Override
    public void visit(ProgramSingle p)
    {
        // ////////////////////////////////////////////////
        // step 1: build a symbol table for class (the class table)
        // a class table is a mapping from class names to class bindings
        // classTable: className -> ClassBinding{extends, fields, methods}
        buildMainClass((MainClass.MainClassSingle) p.mainClass);
        for (Class.T c : p.classes) {
            buildClass((ClassSingle) c);
        }
        
        // we can double check that the class table is OK!
        if (control.Control.ConAst.elabClassTable) {
            this.classTable.dump();
        }
        
        // ////////////////////////////////////////////////
        // step 2: elaborate each class in turn, under the class table
        // built above.
        p.mainClass.accept(this);
        for (Class.T c : p.classes) {
            c.accept(this);
        }
        
    }
}
