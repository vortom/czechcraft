package cz.czechcraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common entry point for CzechCraft. Loader-agnostic. */
public final class CzechCraft {

  public static final String MOD_ID = "czechcraft";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  private CzechCraft() {
    // static-only
  }
}
