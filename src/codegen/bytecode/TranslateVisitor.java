package codegen.bytecode;

import java.util.Hashtable;
import java.util.LinkedList;

import ast.Ast.Exp.T;
import util.Label;
import codegen.bytecode.Ast.Class;
import codegen.bytecode.Ast.Class.ClassSingle;
import codegen.bytecode.Ast.Dec;
import codegen.bytecode.Ast.Dec.DecSingle;
import codegen.bytecode.Ast.MainClass;
import codegen.bytecode.Ast.MainClass.MainClassSingle;
import codegen.bytecode.Ast.Method;
import codegen.bytecode.Ast.Method.MethodSingle;
import codegen.bytecode.Ast.Program;
import codegen.bytecode.Ast.Program.ProgramSingle;
import codegen.bytecode.Ast.Stm;
import codegen.bytecode.Ast.Stm.Aload;
import codegen.bytecode.Ast.Stm.Areturn;
import codegen.bytecode.Ast.Stm.Astore;
import codegen.bytecode.Ast.Stm.Goto;
import codegen.bytecode.Ast.Stm.Ificmplt;
import codegen.bytecode.Ast.Stm.Ifne;
import codegen.bytecode.Ast.Stm.Iload;
import codegen.bytecode.Ast.Stm.Imul;
import codegen.bytecode.Ast.Stm.Iadd;
import codegen.bytecode.Ast.Stm.Invokevirtual;
import codegen.bytecode.Ast.Stm.Ireturn;
import codegen.bytecode.Ast.Stm.Istore;
import codegen.bytecode.Ast.Stm.Isub;
import codegen.bytecode.Ast.Stm.LabelJ;
import codegen.bytecode.Ast.Stm.Ldc;
import codegen.bytecode.Ast.Stm.New;
import codegen.bytecode.Ast.Stm.Print;
import codegen.bytecode.Ast.Type;
import codegen.bytecode.Ast.Type.*;

// Given a Java ast, translate it into Java bytecode.

public class TranslateVisitor implements ast.Visitor
{
    private String classId;
    private int index;
    private Hashtable<String, Integer> indexTable;
    private Type.T type; // type after translation
    private Dec.T dec;
    private LinkedList<Stm.T> stms;
    private Method.T method;
    private Class.T classs;
    private MainClass.T mainClass;
    private boolean inAssign;
    private LinkedList<Stm.T> assignList;
    public Program.T program;
    
    public TranslateVisitor()
    {
        this.classId = null;
        this.indexTable = new Hashtable<>();
        this.type = null;
        this.dec = null;
        this.stms = new LinkedList<Stm.T>();
        this.method = null;
        this.classs = null;
        this.mainClass = null;
        this.program = null;
        this.inAssign = false;
        this.assignList = null;
    }
    
    public static int getLineNumber() {
        
        int line=Thread.currentThread().getStackTrace()[2].getLineNumber();
        
        return line;
        
    }
    
    private void emit(Stm.T s)
    {
        if (this.inAssign)
            this.assignList.add(s);
        else
            this.stms.add(s);
    }
    
    
    
    // /////////////////////////////////////////////////////
    // expressions
    // public Add(T left, T right)
    @Override
    public void visit(ast.Ast.Exp.Add e)
    {
        e.left.accept(this);
        e.right.accept(this);
        emit(new Iadd());
    }
    
    @Override
    public void visit(ast.Ast.Exp.And e)
    {
        Label left = new Label(), right = new Label(), out = new Label();
        e.left.accept(this);
        emit(new Stm.Ifne(left));
        emit(new Stm.Ldc(0));
        emit(new Stm.Goto(out));

        emit(new LabelJ(left));
        e.right.accept(this);
        emit(new Stm.Ifne(right));
        emit(new Stm.Ldc(0));
        emit(new Stm.Goto(out));

        emit(new LabelJ(right));
        emit(new Stm.Ldc(1));
        emit(new LabelJ(out));
    }
    
    @Override
    public void visit(ast.Ast.Exp.ArraySelect e)
    {
        e.array.accept(this);
        e.index.accept(this);
        emit(new Stm.Iaload());
    }
    
    //public Call(T exp, String id, java.util.LinkedList<T> args)
    
    //Invokevirtual(String f, String c, LinkedList<Type.T> at, Type.T rt)
    @Override
    public void visit(ast.Ast.Exp.Call e)
    {
        e.exp.accept(this);
        if(e.args!=null)
            for (ast.Ast.Exp.T x : e.args) {
                x.accept(this);
            }
        e.rt.accept(this);
        Type.T rt = this.type;
        LinkedList<Type.T> at = new LinkedList<Type.T>();
        for (ast.Ast.Type.T t : e.at) {
            t.accept(this);
            at.add(this.type);
        }
        emit(new Invokevirtual(e.id, e.type, at, rt));
        return;
    }
    
