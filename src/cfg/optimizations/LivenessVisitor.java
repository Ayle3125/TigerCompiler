package cfg.optimizations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import cfg.Cfg.Block;
import cfg.Cfg.Block.BlockSingle;
import cfg.Cfg.Class.ClassSingle;
import cfg.Cfg.Dec.DecSingle;
import cfg.Cfg.MainMethod.MainMethodSingle;
import cfg.Cfg.Method;
import cfg.Cfg.Method.MethodSingle;
import cfg.Cfg.Operand;
import cfg.Cfg.Operand.Int;
import cfg.Cfg.Operand.Var;
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
import cfg.Cfg.Transfer;
import cfg.Cfg.Transfer.Goto;
import cfg.Cfg.Transfer.If;
import cfg.Cfg.Transfer.Return;
import cfg.Cfg.Type.ClassType;
import cfg.Cfg.Type.IntArrayType;
import cfg.Cfg.Type.IntType;
import cfg.Cfg.Vtable.VtableSingle;

public class LivenessVisitor implements cfg.Visitor
{
    // gen, kill for one statement
    private java.util.LinkedHashSet<String> oneStmGen;
    private java.util.LinkedHashSet<String> oneStmKill;
    
    // gen, kill for one transfer
    private java.util.LinkedHashSet<String> oneTransferGen;
    private java.util.LinkedHashSet<String> oneTransferKill;
    
    
    // gen, kill for statements
    public static java.util.LinkedHashMap<Stm.T, java.util.LinkedHashSet<String>> stmGen;
    public static java.util.LinkedHashMap<Stm.T, java.util.LinkedHashSet<String>> stmKill;
    
    // gen, kill for transfers
    public static java.util.LinkedHashMap<Transfer.T, java.util.LinkedHashSet<String>> transferGen;
    public static java.util.LinkedHashMap<Transfer.T, java.util.LinkedHashSet<String>> transferKill;
    
    // gen, kill for blocks
    public static java.util.LinkedHashMap<Block.T, java.util.LinkedHashSet<String>> blockGen;
    public static java.util.LinkedHashMap<Block.T, java.util.LinkedHashSet<String>> blockKill;
    
    // liveIn, liveOut for blocks
    public static java.util.LinkedHashMap<Block.T, java.util.LinkedHashSet<String>> blockLiveIn;
    public static java.util.LinkedHashMap<Block.T, java.util.LinkedHashSet<String>> blockLiveOut;
    
    // liveIn, liveOut for statements
    public static java.util.LinkedHashMap<Stm.T, java.util.LinkedHashSet<String>> stmLiveIn;
    public static java.util.LinkedHashMap<Stm.T, java.util.LinkedHashSet<String>> stmLiveOut;
    
    // liveIn, liveOut for transfer
    public static java.util.LinkedHashMap<Transfer.T, java.util.LinkedHashSet<String>> transferLiveIn;
    public static java.util.LinkedHashMap<Transfer.T, java.util.LinkedHashSet<String>> transferLiveOut;
    
    // As you will walk the tree for many times, so
    // it will be useful to recored which is which:
    enum Liveness_Kind_t
    {
        None, StmGenKill, BlockGenKill, BlockInOut, StmInOut,
    }
    
    private Liveness_Kind_t kind = Liveness_Kind_t.None;
    
    public LivenessVisitor()
    {
        this.oneStmGen = new java.util.LinkedHashSet<>();
        this.oneStmKill = new java.util.LinkedHashSet<>();

        this.oneTransferGen = new java.util.LinkedHashSet<>();
        this.oneTransferKill = new java.util.LinkedHashSet<>();
        
        LivenessVisitor.stmGen = new java.util.LinkedHashMap<>();
        LivenessVisitor.stmKill = new java.util.LinkedHashMap<>();
        
        LivenessVisitor.transferGen = new java.util.LinkedHashMap<>();
        LivenessVisitor.transferKill = new java.util.LinkedHashMap<>();
        
        LivenessVisitor.blockGen = new java.util.LinkedHashMap<>();
        LivenessVisitor.blockKill = new java.util.LinkedHashMap<>();
        
        LivenessVisitor.blockLiveIn = new java.util.LinkedHashMap<>();
        LivenessVisitor.blockLiveOut = new java.util.LinkedHashMap<>();
        
        LivenessVisitor.stmLiveIn = new java.util.LinkedHashMap<>();
        LivenessVisitor.stmLiveOut = new java.util.LinkedHashMap<>();
        
        LivenessVisitor.transferLiveIn = new java.util.LinkedHashMap<>();
        LivenessVisitor.transferLiveOut = new java.util.LinkedHashMap<>();
        
        this.kind = Liveness_Kind_t.None;
    }

    // /////////////////////////////////////////////////////
    // operand
    @Override
    public void visit(Int operand)
    {
        return;
    }
    
    @Override
    public void visit(Var operand)
    {
        this.oneStmGen.add(operand.id);
        return;
    }
    
    // statements
    @Override
    public void visit(Add s)
    {
        this.oneStmKill.add(s.dst);
        // Invariant: accept() of operand modifies "gen"
        s.left.accept(this);
        s.right.accept(this);
        return;
    }
    
    @Override
    public void visit(InvokeVirtual s)
    {
        this.oneStmKill.add(s.dst);
        this.oneStmGen.add(s.obj);
        for (Operand.T arg : s.args) {
            arg.accept(this);
        }
        return;
    }
    
