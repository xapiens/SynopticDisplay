/*
 * Copyright 1999, Universities Research Association.  All rights reserved.
 */
package  gov.fnal.controls.tools.expressions;
/**
 * It is a default class for computational context for expressions
 * It is used for defining standart variables (PI & E)
 * and standart java.lang.Math functions.
 * It can be used as "adapter" for constructing your own contexts
 *
 * @author Timofei Bolshakov
 * @version 1
 * @see gov.fnal.controls.tools.expressions.Context
 * @see gov.fnal.controls.tools.expressions.Expression
 */
public class DefaultObjectContext extends DefaultContext implements ObjectContext{
    public boolean haveDoubleVal(Object l){return l instanceof Number;}
    public double  doubleVal(Object l){    if(l instanceof Number) return ((Number)l).doubleValue(); else  return Double.NaN; }

    public DefaultObjectContext(){ super();}
    /**
     * Create a context with PI, E and user defined variables
     * names are names of this variables and values are values ...
     *
     * @param names array of names for user defined variables
     * @param values array of values for the names
     */
    public DefaultObjectContext(String []names, double values[]){ super(names, values);}
    /** Get the value of variable name */
    public Object getObjectValue(String name){
        // It is the place to generate VariableNotFoundException
        return variables.get(name);
    }
    /** Set the value of variable name */
    public void   setObjectValue(String name, Object val){
        variables.put(name, val);
    }
    /** Compute a function from one argument ... */
    public Object computeObjectFunction (String name, Object arg){
        if(name.equalsIgnoreCase("isError"))
            return new Boolean(
                    arg==null ||
                   (haveDoubleVal(arg) && Double.isNaN(doubleVal(arg)))
            );
        else if(haveDoubleVal(arg)){ return new Double(super.computeFunction(name, doubleVal(arg)));}
        else return new Double(Double.NaN);
    }
    /** Compute a function from many argument ... */
    public Object computeObjectMFunction (String name, Object []arg){
    double double_args[] = new double[arg.length];
        for(int i=0; i<arg.length; i++)
            if(haveDoubleVal(arg[i])) double_args[i] = doubleVal(arg[i]);
            else return new Double(Double.NaN);
        return new Double(super.computeMFunction(name, double_args));
    }

    public Object addObjects(Object l, Object r){
        if(haveDoubleVal(l) && haveDoubleVal(r))
            return new Double( doubleVal(l) + doubleVal(r) );
        else return new Double(Double.NaN);
    }
    public Object subObjects(Object l, Object r){
        if(haveDoubleVal(l) && haveDoubleVal(r))
            return new Double( doubleVal(l) - doubleVal(r) );
        else return new Double(Double.NaN);
    }
    public Object mulObjects(Object l, Object r){
        if(haveDoubleVal(l) && haveDoubleVal(r))
            return new Double( doubleVal(l) * doubleVal(r) );
        else return new Double(Double.NaN);
    }
    public Object divObjects(Object l, Object r){
        if(haveDoubleVal(l) && haveDoubleVal(r))
            return new Double( doubleVal(l) / doubleVal(r) );
        else return new Double(Double.NaN);
    }
    public Object powObjects(Object l, Object r){
        if(haveDoubleVal(l) && haveDoubleVal(r))
            return new Double( Math.pow(doubleVal(l), doubleVal(r)) );
        else return new Double(Double.NaN);
    }

    public Boolean lessObjects(Object l, Object r){
        if(haveDoubleVal(l) && haveDoubleVal(r))
            return new Boolean(doubleVal(l)<doubleVal(r));
        else return new Boolean(false);
    }
    public Boolean greaterObjects(Object l, Object r){
        if(haveDoubleVal(l) && haveDoubleVal(r))
            return new Boolean(doubleVal(l)>doubleVal(r));
        else return new Boolean(false);
    }
    public Boolean equalObjects(Object l, Object r){
        if(haveDoubleVal(l) && haveDoubleVal(r))
            return new Boolean(doubleVal(l)==doubleVal(r));
        else return new Boolean(false);
    }
}
