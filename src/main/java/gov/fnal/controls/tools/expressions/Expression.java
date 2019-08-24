/*
 * Copyright 1999, Universities Research Association.  All rights reserved.
 */
package  gov.fnal.controls.tools.expressions;
//{{{ Imports and description
/**Import context classes */
import java.util.*;

/**
 * This class presents an algebraic expression ...
 * When one <B>parse</B> a string with expression it creates a tree presenting this expression.
 * After parsing you can <B>compute</B> an expression with you context.
 * You can also compute an expression without any context, but in this case a DefaultContext
 * object is created and used for calculations. You can look at the DefaultContext from
 * this package. Actually context is used for defining new variables and new functions.
 * All standart java.lang.Math functions are defined in DefaultContext. Variables PI and
 * E are defined in the DefaultContext also.
 * Errors are not generate Exceptions. Probably i will add Exceptions.
 *
 * How to use it:
 * <pre>
 * 	//String is the exp_string  with algebraic expression ...
 * 	Expression exp = new Expression(exp_string);
 * 	...
 * 	value = exp.compute();
 * </pre>
 * OR
 * <pre>
 * 	//String is the exp_string  with algebraic expression ...
 * 	// and you use the variable names defined down inside this expression ...
 * 	Expression exp = new Expression(exp_string);
 * 	...
 * 	String names[]  = {"zappa", "kappa", "e", "MAXVALUE"};
 * 	double values[] = {0,        1,       100, 100000};
 * 	value = exp.compute(names, variables);
 * </pre>
 * OR
 * <pre>
 * 	//String is the exp_string  with algebraic expression ...
 * 	// and you use the variable names defined down inside this expression ...
 * 	// and you use some special functions and/or avriables in this expression
 * 	Expression exp = new Expression(exp_string);
 * 	...
 * 	String names[]  = {"zappa", "kappa", "e", "MAXVALUE"};
 * 	double values[] = {0,        1,       100, 100000};
 * 	Context complex_context = new DefaultContext(names, values){
 * 		public double getValue(String name){
 * 			if(name.equals("setting#1"))
 * 				return business_logic.search_value_in_the_database(name);
 * 			return super.getValue();
 * 		}
 * 		public double computeFunction(String name, double arg){
 * 			if(name.equals("my_specific_function")
 * 				return business_logic.compute_my_specefic_function(arg);
 * 			return super.computeFunction(name, arg);
 * 		}
 * 		public double computeMFunction(String name, double arg[]){
 * 			if(name.equals("another_specific_function"){
 * 				if(arg.length != 3) throw SeriousException("Illegal arguments!");
 * 				return business_logic.compute_another_specific_function(arg[0], arg[1], arg[2]);
 * 			}
 * 			return super.computeMFunction(name, arg);
 * 		}
 * 	};
 *
 * 	value = exp.compute(complex_context);
 * </pre>
 *
 * @author Timofei Bolshakov
 * @version 1
 * @see gov.fnal.controls.tools.expressions.Context
 * @see gov.fnal.controls.tools.expressions.DefaultContext
*///}}}
public class Expression {
    /**
     * create a new Expression object and <b>parse</b> expression String
     *
     * @param s string with expression
     */
    public Expression(String s){ parse(s);}

    /**
     * create a new Expression object
     */
    public Expression(){ ; }
    boolean parsed = false;
    public Object[] getVars(){if(root==null) return null; else return root.getVars(); }

    static final int NULL  = -1;
    static final int VAR   = 0;
    static final int FUNC  = 1;
    static final int BINOP = 2;
    static final int DIGIT = 3;
    static final int NEXT_ARG    = 4;
    static final int CLOSING_PAR = 5;
    static final int OPENING_PAR = 6;
    int token = NULL;

    ExpTree root = null;

    /**
     * Evaluate the expression with user defined context, presented by pre-parsed tree
     *
     * @param c Context for the expression evaluation
     * @return result of evaluation
     */
    public double compute(Context c){return (root==null)?0:((c==null)?compute():root.compute(c)); }

    /**
     * Evaluate the expression with default context, presented by pre-parsed tree
     *
     * @return result of evaluation
     */
    public double compute(){         return (root==null)?0:root.compute(new DefaultContext());   }

    /**
     * Evaluate the expression presented by pre-parsed tree
     * with default context and add to this conetxt
     * user defined variables, speciafied as in the DefaultContext constructor.
     *
     * @param names Names of user defined variables
     * @param values Values of this variables
     * @return result of evaluation
     */
    public double compute(String []names, double []values)
        {         return (root==null)?0:root.compute(new DefaultContext(names, values));   }

