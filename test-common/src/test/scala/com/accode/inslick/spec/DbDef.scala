package com.accode.inslick.spec
import com.accode.inslick.{API, api}

case class DbDef(path: String, api: API)

object DbDef {
  val h2         = DbDef("h2", api)
  val mysql      = DbDef("mysql", API.rows)
  val mariadb    = DbDef("mariadb", api)
  val postgres14 = DbDef("postgres14", API.rows)
  val postgres9  = DbDef("postgres9", api)
  val sqlite     = DbDef("sqlite", API.values)

  val all = List(
    h2,
    mysql,
    mariadb,
    postgres14,
    postgres9,
    sqlite
  )
}
