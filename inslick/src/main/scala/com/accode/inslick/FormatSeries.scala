package com.accode.inslick

class FormatSeries(val series: String, val row: String)

object FormatSeries {
  val Rows   = new FormatSeries("", "row")
  val Values = new FormatSeries("values ", "")
}
