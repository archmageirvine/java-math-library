/*
 * PSIQS 4.0 is a Java library for integer factorization, including a parallel self-initializing quadratic sieve (SIQS).
 * Copyright (C) 2018  Tilman Neumann (www.tilman-neumann.de)
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
package de.tilman_neumann.math.base.bigint;

import java.math.BigInteger;

import static de.tilman_neumann.math.base.bigint.BigIntConstants.*;

/**
 * Modular power.
 * @author Tilman Neumann
 */
public class ModularPower {
	/**
	 * Computes a^b (mod c) for all-BigInteger arguments.</br></br>
	 * 
	 * <em>BigIntegers implementation is much faster!</em>.
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return a^b (mod c)
	 */
  	/* not public */ BigInteger modPow(BigInteger a, BigInteger b, BigInteger c) {
  		BigInteger modPow = ONE;
  		while (b.compareTo(ZERO) > 0) {
  			if ((b.intValue() & 1) == 1) { // oddness test needs only the lowest bit
  				modPow = modPow.multiply(a).mod(c);
  			}
  			a = a.multiply(a).mod(c);
  			b = b.shiftRight(1);
  		}
  		return modPow;
  	}
  	
	/**
	 * Computes a^b (mod c) for <code>a</code> BigInteger, <code>b, c</code> int. Very fast.
	 * @param a
	 * @param b
	 * @param c
	 * @return a^b (mod c)
	 */
  	public int modPow(BigInteger a, int b, int c) {
  		// products need long precision
  		long modPow = 1;
  		long aModC = a.mod(BigInteger.valueOf(c)).longValue();
  		while (b > 0) {
  			if ((b&1) == 1) modPow = (modPow * aModC) % c;
  			aModC = (aModC * aModC) % c;
  			b >>= 1;
  		}
  		return (int) modPow;
  	}
  	
	/**
	 * Computes a^b (mod c) for all-int arguments. Very fast.
	 * @param a
	 * @param b
	 * @param c
	 * @return a^b (mod c)
	 */
  	public int modPow(int a, int b, int c) {
  		// products need long precision
  		long modPow = 1;
  		long aModC = a % c;
  		while (b > 0) {
  			if ((b&1) == 1) modPow = (modPow * aModC) % c;
  			aModC = (aModC * aModC) % c;
  			b >>= 1;
  		}
  		return (int) modPow;
  	}
}
