package com.accode.inslick.data
import com.accode.inslick.API
import com.accode.inslick.slick.SqlRunner
import slick.jdbc.SetParameter

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}

class Queries(api: API, sqlRunner: SqlRunner) {
  import api._
  import sqlRunner._

  private implicit val localDateSP: SetParameter[LocalDate] =
    SetParameter((d, pp) => pp.setDate(Date.valueOf(d)))
  private implicit val localDateTimeSP: SetParameter[LocalDateTime] =
    SetParameter((d, pp) => pp.setTimestamp(Timestamp.valueOf(d)))

  val selectAll = {
    val v = Animal.all.map(_.tuple)
    val q = sqli"""
      select count(*) from animal a
      where (a.id, a.name, a.kind, a.legs, a.has_tail, created, updated)
        in *$v""".count
    Query(q, v.size)
  }

  val selectName = {
    val v = Animal.all.map(_.name)
    val q = sqli"""
      select count(*) from animal
      where name in *$v""".count
    Query(q, v.size)
  }

  val all = List(selectAll, selectName)
}
