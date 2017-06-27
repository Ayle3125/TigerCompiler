package elaborator;

import java.util.Iterator;
import java.util.LinkedList;

import ast.Ast.Dec;
import ast.Ast.Type;

public class MethodTable
{
    private java.util.Hashtable<String, Type.T> table;
    public java.util.Hashtable<String, Type.T> formals;
    public java.util.Hashtable<String, Boolean> locals;//if init
    public java.util.Hashtable<String, Boolean> localValIsUseTable;//if used

    
    public MethodTable(){
        this.table = new java.util.Hashtable<String, Type.T>();
        this.formals = new java.util.Hashtable<String, Type.T>();
        this.locals = new java.util.Hashtable<String, Boolean>();
        this.localValIsUseTable = new java.util.Hashtable<String, Boolean>();

    }
    
    // Duplication is not allowed
    public void put(LinkedList<Dec.T> formals,LinkedList<Dec.T> locals)
    {
        this.formals = new java.util.Hashtable<String, Type.T>();
        this.locals = new java.util.Hashtable<String, Boolean>();

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
            this.locals.put(decc.id, false);
            this.localValIsUseTable.put(decc.id, false);
        }
        
    }
    
    // return null for non-existing keys
    public Type.T get(String id)
    {
        return this.table.get(id);
    }

    public void initLocalVal(String id) {
        if (id == null || !this.locals.containsKey(id))
            return;
        this.locals.remove(id);
        this.locals.put(id, true);
    }

    public boolean isInit(String id) {
        if (id == null || !this.locals.containsKey(id))
            return true;
        return this.locals.get(id);
    }

    public void useLocalVal(String id) {
        if (id == null || !this.localValIsUseTable.containsKey(id))
            return;
        this.localValIsUseTable.remove(id);
        this.localValIsUseTable.put(id, true);
    }

    public boolean isuse(String id) {
        if (id == null || !this.localValIsUseTable.containsKey(id))
            return true;
        return this.localValIsUseTable.get(id);
    }

    public void dump()
    {
        System.out.println("variables：");
        for (Iterator<String> it = table.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            Type.T value = table.get(key);
            System.out.println("\t" + key + ":\t" + value);
        }
        System.out.println("==============");
    }
    
    @Override
    public String toString()
    {
        return this.table.toString();
    }
}
