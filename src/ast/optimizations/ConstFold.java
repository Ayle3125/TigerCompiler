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

// Constant folding optimizations on an AST.

public class ConstFold implements ast.Visitor
{
    private Class.T newClass;
    private MainClass.T mainClass;
    public Program.T program;
    private Method.T methodd;
    private Exp.T expp;
    private Stm.T stmm;
    public Exp.T retExpp;
    
    public ConstFold()
    {
        this.newClass = null;
        this.mainClass = null;
        this.program = null;
        this.methodd = null;
        this.expp = null;
        this.stmm = null;
        this.retExpp = null;
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
        Exp.T left = this.expp == null ? e.left : this.expp;
        this.expp = null;

        e.right.accept(this);
        Exp.T right = this.expp == null ? e.right : this.expp;
        this.expp = null;

        if (left.toString().startsWith("ast.Ast$Exp$Num@") && right.toString().startsWith("ast.Ast$Exp$Num@")) {
            int leftt = ((Exp.Num) left).num;
            int rightt = ((Exp.Num) right).num;
            this.expp = new Exp.Num(e.lineNum ,leftt + rightt);
            ast.optimizations.Main.modified();
        }
        return;
    }
    
    @Override
    public void visit(And e)
    {
        e.left.accept(this);
        Exp.T left = this.expp == null ? e.left : this.expp;
        this.expp = null;

        e.right.accept(this);
        Exp.T right = this.expp == null ? e.right : this.expp;
        this.expp = null;

        if (left.toString().startsWith("@True") && right.toString().startsWith("@True")) {
            this.expp = new Exp.True(e.lineNum);
            ast.optimizations.Main.modified();
        } else if (left.toString().startsWith("@False") && right.toString().startsWith("@True")) {
            this.expp = new Exp.False(e.lineNum);
            ast.optimizations.Main.modified();
        } else if (left.toString().startsWith("@True") && right.toString().startsWith("@False")) {
            this.expp = new Exp.False(e.lineNum);
            ast.optimizations.Main.modified();
        } else if (left.toString().startsWith("@False") && right.toString().startsWith("@False")) {
            this.expp = new Exp.True(e.lineNum);
            ast.optimizations.Main.modified();
        }

        return;
    }
    
    @Override
    public void visit(ArraySelect e)
    {

        e.array.accept(this);
        Exp.T array = this.expp == null ? e.array : this.expp;
        this.expp = null;

        e.index.accept(this);
        Exp.T index = this.expp == null ? e.index : this.expp;
        this.expp = null;

        this.expp = new Exp.ArraySelect( e.lineNum, array, index);
    }
    
    @Override
    public void visit(Call e)
    {
        e.exp.accept(this);
        Exp.T exp = this.expp == null ? e.exp : this.expp;
        Exp.Call call = new Exp.Call(e.lineNum, exp, e.id, e.args);
        call.rt = e.rt;
        call.at = e.at;
        call.type = e.type;
        this.expp = call;
        return;
    }
    
    @Override
    public void visit(False e)
    {
        this.expp = new Exp.False(e.lineNum);
        return;
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
        Exp.T array = this.expp == null ? e.array : this.expp;
        this.expp = new Exp.Length(e.lineNum, array);

        return;
    }
    
    @Override
    public void visit(Lt e)
    {
        e.left.accept(this);
        Exp.T left = this.expp == null ? e.left : this.expp;
        this.expp = null;

        e.right.accept(this);
        Exp.T right = this.expp == null ? e.right : this.expp;
        this.expp = null;

        if (left.toString().startsWith("ast.Ast$Exp$Num@") && right.toString().startsWith("ast.Ast$Exp$Num@")) {
            int leftt = ((Exp.Num) left).num;
            int rightt = ((Exp.Num) right).num;
            this.expp = leftt < rightt ? new Exp.True(e.lineNum) : new Exp.False(e.lineNum);
            ast.optimizations.Main.modified();

        }
        return;
    }
    
