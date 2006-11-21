/*                     __                                               *\
**     ________ ___   / /  ___     Scala API                            **
**    / __/ __// _ | / /  / _ |    (c) 2006-2007, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

// $Id$


package scala.runtime


import java.lang.Character
import Predef.NoSuchElementException

final class RichChar(x: Char) extends Proxy with Ordered[Char] {

  // Proxy.self
  def self: Any = x

  // Ordered[Char].compare
  def compare (y: Char): Int = if (x < y) -1 else if (x > y) 1 else 0

  def asDigit: Int = Character.digit(x, Character.MAX_RADIX)

  def isControl: Boolean = Character.isISOControl(x)
  def isDigit: Boolean = Character.isDigit(x)
  def isLetter: Boolean = Character.isLetter(x)
  def isLetterOrDigit: Boolean = Character.isLetterOrDigit(x)
  def isLowerCase: Boolean = Character.isLowerCase(x)
  def isUpperCase: Boolean = Character.isUpperCase(x)
  def isWhitespace: Boolean = Character.isWhitespace(x)

  def toLowerCase: Char = Character.toLowerCase(x)
  def toUpperCase: Char = Character.toUpperCase(x)

  def to(y: Char): Iterator[Char] = new BufferedIterator[Char] {
    private var ch = x
    def hasNext: Boolean = ch < y
    def next: Char =
      if (hasNext) { val j = ch; ch = (ch + 1).toChar; j }
      else throw new NoSuchElementException("next on empty iterator")
    def head: Char =
      if (hasNext) ch
      else throw new NoSuchElementException("head on empty iterator")
  }

}
