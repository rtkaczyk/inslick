package com.accode.inslick

import com.accode.inslick.MacroBuilder.Expand
import slick.jdbc.ActionBasedSQLInterpolation

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

object MacroBuilder {
  def impl(c: blackbox.Context)(params: c.Tree*): c.Tree =
    new MacroBuilder[c.type](c)(params).sqli

  val Expand = '*'
}

class MacroBuilder[C <: blackbox.Context](val c: C)(inputParams: Seq[C#Tree])
    extends TreeBuilder[C] {
  import c.universe._

  case class ExpandedQuery(
      parts: List[LStr],
      params: List[Tree],
      aux: List[ValDef],
      types: List[TypeTree]
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

  private def extractStringParts: List[LStr] = {
    val Apply(Select(Apply(_, List(Apply(_, strParts))), _), _) = c.macroApplication
    strParts.map {
      case l @ LStr(s) => LStr(s)
      case _           => abort("StringContext parts must be String literals")
    }
  }

  @tailrec
  private def expandQuery(
      parts: List[LStr],
      params: List[Tree],
      expQ: ExpandedQuery = ExpandedQuery(Nil, Nil, Nil, Nil)
  ): ExpandedQuery =
    (parts, params) match {
      case (part :: remParts, param :: remParams) =>
        val (escaped, expand) = escape(part)
        if (expand)
          expandQuery(remParts, remParams, expandParam(escaped, param, expQ))
        else
          expQ.copy(escaped :: expQ.parts, param :: expQ.params)

      case (part :: Nil, Nil) =>
        expQ.copy(part :: expQ.parts).reverse

      case (_, _) =>
        abort("Invalid StringContext")
    }

  private def expandParam(part: LStr, param: Tree, expQ: ExpandedQuery): ExpandedQuery = {
    val paramType = TypeTree(param.tpe)
    val svpTree =
      if (expQ.types.exists(_.equalsStructure(paramType)) || implicitAvailable(paramType)) Nil
      else List(defineImplicit(paramType))

    val (qm, qmTree) = defineQM(param)
    val aux          = qmTree :: svpTree

    val left   = LStr(part.const + "#") -> q"$qm.before"
    val middle = LStr("")               -> param
    val right  = LStr("#")              -> q"$qm.after"

    val (newParts, newParams) = List(right, middle, left).unzip

    ExpandedQuery(
      newParts ::: expQ.parts,
      newParams ::: expQ.params,
      aux ::: expQ.aux,
      svpTree.map(_ => paramType) ::: expQ.types
    )
  }

  private def defineImplicit(param: TypeTree): ValDef = {
    val name = TermName(c.freshName("__svp"))
    q"implicit val $name: $tpSetValuesParameter[$param] = $tmSetValuesParameter.apply"
      .asInstanceOf[ValDef]
  }

  private def implicitAvailable(param: TypeTree): Boolean = {
    val svp = tq"$tpSetValuesParameter[$param]"
    val tc  = c.typecheck(q"$tmImplicitly[$svp]", silent = true)
    !tc.equalsStructure(EmptyTree)
  }

  private def defineQM(param: Tree): (TermName, ValDef) = {
    val name = TermName(c.freshName("__qm"))
    name -> q"val $name = new $tpQueryManipulation($param)".asInstanceOf[ValDef]
  }

  private def escape(part: LStr): (LStr, Boolean) = {
    val c = part.const.reverse.takeWhile(_ == Expand).length
    val e = c % 2 == 1
    val s = part.const.dropRight(c) + Expand.toString * (c / 2)
    (LStr(s), e)
  }

  private def constructSqlInterpolation(expQ: ExpandedQuery): Tree = {
    val sc  = q"$tmStringContext(..${expQ.parts.map(_.tree)})"
    val sql = q"new $tpSlickInterpolation($sc).sql(..${expQ.params})"
    val res = q"{..${expQ.aux};$sql}"
    info(res.toString)
    res
  }

  private def info(msg: String): Unit =
    c.info(c.enclosingPosition, msg, force = true)

  private def abort(msg: String): Nothing =
    c.abort(c.enclosingPosition, msg)

  private val tpSlickInterpolation = mkType[ActionBasedSQLInterpolation]
  private val tpSetValuesParameter = mkType[SetValuesParameter[_]]
  private val tmSetValuesParameter = mkTerm[SetValuesParameter[_]]
  private val tmStringContext      = mkTerm[StringContext]
  private val tmImplicitly         = mkTerm("scala.Predef.implicitly")
  private val tpQueryManipulation  = mkType[QueryManipulation[_]]
}
