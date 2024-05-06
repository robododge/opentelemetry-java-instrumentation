val stableVersion = "1.21.0"
val alphaVersion = "1.21.0-alpha"

allprojects {
  if (findProperty("otel.stable") != "true") {
    version = alphaVersion
  } else {
    version = stableVersion
  }
  version = "1.21.0-koala-v2"
}
