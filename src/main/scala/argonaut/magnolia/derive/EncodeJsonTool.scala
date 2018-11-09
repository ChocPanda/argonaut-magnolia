package argonaut.magnolia.derive

import _root_.argonaut._
import _root_.magnolia._

import scala.language.experimental.macros

trait EncodeJsonTool {

  type Typeclass[T] = EncodeJson[T]

  def combine[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] = EncodeJson { t: T =>
    Json(ctx.parameters.map { param =>
      (param.label, param.typeclass.encode(param.dereference(t)))
    }: _*)
  }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = EncodeJson { t: T =>
    ctx.dispatch(t) { subtype =>
      val json = subtype.typeclass.encode(subtype.cast(t))
      Json.obj((subtype.typeName.short, json))
    }
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}

object EncodeJsonTool extends EncodeJsonTool
