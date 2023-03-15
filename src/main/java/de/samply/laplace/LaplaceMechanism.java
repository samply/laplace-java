package de.samply.laplace;

import java.security.SecureRandom;

/**
 * This class implements the laplacian mechanism.
 *
 * @author Tobias Kussel, Deniz Tas, Alexander Kiel
 */
class LaplaceMechanism {

  /**
   * This class does not need construction
   */
  private LaplaceMechanism(){}

  /**
   * Performs the actual permutation of a value with the (epsilon, 0) laplacian
   * mechanism and rounds the result to the nearest 10th position.
   *
   * @param value         clear value to permute
   * @param sensitivity   sensitivity of query
   * @param epsilon       privacy budget parameter
   * @param obfuscateZero should only values greater than zero be ofuscated, or
   *                      all values including zero
   * @param roundingStep  Rounding to the given number is performed
   * @param rand          SecureRandom generator, e.g. for seeded randomness
   * @return the permuted value
   */
  public static long privatize(long value, double sensitivity, double epsilon,
                        boolean obfuscateZero, long roundingStep,
                        SecureRandom rand) {
    if ((value > 0) || obfuscateZero) {
      double obfuscatedValue = value + laplace(0, sensitivity / epsilon, rand);
      return Math.max(0, roundParametric(obfuscatedValue, roundingStep));
    } else {
      return 0;
    }
  }

  /**
   * Round the value to the next step
   *
   * @param value         value to round
   * @param stepParameter step to round to, e.g., 1, 5, or 10
   */
  private static long roundParametric(double value, long stepParameter) {
    return Math.round(value / stepParameter) * stepParameter;
  }

  /**
   * Draw from a laplacian distribution.
   *
   * @param mu mean of distribution
   * @param b  diversity of distribution, usually b=sensitivity/epsilon.
   */
  private static double laplace(double mu, double b, SecureRandom rand) {
    double min = -0.5;
    double max = 0.5;
    double random = rand.nextDouble();
    double uniform = min + random * (max - min);
    return mu - b * Math.signum(uniform) * Math.log(1 - 2 * Math.abs(uniform));
  }
}
