package accode.inslick.data
import zio.Task

case class Query[+T](q: Task[T], name: String, expected: T)

object Query {
  def apply[T](q: Task[T], e: T)(implicit name: sourcecode.Name): Query[T] =
    Query(q, name.value, e)
}
