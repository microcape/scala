/* NSC -- new Scala compiler
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Martin Odersky
 */
// $Id$

package scala.tools.nsc
package ast

import scala.collection.mutable.ListBuffer
import symtab.Flags._
import symtab.SymbolTable

/** XXX to resolve: TreeGen only assumes global is a SymbolTable, but
 *  TreeDSL at the moment expects a Global.  Can we get by with SymbolTable?
 */
abstract class TreeGen
{
  val global: SymbolTable

  import global._
  import definitions._
  
  def rootId(name: Name)          = Select(Ident(nme.ROOTPKG), name)
  def rootScalaDot(name: Name)    = Select(rootId(nme.scala_) setSymbol ScalaPackage, name)
  def scalaDot(name: Name)        = Select(Ident(nme.scala_) setSymbol ScalaPackage, name)
  def scalaAnyRefConstr           = scalaDot(nme.AnyRef.toTypeName)
  def scalaUnitConstr             = scalaDot(nme.Unit.toTypeName)
  def scalaScalaObjectConstr      = scalaDot(nme.ScalaObject.toTypeName)
  def productConstr               = scalaDot(nme.Product.toTypeName)
  
  def scalaFunctionConstr(argtpes: List[Tree], restpe: Tree): Tree = 
    AppliedTypeTree(
      scalaDot(newTypeName("Function"+argtpes.length)),
      argtpes ::: List(restpe))

  /** Builds a reference to value whose type is given stable prefix.
   *  The type must be suitable for this.  For example, it
   *  must not be a TypeRef pointing to an abstract type variable.
   */
  def mkAttributedQualifier(tpe: Type): Tree =
    mkAttributedQualifier(tpe, NoSymbol)

  /** Builds a reference to value whose type is given stable prefix.
   *  If the type is unsuitable, e.g. it is a TypeRef for an
   *  abstract type variable, then an Ident will be made using
   *  termSym as the Ident's symbol.  In that case, termSym must
   *  not be NoSymbol.
   */
  def mkAttributedQualifier(tpe: Type, termSym: Symbol): Tree = tpe match {
    case NoPrefix =>
      EmptyTree
    case ThisType(clazz) =>
      if (clazz.isRoot || clazz.isEmptyPackageClass) EmptyTree
      else mkAttributedThis(clazz)
    case SingleType(pre, sym) =>
      val qual = mkAttributedStableRef(pre, sym)
      qual.tpe match {
        case MethodType(List(), restpe) =>
          Apply(qual, List()) setType restpe
        case _ =>
          qual
      }
    case TypeRef(pre, sym, args) =>
      if (sym.isRoot) {
        mkAttributedThis(sym)
      } else if (sym.isModuleClass) {
        val qual = mkAttributedRef(pre, sym.sourceModule)
        qual.tpe match {
          case MethodType(List(), restpe) =>
            Apply(qual, List()) setType restpe
          case _ =>
            qual
        }
      } else if (sym.isModule || sym.isClass) {
        assert(phase.erasedTypes, tpe)
        mkAttributedThis(sym)
      } else if (sym.isType) {
        assert(termSym != NoSymbol)
        mkAttributedIdent(termSym) setType tpe
      } else {
        mkAttributedRef(pre, sym)
      }

    case ConstantType(value) =>
      Literal(value) setType tpe

    case AnnotatedType(_, atp, _) =>
      mkAttributedQualifier(atp)

    case RefinedType(parents, _) =>
      // I am unclear whether this is reachable, but
      // the following implementation looks logical -Lex
      val firstStable = parents.find(_.isStable)
      assert(!firstStable.isEmpty)
      mkAttributedQualifier(firstStable.get)

    case _ =>
      throw new Error("bad qualifier: " + tpe)
  }

  /** Builds a reference to given symbol with given stable prefix. */
  def mkAttributedRef(pre: Type, sym: Symbol): Tree = {
    val qual = mkAttributedQualifier(pre)
    qual match {
      case EmptyTree                                                              => mkAttributedIdent(sym)
      case This(clazz) if (qual.symbol.isRoot || qual.symbol.isEmptyPackageClass) => mkAttributedIdent(sym)
      case _                                                                      => mkAttributedSelect(qual, sym)
    }
  }

  /** Builds a reference to given symbol. */
  def mkAttributedRef(sym: Symbol): Tree =
    if (sym.owner.isClass) mkAttributedRef(sym.owner.thisType, sym)
    else mkAttributedIdent(sym)

  /** Replaces tree type with a stable type if possible */
  def stabilize(tree: Tree): Tree = tree match {
    case Ident(_) =>
      if (tree.symbol.isStable) tree.setType(singleType(tree.symbol.owner.thisType, tree.symbol))
      else tree
    case Select(qual, _) =>
      assert((tree.symbol ne null) && (qual.tpe ne null))
      if (tree.symbol.isStable && qual.tpe.isStable)
        tree.setType(singleType(qual.tpe, tree.symbol))
      else tree
    case _ =>
      tree
  }

