package ast.optimizations;

import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec;
import ast.Ast.Dec.DecSingle;
import ast.Ast.MainClass.MainClassSingle;
import ast.Ast.Method;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Type.Boolean;
import ast.Ast.Type.ClassType;
import ast.Ast.Type.Int;
import ast.Ast.Type.IntArray;
import ast.Ast.Exp.*;
import ast.Ast.Stm.*;
import ast.Ast.Type.*;

// Dead code elimination optimizations on an AST.

public class DeadCode implements ast.Visitor
{
    private ast.Ast.Class.T newClass;
    private ast.Ast.MainClass.T mainClass;
    public  ast.Ast.Program.T program;
    private ast.Ast.Method.T method;
    private ast.Ast.Stm.T stm;
    private ast.Ast.Dec.T localDec;
    
    public DeadCode()
    {
        this.newClass = null;
        this.mainClass = null;
        this.program = null;
        this.method = null;
        this.stm = null;
        this.localDec = null;
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
    }
    
    @Override
    public void visit(And e)
    {
    }
    
    @Override
    public void visit(ArraySelect e)
    {
    }
    
    @Override
    public void visit(Call e)
    {
        return;
    }
    
    @Override
    public void visit(False e)
    {
    }
    
    @Override
    public void visit(Id e)
    {
        return;
    }
    
    @Override
    public void visit(Length e)
    {
    }
    
    @Override
    public void visit(Lt e)
    {
        return;
    }
    
    @Override
    public void visit(NewIntArray e)
    {
    }
    
    @Override
    public void visit(NewObject e)
    {
        return;
    }
    
    @Override
    public void visit(Not e)
    {
    }
    
    @Override
    public void visit(Num e)
    {
        return;
    }
    
    @Override
    public void visit(Sub e)
    {
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
        
        return;
    }
    
    @Override
    public void visit(True e)
    {
    }
    
    // statements
    @Override
    public void visit(Assign s)
    {
        this.stm = s;
        return;
    }
    
    @Override
    public void visit(AssignArray s)
    {
        this.stm = s;
        return;
    }
    
    @Override
    public void visit(Block s)
    {
        java.util.LinkedList<ast.Ast.Stm.T> newStms = new java.util.LinkedList<ast.Ast.Stm.T>();
        for (ast.Ast.Stm.T t : s.stms) {
            t.accept(this);
            if (this.stm != null)
                newStms.add(this.stm);
        }
        this.stm = new ast.Ast.Stm.Block(newStms);
    }
    
    @Override
    public void visit(If s)
    {
        if (s.condition.toString().equals("@True")) {
            this.stm = s.thenn;
            ast.optimizations.Main.modified();
        } else if (s.condition.toString().equals("@False")) {
            this.stm = s.elsee;
            ast.optimizations.Main.modified();
        } else
            this.stm = s;
        return;
    }
    
    @Override
    public void visit(Print s)
    {
        this.stm = s;
        return;
    }
    
    @Override
    public void visit(While s)
    {
        if (s.condition.toString().equals("@False")) {
            this.stm = null;
            ast.optimizations.Main.modified();
        } else
            this.stm = s;
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
        if (d.isUsed)
            this.localDec = d;
        else {
            ast.optimizations.Main.modified();
            this.localDec = null;
        }
        return;
    }
    
    // method
    @Override
    public void visit(MethodSingle m)
    {
        java.util.LinkedList<Dec.T> newLocals = new java.util.LinkedList<Dec.T>();
        java.util.LinkedList<Stm.T> newStms = new java.util.LinkedList<Stm.T>();

        for (Dec.T local : m.locals) {
            local.accept(this);
            if (this.localDec != null)
                newLocals.add(this.localDec);
        }

        for (Stm.T s : m.stms) {
            s.accept(this);
            if (this.stm != null)
                newStms.add(this.stm);
        }

        m.retExp.accept(this);

        this.method = new MethodSingle(m.retType, m.id, m.formals, newLocals, newStms, m.retExp);

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
        this.program = p;

        p.mainClass.accept(this);

        java.util.LinkedList<ast.Ast.Class.T> newClasses = new java.util.LinkedList<ast.Ast.Class.T>();
        for (ast.Ast.Class.T c : p.classes) {
            c.accept(this);
            newClasses.add(this.newClass);
        }

        this.program = new ProgramSingle(this.mainClass, newClasses);

        if (control.Control.isTracing("ast.DeadCode")) {
            System.out.println("before optimization:");
            ast.PrettyPrintVisitor pp = new ast.PrettyPrintVisitor();
            p.accept(pp);
            System.out.println("after optimization:");
            this.program.accept(pp);
        }
        return;
    }
}
