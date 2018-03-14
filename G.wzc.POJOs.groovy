import com.intellij.database.model.DasTable
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */
// 包名更换
packageName = "com.sephiroth.jpademo.entity;"
beforName = "Entity"
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
  out.println "package $packageName"
  out.println ""
  out.println ""
  // 引用映射
  out.println "import lombok.Data;"
  out.println "import org.hibernate.annotations.GenericGenerator;"
  out.println ""
  out.println "import javax.persistence.*;"
  out.println "import java.io.Serializable;"
  // jpa映射
  out.println "@Entity"
  out.println "@Table(name = \"$tablename\")"
  out.println "@Data"
  // jpa映射end
  out.println "public class $beforName$className  implements Serializable {"
  out.println ""
  fields.each() {
    if (it.annos != "") out.println "  ${it.annos}"
    // 列映射
    // 主键映射
    if (it.name == "id" && it.type == "String") {
      out.println "  @Id"
      out.println """  @GenericGenerator(name = "user-uuid", strategy = "uuid")
  @GeneratedValue(generator = "user-uuid")
  @Column(name = "id", nullable = false, length = 64)"""
    }
    else if(it.name == "id") {
      out.println """  @GeneratedValue
  @Column(name = \"$it.colname\")"""
    }
    else {
      out.println "  @Column(name = \"$it.colname\")"
    }
    out.println "  private ${it.type} ${it.name};"
    out.println ""
  }
  fields.each() {
    out.println "  public static final String  _${it.name} = \"${it.name}\";"
    out.println ""
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
