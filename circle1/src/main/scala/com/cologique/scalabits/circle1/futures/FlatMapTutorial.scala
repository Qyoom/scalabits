package com.cologique.scalabits.circle1.futures

import akka.dispatch.Future
import akka.actor.ActorSystem

object FlatMapTutorial extends App {

  /*
   * Many monads may be considered as a data structure on a "substrate" type
   * with slots of that type, basically, generalized collections. 
   * We'll call these "slotted monads". This is my (Azad Bolour) terminology for 
   * pedagogical purposes only.
   *
   * For example, List[String] is a slotted monad having an arbitrary number of 
   * substrate slots of type String. And Option[Int] is a slotted monad having at 
   * most one slot of type Int.
   */

  /* 
   * In the context of a slotted monad, a mapping function is a function from 
   * one substrate to another:
   * 
   *     mapper: Substrate1 => Substrate2. 
   * 
   * The two substrates may be the same or different types. The mapping function is 
   * applied to each slot of a slotted monad to produce another slotted monad
   * of the same structure as the original monad, where each slot value is replaced
   * by its mapper-mapped value.
   */

  /**
   * A mapper: Int => Int
   */
  def triple(i: Int) = 3 * i

  /**
   * A monad of substrate type Int.
   */
  val option1 = Option(1)

  /**
   * Application of the mapper.
   */
  val tripleOption1 = option1 map triple
  println(tripleOption1)

  /*
   * A special case of a slotted monad is one that has no slots.
   * In this case, there are no values to apply the function to, but 
   * the structure of the monad, that is, emptiness, is preserved.
   */
  val option2: Option[Int] = None
  val tripleOption2 = option2 map triple
  println(tripleOption2)
  println(tripleOption2.isEmpty)

  /**
   * Another mapper: length: String => Int
   */
  val option3 = Option("xyz")
  val length3 = option3 map { _.length }
  println(length3)

  val option4: Option[String] = None
  val length4 = option4 map { _.length }
  println(length4)

  /**
   * A mapper applied to a list monad.
   */
  val list1 = List(1, 2, 3, 4)
  val tripleList1 = list1 map triple
  println(tripleList1)

  /*
   * Suppose now that for a mapper: Substrate1 => Substrate2, the range, Substrate2, 
   * is itself a slotted monad. The result of applying the map to a slotted monad
   * is then a nested monad.
   */

  /**
   * A monad-producing mapper: sqrt: Double => Option(Double)
   */
  def sqrt(n: Int): Option[Double] = {
    val d: Double = n
    val root = math.sqrt(d)
    if (root.isNaN()) None else Some(root)
  }

  /**
   * A monad-producing mapper, mapped on a monad, produces a nested monad.
   */
  val sq1 = option1 map sqrt
  println(sq1)
  val sqNone = option2 map sqrt
  println(sqNone)

  /**
   * Another monad-producing mapper: Int => List[Double] -
   * compute all square roots of an integer and return them in a list.
   */
  def allSqrts(n: Int): List[Double] = {
    val d: Double = n
    val root = math.sqrt(d)
    if (root.isNaN) List.empty else List(root, -root)
  }

  /**
   * Nested list for square roots of a set of integers.
   */
  val sqList1 = List(1, -1, 4, -4, 100, -100) map allSqrts
  println(sqList1)

  /**
   * Another list-monad-producing mapper: String => List[String]
   */
  def prefixes(s: String): List[String] = (1 to s.length) map { s.substring(0, _) } toList

  val words = List("the", "quick", "brown", "fox")
  val wordPrefixes = words map prefixes
  println(wordPrefixes)

  /*
   * So far so good.
   * 
   * Now there are a variety of applications where we need to be able to compose
   * monad-producing mappers. 
   * 
   * Consider the functions:
   * 
   * 	find: Criteria => List[Person]
   * 	children: Person => List[Person]
   * 
   * These are list monad producers and the output of the first is compatible with 
   * the input of the second. The natural composition of these monoad producers is 
   * function that takes a criteria and returns the children of all persons
   * satisfying that criteria. The composition is similar to a relational join.
   * 
   * This is where Scala's flatMap comes in (in monad terminology, this is a "bind"
   * operation).
   * 
   * flatMap flattens the result of applying a monad-producing mapper to a monad.
   * So the result is no longer a nested monad but an ordinary monad. And this makes
   * it possible to pipe the result into another monad-producing mapper in a 
   * composition pipeline.
   */

  // To keep things simple, et's continue with our simple integer and string examples above.

  /**
   * flatMap produces a normal monad - not a nested one.
   */
  val sqrt1 = option1 flatMap sqrt
  println(sqrt1)

  /**
   * flatMap correctly propagates the None option.
   */
  val sqrtNone = option2 flatMap sqrt
  println(sqrtNone)

  /**
   * flatMap produces the None option when the monad producer returns None.
   */
  val sqrtMinus1 = Option(-1) flatMap sqrt
  println(sqrtMinus1)

  /*
   * Another monad-producing mapper: String => Option[String]
   */
  def lookup(word: String): Option[String] = {
    words.find(_ == word)
  }

  val foxOption = Option("fox")
  val nestedFoxOption = foxOption map lookup
  println(nestedFoxOption)

  val flatFoxOption = foxOption flatMap lookup
  println(flatFoxOption)

  /*
   * With this machinery, we can now set up a compositional pipeline of monad producers.
   */

  /*
   * Getting back to the prefixes list-monad-producing function.
   */

  // Tiny pipeline.

  val flatWordPrefixes = words flatMap prefixes
  println(flatWordPrefixes)

  // Two-element pipeline.

  val pipeResult = words flatMap prefixes flatMap { (s: String) => List(s, s + s) }
  println(pipeResult)

