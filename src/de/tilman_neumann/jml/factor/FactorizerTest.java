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
package de.tilman_neumann.jml.factor;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.base.congruence.*;
import de.tilman_neumann.jml.factor.base.matrixSolver.*;
import de.tilman_neumann.jml.factor.cfrac.*;
import de.tilman_neumann.jml.factor.cfrac.tdiv.*;
import de.tilman_neumann.jml.factor.ecm.*;
import de.tilman_neumann.jml.factor.hart.*;
import de.tilman_neumann.jml.factor.lehman.*;
import de.tilman_neumann.jml.factor.pollardRho.*;
import de.tilman_neumann.jml.factor.psiqs.*;
import de.tilman_neumann.jml.factor.siqs.*;
import de.tilman_neumann.jml.factor.siqs.poly.*;
import de.tilman_neumann.jml.factor.siqs.poly.baseFilter.*;
import de.tilman_neumann.jml.factor.siqs.powers.*;
import de.tilman_neumann.jml.factor.siqs.sieve.*;
import de.tilman_neumann.jml.factor.siqs.tdiv.*;
import de.tilman_neumann.jml.factor.squfof.*;
import de.tilman_neumann.jml.factor.tdiv.*;
import de.tilman_neumann.jml.primes.probable.BPSWTest;
import de.tilman_neumann.jml.sequence.*;
import de.tilman_neumann.util.*;

/**
 * Main class to compare the performance of factor algorithms.
 * @author Tilman Neumann
 */
@SuppressWarnings("unused") // suppress warnings on unused imports
public class FactorizerTest {
	private static final Logger LOG = Logger.getLogger(FactorizerTest.class);

	private static final boolean DEBUG = false;
	
	// algorithm options
	/** number of test numbers */
	private static final int N_COUNT = 1;
	/** the bit size of N to start with */
	private static final int START_BITS = 200;
	/** the increment in bit size from test set to test set */
	private static final int INCR_BITS = 10;
	/** maximum number of bits to test (no maximum if null) */
	private static final Integer MAX_BITS = null;
	/** each algorithm is run REPEATS times for each input in order to reduce GC influence on timings */
	private static final int REPEATS = 1;
	/** Nature of test numbers */
	private static final TestNumberNature TEST_NUMBER_NATURE = TestNumberNature.MODERATE_SEMIPRIMES;
	/** Test mode */
	private static final TestMode TEST_MODE = TestMode.FIRST_FACTOR;

	private BPSWTest bpsw = new BPSWTest();
	
	/** 
	 * Algorithms to compare. Non-static to permit to use Loggers in the algorithm constructors.
	 */
	private FactorAlgorithm[] algorithms;
	
