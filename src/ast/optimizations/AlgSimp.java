package ast.optimizations;

import ast.Ast.Class;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.And;
import ast.Ast.Exp.ArraySelect;
import ast.Ast.Exp.Call;
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
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;

// Algebraic simplification optimizations on an AST.

public class AlgSimp implements ast.Visitor
{
    private Class.T newClass;
    private MainClass.T mainClass;
    public Program.T program;
    private Method.T method;
    private Exp.T exp;
    private Stm.T stm;
    public Exp.T retExp;
    
    public AlgSimp()
    {
        this.newClass = null;
        this.mainClass = null;
        this.program = null;
        this.method = null;
        this.exp = null;
        this.stm = null;
        this.retExp = null;
    }
    
    // //////////////////////////////////////////////////////
    // 
    public String genId()
    {
        return util.Temp.next();
    }
    
    // /////////////////////////////////////////////////////
    // expressions
    @Override
    public void visit(Add e)
    {
        e.left.accept(this);
        Exp.T left = this.exp == null ? e.left : this.exp;
        this.exp = null;

        e.right.accept(this);
        Exp.T right = this.exp == null ? e.right : this.exp;
        this.exp = null;
        if (left.toString().startsWith("ast.Ast$Exp$Num@") && right.toString().startsWith("ast.Ast$Exp$Id@")) {
            if (((Exp.Num) e.left).num == 0) {
                this.exp = e.right;
                ast.optimizations.Main.modified();
            }
        } else if (left.toString().startsWith("ast.Ast$Exp$Id@") && right.toString().startsWith("ast.Ast$Exp$Num@")) {
            if (((Exp.Num) e.right).num == 0) {
                this.exp = e.left;
                ast.optimizations.Main.modified();
            }
        }
        return;
    }
    
    @Override
    public void visit(And e)
    {
        e.left.accept(this);
        Exp.T left = this.exp == null ? e.left : this.exp;
        this.exp = null;

        e.right.accept(this);
        Exp.T right = this.exp == null ? e.right : this.exp;
        this.exp = null;
        
        this.exp = new Exp.And(e.lineNum, left, right);

        return;
    }
    
    @Override
    public void visit(ArraySelect e)
    {

        e.array.accept(this);
        Exp.T array = this.exp == null ? e.array : this.exp;
        this.exp = null;

        e.index.accept(this);
        Exp.T index = this.exp == null ? e.index : this.exp;
        this.exp = null;

        this.exp = new Exp.ArraySelect(e.lineNum, array, index);
        return;
    }
    
    @Override
    public void visit(Call e)
    {
        e.exp.accept(this);
        Exp.T exp = this.exp == null ? e.exp : this.exp;
        Exp.Call call = new Exp.Call(e.lineNum, exp, e.id, e.args);
        call.rt = e.rt;
        call.at = e.at;
        call.type = e.type;
        this.exp = call;
        return;
    }
    
    @Override
    public void visit(False e)
    {
        this.exp = new Exp.False(e.lineNum);
    }
    
    @Override
    public void visit(Id e)
    {
        return;
    }
    
    @Override
    public void visit(Length e)
    {
        e.array.accept(this);
        Exp.T array = this.exp == null ? e.array : this.exp;
        this.exp = new Exp.Length(e.lineNum, array);
        return;
    }
    
    @Override
    public void visit(Lt e)
    {
        e.left.accept(this);
        Exp.T left = this.exp == null ? e.left : this.exp;
        this.exp = null;

        e.right.accept(this);
        Exp.T right = this.exp == null ? e.right : this.exp;
        this.exp = null;

        this.exp = new Exp.Lt(e.lineNum, left, right);
        return;
    }
    
    @Override
    public void visit(NewIntArray e)
    {
        e.exp.accept(this);
        Exp.T exp = this.exp == null ? e.exp : this.exp;
        this.exp = new Exp.NewIntArray(e.lineNum, exp);
        return;
    }
    
    @Override
    public void visit(NewObject e)
    {
        return;
    }
    
    @Override
    public void visit(Not e)
    {
        e.exp.accept(this);
        Exp.T newExp = this.exp == null ? e.exp : this.exp;
        this.exp = new Exp.Not(e.lineNum, newExp);
        return;
    }
    
    @Override
    public void visit(Num e)
    {
        return;
    }
    
    @Override
    public void visit(Sub e)
    {
        e.left.accept(this);
        Exp.T left = this.exp == null ? e.left : this.exp;
        this.exp = null;

        e.right.accept(this);
        Exp.T right = this.exp == null ? e.right : this.exp;
        this.exp = null;

        if (left.toString().startsWith("ast.Ast$Exp$Id@") && right.toString().startsWith("ast.Ast$Exp$Num@")) {
            if (((Exp.Num) e.right).num == 0) {
                this.exp = e.left;
                ast.optimizations.Main.modified();
            }
        }
        return;
    }
    
    @Override
    public void visit(This e)
    {
        return;
    }
    
