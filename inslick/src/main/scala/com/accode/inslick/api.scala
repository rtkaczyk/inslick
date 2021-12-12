package com.accode.inslick
import com.accode.inslick.ApiDef.SqliInterpolator
import slick.jdbc.SQLActionBuilder

import scala.language.experimental.macros
import scala.language.implicitConversions

trait ApiDef extends ApiCommon {
  implicit def sqliInterpolator(s: StringContext): SqliInterpolator =
    new SqliInterpolator(s)
}

object ApiDef {
  class SqliInterpolator(val s: StringContext) extends AnyVal {
    def sqli(params: Any*): SQLActionBuilder = macro MacroSqli.impl
  }
}

object api extends ApiDef

object rows {
  object api extends ApiDef
}

object values {
  object api extends ApiDef {
    override implicit val formatSeries = FormatSeries.Values
  }
}
