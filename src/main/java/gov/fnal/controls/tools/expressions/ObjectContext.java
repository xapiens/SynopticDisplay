/*
 * Copyright 1999, Universities Research Association.  All rights reserved.
 */
package  gov.fnal.controls.tools.expressions;

/**
 * This interface is used as the context in expressions
 * evaluation. It provides access to the user defined variables
 * and user defined functions.
 *
 * @author Timofei Bolshakov
 * @version 1
 * @see gov.fnal.controls.tools.expressions.DefaultContext
 * @see gov.fnal.controls.tools.expressions.Expression
 */
public interface ObjectContext extends Context{

    /**
     * Return a value of variable
     *
     * @param var_name Name of the user defined/system variable
     * @return the <b>double</b> value of this variable
     * @see gov.fnal.controls.tools.expressions.DefaultContext#getValue
     */
    // for variables
    public Object getObjectValue(String var_name);

    /**
     * Set the value of variable
     *
     * @param var_name variable name
     * @param val value to set
     * @see gov.fnal.controls.tools.expressions.DefaultContext#setValue
     */
    public void   setObjectValue(String var_name, Object val);

    /**
     * Compute user defined/system function from one parameter.
     *
     * @param f_name name of the function
     * @param arg argument of this function
     * @return computed value
     * @see gov.fnal.controls.tools.expressions.DefaultContext#computeFunction
     */
    // for functions
    public Object computeObjectFunction (String f_name, Object arg);

    /**
     * Compute user defined/system function from multiple parameters.
     *
     * @param f_name name of the function
     * @param arg array of arguments of this function
     * @return computed value
     * @see gov.fnal.controls.tools.expressions.DefaultContext#computeMFunction
     */
    public Object computeObjectMFunction(String f_name, Object []arg);

    public Object addObjects(Object l, Object r);
    public Object subObjects(Object l, Object r);
    public Object mulObjects(Object l, Object r);
    public Object divObjects(Object l, Object r);
    public Object powObjects(Object l, Object r);

    public Boolean lessObjects(Object l, Object r);
    public Boolean greaterObjects(Object l, Object r);
    public Boolean equalObjects(Object l, Object r);

}
