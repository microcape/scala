/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


// generated by genprod on Thu Apr 29 17:52:16 CEST 2010  

package scala




/** Tuple1 is the canonical representation of a @see Product1 
 *  
 */
case class Tuple1[@specialized(Int, Long, Double) +T1](_1:T1)
  extends Product1[T1]
{  
  override def toString() = "(" + _1 + ")"  
  
}
