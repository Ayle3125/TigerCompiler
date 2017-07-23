package cfg.optimizations;

import java.util.HashMap;
import java.util.HashSet;

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

public class ReachingDefinition implements cfg.Visitor
{
    // gen, kill for one statement
    private HashSet<Stm.T> oneStmGen;
    private HashSet<Stm.T> oneStmKill;
    
    // gen, kill for one transfer
    private HashSet<Stm.T> oneTransferGen;
    private HashSet<Stm.T> oneTransferKill;
    
    // gen, kill for statements
    private static HashMap<Stm.T, HashSet<Stm.T>> stmGen;
    private static HashMap<Stm.T, HashSet<Stm.T>> stmKill;
    
    // gen, kill for transfers
    private static HashMap<Transfer.T, HashSet<Stm.T>> transferGen;
    private static HashMap<Transfer.T, HashSet<Stm.T>> transferKill;
    
    // gen, kill for blocks
    private static HashMap<Block.T, HashSet<Stm.T>> blockGen;
    private static HashMap<Block.T, HashSet<Stm.T>> blockKill;
    
    // in, out for blocks
    public static java.util.LinkedHashMap<Block.T, java.util.LinkedHashSet<Stm.T>> blockIn;
    public static java.util.LinkedHashMap<Block.T, java.util.LinkedHashSet<Stm.T>> blockOut;
    //    private static HashMap<Block.T, HashSet<Stm.T>> blockIn;
    //    private static HashMap<Block.T, HashSet<Stm.T>> blockOut;
    
    // in, out for statements
    public static java.util.LinkedHashMap<Stm.T, java.util.LinkedHashSet<Stm.T>> stmIn;
    public static java.util.LinkedHashMap<Stm.T, java.util.LinkedHashSet<Stm.T>> stmOut;
    //    public static HashMap<Stm.T, HashSet<Stm.T>> stmIn;
    //    public static HashMap<Stm.T, HashSet<Stm.T>> stmOut;
    
    // liveIn, liveOut for transfer
    public static java.util.LinkedHashMap<Transfer.T, java.util.LinkedHashSet<Stm.T>> transferIn;
    public static java.util.LinkedHashMap<Transfer.T, java.util.LinkedHashSet<Stm.T>> transferOut;
    //    public static HashMap<Transfer.T, HashSet<Stm.T>> transferIn;
    //    public static HashMap<Transfer.T, HashSet<Stm.T>> transferOut;
    
    java.util.LinkedHashMap<String, java.util.LinkedList<Stm.T>> def;
    
    public ReachingDefinition()
    {
        this.oneStmGen = new HashSet<>();
        this.oneStmKill = new HashSet<>();
        
        this.oneTransferGen = new HashSet<>();
        this.oneTransferKill = new HashSet<>();
        
        this.stmGen = new HashMap<>();
        this.stmKill = new HashMap<>();
        
        this.transferGen = new HashMap<>();
        this.transferKill = new HashMap<>();
        
        this.blockGen = new HashMap<>();
        this.blockKill = new HashMap<>();
        
        this.blockIn = new java.util.LinkedHashMap<>();
        this.blockOut = new java.util.LinkedHashMap<>();
        
        this.stmIn = new java.util.LinkedHashMap<>();
        this.stmOut = new java.util.LinkedHashMap<>();
        
        this.transferIn = new java.util.LinkedHashMap<>();
        this.transferOut = new java.util.LinkedHashMap<>();
        
        def = new java.util.LinkedHashMap<>();
    }
    
    enum ReachingDefinition_Kind_t {
        None, StmGenKill, BlockGenKill, BlockInOut, StmInOut,
    }
    
    private ReachingDefinition_Kind_t kind = ReachingDefinition_Kind_t.None;
    
    
    // /////////////////////////////////////////////////////
    // utilities
    private void calcOneStmKill(Stm.T s, String t) {
        java.util.LinkedList<Stm.T> stms = def.get(t);
        for (Stm.T s2 : stms) {
            if (s2 != s) {
                this.oneStmKill.add(s2);
            }
        }
        
    }
    
    private boolean continuee;
    
