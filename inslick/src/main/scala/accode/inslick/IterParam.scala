package accode.inslick
import slick.jdbc.{PositionedParameters, SetParameter, SetTupleParameter}

import scala.language.higherKinds

abstract class IterParam[C](val size: C => Int, val dim: Int)
    extends SetParameter[C] {

  private val self = this

  val fp: FormatParams = FormatParams.default

  def formatParams(implicit _fp: FormatParams): IterParam[C] =
    new IterParam[C](size, dim) {
      override def apply(v: C, pp: PositionedParameters) = self.apply(v, pp)

      override val fp: FormatParams = _fp
    }

  def formatParams(formats: (Int, String)*): IterParam[C] =
    formatParams(FormatParams(formats: _*))
}

object IterParam {
  def apply[C[_ >: A], A: SetParameter](toIterable: C[A] => Iterable[A]): IterParam[C[A]] = {
    val dim: Int = implicitly[SetParameter[A]] match {
      case t: SetTupleParameter[_] => t.children.size
      case _                       => 1
    }

    val size: C[A] => Int = toIterable.andThen(_.size)

    new IterParam[C[A]](size, dim) {
      override def apply(c: C[A], pp: PositionedParameters): Unit =
        toIterable(c).foreach(x => implicitly[SetParameter[A]].apply(x, pp))
    }
  }

  def apply[C[B >: A] <: Iterable[B], A: SetParameter](): IterParam[C[A]] =
    apply((xs: C[A]) => xs)
}
