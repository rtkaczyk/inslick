package com.accode.inslick.data

case class Query[T](query: T, name: String, excpectedN: Int)

object Query {
  def apply[T](q: T, n: Int)(implicit name: sourcecode.Name): Query[T] =
    Query(q, name.value, n)
}
