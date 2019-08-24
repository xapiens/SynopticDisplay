/*
 * Copyright 1999, Universities Research Association.  All rights reserved.
 */
package  gov.fnal.controls.tools.expressions;

import java.util.*;

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
public class DefaultContext implements Context{
	/** Here I store my variables ... */
	public Map variables = new HashMap();
	/** Create a context with PI and E variables */
	public DefaultContext(){
		variables.put("PI", new Double(Math.PI));
		variables.put("E",  new Double(Math.E));
	}
	/**
	 * Create a context with PI, E and user defined variables
	 * names are names of this variables and values are values ...
	 *
	 * @param names array of names for user defined variables
	 * @param values array of values for the names
	 */
	public DefaultContext(String []names, double values[]){
		variables.put("PI", new Double(Math.PI));
		variables.put("E",  new Double(Math.E));
		for(int i=0; i<names.length; i++)
			if(i<values.length) variables.put(names[i], new Double(values[i]));
			// It is the place to generate IllegalArgumentException
			else                variables.put(names[i], new Double(0));
	}
	/** Get the value of variable name */
	public double getValue(String name){
		Double d = (Double)variables.get(name);
		// It is the place to generate VariableNotFoundException
		return (d==null)?0:d.doubleValue();
	}
	/** Set the value of variable name */
	public void   setValue(String name, double val){
		variables.put(name, new Double(val));
	}

	/** Compute a function from one argument ... */
	public double computeFunction (String name, double arg){
		if(name.equalsIgnoreCase("abs"))                        return Math.abs(arg);
		if(name.equalsIgnoreCase("sin"))                        return Math.sin(arg);
		if(name.equalsIgnoreCase("asin"))                       return Math.asin(arg);
		if(name.equalsIgnoreCase("cos"))                        return Math.cos(arg);
		if(name.equalsIgnoreCase("acos"))                       return Math.acos(arg);
		if(name.equalsIgnoreCase("tan"))                        return Math.tan(arg);
		if(name.equalsIgnoreCase("atan"))                       return Math.atan(arg);
		if(name.equalsIgnoreCase("sqrt"))                       return Math.sqrt(arg);
		if(name.equalsIgnoreCase("exp"))                        return Math.exp(arg);
		if(name.equalsIgnoreCase("log"))                        return Math.log(arg);
		if(name.equalsIgnoreCase("ceil"))                       return Math.ceil(arg);
		if(name.equalsIgnoreCase("floor"))                      return Math.floor(arg);
		if(name.equalsIgnoreCase("rint"))                       return Math.rint(arg);
		if(name.equalsIgnoreCase("toDegrees"))                  return Math.toDegrees(arg);
		if(name.equalsIgnoreCase("toRadians"))                  return Math.toRadians(arg);

        //sinh(z) = 1/2 (exp(z) - exp(-z))
		if(name.equalsIgnoreCase("sinh"))  return 0.5*(Math.exp(arg)-Math.exp(-arg));
        //cosh(z) = 1/2 (exp(z) + exp(-z))
		if(name.equalsIgnoreCase("cosh"))  return 0.5*(Math.exp(arg)+Math.exp(-arg));
        //tanh(z) = sinh(z)/cosh(z)
		if(name.equalsIgnoreCase("cosh"))  return (Math.exp(arg)-Math.exp(-arg))/(Math.exp(arg)+Math.exp(-arg));

		if(name.equalsIgnoreCase("sqr"))                        return arg*arg;

		// It is the place to generate FunctionNotFoundException
		return 0;
	}
	/** Compute a function from many argument ... */
	public double computeMFunction (String name, double []arg){
		// It is the place to generate IllegalArgumentException
		if(arg.length==0) return 0;
		if(name.equalsIgnoreCase("min")){
			double min = arg[0];
			for(int i=1; i<arg.length; i++) if(arg[i]<min) min = arg[i];
			return min;
		}
		if(name.equalsIgnoreCase("max")){
			double max = arg[0];
			for(int i=1; i<arg.length; i++) if(arg[i]>max) max = arg[i];
			return max;
		}
		if(name.equalsIgnoreCase("remainder")){
			if(arg.length<2) return 0;
			return Math.IEEEremainder(arg[0], arg[1]);
		}
		if(name.equalsIgnoreCase("pow")){
			if(arg.length<2) return 0;
			return Math.pow(arg[0], arg[1]);
		}
		if(name.equalsIgnoreCase("atan2")){
			if(arg.length<2) return 0;
			return Math.atan2(arg[0], arg[1]);
		}
        if(name.equalsIgnoreCase("average")){
            double sum = 0;
            for(int i = 0; i<arg.length; i++) sum+=arg[i];
            return sum/arg.length;
        }
        if(name.equalsIgnoreCase("averagef")){
            double sum = 0, counter = 0;
            for(int i = 0; i<arg.length; i++) if(!Double.isNaN(arg[i])) { sum+=arg[i]; counter+=1;}
            return sum/counter;
        }
        if(name.equalsIgnoreCase("countErrorFree")){
            double counter = 0;
            for(int i = 0; i<arg.length; i++) if(!Double.isNaN(arg[i])) counter+=1;
            return counter;
        }
        if(name.equalsIgnoreCase("sum")){
            double sum = 0;
            for(int i = 0; i<arg.length; i++) sum+=arg[i];
            return sum;
        }
        if(name.equalsIgnoreCase("sumf")){
            double sum = 0;
            for(int i = 0; i<arg.length; i++) if(!Double.isNaN(arg[i])) sum+=arg[i];
            return sum;
        }
        if( name.startsWith("dominant") ){
            String domName = name.substring( 8 );
            if( arg.length == 2 ) return Dominant.getGood( name, arg[0], arg[1] );
            if( arg.length == 3 ) return Dominant.getGood( name, arg[0], arg[1], arg[2] );
        }
		// It is the place to generate FunctionNotFoundException
		return 0;
	}
}