  /** Cast `tree' to type `pt' */
  def mkAttributedCastUntyped(tree: Tree, pt: Type): Tree = {
    if (settings.debug.value) log("casting " + tree + ":" + tree.tpe + " to " + pt)
    assert(!tree.tpe.isInstanceOf[MethodType], tree)
    assert(!pt.typeSymbol.isPackageClass)
    assert(!pt.typeSymbol.isPackageObjectClass)
    assert(pt eq pt.normalize) //@MAT only called during erasure, which already takes care of that
    atPos(tree.pos)(mkAsInstanceOf(tree, pt, false))
  }

  /** Builds a reference with stable type to given symbol */
  def mkAttributedStableRef(pre: Type, sym: Symbol): Tree =
    stabilize(mkAttributedRef(pre, sym))

  def mkAttributedStableRef(sym: Symbol): Tree =
    stabilize(mkAttributedRef(sym))

  def mkAttributedThis(sym: Symbol): Tree =
    This(sym.name) setSymbol sym setType sym.thisType

  def mkAttributedIdent(sym: Symbol): Tree =
    Ident(sym.name) setSymbol sym setType sym.tpe

  /** XXX this method needs a close analysis to identify its essential logical
   *  units - this attempt at modularization verges on the arbitrary.
   */
  def mkAttributedSelect(qual: Tree, sym: Symbol): Tree = {
    def tpe = qual.tpe
    def isUnqualified(s: Symbol) =
      s != null && (List(nme.ROOT, nme.EMPTY_PACKAGE_NAME) contains s.name.toTermName)
    def isInPkgObject(s: Symbol) =
      tpe != null && s.owner.isPackageObjectClass && s.owner.owner == tpe.typeSymbol
    def getQualifier() =
      if (!isInPkgObject(sym)) qual
      else {
        val pkgobj = sym.owner.sourceModule
        Select(qual, nme.PACKAGEkw) setSymbol pkgobj setType singleType(tpe, pkgobj)
      } 

    if (isUnqualified(qual.symbol)) mkAttributedIdent(sym)
    else {
      val newQualifier = getQualifier()
      def verifyType(tree: Tree) =
        if (newQualifier.tpe == null) tree
        else tree setType (tpe memberType sym)
      
      verifyType( Select(newQualifier, sym.name) setSymbol sym )
    }
  }
  
  private def mkTypeApply(value: Tree, tpe: Type, what: Symbol) =
    Apply(
      TypeApply(
        mkAttributedSelect(value, what),
        List(TypeTree(tpe.normalize))
      ),
      Nil
    )
  /** Builds an instance test with given value and type. */
  def mkIsInstanceOf(value: Tree, tpe: Type, any: Boolean = true): Tree =
    mkTypeApply(value, tpe, (if (any) Any_isInstanceOf else Object_isInstanceOf))

  /** Builds a cast with given value and type. */
  def mkAsInstanceOf(value: Tree, tpe: Type, any: Boolean = true): Tree =
    mkTypeApply(value, tpe, (if (any) Any_asInstanceOf else Object_asInstanceOf))

  def mkClassOf(tp: Type): Tree = 
    Literal(Constant(tp)) setType Predef_classOfType(tp)

  def mkCheckInit(tree: Tree): Tree = {
    val tpe =
      if (tree.tpe != null || !tree.hasSymbol) tree.tpe
      else tree.symbol.tpe

    if (!global.phase.erasedTypes && settings.Xchecknull.value && 
        tpe <:< NotNullClass.tpe && !tpe.isNotNull)
      mkRuntimeCall(nme.checkInitialized, List(tree))
    else
      tree
  }

  /** Builds a list with given head and tail. */
  def mkNewCons(head: Tree, tail: Tree): Tree =
    New(Apply(mkAttributedRef(ConsClass), List(head, tail)))

  /** Builds a list with given head and tail. */
  def mkNil: Tree = mkAttributedRef(NilModule)

  /** Builds a tuple */
  def mkTuple(elems: List[Tree]): Tree =
    if (elems.isEmpty) Literal(())
    else Apply(
      Select(mkAttributedRef(TupleClass(elems.length).caseModule), nme.apply),
      elems)

  // tree1 AND tree2
  def mkAnd(tree1: Tree, tree2: Tree): Tree =
    Apply(Select(tree1, Boolean_and), List(tree2))

  // tree1 OR tree2
  def mkOr(tree1: Tree, tree2: Tree): Tree =
    Apply(Select(tree1, Boolean_or), List(tree2))

  def mkCached(cvar: Symbol, expr: Tree): Tree = {
    val cvarRef = if (cvar.owner.isClass) Select(This(cvar.owner), cvar) else Ident(cvar)
    Block(
      List(
        If(Apply(Select(cvarRef, nme.eq), List(Literal(Constant(null)))),
           Assign(cvarRef, expr),
           EmptyTree)),
      cvarRef
    )
  }

