package accode

package object inslick {

  object rows {
    object syntax extends Syntax(FormatSeries.Rows)
  }

  object values {
    object syntax extends Syntax(FormatSeries.Values)
  }

  val syntax: Syntax = rows.syntax
}