	public FactorizerTest() {
		algorithms = new FactorAlgorithm[] {

			// Trial division
//			new TDiv31(),
//			new TDiv31Inverse(),
//			new TDiv31Barrett(), // Fastest algorithm for N < 29 bit
//			new TDiv63Inverse(1<<21),
//			new TDiv().setTestLimit(1<<20),
			
			// Hart's one line factorizer
			//new Hart_Simple(),
//			new Hart_Fast(false),
//			new Hart_Fast(true),
//			new Hart_TDiv_Race(), 
//			new Hart_TDiv_Race2(),
//			new Hart_Squarefree(false), // best algorithm for semiprime N for 29 to 37 bit
//			new Hart_Fast2Mult(false), // best algorithm for semiprime N for 38 to 45 bit
//			new Hart_Fast2Mult_FMA(false),

			// Lehman
			//new Lehman_Simple(false),
			//new Lehman_Smith(false),
//			new Lehman_Fast(false), // the variant implemented by bsquared
			//new Lehman_Fast(true),
//			new Lehman_CustomKOrder(false),

			// PollardRho
			//new PollardRho(),
			//new PollardRho_ProductGcd(),
			//new PollardRhoBrent(),
			//new PollardRho31(),
			//new PollardRhoBrent31(),
//			new PollardRhoBrentMontgomeryR64Mul63(),
//			new PollardRhoBrentMontgomery64(),
//			new PollardRhoBrentMontgomery64_MH(),
//			new PollardRhoBrentMontgomery64_MHInlined(),
			
			// SquFoF variants
			// * pretty good, but never the best algorithm
			// * SquFoF31 works until 52 bit and is faster there than SquFoF63
			// * best multiplier sequence = 1680 * {squarefree sequence}
			// * best stopping criterion = O(5.th root(N))
//			new SquFoF63(),
			//new SquFoF31(),
			//new SquFoF31Preload(),
			
			// CFrac
			// * never the best algorithm: SquFoF63 is better for N <= 65 bit, SIQS is better for N >= 55 bits
			// * stopRoot, stopMult: if big enough, then a second k is rarely needed; (5, 1.5) is good
			// * TDiv_CF01 is good for N < 80 bits; for N > 90 bit we need TDiv_CF02
			// * ksAdjust: Must be <=3 for N=20bit, <=6 for N=30 bit etc. // TODO this implies some optimization potential
//			new CFrac(true, 5, 1.5F, 0.152F, 0.253F, new TDiv_CF01(), new MatrixSolver_Gauss02(), 5),
//			new CFrac(true, 5, 1.5F, 0.152F, 0.253F, new TDiv_CF02(), new MatrixSolver_Gauss02(), 5),
//			new CFrac(true, 5, 1.5F, 0.152F, 0.253F, new TDiv_CF03(), new MatrixSolver_Gauss02(), 5),
//			new CFrac63(true, 5, 1.5F, 0.152F, 0.25F, new TDiv_CF63_01(), new MatrixSolver_Gauss02(), 3),
//			new CFrac63(true, 5, 1.5F, 0.152F, 0.25F, new TDiv_CF63_02(), new MatrixSolver_Gauss02(), 12),

			// ECM
//			new TinyEcm64(),
//			new TinyEcm64_MH(),
//			new TinyEcm64_MHInlined(), // best algorithm for N from 46 to 62 bit
//			new EllipticCurveMethod(-1),

			// SIQS:
			// small N
//			new SIQS_Small(0.32F, 0.37F, null, new SIQSPolyGenerator(), 10, true),
//			new SIQS(0.32F, 0.37F, null, new NoPowerFinder(), new SIQSPolyGenerator(), new SimpleSieve(), new TDiv_QS_Small(), 10, new MatrixSolver_Gauss02()),
//			new SIQS(0.32F, 0.37F, null, new NoPowerFinder(), new SIQSPolyGenerator(), new Sieve03g(), new TDiv_QS_Small(), 10, new MatrixSolver_Gauss02()),
//			new SIQS(0.32F, 0.37F, null, new NoPowerFinder(), new SIQSPolyGenerator(), new Sieve03gU(), new TDiv_QS_Small(), 10, new MatrixSolver_Gauss02()),
//			new SIQS(0.32F, 0.37F, null, new NoPowerFinder(), new SIQSPolyGenerator(), new Sieve03gU(), new TDiv_QS_Small(), 10, new MatrixSolver_Gauss02()),
			
			// large N
//			new SIQS(0.31F, 0.37F, null, new NoPowerFinder(), new SIQSPolyGenerator(), new Sieve03g(), new TDiv_QS_2LP_Full(true), 10, new MatrixSolver_PGauss01(12)),
//			new SIQS(0.31F, 0.37F, null, new NoPowerFinder(), new SIQSPolyGenerator(), new Sieve03gU(), new TDiv_QS_2LP_Full(true), 10, new MatrixSolver_PGauss01(12)),
//			new SIQS(0.31F, 0.37F, null, new NoPowerFinder(), new SIQSPolyGenerator(), new Sieve03h(), new TDiv_QS_2LP(true), 10, new MatrixSolver_PGauss01(12)),
//			new SIQS(0.31F, 0.37F, null, new NoPowerFinder(), new SIQSPolyGenerator(), new Sieve03hU(), new TDiv_QS_2LP(true), 10, new MatrixSolver_PGauss01(4)),

			// sieving with prime powers: best sieve for small N!
//			new SIQS(0.31F, 0.37F, null, new PowerOfSmallPrimesFinder(), new SIQSPolyGenerator(), new Sieve03hU(), new TDiv_QS_2LP(true), 10, new MatrixSolver_Gauss03()),
//			new SIQS(0.31F, 0.37F, null, new AllPowerFinder(), new SIQSPolyGenerator(), new Sieve03hU(), new TDiv_QS_2LP(true), 10, new MatrixSolver_Gauss03()),

			// Multi-threaded SIQS:
			// On a Ryzen 3900X, Cmult=0.31 seems to be best for N <= 345 bit, Cmult=0.305 best for N > 345 bit.
			// Probably, this depends heavily on the number of threads and the hardware, in particular the size of the L3-Cache.
//			new PSIQS(0.31F, 0.37F, null, 20, new NoPowerFinder(), new MatrixSolver_BlockLanczos()),
			new PSIQS_U(0.31F, 0.37F, null, 20, new NoPowerFinder(), new MatrixSolver_BlockLanczos()),
//			new PSIQS_U(0.31F, 0.37F, null, 20, new NoPowerFinder(), new MatrixSolver_PGauss01(12)),
//			new PSIQS_U(0.31F, 0.37F, null, 20, new PowerOfSmallPrimesFinder(), new MatrixSolver_BlockLanczos()),
//			new PSIQS_U(0.31F, 0.37F, null, 20, new AllPowerFinder(), new MatrixSolver_BlockLanczos()),

			// experimental PSIQS variants
//			new PSIQS_U_nLP(0.31F, 0.37F, null, 20, new NoPowerFinder(), new MatrixSolver_BlockLanczos()),
//			new PSIQS_U_3LP(0.31F, 0.37F, null, 20, new NoPowerFinder(), new MatrixSolver_BlockLanczos()),
//			new PSIQS_SB_U(0.31F, 0.37F, null, 20, new NoPowerFinder(), new MatrixSolver_BlockLanczos()),
//			new PSIQS_SB(0.31F, 0.37F, null, 20, new NoPowerFinder(), new MatrixSolver_BlockLanczos()),

			// Best combination of sub-algorithms for general factor arguments of any size
//			new CombinedFactorAlgorithm(16, 1<<16, true),
		};
	}
	
