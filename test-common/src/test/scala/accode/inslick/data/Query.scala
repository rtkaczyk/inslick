package accode.inslick.data
import zio.Task

case class Query(query: Task[Int], name: String, expected: Int)

object Query {
  def apply(q: Task[Int], n: Int)(implicit name: sourcecode.Name): Query =
    Query(q, name.value, n)
}