  /*
   * We could also mix in maps in such a pipeline. A map takes a monad and produces
   * another, so it can be mixed in such a pipeline as long as its substrate 
   * types match what is input into it and what it outputs into.
   */

  val pipeResult2 = words flatMap prefixes map { _.length }
  println(pipeResult2)

  /**
   * An integer monad producer.
   */
  def plusOrMinus(i: Int) = List(i, -i)

  val pipeResult3 = words flatMap prefixes map { _.length } flatMap plusOrMinus
  println(pipeResult3)

  /*
  * The Scala for comprehension is syntactic sugar for a combination of 
  * flatMap and map.
  * 
  * We'll first incrementally refactor our tiny prefixes pipeline to 
  * look more like a for comprehension.
  */

  // First, the tiny pipeline.

  val flatPrefixes = words flatMap prefixes
  println(flatPrefixes)

  // Next nest flatMap's monad-producing function.

  val flatPrefixes1 = words flatMap { word => prefixes(word) }
  println(flatPrefixes1 == flatPrefixes)

  // Next add a final nested mapper.
  // In this case the final mapper is the identity function, and, of course, superfluous.

  val flatPrefixes2 = words flatMap { word => prefixes(word) map { prefix => prefix } }
  println(flatPrefixes2 == flatPrefixes)

  /*
   * This final structure is isomorphic to a for comprehension. In fact, the meaning
   * of a for comprehension is defined as such a nested pipeline.
   */
  val forComprehensionPrefixes =
    for (
      word <- words;
      prefix <- prefixes(word)
    ) yield (prefix)

  println(forComprehensionPrefixes)

  println(forComprehensionPrefixes == flatPrefixes)

  /*
   * Our original pipelines were not nested. They looked similar to unix pipes.
   * The for comprehension pipeline, on the other hand, is nested. This allows the 
   * values traversed by earlier flatMaps to be available to the later flatMaps,
   * and to the final mapper (in closures). So thanks to closure, a nested pipeline
   * affords us significantly more expressive power than a "flat" pipeline.
   * 
   * Note that the final mapper is the argument to yield.
   */

  // Nested pipeline using closed variables from outer scopes.

  val wordPrefixPairs = words flatMap { word => prefixes(word) map { prefix => (word, prefix) } }
  println(wordPrefixPairs)

  // Here is another example where nesting comes in handy.

  val opt16 = Option(16)

  // This does not have to be nested.
  println(opt16 flatMap { (number: Int) => sqrt(number) })

  // But this does have to be nested.
  val fourthRt = opt16 flatMap {
    (number: Int) =>
      sqrt(number) flatMap
        { (root: Double) =>
          sqrt(root.toInt) map
            { (fourthRoot: Double) => ("4'th root", number, fourthRoot) }
        }
  }
  println(fourthRt)

  /*
   * Futures are another example of a slotted monad. 
   * 
   * Just as the flatMap function for Option propagates the notion of emptiness
   * in a pipeline of option-producing function composition, the flatMap function of 
   * Future propagates the notion of incompleteness in a pipeline of future-producing 
   * function composition.
   */

  // Let's start with independent options first.

  val optionsTupleByFlatMap =
    Option(1) flatMap { (x1: Int) =>
      Option(2) flatMap { (x2: Int) =>
        Option(3) map { (x3: Int) =>
          (x1, x2, x3)
        }
      }
    }

  val optionsTupleByForComprehension = for (
    x1 <- Option(1);
    x2 <- Option(2);
    x3 <- Option(3)
  ) yield (x1, x2, x3)

  println(optionsTupleByFlatMap == optionsTupleByForComprehension)

  // Here are the isomorphic construction for independent futures.

  implicit val system = ActorSystem("future")
  def shutdownActorSystem = system.shutdown

  val futuresTupleByFlatMap =
    Future(1) flatMap { (x1: Int) =>
      Future(2) flatMap { (x2: Int) =>
        Future(3) map { (x3: Int) =>
          (x1, x2, x3)
        }
      }
    }

  futuresTupleByFlatMap onSuccess {
    case (x1, x2, x3) => println("sum via flatMap futures: " + (x1 + x2 + x3))
  }

  val futuresTupleByForComprehension = for (
    x1 <- Future(1);
    x2 <- Future(2);
    x3 <- Future(3)
  ) yield (x1, x2, x3)

  futuresTupleByForComprehension onSuccess {
    case (x1, x2, x3) => println("sum via for comprehension futures: " + (x1 + x2 + x3))
  }

  /*
   * Now let's actually compose futures by using future-producing functions.
   */

  /**
   * A future-producing function.
   */
  def slowDouble(num: Int): Future[Int] = {
    def double: Int = {
      Thread.sleep(200)
      return 2 * num
    }
    return Future(double)
  }

  val startTime1 = System.currentTimeMillis
  val eightTimesFutureByFlatMap = Future(5) flatMap slowDouble flatMap slowDouble flatMap slowDouble

  eightTimesFutureByFlatMap onSuccess {
    case q: Int => println("8 * 5 (by flatMap futures = " + q + " - computed in: " + (System.currentTimeMillis - startTime1) + " millis")
  }

  val startTime2 = System.currentTimeMillis

  val eightTimesFutureByForComprehension = for (
    x1 <- Future(5);
    x2 <- slowDouble(x1);
    x3 <- slowDouble(x2);
    x4 <- slowDouble(x3)
  ) yield (x4)

  eightTimesFutureByForComprehension onSuccess {
    case q: Int => println("8 * 5 (by for comprehension futures) = " + q + " - computed in: " + (System.currentTimeMillis - startTime2) + " millis")
  }

  shutdownActorSystem

}
