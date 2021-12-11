package com.accode.inslick

class QueryManipulation[C: SetValuesParameter](xs: C) {
  private val ev   = implicitly[SetValuesParameter[C]]
  private val size = ev.size(xs)
  private val dim  = ev.dim

  require(size > 0, "Cannot construct rows for empty collection")
  require(dim > 0, "Row dimension must be positive")

  def before: String =
    dim match {
      case 1 => "("
      case _ => "(row("
    }

  def after: String =
    dim match {
      case 1 =>
        row(size).drop("(?".length)

      case _ =>
        List.fill(size)("row" + row(dim))
          .mkString("(", ", ", ")")
          .drop("(row(?".length)
    }

  private def row(size: Int): String =
    "(?" + ", ?" * (size - 1) + ")"
}
