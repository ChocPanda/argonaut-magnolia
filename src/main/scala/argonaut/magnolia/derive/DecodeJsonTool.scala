package argonaut.magnolia.derive

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
      (cursor --\ p.label).as(p.typeclass)
    }
  }

  def dispatch[T](sealedTrait: SealedTrait[Typeclass, T]): Typeclass[T] = {
    val typeNames = Map(sealedTrait.subtypes.map(subType => (subType.typeName.short, subType.typeclass)): _*)
    cursor: HCursor =>
      for {
        typeName  <- cursor.field(typeHintField).as[String]
        typeclass <- DecodeResult(typeNames.get(typeName).toRight(("Unrecognized type hint:", cursor.history)))
        res       <- typeclass.decode(cursor)
      } yield res
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}

object DecodeJsonTool extends DecodeJsonTool
