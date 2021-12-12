package com.accode.inslick

import slick.jdbc.{PositionedParameters, SetParameter, SetTupleParameter}

import scala.language.higherKinds

abstract class InParameter[C](val size: C => Int, val dim: Int)
    extends SetParameter[C] {

  private val self = this

  val fp: FormatParams = FormatParams.default

  def formatParams(implicit _fp: FormatParams): InParameter[C] =
    new InParameter[C](size, dim) {
      override def apply(v: C, pp: PositionedParameters) = self.apply(v, pp)

      override val fp: FormatParams = _fp
    }

  def formatParams(formats: (Int, String)*): InParameter[C] =
    formatParams(FormatParams(formats: _*))
}

object InParameter {
  def apply[C[_ >: A], A: SetParameter](toIterable: C[A] => Iterable[A]): InParameter[C[A]] = {
    val dim: Int = implicitly[SetParameter[A]] match {
      case t: SetTupleParameter[_] => t.children.size
      case _                       => 1
    }

    val size: C[A] => Int = toIterable.andThen(_.size)

    new InParameter[C[A]](size, dim) {
      override def apply(c: C[A], pp: PositionedParameters): Unit =
        toIterable(c).foreach(x => implicitly[SetParameter[A]].apply(x, pp))
    }
  }

  def apply[C[B >: A] <: Iterable[B], A: SetParameter](): InParameter[C[A]] =
    apply((xs: C[A]) => xs)
}