    private void calculateStmTransferGenKill(BlockSingle b) {
        oneStmGen = new java.util.LinkedHashSet<Stm.T>();
        for (Stm.T s : b.stms) {
            this.oneStmGen = new java.util.LinkedHashSet<>();
            this.oneStmKill = new java.util.LinkedHashSet<>();
            s.accept(this);
            
            if (control.Control.isTracing("ReachingDefinition.step1")) {
                System.out.print("\ngen, kill for stm: ");
                System.out.print(s);
                System.out.print("\ngen is:\n");
                System.out.print(this.oneStmGen);
                System.out.println();
                System.out.print("\nkill is:\n");
                System.out.println(this.oneStmKill);
                System.out.println();
            }
            ReachingDefinition.stmGen.put(s, this.oneStmGen);
            ReachingDefinition.stmKill.put(s, this.oneStmKill);
        }
        
        this.oneTransferGen = new java.util.LinkedHashSet<>();
        this.oneTransferKill = new java.util.LinkedHashSet<>();
        b.transfer.accept(this);
        
        if (control.Control.isTracing("ReachingDefinition.step1")) {
            System.out.print("\ngen, kill for transfer: ");
            System.out.print("\ngen is:\n");
            System.out.print(this.oneTransferGen);
            System.out.println();
            System.out.print("\nkill is:\n");
            System.out.println(this.oneTransferKill);
            System.out.println();
        }
        ReachingDefinition.transferGen.put(b.transfer, this.oneTransferGen);
        ReachingDefinition.transferKill.put(b.transfer, this.oneTransferKill);
        
        return;
    }
    
    private void calculateBlockGenKill(BlockSingle b) {
        java.util.LinkedHashSet<Stm.T> newGen = new java.util.LinkedHashSet<Stm.T>();
        java.util.LinkedHashSet<Stm.T> newKill = new java.util.LinkedHashSet<Stm.T>();
        java.util.LinkedHashSet<Stm.T> tempGen = null;
        for (Stm.T stm : b.stms) {
            tempGen = newGen;
            newGen = new java.util.LinkedHashSet<Stm.T>();
            
            newGen.addAll(ReachingDefinition.stmGen.get(stm));
            
            for (Stm.T t : tempGen) {
                if (!ReachingDefinition.stmKill.get(stm).contains(t))
                    newGen.add(t);
            }
            
            newKill.addAll(ReachingDefinition.stmKill.get(stm));
        }
        
        ReachingDefinition.blockGen.put(b, newGen);
        ReachingDefinition.blockKill.put(b, newKill);
        
        if (control.Control.isTracing("ReachingDefinition.step2")) {
            System.out.print("\ngen, kill for block: ");
            System.out.print("\ngen is:\n");
            System.out.print(newGen);
            System.out.println();
            System.out.print("\nkill is:\n");
            System.out.println(newKill);
            System.out.println();
        }
        return;
    }
    
    private void calculateBlockInOut(BlockSingle b) {
        java.util.LinkedHashSet<Stm.T> newIn = ReachingDefinition.blockIn.get(b);
        java.util.LinkedHashSet<Stm.T> newOut = ReachingDefinition.blockOut.get(b);
        
        if (newIn == null)
            newIn = new java.util.LinkedHashSet<Stm.T>();
        if (newOut == null)
            newOut = new java.util.LinkedHashSet<Stm.T>();
        
        if (ReachingDefinition.blockGen.get(b) != null && newOut.addAll(ReachingDefinition.blockGen.get(b)))
            this.continuee = true;
        
        for (Stm.T t : newIn) {
            if (!ReachingDefinition.blockKill.get(b).contains(t) && newOut.add(t)) {
                this.continuee = true;
            }
        }
        
        for (Block.T sb : b.pred) {
            java.util.LinkedHashSet<Stm.T> block = ReachingDefinition.blockOut.get(sb);
            if (block != null && newIn.addAll(block))
                this.continuee = true;
        }
        
        ReachingDefinition.blockIn.put(b, newIn);
        ReachingDefinition.blockOut.put(b, newOut);
        
        return;
    }
    
