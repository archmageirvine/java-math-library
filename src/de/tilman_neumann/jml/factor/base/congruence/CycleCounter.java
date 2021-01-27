package de.tilman_neumann.jml.factor.base.congruence;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;

/**
 * Counts the number of independent cycles in the partial relations.
 * So far it seems to work for 1- and 2-partials.
 * 
 * @see [LM94] Lenstra, Manasse 1994: "Factoring With Two Large Primes", Mathematics of Computation, volume 63, number 208, page 789.
 * @see [LLDMW02] Leyland, Lenstra, Dodson, Muffett, Wagstaff 2002: "MPQS with three large primes", Lecture Notes in Computer Science, 2369.
 * 
 * @author Tilman Neumann
 */
// TODO implement cycle-counting for 3-partials
// TODO implement cycle-finding following [LM94] for 2-partials
public class CycleCounter {
	private static final Logger LOG = Logger.getLogger(CycleCounter.class);
	private static final boolean DEBUG = false; // used for logs and asserts

	// cycle counting
	private int maxLargeFactors;
	private HashMap<Long, Long> vertexMap; // contains edges: bigger to smaller prime; size is v = #vertices
	private HashSet<Long> roots; // roots of disjoint compounds
	private HashSet<Partial> relations; // all distinct relations
	private int edgeCount;

	/**
	 * Full constructor.
	 * @param maxLargeFactors the maximum number of large primes in partials:  1, 2 or 3
	 */
	public CycleCounter(int maxLargeFactors) {
		this.maxLargeFactors = maxLargeFactors;
		vertexMap = new HashMap<>(); // bigger to smaller prime
		roots = new HashSet<>();
		relations = new HashSet<>();
		edgeCount = 0;
	}
	
	/**
	 * Counts the number of independent cycles in the partial relations following [LM94]. Works for 2 large primes.
	 * @param partial the newest partial relation to add
	 */
	public void countIndependentCycles(Partial partial) {
		boolean added = relations.add(partial);
		if (!added) {
			// The partial is a duplicate of another relation we already have
			LOG.error("Found duplicate relation!" + partial);
			return;
		}
		
		// We compute the following two variable once again,
		// but that doesn't matter 'cause it's no production code
		Long[] oddExpBigFactors = partial.getLargeFactorsWithOddExponent();
		int oddExpBigFactorsCount = oddExpBigFactors.length;

		// pad array with 1's to the length maxLargeFactors
		Long[] largeFactors = new Long[maxLargeFactors];
		int oneCount = maxLargeFactors-oddExpBigFactorsCount;
		for (int i=0; i<oneCount; i++) {
			largeFactors[i] = 1L;
		}
		for (int i=0; i<oddExpBigFactorsCount; i++) {
			largeFactors[i+oneCount] = oddExpBigFactors[i];			
		}
		if (DEBUG) LOG.debug("Add largeFactors = " + Arrays.toString(oddExpBigFactors) + " = " + Arrays.toString(largeFactors));

		// add vertices
		for (int i=0; i<maxLargeFactors; i++) {
			long largeFactor = largeFactors[i];
			if (vertexMap.get(largeFactor) == null) {
				// new vertex creates new compound
				vertexMap.put(largeFactor, largeFactor); // v = v + 1
				roots.add(largeFactor); // c = c + 1
			}
		}
		if (DEBUG) LOG.debug("after adding vertices: vertexMap = " + vertexMap + ", roots = " + roots);
		
		// add edges
		for (int i=0; i<maxLargeFactors; i++) {
			long f1 = largeFactors[i];
			for (int j=i+1; j<maxLargeFactors; j++) {
				long f2 = largeFactors[j];
				if (f1 != f2) {
					// find roots
					long r1 = getRoot(f1);
					long r2 = getRoot(f2);
					if (DEBUG) LOG.debug("i="+i+", j=" + j + ": f1=" + f1 + ", f2=" + f2 + ", r1=" + r1 + ", r2=" + r2);
					// insert edge: the smaller root is made the parent of the larger root, and the larger root is no root anymore
					if (r1 < r2) {
						vertexMap.put(r2, r1);
						roots.remove(r2);
					} else if (r1 > r2) {
						vertexMap.put(r1, r2);
						roots.remove(r1);
					} // else: r1 and r2 are in the same compound -> a cycle has been found
					
					// To speed up the process we could also set the "parents" of all vertexMap nodes passed in root finding to the new root
				}
				edgeCount++;
			}
		}
		
		if (DEBUG) {
			if (maxLargeFactors==2) assertEquals(relations.size(), edgeCount);
			if (maxLargeFactors==3) assertEquals(3*relations.size(), edgeCount);
			// general case is maxLargeFactors*(maxLargeFactors-1)/2 ?
			
			LOG.debug(edgeCount + " edges");
			LOG.debug(roots.size() + " roots = " + roots);
			LOG.debug(vertexMap.size() + " vertices = " + vertexMap);
			LOG.debug(relations.size() + " relations");
		}
	}
	
	/**
	 * Find the root of a prime p in the edges graph.
	 * @param p
	 * @return root of p (may be 1 or p itself)
	 */
	private Long getRoot(long p) {
		long q;
		// Not permitting 1 as a root promised finding more smooths, but unfortunately it seems to be wrong
		//while ((q = vertexMap.get(p)) != p && q!=1) p = q;
		// The following works
		while ((q = vertexMap.get(p)) != p) p = q;
		return p;
	}

