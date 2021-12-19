package accode.inslick.spec
import accode.inslick.{Syntax, rows, syntax, values}

case class DbDef(path: String, syntax: Syntax)

object DbDef {
  val h2         = DbDef("h2", syntax)
  val mysql      = DbDef("mysql", rows.syntax)
  val mariadb    = DbDef("mariadb", syntax)
  val postgres14 = DbDef("postgres14", rows.syntax)
  val postgres9  = DbDef("postgres9", syntax)
  val sqlite     = DbDef("sqlite", values.syntax)

  val all = List(
    h2,
    mysql,
    mariadb,
    postgres14,
    postgres9,
    sqlite
  )
}
