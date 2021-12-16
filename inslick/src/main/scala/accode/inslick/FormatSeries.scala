package accode.inslick

class FormatSeries(val series: String, val row: String, val parens: Boolean)

object FormatSeries {
  val Rows   = new FormatSeries("", "row", parens = true)
  val Values = new FormatSeries("values ", "", parens = true)
  val Insert = new FormatSeries("", "", parens = false)
}
