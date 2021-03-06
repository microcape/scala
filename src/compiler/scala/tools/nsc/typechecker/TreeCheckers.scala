/* NSC -- new Scala compiler
 * Copyright 2005-2010 LAMP/EPFL
 * @author  Martin Odersky
 */

package scala.tools.nsc
package typechecker

import scala.tools.nsc.symtab.Flags._
import scala.collection.mutable
import mutable.HashMap
import util.returning

abstract class TreeCheckers extends Analyzer {
  import global._

  lazy val tpeOfTree = new HashMap[Tree, Type]
  
  def posstr(p: Position) = 
    try p.source.path + ":" + p.line
    catch { case _: UnsupportedOperationException => p.toString }
  
  def errorFn(msg: Any): Unit                = println("[check: %s] %s".format(phase.prev, msg))
  def errorFn(pos: Position, msg: Any): Unit = errorFn(posstr(pos) + ": " + msg)

  def assertFn(cond: Boolean, msg: => Any) =
    if (!cond) errorFn(msg)

  def checkTrees {
    if (settings.verbose.value)
      Console.println("[consistency check at the beginning of phase " + phase + "]")
    
    currentRun.units foreach check
  }
  
  def printingTypings[T](body: => T): T = {
    val saved = global.printTypings
    global.printTypings = true
    val result = body
    global.printTypings = saved
    result
  }
  def runWithUnit[T](unit: CompilationUnit)(body: => Unit): Unit = {
    val unit0 = currentRun.currentUnit
    currentRun.currentUnit = unit
    body
    currentRun.advanceUnit
    assertFn(currentRun.currentUnit == unit, "currentUnit is " + currentRun.currentUnit + ", but unit is " + unit)
    currentRun.currentUnit = unit0
  }

  def check(unit: CompilationUnit) {
    informProgress("checking "+unit)
    val context = rootContext(unit)
    context.checking = true
    tpeOfTree.clear
    val checker = new TreeChecker(context)
    runWithUnit(unit) {
      checker.precheck.traverse(unit.body)
      checker.typed(unit.body)
      checker.postcheck.traverse(unit.body)
    }
  }

  override def newTyper(context: Context): Typer = new TreeChecker(context)

  class TreeChecker(context0: Context) extends Typer(context0) {
    private def classstr(x: AnyRef) = x.getClass.getName split '.' last;
    private def typestr(x: Type)    = " (tpe = " + x + ")"
    private def treestr(t: Tree)    = t + " [" + classstr(t) + "]" + typestr(t.tpe)
    private def ownerstr(s: Symbol) = "'" + s + "'" + s.locationString
    
    // XXX check for tree.original on TypeTrees.
    private def treesDiffer(t1: Tree, t2: Tree) =
      errorFn(t1.pos, "trees differ\n old: " + treestr(t1) + "\n new: " + treestr(t2))
    private def typesDiffer(tree: Tree, tp1: Type, tp2: Type) =
      errorFn(tree.pos, "types differ\n old: " + tp1 + "\n new: " + tp2 + "\n tree: " + tree)
    private def ownersDiffer(tree: Tree, shouldBe: Symbol) = {
      val sym = tree.symbol
      errorFn(tree.pos, sym + " has wrong owner: " + ownerstr(sym.owner) + ", should be: " + ownerstr(shouldBe))
    }
    
    /** XXX Disabled reporting of position errors until there is less noise. */
    private def noPos(t: Tree) =
      () // errorFn("no pos: " + treestr(t))
    private def noType(t: Tree) =
      errorFn(t.pos, "no type: " + treestr(t))

    private def checkSym(t: Tree) =
      if (t.symbol == NoSymbol)
        errorFn(t.pos, "no symbol: " + treestr(t))

    override def typed(tree: Tree, mode: Int, pt: Type): Tree = returning(tree) {
      case EmptyTree | TypeTree() => ()
      case _ if tree.tpe != null  =>
        tpeOfTree.getOrElseUpdate(tree, {
          val saved = tree.tpe
          tree.tpe = null
          saved
        })
        super.typed(tree, mode, pt) match {
          case _: Literal     => ()
          case x if x ne tree => treesDiffer(tree, x)
          case _              => ()
        }
      case _ => ()
    }

    object precheck extends Traverser {
      override def traverse(tree: Tree) {
        val sym = tree.symbol
        def accessed = sym.accessed
        def fail(msg: String) = errorFn(tree.pos, msg + classstr(tree) + " / " + tree)
        
        tree match {
          case DefDef(_, _, _, _, _, _) =>
            if (sym.isGetterOrSetter && !sym.isDeferred) {
              sym.tpe.resultType match {
                case _: ConstantType  => ()
                case _                =>
                  checkSym(tree)
                  /** XXX: lots of syms show up here with accessed == NoSymbol. */
                  if (accessed != NoSymbol) {
                    val agetter = accessed.getter(sym.owner)
                    val asetter = accessed.setter(sym.owner)

                    assertFn(agetter == sym || asetter == sym,
                      sym + " is getter or setter, but accessed sym " + accessed + " shows " + agetter + " and " + asetter
                    )
                  }
              }
            }
          case ValDef(_, _, _, _) =>
            if (sym.hasGetter && !sym.isOuterField) {
              assertFn(sym.getter(sym.owner) != NoSymbol, ownerstr(sym) + " has getter but cannot be found. " + sym.ownerChain)
            }
          case Apply(fn, args) =>
            if (args exists (_ == EmptyTree))
              errorFn(tree.pos, "Apply arguments to " + fn + " contains an empty tree: " + args)
              
          case Select(qual, name) =>
            checkSym(tree)
          case This(_) =>
            checkSym(tree)
            if (sym.isStatic && (sym hasFlag MODULE)) ()
            else if (currentOwner.ownerChain takeWhile (_ != sym) exists (_ == NoSymbol))
              return fail("tree symbol "+sym+" does not point to enclosing class; tree = ")
          
          /** XXX: temporary while Import nodes are arriving untyped. */
          case Import(_, _) =>
            return
          case _ =>
        }

        if (tree.pos == NoPosition && tree != EmptyTree)
          noPos(tree)
        else if (tree.tpe == null && phase.id > currentRun.typerPhase.id)
          noType(tree)
        else if (tree.isDef) {
          checkSym(tree)
          
          tree match {
            case x: PackageDef    =>
              if (sym.ownerChain contains currentOwner) ()
              else fail(sym + " owner chain does not contain currentOwner " + currentOwner)
            case _ =>
              def cond(s: Symbol) = s.isTerm && !s.isMethod && s != sym.owner

              if (sym.owner != currentOwner) {
                val found = currentOwner.ownerChain find (x => !cond(x)) getOrElse fail("DefTree can't find owner: ")
                if (sym.owner != found)
                  fail("Expected owner %s, found %s: ".format(found, sym.owner))
              }
          }
        }
        super.traverse(tree)
      }
    }

    object postcheck extends Traverser {
      override def traverse(tree: Tree) {
        tree match {
          case EmptyTree | TypeTree() => ()
          case _ =>
            tpeOfTree get tree foreach { oldtpe =>
              if (oldtpe =:= tree.tpe) ()
              else typesDiffer(tree, oldtpe, tree.tpe)
              
              tree.tpe = oldtpe
              super.traverse(tree)
            }
        }
      }
    }
  }
}
