package com.accode.inslick.data
import slick.dbio.DBIO

case class Query(query: DBIO[Int], name: String, excpectedN: Int)

object Query {
  def apply(q: DBIO[Int], n: Int)(implicit name: sourcecode.Name): Query =
    Query(q, name.value, n)
}