	@SuppressWarnings("unchecked")
	private void testRange(int bits) {
		BigInteger N_min = I_1.shiftLeft(bits-1);
		// Compute test set
		BigInteger[] testNumbers = TestsetGenerator.generate(N_COUNT, bits, TEST_NUMBER_NATURE);

		// TEST_MODE=FIRST_FACTOR needs factors, TEST_MODE=PRIME_FACTORIZATION needs factorSetArray
		BigInteger[] factors = null;
		SortedMultiset<BigInteger>[] factorSetArray = null;
		SortedMultiset<BigInteger>[] correctFactorSets = null;
		if (TEST_MODE==TestMode.FIRST_FACTOR) {
			factors = new BigInteger[N_COUNT];
		} else {
			// TEST_MODE==TestMode.PRIME_FACTORIZATION
			correctFactorSets = new SortedMultiset_BottomUp[N_COUNT];
			factorSetArray = new SortedMultiset_BottomUp[N_COUNT];
		}

		if (N_COUNT > 1) {
			LOG.info("Test N with " + bits + " bit, e.g. N = " + testNumbers[0]);
		} else {
			LOG.info("Test N = " + testNumbers[0] + " (" + bits + " bit)");
		}
		
		// take REPEATS timings for each algorithm to be quite sure that one timing is not falsified by garbage collection
		TreeMap<Long, List<FactorAlgorithm>> ms_2_algorithms = new TreeMap<Long, List<FactorAlgorithm>>();
		for (int i=0; i<REPEATS; i++) {
			for (FactorAlgorithm algorithm : algorithms) {
				// exclude special size implementations
				String algName = algorithm.getName();
				if (bits<54 && algName.startsWith("SIQS")) continue; // unstable for smaller N
				if (bits<57 && algName.startsWith("PSIQS")) continue; // unstable for smaller N
				if (bits>98 && algName.startsWith("CFrac63")) continue; // unstable for N>98 bits
				if (bits>52 && algName.startsWith("SquFoF31")) continue; // int implementation
				if (bits>59 && algName.startsWith("Lehman")) continue; // TODO make it work again for 60 bit?
				if (bits>31 && algName.startsWith("TDiv31")) continue; // int implementation
				if (bits>31 && algName.startsWith("PollardRho31")) continue; // long implementation
				if (bits>42 && algName.startsWith("TDiv63Inverse")) continue; // not enough primes stored
				if (bits>57 && algName.equals("PollardRhoBrentMontgomeryR64Mul63")) continue; // very slow above
				
				System.gc(); // create equal conditions for all algorithms

				int failCount = 0;
				BigInteger failExample = null;
				long duration;
				switch (TEST_MODE) {
				case FIRST_FACTOR: {
					// test performance
					long startTimeMillis = System.currentTimeMillis();
					for (int j=0; j<N_COUNT; j++) {
						try {
							factors[j] = algorithm.findSingleFactor(testNumbers[j]); // TODO use searchFactors() here, too ?
						} catch (ArithmeticException e) {
							LOG.error("FactorAlgorithm " + algorithm.getName() + " threw Exception while searching for a factor of N=" + testNumbers[j] + ": " + e);
						}
					}
					long endTimeMillis = System.currentTimeMillis();
					duration = endTimeMillis - startTimeMillis; // duration in ms
					//LOG.debug("algorithm " + algName + " finished test set with " + bits + " bits");
					
					// verify
					for (int j=0; j<N_COUNT; j++) {
						BigInteger N = testNumbers[j];
						BigInteger factor = factors[j];
						// test correctness
						if (factor==null || factor.equals(I_0) || factor.equals(I_1) || factor.mod(N).equals(I_0)) {
							//LOG.error("FactorAlgorithm " + algorithm.getName() + " did not find a factor of N=" + N + ", it returned " + factor);
							failExample = N;
							failCount++;
						} else {
							// not null, not trivial -> test division
							BigInteger[] test = N.divideAndRemainder(factor);
							if (!test[1].equals(I_0)) {
								//LOG.error("FactorAlgorithm " + algorithm.getName() + " returned " + factor + ", but this is not a factor of N=" + N);
								failExample = N;
								failCount++;
							}
						}
					}
					break;
				}
				case PRIME_FACTORIZATION: {
					// test performance
					long startTimeMillis = System.currentTimeMillis();
					for (int j=0; j<N_COUNT; j++) {
						try {
							factorSetArray[j] = algorithm.factor(testNumbers[j]);
						} catch (ArithmeticException e) {
							LOG.error("Algorithm " + algorithm.getName() + " threw Exception while factoring N=" + testNumbers[j] + ": " + e);
							factorSetArray[j] = null; // to have correct fail count
						}
					}
					long endTimeMillis = System.currentTimeMillis();
					duration = endTimeMillis - startTimeMillis; // duration in ms
					//LOG.debug("algorithm " + algName + " finished test set with " + bits + " bits");
					
					// test correctness
					for (int j=0; j<N_COUNT; j++) {
						BigInteger N = testNumbers[j];
						SortedMultiset<BigInteger> factorSet = factorSetArray[j];
						SortedMultiset<BigInteger> correctFactors = correctFactorSets[j];
						if (correctFactors == null) {
							correctFactors = correctFactorSets[j] = testAndGetCorrectFactors(N, factorSet);
						}
						
						if (!correctFactors.equals(factorSet)) {
							if (DEBUG) LOG.error("FactorAlgorithm " + algorithm.getName() + " did not find all factors of N=" + N + ". Correct factors=" + correctFactors + ", found factors=" + factorSet);
							failExample = N;
							failCount++;
						}
					}
					break;
				}
				default: throw new IllegalArgumentException("TestMode = " + TEST_MODE);
				}
				
				List<FactorAlgorithm> algList = ms_2_algorithms.get(duration);
				if (algList==null) algList = new ArrayList<FactorAlgorithm>();
				algList.add(algorithm);
				ms_2_algorithms.put(duration, algList);
				if (failCount>0) {
					LOG.error("FactorAlgorithm " + algorithm.getName() + " failed at " + failCount + "/" + N_COUNT + " test numbers, e.g. for N = " + failExample);
				}
			}
		}
		
		// log best algorithms first
		int rank=1;
		for (long ms : ms_2_algorithms.keySet()) {
			List<FactorAlgorithm> algList = ms_2_algorithms.get(ms);
			int j=0;
			for (FactorAlgorithm algorithm : algList) {
				String durationStr = TimeUtil.timeStr(ms);
				LOG.info("#" + rank + ": Algorithm " + algorithm.getName() + " took " + durationStr);
				j++;
			}
			rank += j;
		}
	}
	
