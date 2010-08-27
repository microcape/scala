// see #2781
object Test {
  class Rep[T](x : T)
 
  class SomeOps[T](x : Rep[T]) { def foo = 1 }
  implicit def mkOps[X, T](x : X)(implicit conv: X => Rep[T]) : SomeOps[T] = new SomeOps(conv(x))

  val a: Rep[Int] = error("")
  a.foo // mkOps(a).foo
}