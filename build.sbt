lazy val metaV = "3.6.0"

lazy val accesibleScala = project
  .in(file("accessible"))
  .settings(
    moduleName := "accesible-scala",
    assemblyJarName in assembly := "as.jar",
    mainClass.in(assembly) := Some("ch.epfl.scala.accessible.Main"),
    libraryDependencies ++= List(
      "com.lihaoyi" %% "pprint" % "0.5.2", // for debugging
      "org.scalameta" %% "scalameta" % metaV,
      "org.scalameta" %% "contrib" % metaV
    )
  )

lazy val testsShared = project
  .in(file("tests/shared"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "utest" % "0.6.3",
      "org.scalameta" %% "testkit" % metaV
    )
  )
  .dependsOn(accesibleScala)

lazy val unit = project
  .in(file("tests/unit"))
  .dependsOn(testsShared)

lazy val slow = project
  .in(file("tests/slow"))
  .settings(
    libraryDependencies += "me.tongfei" % "progressbar" % "0.5.5",
    fork in (Test, test) := true,
    fork in (Test, testOnly) := true,
    fork in (Test, testQuick) := true,
    cancelable in Global := true,
    javaOptions in (Test, test) ++= {
      val mem =
        if (sys.env.get("CI").isDefined) "4"
        else sys.env.get("SLOWMEM").getOrElse("20")

      Seq(
        "-Xss20m",
        "-Xms4G",
        s"-Xmx${mem}G",
        "-XX:ReservedCodeCacheSize=1024m",
        "-XX:+TieredCompilation",
        "-XX:+CMSClassUnloadingEnabled"
      )
    },
    javaOptions in (Test, testOnly) ++= (javaOptions in (Test, test)).value,
    javaOptions in (Test, testQuick) ++= (javaOptions in (Test, test)).value
  )
  .dependsOn(testsShared)
