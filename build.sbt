/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.typesafe.sbt.digest.Import.*
import com.typesafe.sbt.gzip.Import.*
import com.typesafe.sbt.web.Import.*
import com.typesafe.sbt.web.SbtWeb
import sbt.Keys.*
import sbt.*
import play.sbt.routes.RoutesKeys.*
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.*
import DefaultBuildSettings.{defaultSettings, scalaSettings, targetJvm}
import play.sbt.PlayImport.PlayKeys.playDefaultPort

val appName = "fset-faststream-frontend"
val appDependencies : Seq[ModuleID] = AppDependencies()

lazy val playSettings : Seq[Setting[?]] = Seq(routesImport ++= Seq("binders.CustomBinders._", "models._"))

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

ThisBuild / scalaVersion := "3.3.4"
ThisBuild / majorVersion := 0

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtWeb, SbtDistributablesPlugin)
  .settings(playSettings *)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(playDefaultPort := 9284)
  .settings(
    targetJvm := "jvm-1.8",
    routesImport += "controllers.Binders._",
    libraryDependencies ++= appDependencies,
    Test / parallelExecution := false,
    Test / fork := true,
    Test / javaOptions += "-Dlogger.resource=logback-test.xml",
    Test / javaOptions += "-Dmicroservice.services.user-management.url.host=http://localhost:11111",
    retrieveManaged := true,
    scalacOptions += "-feature",
    // Use these to help troubleshoot Scala 3 cyclic dependency compile errors
//    scalacOptions += "-Ydebug-cyclic",
//    scalacOptions += "-explain-cyclic",
//    scalacOptions += "-explain",
    // Currently don't enable warning in value discard in tests until ScalaTest 3
    Compile / compile / scalacOptions += "-Ywarn-value-discard"
  )
  // Even though log4j does not appear in the dependency graph, sbt still downloads it into the Coursier cache
  // when we compile. It is version log4j-1.2.17.jar, which contains the security vulnerabilities so as a workaround
  // we exclude any log4j library here
  .settings(excludeDependencies += "log4j" % "log4j")
  // play-silhouette 6.1.1 pulls in transitive dependencies which have vulnerabilities
  // (bcpkix-jdk15on:1.60 and bcprov-jdk15on:1.60) so replace them here with newer versions
  .settings(dependencyOverrides += "org.bouncycastle" % "bcpkix-jdk15on" % "1.70")
  .settings(dependencyOverrides += "org.bouncycastle" % "bcprov-jdk15on" % "1.70")
  .settings(Compile / doc / sources := Seq.empty)
  .settings(pipelineStages := Seq(digest, gzip))

  // Disable scalariform awaiting release of to fix parameter formatting for implicit parameters
//  .settings(ScalariformSettings())

  .settings(compileScalastyle := (Compile / scalastyle).toTask("").value,
    (Compile / compile) := ((Compile / compile) dependsOn compileScalastyle).value
  )
  // Silhouette transitive dependencies require that the Atlassian repository be first in the resolver list
  .settings(resolvers += ("Atlassian Releases" at "https://maven.atlassian.com/public/"))
  .settings(resolvers ++= Seq(Resolver.jcenterRepo))
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)