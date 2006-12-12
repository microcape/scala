
/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2007, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id: genprod.scala 9494 2006-12-11 11:35:38Z odersky $

// generated by genprod on Tue Dec 12 11:32:13 CET 2006

package scala

import Predef._

object Product0 {
  def unapply(x:Any): Option[Product0] =
    if(x.isInstanceOf[Product0]) Some(x.asInstanceOf[Product0]) else None
}

/** Product0 is a cartesian product of 0 components 
 */
trait Product0 extends Product {

  /**
   *  The arity of this product.
   *  @return 0
   */
  override def arity = 0

  /**
   *  Returns the n-th projection of this product if 0&lt;=n&lt;arity,
   *  otherwise null.
   *
   *  @param n number of the projection to be returned 
   *  @return  same as _(n+1)
   *  @throws  IndexOutOfBoundsException
   */
  override def element(n: Int) = n match {
    case _ => throw new IndexOutOfBoundsException(n.toString())
  }

  
}
