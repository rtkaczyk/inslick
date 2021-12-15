package com.accode.inslick

class QueryManipulation[C](xs: C)(implicit p: InParameter[C], fs: FormatSeries) {
  private val size = p.size(xs)
  private val dim  = p.dim
  private val fp   = p.fp.formats
  require(size > 0, "Cannot construct rows for empty collection")
  require(dim > 0, "Row dimension must be positive")

  private lazy val b4Series         = s"(${fs.series + fs.row}("
  private lazy val b4FirstP: String = fp(1).takeWhile(_ != '?')

  private def mkRow(size: Int): String =
    List.tabulate(size) { i =>
      val idx = if (dim == 1) 1 else i + 1
      fp(idx)
    }.mkString("(", ", ", ")")

  def before: String =
    dim match {
      case 1 => "(" + b4FirstP
      case _ => b4Series + b4FirstP
    }

  def after: String =
    dim match {
      case 1 =>
        mkRow(size).drop(s"($b4FirstP?".length)

      case _ =>
        val row = fs.row + mkRow(dim)
        List.fill(size)(row)
          .mkString("(" + fs.series, ", ", ")")
          .drop(b4Series.length + b4FirstP.length + 1)
    }
}
