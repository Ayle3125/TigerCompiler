package codegen.bytecode;

import codegen.bytecode.Ast.*;

public class ClassTable {
	private java.util.Hashtable<String, ClassBinding> table;

	public ClassTable() {
		this.table = new java.util.Hashtable<String, ClassBinding>();
	}

	public void init(String current, String extendss) {
		this.table.put(current, new ClassBinding(extendss));
		return;
	}

	public void initDecs(String current, java.util.LinkedList<Dec.T> decs) {
		ClassBinding cb = this.table.get(current);
		for (Dec.T dec : decs) {
			Dec.DecSingle decc = (Dec.DecSingle) dec;
			cb.put(current, decc.type, decc.id);
		}
		this.table.put(current, cb);
	}

	public void initMethod(String current, Type.T ret, java.util.LinkedList<Dec.T> args, String mid) {
		ClassBinding cb = this.table.get(current);
		cb.putm(current, ret, args, mid);
		return;
	}

	public void inherit(String c) {
		ClassBinding cb = this.table.get(c);
		if (cb.visited)
			return;

		if (cb.extendss == null) {
			cb.visited = true;
			return;
		}

		inherit(cb.extendss);

		ClassBinding pb = this.table.get(cb.extendss);
		// this tends to be very slow...
		// need a much fancier data structure.
		java.util.LinkedHashMap<String, Tuple> newFields = new java.util.LinkedHashMap<String, Tuple>();
		newFields.putAll(pb.fields);
		newFields.putAll(cb.fields);

		cb.updateFields(newFields);

		// methods;
		java.util.LinkedHashMap<String, Ftuple> newMethods = new java.util.LinkedHashMap<String, Ftuple>();
		newMethods.putAll(pb.methods);
		newMethods.putAll(cb.methods);
		cb.updateMethods(newMethods);
		// set the mark
		cb.visited = true;
		return;
	}

	// return null for non-existing keys
	public ClassBinding get(String c) {
		if (c == null)
			return null;
		return this.table.get(c);
	}

	@Override
	public String toString() {
		return this.table.toString();
	}
}
