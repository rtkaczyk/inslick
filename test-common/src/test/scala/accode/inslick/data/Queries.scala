package accode.inslick.data

import accode.inslick.slickv.Slick
import accode.inslick.spec.DbDef
import slick.jdbc.SetParameter
import zio.ZIO

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}

class Queries(db: DbDef, slick: Slick) {
  import db.api._
  import slick._

  private implicit val localDateSP: SetParameter[LocalDate] =
    SetParameter((d, pp) => pp.setDate(Date.valueOf(d)))
  private implicit val localDateTimeSP: SetParameter[LocalDateTime] =
    SetParameter((d, pp) => pp.setTimestamp(Timestamp.valueOf(d)))

  val repopulate_animal = {
    val v  = Animal.all.map(_.tuple)
    val q1 = sqli"delete from animal".count
    val q2 = sqli"insert into animal values *i$v".count
    Query(q1 *> q2, v.size)
  }

  val select_animal_all_single = {
    val v = Animal.all.filter(_.alias.isDefined).take(1).map(_.noOpt)
    val q = sqli"""
      select count(*) from animal
      where (id, name, kind, legs, has_tail, created, updated) in *$v""".count
    Query(q, v.size)
  }

  val select_animal_all_all = {
    val v1 = Animal.all.filter(_.alias.isDefined).map(_.tuple)
    val v2 = Animal.all.filter(_.alias.isEmpty).map(_.noOpt)
    val q = sqli"""
      select count(*) from animal
      where (id, name, kind, alias, legs, has_tail, created, updated) in *$v1 or
        ((id, name, kind, legs, has_tail, created, updated) in *$v2 and alias is null)""".count
    Query(q, Animal.all.size)
  }

  val select_animal_name_single = {
    val v = Animal.all.take(1).map(_.name)
    val q = sqli"""
      select count(*) from animal
      where name in *$v""".count
    Query(q, v.size)
  }

  val select_animal_name_all = {
    val v = Animal.all.map(_.name)
    val q = sqli"""
      select count(*) from animal
      where name in *$v""".count
    Query(q, v.size)
  }

  val select_animal_optional = {
    val v = Animal.all.map(a => (a.name, a.alias))
    val q = sqli"""
      select count(*) from animal
      where (name, alias) in *$v""".count
    Query(q, Animal.all.count(_.alias.isDefined))
  }

  val select_animal_cast_multiple = {
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

  val select_animal_cast_middle_only = {
    val v = Animal.all.map(a => (a.id, a.created.toString, a.name)).toSet

    implicit val ip = {
      val castDate = db.path match {
        case "sqlite" => "cast(strftime('%s', ?, 'utc') as int) * 1000"
        case _        => "cast(? as date)"
      }
      iterParam[Set, (Long, String, String)].formatParams(2 -> castDate)
    }

    val q = sqli"select count(*) from animal where (id, created, name) in *$v".count
    Query(q, v.size)
  }

  val select_animal_cast_single = {
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

  val select_animal_multiple = {
    val v1 = Animal.all.slice(0, 2).map(a => (a.name, a.kind))
    val v2 = Animal.all.slice(2, 4).map(a => (a.name, a.kind))
    val v3 = Animal.all.drop(4).map(_.id)

    val q = sqli"""
      select count(*) from animal where
        (name, kind) in *$v1 or
        (name, kind) in *$v2 or
        (id) in *$v3 or
        name = ${"dog"}""".count

    Query(q, Animal.all.size)
  }

  val select_animal_multiple_implicit_provided = {
    val v1 = Animal.all.take(2).map(a => (a.name, a.kind))
    val v2 = Animal.all.drop(2).map(a => (a.created, a.updated))

    implicit val ip: IterParam[List[(String, String)]] = iterParam

    val q = sqli"""
      select count(*) from animal where
        (name, kind)       in *$v1 or
        (created, updated) in *$v2""".count

    Query(q, Animal.all.size)
  }

  val select_animal_multiple_type_alias = {
    type Id = Long
    val v1: List[Id]   = Animal.all.take(2).map(_.id)
    val v2: List[Long] = Animal.all.drop(2).map(_.id)

    val q = sqli"""
      select count(*) from animal where
        id in *$v1 or id in *$v2""".count

    Query(q, Animal.all.size)
  }

  val select_animal_explicit_syntax = {
    import accode.inslick.api._
    val v = Animal.all.map(_.id)
    val q = db.path match {
      case "sqlite" => sqli"select count(*) from animal where id in *v$v".count
      case _        => sqli"select count(*) from animal where id in *r$v".count
    }

    Query(q, v.size)
  }

  val select_animal_escape = {
    val v = Animal.all.map(_.id)
    val q = sqli"select count(*) from animal where id in *$v and ${1}**${1} = 1".count
    Query(q, v.size)
  }

  private val truncatePerson = sqli"delete from person".count.unit
  private val repopulatePerson = {
    val v = Person.all.map(_.tuple)
    val q = sqli"insert into person values *i$v".count
    truncatePerson *> q
  }

  val insert_person = {
    val q1 = sqli"insert into person (first_name) values *i${List("Anna")}".count
    val q2 = sqli"insert into person (first_name) values *i${List("Adam", "Anna")}".count
    val q3 = sqli"insert into person (last_name, shoe_size) values *i${List(("Ada", 37))}".count

    val q = truncatePerson *> ZIO.collectAll(List(q1, q2, q3))
    Query(q, List(1, 2, 1))
  }

  val update_person = {
    val v1 = Person.all.take(1).map(_.id)
    val v2 = Person.all.map(_.id)
    val v3 = Person.all.take(1).map(p => (p.firstName, p.lastName))
    val v4 = Person.all.map(p => (p.firstName, p.lastName))

    val q1 = sqli"update person set shoe_size = 0 where id in *$v1".count
    val q2 = sqli"update person set shoe_size = 0 where id in *$v2".count
    val q3 = sqli"update person set shoe_size = 0 where (first_name, last_name) in *$v3".count
    val q4 = sqli"update person set shoe_size = 0 where (first_name, last_name) in *$v4".count

    val n = Person.all.size
    val q = repopulatePerson *> ZIO.collectAll(List(q1, q2, q3, q4))
    Query(q, List(1, n, 1, n))
  }

  val delete_person = {
    val v1 = Person.all.take(1).map(_.id)
    val v2 = Person.all.map(_.id)
    val v3 = Person.all.take(1).map(p => (p.firstName, p.lastName))
    val v4 = Person.all.map(p => (p.firstName, p.lastName))

    val q1 = sqli"delete from person where id in *$v1".count
    val q2 = sqli"delete from person where id in *$v2".count
    val q3 = sqli"delete from person where (first_name, last_name) in *$v3".count
    val q4 = sqli"delete from person where (first_name, last_name) in *$v4".count

    val n = Person.all.size
    val q = ZIO.collectAll(List(q1, q2, q3, q4).map(repopulatePerson *> _))
    Query(q, List(1, n, 1, n))
  }

  lazy val all: List[Query[Any]] = {
    import scala.reflect.runtime.universe._

    val mirror = runtimeMirror(this.getClass.getClassLoader)
    typeOf[this.type].decls.collect {
      case m: MethodSymbol if m.returnType <:< typeOf[Query[Any]] =>
        mirror.reflect(this).reflectMethod(m)().asInstanceOf[Query[Any]]
    }.toList
  }
}
