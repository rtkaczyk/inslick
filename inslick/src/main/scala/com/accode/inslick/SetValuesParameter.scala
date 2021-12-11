package com.accode.inslick

import slick.jdbc.{PositionedParameters, SetParameter, SetTupleParameter}

import scala.language.higherKinds

abstract class SetValuesParameter[C](val size: C => Int, val dim: Int) extends SetParameter[C]

object SetValuesParameter {
  def apply[C[_ >: A], A: SetParameter](toIterable: C[A] => Iterable[A])
      : SetValuesParameter[C[A]] = {
    val dim: Int = implicitly[SetParameter[A]] match {
      case t: SetTupleParameter[_] => t.children.size
      case _                       => 1
    }

    val size: C[A] => Int = toIterable.andThen(_.size)

    new SetValuesParameter[C[A]](size, dim) {
      override def apply(c: C[A], pp: PositionedParameters): Unit =
        toIterable(c).foreach(x => implicitly[SetParameter[A]].apply(x, pp))
    }
  }

  def apply[C[B >: A] <: Iterable[B], A: SetParameter]: SetValuesParameter[C[A]] =
    apply((xs: C[A]) => xs)
}
