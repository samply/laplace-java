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

/**
 * Builder for a LaplaceObfuscator
 *
 * @author Tobias Kussel
 */
class LaplaceObfuscatorBuilder {

  private SecureRandom rng;
  private boolean obfuscateZero;
  private boolean useCaching;
  private int roundingStep;

  /**
   * Constructor of the obfuscator builder. Sets the default value of caching,
   * obfuscating all values, including zero, rounding the results to the next
   * integer.
   */
  public LaplaceObfuscatorBuilder() {
    this.rng = null;
    this.obfuscateZero = true;
    this.useCaching = true;
    this.roundingStep = 1;
  }

  /**
   * Provides an external, e.g. seeded, RNG to the obfuscator.
   *
   * @param rng External SecureRandom RNG
   */
  public LaplaceObfuscatorBuilder randomGenerator(SecureRandom rng) {
    this.rng = rng;
    return this;
  }

  /**
   * Configures, if zeros should be obfuscated or returned truthfully. If not
   * explicitly stated, this parameter defaults to true.
   *
   * @param obfuscateZero If false, zeros will be returned unobfuscated.
   */
  public LaplaceObfuscatorBuilder obfuscateZero(boolean obfuscateZero) {
    this.obfuscateZero = obfuscateZero;
    return this;
  }

  /**
   * Configures, if caching of the results should be used. As it improves
   * security, it defaults to true, if not explicitly stated as false.
   *
   * @param useCaching If false, each value will be obfuscated with new
   *                   randomness
   */
  public LaplaceObfuscatorBuilder useCaching(boolean useCaching) {
    this.useCaching = useCaching;
    return this;
  }

  /**
   * Configures the rounding step after obfuscating. Defaults to 1 to round to
   * the nearest integer.
   *
   * @param rundingStep Gives step to round to, e.g. 1, 5, or 10. Defaults to 1.
   */
  public LaplaceObfuscatorBuilder roundingStep(int roundingStep) {
    this.roundingStep = roundingStep;
    return this;
  }

  /**
   * Buildes the LaplaceObfuscator
   */
  public LaplaceObfuscator build() {
    return new LaplaceObfuscator(rng, obfuscateZero, useCaching, roundingStep);
  }
}
