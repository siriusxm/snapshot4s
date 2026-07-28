import scalafix.v1._
import scala.meta._

class ResetSnapshots extends SyntacticRule("ResetSnapshots") {

  override def fix(implicit doc: SyntacticDocument): Patch = {
    doc.tree.collect { case tree @ q"assertInlineSnapshot($found, $expected)" =>
      Patch.replaceTree(tree, s"assertInlineSnapshot($found, ???)")
    }.asPatch
  }
}
