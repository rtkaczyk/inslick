package com.accode.slickvalues
import slick.jdbc.{SQLActionBuilder, SetParameter}

import scala.language.experimental.macros
import scala.language.higherKinds

object api {
  implicit class MVSqlInterpolator(val s: StringContext) extends AnyVal {
    def sqlv(params: Any*): SQLActionBuilder = macro MacroBuilder.impl
  }

  def setValues[C[U >: T] <: Iterable[U], T: SetParameter]: SetValuesParameter[C[T]] =
    SetValuesParameter((xs: C[T]) => xs)

  type SetValuesParameter[V] = com.accode.slickvalues.SetValuesParameter[V]
}
