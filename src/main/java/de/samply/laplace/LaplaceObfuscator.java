/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.samply.laplace;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.tuple.Triple;

/**
 * This class implements a result-caching obfuscator using the laplace mechanism
 * for statistical disclosure control
 *
 * @author Tobias Kussel
 */
class LaplaceObfuscator {
  private final Map<Triple<Double, Long, Long>, Long> resultCache;
  private final SecureRandom rng;
  private final boolean obfuscateZero;
  private final boolean useCaching;
  private final int roundingStep;

  private static final String SEPERATOR = System.getProperty("line.separator");

  /**
   * Constructor of the obfuscator, takes all configuration parameters. Please
   * use the LaplaceObfuscatorBuilder for construction.
   *
   * @param rng           SecureRandom PRNG
   * @param obfuscateZero If false, zero values will not be permuted but
   *                      returned truthfully. If true, all values are
   *                      obfuscated.
   * @param useCaching    If false, no cache will be used
   * @param roundingStep  Step for the result rounding, e.g., 1, 5, or 10
   *
   */
  public LaplaceObfuscator(SecureRandom rng, boolean obfuscateZero,
                           boolean useCaching, int roundingStep) {
    this.resultCache = new ConcurrentHashMap<>();
    this.rng = Optional.ofNullable(rng).orElseGet(SecureRandom::new);
    this.obfuscateZero = obfuscateZero;
    this.useCaching = useCaching;
    this.roundingStep = roundingStep;
  }

  /**
   * Permute a value with the (epsilon, 0) laplacian mechanism.
   * The default cache bin of 1 is used.
   *
   * @param value       clear value to permute
   * @param sensitivity sensitivity of query
   * @param epsilon     privacy budget parameter
   * @return the permuted value
   */
  public synchronized long privatize(long value, double sensitivity,
                                     double epsilon) {
    return privatize(value, sensitivity, epsilon, 1);
  }

  /**
   * Permute a value with the (epsilon, 0) laplacian mechanism.
   * To force new random values, e.g. for different stratifiers, different cache
   * bins can be used.
   *
   * @param value       clear value to permute
   * @param sensitivity sensitivity of query
   * @param epsilon     privacy budget parameter
   * @param cacheBin    cache bin to use, e.g., to enforce the usage of new
   *                    randomness
   * @return the permuted value
   */
  public synchronized long privatize(long value, double sensitivity,
                                     double epsilon, long cacheBin) {
    if (useCaching) {
      return resultCache.computeIfAbsent(
          Triple.of(sensitivity, value, cacheBin),
          k
          -> LaplaceMechanism.privatize(value, sensitivity, epsilon,
                                        obfuscateZero, roundingStep, rng));
    } else {
      return LaplaceMechanism.privatize(value, sensitivity, epsilon,
                                        obfuscateZero, roundingStep, rng);
    }
  }

  /**
   * Returns if a parameter triple is included in the cache
   *
   * @param value       clear value to permute
   * @param sensitivity sensitivity of query
   * @param cacheBin    cache bin to use, e.g., to enforce the usage of new
   *                    randomness
   * @return true, if included in cache; false otherwise
   *
   * @throws IllegalStateException if this method is invoked on a non-caching
   *                               obfuscator
   */
  public synchronized boolean isCached(long value, double sensitivity,
                                       long cacheBin)
      throws IllegalStateException {
    if (useCaching) {
      Triple<Double, Long, Long> cacheKey =
          Triple.of(sensitivity, value, cacheBin);
      return resultCache.containsKey(cacheKey);
    } else {
      throw new IllegalStateException(
          "Obfuscator is not caching, hence, inspecting the cache is not allowed");
    }
  }

  /**
   * Fully clears the result cache
   *
   * @throws IllegalStateException if this method is invoked on a non-caching
   *                               obfuscator
   */
  public synchronized void clearCache() throws IllegalStateException {
    if (useCaching) {
      resultCache.clear();
    } else {
      throw new IllegalStateException(
          "Obfuscator is not caching, hence, clearing the cache is not allowed");
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(this.getClass().getName())
        .append(" Object {")
        .append(SEPERATOR);
    result.append(" Externally seeded RNG: ").append(rng).append(SEPERATOR);
    result.append(" Obfuscate Zero: ").append(obfuscateZero).append(SEPERATOR);
    result.append(" Use Caching: ").append(useCaching).append(SEPERATOR);
    result.append(" Round results step: ")
        .append(roundingStep)
        .append(SEPERATOR);
    result.append(" Cache: ").append(resultCache).append(SEPERATOR);
    result.append("}");

    return result.toString();
  }

  @Override
  public int hashCode() {
    int result = resultCache.hashCode();
    result = 31 * result + rng.hashCode();
    result = 37 * result + (obfuscateZero ? 1 : 2);
    result = 41 * result + (useCaching ? 1 : 2);
    result = 43 * result + roundingStep;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof LaplaceObfuscator)) {
      return false;
    }
    return ((LaplaceObfuscator)o).resultCache.equals(resultCache) &&
        ((LaplaceObfuscator)o).rng.equals(rng) &&
        (((LaplaceObfuscator)o).obfuscateZero == obfuscateZero) &&
        (((LaplaceObfuscator)o).useCaching == useCaching) &&
        (((LaplaceObfuscator)o).roundingStep == roundingStep);
  }
}
