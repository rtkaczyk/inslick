package accode.inslick
import accode.inslick.API.SqliInterpolator
import slick.jdbc.{SQLActionBuilder, SetParameter}

import scala.language.experimental.macros
import scala.language.{higherKinds, implicitConversions}

abstract class API(fs: FormatSeries) {
  implicit def sqliInterpolator(s: StringContext): SqliInterpolator =
    new SqliInterpolator(s)

  implicit val formatSeries: FormatSeries = fs

  type InParameter[C] = IterParam[C]

  def iterParam[C[U >: T] <: Iterable[U], T: SetParameter]: InParameter[C[T]] =
    IterParam((xs: C[T]) => xs)
}

object API {
  class SqliInterpolator(val s: StringContext) extends AnyVal {
    def sqli(params: Any*): SQLActionBuilder = macro MacroSqli.impl
  }

  object rows extends API(FormatSeries.Rows)

  object values extends API(FormatSeries.Values)
}
