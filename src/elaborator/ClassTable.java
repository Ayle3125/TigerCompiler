package elaborator;

import java.util.Iterator;

import ast.Ast.Dec;
import ast.Ast.Type;

public class ClassTable
{
    // map each class name (a string), to the class bindings.
    private java.util.Hashtable<String, ClassBinding> table;
    
    public ClassTable()
    {
        this.table = new java.util.Hashtable<String, ClassBinding>();
    }
    
    // Duplication is not allowed
    public void put(String c, ClassBinding cb)
    {
        if (this.table.get(c) != null) {
            System.out.println("duplicated class: " + c);
            System.exit(1);
        }
        this.table.put(c, cb);
    }
    
    // put a field into this table
    // Duplication is not allowed
    public void put(String c, String id, Type.T type)
    {
        ClassBinding cb = this.table.get(c);
        cb.put(id, type);
        return;
    }
    
    // put a method into this table
    // Duplication is not allowed.
    // Also note that MiniJava does NOT allow overloading.
    public void put(String c, String id, MethodType type)
    {
        ClassBinding cb = this.table.get(c);
        cb.put(id, type);
        return;
    }
    
    // return null for non-existing class
    public ClassBinding get(String className)
    {
        return this.table.get(className);
    }
    
    // get type of some field
    // return null for non-existing field.
    public Type.T get(String className, String xid)
    {
        ClassBinding cb = this.table.get(className);
        Type.T type = cb.fields.get(xid);
        while (type == null) { // search all parent classes until found or fail
            if (cb.extendss == null)
                return type;
            
            cb = this.table.get(cb.extendss);
            type = cb.fields.get(xid);
        }
        return type;
    }
    
    // get type of some method
    // return null for non-existing method
    public MethodType getm(String className, String mid)
    {
        ClassBinding cb = this.table.get(className);
        MethodType type = cb.methods.get(mid);
        while (type == null) { // search all parent classes until found or fail
            if (cb.extendss == null)
                return type;
            
            cb = this.table.get(cb.extendss);
            type = cb.methods.get(mid);
        }
        return type;
    }
    
    public void dump()
    {
        for (Iterator<String> it = table.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            ClassBinding value = table.get(key);
            System.out.println("classname:\t" + key);

            System.out.print("extends: ");
            if (value.extendss != null)
                System.out.println(value.extendss);
            else
                System.out.println("<>");
            System.out.println("fields:");
            for (Iterator<String> itt = value.fields.keySet().iterator(); itt.hasNext();) {
                String keyy = itt.next();
                Type.T valuee = value.fields.get(keyy);
                System.out.println("\t" + keyy + "：" + valuee);
            }
            System.out.println("methods:");
            for (Iterator<String> itt = value.methods.keySet().iterator(); itt.hasNext();) {
                String keyy = itt.next();
                MethodType valuee = value.methods.get(keyy);
                System.out.println("\t" + keyy + "：\t");
                System.out.print("\t\tArguements:\t");
                for (Dec.T dec : valuee.argsType) {
                    Dec.DecSingle decc = (Dec.DecSingle) dec;
                    System.out.print(decc.type.toString() + "*\t");
                }
                System.out.println("\n\t\tReturn value:\t" + valuee.retType);
            }

            System.out.println("======================================");
        }
    }
    
    @Override
    public String toString()
    {
        return this.table.toString();
    }
}
