/*
 * Copyright 1999, Universities Research Association.  All rights reserved.
 */
package  gov.fnal.controls.tools.expressions;
//{{{ imports and description
/**Import context classes */
import java.util.*;
import java.io.Serializable;

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
 */
//}}}
public class ObjectExpression extends Expression implements Serializable{
    public ObjectExpression(String s){super(); parse(s);}
    public ObjectExpression(){super();}

    public static Object computeObject(String exp, ObjectContext c){  return (new ObjectExpression(exp)).computeObject(c); }
    public static Object computeObject(String exp){  return (new ObjectExpression(exp)).computeObject(); }
    public static Object computeObject(String exp, String []names, double []values)
        {         return (new ObjectExpression(exp)).computeObject(new DefaultObjectContext(names, values));   }
    /**
     * Evaluate the expression with user defined context, presented by pre-parsed tree
     *
     * @param c Context for the expression evaluation
     * @return result of evaluation
     */
    public Object computeObject(ObjectContext c){return (objectRoot==null)?null:((c==null)?computeObject():objectRoot.computeObject(c)); }

    public Object[] getVars(){if(objectRoot==null) return null; else return objectRoot.getVars(); }

    public void parse(String s){
        exp_string = s; next_one.clear(); st = new StringTokenizer(exp_string, dividers, true);
        objectRoot = objectExpr();
        root = objectRoot;
    }

    /**
     * Evaluate the expression with default context, presented by pre-parsed tree
     *
     * @return result of evaluation
     */
    public Object computeObject(){   return (objectRoot==null)?null:objectRoot.computeObject(new DefaultObjectContext());   }

    /**
     * Evaluate the expression presented by pre-parsed tree
     * with default context and add to this conetxt
     * user defined variables, speciafied as in the DefaultContext constructor.
     *
     * @param names Names of user defined variables
     * @param values Values of this variables
     * @return result of evaluation
     */
    public Object computeObject(String []names, double []values)
        {         return (objectRoot==null)?null:objectRoot.computeObject(new DefaultObjectContext(names, values));   }

    /**
     * Abstract class of the tree node I use for building a computational tree
     *
     * @author Timofei Bolshakov
     * @version 1
     */
    public abstract class ObjectExpTree extends Expression.BooleanExpTree implements Serializable{
        ObjectExpTree left = null, right = null;
        public ObjectExpTree(){}
        public ObjectExpTree(ObjectExpTree l, ObjectExpTree r){left = l; right = r; }
        public void setLeft (ObjectExpTree l) {left  = l; }
        public void setRight(ObjectExpTree r) {right = r; }
        abstract public Object computeObject(ObjectContext c);
        public boolean computeBoolean(Context c){return false;}
        public Object[] getVars(){
            Object l[] = (left==null)?null:left.getVars(), r[]=(right==null)?null:right.getVars();
            if(l==null && r==null) return null;
            HashSet hs = new HashSet();
            if(l!=null) for(int i = 0; i<l.length;  hs.add(l[i++]));
            if(r!=null) for(int i = 0; i<r.length;  hs.add(r[i++]));
            return hs.toArray();
        }
    }

    ObjectExpTree objectRoot = null;

