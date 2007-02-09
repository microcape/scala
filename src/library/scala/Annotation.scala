/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2006-2007, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id: Annotation.scala 9916 2007-02-07 13:07:33 +0000 (Wed, 07 Feb 2007) michelou $


package scala

/** <p>
 *   A base class for annotations. Annotations extending this class directly
 *   are not preserved for the Scala type checker and are also not stored
 *   as Java annotations in classfiles. To enable either or both of these,
 *   one needs to inherit from <code>StaticAnnotation</code> or/and
 *   <a href="ClassfileAnnotation.html"><code>ClassfileAnnotation</code></a>.
 *  </p>
 *
 *  @author  Martin Odersky
 *  @version 1.1, 2/02/2007
 */
abstract class Annotation extends Attribute /* for now, to enable bootstrapping */ {}
