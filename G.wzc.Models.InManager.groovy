import com.intellij.database.model.DasTable
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil
import sun.java2d.pipe.OutlineTextRenderer

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */
// 包名更换
packageName = "com.sephiroth.jpademo.model"
beforName = "InManager"
typeMapping = [
  (~/(?i)int/)                      : "long",
  (~/(?i)float|double|decimal|real/): "double",
  (~/(?i)datetime|timestamp/)       : "java.sql.Timestamp",
  (~/(?i)date/)                     : "java.sql.Date",
  (~/(?i)time/)                     : "java.sql.Time",
  (~/(?i)/)                         : "String"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
  SELECTION.filter { it instanceof DasTable && it.getKind() == ObjectKind.TABLE }.each { generate(it, dir) }
}

def generate(table, dir) {
  def className = javaName(table.getName(), true)
  def fields = calcFields(table)
  new File(dir, beforName+className + ".java").withPrintWriter { out -> generate(out, className, fields,table.getName()) }
}

def generate(out, className, fields ,tablename) {
  out.println "package $packageName.$className;"
  out.println ""
  out.println ""
  // 引用映射
  out.println "import com.sephiroth.jpademo.base.jpa.BasePagination;"
  out.println "import com.sephiroth.jpademo.retention.RetentionPagination;"
  out.println "import lombok.Data;"
  out.println "import org.hibernate.validator.constraints.NotEmpty;"
  out.println ""
  out.println "@Data"
  out.println "public class $beforName$className  extends BasePagination {"
  out.println ""
  fields.each() {
    if(it.type == "java.sql.Timestamp"||
            it.type == "java.sql.Date"||
            it.type == "java.sql.Time") {
      out.println "  @RetentionPagination(scpeEnum = RetentionPagination.ScpeEnum.gteq)"
      out.println "  private ${it.type} ${it.name};"
      out.println ""
      out.println "  @RetentionPagination(scpeEnum = RetentionPagination.ScpeEnum.lteq)"
      out.println "  private ${it.type} ${it.name}1;"
      out.println ""

    }else {
      out.println "  @RetentionPagination"
      out.println "  private ${it.type} ${it.name};"
      out.println ""
    }
  }
  out.println ""
  out.println "}"
}

def calcFields(table) {
  DasUtil.getColumns(table).reduce([]) { fields, col ->
    def spec = Case.LOWER.apply(col.getDataType().getSpecification())
    def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
    fields += [[
                 name : javaName(col.getName(), false),
                 colname : col.getName(),
                 type : typeStr,
                 annos: """
  /**
   * $col.comment
   */"""]]
  }
}

def javaName(str, capitalize) {
  def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
    .collect { Case.LOWER.apply(it).capitalize() }
    .join("")
    .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
  capitalize || s.length() == 1? s : Case.LOWER.apply(s[0]) + s[1..-1]
}