    @Override
    public void visit(ast.Ast.Exp.False e)
    {
        emit(new Stm.Ldc(0));
    }
    
    @Override
    public void visit(ast.Ast.Exp.Id e)
    {

        if (e.isField) {
            e.type.accept(this);
            Type.T type = this.type;
            emit(new Stm.Getfield(this.classId, e.id, type));
        } else {
            int index = this.indexTable.get(e.id);
            ast.Ast.Type.T type = e.type;
            if (type.getNum() > 0)// a reference
                emit(new Stm.Aload(index));
            else
                emit(new Stm.Iload(index));
        }
        return;
    }
    
    @Override
    public void visit(ast.Ast.Exp.Length e)
    {
        e.array.accept(this);
        emit(new Stm.Arraylength());
    }
    
    //public Lt(T left, T right)// tl=3   fl=4   el=5
    @Override
    public void visit(ast.Ast.Exp.Lt e)
    {
        Label tl = new Label(), fl = new Label(), el = new Label();
        e.left.accept(this);
        e.right.accept(this);
        emit(new Ificmplt(tl));
        emit(new LabelJ(fl));
        emit(new Ldc(0));
        emit(new Goto(el));
        emit(new LabelJ(tl));
        emit(new Ldc(1));
        emit(new Goto(el));
        emit(new LabelJ(el));
        
//        e.left.accept(this);
//        e.right.accept(this);
//        emit(new Ificmplt(g_label));
//        g_label=null;
        
        return;
    }
    
    
    @Override
    public void visit(ast.Ast.Exp.NewIntArray e)
    {
        e.exp.accept(this);
        emit(new Stm.NewarrayInt());
    }
    
    @Override
    public void visit(ast.Ast.Exp.NewObject e)
    {
        emit(new New(e.id));
        return;
    }
    
    @Override
    public void visit(ast.Ast.Exp.Not e)
    {
        System.out.println("NOT ->"+getLineNumber());
    }
    
    @Override
    public void visit(ast.Ast.Exp.Num e)
    {
        emit(new Ldc(e.num));
        return;
    }
    
    @Override
    public void visit(ast.Ast.Exp.Sub e)
    {
        e.left.accept(this);
        e.right.accept(this);
        emit(new Isub());
        return;
    }
    
    @Override
    public void visit(ast.Ast.Exp.This e)
    {
        emit(new Aload(0));
        return;
    }
    
    @Override
    public void visit(ast.Ast.Exp.Times e)
    {
        e.left.accept(this);
        e.right.accept(this);
        emit(new Imul());
        return;
    }
    
    @Override
    public void visit(ast.Ast.Exp.True e)
    {
        System.out.println("imple True  ->"+getLineNumber());
    }
    
    
    //public Assign(String id, Exp.T exp)
    // ///////////////////////////////////////////////////
    // statements
    @Override
    public void visit(ast.Ast.Stm.Assign s)
    {
        if (s.isField) {
            this.inAssign = true;//capture all emitted stm
            this.assignList = new LinkedList<Stm.T>();
            s.exp.accept(this);
            s.type.accept(this);
            Type.T type = this.type;
            this.inAssign = false;
            emit( new Stm.Putfield(this.classId, s.id, type, this.assignList) );
            this.assignList = null;
        } else {
            s.exp.accept(this);
            int index = this.indexTable.get(s.id);
            ast.Ast.Type.T type = s.type;
            
            if (type.getNum() > 0)
                emit(new Astore(index));
            else
                emit(new Istore(index));
        }
        return;
    }
    
    @Override
    public void visit(ast.Ast.Stm.AssignArray s)
    {
        if (s.isField) {
            emit(new Stm.Getfield(this.classId, s.id, new Type.IntArray()));
        } else {
            int index = this.indexTable.get(s.id);
            emit(new Stm.Aload(index));
        }
        s.index.accept(this);
        s.exp.accept(this);
        emit(new Stm.Iastore());
 
    }
    
    @Override
    public void visit(ast.Ast.Stm.Block s)
    {
        for (ast.Ast.Stm.T stm : s.stms) {
            stm.accept(this);
        }
    }
    
