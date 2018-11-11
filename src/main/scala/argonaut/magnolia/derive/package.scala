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

import _root_.argonaut._
import _root_.magnolia._
import mercator.Monadic

import scala.language.experimental.macros

package object derive {

  type Typeclass[T] = CodecJson[T]

  val typeHintField = "type"

  implicit private val monadic: Monadic[DecodeResult] = new Monadic[DecodeResult] {
    override def point[A](value: A): DecodeResult[A] = DecodeResult.ok(value)

    override def flatMap[A, B](from: DecodeResult[A], fn: A => DecodeResult[B]): DecodeResult[B] = from.flatMap(fn)

    override def map[A, B](from: DecodeResult[A], fn: A => B): DecodeResult[B] = from.map(fn)
  }

  def combine[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] = {
    val encoder: T => Json = { t: T =>
      Json(ctx.parameters.map { param =>
        (param.label, param.typeclass.encode(param.dereference(t)))
      }: _*)
    }
    val decoder: HCursor => DecodeResult[T] = { cursor: HCursor =>
      ctx.constructMonadic { p =>
        (cursor --\ p.label).as(p.typeclass).map { a: Param[Typeclass, T]#PType =>
          a
        }
      }
    }
    CodecJson[T](encoder, decoder)
  }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = {
    val encoder: T => Json = { t: T =>
      ctx.dispatch(t) { subtype =>
        val json = subtype.typeclass.encode(subtype.cast(t))
        Json.obj((subtype.typeName.short, json))
      }
    }
    val typeNames = Map(ctx.subtypes.map(subType => (subType.typeName.short, subType.typeclass)): _*)
    val decoder: HCursor => DecodeResult[T] = { cursor: HCursor =>
      for {
        typeName  <- cursor.field(typeHintField).as[String] // This isn't what I meant
        typeclass <- DecodeResult(typeNames.get(typeName).toRight(("Unrecognized type hint:", cursor.history)))
        res       <- typeclass.decode(cursor)
      } yield res
    }
    CodecJson[T](encoder, decoder)
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]

}
