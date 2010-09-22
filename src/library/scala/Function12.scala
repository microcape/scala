/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2002-2010, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


// generated by genprod on Thu Sep 09 09:06:40 PDT 2010  (with extra methods)

package scala




/** <p>
 *    Function with 12 parameters.
 *  </p>
 *  
 */
trait Function12[-T1, -T2, -T3, -T4, -T5, -T6, -T7, -T8, -T9, -T10, -T11, -T12, +R] extends AnyRef { self =>
  def apply(v1:T1,v2:T2,v3:T3,v4:T4,v5:T5,v6:T6,v7:T7,v8:T8,v9:T9,v10:T10,v11:T11,v12:T12): R
  override def toString() = "<function12>"
  
  /** f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12)  == (f.curried)(x1)(x2)(x3)(x4)(x5)(x6)(x7)(x8)(x9)(x10)(x11)(x12)
   */
  def curried: T1 => T2 => T3 => T4 => T5 => T6 => T7 => T8 => T9 => T10 => T11 => T12 => R = {
    (x1: T1) => ((x2: T2, x3: T3, x4: T4, x5: T5, x6: T6, x7: T7, x8: T8, x9: T9, x10: T10, x11: T11, x12: T12) => self.apply(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12)).curried
  }
  @deprecated("Use 'curried' instead")
  def curry = curried

  /* f(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12) == (f.tupled)(Tuple12(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12))
   */
  def tupled: Tuple12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12] => R = {
    case Tuple12(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12) => apply(x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12)
  }

}
