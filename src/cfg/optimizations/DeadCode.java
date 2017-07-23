package cfg.optimizations;

import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.Class.ClassSingle;
import cfg.Cfg.Dec.DecSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Operand.Int;
import cfg.Cfg.Operand.Var;
import cfg.Cfg.Block;
import cfg.Cfg.Method;
import cfg.Cfg.Program;
import cfg.Cfg.Program.ProgramSingle;
import cfg.Cfg.Stm;
import cfg.Cfg.Stm.Add;
import cfg.Cfg.Stm.InvokeVirtual;
import cfg.Cfg.Stm.Lt;
import cfg.Cfg.Stm.Move;
import cfg.Cfg.Stm.NewObject;
import cfg.Cfg.Stm.Print;
import cfg.Cfg.Stm.Sub;
import cfg.Cfg.Stm.Times;
import cfg.Cfg.Transfer.Goto;
import cfg.Cfg.Transfer.If;
import cfg.Cfg.Transfer.Return;
import cfg.Cfg.Type.ClassType;
import cfg.Cfg.Type.IntArrayType;
import cfg.Cfg.Type.IntType;
import cfg.Cfg.Vtable.VtableSingle;

public class DeadCode implements cfg.Visitor
{
    public Program.T program;
    private Method.T methodd;
    private BlockSingle blockk;
    
    public DeadCode()
    {
        this.program = null;
        this.methodd = null;
        this.blockk = null;
    } 
    
    // /////////////////////////////////////////////////////
    // operand
    @Override
    public void visit(Int operand)
    {
    }
    
    @Override
    public void visit(Var operand)
    {
    }
    
    // statements
    @Override
    public void visit(Add s)
    {
    }
    
    @Override
    public void visit(InvokeVirtual s)
    {
    }
    
    @Override
    public void visit(Lt s)
    {
    }
    
    @Override
    public void visit(Move s)
    {
    }
    
    @Override
    public void visit(NewObject s)
    {
    }
    
    @Override
    public void visit(Print s)
    {
    }
    
    @Override
    public void visit(Sub s)
    {
    }
    
    @Override
    public void visit(Times s)
    {
    }
    
    // transfer
    @Override
    public void visit(If s)
    {
    }
    
    @Override
    public void visit(Goto s)
    {
    }
    
    @Override
    public void visit(Return s)
    {
    }
    
    // type
    @Override
    public void visit(ClassType t)
    {
    }
    
    @Override
    public void visit(IntType t)
    {
    }
    
    @Override
    public void visit(IntArrayType t)
    {
    }
    
    // dec
    @Override
    public void visit(DecSingle d)
    {
    }
    
    // block
    @Override
    public void visit(BlockSingle b)
    {
        java.util.LinkedList<Stm.T> newStms = new java.util.LinkedList<Stm.T>();

        for (Stm.T stm : b.stms) {
            if (stm.dst != null) {
                if (stm.toString().startsWith("this->") || stm.toString().startsWith("(this->")
                        || LivenessVisitor.stmLiveOut.get(stm).contains(stm.dst))
                    newStms.add(stm);
                else {
                    // System.out.println(b.label);
                    // System.out.println("delete stm:" + stm);
                }
            } else {
                newStms.add(stm);
            }
        }
        this.blockk = new BlockSingle(b.label, newStms, b.transfer);
    }
    
    // method
    @Override
    public void visit(MethodSingle m)
    {
        java.util.LinkedList<Block.T> newBlocks = new java.util.LinkedList<Block.T>();
        
        for (Block.T block : m.blocks) {
            BlockSingle b = (BlockSingle) block;
            b.accept(this);
            newBlocks.add(this.blockk);
        }
        
        this.methodd = new MethodSingle(m.retType, m.id, m.classId, m.formals, m.locals, newBlocks, m.entry, m.exit, m.retValue);
        return;
    }
    
    @Override
    public void visit(MainMethodSingle m)
    {
    }
    
    // vtables
    @Override
    public void visit(VtableSingle v)
    {
    }
    
    // class
    @Override
    public void visit(ClassSingle c)
    {
    }
    
    // program
    @Override
    public void visit(ProgramSingle p)
    {
        java.util.LinkedList<Method.T> newMethods = new java.util.LinkedList<Method.T>();
        
        p.mainMethod.accept(this);
        for (Method.T method : p.methods) {
            method.accept(this);
            newMethods.add(this.methodd);
        }
        
        this.program = new ProgramSingle(p.classes, p.vtables, newMethods, p.mainMethod);
        return;
    }
    
}
