/**
 * 
 */
package net.jadoth.test.lang;

import static java.lang.System.out;

import java.util.Arrays;

import net.jadoth.functional._intProcedure;
import net.jadoth.math._intRange;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestRange_int
{

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		testRange(  0, 10);
		testRange(  5, 10);
		testRange(- 5, 10);
		testRange(- 5,-10);
		testRange(-10,- 5);
		testRange( 10,  5);
		
		for(final int value : range(5, 10)) out.println(value);
		/* pretty cool:
		 * "For each integer value in range from 5 to 10: output print line value"
		 */
		
		//range(5, 10).process(#(value){out.println(value)})
		/* even cooler with JDK 7:
		 * "Range from 5 to 10: process each value by output print line value"
		 */

	}
	
	static int sum = 0;
	static final _intProcedure SUM_PROCESSOR = new _intProcedure(){
		@Override public void accept(final int i){
			sum += i;
		}
	};
	static final _intProcedure getSumProcessor()
	{
		sum = 0;
		return SUM_PROCESSOR;
	}
	
	static void testRange(final int from, final int to)
	{
		final _intRange range = range(from, to);
		System.out.println(range);
		System.out.println(Arrays.toString(range.toArray()));
		System.out.println(Arrays.toString(range.toArray(new Integer[range.size()])));
		System.out.println(Arrays.toString(range.toArray_int()));
		for(final Integer i : range)
		{
			System.out.print(i);
			System.out.print(',');
		}
		System.out.println("");
		range.process(getSumProcessor());
		System.out.println("Sum = "+sum);
		System.out.println("");
	}

	public static _intRange range(final int from, final int to)
	{
		return _intRange.New(from, to);
	}

}
