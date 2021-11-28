package com.accode.slickvalues

import slick.jdbc.ActionBasedSQLInterpolation

import scala.reflect.macros.blackbox

object MacroBuilder {
  def impl(c: blackbox.Context)(params: c.Tree*): c.Tree =
    new MacroBuilder[c.type](c)(params).sql
}

class MacroBuilder[C <: blackbox.Context](val c: C)(inputParams: Seq[C#Tree]) extends TreeBuilder[C] {
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
        case (acc, (part @ LStr(partStr), param)) =>
          if (!paramMatches(param))
            (part, param) :: acc
          else
            (LStr("#"), q"$tmQueryManipulation($param)") ::
              (LStr(partStr + "("), param) ::
              acc
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

  private def constructSqlInterpolation(parts: List[Tree], params: List[Tree]): Tree = {
    val strCtx = q"$tmStringContext(..$parts)"
    val res    = q"new $tpActionBasedSQLInterpolation($strCtx).sql(..$params)"
    info(s"\nRES: $res\n    ${showRaw(res)}")
    res
  }

  private def info(msg: String): Unit =
    c.info(c.enclosingPosition, msg, force = true)

  private def abort(msg: String): Nothing =
    c.abort(c.enclosingPosition, msg)

  private val tpActionBasedSQLInterpolation = mkType[ActionBasedSQLInterpolation]
  private val tpSetValuesParameter          = mkType[SetValuesParameter[_]]
  private val tmStringContext               = mkTerm[StringContext]
  private val tmImplicitly                  = mkTerm("scala.Predef.implicitly")
  private val tmQueryManipulation           = mkTerm[QueryManipulation.type]
}
