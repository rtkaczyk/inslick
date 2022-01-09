package accode.inslick
import accode.inslick.MacroSqli._
import slick.jdbc.{ActionBasedSQLInterpolation, SetParameter}

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

object MacroSqli {
  def impl(c: blackbox.Context)(params: c.Tree*): c.Tree =
    new MacroSqli[c.type](c)(params).sqli

  private sealed abstract class Expand(val c: Char) {
    def s = c.toString
  }
  private case object Expand  extends Expand('*')
  private case object ExpandR extends Expand('r')
  private case object ExpandV extends Expand('v')
  private case object ExpandI extends Expand('i')
}

class MacroSqli[C <: blackbox.Context](val c: C)(inputParams: Seq[C#Tree])
    extends TreeUtils[C] {
  import c.universe._

  private case class ExpandedQuery(
      parts: List[LStr],
      params: List[Tree],
      aux: List[ValDef],
      types: List[Type]
  ) {
    def reverse = ExpandedQuery(parts.reverse, params.reverse, aux.reverse, types)
  }

  def sqli: Tree = {
    val expanded = expandQuery(
      extractStringParts,
      inputParams.toList.asInstanceOf[List[Tree]]
    )
    constructSqlInterpolation(expanded)
  }

  private def extractStringParts: List[LStr] =
    c.macroApplication match {
      case q"$_($_(..${parts: List[String]})).$_(..$_)" =>
        parts.map(LStr(_))
      case _ =>
        abort("Invalid sqli macro usage (hint: StringContext parts must be String literals")
    }

  @tailrec
  private def expandQuery(
      parts: List[LStr],
      params: List[Tree],
      expQ: ExpandedQuery = ExpandedQuery(Nil, Nil, Nil, Nil)
  ): ExpandedQuery =
    (parts, params) match {
      case (part :: remParts, param :: remParams) =>
        val (escaped, doExpand) = escape(part)
        val nextExpQ = doExpand.fold {
          expQ.copy(escaped :: expQ.parts, param :: expQ.params)
        } { expand =>
          expandParam(escaped, param, expQ, expand)
        }
        expandQuery(remParts, remParams, nextExpQ)

      case (part :: Nil, Nil) =>
        expQ.copy(part :: expQ.parts).reverse

      case (_, _) =>
        abort("Invalid StringContext (parts.size != params.size + 1")
    }

  private def expandParam(
      part: LStr,
      param: Tree,
      expQ: ExpandedQuery,
      expand: Expand
  ): ExpandedQuery = {
    val pType = param.tpe

    val svpTree =
      if (expQ.types.exists(_ =:= pType) || implicitAvailable(tpIterParam, pType))
        Nil
      else
        List(defineImplicit(pType))

    val (qm, qmTree) = defineQM(param, expand)
    val aux          = qmTree :: svpTree

    val left   = LStr(part.const + "#") -> q"$qm.before"
    val middle = LStr("")               -> param
    val right  = LStr("#")              -> q"$qm.after"

    val (newParts, newParams) = List(right, middle, left).unzip

    ExpandedQuery(
      newParts ::: expQ.parts,
      newParams ::: expQ.params,
      aux ::: expQ.aux,
      svpTree.map(_ => pType) ::: expQ.types
    )
  }

  private def defineImplicit(param: Type): ValDef = {
    val name    = TermName(c.freshName("__iterParam"))
    val mapping = elemMapping(param)
    q"implicit val $name: $tpIterParam[$param] = $tmIterParam($tmIdentity, $mapping)"
      .asInstanceOf[ValDef]
  }

  private def elemMapping(param: Type): Tree =
    param.typeArgs match {
      case List(ccType) if isCaseClass(ccType) && !implicitAvailable(tpSetParameter, ccType) =>
        caseClassMapping(ccType)
      case _ =>
        tmIdentity
    }

  private def isCaseClass(tpe: Type): Boolean =
    tpe.typeSymbol.isClass && tpe.typeSymbol.asClass.isCaseClass

  private def caseClassMapping(cc: Type): Tree = {
    val accessors = cc.decls.collect {
      case m: MethodSymbol if m.isCaseAccessor => q"c.$m: ${m.returnType}"
    }
    q"(c: $cc) => (..$accessors)"
  }

  private def implicitAvailable(typeclass: Tree, param: Type): Boolean = {
    val ip = tq"$typeclass[$param]"
    val tc = c.typecheck(q"$tmImplicitly[$ip]", silent = true)
    !tc.equalsStructure(EmptyTree)
  }

  private def defineQM(param: Tree, expand: Expand): (TermName, ValDef) = {
    val name = TermName(c.freshName("__qm"))
    val qm = expand match {
      case Expand  => q"$tmQueryManipulation($param)"
      case ExpandR => q"$tmQueryManipulation($tmFormatRows, $param)"
      case ExpandV => q"$tmQueryManipulation($tmFormatValues, $param)"
      case ExpandI => q"$tmQueryManipulation($tmFormatInsert, $param)"
    }
    name -> q"val $name = $qm".asInstanceOf[ValDef]
  }

  private def escape(part: LStr): (LStr, Option[Expand]) = {
    val expandCandidate =
      List(ExpandR, ExpandV, ExpandI)
        .find(e => part.const.lastOption.contains(e.c))
        .getOrElse(Expand)

    val toEscape = if (expandCandidate == Expand) part.const else part.const.dropRight(1)
    val escLen   = toEscape.reverse.takeWhile(_ == Expand.c).length
    val doExpand = escLen % 2 == 1
    val escaped  = toEscape.dropRight(escLen) + Expand.s * (escLen / 2)

    (LStr(escaped), if (doExpand) Some(expandCandidate) else None)
  }

  private def constructSqlInterpolation(expQ: ExpandedQuery): Tree = {
    val sc  = q"$tmStringContext(..${expQ.parts.map(_.tree)})"
    val sql = q"new $tpSlickInterpolation($sc).sql(..${expQ.params})"
    q"{..${expQ.aux};$sql}"
  }

  private def info(msg: String): Unit =
    c.info(c.enclosingPosition, msg, force = true)

  private def abort(msg: String): Nothing =
    c.abort(c.enclosingPosition, msg)

  private val tpSlickInterpolation = mkType[ActionBasedSQLInterpolation]
  private val tpIterParam          = mkType[IterParam[_]]
  private val tmIterParam          = mkTerm[IterParam[_]]
  private val tpSetParameter       = mkType[SetParameter[_]]
  private val tmStringContext      = mkTerm[StringContext]
  private val tmImplicitly         = mkTerm("scala.Predef.implicitly")
  private val tmIdentity           = mkTerm("scala.Predef.identity")
  private val tmQueryManipulation  = mkTerm[QueryManipulation[_]]
  private val tmFormatRows         = mkTerm[FormatSeries.Rows.type]
  private val tmFormatValues       = mkTerm[FormatSeries.Values.type]
  private val tmFormatInsert       = mkTerm[FormatSeries.Insert.type]
}
