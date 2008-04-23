
/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2008, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id$

// generated by genprod on Wed Apr 23 10:06:16 CEST 2008  

package scala

/** Tuple3 is the canonical representation of a @see Product3 
 *  
 */
case class Tuple3[+T1, +T2, +T3](_1:T1, _2:T2, _3:T3) 
  extends Product3[T1, T2, T3]  {

   override def toString() = {
     val sb = new StringBuilder
     sb.append('(').append(_1).append(',').append(_2).append(',').append(_3).append(')')
     sb.toString
   }
  
}
