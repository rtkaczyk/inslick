package accode.inslick

class FormatSeries(val series: String, val row: String, val parens: Boolean)

object FormatSeries {
  object Rows   extends FormatSeries("", "row", parens = true)
  object Values extends FormatSeries("values ", "", parens = true)
  object Insert extends FormatSeries("", "", parens = false)
}
