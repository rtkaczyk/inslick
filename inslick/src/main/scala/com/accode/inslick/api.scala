package com.accode.inslick
import slick.jdbc.{SQLActionBuilder, SetParameter}

import scala.language.experimental.macros
import scala.language.higherKinds

object api {
  implicit class SqlVInterpolator(val s: StringContext) extends AnyVal {
    def sqli(params: Any*): SQLActionBuilder = macro MacroSqli.impl
  }

  def setValues[C[U >: T] <: Iterable[U], T: SetParameter]: SetValuesParameter[C[T]] =
    SetValuesParameter((xs: C[T]) => xs)

  type SetValuesParameter[V] = com.accode.inslick.SetValuesParameter[V]
}
