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
package de.tilman_neumann.math.factor.base.congruence;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import de.tilman_neumann.math.factor.base.SortedIntegerArray;

/**
 * A smooth congruence from a single AQ-pair.
 * @author Tilman Neumann
 */
abstract public class Smooth_Simple extends AQPair implements Smooth {

	private Integer[] oddExpElements;

	public Smooth_Simple(BigInteger A, SortedIntegerArray smallFactors) {
		super(A, smallFactors);
		// determine small factors with odd exponents: first we need a set to eliminate duplicates.
		Set<Integer> result = new HashSet<Integer>();
		for (int i=0; i<this.smallFactors.length; i++) {
			if ((smallFactorExponents[i]&1)==1) result.add(this.smallFactors[i]);
		}
		// convert to array
		this.oddExpElements = result.toArray(new Integer[result.size()]);
	}

	@Override
	public void addMyAQPairsViaXor(Set<AQPair> targetSet) {
		if (!targetSet.remove(this)) targetSet.add(this);
	}

	@Override
	public Integer[] getMatrixElements() {
		return oddExpElements;
	}

	@Override
	public boolean isExactSquare() {
		return oddExpElements.length==0;
	}
}
