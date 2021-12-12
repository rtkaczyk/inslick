package com.accode.inslick
import slick.jdbc.SetParameter

import scala.language.higherKinds

trait ApiCommon {
  type InParameter[C] = com.accode.inslick.InParameter[C]

  def inParam[C[U >: T] <: Iterable[U], T: SetParameter]: InParameter[C[T]] =
    InParameter((xs: C[T]) => xs)

  implicit val formatSeries: FormatSeries = FormatSeries.Rows
}
