val snapshot = true

allprojects {
  var ver = "1.13.1"
  if (findProperty("otel.stable") != "true") {
    ver += "-alpha"
  }
  if (snapshot) {
    ver += "-debug_clnj"
  }
  version = ver
}