    String exp_string;
    StringTokenizer st;
    static final String dividers   = "'+-*/^(), \r\n\t\"&$?|!=@=<=";
    static final String operations = "+-*/^&$?|!=@=<=";
    static final String spaces     = " \r\n\t";

    /**
     * Parse the string with expression and create a new expression tree
     *
     * @param s String to parse
     */
    public void parse(String s){
        exp_string = s; next_one.clear(); st = new StringTokenizer(exp_string, dividers, true);
        root = expr();
    }

    //String next_one = null;
    ArrayList next_one = new ArrayList();

    String getToken(){//{{{
        String s = " ";
        String next = null;
        if(next_one.size()!=0) next = (String)next_one.remove(0);
        if(next!=null && spaces.indexOf(next)==-1){
            String s1 = new String(next);   next = null;
            if(s1.equals("\n"))            {token = NULL;        return null;}
            if(s1.equals(","))             {token = NEXT_ARG;    return s1;}
            if(operations.indexOf(s1)!=-1) {token = BINOP;       return s1;}
            if(s1.equals(")"))             {token = CLOSING_PAR; return s1; }
            s = s1;
        }else{
            try{   while(s!=null && spaces.indexOf(s)!=-1) s = st.nextToken();
            }catch(NoSuchElementException e){   token = NULL;    return null;}


            if(s==null || s.equals("\n") || s.equals("\r") ) {     token = NULL;      return null;}

            try{
                Double d = new Double(s);
                token = DIGIT;   return s;
            }catch(NumberFormatException e){}

            if(s.equals("\'") || s.equals("\"")){
                String this_delimiter = s;
                token = VAR;  s = "";
                try{
                    while(true)
                        { String s1 = st.nextToken();
                            if(s1!=null && s1.equals(this_delimiter)) break;
                            else if(s1!=null)
                                s+=s1;
                        }
                }catch(NoSuchElementException e){token = NULL;  return null;}
                return s;
            }

            if(operations.indexOf(s)!=-1){
                if(s.equals("@")||s.equals("<")||s.equals("!")){
                    next = st.nextToken();
                    if(next.equals("=")){ s+=next; next = null; }
                    else next_one.add(0, next);
                }
                token = BINOP;       return s;
            }
            if(s.equals(","))            { token = NEXT_ARG;    return s;}
            if(s.equals("(")){             token = OPENING_PAR; return s;}
            if(s.equals(")")){             token = CLOSING_PAR; return s;}
        }

        // to divide variables & functions ...
        next = " ";
        try{ while(next!=null && spaces.indexOf(next)!=-1) next = st.nextToken();
        }catch(NoSuchElementException e)                   {token = VAR;  return s;}

        if(next.equals(",") || next.equals(")")) {next_one.add(0, next); token = VAR;  return s;}
        if(operations.indexOf(next)!=-1)         {next_one.add(0, next); token = VAR;  return s;}
        if(next.equals("("))                     {next_one.add(0, next); token = FUNC; return s; }
        token = NULL; return null;
    }//}}}

    /**
     * Abstract class of the tree node I use for building a computational tree
     *
     * @author Timofei Bolshakov
     * @version 1
     */
    public abstract class ExpTree{
        public ExpTree left = null, right = null;
        public ExpTree(){;}
        public ExpTree(ExpTree l, ExpTree r){left = l; right = r; }
        public ExpTree getLeft()    {return left;}
        public ExpTree getRight()   {return right;}
        public void setLeft (ExpTree l) {left  = l; }
        public void setRight(ExpTree r) {right = r; }
        public abstract double  compute(Context c);
        public Object[] getVars(){
            Object l[] = (left==null)?null:left.getVars(), r[]=(right==null)?null:right.getVars();
            if(l==null && r==null) return null;
            HashSet hs = new HashSet();
            if(l!=null) for(int i = 0; i<l.length;  hs.add(l[i++]));
            if(r!=null) for(int i = 0; i<r.length;  hs.add(r[i++]));
            return hs.toArray();
        }
    }

    public abstract class BooleanExpTree extends ExpTree{
        public BooleanExpTree(){ super();}
        public BooleanExpTree(ExpTree l, ExpTree r){super(l,r);}
        public double  compute(Context c){return Double.NaN;}
        public abstract boolean computeBoolean(Context c);
    }

