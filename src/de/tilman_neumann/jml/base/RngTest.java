/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2022 Tilman Neumann (www.tilman-neumann.de)
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
package de.tilman_neumann.jml.base;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import org.apache.log4j.Logger;

import de.tilman_neumann.test.junit.ClassTest;

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static de.tilman_neumann.jml.base.BigDecimalConstants.*;

public class RngTest extends ClassTest {

	private static final Logger LOG = Logger.getLogger(RngTest.class);
	
	private static final Rng RNG = new Rng();
	
	public void testUniformBigInteger() {
		int numberTests = 100;
		BigInteger maxValue = I_1E3;
		BigInteger tooLow = BigInteger.valueOf(400);
		BigInteger tooHigh = BigInteger.valueOf(600);
		
		BigInteger sum = I_0;
		for (int i=0; i<numberTests; i++) {
			BigInteger value = RNG.nextBigInteger(maxValue);
			sum = sum.add(value);
		}
		BigDecimal mean = new BigDecimal(sum).divide(BigDecimal.valueOf(numberTests), 5, RoundingMode.HALF_EVEN);
		LOG.info("mean of numbers generated from [0.." + maxValue + ") = " + mean);
		if (BigDecimalMath.compare(mean, tooLow)<0 || BigDecimalMath.compare(mean, tooHigh)>0) {
			fail();
		}
	}

	public void testUniformBigDecimal() {
		int numberTests = 100;
		BigDecimal minValue = BigDecimal.valueOf(100000);
		BigDecimal maxValue = BigDecimal.valueOf(200000);
		int minScale = 20;
		int maxScale = 50;
		BigDecimal sum = F_0;
		int lowDigitsSum = 0;
		for (int i=0; i<numberTests; i++) {
			BigDecimal value = RNG.nextBigDecimal(minValue, maxValue, minScale, maxScale);
			sum = sum.add(value);
			int lowDigits = Math.max(0, value.scale());
			lowDigitsSum += lowDigits;
		}
		BigDecimal mean = sum.divide(BigDecimal.valueOf(numberTests), 25, RoundingMode.HALF_EVEN);
		LOG.info("mean of numbers generated from [" + minValue + ", " + maxValue + ") = " + mean);
		int lowDigitsMean = (lowDigitsSum+numberTests/2)/numberTests;
		LOG.info("mean of after-comma digits = " + lowDigitsMean);
		if (lowDigitsMean<=30 || lowDigitsMean>=40) {
			 // mean after-comma digits should be around 35
			fail();
		}
	}
}
