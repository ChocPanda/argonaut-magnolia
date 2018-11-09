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

import scala.language.experimental.macros

object EncodeJsonTool {

  def gen[T]: Typeclass[T] = macro Magnolia.gen[T]

  type Typeclass[T] = EncodeJson[T]

  val typeHintField = "type"

  def combine[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] = EncodeJson { t: T =>
    Json(ctx.parameters.map { param =>
      (param.label, param.typeclass.encode(param.dereference(t)))
    }: _*)
  }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = EncodeJson { t: T =>
    ctx.dispatch(t) { subtype =>
      val json = subtype.typeclass.encode(subtype.cast(t))
      json.->:((typeHintField, JString(subtype.typeName.short)))
    }
  }
}