    @Override
    public void visit(Lt s)
    {
        this.oneStmKill.add(s.dst);
        // Invariant: accept() of operand modifies "gen"
        s.left.accept(this);
        s.right.accept(this);
        return;
    }
    
    @Override
    public void visit(Move s)
    {
        this.oneStmKill.add(s.dst);
        // Invariant: accept() of operand modifies "gen"
        s.src.accept(this);
        return;
    }
    
    @Override
    public void visit(NewObject s)
    {
        this.oneStmKill.add(s.dst);
        return;
    }
    
    @Override
    public void visit(Print s)
    {
        s.arg.accept(this);
        return;
    }
    
    @Override
    public void visit(Sub s)
    {
        this.oneStmKill.add(s.dst);
        // Invariant: accept() of operand modifies "gen"
        s.left.accept(this);
        s.right.accept(this);
        return;
    }
    
    @Override
    public void visit(Times s)
    {
        this.oneStmKill.add(s.dst);
        // Invariant: accept() of operand modifies "gen"
        s.left.accept(this);
        s.right.accept(this);
        return;
    }
    
    // transfer
    @Override
    public void visit(If s)
    {
        // Invariant: accept() of operand modifies "gen"
        s.operand.accept(this);
        return;
    }
    
    @Override
    public void visit(Goto s)
    {
        return;
    }
    
    @Override
    public void visit(Return s)
    {
        // Invariant: accept() of operand modifies "gen"
        s.operand.accept(this);
        return;
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
    
    // utility functions:
    private void calculateStmTransferGenKill(BlockSingle b)
    {
        if (control.Control.isTracing("liveness.step1"))
            System.out.print("\n" + b.label + ":");
        for (Stm.T s : b.stms) {
            this.oneStmGen = new java.util.LinkedHashSet<>();
            this.oneStmKill = new java.util.LinkedHashSet<>(); 
            s.accept(this);
            LivenessVisitor.stmGen.put(s, this.oneStmGen);
            LivenessVisitor.stmKill.put(s, this.oneStmKill);
            if (control.Control.isTracing("liveness.step1")) {
                System.out.print("\ngen, kill for statement:");
                s.toString();
                System.out.print("\ngen is:");
                for (String str : this.oneStmGen) {
                    System.out.print(str + ", ");
                }
                System.out.print("\nkill is:");
                for (String str : this.oneStmKill) {
                    System.out.print(str + ", ");
                }
            }
        }
        this.oneTransferGen = new java.util.LinkedHashSet<>();
        this.oneTransferKill = new java.util.LinkedHashSet<>();
        b.transfer.accept(this);
        LivenessVisitor.transferGen.put(b.transfer, this.oneTransferGen);
        LivenessVisitor.transferKill.put(b.transfer, this.oneTransferKill);
        if (control.Control.isTracing("liveness.step1")) {
            System.out.print("\ngen, kill for transfer:");
            b.toString();
            System.out.print("\ngen is:");
            for (String str : this.oneTransferGen) {
                System.out.print(str + ", ");
            }
            System.out.println("\nkill is:");
            for (String str : this.oneTransferKill) {
                System.out.print(str + ", ");
            }
        }
        return;
    }
    
    // block
    @Override
    public void visit(BlockSingle b)
    {
        switch (this.kind) {
        case StmGenKill:
            calculateStmTransferGenKill(b);
            break;
        default:
            // Your code here:
            return;
        }
    }
    
    // method
    @Override
    public void visit(MethodSingle m)
    {
        // Four steps:
        // Step 1: calculate the "gen" and "kill" sets for each
        // statement and transfer
        this.kind = Liveness_Kind_t.StmGenKill;
        for (Block.T block : m.blocks) {
            block.accept(this);
        }
        
        // Step 2: calculate the "gen" and "kill" sets for each block.
        // For this, you should visit statements and transfers in a
        // block in a reverse order.
        // Your code here:
        
        // Step 3: calculate the "liveIn" and "liveOut" sets for each block
        // Note that to speed up the calculation, you should first
        // calculate a reverse topo-sort order of the CFG blocks, and
        // crawl through the blocks in that order.
        // And also you should loop until a fix-point is reached.
        // Your code here:
        
        // Step 4: calculate the "liveIn" and "liveOut" sets for each
        // statement and transfer
        // Your code here:
        
    }
    
    @Override
    public void visit(MainMethodSingle m)
    {
        // Four steps:
        // Step 1: calculate the "gen" and "kill" sets for each
        // statement and transfer
        this.kind = Liveness_Kind_t.StmGenKill;
        for (Block.T block : m.blocks) {
            block.accept(this);
        }
        
        // Step 2: calculate the "gen" and "kill" sets for each block.
        // For this, you should visit statements and transfers in a
        // block in a reverse order.
        // Your code here:
        
        // Step 3: calculate the "liveIn" and "liveOut" sets for each block
        // Note that to speed up the calculation, you should first
        // calculate a reverse topo-sort order of the CFG blocks, and
        // crawl through the blocks in that order.
        // And also you should loop until a fix-point is reached.
        // Your code here:
        
        // Step 4: calculate the "liveIn" and "liveOut" sets for each
        // statement and transfer
        // Your code here:
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
        p.mainMethod.accept(this);
        for (Method.T mth : p.methods) {
            mth.accept(this);
        }
        return;
    }
    
}
