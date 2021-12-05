package com.accode.inslick
import scala.reflect.macros.blackbox

trait TreeBuilder[C <: blackbox.Context] {
  val c: C
  import c.universe._

  object LStr {
    def apply(s: String): Tree = Literal(Constant(s))
    def unapply(t: Tree): Option[String] = t match {
      case Literal(Constant(s: String)) => Some(s)
      case _                            => None
    }
  }

  protected def asTree(tpe: Type): Tree = tpe match {
    case TypeRef(_, t, Nil) =>
      mkType(t)

    case TypeRef(_, t, trs) =>
      tq"${mkType(t)}[..${trs.map(asTree)}]"

    case _ =>
      EmptyTree
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
