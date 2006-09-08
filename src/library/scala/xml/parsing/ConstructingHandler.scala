/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2006, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id$


package scala.xml.parsing

/** Implementation of MarkupHandler that constructs nodes.
 *
 *  @author  Burak Emir
 *  @version 1.0
 */
abstract class ConstructingHandler extends MarkupHandler {

  val preserveWS: Boolean

  def elem(pos: int, pre: String, label: String, attrs: MetaData, pscope: NamespaceBinding, nodes: NodeSeq): NodeSeq =
    Elem(pre, label, attrs, pscope, nodes:_*)

  def procInstr(pos: Int, target: String, txt: String) =
    ProcInstr(target, txt)

  def comment(pos: Int, txt: String) =
    Comment(txt)

  def entityRef(pos: Int, n: String) =
    EntityRef(n)

  def text(pos: Int, txt:String) =
    Text(txt)

}
