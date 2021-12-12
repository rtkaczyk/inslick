package com.accode.inslick
import com.accode.inslick.spec.Db._
import com.accode.inslick.spec.SqliSpec

object H2Spec       extends SqliSpec(h2)
object PostgresSpec extends SqliSpec(postgres)
object MysqlSpec    extends SqliSpec(mysql)
object MariadbSpec  extends SqliSpec(mariadb)
object SqliteSpec   extends SqliSpec(sqlite)
