# Samply.Laplace

Samply.Laplace is a Java library to obfuscate discrete values using differential privacy-inspired methods.
The values are obfuscated by perturbing them with random values sampled from a laplace distribution with configurable parameters for location, sensitivity, and privacy budget. To not leak more information for repeated identical or equivalent queries, the perturbation is (per default) cached. The library exposes an API to finely control the caching behaviour, e.g. to obfuscate data that is stratified in a number of ways. Furthermore, a rounding step can be configured to never leak individual-level data.  
Optionally, true zero values can be returned unperturbed. While lowering the privacy level slightly, this can vastly improve subsequent processes for data access control.
Samply.Laplace is threat-safe and can be used in a concurrent environment.

## Dependencies

Samply.Laplace is light on the dependencies, however two are required:

* [Apache Commons Lang3]() for the `Tripel` data structure, and
* [Junit 5]( ) for the unit tests.

## Getting Started

In this section the "installation" and usage of Samply.Laplace is described.

### Include via maven

To use Samply.Laplace in your project, please include the following dependency in your `pom.xml`:

```xml
<dependency>
  <groupId>de.samply</groupId>
  <artifactId>laplace</artifactId>
  <version>0.0.1</version>
</dependency>
```

### Example Usage

Using and configuring Samply.Laplace requires to build a LaplaceObfuscator:

```java
import de.samply.laplace.LaplaceObfuscator;
import de.samply.laplace.LaplaceObfuscatorBuilder;

import java.security.SecureRandom;

class main {
 public static void main(String args[]) {
   LaplaceObfuscator obfuscator = new LaplaceObfuscatorBuilder()
                                        .randomGenerator(new SecureRandom()) // Optionally provide a random generator, e.g. for seeding in case of unit tests. Defaults to an automatically seeded SecureRandom() generator.
                                        .obfuscateZero(false) // Should values of zero be obfuscated or reported? Defaults to obfuscate all values, including zero
                                        .useCaching(true) // Should caching similar results be used? Generally, this is a good idea to increase privacy, so only deactivate if you have a good reason.
                                        .roundingStep(10) // This example rounds the obfuscated results to the next 10s. Defaults to 1, to return discrete results.
                                        .build();
   double sensitivity = 1.0; // Sensitivity of the query/data. For easy (patient) counting queries usually 1.
   double epsilon = 0.28; // Privacy budget parameter. The chosen values for sensitivity and epsilon result in a laplace distribution with stddev of around 5
   long cache_bin = 1; // Optional parameter to force the generation of new randomness, e.g. for differently stratified counts

   long firstTrueValue = 10;
   long firstObfuscatedValue = obfuscator.privatize(firstTrueValue, sensitivity, epsilon, cache_bin);

   // obfuscating the same query again, should result in the same perturbation
   long sameAsFirst = obfuscator.privatize(firstTrueValue, sensitivity, epsilon, cache_bin);

   // However if you change the cache bin, you can force the generation of a new perturbation
   long DifferentThenFirst = obfuscator.privatize(firstTrueValue, sensitivity, epsilon, cache_bin + 1);
  }
 }
```

### Interface of LaplaceObfuscator

While the above example shows the typical usage, `LaplaceObfuscator` provides additional methods, e.g. for convenience or to manage the result cache.

* The constructor is not supposed to be used directly. Please use the `LaplaceObfuscatorBuilder` to configure and create the obfuscator.
* There exsist two `privatize` methods: One omits the `cache_bin` parameter for convenience and uses a single cache bin.
* The cache is based on the triple `(sensitivity, trueValue, cache_bin)`. With `isCached(...)` you can inspect the caching status. With `clearCache()` you clear *all* cache bins.

Note: the `toString()` methods returns the cache contents. Never leak this information to a user, as this contains the obfuscated *and* unobfuscated values.

## License

Distributed under the Apache-2.0 License. See [LICENSE](LICENSE) for more information.