    Label g_label;
    
    
    @Override
    public void visit(ast.Ast.Stm.If s)
    {
/*        Label tl = new Label(), fl = new Label(), el = new Label();
        s.condition.accept(this);
        
        emit(new Ifne(tl));
        emit(new LabelJ(fl));
        s.elsee.accept(this);
        emit(new Goto(el));
        emit(new LabelJ(tl));
        s.thenn.accept(this);
        emit(new Goto(el));
        emit(new LabelJ(el));*/
        
        Label succLabel = new Label(),
                endLabel  = new Label(); 
        g_label=succLabel;
        s.condition.accept(this);
        s.elsee.accept(this);
        emit(new Goto(endLabel));
        emit(new LabelJ(succLabel));
        s.thenn.accept(this);
        emit(new LabelJ(endLabel));
        
        return;
    }
    
    @Override
    public void visit(ast.Ast.Stm.Print s)
    {
        s.exp.accept(this);
        emit(new Print());
        return;
    }
    
    //public While(Exp.T condition, T body)
    @Override
    public void visit(ast.Ast.Stm.While s)
    {
        Label origLabel  = new Label(),  
                succlLabel = new Label(), 
                endLabel   = new Label();
        g_label=succlLabel;
        emit(new LabelJ(origLabel));
        
        s.condition.accept(this);
        emit(new Goto(endLabel));
        emit(new LabelJ(succlLabel));
        s.body.accept(this);
        emit(new Goto(origLabel));
        
        emit(new LabelJ(endLabel));
        
        return;
    }
    
    // type
    @Override
    public void visit(ast.Ast.Type.Boolean t)
    {
        this.type = new Type.Int(); 
    }
    
    // public ClassType(String id)
    @Override
    public void visit(ast.Ast.Type.ClassType t)
    {
        this.type = new ClassType(t.id);     
    }
    
    @Override
    public void visit(ast.Ast.Type.Int t)
    {
        this.type = new Int();
    }
    
    @Override
    public void visit(ast.Ast.Type.IntArray t)
    {
        this.type = new IntArray(); 
        
    }
    
    // dec
    @Override
    public void visit(ast.Ast.Dec.DecSingle d)
    {
        d.type.accept(this);
        this.dec = new DecSingle(this.type, d.id);
        this.indexTable.put(d.id, index++);
        return;
    }
    
    // method
    @Override
    public void visit(ast.Ast.Method.MethodSingle m)
    {
        // record, in a hash table, each var's index
        // this index will be used in the load store operation
        this.index = 1;
        this.indexTable = new Hashtable<String, Integer>();
        
        m.retType.accept(this);
        Type.T newRetType = this.type;
        LinkedList<Dec.T> newFormals = new LinkedList<Dec.T>();
        for (ast.Ast.Dec.T d : m.formals) {
            d.accept(this);
            newFormals.add(this.dec);
        }
        LinkedList<Dec.T> locals = new java.util.LinkedList<Dec.T>();
        for (ast.Ast.Dec.T d : m.locals) {
            d.accept(this);
            locals.add(this.dec);
        }
        this.stms = new LinkedList<Stm.T>();
        for (ast.Ast.Stm.T s : m.stms) {
            s.accept(this);
        }
        
        // return statement is specially treated
        m.retExp.accept(this);
        
        if (m.retType.getNum() > 0)
            emit(new Areturn());
        else
            emit(new Ireturn());
        
        this.method = new MethodSingle(newRetType, m.id, this.classId, newFormals,
                locals, this.stms, 0, this.index);
        
        return;
    }
    
    // class
    @Override
    public void visit(ast.Ast.Class.ClassSingle c)
    {
        this.classId = c.id;
        LinkedList<Dec.T> newDecs = new LinkedList<Dec.T>();
        for (ast.Ast.Dec.T dec : c.decs) {
            dec.accept(this);
            newDecs.add(this.dec);
        }
        LinkedList<Method.T> newMethods = new LinkedList<Method.T>();
        for (ast.Ast.Method.T m : c.methods) {
            m.accept(this);
            newMethods.add(this.method);
        }
        this.classs = new ClassSingle(c.id, c.extendss, newDecs, newMethods);
        return;
    }
    
    // main class
    @Override
    public void visit(ast.Ast.MainClass.MainClassSingle c)
    {
        c.stm.accept(this);
        this.mainClass = new MainClassSingle(c.id, c.arg, this.stms);
        this.stms = new LinkedList<Stm.T>();
        return;
    }
    
    // program
    @Override
    public void visit(ast.Ast.Program.ProgramSingle p)
    {
        // do translations
        p.mainClass.accept(this);
        
        LinkedList<Class.T> newClasses = new LinkedList<Class.T>();
        for (ast.Ast.Class.T classes : p.classes) {
            classes.accept(this);
            newClasses.add(this.classs);
        }
        this.program = new ProgramSingle(this.mainClass, newClasses);
        return;
    }
    
}
