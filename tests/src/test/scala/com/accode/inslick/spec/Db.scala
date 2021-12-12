package com.accode.inslick.spec

import com.accode.inslick.{ApiDef, api, rows, values}

case class Db(path: String, api: ApiDef)

object Db {
  val h2       = Db("h2", api)
  val postgres = Db("postgres", rows.api)
  val mysql    = Db("mysql", api)
  val mariadb  = Db("mariadb", rows.api)
  val sqlite   = Db("sqlite", values.api)
}
