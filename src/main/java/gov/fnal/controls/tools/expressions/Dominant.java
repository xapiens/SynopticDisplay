package gov.fnal.controls.tools.expressions;

import java.util.*;

/** Written in 2008 by Timofei Bolshakov by request of Kevin Cahill and Tony Levelling
 *  Copyright Fermi National Accelerators Laboratory
 *  @author Timofei Bolshakov
 */
public class Dominant{
    private double delta, ratio;
    private int historySize = 60; // minute of 1 Hz readings
    private String name;
    private SortedMap<Long,   Double>  values;
    private SortedSet<double[]> smartCounts;
    public Dominant(String name, double delta){                  this( name, delta, 60, 0.5 ); }
    public Dominant(String name, double delta, int historySize){ this( name, delta, historySize, 0.5 ); } 
    public Dominant(String name, double delta, int historySize, double ratio){//{{{
        this.name   = name; this.historySize = historySize; this.ratio = ratio; this.delta = delta;
        values      = new TreeMap<Long, Double>( Collections.reverseOrder() );
        smartCounts = new TreeSet<double[]>(     getDoubleArrayComparator() );
    }//}}}
    public double newValue( long ts, double val ){//{{{
        values.put( ts, val ); inc( val );
        while( values.size() > historySize ) { double v = values.remove( values.lastKey() ); dec( v ); }
        if( isInDominantBand( val ) ) return val; 
        else // instead of NaN return last good
            for( double v : values.values() ) if( isInDominantBand( v ) ) return v; // garantee
        return Double.NaN; // there are no dominant area with such a size.
    }//}}}
    public double newValue( double val ){ return newValue( System.currentTimeMillis(), val ); }
    public void inc(double value){//{{{
        double [] minKey = new double[]{ value, 0 }, maxKey = new double[]{ value, Double.MAX_VALUE };
        SortedSet<double[]> counts = smartCounts.subSet( minKey, maxKey );
        if( counts.size() == 0 ) smartCounts.add( minKey );
        else                     smartCounts.add( new double[]{ value, counts.last()[1]+1 } );
    }//}}}
    public void dec(double value){//{{{
        double [] minKey = new double[]{ value, 0 }, maxKey = new double[]{ value, Double.MAX_VALUE };
        SortedSet<double[]> counts = smartCounts.subSet( minKey, maxKey );
        if( counts.size() != 0 ) smartCounts.remove( counts.last() );
    }//}}}
    public boolean isInDominantBand( double value ){//{{{
        if( smartCounts.size() == 0 ) return false;
        double [] minKey = new double[]{ value-delta, 0 }, maxKey = new double[]{ value+delta, Double.MAX_VALUE };
        SortedSet<double[]> counts = smartCounts.subSet( minKey, maxKey );
        //System.out.println("Min: "+ Arrays.toString( minKey ) );
        //System.out.println("Max: "+ Arrays.toString( maxKey ) );
        //for( double[]entry : counts ) System.out.println( Arrays.toString( entry ) );
        //System.out.printf( "counts %d whole %d ratio %f\n", counts.size(), smartCounts.size(), ratio ); 
        return ( ( ( (double) counts.size()) / ( (double) smartCounts.size() ) ) > ratio ); 
    }//}}}

    public static class DoubleArrayComparator implements Comparator<double[]>{//{{{
        private DoubleArrayComparator(){} // there is only one !
        public int compare(double []a1, double []a2){
            if( a1 == null && a2 == null ) return 0;
            if( a1 == null && a2 != null ) return 1;
            if( a1 != null && a2 == null ) return -1;
            for(int idx = 0; idx < a1.length && idx < a2.length; idx ++ )
                if( a1[idx] < a2[idx] )      return -1;
                else if( a1[idx] > a2[idx] ) return 1;
            if( a1.length < a2.length ) return -1;
            if( a1.length > a2.length ) return 1;
            return 0;
        }
        public boolean equals( Object o ){ return o==this; }
    }//}}}
    private static DoubleArrayComparator doubleArrayComparator = new DoubleArrayComparator();
    public static DoubleArrayComparator getDoubleArrayComparator(){ return doubleArrayComparator; }
    
    public static Map<String, Dominant> allDominants = new HashMap<String, Dominant>(  );
    public static double getGood( String name, double delta, int historySize, double ratio, long ts, double value ){//{{{
        if( name == null ) return Double.NaN;
        name = name.toUpperCase().intern();
        if( !allDominants.containsKey( name ) ) allDominants.put( name, new Dominant( name, delta, historySize, ratio ) );
        return allDominants.get( name ).newValue( ts, value ); 
    }//}}}

    public static double getGood( String name, double delta, int historySize, double ratio, double value )
        { return                getGood( name, delta, historySize, ratio, System.currentTimeMillis(), value ); }
    public static double getGood( String name, double delta, int historySize, long ts, double value )
        { return                getGood( name, delta, historySize, 0.5, ts, value ); }
    public static double getGood( String name, double delta, int historySize, double value )
        { return                getGood( name, delta, historySize, 0.5, System.currentTimeMillis(), value ); }
    public static double getGood( String name, double delta, long ts, double value )
        { return                getGood( name, delta, 60, 0.5, ts, value ); }
    public static double getGood( String name, double delta, double ratio, double value )
        { return                getGood( name, delta, 60, ratio, System.currentTimeMillis(), value ); }
    public static double getGood( String name, double delta, double value )
        { return                getGood( name, delta, 60, 0.5, System.currentTimeMillis(), value ); }
        
    public static void main(String []args ){ // testing
        System.out.println( doubleArrayComparator.compare( new double[]{0}, new double[]{1} ) );
        double v;
        for( int i = 0; i < 120; i++ ){
            double ret = getGood( "test", 0.3, v = Math.exp( Math.pow( Math.random()-1, 2 ) ) );
            System.out.printf( "%f ==> %f\n", v, ret );
        }
    }
}
