/*
 * Copyright 2018 com.github.chocpanda
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

package argonaut.magnolia

import org.scalacheck.{ Prop, Test }

object Utils {

  /**
    * This was largely copy and pasted from:
    * https://github.com/lihaoyi/utest/blob/5b382ae0a4bb3a25d8cb64d332b7bcb7fc73ace2/utest/shared/src/main/scala/utest/asserts/Asserts.scala#L185
    *
    * I have just extended it a little for interoperability with org.scalacheck by returning true at the end
    */
  implicit class ArrowAssert[T](lhs: T) {
    def ==>[V](rhs: V): Boolean = {
      (lhs, rhs) match {
        // Hack to make Arrays compare sanely; at some point we may want some
        // custom, extensible, typesafe equality check but for now this will do
        case (lhs: Array[_], rhs: Array[_]) =>
          Predef.assert(lhs.toSeq == rhs.toSeq, s"==> assertion failed: ${lhs.toSeq} != ${rhs.toSeq}")
        case (_, _) =>
          Predef.assert(lhs == rhs, s"==> assertion failed: $lhs != $rhs")
      }

      true
    }
  }

  implicit class PropExtensions(val prop: Prop) extends AnyVal {
    def validate(): Unit = {
      val result = Test.check(Test.Parameters.default, prop)
      assert(result.passed)
    }
  }
}
