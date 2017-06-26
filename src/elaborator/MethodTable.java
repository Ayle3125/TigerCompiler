package elaborator;

import java.util.LinkedList;

import ast.Ast.Dec;
import ast.Ast.Type;

public class MethodTable
{
    private java.util.Hashtable<String, Type.T> table;
    public java.util.Hashtable<String, Type.T> formals;
    public java.util.Hashtable<String, Type.T> locals;
    
    public MethodTable()
    {
        this.table = new java.util.Hashtable<String, Type.T>();
    }
    
    // Duplication is not allowed
    public void put(LinkedList<Dec.T> formals,
            LinkedList<Dec.T> locals)
    {
        this.formals = new java.util.Hashtable<String, Type.T>();
        this.locals = new java.util.Hashtable<String, Type.T>();

        for (Dec.T dec : formals) {
            Dec.DecSingle decc = (Dec.DecSingle) dec;
            if (this.table.get(decc.id) != null) {
                System.out.println("duplicated parameter: " + decc.id);
                System.exit(1);
            }
            this.table.put(decc.id, decc.type);
            this.formals.put(decc.id, decc.type);
        }
        
        for (Dec.T dec : locals) {
            Dec.DecSingle decc = (Dec.DecSingle) dec;
            if (this.table.get(decc.id) != null) {
                System.out.println("duplicated variable: " + decc.id);
                System.exit(1);
            }
            this.table.put(decc.id, decc.type);
            this.locals.put(decc.id, decc.type);
        }
        
    }
    
    // return null for non-existing keys
    public Type.T get(String id)
    {
        return this.table.get(id);
    }
    
    public void dump()
    {
        System.out.println(this.table);
    }
    
    @Override
    public String toString()
    {
        return this.table.toString();
    }
}
