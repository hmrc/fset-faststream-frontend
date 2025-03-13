/*
 * Copyright 2025 HM Revenue & Customs
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

package models

import models.ProgressStatuses.ProgressStatus
import play.api.libs.json.{JsValue, Json, OFormat}
import testkit.BaseSpec

class ApplicationDataSpec extends BaseSpec {

  "ProgressStatuses" should {
    "serialize to and from json" in {
      val myTest = MyTest(ProgressStatuses.SUBMITTED)
      val json = Json.toJson(myTest)

      val jsonString = """{"progressStatus":"SUBMITTED"}"""
      jsonString mustBe json.toString

      val jsValue: JsValue = Json.parse(jsonString)
      val fromJson = Json.fromJson[MyTest](jsValue).get
      fromJson mustBe myTest
    }

    "be able to deserialize all progress statuses from json (this uses reflection)" in {
      import scala.reflect.runtime.universe._

      // Creates a runtime reflection mirror from the JVM classloader for this instance
      val mirror = runtimeMirror(this.getClass.getClassLoader)

      // Get the reflective mirror for the given instance (this)
      // Such a mirror can be used to further reflect against the members of the object
      // to get/set fields, invoke methods and inspect inner classes and objects.
      val instanceMirror: InstanceMirror = mirror.reflect(ProgressStatuses)

      // The symbol corresponds to the runtime class of the reflected instance
      // Then we fetch the type signature of the symbol
      val scalaType: Type = instanceMirror.symbol.typeSignature

      val members = scalaType.members

      val statuses = members.collect { member =>
        member.typeSignature match {
          // <:< is a method that checks if the type conforms to the given type argument
          // e.g. typeOf[ProgressStatus]
          case theType if theType <:< typeOf[ProgressStatus] =>
            val module = member.asModule
            // Reflects against a static module symbol and returns a mirror that can be used to get
            // the instance of the object
            mirror.reflectModule(module).instance.asInstanceOf[ProgressStatus]
        }
      }.toSeq

      def getProgressStatusKey(ps: ProgressStatus): String = {
        // These progress statuses contains hyphenated keys instead of keys with underscores so we need to deal with them as special cases
        val hyphenatedKeys = Seq(
          ProgressStatuses.PERSONAL_DETAILS, ProgressStatuses.SCHEME_PREFERENCES,
          ProgressStatuses.LOCATION_PREFERENCES, ProgressStatuses.ASSISTANCE_DETAILS
        )
        if (hyphenatedKeys.contains(ps)) {
          ps.key
        } else {
          ps.toString
        }
      }

      statuses.foreach { ps =>
        val jsValue: JsValue = Json.parse(s"{\"progressStatus\":\"${getProgressStatusKey(ps)}\"}")
        val fromJson = Json.fromJson[MyTest](jsValue).get

        val myTest = MyTest(ps)
        fromJson mustBe myTest
      }
    }
  }

  case class MyTest(progressStatus: ProgressStatus)
  object MyTest {
    implicit val format: OFormat[MyTest] = Json.format[MyTest]
  }
}
