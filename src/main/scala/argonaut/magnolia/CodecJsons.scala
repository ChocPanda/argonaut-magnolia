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

import argonaut._

trait CodecJsons extends EncodeJsons with DecodeJsons {

  implicit val JsonCodecJson: CodecJson[Json] = CodecJson(JsonEncodeJson.encode, JsonDecodeJson.decode)

  implicit val HCursorCodecJson: CodecJson[HCursor] = CodecJson(HCursorEncodeJson.encode, HCursorDecodeJson.decode)

  implicit val UnitCodecJson: CodecJson[Unit] = CodecJson(UnitEncodeJson.encode, UnitDecodeJson.decode)

  implicit def listCodecJson[A](implicit e: CodecJson[A]): CodecJson[List[A]] =
    CodecJson(ListEncodeJson[A](e).encode, ListDecodeJson[A](e).decode)

  implicit def vectorCodecJson[A](implicit e: CodecJson[A]): CodecJson[Vector[A]] =
    CodecJson(VectorEncodeJson[A](listCodecJson(e)).encode, VectorDecodeJson[A](e).decode)

  implicit def streamCodecJson[A](implicit e: CodecJson[A]): CodecJson[Stream[A]] =
    CodecJson(StreamEncodeJson[A](e).encode, StreamDecodeJson[A](e).decode)

  implicit val StringCodecJson: CodecJson[String] = CodecJson(StringEncodeJson.encode, StringDecodeJson.decode)

  implicit val UUIDCodecJson: CodecJson[java.util.UUID] = CodecJson(UUIDEncodeJson.encode, UUIDDecodeJson.decode)

  implicit val DoubleCodecJson: CodecJson[Double] = CodecJson(DoubleEncodeJson.encode, DoubleDecodeJson.decode)

  implicit val FloatCodecJson: CodecJson[Float] = CodecJson(FloatEncodeJson.encode, FloatDecodeJson.decode)

  implicit val IntCodecJson: CodecJson[Int] = CodecJson(IntEncodeJson.encode, IntDecodeJson.decode)

  implicit val LongCodecJson: CodecJson[Long] = CodecJson(LongEncodeJson.encode, LongDecodeJson.decode)

  implicit val ShortCodecJson: CodecJson[Short] = CodecJson(ShortEncodeJson.encode, ShortDecodeJson.decode)

  implicit val ByteCodecJson: CodecJson[Byte] = CodecJson(ByteEncodeJson.encode, ByteDecodeJson.decode)

  implicit val BigDecimalCodecJson: CodecJson[BigDecimal] =
    CodecJson(BigDecimalEncodeJson.encode, BigDecimalDecodeJson.decode)

  implicit val BigIntCodecJson: CodecJson[BigInt] = CodecJson(BigIntEncodeJson.encode, BigIntDecodeJson.decode)

  implicit val BooleanCodecJson: CodecJson[Boolean] = CodecJson(BooleanEncodeJson.encode, BooleanDecodeJson.decode)

  implicit val CharCodecJson: CodecJson[Char] = CodecJson(CharEncodeJson.encode, CharDecodeJson.decode)

  implicit val JDoubleCodecJson: CodecJson[java.lang.Double] =
    CodecJson(JDoubleEncodeJson.encode, JDoubleDecodeJson.decode)

  implicit val JFloatCodecJson: CodecJson[java.lang.Float] = CodecJson(JFloatEncodeJson.encode, JFloatDecodeJson.decode)

  implicit val JIntegerCodecJson: CodecJson[java.lang.Integer] =
    CodecJson(JIntegerEncodeJson.encode, JIntegerDecodeJson.decode)

  implicit val JLongCodecJson: CodecJson[java.lang.Long] = CodecJson(JLongEncodeJson.encode, JLongDecodeJson.decode)

  implicit val JShortCodecJson: CodecJson[java.lang.Short] = CodecJson(JShortEncodeJson.encode, JShortDecodeJson.decode)

  implicit val JByteCodecJson: CodecJson[java.lang.Byte] = CodecJson(JByteEncodeJson.encode, JByteDecodeJson.decode)

  implicit val JBooleanCodecJson: CodecJson[java.lang.Boolean] =
    CodecJson(JBooleanEncodeJson.encode, JBooleanDecodeJson.decode)

  implicit val JCharacterCodecJson: CodecJson[java.lang.Character] =
    CodecJson(JCharacterEncodeJson.encode, JCharacterDecodeJson.decode)

  implicit def optionCodecJson[A](implicit e: CodecJson[A]): CodecJson[Option[A]] =
    CodecJson(OptionEncodeJson[A](e).encode, OptionDecodeJson[A](e).decode)

  implicit def eitherCodecJson[A, B](implicit ea: CodecJson[A], eb: CodecJson[B]): CodecJson[Either[A, B]] =
    CodecJson(EitherEncodeJson[A, B](ea, eb).encode, EitherDecodeJson[A, B](ea, eb).decode)

  implicit def mapCodecJson[K, V](
    implicit ek: EncodeJsonKey[K],
    dk: DecodeJsonKey[K],
    cv: CodecJson[V]
  ): CodecJson[Map[K, V]] =
    CodecJson(
      MapEncodeJson[K, V](ek, cv).encode,
      MapDecodeJson[V](cv).decode(_).map(_.map { case (k, v) => (dk.fromJsonKey(k), v) })
    )

  implicit def setCodecJson[A](implicit e: CodecJson[A]): CodecJson[Set[A]] =
    CodecJson(SetEncodeJson[A](e).encode, SetDecodeJson[A](e).decode)

}

trait DecodeJsonKey[K] {
  self =>
  def fromJsonKey(key: String): K

  final def map[B](f: K => B): DecodeJsonKey[B] = { key: String =>
    f(self.fromJsonKey(key))
  }
}

object DecodeJsonKey {

  @inline def apply[A](implicit A: DecodeJsonKey[A]): DecodeJsonKey[A] = A

  def from[A](f: String => A): DecodeJsonKey[A] = { key: String =>
    f(key)
  }

  implicit val StringDecodeJsonKey: DecodeJsonKey[String] = from(x => x)
}
