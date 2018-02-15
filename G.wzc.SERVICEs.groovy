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
packageName = "com.sephiroth.jpademo.service;"
beforName = "Service"
entityName = "Entity"
jpaName = "Jpa"
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
    new File(dir, beforName + className + ".java").withPrintWriter { out -> generate(out, className, fields,table.getName()) }
}

def generate(out, className, fields ,tablename) {
    out.println "package $packageName"
    out.println ""
    out.println ""
    // 引用映射
    out.println "import com.sephiroth.jpademo.entity.$entityName$className;"
    out.println "import com.sephiroth.jpademo.base.BaseJpaRepository;"
    out.println "import com.sephiroth.jpademo.base.BaseService;"
    out.println "import com.sephiroth.jpademo.jpadao.$jpaName$className;"
    out.println "import org.springframework.beans.factory.annotation.Autowired;"
    out.println "import org.springframework.stereotype.Service;"
    out.println ""
    // jpa映射end
    out.println "/**\n" +
            " * @Author: 吴占超\n" +
            " * @Description: Service \n" +
            " * @Date: Create in \n" +
            " * @Modified By:\n" +
            " */"
    out.println "@Service"
    out.println "public class $beforName$className  extends\n" +
            "    BaseService<$entityName$className> {"
    out.println ""
    out.println "    @Autowired"
    out.println "    private $jpaName$className jpa$className;"
    out.println ""
    out.println "    @Override"
    out.println "    public BaseJpaRepository getBaseJpaRepository() {"
    out.println "        return jpa$className;"
    out.println "    }"
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
