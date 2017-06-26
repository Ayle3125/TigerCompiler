package codegen.C;

import java.util.LinkedList;

import com.sun.accessibility.internal.resources.accessibility;

import ast.Ast.Type.Boolean;
import codegen.C.Ast.Class.ClassSingle;
import codegen.C.Ast.Dec;
import codegen.C.Ast.Type;
import codegen.C.Ast.Dec.DecSingle;
import codegen.C.Ast.Exp;
import codegen.C.Ast.Exp.Add;
import codegen.C.Ast.Exp.And;
import codegen.C.Ast.Exp.ArraySelect;
import codegen.C.Ast.Exp.Call;
import codegen.C.Ast.Exp.False;
import codegen.C.Ast.Exp.Id;
import codegen.C.Ast.Exp.Length;
import codegen.C.Ast.Exp.Lt;
import codegen.C.Ast.Exp.NewIntArray;
import codegen.C.Ast.Exp.NewObject;
import codegen.C.Ast.Exp.Not;
import codegen.C.Ast.Exp.Num;
import codegen.C.Ast.Exp.Sub;
import codegen.C.Ast.Exp.T;
import codegen.C.Ast.Exp.This;
import codegen.C.Ast.Exp.Times;
import codegen.C.Ast.Exp.True;
import codegen.C.Ast.MainMethod.MainMethodSingle;
import codegen.C.Ast.Method;
import codegen.C.Ast.Method.MethodSingle;
import codegen.C.Ast.Program.ProgramSingle;
import codegen.C.Ast.Stm;
import codegen.C.Ast.Stm.Assign;
import codegen.C.Ast.Stm.AssignArray;
import codegen.C.Ast.Stm.Block;
import codegen.C.Ast.Stm.If;
import codegen.C.Ast.Stm.Print;
import codegen.C.Ast.Stm.While;
import codegen.C.Ast.Type.ClassType;
import codegen.C.Ast.Type.Int;
import codegen.C.Ast.Type.IntArray;
import codegen.C.Ast.Vtable;
import codegen.C.Ast.Vtable.VtableSingle;
import control.Control;

public class PrettyPrintVisitor implements Visitor
{
    private int indentLevel;
    private java.io.BufferedWriter writer;
    
    private void error(int line)
    {
        System.out.println(" type mismatch "+String.valueOf(line));
        System.exit(1);
    }
    
    public static int getLineNumber() {
        return Thread.currentThread().getStackTrace()[2].getLineNumber();
    }
    
    
    public PrettyPrintVisitor()
    {
        this.indentLevel = 0;
    }
    
    private void indent()
    {
        this.indentLevel += 4;
    }
    
    private void unIndent()
    {
        this.indentLevel -= 4;
    }
    
    private void printSpaces()
    {
        int i = this.indentLevel;
        while (i-- != 0)
            this.say(" ");
    }
    
