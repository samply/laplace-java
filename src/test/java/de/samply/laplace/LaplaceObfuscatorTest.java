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

import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.junit.jupiter.api.Test;

public class LaplaceObfuscatorTest {
  /* Hex to byte[] functions are only required for seeding the test's rng */
  private byte hexToByte(String hexString) {
    int firstDigit = toDigit(hexString.charAt(0));
    int secondDigit = toDigit(hexString.charAt(1));
    return (byte) ((firstDigit << 4) + secondDigit);
  }

  private int toDigit(char hexChar) {
    int digit = Character.digit(hexChar, 16);
    if (digit == -1) {
      throw new IllegalArgumentException("Invalid Hexadecimal Character: " +
          hexChar);
    }
    return digit;
  }

  private byte[] decodeHexString(String hexString) {
    if (hexString.length() % 2 == 1) {
      throw new IllegalArgumentException(
          "Invalid hexadecimal String supplied.");
    }

    byte[] bytes = new byte[hexString.length() / 2];
    for (int i = 0; i < hexString.length(); i += 2) {
      bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
    }
    return bytes;
  }

  @Test
  public void testPrivatizeCachedResult_expectOk()
      throws NoSuchAlgorithmException {
    byte[] seed = decodeHexString("DEADBEEF");
    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
    rng.setSeed(seed);
    LaplaceObfuscator obfuscator = new LaplaceObfuscatorBuilder().randomGenerator(rng).build();
    double sensitivity = 1.0;
    long value = 10L;
    long cache_bin = 1L;
    double epsilon = 0.0001;

    long expectedResult = obfuscator.privatize(value, sensitivity, epsilon, cache_bin);
    long secondResult = obfuscator.privatize(value, sensitivity, epsilon, cache_bin);
    long thirdResult = obfuscator.privatize(value, sensitivity, epsilon, cache_bin);

    assertEquals(expectedResult, secondResult);
    assertEquals(expectedResult, thirdResult);
    assertTrue(obfuscator.isCached(value, sensitivity, cache_bin));
    obfuscator.clearCache();

    long fourthResult = obfuscator.privatize(value, sensitivity, epsilon, cache_bin);
    assertNotEquals(expectedResult, fourthResult);
  }

  @Test
  public void testPrivatizeNonCachingResult_expectOk()
      throws NoSuchAlgorithmException {
    byte[] seed = decodeHexString("DEADBEEF");
    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
    rng.setSeed(seed);
    LaplaceObfuscator obfuscator = new LaplaceObfuscatorBuilder()
        .randomGenerator(rng)
        .useCaching(false)
        .build();
    double sensitivity = 1.0;
    long value = 10L;
    long cache_bin = 1L;
    double epsilon = 0.0001;

    long expectedResult = obfuscator.privatize(value, sensitivity, epsilon, cache_bin);
    long secondResult = obfuscator.privatize(value, sensitivity, epsilon, cache_bin);
    long thirdResult = obfuscator.privatize(value, sensitivity, epsilon, cache_bin);

    assertNotEquals(expectedResult, secondResult);
    assertNotEquals(expectedResult, thirdResult);
  }

  @Test
  public void testPrivatizeNonCachingResult_expectException()
      throws NoSuchAlgorithmException {
    byte[] seed = decodeHexString("DEADBEEF");
    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
    rng.setSeed(seed);
    LaplaceObfuscator obfuscator = new LaplaceObfuscatorBuilder()
        .randomGenerator(rng)
        .useCaching(false)
        .build();
    double sensitivity = 1.0;
    long value = 10L;
    long cache_bin = 1L;

    assertThrows(IllegalStateException.class,
        () -> obfuscator.isCached(value, sensitivity, cache_bin),
        " Expected isCached() to throw, but didn't");
    assertThrows(IllegalStateException.class,
        () -> {
          obfuscator.clearCache();
        });
  }

  @Test
  public void testPrivatizeNewResult_expectOk()
      throws NoSuchAlgorithmException {
    byte[] seed = decodeHexString("DEADBEEF");
    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
    rng.setSeed(seed);
    LaplaceObfuscator obfuscator = new LaplaceObfuscatorBuilder().randomGenerator(rng).build();
    double sensitivity = 1.0;
    long value = 10L;
    double epsilon = 0.0001;

    long expectedResult = obfuscator.privatize(value, sensitivity, epsilon, 1L);
    long actualResult = obfuscator.privatize(value, sensitivity, epsilon, 2L);

    assertNotEquals(expectedResult, actualResult);
  }

  @Test
  public void testPrivatizeSeededRNG_expectOk()
      throws NoSuchAlgorithmException {
    byte[] seed = decodeHexString("DEADBEEF");
    SecureRandom rng1 = SecureRandom.getInstance("SHA1PRNG");
    SecureRandom rng2 = SecureRandom.getInstance("SHA1PRNG");
    SecureRandom rng3 = SecureRandom.getInstance("SHA1PRNG");
    rng1.setSeed(seed);
    rng2.setSeed(seed);
    rng3.setSeed(seed);
    LaplaceObfuscator obfuscator1 = new LaplaceObfuscatorBuilder().randomGenerator(rng1).build();
    LaplaceObfuscator obfuscator2 = new LaplaceObfuscatorBuilder().randomGenerator(rng2).build();
    LaplaceObfuscator obfuscator3 = new LaplaceObfuscatorBuilder().randomGenerator(rng3).build();
    double sensitivity = 1.0;
    long value = 10L;
    long cache_bin = 1L;
    double epsilon = 0.0001;

    long expectedResult1 = obfuscator1.privatize(value, sensitivity, epsilon, cache_bin);
    long expectedResult2 = obfuscator2.privatize(value, sensitivity, epsilon, cache_bin);
    long expectedResult3 = obfuscator3.privatize(value, sensitivity, epsilon, cache_bin);

    assertEquals(expectedResult1, expectedResult2);
    assertEquals(expectedResult1, expectedResult3);
  }

  @Test
  public void testPrivatizeZero_expectOk() throws NoSuchAlgorithmException {
    byte[] seed = decodeHexString("DEADBEEF");
    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
    rng.setSeed(seed);
    LaplaceObfuscator obfuscator = new LaplaceObfuscatorBuilder()
        .randomGenerator(rng)
        .obfuscateZero(false)
        .build();
    double sensitivity = 1.0;
    double epsilon = 0.0001;

    long zeroResult = obfuscator.privatize(0L, sensitivity, epsilon, 1L);
    long nonzeroResult = obfuscator.privatize(100L, sensitivity, epsilon, 2L);

    assertEquals(0, zeroResult);
    assertNotEquals(0, nonzeroResult);
  }

  @Test
  public void testPrivatizeRounding_expectOk() throws NoSuchAlgorithmException {
    byte[] seed = decodeHexString("DEADBEEF");
    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
    rng.setSeed(seed);
    for (int i = 0; i < 1000; i++) {
      int value = rng.nextInt();
      int step = rng.nextInt();
      LaplaceObfuscator obfuscator = new LaplaceObfuscatorBuilder()
          .randomGenerator(rng)
          .roundingStep(step)
          .build();
      double sensitivity = 1.0;
      double epsilon = 0.0001;
      long result = obfuscator.privatize(value, sensitivity, epsilon);
      assertEquals(0, (result % step));
    }
  }
}