	public String getCycleCountResult() {
		int cycleCount; {
			switch (maxLargeFactors) {
			case 1:
			case 2:
				cycleCount = edgeCount + roots.size() - vertexMap.size();
				return "maxLargeFactors=" + maxLargeFactors + ": #independent cycles = " + cycleCount + " (" + edgeCount + " edges + " + roots.size() + " compounds - " + vertexMap.size() + " vertices)";
			case 3:
				// The "thought to be" formula from [Leyland, Lenstra, Dodson, Muffett, Wagstaff: "MPQS with three large primes", Lecture Notes in Computer Science, 2369, p.7]
				cycleCount = edgeCount + roots.size() - vertexMap.size() - 2*relations.size();
				return "maxLargeFactors=" + maxLargeFactors + ": #independent cycles = " + cycleCount + " (" + edgeCount + " edges + " + roots.size() + " compounds - " + vertexMap.size() + " vertices - 2*" + relations.size() + " relations)";
			default:
				throw new IllegalStateException("cycle counting is not implemented yet for maxLargeFactors>3, but maxLargeFactors = " + maxLargeFactors);
			}
		}
	}
	
	/**
	 * Finds independent cycles and uses them to combine partial to smooth relations, following [LLDMW02].
	 * Tested with 2 large primes, and should work for 3 large primes, too.
	 */
	public void findCycles() {
		// Create maps from large primes to partials, vice versa, and chains.
		// These are needed so we can remove elements without changing the relations itself.
		HashMap<Long, ArrayList<Partial>> largeFactors_2_partials = new HashMap<>();
		HashMap<Partial, ArrayList<Long>> partials_2_largeFactors = new HashMap<>();
		HashMap<Partial, ArrayList<Partial>> chains = new HashMap<>();
		for (Partial newPartial : relations) {
			Long[] oddExpBigFactors = newPartial.getLargeFactorsWithOddExponent();
			ArrayList<Long> factorsList = new ArrayList<>(); // copy needed
			for (Long oddExpBigFactor : oddExpBigFactors) {
				factorsList.add(oddExpBigFactor);				
				ArrayList<Partial> partialCongruenceList = largeFactors_2_partials.get(oddExpBigFactor);
				// For large N, most large factors appear only once. Therefore we create an ArrayList with initialCapacity=1 to safe memory.
				// Even less memory would be needed if we had a HashMap<Long, Object>
				// and store AQPairs or AQPair[] in the Object part. But I do not want to break the generics...
				if (partialCongruenceList==null) partialCongruenceList = new ArrayList<Partial>(1);
				partialCongruenceList.add(newPartial);
				largeFactors_2_partials.put(oddExpBigFactor, partialCongruenceList);
			}
			partials_2_largeFactors.put(newPartial, factorsList);
			chains.put(newPartial, new ArrayList<>());
		}
		
		// result
		List<Smooth> smoothsFromPartials = new ArrayList<>();
		
		boolean tablesChanged;
		do {
			tablesChanged = false;
			Iterator<Partial> r0Iter = partials_2_largeFactors.keySet().iterator();
			while (r0Iter.hasNext()) {
				Partial r0 = r0Iter.next();
				ArrayList<Long> r0Factors = partials_2_largeFactors.get(r0);
				if (r0Factors.size() != 1) continue;
				
				Long p = r0Factors.get(0);
				ArrayList<Partial> riList = largeFactors_2_partials.get(p);
				for (Partial ri : riList) {
					if (r0.equals(ri)) continue;
					
					ArrayList<Long> riFactors = partials_2_largeFactors.get(ri);
					if (DEBUG) assertNotNull(riFactors);
					if (riFactors.size() == 1) {
						// found cycle -> create new Smooth consisting of r0, ri and their chains
						if (DEBUG) {
							SortedMultiset<Long> combinedLargeFactors = new SortedMultiset_BottomUp<Long>();
							combinedLargeFactors.addAll(r0.getLargeFactorsWithOddExponent());
							for (Partial partial : chains.get(r0)) combinedLargeFactors.addAll(partial.getLargeFactorsWithOddExponent());
							combinedLargeFactors.addAll(ri.getLargeFactorsWithOddExponent());
							for (Partial partial : chains.get(ri)) combinedLargeFactors.addAll(partial.getLargeFactorsWithOddExponent());
							// test combinedLargeFactors
							for (Long factor : combinedLargeFactors.keySet()) {
								assertTrue((combinedLargeFactors.get(factor) & 1) == 0);
							}
						}
						HashSet<Partial> allPartials = new HashSet<>();
						allPartials.add(r0);
						allPartials.addAll(chains.get(r0));
						allPartials.add(ri);
						allPartials.addAll(chains.get(ri));
						Smooth smooth = new Smooth_Composite(allPartials);
						smoothsFromPartials.add(smooth);
						continue;
					}
					
					// otherwise add r0 and its chain to the chain of ri
					ArrayList<Partial> riChain = chains.get(ri);
					riChain.add(r0);
					riChain.addAll(chains.get(r0));
					// delete p from the prime list of ri
					if (riFactors != null) riFactors.remove(p);
				} // end for ri

				// "the entry keyed by r0 is deleted from pbr", pbr = partials_2_largeFactors;
				// this requires iterator.remove() so we can continue working with the outer collection
				r0Iter.remove(); // except avoiding ConcurrentModificationExceptions the same as partials_2_largeFactors.remove(r0);
				
				// "the entry for r0 keyed by p is deleted from rbp", rbp = largeFactors_2_partials
				// This choice promised finding more smooths, but unfortunately it was wrong, delivered combinations with odd exponents
				// riList.remove(r0);
				// The following works
				ArrayList<Partial> partials = largeFactors_2_partials.get(p);
				for (Partial partial : partials) {
					ArrayList<Long> pList = partials_2_largeFactors.get(partial);
					if (pList != null) pList.remove(p);
				}
				
				tablesChanged = true;
			} // end while r0
		} while (tablesChanged);
		
		LOG.debug("Found " + smoothsFromPartials.size() + " smooths from partials");
	}
}