    private void calculateStmTransferInOut(BlockSingle b) {
        for (Stm.T stm : b.stms) {
            java.util.LinkedHashSet<Stm.T> newIn = ReachingDefinition.stmIn.get(stm);
            java.util.LinkedHashSet<Stm.T> newOut = ReachingDefinition.stmOut.get(stm);
            
            if (newIn == null)
                newIn = new java.util.LinkedHashSet<Stm.T>();
            if (newOut == null)
                newOut = new java.util.LinkedHashSet<Stm.T>();
            
            if (ReachingDefinition.stmGen.get(stm) != null && newOut.addAll(ReachingDefinition.stmGen.get(stm)))
                this.continuee = true;
            for (Stm.T t : newIn) {
                if (!ReachingDefinition.stmKill.get(stm).contains(t) && newOut.add(t))
                    this.continuee = true;
            }
            
            if (stm.pred == null) {
                if (b.pred.size() > 0) {
                    for (Block.T t : b.pred) {
                        BlockSingle block = (BlockSingle) t;
                        if (block.stms.size() > 0) {
                            if (ReachingDefinition.stmOut.get(block.stms.getLast()) != null
                                    && newIn.addAll(ReachingDefinition.stmOut.get(block.stms.getLast())))
                                this.continuee = true;
                        } else {
                            if (ReachingDefinition.transferOut.get(block.transfer) != null
                                    && newIn.addAll(ReachingDefinition.transferOut.get(block.transfer)))
                                this.continuee = true;
                        }
                    }
                }
            } else {
                if (newIn.addAll(ReachingDefinition.stmOut.get(stm.pred)))
                    this.continuee = true;
            }
            
            ReachingDefinition.stmIn.put(stm, newIn);
            ReachingDefinition.stmOut.put(stm, newOut);
        }
        {
            java.util.LinkedHashSet<Stm.T> newIn = ReachingDefinition.transferIn.get(b.transfer);
            java.util.LinkedHashSet<Stm.T> newOut = ReachingDefinition.transferOut.get(b.transfer);
            if (newIn == null)
                newIn = new java.util.LinkedHashSet<Stm.T>();
            if (newOut == null)
                newOut = new java.util.LinkedHashSet<Stm.T>();
            
            if (ReachingDefinition.transferGen.get(b.transfer) != null && newOut.addAll(ReachingDefinition.transferGen.get(b.transfer)))
                this.continuee = true;
            for (Stm.T t : newIn) {
                if (!ReachingDefinition.transferKill.get(b.transfer).contains(t) && newOut.add(t))
                    this.continuee = true;
            }
            
            if (b.transfer.pred == null) {
                if (b.pred.size() > 0) {
                    for (Block.T t : b.pred) {
                        BlockSingle block = (BlockSingle) t;
                        if (block.stms.size() > 0) {
                            if (ReachingDefinition.stmOut.get(block.stms.getLast()) != null
                                    && newIn.addAll(ReachingDefinition.stmOut.get(block.stms.getLast())))
                                this.continuee = true;
                        } else {
                            if (ReachingDefinition.transferOut.get(block.transfer) != null
                                    && newIn.addAll(ReachingDefinition.transferOut.get(block.transfer)))
                                this.continuee = true;
                        }
                    }
                }
            } else {
                if (newIn.addAll(ReachingDefinition.stmOut.get(b.transfer.pred)))
                    this.continuee = true;
            }
            
            ReachingDefinition.transferIn.put(b.transfer, newIn);
            ReachingDefinition.transferOut.put(b.transfer, newOut);
            
        }
        return;
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
        calcOneStmKill(s, s.dst);
        this.oneStmGen.add(s);
        s.left.accept(this);
        s.right.accept(this);
        return;
    }
    
    @Override
    public void visit(InvokeVirtual s)
    {
        calcOneStmKill(s, s.dst);
        this.oneStmGen.add(s);
        for (Operand.T arg : s.args) {
            arg.accept(this);
        }
        return;
    }
    
    @Override
    public void visit(Lt s)
    {
        calcOneStmKill(s, s.dst);
        this.oneStmGen.add(s);
        // Invariant: accept() of operand modifies "gen"
        s.left.accept(this);
        s.right.accept(this);
        return;
    }
    
    @Override
    public void visit(Move s)
    {
        calcOneStmKill(s, s.dst);
        this.oneStmGen.add(s);
        // Invariant: accept() of operand modifies "gen"
        s.src.accept(this);
        return;
    }
    
    @Override
    public void visit(NewObject s)
    {
        calcOneStmKill(s, s.dst);
        this.oneStmGen.add(s);
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
        calcOneStmKill(s, s.dst);
        this.oneStmGen.add(s);
        // Invariant: accept() of operand modifies "gen"
        s.left.accept(this);
        s.right.accept(this);
        return;
    }
    
    @Override
    public void visit(Times s)
    {
        calcOneStmKill(s, s.dst);
        this.oneStmGen.add(s);
        // Invariant: accept() of operand modifies "gen"
        s.left.accept(this);
        s.right.accept(this);
        return;
    }
    
    // transfer
    @Override
    public void visit(If s)
    {
    }
    
    @Override
    public void visit(Goto s)
    {
        return;
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
        switch (this.kind) {
        case StmGenKill:
            calculateStmTransferGenKill(b);
            break;
        case BlockGenKill:
            calculateBlockGenKill(b);
            break;
        case BlockInOut:
            calculateBlockInOut(b);
            break;
        case StmInOut:
            calculateStmTransferInOut(b);
            break;
        default:
            return;
        }
    }
    
