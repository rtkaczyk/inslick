package com.accode.inslick

import slick.jdbc.ActionBasedSQLInterpolation

import scala.reflect.macros.blackbox

object MacroBuilder {
  def impl(c: blackbox.Context)(params: c.Tree*): c.Tree =
    new MacroBuilder[c.type](c)(params).sql
}

class MacroBuilder[C <: blackbox.Context](val c: C)(inputParams: Seq[C#Tree])
    extends TreeBuilder[C] {
  import c.universe._

  def sql: Tree = {
    val (parts, params) = modifyQuery(
      extractStringParts,
      inputParams.toList.asInstanceOf[List[Tree]]
    )
    constructSqlInterpolation(parts, params)
  }

  private def extractStringParts: List[Tree] = {
    val Apply(Select(Apply(_, List(Apply(_, strParts))), _), _) = c.macroApplication
    strParts.map {
      case l @ LStr(_) => l
      case _           => abort("StringContext parts must be String literals")
    }
  }

  private def modifyQuery(parts: List[Tree], params: List[Tree]): (List[Tree], List[Tree]) = {
    val (modParts, modParams) = parts
      .zip(params)
      .foldLeft(List.empty[(Tree, Tree)]) {
        case (acc, (LStr(partStr), param)) if paramMatches(param) =>
          val after  = (LStr("#"), q"$tmQueryManipulation($param)")
          val before = (LStr(partStr + "("), param)
          after :: before :: acc

        case (acc, (part, param)) =>
          (part, param) :: acc
      }
      .reverse
      .unzip
    (modParts :+ parts.last, modParams)
  }

  private def paramMatches(param: c.Tree): Boolean = {
    val svp = tq"$tpSetValuesParameter[${TypeTree(param.tpe)}]"
    val tc  = c.typecheck(q"$tmImplicitly[$svp]", silent = true)
    !tc.equalsStructure(EmptyTree)
  }

  private def constructSqlInterpolation(parts: List[Tree], params: List[Tree]): Tree =
    q"new $tpSlickInterpolation($tmStringContext(..$parts)).sql(..$params)"

  private def info(msg: String): Unit =
    c.info(c.enclosingPosition, msg, force = true)

  private def abort(msg: String): Nothing =
    c.abort(c.enclosingPosition, msg)

  private val tpSlickInterpolation = mkType[ActionBasedSQLInterpolation]
  private val tpSetValuesParameter = mkType[SetValuesParameter[_]]
  private val tmStringContext      = mkTerm[StringContext]
  private val tmImplicitly         = mkTerm("scala.Predef.implicitly")
  private val tmQueryManipulation  = mkTerm[QueryManipulation.type]
}
