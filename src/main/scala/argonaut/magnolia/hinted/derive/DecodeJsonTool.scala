/*
 * Copyright 2018 Matt Searle
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

package argonaut.magnolia.hinted.derive

import _root_.argonaut._
import _root_.magnolia._
import mercator.Monadic

import scala.language.experimental.macros

trait DecodeJsonTool {

  type Typeclass[T] = DecodeJson[T]

  val typeHintField = "type"

  implicit private val monadic: Monadic[DecodeResult] = new Monadic[DecodeResult] {
    override def point[A](value: A): DecodeResult[A] = DecodeResult.ok(value)

    override def flatMap[A, B](from: DecodeResult[A], fn: A => DecodeResult[B]): DecodeResult[B] = from.flatMap(fn)

    override def map[A, B](from: DecodeResult[A], fn: A => B): DecodeResult[B] = from.map(fn)
  }

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = { cursor: HCursor =>
    caseClass.constructMonadic { p =>
      (cursor --\ p.label).as(p.typeclass).map { a: Param[Typeclass, T]#PType =>
        a
      }
    }
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    val names = Map(sealedTrait.subtypes.map(subType => (subType.typeName.short, subType.typeclass)): _*)
    cursor: HCursor =>
      for {
        typeName  <- cursor.field(typeHintField).as[String]
        typeClass <- DecodeResult(names.get(typeName).toRight(("Unrecognized type hint", cursor.history)))
        res       <- typeClass.decode(cursor)
      } yield res
  }

  def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}

object DecodeJsonTool extends DecodeJsonTool {
  implicit def derive[T]: DecodeJson[T] = macro Magnolia.gen[T]
}
