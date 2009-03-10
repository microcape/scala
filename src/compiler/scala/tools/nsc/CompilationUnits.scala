/* NSC -- new Scala compiler
 * Copyright 2005-2009 LAMP/EPFL
 * @author  Martin Odersky
 */
// $Id$

package scala.tools.nsc

import scala.tools.nsc.util.{FreshNameCreator,OffsetPosition,Position,SourceFile}
import scala.tools.nsc.io.AbstractFile
import scala.collection.mutable.{HashSet, HashMap, ListBuffer}

trait CompilationUnits { self: Global =>

  /** One unit of compilation that has been submitted to the compiler.
    * It typically corresponds to a single file of source code.  It includes
    * error-reporting hooks.  */
  class CompilationUnit(val source: SourceFile) extends CompilationUnitTrait {
    /** the fresh name creator */
    var fresh : FreshNameCreator = new FreshNameCreator.Default

    /** the content of the compilation unit in tree form */
    var body: Tree = EmptyTree

    /** Note: depends now contains toplevel classes.
     *  To get their sourcefiles, you need to dereference with .sourcefile
     */
    val depends = new HashSet[Symbol]
    /** so we can relink 
     */
    val defined = new HashSet[Symbol]

    /** Synthetic definitions generated by namer, eliminated by typer.
     */
    val synthetics = new HashMap[Symbol, Tree]

    /** things to check at end of compilation unit */
    val toCheck = new ListBuffer[() => Unit]

    /** used to track changes in a signature */
    var pickleHash : Long = 0

    def position(pos: Int) = source.position(pos)

    /** The icode representation of classes in this compilation unit.
     *  It is empty up to phase 'icode'.
     */
    val icode: HashSet[icodes.IClass] = new HashSet

    def error(pos: Position, msg: String) =
      reporter.error(pos, msg)

    def warning(pos: Position, msg: String) = 
      reporter.warning(pos, msg)

    def deprecationWarning(pos: Position, msg: String) = 
      if (settings.deprecation.value) warning(pos, msg)
      else currentRun.deprecationWarnings = true

    def uncheckedWarning(pos: Position, msg: String) = 
      if (settings.unchecked.value) warning(pos, msg)
      else currentRun.uncheckedWarnings = true

    def incompleteInputError(pos: Position, msg:String) =
      reporter.incompleteInputError(pos, msg) 

    /** Is this about a .java source file? */
    lazy val isJava = source.file.name.endsWith(".java")
    
    override def toString() = source.toString()

    def clear() {
      fresh = null
      body = null
      depends.clear
      defined.clear
    }
  }
}