    @Override
    public void visit(Times e)
    {
        e.left.accept(this);
        Exp.T left = this.exp == null ? e.left : this.exp;
        this.exp = null;

        e.right.accept(this);
        Exp.T right = this.exp == null ? e.right : this.exp;
        this.exp = null;

        if (left.toString().startsWith("ast.Ast$Exp$Num@") && right.toString().startsWith("ast.Ast$Exp$Id@")) {
            if (((Exp.Num)e.left).num == 0) {
                this.exp = new Exp.Num(e.lineNum, 0);
                ast.optimizations.Main.modified();
            } else if (((Exp.Num) e.left).num == 1) {
                this.exp = e.right;
                ast.optimizations.Main.modified();
            } else if (((Exp.Num) e.left).num == 2) {
                this.exp = new Exp.Add(e.lineNum, right, right);
                ast.optimizations.Main.modified();
            }
        } else if (left.toString().startsWith("ast.Ast$Exp$@") && right.toString().startsWith("ast.Ast$Exp$Num@")) {
            if (((Exp.Num) e.right).num == 0) {
                this.exp = new Exp.Num(e.lineNum, 0);
                ast.optimizations.Main.modified();
            } else if (((Exp.Num) e.right).num == 1) {
                this.exp = e.left;
                ast.optimizations.Main.modified();
            } else if (((Exp.Num) e.right).num == 2) {
                this.exp = new Exp.Add(e.lineNum, left, left);
                ast.optimizations.Main.modified();
            }
        }
        
        return;
    }
    
    @Override
    public void visit(True e)
    {
        this.exp = new Exp.True(e.lineNum);
        return;
    }
    
    /////////////////////////////////////////
    // statements
    @Override
    public void visit(Assign s)
    {
        s.exp.accept(this);
        Exp.T newExp = this.exp == null ? s.exp : this.exp;
        this.exp = null;

        this.stm = new Stm.Assign(s.id, newExp, s.type);
        return;
    }
    
    @Override
    public void visit(AssignArray s)
    {
        s.index.accept(this);
        Exp.T newIndex = this.exp == null ? s.index : this.exp;
        this.exp = null;

        s.exp.accept(this);
        Exp.T newExp = this.exp == null ? s.exp : this.exp;
        this.exp = null;

        this.stm = new Stm.AssignArray(s.id, newIndex, newExp);
        return;
    }
    
    @Override
    public void visit(Block s)
    {  
        java.util.LinkedList<Stm.T> newStms = new java.util.LinkedList<Stm.T>();
        for (Stm.T t : s.stms) {
            t.accept(this);
            newStms.add(this.stm);
        }
        this.stm = new Stm.Block(newStms);
        return;
    }
    
    @Override
    public void visit(If s)
    {
        s.condition.accept(this);
        Exp.T newCondition = this.exp == null ? s.condition : this.exp;
        this.exp = null;

        s.thenn.accept(this);
        Stm.T newThenn = this.stm == null ? s.thenn : this.stm;
        this.stm = null;

        s.elsee.accept(this);
        Stm.T elsee = this.stm == null ? s.elsee : this.stm;
        this.stm = null;

        this.stm = new Stm.If(newCondition, newThenn, elsee);
        return;
    }
    
    @Override
    public void visit(Print s)
    {
        s.exp.accept(this);
        this.stm = new Stm.Print(this.exp == null ? s.exp : this.exp);
        this.exp = null;
        return;
    }
    
    @Override
    public void visit(While s)
    {
        s.condition.accept(this);

        Exp.T newCondition = this.exp == null ? s.condition : this.exp;
        this.exp = null;

        s.body.accept(this);
        Stm.T newBody = this.stm == null ? s.body : this.stm;
        this.stm = null;

        this.stm = new Stm.While(newCondition, newBody);
        return;
    }
    
    // type
    @Override
    public void visit(Boolean t)
    {
    }
    
    @Override
    public void visit(ClassType t)
    {
    }
    
    @Override
    public void visit(Int t)
    {
    }
    
    @Override
    public void visit(IntArray t)
    {
    }
    
    // dec
    @Override
    public void visit(DecSingle d)
    {
        return;
    }
    
    // method
    @Override
    public void visit(MethodSingle m)
    {
        java.util.LinkedList<Stm.T> newStms = new java.util.LinkedList<Stm.T>();
        for (Stm.T s : m.stms) {
            s.accept(this);
            newStms.add(this.stm);
            this.stm = null;
        }
        m.retExp.accept(this);
        this.method = new MethodSingle(m.retType, m.id, m.formals, m.locals, newStms, this.exp == null ? m.retExp : this.exp);
        this.exp = null;
        return;
    }
    
    // class
    @Override
    public void visit(ClassSingle c)
    {

        java.util.LinkedList<Method.T> newMethods = new java.util.LinkedList<Method.T>();
        for (Method.T m : c.methods) {
            m.accept(this);
            newMethods.add(this.method);
        }
        this.newClass = new ClassSingle(c.id, c.extendss, c.decs, newMethods);

        return;
    }
    
    // main class
    @Override
    public void visit(MainClassSingle c)
    {
        c.stm.accept(this);
        this.stm = null;
        this.mainClass = new MainClassSingle(c.id, c.arg, c.stm);
        return;
    }
    
    // program
    @Override
    public void visit(ProgramSingle p)
    {
        
        // You should comment out this line of code:
        //this.program = p;
        p.mainClass.accept(this);

        java.util.LinkedList<ast.Ast.Class.T> newClasses = new java.util.LinkedList<ast.Ast.Class.T>();
        for (ast.Ast.Class.T c : p.classes) {
            c.accept(this);
            newClasses.add(this.newClass);
        }
        
        this.program = new ProgramSingle(this.mainClass, newClasses);

        if (control.Control.isTracing("ast.AlgSimp")) {
            System.out.println("before optimization:");
            ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
            p.accept(pp);
            System.out.println("after optimization:");
            this.program.accept(pp);
        }
        return;
    }
}