    @Override
    public void visit(NewIntArray e)
    {
        e.exp.accept(this);
        Exp.T exp = this.expp == null ? e.exp : this.expp;
        this.expp = new Exp.NewIntArray(e.lineNum, exp);

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
        Exp.T newExp = this.expp == null ? e.exp : this.expp;
        this.expp = new Exp.Not(e.lineNum, newExp);
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
        Exp.T left = this.expp == null ? e.left : this.expp;
        this.expp = null;

        e.right.accept(this);
        Exp.T right = this.expp == null ? e.right : this.expp;
        this.expp = null;

        if (left.toString().startsWith("ast.exp.Num@") && right.toString().startsWith("ast.exp.Num@")) {
            int leftt = ((Exp.Num) left).num;
            int rightt = ((Exp.Num) right).num;
            this.expp = new Exp.Num(e.lineNum, leftt - rightt);
            ast.optimizations.Main.modified();
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
        Exp.T left = this.expp == null ? e.left : this.expp;
        this.expp = null;

        e.right.accept(this);
        Exp.T right = this.expp == null ? e.right : this.expp;
        this.expp = null;

        if (left.toString().startsWith("ast.exp.Num@") && right.toString().startsWith("ast.exp.Num@")) {
            int leftt = ((Exp.Num) left).num;
            int rightt = ((Exp.Num) right).num;
            this.expp = new Exp.Num(e.lineNum, leftt * rightt);
            ast.optimizations.Main.modified();
        }
        return;
    }
    
    @Override
    public void visit(True e)
    {
        this.expp = new Exp.True(e.lineNum);
        return;
    }
    
    // statements
    @Override
    public void visit(Assign s)
    {
        s.exp.accept(this);
        Exp.T newExp = this.expp == null ? s.exp : this.expp;
        this.expp = null;

        this.stmm = new Stm.Assign(s.id, newExp, s.type);
        return;
    }
    
    @Override
    public void visit(AssignArray s)
    {
        s.index.accept(this);
        Exp.T newIndex = this.expp == null ? s.index : this.expp;
        this.expp = null;

        s.exp.accept(this);
        Exp.T newExp = this.expp == null ? s.exp : this.expp;
        this.expp = null;

        this.stmm = new Stm.AssignArray(s.id, newIndex, newExp);
        return;
    }
    
    @Override
    public void visit(Block s)
    {
        java.util.LinkedList<Stm.T> newStms = new java.util.LinkedList<Stm.T>();
        for (Stm.T t : s.stms) {
            t.accept(this);
            newStms.add(this.stmm);
        }
        this.stmm = new Stm.Block(newStms);
    }
    
    @Override
    public void visit(If s)
    {
        s.condition.accept(this);
        Exp.T newCondition = this.expp == null ? s.condition : this.expp;
        this.expp = null;

        s.thenn.accept(this);
        Stm.T newThenn = this.stmm == null ? s.thenn : this.stmm;
        this.stmm = null;

        s.elsee.accept(this);
        Stm.T elsee = this.stmm == null ? s.elsee : this.stmm;
        this.stmm = null;

        this.stmm = new Stm.If(newCondition, newThenn, elsee);
        return;
    }
    
    @Override
    public void visit(Print s)
    {
        s.exp.accept(this);
        this.stmm = new Stm.Print(this.expp == null ? s.exp : this.expp);
        this.expp = null;
        return;
    }
    
    @Override
    public void visit(While s)
    {
        s.condition.accept(this);
        Exp.T newCondition = this.expp == null ? s.condition : this.expp;
        this.expp = null;

        s.body.accept(this);
        Stm.T newBody = this.stmm == null ? s.body : this.stmm;
        this.stmm = null;

        this.stmm = new Stm.While(newCondition, newBody);
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
            newStms.add(this.stmm);
            this.stmm = null;
        }

        m.retExp.accept(this);

        this.methodd = new MethodSingle(m.retType, m.id, m.formals, m.locals, newStms, this.expp == null ? m.retExp : this.expp);
        this.expp = null;
        return;
    }
    
    // class
    @Override
    public void visit(ClassSingle c)
    {
        java.util.LinkedList<Method.T> newMethods = new java.util.LinkedList<Method.T>();
        for (Method.T m : c.methods) {
            m.accept(this);
            newMethods.add(this.methodd);
        }
        this.newClass = new ClassSingle(c.id, c.extendss, c.decs, newMethods);
        return;
    }
    
    // main class
    @Override
    public void visit(MainClassSingle c)
    {
        c.stm.accept(this);
        this.stmm = null;
        this.mainClass = new MainClassSingle(c.id, c.arg, c.stm);
        return;
    }
    
    // program
    @Override
    public void visit(ProgramSingle p)
    {
        p.mainClass.accept(this);

        java.util.LinkedList<ast.Ast.Class.T> newClasses = new java.util.LinkedList<ast.Ast.Class.T>();
        for (ast.Ast.Class.T c : p.classes) {
            c.accept(this);
            newClasses.add(this.newClass);
        }

        this.program = new ProgramSingle(this.mainClass, newClasses);

        if (control.Control.isTracing("ast.ConstFold")) {
            System.out.println("before optimization:");
            ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
            p.accept(pp);
            System.out.println("after optimization:");
            this.program.accept(pp);
        }
        return;
    }
}