    ObjectExpTree objectExpr(){
        ObjectExpTree Left = objectTerm(); if(Left==null) return null;
        while(true){
            String s = getToken();
            switch(token){
                case CLOSING_PAR:  next_one.add(0, s);
                case NEXT_ARG: case NULL:  return Left;
                case BINOP:
                        //if(s.equals("*") || s.equals("/") || s.equals("&")) return objectTerm(Left, s);
                    if(s.equals("*") || s.equals("/") || s.equals("&"))
                       { Left = objectTerm(Left, s); continue;  }

                    ObjectExpTree Right = objectExpr();
                    if(s.equals("+")) return new ObjectExpTree(Left, Right){
                        public double compute(Context c){ return left.compute(c)+right.compute(c);}
                        public Object computeObject(ObjectContext c){
                            if(left==null || right==null) return new Double(Double.NaN);
                            Object l = left.computeObject(c), r = right.computeObject(c);
                            return c.addObjects(l, r);
                        }
                    };
                    if(s.equals("-")) return new ObjectExpTree(Left, Right){
                        public double compute(Context c){ return left.compute(c)-right.compute(c);}
                        public Object computeObject(ObjectContext c){
                            if(left==null || right==null) return new Double(Double.NaN);
                            Object l = left.computeObject(c), r = right.computeObject(c);
                            return c.subObjects(l, r);
                        }
                    };
                    if(s.equals("|")){
                        return new ObjectExpTree(Left, Right){
                            public boolean computeBoolean(Context c)
                                { return ((BooleanExpTree)left).computeBoolean(c)|((BooleanExpTree)right).computeBoolean(c); }
                            public Object computeObject(ObjectContext c){
                                if(left==null || right==null) return new Double(Double.NaN);
                                Object l = left.computeObject(c), r = right.computeObject(c);
                                if(l instanceof Boolean && r instanceof Boolean)
                                    return new Boolean(((Boolean)l).booleanValue()|((Boolean)r).booleanValue());
                                return null;
                            }
                        };
                    }
                    if(s.equals("<")){
                        return new ObjectExpTree(Left, Right){
                            public boolean computeBoolean(Context c)
                                { return  left.compute(c)<right.compute(c); }
                            public Object computeObject(ObjectContext c){
                                if(left==null || right==null) return new Double(Double.NaN);
                                Object l = left.computeObject(c), r = right.computeObject(c);
                                return c.lessObjects(l, r);
                            }
                        };
                    }
                    if(s.equals("<=")){
                        return new ObjectExpTree(Left, Right){
                            public boolean computeBoolean(Context c)
                                { return left.compute(c)<=right.compute(c); }
                            public Object computeObject(ObjectContext c){
                                if(left==null || right==null) return new Double(Double.NaN);
                                Object l = left.computeObject(c), r = right.computeObject(c);
                                return new Boolean(c.lessObjects(l, r).booleanValue() | c.equalObjects(l, r).booleanValue());
                            }
                        };
                    }
                    if(s.equals("@")){      //use to be '>'
                        return new ObjectExpTree(Left, Right){
                            public boolean computeBoolean(Context c)
                                { return left.compute(c)>right.compute(c); }
                            public Object computeObject(ObjectContext c){
                                if(left==null || right==null) return new Double(Double.NaN);
                                Object l = left.computeObject(c), r = right.computeObject(c);
                                return c.greaterObjects(l, r);
                            }
                        };
                    }
                    if(s.equals("@=")){      //use to be '>='
                        return new ObjectExpTree(Left, Right){
                            public boolean computeBoolean(Context c)
                                { return left.compute(c)>=right.compute(c); }
                            public Object computeObject(ObjectContext c){
                                if(left==null || right==null) return new Double(Double.NaN);
                                Object l = left.computeObject(c), r = right.computeObject(c);
                                return new Boolean(c.greaterObjects(l, r).booleanValue() | c.equalObjects(l, r).booleanValue());
                            }
                        };
                    }
                    if(s.equals("=")){ // Equal with precesion ???
                        return new ObjectExpTree(Left, Right){
                            public boolean computeBoolean(Context c)
                                { return left.compute(c)==right.compute(c); }
                            public Object computeObject(ObjectContext c){
                                if(left==null || right==null) return new Double(Double.NaN);
                                Object l = left.computeObject(c), r = right.computeObject(c);
                                return c.equalObjects(l, r);
                            }
                        };
                    }
                    if(s.equals("!=")){ // Equal with precesion ???
                        return new ObjectExpTree(Left, Right){
                            public boolean computeBoolean(Context c)
                                { return left.compute(c)!=right.compute(c); }
                            public Object computeObject(ObjectContext c){
                                if(left==null || right==null) return new Double(Double.NaN);
                                Object l = left.computeObject(c), r = right.computeObject(c);
                                return new Boolean(!c.equalObjects(l, r).booleanValue());
                            }
                        };
                    }
            }
            System.out.println("error in expr. parsing! - 1");
            return null;
        }
    }

    ObjectExpTree objectTerm(){
        ObjectExpTree Left = objectPower(); if(Left==null) return null;
        String s = getToken();
        return objectTerm(Left, s);
    }