    ExpTree expr(){//{{{
        ExpTree Left = term(); if(Left==null) return null;
        while(true){
            String s = getToken();
            switch(token){
                case CLOSING_PAR:  next_one.add(0, s);
                case NEXT_ARG: case NULL:  return Left;
                case BINOP:
                    if(s.equals("*") || s.equals("/") || s.equals("&"))
                        { Left = term(Left, s); continue;  }

                    ExpTree Right = expr();
                    if(s.equals("+")) return new ExpTree(Left, Right){
                        public double compute(Context c){ return left.compute(c)+right.compute(c);}
                    };
                    if(s.equals("-")) return new ExpTree(Left, Right){
                        public double compute(Context c){ return left.compute(c)-right.compute(c);}
                    };
                    if(s.equals("|")){
                        if(Left instanceof BooleanExpTree && Right instanceof BooleanExpTree)
                            return new BooleanExpTree(Left, Right){
                                public boolean computeBoolean(Context c){
                                    return ((BooleanExpTree)left).computeBoolean(c)|((BooleanExpTree)right).computeBoolean(c);
                                }
                            };
                    }
                    if(s.equals("<")){
                            return new BooleanExpTree(Left, Right){
                                public boolean computeBoolean(Context c)
                                    { return left.compute(c)<right.compute(c); }
                            };
                    }
                    if(s.equals("<=")){
                            return new BooleanExpTree(Left, Right){
                                public boolean computeBoolean(Context c)
                                    { return left.compute(c)<=right.compute(c); }
                            };
                    }
                    if(s.equals("@")){      //use to be '>'
                            return new BooleanExpTree(Left, Right){
                                public boolean computeBoolean(Context c)
                                    { return left.compute(c)>right.compute(c); }
                            };
                    }
                    if(s.equals("@=")){      //use to be '>='
                            return new BooleanExpTree(Left, Right){
                                public boolean computeBoolean(Context c)
                                    { return left.compute(c)>=right.compute(c); }
                            };
                    }
                    if(s.equals("=")){ // Equal with precesion ???
                            return new BooleanExpTree(Left, Right){
                                public boolean computeBoolean(Context c)
                                    { return left.compute(c)==right.compute(c); }
                            };
                    }
                    if(s.equals("!=")){ // Equal with precesion ???
                            return new BooleanExpTree(Left, Right){
                                public boolean computeBoolean(Context c)
                                    { return left.compute(c)!=right.compute(c); }
                            };
                    }
            }
            System.out.println("error in expr. parsing 1!");
            return null;
        }
    }//}}}

    ExpTree term(){//{{{
        ExpTree Left = power(); if(Left==null) return null;
        String s = getToken();
        return term(Left, s);
    }//}}}

    ExpTree term(ExpTree Left, String s){//{{{
        switch(token){
            case NEXT_ARG: case CLOSING_PAR:  next_one.add(0, s);
            case NULL:  return Left;
            case BINOP:
                if(s.equals("*")) {
                  ExpTree Right = power();
                  return new ExpTree(Left, Right){
                    public double compute(Context c){ return left.compute(c)*right.compute(c);}
                  };
                }
                if(s.equals("/")) {
                  ExpTree Right = power();
                  return new ExpTree(Left, Right){
                    public double compute(Context c){ return left.compute(c)/right.compute(c);}
                  };
                }
                if(s.equals("&")) {
                    ExpTree Right = power();
                    if(Left instanceof BooleanExpTree && Right instanceof BooleanExpTree)
                        return new BooleanExpTree(Left, Right){
                            public boolean computeBoolean(Context c){
                                return ((BooleanExpTree)left).computeBoolean(c)&((BooleanExpTree)right).computeBoolean(c);
                            }
                        };
                    System.out.println("error in expr. parsing 2!");
                    return null;
                }
                next_one.add(0, s); return Left;
        }
        System.out.println("error in expr. parsing 3!");
        return null;
    }//}}}

    ExpTree power(){//{{{
        ExpTree Left = atom(); if(Left==null) return null;
        String s = getToken();
        switch(token){
            case NEXT_ARG: case CLOSING_PAR:  next_one.add(0, s);
            case NULL:     return Left;
            case BINOP:
                if(s.equals("^")){
                    ExpTree Right = power();
                    return new ExpTree(Left, Right){
                        public double compute(Context c)
                           { return Math.pow(left.compute(c), right.compute(c)); }
                    };
                }else{ next_one.add(0, s); return Left;}
        }
        System.out.println("error in expr. parsing 4!");
        return null;
    }//}}}