    // method
    @Override
    public void visit(MethodSingle m)
    {
        // Five steps:
        // Step 0: for each argument or local variable "x" in the
        // method m, calculate x's definition site set def(x).
        
        for (Block.T b : m.blocks) {
            BlockSingle block = (BlockSingle) b;
            for (Stm.T s : block.stms) {
                if (s.dst != null) {
                    java.util.LinkedList<Stm.T> set = def.get(s.dst);
                    if (set != null) {
                        set.add(s);
                    } else {
                        set = new java.util.LinkedList<Stm.T>();
                        set.add(s);
                        def.put(s.dst, set);
                    }
                }
            }
        }
        
        // Step 1: calculate the "gen" and "kill" sets for each
        // statement and transfer
        this.kind = ReachingDefinition_Kind_t.StmGenKill;
        for (Block.T block : m.blocks) {
            block.accept(this);
        }
        
        // Step 2: calculate the "gen" and "kill" sets for each block.
        // For this, you should visit statements and transfers in a
        // block sequentially.
        this.kind = ReachingDefinition_Kind_t.BlockGenKill;
        for (Block.T block : m.blocks) {
            block.accept(this);
        }
        
        // Step 3: calculate the "in" and "out" sets for each block
        // Note that to speed up the calculation, you should use
        // a topo-sort order of the CFG blocks, and
        // crawl through the blocks in that order.
        // And also you should loop until a fix-point is reached.
        
        this.kind = ReachingDefinition_Kind_t.BlockInOut;
        
        java.util.HashMap<String, Block.T> blockMap = new java.util.HashMap<>();
        for (Block.T b : m.blocks) {
            BlockSingle block = (BlockSingle) b;
            blockMap.put(block.label.toString(), block);
        }
        
        for (Block.T b : m.blocks) {
            BlockSingle block = (BlockSingle) b;
            if (block.transfer.getClass().getName().toString().equals("cfg.transfer.If")) {
                Transfer.If iff = (Transfer.If) block.transfer;
                ((BlockSingle) blockMap.get(iff.truee.toString())).pred.add(block);
                ((BlockSingle) blockMap.get(iff.falsee.toString())).pred.add(block);
            } else if (block.transfer.getClass().getName().toString().equals("cfg.transfer.Goto")) {
                Transfer.Goto Gotoo = (Transfer.Goto) block.transfer;
                ((BlockSingle) blockMap.get(Gotoo.label.toString())).pred.add(block);
            }
        }
        
        do {
            this.continuee = false;
            for (Block.T block : m.blocks) {
                block.accept(this);
            }
        } while (this.continuee);
        
        if (control.Control.isTracing("ReachingDefinition.step3")) {
            for (Block.T block : m.blocks) {
                BlockSingle b= (BlockSingle) block;
                
                System.out.print("\n" + b.label + ":");
                System.out.print("\nin, out for block:");
                System.out.print("\nIn is:\t");
                System.out.print(ReachingDefinition.blockIn.get(block));
                System.out.print("\nOut is:\t");
                System.out.println(ReachingDefinition.blockOut.get(block));
                
            }
        }
        
        // Step 4: calculate the "in" and "out" sets for each
        // statement and transfer
        
        this.kind = ReachingDefinition_Kind_t.StmInOut;
        
        for (Block.T b : m.blocks) {
            BlockSingle block = (BlockSingle) b;
            Object pred = null;
            for (Stm.T s : block.stms) {
                s.pred = pred;
                pred = s;
            }
            
            block.transfer.pred = pred;
        }
        
        do {
            this.continuee = false;
            for (Block.T block : m.blocks) {
                block.accept(this);
            }
        } while (this.continuee);
        
        if (control.Control.isTracing("ReachingDefinition.step4")) {
            for (Block.T block : m.blocks) {
                BlockSingle b = (BlockSingle) block;
                System.out.print("\n" + b.label + ":");
                
                for (Stm.T stm : b.stms) {
                    System.out.print("\nin, out for stm:");
                    System.out.print("\nIn is:\t");
                    System.out.print(ReachingDefinition.stmIn.get(stm));
                    System.out.print("\nOut is:\t");
                    System.out.print(ReachingDefinition.stmOut.get(stm));
                    System.out.println();
                }
                
                System.out.print("\nin, out for transfer:");
                System.out.print("\nIn is:\t");
                System.out.print(ReachingDefinition.transferIn.get(b.transfer));
                System.out.print("\nOut is:\t");
                System.out.println(ReachingDefinition.transferOut.get(b.transfer));
            }
        }
        
    }
    
