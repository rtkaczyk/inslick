package accode.inslick
import accode.inslick.Syntax.SqliInterpolator
import slick.jdbc.{SQLActionBuilder, SetParameter}

import scala.language.experimental.macros
import scala.language.{higherKinds, implicitConversions}

abstract class Syntax(fs: FormatSeries) {
  implicit def sqliInterpolator(s: StringContext): SqliInterpolator =
    new SqliInterpolator(s)

  implicit val formatSeries: FormatSeries = fs

  type IterParam[C] = accode.inslick.IterParam[C]

  def iterParam[C[U >: T] <: Iterable[U], T: SetParameter]: IterParam[C[T]] =
    IterParam(identity, identity)

  def iterParam[C[U >: T] <: Iterable[U], T: SetParameter](dim: Int): IterParam[C[T]] =
    IterParam(identity, identity, Some(dim))

  def iterParam[C[U >: T] <: Iterable[U], T, V: SetParameter](mapping: T => V): IterParam[C[T]] =
    IterParam(identity, mapping)
}

object Syntax {
  class SqliInterpolator(val s: StringContext) extends AnyVal {
    def sqli(params: Any*): SQLActionBuilder = macro MacroSqli.impl
  }
}