    ExpTree atom(){//{{{
       String s = getToken();
       switch(token){
            case DIGIT: { final double val = (new Double(s)).doubleValue();
                        return new ExpTree(){
                            public double compute(Context c){ return val; }
                        };
            }
            case VAR:{  final String name = new String(s);
                        double val = Double.NaN;
                        try{ val = Double.parseDouble( name ); }catch(Exception exc){ val = Double.NaN; }
                        if( Double.isNaN( val ) )
                            return new ExpTree(){
                                public double compute(Context c) {return c.getValue(name); }
                                public Object[] getVars(){ return new Object[]{name};}
                            };
                        else{
                            final double v = val;
                            return new ExpTree(){
                                public double compute(Context c){ return v; }
                            };
                        }
            }
            case FUNC:
                ArrayList args  = new ArrayList();
                ExpTree   expression;
                if(next_one.size()>0) next_one.remove(0); // eliminate (
                while((expression = expr())!=null) args.add(expression);
                if(next_one.size()>0)next_one.remove(0);  // eliminate )
                final String name = new String(s);
                System.out.println("before condif name="+name+" args length is "+args.size());
                if(name.equalsIgnoreCase("condif")){
                        final Object [] expressions = args.toArray();
                        if(!(expressions[0] instanceof BooleanExpTree)) break;
                        if(expressions.length<3) break;
                        return new ExpTree(){
                            public double compute(Context c){
                                boolean flag = ((BooleanExpTree)expressions[0]).computeBoolean(c);
                                double []dargs  = new double [expressions.length-1];
                                for(int i=0; i<dargs.length; i++)
                                    dargs[i] = ((ExpTree)expressions[i+1]).compute(c);
                                return flag?dargs[0]:dargs[1];
                            }
                            public Object[] getVars(){
                                HashSet al = new HashSet();
                                for(int i = 0; i<expressions.length; i++){
                                    Object []vars = ((ExpTree)expressions[i]).getVars();
                                    if(vars==null || vars.length==0) continue;
                                    for(int j = 0; j<vars.length; j++) al.add(vars[j]);
                                }
                                return al.toArray();
                            }
                        };
                 }else if(args.size()>1){
                        final Object [] expressions = args.toArray();
                        return new ExpTree(){
                            public double compute(Context c){
                                double []dargs  = new double [expressions.length];
                                for(int i=0; i<dargs.length; i++)
                                    dargs[i] = ((ExpTree)expressions[i]).compute(c);
                                return c.computeMFunction(name, dargs);
                            }
                            public Object[] getVars(){
                                HashSet al = new HashSet();
                                for(int i = 0; i<expressions.length; i++){
                                    Object []vars = ((ExpTree)expressions[i]).getVars();
                                    if(vars==null || vars.length==0) continue;
                                    for(int j = 0; j<vars.length; j++) al.add(vars[j]);
                                }
                                return al.toArray();
                            }
                        };
                }else if(name.equalsIgnoreCase("isError")){
                        final ExpTree e = (ExpTree)args.get(0);
                        return new BooleanExpTree(){
                            public boolean computeBoolean(Context c){
                                if(e!=null){
                                    double arg  = e.compute(c);
                                    return Double.isNaN(arg);
                                }else return true;
                            }
                            public Object[] getVars(){	return e.getVars();	}
                        };
                 }else{
                        final ExpTree e = (ExpTree)args.get(0);
                        return new ExpTree(){
                            public double compute(Context c){
                                if(e!=null){
                                    double arg  = e.compute(c);
                                    return c.computeFunction(name, arg);
                                }else return c.computeFunction(name, Double.NaN);
                            }
                            public Object[] getVars(){	return e.getVars();	}
                        };
                 }
              case OPENING_PAR:  ExpTree e = expr(); if(next_one.size()>0) next_one.remove(0);  return e; // test for right bracket
              case NEXT_ARG: case CLOSING_PAR:  next_one.add(0, s);   return null;
              case BINOP:
                if(s.equals("-")){
                    ExpTree Right = term();
                    return new ExpTree(null, Right){
                        public double compute(Context c){ return -right.compute(c);}
                    };
                }
                if(s.equals("!")){
                    ExpTree Right = atom();
                    if(Right instanceof BooleanExpTree) return new BooleanExpTree(null, Right){
                        public boolean computeBoolean(Context c){ return !((BooleanExpTree)right).computeBoolean(c);}
                    };
                }
              break;
      }
      System.out.println("error in expr. parsing 5!");
      return null;
   }//}}}

   /**
    * Test the expression class.
    * Also provide examples how to use it.
    */
   public static void main(String []args){//{{{
      String var_names[] = {"x", "y", "z", "spaced variable"};
      double vars     [] = {100, 200, 300, 1};
      Expression e = new Expression();
      e.parse("condif((x@0), y*10000.0/z, 0.0)");
//      e.parse("y*10000.0/z");
      System.out.println(e.compute(new DefaultContext(var_names, vars)));
      while(true){
        System.out.print("Input an expression. Empty line terminate programm.\n>");
        byte b[] = new byte[128]; int byte_count;
        try{ byte_count = System.in.read(b); }catch(Exception exc){continue;}
        String str = new String(b, 0, byte_count-1); str.trim();
        if(str.length()==0) System.exit(0);
        System.out.println(str);
        e.parse(str);
        System.out.println("Result is " +
           e.compute(new DefaultContext(var_names, vars)));
      }
   }//}}}
}