    ObjectExpTree objectTerm(ObjectExpTree Left, String s){
        switch(token){
            case NEXT_ARG: case CLOSING_PAR:  next_one.add(0, s);
            case NULL:  return Left;
            case BINOP:
                if(s.equals("*")) {
                  ObjectExpTree Right = objectPower();
                  return new ObjectExpTree(Left, Right){
                    public double compute(Context c){ return left.compute(c)*right.compute(c);}
                    public Object computeObject(ObjectContext c){
                        Object l = left.computeObject(c), r = right.computeObject(c);
                        return c.mulObjects(l, r);
                    }
                  };
                }
                if(s.equals("/")) {
                  ObjectExpTree Right = objectPower();
                  return new ObjectExpTree(Left, Right){
                    public double compute(Context c){ return left.compute(c)/right.compute(c);}
                    public Object computeObject(ObjectContext c){
                            Object l = left.computeObject(c), r = right.computeObject(c);
                            return c.divObjects(l, r);
                    }
                  };
                }
                if(s.equals("&")) {
                    ObjectExpTree Right = objectPower();
                    return new ObjectExpTree(Left, Right){
                        public boolean computeBoolean(Context c)
                            { return ((BooleanExpTree)left).computeBoolean(c)&((BooleanExpTree)right).computeBoolean(c);  }
                        public Object computeObject(ObjectContext c){
                                Object l = left.computeObject(c), r = right.computeObject(c);
                                if(l instanceof Boolean && r instanceof Boolean)
                                    return new Boolean(((Boolean)l).booleanValue()&((Boolean)r).booleanValue());
                                return null;
                        }
                    };
                }
                next_one.add(0, s); return Left;
        }
        System.out.println("error in expr. parsing! - 2 "+token+" "+s);
        return null;
    }

    ObjectExpTree objectPower(){
        ObjectExpTree Left = objectAtom(); if(Left==null) return null;
        String s = getToken();
        switch(token){
            case NEXT_ARG: case CLOSING_PAR:  next_one.add(0, s);
            case NULL:     return Left;
            case BINOP:
                if(s.equals("^")){
                    ObjectExpTree Right = objectPower();
                    return new ObjectExpTree(Left, Right){
                        public double compute(Context c)
                           { return Math.pow(left.compute(c), right.compute(c)); }
                        public Object computeObject(ObjectContext c){
                            Object l = left.computeObject(c), r = right.computeObject(c);
                            return c.powObjects(l, r);
                        }
                    };
                }else{ next_one.add(0, s); return Left;}
        }
        System.out.println("error in expr. parsing! - 3 "+token+" "+s);
        return null;
    }

