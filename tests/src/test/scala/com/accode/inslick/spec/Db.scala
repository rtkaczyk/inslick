package com.accode.inslick.spec

import com.accode.inslick.{ApiDef, api, rows, values}

case class Db(path: String, api: ApiDef)

object Db {
  val h2         = Db("h2", api)
  val mysql      = Db("mysql", api)
  val mariadb    = Db("mariadb", rows.api)
  val postgres14 = Db("postgres14", rows.api)
  val postgres9  = Db("postgres9", api)
  val sqlite     = Db("sqlite", values.api)

  val all = List(
    h2,
    mysql,
    mariadb,
    postgres14,
    postgres9,
    sqlite
  )
}