	private SortedMultiset<BigInteger> testAndGetCorrectFactors(BigInteger N, SortedMultiset<BigInteger> factorSet) {
		if (factorSet != null) {
			// analyzing the found factors will usually be faster than doing another factorization with a safe algorithm
			BigInteger product = I_1;
			for (BigInteger factor : factorSet.keySet()) {
				if (!bpsw.isProbablePrime(factor)) {
					// not finding the prime factorization is an error -> make sure that N != product.
					break;
				}
				int exp = factorSet.get(factor);
				BigInteger pow = factor.pow(exp);
				product = product.multiply(pow);
			}
			if (N.equals(product)) {
				return factorSet;
			}
		}
		
		// Something went wrong above, so now we factor N with a safe algorithm. Strategies with a better performance certainly exist.
		return FactorAlgorithm.getDefault().factor(N);
		// XXX Do we want the verification factor algorithm to log details if ANALYZE==true?
		// XXX It is not really nice to see SIQS been run when another algorithm was tested :-/
	}
	
	/**
	 * Test factor algorithms for sets of factor arguments of growing size and report timings after each set.
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	FactorizerTest testEngine = new FactorizerTest();
		int bits = START_BITS;
		while (true) {
			// test N with the given number of bits, i.e. 2^(bits-1) <= N <= (2^bits)-1
			testEngine.testRange(bits);
			bits += INCR_BITS;
			if (MAX_BITS!=null && bits > MAX_BITS) break;
		}
	}
}
