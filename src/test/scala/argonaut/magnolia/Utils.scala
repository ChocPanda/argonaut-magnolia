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
    * Add interoperability with org.scalacheck by returning true at the end
    * ScalaCheck Test requires a non-void return type
    */
  implicit class ArrowAssert[T](lhs: T) {
    def ==>[V](rhs: V): Boolean = {
      utest.ArrowAssert(lhs).==>[V](rhs)
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
