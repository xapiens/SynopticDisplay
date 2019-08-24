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
public interface Context{

    /**
     * Return a value of variable
     * 
     * @param var_name Name of the user defined/system variable
     * @return the <b>double</b> value of this variable
     * @see gov.fnal.controls.tools.expressions.DefaultContext#getValue
     */
    // for variables
    public double getValue(String var_name); 

    /**
     * Set the value of variable
     * 
     * @param var_name variable name
     * @param val value to set
     * @see gov.fnal.controls.tools.expressions.DefaultContext#setValue
     */
    public void   setValue(String var_name, double val);

    /**
     * Compute user defined/system function from one parameter.
     * 
     * @param f_name name of the function
     * @param arg argument of this function
     * @return computed value
     * @see gov.fnal.controls.tools.expressions.DefaultContext#computeFunction
     */
    // for functions
    public double computeFunction (String f_name, double arg);

    /**
     * Compute user defined/system function from multiple parameters.
     * 
     * @param f_name name of the function
     * @param arg array of arguments of this function
     * @return computed value
     * @see gov.fnal.controls.tools.expressions.DefaultContext#computeMFunction
     */
    public double computeMFunction(String f_name, double []arg); 
}
