package com.accode.inslick

class FormatParams private (val formats: Map[Int, String])

object FormatParams {
  def apply(formats: Map[Int, String]): FormatParams = {
    require(
      formats.values.forall(_.count(_ == '?') == 1),
      "Each format must contain a single '?'"
    )
    new FormatParams(formats.withDefaultValue("?"))
  }

  def apply(formats: (Int, String)*): FormatParams =
    apply(formats.toMap)

  val default: FormatParams = apply()
}
