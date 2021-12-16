package accode.inslick

class QueryManipulation[C] private (xs: C, fs: FormatSeries, p: IterParam[C]) {
  private val size = p.size(xs)
  private val dim  = p.dim
  private val fp   = p.fp.formats
  require(size > 0, "Cannot construct rows for empty collection")
  require(dim > 0, "Row dimension must be positive")

  private def parenBefore = "(".filter(_ => fs.parens)
  private def parenAfter  = ")".filter(_ => fs.parens)

  private lazy val b4Series         = s"$parenBefore${fs.series + fs.row}("
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
          .mkString(parenBefore + fs.series, ", ", parenAfter)
          .drop(b4Series.length + b4FirstP.length + 1)
    }
}

object QueryManipulation {
  def apply[C](fs: FormatSeries, xs: C)(implicit p: IterParam[C]): QueryManipulation[C] =
    new QueryManipulation[C](xs, fs, p)

  def apply[C](xs: C)(implicit fs: FormatSeries, p: IterParam[C]): QueryManipulation[C] =
    new QueryManipulation[C](xs, fs, p)
}
