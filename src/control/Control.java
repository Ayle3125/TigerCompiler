package control;

public class Control
{
    // the lexer
    public static class ConLexer
    {
        public static boolean test = false;
        public static boolean dump = false;
    }
    
    // the straight-line program interpreter
    public static class ConSlp
    {
        public enum T{NONE, ARGS, INTERP, COMPILE, TEST, DIV};
        
        public static T action = T.NONE;
        public static boolean div = false;
        public static boolean keepasm = false;
    }
    
    // Ast and elaborator
    public static class ConAst
    {
        public static boolean dumpAst = false;
        
        // elaborator
        public static boolean elabClassTable = false;
        public static boolean elabMethodTable = false;
    }
    
    // generate Java bytecode.
    public static class ConCodeGen
    {
        public enum Kind_t {
            Bytecode, C
        }
        
        public static Kind_t codegen = Kind_t.Bytecode;
        
        // output .s file
        public static String fileName = null;
        
        public static String outputName = null;
        
    }
}
