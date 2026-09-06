package cz.czechcraft.fabric;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Guards the <em>packaged artifact</em> rather than the dev classpath.
 *
 * <p>Every other check in this repo — {@code runClient}, {@code runDatagen}, CI {@code build}, and
 * the other JUnit tests — resolves {@code common} through the {@code implementation
 * project(":common")} dependency. They therefore pass whether or not those classes are actually
 * bundled into the mod jar that players download. v1.0.0 shipped with all five {@code common}
 * classes missing and crashed on startup for every player on every Minecraft version; nothing in
 * the build noticed. These tests open the built jar itself, which is the only way to catch it.
 */
class JarPackagingTest {

  private static final String MOD_JAR_PROPERTY = "czechcraft.modJar";
  private static final String COMMON_CLASSES_PROPERTY = "czechcraft.commonClassesDirs";
  private static final String MOD_PACKAGE_PREFIX = "cz.czechcraft.";

  /**
   * Every class compiled into {@code common} must be present in the mod jar. Derived from the
   * compiled output rather than a hardcoded list, so new items and packages are covered
   * automatically.
   */
  @Test
  void modJarContainsEveryCommonClass() throws IOException {
    Set<String> expected = commonClassEntries();
    assertTrue(
        !expected.isEmpty(),
        "found no compiled classes in "
            + COMMON_CLASSES_PROPERTY
            + " — is the common module built?");

    Set<String> missing = new TreeSet<>();
    try (JarFile jar = new JarFile(modJar().toFile())) {
      for (String entry : expected) {
        if (jar.getJarEntry(entry) == null) {
          missing.add(entry);
        }
      }
    }

    assertTrue(
        missing.isEmpty(),
        "mod jar is missing "
            + missing.size()
            + " class(es) from the common module — the shipped mod would crash on startup with"
            + " NoClassDefFoundError. Missing: "
            + missing);
  }

  /**
   * Every CzechCraft class in the jar must resolve using only the jar as its source. Uses a
   * child-first loader for {@code cz.czechcraft.*} so the test classpath's copy of {@code common}
   * cannot mask a missing entry — the exact blind spot that let v1.0.0 ship.
   */
  @Test
  void modClassesResolveFromJarAlone() throws IOException {
    List<String> classNames = new ArrayList<>();
    try (JarFile jar = new JarFile(modJar().toFile())) {
      jar.stream()
          .map(e -> e.getName())
          .filter(n -> n.startsWith("cz/czechcraft/") && n.endsWith(".class"))
          .map(n -> n.substring(0, n.length() - ".class".length()).replace('/', '.'))
          .forEach(classNames::add);
    }
    assertTrue(!classNames.isEmpty(), "mod jar contains no CzechCraft classes at all");

    List<String> failures = new ArrayList<>();
    try (URLClassLoader loader = jarFirstLoader()) {
      for (String name : classNames) {
        try {
          Class.forName(name, false, loader);
        } catch (Throwable t) {
          failures.add(name + " -> " + t.getClass().getSimpleName());
        }
      }
    }

    if (!failures.isEmpty()) {
      fail("class(es) in the mod jar could not be linked from the jar alone: " + failures);
    }
  }

  /**
   * Loads {@code cz.czechcraft.*} exclusively from the mod jar and everything else (Minecraft,
   * Fabric API) from the test classpath.
   */
  private static URLClassLoader jarFirstLoader() throws MalformedURLException {
    URL[] urls = {modJar().toUri().toURL()};
    return new URLClassLoader(urls, JarPackagingTest.class.getClassLoader()) {
      @Override
      protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!name.startsWith(MOD_PACKAGE_PREFIX)) {
          return super.loadClass(name, resolve);
        }
        synchronized (getClassLoadingLock(name)) {
          Class<?> loaded = findLoadedClass(name);
          if (loaded == null) {
            loaded = findClass(name);
          }
          if (resolve) {
            resolveClass(loaded);
          }
          return loaded;
        }
      }
    };
  }

  /** Jar-relative entry names ({@code cz/czechcraft/CzechCraft.class}) for common's output. */
  private static Set<String> commonClassEntries() {
    Set<String> entries = new TreeSet<>();
    for (String dir : requiredProperty(COMMON_CLASSES_PROPERTY).split(java.io.File.pathSeparator)) {
      Path root = Path.of(dir);
      if (!Files.isDirectory(root)) {
        continue;
      }
      try (Stream<Path> files = Files.walk(root)) {
        files
            .filter(p -> p.toString().endsWith(".class"))
            .map(p -> root.relativize(p).toString().replace(java.io.File.separatorChar, '/'))
            .forEach(entries::add);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return entries;
  }

  private static Path modJar() {
    Path jar = Path.of(requiredProperty(MOD_JAR_PROPERTY));
    assertTrue(
        Files.isRegularFile(jar), "mod jar not found at " + jar + " — run :fabric:jar first");
    return jar;
  }

  private static String requiredProperty(String key) {
    String value = System.getProperty(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(
          "system property "
              + key
              + " is not set — these tests must run through Gradle (:fabric:test), which wires it"
              + " to the built artifact");
    }
    return value;
  }
}