    ObjectExpTree objectAtom(){
       String s = getToken();
       switch(token){
            case DIGIT: { final double val = (new Double(s)).doubleValue();
                        return new ObjectExpTree(){
                            public double compute(Context c){ return val; }
                            public Object computeObject(ObjectContext c) {return new Double(val); }
                        };
            }
            case VAR:{  final String name = new String(s);
                        double val = Double.NaN;
                        try{ val = Double.parseDouble( name ); }catch(Exception exc){ val = Double.NaN; }
                        if( Double.isNaN( val ) )
                            return new ObjectExpTree(){
                                public double compute(Context c) {return c.getValue(name); }
                                public Object computeObject(ObjectContext c) {return c.getObjectValue(name); }
                                public Object[] getVars(){ return new Object[]{name};}
                            };
                        else{
                            final double v = val;
                            return new ObjectExpTree(){
                                public double compute(Context c){ return v; }
                                public Object computeObject(ObjectContext c) {return new Double(v); }
                            };
                        }
            }
            case FUNC:
                ArrayList args  = new ArrayList();
                ObjectExpTree   expression;
                if(next_one.size()>0) next_one.remove(0); // eliminate (
                while((expression = objectExpr())!=null) args.add(expression);
                if(next_one.size()>0) next_one.remove(0); // eliminate )
                final String name = new String(s);
                if(name.equalsIgnoreCase("condif")){
                    final Object [] expressions = args.toArray();
                    if(expressions.length<3) break;
                    return new ObjectExpTree(){
                        public double compute(Context c){
                            boolean flag = ((BooleanExpTree)expressions[0]).computeBoolean(c);
                            double []dargs  = new double [expressions.length-1];
                            for(int i=0; i<dargs.length; i++)
                                dargs[i] = ((ExpTree)expressions[i+1]).compute(c);
                            return flag?dargs[0]:dargs[1];
                        }
                        public Object computeObject(ObjectContext c){
                            Object []dargs  = new Object [expressions.length];
                            for(int i=0; i<dargs.length; i++)
                                dargs[i] = ((ObjectExpTree)expressions[i]).computeObject(c);
                            if(dargs[0] instanceof Boolean)
                                return ((Boolean)dargs[0]).booleanValue()?dargs[1]:dargs[2];
                            else return null;
                        }
                        public Object[] getVars(){
                            HashSet al = new HashSet();
                            for(int i = 0; i<expressions.length; i++){
                                Object []vars = ((ObjectExpTree)expressions[i]).getVars();
                                if(vars==null || vars.length==0) continue;
                                for(int j = 0; j<vars.length; j++) al.add(vars[j]);
                            }
                            return al.toArray();
                        }
                    };
             }else 	if(args.size()>1){
                    final Object [] expressions = args.toArray();
                    return new ObjectExpTree(){
                        public double compute(Context c){
                            double []dargs  = new double [expressions.length];
                            for(int i=0; i<dargs.length; i++)
                                dargs[i] = ((ObjectExpTree)expressions[i]).compute(c);
                            return c.computeMFunction(name, dargs);
                        }
                        public Object computeObject(ObjectContext c){
                            Object []dargs  = new Object [expressions.length];
                            for(int i=0; i<dargs.length; i++)
                                dargs[i] = ((ObjectExpTree)expressions[i]).computeObject(c);
                            return c.computeObjectMFunction(name, dargs);
                        }
                        public Object[] getVars(){
                            HashSet al = new HashSet();
                            for(int i = 0; i<expressions.length; i++){
                                Object []vars = ((ObjectExpTree)expressions[i]).getVars();
                                if(vars==null || vars.length==0) continue;
                                for(int j = 0; j<vars.length; j++) al.add(vars[j]);
                            }
                            return al.toArray();
                        }
                    };
                 }else if(args.size()==1){
                    final ObjectExpTree e = (ObjectExpTree)args.get(0);
                    return new ObjectExpTree(){
                        public double compute(Context c)
                            { return c.computeFunction(name, e.compute(c)); }
                        public Object computeObject(ObjectContext c)
                            { return c.computeObjectFunction(name, e.computeObject(c)); }
                        public Object[] getVars(){	return e.getVars();	}
                    };
                 }else{
                    return new ObjectExpTree(){
                        public double compute(Context c)
                            { return c.computeFunction(name, Double.NaN); }
                        public Object computeObject(ObjectContext c)
                            { return c.computeObjectFunction(name, null); }
                    };
                 }
              case OPENING_PAR:  
                  ObjectExpTree e = objectExpr(); 
                  if(next_one.size()>0) next_one.remove(0); 
                  return e; // test for right bracket
              case NEXT_ARG: case CLOSING_PAR:  next_one.add(0, s);   return null;

              case BINOP:
                if(s.equals("-")){
                    //System.out.println("Before rigth");
                    ObjectExpTree Right = objectTerm();
                    //System.out.println("After rigth ");
                    return new ObjectExpTree(null, Right){
                        public double compute(Context c){ return -right.compute(c);}
                        public Object computeObject(ObjectContext c){
                            Object r = right.computeObject(c);
                            return c.subObjects(new Double(0), r);
                        }
                    };
                }
                if(s.equals("!")){
                    ObjectExpTree Right = objectAtom();
                    return new ObjectExpTree(null, Right){
                        public boolean computeBoolean(Context c){ return !((BooleanExpTree)right).computeBoolean(c);}
                        public Object computeObject(ObjectContext c){
                            Object r = right.computeObject(c);
                            if(r instanceof Boolean) return new Boolean(!((Boolean)r).booleanValue());
                            return null;
                        }
                    };
                }
              break;

     }
     System.out.println("error in expr. parsing! - 4 "+token+" "+s);
     return null;
   }

   /**
    * Test the expression class.
    * Also provide examples how to use it.
    */
   public static void main(String []args){
      String var_names[] = {"x", "y", "z", "spaced variable"};
      double vars     [] = {100, 200, 300, 1};
      ObjectExpression e = new ObjectExpression();
      e.parse("condif((x @ 0.0), y*10000.0/z, 0.0)");
//      e.parse("condif((x @ 0.0), y*10000.0/z, 0.0)");
      System.out.println(e.compute(new DefaultObjectContext(var_names, vars)));
      while(true){
        System.out.print("Input an expression. Empty line terminate programm.\n>");
        byte b[] = new byte[128]; int byte_count;
        try{ byte_count = System.in.read(b); }catch(Exception exc){continue;}
        String str = new String(b, 0, byte_count-1); str.trim();
        if(str.length()==0) System.exit(0);
        System.out.println(str);
        e.parse(str);
        System.out.println("Result is " + e.compute(new DefaultObjectContext(var_names, vars)));
      }
   }
}