  // var m$: T = null; or, if class member: local var m$: T = _;
  def mkModuleVarDef(accessor: Symbol) = {
    val mvar = accessor.owner.newVariable(accessor.pos.focus, nme.moduleVarName(accessor.name))
      .setInfo(accessor.tpe.finalResultType)
      .setFlag(MODULEVAR);
    if (mvar.owner.isClass) {
      mvar setFlag (PRIVATE | LOCAL | SYNTHETIC)
      mvar.owner.info.decls.enter(mvar)
    } 
    ValDef(mvar, if (mvar.owner.isClass) EmptyTree else Literal(Constant(null)))
  }

  // def m: T = { if (m$ eq null) m$ = new m$class(...) m$ }
  // where (...) are eventual outer accessors
  def mkCachedModuleAccessDef(accessor: Symbol, mvar: Symbol) =
    DefDef(accessor, mkCached(mvar, newModule(accessor, mvar.tpe)))

  // def m: T = new tpe(...)
  // where (...) are eventual outer accessors
  def mkModuleAccessDef(accessor: Symbol, tpe: Type) =
    DefDef(accessor, newModule(accessor, tpe))

  private def newModule(accessor: Symbol, tpe: Type) =
    New(TypeTree(tpe), 
        List(for (pt <- tpe.typeSymbol.primaryConstructor.info.paramTypes) 
             yield This(accessor.owner.enclClass)))

  // def m: T;
  def mkModuleAccessDcl(accessor: Symbol) = 
    DefDef(accessor setFlag lateDEFERRED, EmptyTree)

  def mkRuntimeCall(meth: Name, args: List[Tree]): Tree = {
    assert(meth.toString != "boxArray") // !!! can be removed once arrays are in.
    Apply(Select(mkAttributedRef(ScalaRunTimeModule), meth), args)
  }

  def mkRuntimeCall(meth: Name, targs: List[Type], args: List[Tree]): Tree = {
    Apply(TypeApply(Select(mkAttributedRef(ScalaRunTimeModule), meth), targs map TypeTree), args)
  }

  /** Make a synchronized block on 'monitor'. */
  def mkSynchronized(monitor: Tree, body: Tree): Tree =     
    Apply(Select(monitor, Object_synchronized), List(body))

  def wildcardStar(tree: Tree) =
    atPos(tree.pos) { Typed(tree, Ident(nme.WILDCARD_STAR.toTypeName)) }

  def paramToArg(vparam: Symbol) = {
    val arg = Ident(vparam)
    if (isRepeatedParamType(vparam.tpe)) wildcardStar(arg)
    else arg
  }

  def paramToArg(vparam: ValDef) = {
    val arg = Ident(vparam.name)
    if (treeInfo.isRepeatedParamType(vparam.tpt)) wildcardStar(arg)
    else arg
  }

  /** Make forwarder to method `target', passing all parameters in `params' */
  def mkForwarder(target: Tree, vparamss: List[List[Symbol]]) =
    (target /: vparamss)((fn, vparams) => Apply(fn, vparams map paramToArg))
    
  /** Used in situations where you need to access value of an expression several times
   */
  def evalOnce(expr: Tree, owner: Symbol, unit: CompilationUnit)(within: (() => Tree) => Tree): Tree =
    if (treeInfo.isPureExpr(expr)) {
      within(() => expr);
    } else {
      val temp = owner.newValue(expr.pos.makeTransparent, unit.fresh.newName(expr.pos, "ev$"))
        .setFlag(SYNTHETIC).setInfo(expr.tpe)
      val containing = within(() => Ident(temp) setPos temp.pos.focus setType expr.tpe)
      ensureNonOverlapping(containing, List(expr))
      Block(List(ValDef(temp, expr)), containing) setPos (containing.pos union expr.pos)
    }

  def evalOnceAll(exprs: List[Tree], owner: Symbol, unit: CompilationUnit)(within: (List[() => Tree]) => Tree): Tree = {
    val vdefs = new ListBuffer[ValDef]
    val exprs1 = new ListBuffer[() => Tree]
    for (expr <- exprs) {
      if (treeInfo.isPureExpr(expr)) {
        exprs1 += (() => expr)
      } else {
        val temp = owner.newValue(expr.pos.makeTransparent, unit.fresh.newName(expr.pos, "ev$"))
          .setFlag(SYNTHETIC).setInfo(expr.tpe)
        vdefs += ValDef(temp, expr)
        exprs1 += (() => Ident(temp) setPos temp.pos.focus setType expr.tpe)
      }
    }
    val prefix = vdefs.toList
    val containing = within(exprs1.toList)
    ensureNonOverlapping(containing, exprs)
    if (prefix.isEmpty) containing
    else Block(prefix, containing) setPos (prefix.head.pos union containing.pos)
  }
}
