/*
 * Copyright (c) 2019 Miles Sabin
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

package shapeless3.deriving.internals

import scala.annotation.tailrec
import scala.compiletime.*
import scala.deriving.*

import shapeless3.deriving.*

private[shapeless3] abstract class ErasedInstances[K, FT] extends Serializable {
  def erasedMapK(f: Any => Any): ErasedInstances[K, ?]
  def erasedMap(x: Any)(f: (Any, Any) => Any): Any
  def erasedTraverse(x0: Any)(map: (Any, Any) => Any)(pure: Any => Any)(ap: (Any, Any) => Any)(f: (Any, Any) => Any): Any
}

private[shapeless3] abstract class ErasedProductInstances[K, FT] extends ErasedInstances[K, FT] {
  def erasedMapK(f: Any => Any): ErasedProductInstances[K, _]
  def erasedConstruct(f: Any => Any): Any
  def erasedUnfold(a: Any)(f: (Any, Any) => (Any, Option[Any])): (Any, Option[Any])
  def erasedMap(x0: Any)(f: (Any, Any) => Any): Any
  def erasedMap2(x0: Any, y0: Any)(f: (Any, Any, Any) => Any): Any
  def erasedFoldLeft(x0: Any)(a: Any)(f: (Any, Any, Any) => CompleteOr[Any]): Any
  def erasedFoldLeft2(x0: Any, y0: Any)(a: Any)(f: (Any, Any, Any, Any) => CompleteOr[Any]): Any
  def erasedFoldRight(x0: Any)(a: Any)(f: (Any, Any, Any) => CompleteOr[Any]): Any
  def erasedFoldRight2(x0: Any, y0: Any)(a: Any)(f: (Any, Any, Any, Any) => CompleteOr[Any]): Any
  def erasedProject(x0: Any)(p: Int)(f: (Any, Any) => Any): Any
}

private[shapeless3] final class ErasedProductInstances1[K, FT](val mirror: Mirror.Product, i0: () => Any) extends ErasedProductInstances[K, FT] {

  @deprecated("Preserved for bincompat reasons. DO NOT USE as it will lead to stack overflows when deriving instances for recursive types")
  def this(mirror: Mirror.Product, i0: Any) = this(mirror, () => i0)

  lazy val i = i0()

  inline def toProduct(x: Any): Product = x.asInstanceOf[Product]
  
  final def erasedMapK(f: Any => Any): ErasedProductInstances[K, ?] =
    new ErasedProductInstances1(mirror, f(i))

  final def erasedConstruct(f: Any => Any): Any =
    mirror.fromProduct(Tuple1(f(i)))

  final def erasedUnfold(a: Any)(f: (Any, Any) => (Any, Option[Any])): (Any, Option[Any]) = {
    val (acc0, e0) = f(a, i)
    e0 match {
      case Some(_) => (acc0, Some(mirror.fromProduct(e0)))
      case None => (acc0, None)
    }
  }

  final def erasedMap(x0: Any)(f: (Any, Any) => Any): Any =
    mirror.fromProduct(Tuple1(f(i, toProduct(x0).productElement(0))))

  final def erasedTraverse(x0: Any)(map: (Any, Any) => Any)(pure: Any => Any)(ap: (Any, Any) => Any)(f: (Any, Any) => Any) =
    map(f(i, toProduct(x0).productElement(0)), (x: Any) => mirror.fromProduct(Tuple1(x)))

  final def erasedMap2(x0: Any, y0: Any)(f: (Any, Any, Any) => Any): Any =
    mirror.fromProduct(Tuple1(f(i, toProduct(x0).productElement(0), toProduct(y0).productElement(0))))

  final def erasedFoldLeft(x0: Any)(a: Any)(f: (Any, Any, Any) => CompleteOr[Any]): Any = {
    f(a, i, toProduct(x0).productElement(0)) match {
      case Complete(r) => r
      case acc => acc
    }
  }

  final def erasedFoldLeft2(x0: Any, y0: Any)(a: Any)(f: (Any, Any, Any, Any) => CompleteOr[Any]): Any = {
    f(a, i, toProduct(x0).productElement(0), toProduct(y0).productElement(0)) match {
      case Complete(r) => r
      case acc => acc
    }
  }

  final def erasedFoldRight(x0: Any)(a: Any)(f: (Any, Any, Any) => CompleteOr[Any]): Any = {
    f(i, toProduct(x0).productElement(0), a) match {
      case Complete(r) => r
      case acc => acc
    }
  }

  final def erasedFoldRight2(x0: Any, y0: Any)(a: Any)(f: (Any, Any, Any, Any) => CompleteOr[Any]): Any = {
    f(i, toProduct(x0).productElement(0), toProduct(y0).productElement(0), a) match {
      case Complete(r) => r
      case acc => acc
    }
  }

  final def erasedProject(x0: Any)(p: Int)(f: (Any, Any) => Any): Any =
    f(i, toProduct(x0).productElement(0))
}

object ErasedProductInstances1 {
  def apply[K, FT](mirror: Mirror.Product, i: => Any): ErasedProductInstances1[K, FT] =
    new ErasedProductInstances1(mirror, () => i)
}

private[shapeless3] final class ErasedProductInstancesN[K, FT](val mirror: Mirror.Product, is0: () => Array[Any]) extends ErasedProductInstances[K, FT] {

  @deprecated("Preserved for bincompat reasons. DO NOT USE as it will lead to stack overflows when deriving instances for recursive types")
  def this(mirror: Mirror.Product, is0: Array[Any]) = this(mirror, () => is0)

  lazy val is: Array[Any] = is0()

  import ErasedProductInstances.ArrayProduct

  inline def toProduct(x: Any): Product = x.asInstanceOf[Product]

  final def erasedMapK(f: Any => Any): ErasedProductInstances[K, ?] =
    new ErasedProductInstancesN(mirror, is.map(f))

  final def erasedConstruct(f: Any => Any): Any = {
    val n = is.length
    if (n == 0) mirror.fromProduct(None)
    else {
      val arr = new Array[Any](n)
      var i = 0
      while(i < n) {
        arr(i) = f(is(i))
        i = i+1
      }
      mirror.fromProduct(new ArrayProduct(arr))
    }
  }

  final def erasedUnfold(a: Any)(f: (Any, Any) => (Any, Option[Any])): (Any, Option[Any]) = {
    val n = is.length
    if (n == 0) (a, Some(mirror.fromProduct(None)))
    else {
      val arr = new Array[Any](n)
      var acc = a
      var i = 0
      while(i < n) {
        val (acc0, e0) = f(acc, is(i))
        e0 match {
          case Some(e) =>
            acc = acc0
            arr(i) = e
          case None =>
            return (acc0, None)
        }
        i = i+1
      }
      (acc, Some(mirror.fromProduct(new ArrayProduct(arr))))
    }
  }

  final def erasedMap(x0: Any)(f: (Any, Any) => Any): Any = {
    val n = is.length
    if (n == 0) x0
    else {
      val x = toProduct(x0)
      val arr = new Array[Any](n)
      var i = 0
      while(i < n) {
        arr(i) = f(is(i), x.productElement(i))
        i = i+1
      }
      mirror.fromProduct(new ArrayProduct(arr))
    }
  }

  final def erasedTraverse(x0: Any)(map: (Any, Any) => Any)(pure: Any => Any)(ap: (Any, Any) => Any)(f: (Any, Any) => Any) = {
    val n = is.length

    def prepend(xs: List[Any])(x: Any) = x :: xs
    def fromList(xs: List[Any]) =
      val arr = new Array[Any](n)
      @tailrec def toProduct(xs: List[Any], i: Int): Product = xs match
        case x :: xs => arr(i) = x; toProduct(xs, i - 1)
        case Nil => new ArrayProduct(arr)
      mirror.fromProduct(toProduct(xs, n - 1))

    if (n == 0) pure(x0)
    else {
      val x = toProduct(x0)
      var acc = pure(Nil)
      var i = 0
      while(i < n) {
        acc = ap(map(acc, prepend), f(is(i), x.productElement(i)))
        i = i+1
      }
      map(acc, fromList)
    }
  }

  final def erasedMap2(x0: Any, y0: Any)(f: (Any, Any, Any) => Any): Any = {
    val n = is.length
    if (n == 0) x0
    else {
      val x = toProduct(x0)
      val y = toProduct(y0)
      val arr = new Array[Any](n)
      var i = 0
      while(i < n) {
        arr(i) = f(is(i), x.productElement(i), y.productElement(i))
        i = i+1
      }
      mirror.fromProduct(new ArrayProduct(arr))
    }
  }

  final def erasedFoldLeft(x0: Any)(i: Any)(f: (Any, Any, Any) => CompleteOr[Any]): Any = {
    val n = is.length
    if (n == 0) i
    else {
      val x = toProduct(x0)
      @tailrec
      def loop(i: Int, acc: Any): Any =
        if(i >= n) acc
        else
          f(acc, is(i), x.productElement(i)) match {
            case Complete(r) => r
            case acc =>
              loop(i+1, acc)
          }

      loop(0, i)
    }
  }

  final def erasedFoldRight(x0: Any)(i: Any)(f: (Any, Any, Any) => CompleteOr[Any]): Any = {
    val n = is.length
    if (n == 0) i
    else {
      val x = toProduct(x0)
      @tailrec
      def loop(i: Int, acc: Any): Any =
        if(i < 0) acc
        else
          f(is(i), x.productElement(i), acc) match {
            case Complete(r) => r
            case acc =>
              loop(i-1, acc)
          }

      loop(n-1, i)
    }
  }

  final def erasedFoldLeft2(x0: Any, y0: Any)(i: Any)(f: (Any, Any, Any, Any) => CompleteOr[Any]): Any = {
    val n = is.length
    if (n == 0) i
    else {
      val x = toProduct(x0)
      val y = toProduct(y0)
      @tailrec
      def loop(i: Int, acc: Any): Any =
        if(i >= n) acc
        else
          f(acc, is(i), x.productElement(i), y.productElement(i)) match {
            case Complete(r) => r
            case acc =>
              loop(i+1, acc)
          }

      loop(0, i)
    }
  }

  final def erasedFoldRight2(x0: Any, y0: Any)(i: Any)(f: (Any, Any, Any, Any) => CompleteOr[Any]): Any = {
    val n = is.length
    if (n == 0) i
    else {
      val x = toProduct(x0)
      val y = toProduct(y0)
      @tailrec
      def loop(i: Int, acc: Any): Any =
        if(i < 0) acc
        else
          f(is(i), x.productElement(i), y.productElement(i), acc) match {
            case Complete(r) => r
            case acc =>
              loop(i-1, acc)
          }

      loop(n-1, i)
    }
  }

  final def erasedProject(x0: Any)(p: Int)(f: (Any, Any) => Any): Any =
    f(is(p), toProduct(x0).productElement(p))
}

object ErasedProductInstancesN {
  def apply[K, FT](mirror: Mirror.Product, is: => Array[Any]): ErasedProductInstancesN[K, FT] =
    new ErasedProductInstancesN(mirror, () => is)
}

private[shapeless3] object ErasedProductInstances {
  class ArrayProduct(val elems: Array[Any]) extends Product {
    def canEqual(that: Any): Boolean = true
    def productElement(n: Int) = elems(n)
    def productArity = elems.length
    override def productIterator: Iterator[Any] = elems.iterator
  }

  inline def summonOne[T] = inline erasedValue[T] match {
    case _: Tuple1[a] => summonInline[a]
  }

  val emptyArray: Array[Any] = new Array(0)

  inline def apply[K, FT, E <: Tuple](mirror: Mirror.Product): ErasedProductInstances[K, FT] =
    inline erasedValue[Tuple.Size[E]] match {
      case 0 => ErasedProductInstancesN[K, FT](mirror, emptyArray)
      case 1 => ErasedProductInstances1[K, FT](mirror, summonOne[E])
      case _ => ErasedProductInstancesN[K, FT](mirror, summonAsArray[E])
    }
}

private[shapeless3] final class ErasedCoproductInstances[K, FT](mirror: Mirror.Sum, is0: => Array[Any]) extends ErasedInstances[K, FT] {
  lazy val is = is0

  final def erasedMapK(f: Any => Any): ErasedCoproductInstances[K, ?] =
    new ErasedCoproductInstances(mirror, is.map(f))

  final def ordinal(x: Any): Any = is(mirror.ordinal(x.asInstanceOf))

  final def erasedMap(x: Any)(f: (Any, Any) => Any): Any = {
    val i = ordinal(x)
    f(i, x)
  }

  final def erasedProject(p: Int)(i: Any)(f: (Any, Any) => (Any, Option[Any])): (Any, Option[Any]) =
    f(i, is(p))

  final def erasedInject(p: Int)(f: Any => Any): Any =
    f(is(p))

  final def erasedFold(x: Any)(f: (Any, Any) => Any): Any = {
    val i = ordinal(x)
    f(i, x)
  }

  final def erasedTraverse(x: Any)(map: (Any, Any) => Any)(pure: Any => Any)(ap: (Any, Any) => Any)(f: (Any, Any) => Any): Any = {
    val i = ordinal(x)
    f(i, x)
  }

  final def erasedFold2(x: Any, y: Any)(a: => Any)(f: (Any, Any, Any) => Any): Any = {
    val i = mirror.ordinal(x.asInstanceOf)
    val j = mirror.ordinal(y.asInstanceOf)
    if(i == j) f(is(i), x, y)
    else a
  }

  final def erasedFold2f(x: Any, y: Any)(g: (Int, Int) => Any)(f: (Any, Any, Any) => Any): Any = {
    val i = mirror.ordinal(x.asInstanceOf)
    val j = mirror.ordinal(y.asInstanceOf)
    if(i == j) f(is(i), x, y)
    else g(i, j)
  }
}

private[shapeless3] object ErasedCoproductInstances {
  inline def apply[K, FT, E <: Tuple](mirror: Mirror.Sum) : ErasedCoproductInstances[K, FT] =
    new ErasedCoproductInstances[K, FT](mirror, summonAsArray[E])
}