    @Override
    public void visit(MainMethodSingle m)
    {
        // Five steps:
        // Step 0: for each argument or local variable "x" in the
        // method m, calculate x's definition site set def(x).
        for (Block.T b : m.blocks) {
            BlockSingle block = (BlockSingle) b;
            for (Stm.T s : block.stms) {
                if (s.dst != null) {
                    java.util.LinkedList<Stm.T> set = def.get(s.dst);
                    if (set != null) {
                        set.add(s);
                    } else {
                        set = new java.util.LinkedList<Stm.T>();
                        set.add(s);
                        def.put(s.dst, set);
                    }
                }
            }
        }
        
        // Step 1: calculate the "gen" and "kill" sets for each
        // statement and transfer
        this.kind = ReachingDefinition_Kind_t.StmGenKill;
        for (Block.T block : m.blocks) {
            block.accept(this);
        }
        
        // Step 2: calculate the "gen" and "kill" sets for each block.
        // For this, you should visit statements and transfers in a
        // block sequentially.
        this.kind = ReachingDefinition_Kind_t.BlockGenKill;
        for (Block.T block : m.blocks) {
            block.accept(this);
        }
        
        // Step 3: calculate the "in" and "out" sets for each block
        // Note that to speed up the calculation, you should use
        // a topo-sort order of the CFG blocks, and
        // crawl through the blocks in that order.
        // And also you should loop until a fix-point is reached.
        
        this.kind = ReachingDefinition_Kind_t.BlockInOut;
        
        java.util.HashMap<String, Block.T> blockMap = new java.util.HashMap<>();
        for (Block.T b : m.blocks) {
            BlockSingle block = (BlockSingle) b;
            blockMap.put(block.label.toString(), block);
        }
        
        for (Block.T b : m.blocks) {
            BlockSingle block = (BlockSingle) b;
            if (block.transfer.getClass().getName().toString().equals("cfg.transfer.If")) {
                Transfer.If iff = (Transfer.If) block.transfer;
                ((BlockSingle) blockMap.get(iff.truee.toString())).pred.add(block);
                ((BlockSingle) blockMap.get(iff.falsee.toString())).pred.add(block);
            } else if (block.transfer.getClass().getName().toString().equals("cfg.transfer.Goto")) {
                Transfer.Goto Gotoo = (Transfer.Goto) block.transfer;
                ((BlockSingle) blockMap.get(Gotoo.label.toString())).pred.add(block);
            }
        }
        
        do {
            this.continuee = false;
            for (Block.T block : m.blocks) {
                block.accept(this);
            }
        } while (this.continuee);
        
        if (control.Control.isTracing("ReachingDefinition.step3")) {
            for (Block.T block : m.blocks) {
                BlockSingle b = (BlockSingle) block;
                
                System.out.print("\n" + b.label + ":");
                System.out.print("\nin, out for block:");
                System.out.print("\nIn is:\t");
                System.out.print(ReachingDefinition.blockIn.get(block));
                System.out.print("\nOut is:\t");
                System.out.println(ReachingDefinition.blockOut.get(block));
                
            }
        }
        
        // Step 4: calculate the "in" and "out" sets for each
        // statement and transfer
        this.kind = ReachingDefinition_Kind_t.StmInOut;
        
        for (Block.T b : m.blocks) {
            BlockSingle block = (BlockSingle) b;
            Object pred = null;
            for (Stm.T s : block.stms) {
                s.pred = pred;
                pred = s;
            }
            
            block.transfer.pred = pred;
        }
        
        do {
            this.continuee = false;
            for (Block.T block : m.blocks) {
                block.accept(this);
            }
        } while (this.continuee);
        
        if (control.Control.isTracing("ReachingDefinition.step4")) {
            for (Block.T block : m.blocks) {
                BlockSingle b = (BlockSingle) block;
                System.out.print("\n" + b.label + ":");
                
                for (Stm.T stm : b.stms) {
                    System.out.print("\nin, out for stm:");
                    System.out.print("\nIn is:\t");
                    System.out.print(ReachingDefinition.stmIn.get(stm));
                    System.out.print("\nOut is:\t");
                    System.out.print(ReachingDefinition.stmOut.get(stm));
                    System.out.println();
                }
                
                System.out.print("\nin, out for transfer:");
                System.out.print("\nIn is:\t");
                System.out.print(ReachingDefinition.transferIn.get(b.transfer));
                System.out.print("\nOut is:\t");
                System.out.println(ReachingDefinition.transferOut.get(b.transfer));
            }
        }
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
