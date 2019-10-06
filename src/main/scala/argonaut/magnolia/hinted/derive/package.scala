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

package argonaut.magnolia.hinted

import _root_.argonaut._
import _root_.magnolia._
import mercator.Monadic

import scala.language.experimental.macros

package object derive extends argonaut.magnolia.CodecJsons {

  type Typeclass[T] = CodecJson[T]

  val typeHintField = "type"

  implicit private val monadic: Monadic[DecodeResult] = new Monadic[DecodeResult] {
    override def point[A](value: A): DecodeResult[A] = DecodeResult.ok(value)

    override def flatMap[A, B](from: DecodeResult[A])(fn: A => DecodeResult[B]): DecodeResult[B] = from.flatMap(fn)

    override def map[A, B](from: DecodeResult[A])(fn: A => B): DecodeResult[B] = from.map(fn)
  }

  def combine[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] = {
    val encoder = { t: T =>
      Json(ctx.parameters.map { param =>
        (param.label, param.typeclass.encode(param.dereference(t)))
      }: _*)
    }
    val decoder: HCursor => DecodeResult[T] = { cursor: HCursor =>
      ctx.constructMonadic { param =>
        val field = cursor.--\(param.label)
        field.as(param.typeclass).map { a: Param[Typeclass, T]#PType =>
          a
        }
      }
    }

    CodecJson(encoder, decoder)
  }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = {
    val encoder = { t: T =>
      ctx.dispatch(t) { subtype =>
        val json = subtype.typeclass.encode(subtype.cast(t))
        json.->:((typeHintField, JString(subtype.typeName.short)))
      }
    }
    val decoder: HCursor => DecodeResult[T] = {
      val names = Map(ctx.subtypes.map(subType => (subType.typeName.short, subType.typeclass)): _*)
      cursor: HCursor =>
        for {
          typeName  <- (cursor --\ typeHintField).as[String]
          typeClass <- DecodeResult(names.get(typeName).toRight(("Unrecognized type hint", cursor.history)))
          res       <- typeClass.decode(cursor)
        } yield res
    }

    CodecJson(encoder, decoder)
  }

  implicit def deriveCodec[T]: CodecJson[T] = macro Magnolia.gen[T]

}
