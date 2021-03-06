package accode.inslick

import scala.reflect.macros.blackbox

trait TreeUtils[C <: blackbox.Context] {
  val c: C
  import c.universe._

  class LStr private (val tree: Tree) {
    def const: String = LStr.unapply(tree).get

    override def toString: String = s""""$const""""
  }

  object LStr {
    def apply(s: String): LStr = new LStr(Literal(Constant(s)))
    def unapply(t: Tree): Option[String] = t match {
      case Literal(Constant(s: String)) => Some(s)
      case _                            => None
    }
  }

  protected def mkType[A: TypeTag]: Tree =
    mkType(typeOf[A].typeSymbol)

  protected def mkType(s: Symbol): Tree =
    select(TypeName(_))(s.fullName)

  protected def mkTerm[A: TypeTag]: Tree =
    mkTerm(typeOf[A].typeSymbol)

  protected def mkTerm(s: Symbol): Tree =
    mkTerm(s.fullName)

  protected def mkTerm(s: String): Tree =
    select(TermName(_))(s)

  private def select(fName: String => Name)(s: String): Tree = {
    val tokens = s.split('.').toList.filter(_ != termNames.ROOTPKG.decodedName.toString)
    val path   = tokens.init.map(TermName(_))
    val name   = fName(tokens.last)
    val root   = Ident(termNames.ROOTPKG)
    (path :+ name).foldLeft[Tree](root)(Select(_, _))
  }
}
