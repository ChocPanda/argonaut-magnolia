# Argonaut Magnolia #
[![Build Status](https://travis-ci.org/ChocPanda/argonaut-magnolia.svg?branch=master)](https://travis-ci.org/ChocPanda/argonaut-magnolia)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.chocpanda/argonaut-magnolia_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.chocpanda/argonaut-magnolia)

This project is in an experimental state, but it compiles and there are alpha releases out but the unit tests fail intermittently.
I would appreciate any feedback/help if it's useful.

I am attempting to derive [Argonaut](https://github.com/argonaut-io/argonaut) codecs
using [Magnolia](https://github.com/propensive/magnolia). The functionality would be very similar to
[ArgonautShapeless](https://github.com/alexarchambault/argonaut-shapeless) but hopefully with the
compile time benefits that magnolia provides over shapeless.

It's very simple to use simply add to your build.sbt:
```scala
libraryDependencies += "com.github.chocpanda" % argonaut-magnolia % 0.2.0
```

and import:
```scala
import argonaut.magnolia.derive._
```

or
```scala
import argonaut.magnolia.hinted.derive._
```

## Examples ##

#### Basic product types ####

```scala
package argonaut.magnolia.example

import argonaut.magnolia.derive._
import argonaut._, Argonaut._

object ExampleTest {
    
    sealed trait Entity
    
    final case class Company(name: String)         extends Entity
    final case class Human(name: String, age: Int) extends Entity
    
    // The automatic derivation of simple product types integrates with the existing summoner methods in argonaut
    val encodeCompany = EncodeJson.of[Company]
    val decodeCompany = DecodeJson.of[Company]
    // Using this implicit function which can be called explicitly if necessary
    val companyCodec = deriveCodec[Company]
    
    val company = Company("ChocPanda Ltd")
    encodeCompany.encode(company).spaces2
    /**
      * res1: String =
      * {
      *   "name" : "ChocPanda Ltd"
      * }
      */
      
    Parse.decode(res1)(decodeCompany)
    /**
      * res2: Either[Either[String,(String, argonaut.CursorHistory)],Company] = Right(Company(ChocPanda Ltd))
      */
    
}
```

#### Nested product types ####

```scala
package argonaut.magnolia.example

import argonaut.magnolia.derive._
import argonaut._, Argonaut._

object ExampleTest {
    
    final case class Foo(param1: Int, param2: Boolean, param3: String)
    final case class Bar(fooA: Foo, fooB: Foo)
    
    Bar(Foo(42, false, "Hello internet"), Foo(754, true, "Auf Wiedersehen")).asJson
    /** res5: argonaut.Json = {
      *  "fooA":{"param1":42,"param2":false,"param3":"Hello internet"},
      *  "fooB":{"param1":754,"param2":true,"param3":"Auf Wiedersehen"}
      *  }
      */
}

```

#### Sum types ####

The default codec reifies the type information into a top level member outside of the
resultant json object. In the same way [ArgonautShapeless](https://github.com/alexarchambault/argonaut-shapeless) does.

```scala

package argonaut.magnolia.example

import argonaut.magnolia.derive._
import argonaut._, Argonaut._

object ExampleTest {
    
    sealed trait Entity
    
    final case class Company(name: String)         extends Entity
    final case class Human(name: String, age: Int) extends Entity
    
    EncodeJson.of[Entity].encode(Company("ChocPanda Ltd")).spaces2
    /**
      * res10: String =
      *  {
      *    "Company" : {
      *      "name" : "ChocPanda Ltd"
      *    }
      *  }
      */

    Parse.decode[Entity](res10)
    
    /**
      * res15: Either[Either[String,(String, argonaut.CursorHistory)],Entity] = Right(Company(ChocPanda Ltd)) 
      */
}
```

#### Recursive product types ####

```scala
package argonaut.magnolia.example

import argonaut.magnolia.derive._
import argonaut._, Argonaut._

object ExampleTest {

    sealed trait Tree
    final case class Leaf(value: String)             extends Tree
    final case class Branch(left: Tree, right: Tree) extends Tree
    
    Branch(Leaf("Left leaf"), Branch(Leaf("Right then left leaf"), Leaf("Right then right leaf"))).asJson.spaces2
    /**
      * res8: String =
      *  {
      *    "left" : {
      *      "Leaf" : {
      *        "value" : "Left leaf"
      *      }
      *    },
      *    "right" : {
      *      "Branch" : {
      *        "left" : {
      *          "Leaf" : {
      *            "value" : "Right then left leaf"
      *          }
      *        },
      *        "right" : {
      *          "Leaf" : {
      *            "value" : "Right then right leaf"
      *          }
      *        }
      *      }
      *    }
      *  }
      */
}
```

#### Generics ####

```scala
package argonaut.magnolia.example

import argonaut.magnolia._

object ExampleTest {
    
    sealed trait GTree[+T]
    final case class GLeaf[+T](value: T)                          extends GTree[T]
    final case class GBranch[+T](left: GTree[T], right: GTree[T]) extends GTree[T]
    
    GBranch(GLeaf("Left leaf"), GBranch(GLeaf("Right then left leaf"), GLeaf("Right then right leaf"))).asJson.spaces2
    /**
      * res8: String =
      *  {
      *    "left" : {
      *      "GLeaf" : {
      *        "value" : "Left leaf"
      *      }
      *    },
      *    "right" : {
      *      "GBranch" : {
      *        "left" : {
      *          "GLeaf" : {
      *            "value" : "Right then left leaf"
      *          }
      *        },
      *        "right" : {
      *          "GLeaf" : {
      *            "value" : "Right then right leaf"
      *          }
      *        }
      *      }
      *    }
      *  }
      */
    
    
}
```

### Type hints ###

Hinted Argonaut codecs was inspired by [circe-field-hints](https://github.com/drivetribe/circe-field-hints) where
instead of reifying the type hint outside of the object as is done in [ArgonautShapeless](https://github.com/alexarchambault/argonaut-shapeless)
we add an extra field to the resultant json object with the type hint. (Note the alternative import to use this feature)

```scala

package argonaut.magnolia.example

import argonaut.magnolia.hinted.derive._
import argonaut._, Argonaut._

object ExampleTest {
    
    sealed trait Entity
    
    final case class Company(name: String)         extends Entity
    final case class Human(name: String, age: Int) extends Entity
    
    EncodeJson.of[Entity].encode(Company("ChocPanda Ltd")).spaces2
    /**
      * res10: String =
      *  {
      *    "name" : "ChocPanda Ltd",
      *    "type" : "Company"
      *  }
      */

    Parse.decode[Entity](res10)
    
    /**
      * res15: Either[Either[String,(String, argonaut.CursorHistory)],Entity] = Right(Company(ChocPanda Ltd)) 
      */
}
```

This is useful firstly because if you do any manual json parsing or write a function to do any manual parsing you have
the same number of fields to go down into the json object regardless of whether the type field is serialised, which in
our case would be the difference between:

```scala

package argonaut.magnolia.example

import argonaut._, Argonaut._
    
sealed trait Entity

final case class Company(name: String)         extends Entity
final case class Human(name: String, age: Int) extends Entity

object HintedTest {
        
    import argonaut.magnolia.hinted.derive._
    
    val human: Human = Human("ChocPanda", 26)
    val entity: Entity = human
    
    /**
      * scala> human.asJson.spaces2
      *  res2: String =
      *  {
      *    "name" : "ChocPanda",
      *    "age" : 26
      *  }
      *
      *  scala> entity.asJson.spaces2
      *   res3: String 
      *   {
      *     "name" : "ChocPanda",
      *     "age" : 26,
      *     "type" : "Human"
      *   }
      */
      
      
    
}
```

Iterating over the above resultant json is easier than iterating over the json produced by  

## Features left to add ##

- Configurability
    - Add support for customisation of the serialized object such as serpent cased field names
    - Different field names for the type hints
    - etc...
- Add support for default values and configurability as to whether they are serialised
- Add support for [refined](https://github.com/fthomas/refined) types / some other validation
- Compile for other scala versions including scala-native and scalaJS

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with
any pull requests, please state that the contribution is your original work and that you license
the work to the project under the project's open source license. Whether or not you state this
explicitly, by submitting any copyrighted material via pull request, email, or other means you
agree to license the material under the project's open source license and warrant that you have the
legal authority to do so.

## License ##

This code is open source software licensed under the
[Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0) license.