    private void sayln(String s)
    {
        say(s);
        try {
            this.writer.write("\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private void say(String s)
    {
        try {
            this.writer.write(s);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    // /////////////////////////////////////////////////////
    // expressions
    
    //public Add(T left, T right)
    @Override
    public void visit(Add e)
    {
        e.left.accept(this);
        this.say(" + ");
        e.right.accept(this);
        
    }
    
    @Override
    public void visit(And e)
    {
        this.say("(");
        e.left.accept(this);
        this.say(") && (");
        e.right.accept(this);
        this.say(")");
        
    }
    
    //public ArraySelect(T array, T index)
    @Override
    public void visit(ArraySelect e)
    {
        e.array.accept(this);
        this.say("->__data[");
        e.index.accept(this);
        this.say("]");
    }
    
    @Override
    public void visit(Call e)
    {
        this.say("(__gc_frame." + e.assign + "=");
        e.exp.accept(this);
        this.say(", ");
        this.say("__gc_frame." + e.assign + "->vptr->" + e.id + "("
                + "__gc_frame." + e.assign);
        int size = e.args.size();
        if (size == 0) {
            this.say("))");
            return;
        }
        for (Exp.T x : e.args) {
            this.say(", ");
            x.accept(this);
        }
        this.say("))");
        return;
    }
    
    @Override
    public void visit(codegen.C.Ast.Type.Boolean t) {
        
        this.say("Boolean");
    }
    
    @Override
    public void visit(False e) {
        
        this.say("false");
    }
    
    @Override
    public void visit(Id e)
    {
        if (e.isField)
            this.say("this->");
        else if (e.isLocal) {
            if (e.type instanceof ast.Ast.Type.IntArray
                    || e.type instanceof ast.Ast.Type.ClassType)
                this.say("__gc_frame.");
        }
        this.say(e.id);
    }
    
    @Override
    public void visit(Length e)
    {
        this.say("(");
        e.array.accept(this);
        this.say("->__u.length)");
        
    }
    
    @Override
    public void visit(Lt e)
    {
        e.left.accept(this);
        this.say(" < ");
        e.right.accept(this);
        return;
    }
    
    @Override
    public void visit(NewIntArray e)
    {
        this.say("Tiger_new_array(");
        e.exp.accept(this);
        this.say(")");
    }
    
    @Override
    public void visit(NewObject e)
    {
        this.say("((struct " + e.id + "*)(Tiger_new (&" + e.id
                + "_vtable_, sizeof(struct " + e.id + "))))");
        return;
    }
    
    @Override
    public void visit(Not e)
    {
        this.say("!(");
        this.say("(");
        e.exp.accept(this);
        this.say(")");
    }
    
    @Override
    public void visit(Num e)
    {
        this.say(Integer.toString(e.num));
        return;
    }
    
    @Override
    public void visit(Sub e)
    {
        e.left.accept(this);
        this.say(" - ");
        e.right.accept(this);
        return;
    }
    
    @Override
    public void visit(This e)
    {
        this.say("this");
    }
    
    @Override
    public void visit(Times e)
    {
        e.left.accept(this);
        this.say(" * ");
        e.right.accept(this);
        return;
    }
    
    @Override
    public void visit(True e) {
        // TODO Auto-generated method stub
        this.say("true");
    }
    
    // statements
    @Override
    public void visit(Assign s){
        if (s.isField)
            this.say("this->");
        else if (s.isLocal) {
            if (s.type instanceof ast.Ast.Type.IntArray
                    || s.type instanceof ast.Ast.Type.ClassType)
                this.say("__gc_frame.");
        }
        this.say(s.id + " = ");
        s.exp.accept(this);
        this.sayln(";");
        return;
    }
    
    //s[index]=k;
    // public AssignArray(String id, Exp.T index, Exp.T exp)
    @Override
    public void visit(AssignArray s)
    {
        if (s.isField)
            this.say("this->");
        else if (s.isLocal)
            this.say("__gc_frame.");
        this.say(s.id + "->__data[");
        s.index.accept(this);
        this.say("] = ");
        s.exp.accept(this);
        this.sayln(";");  
    }
    
    // public Block(LinkedList<T> stms)
    @Override
    public void visit(Block s)
    {
        this.sayln("{");
        this.indent();
        
        for (Stm.T stm : s.stms) {
            stm.accept(this);
        }	  

        this.unIndent();
        this.printSpaces();
        this.sayln("}");
    }
    
    //public If(Exp.T condition, T thenn, T elsee)
    @Override
    public void visit(If s)
    {
        this.printSpaces();
        this.say("if (");
        s.condition.accept(this);
        this.sayln("){");
        this.indent();
        s.thenn.accept(this);
        this.unIndent();
        this.printSpaces();
        this.sayln("}");
        this.printSpaces();
        this.sayln("else{");
        this.indent();
        s.elsee.accept(this);
        this.printSpaces();
        this.sayln("}");
        this.unIndent();
        return;
    }
    
    @Override
    public void visit(Print s)
    {
        this.printSpaces();
        this.say("System_out_println (");
        s.exp.accept(this);
        this.sayln(");");
        return;
    }
    
    //public While(Exp.T condition, T body)
    
    @Override
    public void visit(While s)
    {
        this.printSpaces();
        this.say("while(");
        s.condition.accept(this);
        this.sayln("){");
        this.indent();
        s.body.accept(this);
        this.unIndent();
        this.printSpaces();
        this.sayln("}");
        
    }
    
    // type
    @Override
    public void visit(ClassType t)
    {
        this.say("struct " + t.id + " *");
    }
    
    @Override
    public void visit(Int t)
    {
        //this.say("int");
        this.say("long");
    }
    
    @Override
    public void visit(IntArray t)
    {
        //this.say("int *"); 
        this.say("struct __tiger_obj_header *");
    }
    
    // dec
    
    @Override
    public void visit(DecSingle d)
    {

        d.type.accept(this);
        this.say(" ");
        this.sayln(d.id + ";");
    }
    
    // method
    @Override
    public void visit(MethodSingle m)
    {
        m.retType.accept(this);
        this.say(" " + m.classId + "_" + m.id + "(");
        int size = m.formals.size();
        for (Dec.T d : m.formals) {
            DecSingle dec = (DecSingle) d;
            size--;
            dec.type.accept(this);
            this.say(" " + dec.id);
            if (size > 0)
                this.say(", ");
        }
        this.sayln(")");
        this.sayln("{");
        this.indent();

        // generate gc-map for formals
        this.printSpaces();
        this.say("char *__arguments_gc_map = \"");
        for (Dec.T f : m.formals) {
            DecSingle dec = (DecSingle) f;
            if (dec.type instanceof ClassType ||
                    dec.type instanceof Type.IntArray)
                this.say("1");
            else
                this.say("0");
        }
        this.sayln("\"; // generate gc-map for formals");

        // generate gc-frame
        this.sayln("");
        this.printSpaces();
        this.sayln("// START generate gc-frame");
        this.printSpaces();
        this.sayln("struct {");
        this.indent();
        this.printSpaces();
        this.sayln("void *__prev;");
        this.printSpaces();
        this.sayln("char *__arguments_gc_map;");
        this.printSpaces();
        this.sayln("void *__arguments_base_address;");
        this.printSpaces();
        this.sayln("unsigned long __locals_gc_number;");
        
        // method specified fields(locals) of reference type
        for (Dec.T d : m.locals) {
            DecSingle dec = (DecSingle) d;
            if (dec.type instanceof Type.IntArray
                    || dec.type instanceof ClassType) {
                this.printSpaces();
                dec.type.accept(this);
                this.sayln(" " + dec.id + ";");
            }
        }

        this.unIndent();
        this.printSpaces();
        this.sayln("} __gc_frame;");
        this.printSpaces();
        this.sayln("// END generate gc-frame\n");
        

        // generate code to push gc-frame
        this.printSpaces();
        this.sayln("// START generate code to push gc-frame");
        this.printSpaces();
        this.sayln("memset(&__gc_frame, 0, sizeof(__gc_frame));//make sure reference init with NULL");
        this.printSpaces();
        this.sayln("__gc_frame.__prev = gc_frame_prev;");
        this.printSpaces();
        this.sayln("__gc_frame.__arguments_gc_map = __arguments_gc_map;");
        this.printSpaces();
        this.sayln("__gc_frame.__arguments_base_address = &this;");
        this.printSpaces();
        this.say("__gc_frame.__locals_gc_number = ");
        int __locals_gc_number = 0;
        for (Dec.T f : m.locals) {
            DecSingle dec = (DecSingle) f;
            if (dec.type instanceof ClassType
                    || dec.type instanceof Type.IntArray)
                __locals_gc_number++;
        }
        this.say(new Integer(__locals_gc_number).toString());
        this.sayln(";");
        this.printSpaces();
        this.sayln("gc_frame_prev = &__gc_frame;");
        this.printSpaces();
        this.sayln("// END generate code to push gc-frame\n");

        // method specified fields(locals) of non-reference type
        this.printSpaces();
        this.sayln("// START method specified fields(locals) of non-reference type");
        for (Dec.T d : m.locals) {
            DecSingle dec = (DecSingle) d;
            if (dec.type instanceof Type.Int) {
                this.printSpaces();
                dec.type.accept(this);
                this.sayln(" " + dec.id + ";");
            }
        }
        this.printSpaces();
        this.sayln("// END method specified fields(locals) of non-reference type\n");

        // method body
       /* String tmp="";
        
        for (String str : id_list) {
            tmp="#define VALUE ( this-> VALUE )";
            tmp=tmp.replaceAll("VALUE", str);
            this.sayln(tmp);
        }
        
        for (String str : class_id_list) {
            tmp="extern struct VALUE_vtable VALUE_vtable_;";
            tmp=tmp.replaceAll("VALUE", str);
            this.sayln(tmp);
            
        for (Dec.T d : m.locals) {
            DecSingle dec = (DecSingle) d;
            this.say("  ");
            dec.type.accept(this);
            this.say(" " + dec.id + ";\n");
        }
        this.sayln("");
        for (Stm.T s : m.stms)
            s.accept(this);
        this.say("  return ");
        m.retExp.accept(this);
        this.sayln(";");
        this.sayln("}");
        }*/
        

        this.printSpaces();
        this.sayln("// START real body");
        for (Stm.T s : m.stms) {
            this.printSpaces();
            s.accept(this);
        }
        this.printSpaces();
        this.sayln("// END real body\n");

        // generate code to pop gc-frame
        this.printSpaces();
        this.sayln("gc_frame_prev = __gc_frame.__prev; // pop gc-frame");

        this.printSpaces();
        this.say("return ");
        m.retExp.accept(this);
        this.sayln(";");
        this.unIndent();
        this.printSpaces();
        this.sayln("}");
        
        
        return;
    }
    
    @Override
    public void visit(MainMethodSingle m)
    {
        this.sayln("void Tiger_main (long __dummy)");
        this.sayln("{//'__dummy' is just a dummy argument to get base address of argument in main");
        this.indent();
        

        // generate gc-frame
        this.printSpaces();
        this.sayln("// START generate gc-frame");
        this.printSpaces();
        this.sayln("struct {");
        this.indent();
        this.printSpaces();
        this.sayln("void *__prev;");
        this.printSpaces();
        this.sayln("char *__arguments_gc_map;");
        this.printSpaces();
        this.sayln("void *__arguments_base_address;");
        this.printSpaces();
        this.sayln("unsigned long __locals_gc_number;");
        // method specified fields(locals) of reference type
        for (Dec.T d : m.locals) {
            DecSingle dec = (DecSingle) d;
            if (dec.type instanceof Type.IntArray
                    || dec.type instanceof ClassType) {
                this.printSpaces();
                dec.type.accept(this);
                this.sayln(" " + dec.id + ";");
            }
        }
        this.unIndent();
        this.printSpaces();
        this.sayln("} __gc_frame;");
        this.printSpaces();
        this.sayln("// END generate gc-frame\n");

        // generate code to push gc-frame
        this.printSpaces();
        this.sayln("// START generate code to push gc-frame");
        this.printSpaces();
        this.sayln("memset(&__gc_frame, 0, sizeof(__gc_frame));//make sure reference init with NULL");
        this.printSpaces();
        this.sayln("__gc_frame.__prev = gc_frame_prev;");
        this.printSpaces();
        this.sayln("__gc_frame.__arguments_gc_map = NULL;");
        this.printSpaces();
        this.sayln("__gc_frame.__arguments_base_address = &__dummy;");
        this.printSpaces();
        this.say("__gc_frame.__locals_gc_number = ");
        int __locals_gc_number = 0;
        for (Dec.T f : m.locals) {
            DecSingle dec = (DecSingle) f;
            if (dec.type instanceof ClassType
                    || dec.type instanceof Type.IntArray)
                __locals_gc_number++;
        }
        this.say(new Integer(__locals_gc_number).toString());
        this.sayln(";");
        this.printSpaces();
        this.sayln("gc_frame_prev = &__gc_frame;");
        this.printSpaces();
        this.sayln("// END generate code to push gc-frame\n");

        this.printSpaces();
        this.sayln("// START real body");
        this.printSpaces();
        m.stm.accept(this);
        this.printSpaces();
        this.sayln("// END real body");

        // generate code to pop gc-frame
        this.printSpaces();
        this.sayln("gc_frame_prev = __gc_frame.__prev; // pop gc-frame");

        this.unIndent();
        this.printSpaces();
        this.sayln("}");
        
        return;
    }
    
    // vtables
    @Override
    public void visit(VtableSingle v)
    {
        this.sayln("struct " + v.id + "_vtable");
        this.sayln("{");
        this.indent();
        this.printSpaces();
        this.sayln("const char *__class_gc_map;");
        for (codegen.C.Ftuple t : v.ms) {
            this.say("  ");
            t.ret.accept(this);
            this.sayln(" (*" + t.id + ")();");
        }
        this.unIndent();
        this.printSpaces();
        this.sayln("};\n");
        return;
    }
    
    private void outputVtable(VtableSingle v)
    {
        this.sayln("struct " + v.id + "_vtable " + v.id + "_vtable_ = ");
        this.sayln("{");

        this.indent();
        // generate class gc map
        this.printSpaces();
        this.say("\"");
        ClassSingle classs = (ClassSingle) v.classs;
        for (codegen.C.Tuple dec : classs.decs) {
            if (dec.type instanceof Type.IntArray ||
                    dec.type instanceof ClassType)
                this.say("1");
            else
                this.say("0");
        }
        this.sayln("\",");


        for (codegen.C.Ftuple t : v.ms) {
            this.printSpaces();
            this.sayln(t.classs + "_" + t.id + ",");
        }
        this.unIndent();
        this.printSpaces();
        this.sayln("};\n");
        
        
        return;
    }
    
    LinkedList<String> id_list=new LinkedList<>();
    LinkedList<String> class_id_list=new LinkedList<>();
   
    // class
    @Override
    public void visit(ClassSingle c)
    {
        //class_id_list.add(c.id); 
        this.sayln("struct " + c.id + " {");
        this.indent();
        this.printSpaces();
        this.sayln("struct " + c.id + "_vtable *vptr;");
        this.printSpaces();
        this.sayln("int __obj_or_array;//0:obj");
        this.printSpaces();
        this.sayln("void *__forwarding;");
        for (codegen.C.Tuple t : c.decs) {
            this.printSpaces();
            t.type.accept(this);
            this.say(" ");
            this.sayln(t.id + ";");
        }
        this.unIndent();
        this.printSpaces();
        this.sayln("};");
        return;
    }
    
    // program
    @Override
    public void visit(ProgramSingle p)
    {
        // we'd like to output to a file, rather than the "stdout".
        try {
            String outputName = null;
            if (Control.ConCodeGen.outputName != null)
                outputName = Control.ConCodeGen.outputName;
            else if (Control.ConCodeGen.fileName != null)
                outputName = Control.ConCodeGen.fileName + ".c";
            else
                outputName = "a.c";
            
            this.writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(outputName)));
            
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        this.sayln("// This is automatically generated by the Tiger compiler.");
        this.sayln("// Do NOT modify!\n");
        this.sayln("#include \"runtime.h\"\n");
        
        this.sayln("#define true 1");
        this.sayln("#define false 0");
        
        this.sayln("// structures");
        for (codegen.C.Ast.Class.T c : p.classes) {
            c.accept(this);
        }
        
        this.sayln("// vtables structures");
        for (Vtable.T v : p.vtables) {
            v.accept(this);
        }
        this.sayln("");
        

        this.sayln("\n// declarations");
        for (Method.T generalM : p.methods) {
            if (generalM instanceof MethodSingle) {
                MethodSingle m = (MethodSingle) generalM;
                m.retType.accept(this);
                this.say(" " + m.classId + "_" + m.id + "(");
                int size = m.formals.size();
                for (Dec.T d : m.formals) {
                    DecSingle dec = (DecSingle) d;
                    size--;
                    dec.type.accept(this);
                    this.say(" " + dec.id);
                    if (size > 0)
                        this.say(", ");
                }
                this.sayln(");");
            } else {
                /* couldn't happen */
                System.err
                        .println("fatal error, method is not of codegen.C.method.Method class");
                System.exit(3);
            }
        }
        this.sayln("");

        
        this.sayln("// vtables");
        for (Vtable.T v : p.vtables) {
            outputVtable((VtableSingle) v);
        }
        this.sayln("");
        
        this.sayln("// methods");
        for (Method.T m : p.methods) {
            m.accept(this);
        }
        this.sayln("");
        
        this.sayln("// main method");
        p.mainMethod.accept(this);
        this.sayln("");
        
        this.say("\n\n");
        
        try {
            this.writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
    }
    
}
