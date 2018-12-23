/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann (www.tilman-neumann.de)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */
package de.tilman_neumann.jml.factor.lehman;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorAlgorithmBase;
import de.tilman_neumann.jml.gcd.Gcd63;

/**
 * Faster implementation of Lehmans factor algorithm following https://programmingpraxis.com/2017/08/22/lehmans-factoring-algorithm/.
 * Many improvements inspired by Thilo Harich (https://github.com/ThiloHarich/factoring.git).
 * Works for N <= 45 bit.
 * 
 * This version does trial division after the main loop.
 * 
 * @author Tilman Neumann
 */
public class Lehman_TDivLast extends FactorAlgorithmBase {
	private static final Logger LOG = Logger.getLogger(Lehman_TDivLast.class);

	/** This is a constant that is below 1 for rounding up double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;
	
    private static final boolean[] isSquareMod1024 = isSquareMod1024();

    private static boolean[] isSquareMod1024() {
    	boolean[] isSquareMod_1024 = new boolean[1024];
        for (int i = 0; i < 1024; i++) {
        	isSquareMod_1024[(i * i) & 1023] = true;
        }
        return isSquareMod_1024;
    }

    /** A multiplicative constant to adjust the trial division limit. */
	private float tDivLimitMultiplier;
	
	private final Gcd63 gcdEngine = new Gcd63();

	private double[] sqrt, sqrtInv;

    public Lehman_TDivLast(float tDivLimitMultiplier) {
    	this.tDivLimitMultiplier = tDivLimitMultiplier;
    	SMALL_PRIMES.ensurePrimeCount(10000); // for tDivLimitMultiplier ~ 2 we need more than 4793 primes
    	initSqrts();
    }
    
	private void initSqrts() {
		// precompute sqrts for all possible k. Requires ~ (tDivLimitMultiplier*2^15) entries.
		int kMax = (int) (tDivLimitMultiplier*Math.cbrt(1L<<45) + 1);
		//LOG.debug("kMax = " + kMax);
		
		sqrt = new double[kMax + 1];
		sqrtInv = new double[kMax + 1];
		for (int i = 1; i < sqrt.length; i++) {
			final double sqrtI = Math.sqrt(i);
			sqrt[i] = sqrtI;
			sqrtInv[i] = 1.0/sqrtI;
		}
		LOG.info("Lehman: Built sqrt tables for multiplier " + tDivLimitMultiplier + " with " + sqrt.length + " entries");
	}

	@Override
	public String getName() {
		return "Lehman_TDivLast(" + tDivLimitMultiplier + ")";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}
	
	public long findSingleFactor(long N) {
		double cbrt = Math.ceil(Math.cbrt(N));
		
		// 1. Main loop for small k, where we can have more than 1 a-value
		int kLimit = (int) cbrt;
		int kMedium = (kLimit >> 5) | 1;
		//LOG.debug("kLimit = " + kLimit);
		long fourN = N<<2;
		double sqrt4N = Math.sqrt(fourN);
		double sixthRootTerm = 0.25 * Math.pow(N, 1/6.0); // double precision is required for stability
		int k=1;
		for (; k <= kMedium; k++) {
			long kn = k*N;
			double sqrt4kN = sqrt4N * sqrt[k];
			long aStart = (long) (sqrt4kN + ROUND_UP_DOUBLE); // much faster than ceil() !
			long aLimit = (long) (sqrt4kN + sixthRootTerm * sqrtInv[k]);
			int aStep;
			if ((k&1)==0) {
				// k even -> make sure aLimit is odd
				aLimit |= 1;
				aStep = 2;
			} else {
				// minor improvement following https://de.wikipedia.org/wiki/Faktorisierungsmethode_von_Lehman
				// this extra case gives ~ 5 %
				if ((kn & 3) == 3) {
					aStep = 8;
					aLimit += (int) ((7 - kn - aLimit) & 7);
				} else {
					aStep = 4;
					aLimit += (int) ((k + N - aLimit) & 3);
				}
			}
			
			// processing the a-loop top-down is faster than bottom-up
			long fourKN = kn << 2;
			for (long a=aLimit; a >= aStart; a-=aStep) {
				long test = a*a - fourKN;
		        if (isSquareMod1024[(int) (test & 1023)]) {
		        	long b = (long) Math.sqrt(test);
		        	if (b*b == test) {
		        		return gcdEngine.gcd(a+b, N);
		        	}
				}
			}
	    }
		
		// 2. continue main loop for larger even k, where we can have only 1 a-value per k
		int k1 = k;
		for ( ; k1 <= kLimit; k1+=2) {
			// for even k we need odd a
			long a = (long) (sqrt4N * sqrt[k1] + ROUND_UP_DOUBLE) | 1;
			long test = a*a - k1*fourN;
	        if (isSquareMod1024[(int) (test & 1023)]) {
	        	long b = (long) Math.sqrt(test);
	        	if (b*b == test) {
	        		return gcdEngine.gcd(a+b, N);
	        	}
			}
	    }
		
		// 2. continue main loop for larger odd k, where we can have only 1 a-value per k
		int k2 = k+1;
		for ( ; k2 <= kLimit; k2+=2) {
			long a = (long) (sqrt4N * sqrt[k2] + ROUND_UP_DOUBLE);
			long test = a*a - k2*fourN;
	        if (isSquareMod1024[(int) (test & 1023)]) {
	        	long b = (long) Math.sqrt(test);
	        	if (b*b == test) {
	        		return gcdEngine.gcd(a+b, N);
	        	}
			}
	    }

		// 3. Check via trial division whether N has a nontrivial divisor d <= cbrt(N), and if so, return d.
		int tDivLimit = (int) (tDivLimitMultiplier*cbrt);
		int i=0, p;
		while ((p = SMALL_PRIMES.getPrime(i++)) <= tDivLimit) {
			if (N%p==0) return p;
		}
		
		// Nothing found. Either N is prime or the algorithm didn't work because N > 45 bit.
		return 0;
	}
}