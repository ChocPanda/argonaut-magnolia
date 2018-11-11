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

package argonaut.magnolia.hinted.derive

import argonaut.magnolia.Utils._
import argonaut.magnolia.adt._
import argonaut._, Argonaut._

import utest.{ ArrowAssert => _, _ }

import org.scalacheck.Arbitrary
import org.scalacheck.Prop._
import org.scalacheck.magnolia._

object DerivationTest extends TestSuite {

  val tests = Tests {
    'Decode - {
      'Empty - {
        assert(Json.obj().as[Empty.type].value.nonEmpty)
      }

      'EmptyCC - {
        assert(Json.obj().as[EmptyCC].value.nonEmpty)
      }
    }

    'Encode - {
      'Empty - {
        Empty.asJson ==> Json.obj()
      }

      'EmptyCC - {
        EmptyCC().asJson ==> Json.obj()
      }
    }

    'adt - {

      def testCodecLaw[T: Arbitrary]()(implicit codec: CodecJson[T]): Unit =
        forAll { t: T =>
          CodecJson.codecLaw(codec)(t)
        }.validate()

      'ProductType - {

        'CodecLaw - {

          'Leaf - testCodecLaw[Leaf]()

          'Branch - testCodecLaw[Branch]()

          'GLeaf - testCodecLaw[GLeaf[Int]]()

          'GBranch - testCodecLaw[GBranch[Int]]()

          'Company - testCodecLaw[Company]()

          'Human - testCodecLaw[Human]()

          'Address - testCodecLaw[Address]()

          'Greek - testCodecLaw[Greek]()

          'Cyrillic - testCodecLaw[Cyrillic]()

          'Latin - testCodecLaw[Latin]()

          'Letter - testCodecLaw[Letter]()

          'Country - testCodecLaw[Country]()

          'Language - testCodecLaw[Language]()

          'Person - testCodecLaw[Person]()

          'Date - testCodecLaw[Date]()

          'DateRange - testCodecLaw[DateRange]()

        }
      }

      'SumType - {

        'CodecLaw - {
          'Tree - testCodecLaw[Tree]()

          'GTree - testCodecLaw[GTree[Int]]()

          'Entity - testCodecLaw[Entity]()

          'Alphabet - testCodecLaw[Alphabet]()

          'Month - testCodecLaw[Month]()
        }
      }
    }
  }
}
