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
package de.tilman_neumann.math.factor.psiqs;

import java.math.BigInteger;
import java.util.List;

import de.tilman_neumann.math.factor.base.congruence.AQPair;
import de.tilman_neumann.math.factor.siqs.data.BaseArrays;
import de.tilman_neumann.math.factor.siqs.poly.AParamGenerator;
import de.tilman_neumann.math.factor.siqs.poly.PolyReport;
import de.tilman_neumann.math.factor.siqs.poly.PolyGenerator;
import de.tilman_neumann.math.factor.siqs.sieve.Sieve;
import de.tilman_neumann.math.factor.siqs.sieve.SieveParams;
import de.tilman_neumann.math.factor.siqs.sieve.SieveReport;
import de.tilman_neumann.math.factor.siqs.tdiv.TDivReport;
import de.tilman_neumann.math.factor.siqs.tdiv.TDiv_QS;

/**
 * Base class for polynomial generation/sieve/trial division threads for the parallel SIQS implementation (PSIQS).
 * @author Tilman Neumann
 */
abstract public class PSIQSThreadBase extends Thread {

	protected PolyGenerator polyGenerator;
	protected Sieve sieve;
	protected TDiv_QS auxFactorizer;
	private AQPairBuffer aqPairBuffer;
	private boolean finishNow = false;

	/**
	 * Standard constructor.
	 * @param k
	 * @param N
	 * @param kN
	 * @param d the d-parameter of quadratic polynomials Q(x) = (d*a*x + b)^2 - kN
	 * @param sieveParams basic sieve parameters
	 * @param baseArrays primes, power arrays after adding powers
	 * @param apg
	 * @param aqPairBuffer
	 * @param threadIndex
	 */
	public PSIQSThreadBase(
			int k, BigInteger N, BigInteger kN, int d, SieveParams sieveParams, BaseArrays baseArrays, AParamGenerator apg, AQPairBuffer aqPairBuffer,
			PolyGenerator polyGenerator, Sieve sieve, TDiv_QS tdiv, int threadIndex, boolean profile) {
		
		// set thread name
		super("T-" + threadIndex);
		
		// instantiate sub-algorithms
		this.polyGenerator = polyGenerator;
		this.sieve = sieve;
		this.auxFactorizer = tdiv;
		
		// initialize polynomial generator and sub-engines
		// apg is already initialized and the same object for all threads -> a-parameter generation is synchronized on it
		polyGenerator.initializeForN(k, N, kN, d, sieveParams, baseArrays, apg, sieve, auxFactorizer, profile);
		// synchronized buffer to pass AQ-pairs to the main thread -> the same object for all threads
		this.aqPairBuffer = aqPairBuffer;
	}
	
	public void run() {
		while (!finishNow) {
			// create new polynomial Q(x)
			polyGenerator.nextPolynomial();
			
			// run sieve and get the sieve locations x where Q(x) is sufficiently smooth
			List<Integer> smoothXList = sieve.sieve();
			//LOG.debug("Sieve found " + smoothXList.size() + " Q(x) smooth enough to be passed to trial division.");

			// trial division stage: produce AQ-pairs
			List<AQPair> aqPairs = auxFactorizer.testList(smoothXList);
			//LOG.debug("Trial division found " + aqPairs.size() + " Q(x) smooth enough for a congruence.");

			if (aqPairs.size()>0) {
				// add all congruences synchronized and notify control thread
				aqPairBuffer.addAll(aqPairs);
			}
		}
	}

	public void setFinishNow() {
		finishNow = true;
	}

	public void cleanUp() {
		polyGenerator.cleanUp();
		sieve.cleanUp(); // release native memory!
		auxFactorizer.cleanUp();
	}

	public PolyReport getPolyReport() {
		return polyGenerator.getReport();
	}
	
	public SieveReport getSieveReport() {
		return sieve.getReport();
	}
	
	public TDivReport getTDivReport() {
		return auxFactorizer.getReport();
	}
}
