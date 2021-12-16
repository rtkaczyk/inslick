package accode.inslick.data

import accode.inslick.slickv.Slick
import accode.inslick.spec.DbDef
import slick.jdbc.SetParameter

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}

class Queries(db: DbDef, slick: Slick) {
  import db.api._
  import slick._

  private implicit val localDateSP: SetParameter[LocalDate] =
    SetParameter((d, pp) => pp.setDate(Date.valueOf(d)))
  private implicit val localDateTimeSP: SetParameter[LocalDateTime] =
    SetParameter((d, pp) => pp.setTimestamp(Timestamp.valueOf(d)))

  val reinsertAnimalAll = {
    val v  = Animal.all.map(_.tuple)
    val q1 = sqli"delete from animal where ${1}**${1} = 1".count
    val q2 = sqli"insert into animal values *i$v".count
    Query(q1 *> q2, v.size)
  }

  val selectAll = {
    val v = Animal.all.map(_.tuple)
    val q = sqli"""
      select count(*) from animal
      where (id, name, kind, alias, legs, has_tail, created, updated)
        in *$v or alias is null""".count
    Query(q, v.size)
  }

  val selectName = {
    val v = Animal.all.map(_.name)
    val q = sqli"""
      select count(*) from animal
      where name in *$v""".count
    Query(q, v.size)
  }

  val selectWithOptional = {
    val v = Animal.all.map(a => (a.name, a.alias))
    val q = sqli"""
      select count(*) from animal
      where (name, alias) in *$v""".count
    Query(q, Animal.all.count(_.alias.isDefined))
  }

  val selectCastMultiple = {
    val v = Animal.all
      .map(a => (a.created.toString, a.name, a.updated.toString, if (a.hasTail) 1 else 0))

    implicit val ip = {
      val (castBool, castDateTime, castDate) = db.path match {
        case "mysql" | "mariadb" =>
          ("cast(? as signed)", "cast(? as datetime)", "cast(? as date)")
        case "sqlite" =>
          (
            "?",
            "cast(strftime('%s', ?, 'utc') as int) * 1000",
            "cast(strftime('%s', ?, 'utc') as int) * 1000"
          )
        case _ =>
          ("cast(? as boolean)", "cast(? as timestamp)", "cast(? as date)")
      }

      iterParam[List, (String, String, String, Int)].formatParams(
        1 -> castDate,
        3 -> castDateTime,
        4 -> castBool
      )
    }

    val q = sqli"""
      select count(*) from animal
      where (created, name, updated, has_tail) in *$v""".count
    Query(q, v.size)
  }

  val selectCastMiddleOnly = {
    val v = Animal.all.map(a => (a.id, a.created.toString, a.name)).toSet

    implicit val ip = {
      val castDate = db.path match {
        case "sqlite" => "cast(strftime('%s', ?, 'utc') as int) * 1000"
        case _        => "cast(? as date)"
      }
      iterParam[Set, (Int, String, String)].formatParams(2 -> castDate)
    }

    val q = sqli"select count(*) from animal where (id, created, name) in *$v".count
    Query(q, v.size)
  }

  val selectCastSingle = {
    val v: Iterable[String] = Animal.all.map(_.created.toString)

    implicit val ip = {
      val castDate = db.path match {
        case "sqlite" => "cast(strftime('%s', ?, 'utc') as int) * 1000"
        case _        => "cast(? as date)"
      }
      iterParam[Iterable, String].formatParams(1 -> castDate)
    }

    val q = sqli"select count(*) from animal where created in *$v".count
    Query(q, v.size)
  }

  val all = List(
    reinsertAnimalAll,
    selectAll,
    selectName,
    selectWithOptional,
    selectCastMultiple,
    selectCastMiddleOnly,
    selectCastSingle
  )
}
