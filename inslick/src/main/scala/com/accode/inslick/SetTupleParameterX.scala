package com.accode.inslick

import slick.jdbc.{PositionedParameters, SetParameter}

abstract class SetTupleParameterX[A](val dim: Int) extends SetParameter[A]

object SetTupleParameterX {
  def apply[A](dim: Int)(f: (A, PositionedParameters) => Unit): SetTupleParameterX[A] = {
    require(dim > 0, "dim must be positive")
    new SetTupleParameterX[A](dim) {
      override def apply(a: A, pp: PositionedParameters): Unit = f(a, pp)
    }
  }

  trait Implicits {
    implicit def setTuple2[A: SetParameter, B: SetParameter]: SetTupleParameterX[(A, B)] =
      SetTupleParameterX[(A, B)](2) {
        case ((a, b), pp) =>
          implicitly[SetParameter[A]].apply(a, pp)
          implicitly[SetParameter[B]].apply(b, pp)
      }

    implicit def setTuple3[A: SetParameter, B: SetParameter, C: SetParameter]
        : SetTupleParameterX[(A, B, C)] =
      SetTupleParameterX[(A, B, C)](3) {
        case ((a, b, c), pp) =>
          implicitly[SetParameter[A]].apply(a, pp)
          implicitly[SetParameter[B]].apply(b, pp)
          implicitly[SetParameter[C]].apply(c, pp)
      }
  }
}
