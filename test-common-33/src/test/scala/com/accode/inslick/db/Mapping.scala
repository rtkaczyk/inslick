package com.accode.inslick.db
import slick.jdbc.JdbcProfile

import java.sql
import java.time.{LocalDate, LocalDateTime}

trait Mapping {
  val profile: JdbcProfile
  import profile.api._

  implicit def localDateMapping = MappedColumnType.base[LocalDate, sql.Date](
    sql.Date.valueOf,
    _.toLocalDate
  )

  implicit def localDateTimeMapping = MappedColumnType.base[LocalDateTime, sql.Timestamp](
    sql.Timestamp.valueOf,
    _.toLocalDateTime
  )
}
