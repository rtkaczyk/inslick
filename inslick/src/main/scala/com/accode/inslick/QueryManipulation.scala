package com.accode.inslick

object QueryManipulation {
  def apply[C: SetValuesParameter](xs: C): String = {
    val vsp = implicitly[SetValuesParameter[C]]
    apply(vsp.size(xs), vsp.dim)
  }

  def apply(size: Int, dim: Int): String = {
    require(size > 0, "Cannot construct VALUES for empty collection")
    require(dim > 0, "Row dimension must be positive")

    dim match {
      case 1 =>
        (", ?" * size).drop(3) + ")"

      case _ =>
        (s", (${(", ?" * dim).drop(2)})" * size).drop(4)
    }
  }
}
