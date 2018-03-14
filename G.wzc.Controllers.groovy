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
packageName = "com.sephiroth.jpademo.controller"
aftorName = "Controller"
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
    new File(dir, className + aftorName + ".java").withPrintWriter { out -> generate(out, className, fields, table.getName()) }
}

def generate(out, className, fields, tablename) {
    out.println "package $packageName;"
    out.println ""
    out.println ""
    // 引用映射
    out.println "import com.sephiroth.jpademo.base.BaseResult;"
    out.println "import com.sephiroth.jpademo.entity.$entityName$className;"
    out.println "import com.sephiroth.jpademo.model." + $className + ".InManager$className;"
    out.println "import com.sephiroth.jpademo.service.Service$className;"
    out.println "import lombok.val;"
    out.println "import org.springframework.beans.factory.annotation.Autowired;"
    out.println "import org.springframework.validation.annotation.Validated;"
    out.println "import org.springframework.web.bind.annotation.PathVariable;"
    out.println "import org.springframework.web.bind.annotation.RequestBody;"
    out.println "import org.springframework.web.bind.annotation.RequestMapping;"
    out.println "import org.springframework.web.bind.annotation.RestController;"
    out.println ""
    out.println "import javax.validation.Valid;"
    out.println "import java.util.List;"
    out.println ""
    out.println "    /**"
    out.println "    * @Author: 吴占超"
    out.println "    * @Description:"
    out.println "    * @Date: Create in 17:41 2018/2/11"
    out.println "    * @Modified By:"
    out.println "    */"
    out.println "    @RestController"
    out.println "    @RequestMapping(value = \"/$className\")"
    out.println "    @Validated"
    out.println "    public class $classNameController {"
    out.println ""
    out.println "        @Autowired"
    out.println "        private Service$className service$className;"
    out.println ""
    out.println "        @RequestMapping(value = \"/manager\")"
    out.println "        public BaseResult cutUserPage(@RequestBody @Valid InManager$className param) {"
    out.println "            val pair = service$className.cutPage(param);"
    out.println "            return new BaseResult(new Runnable(){"
    out.println "               public List<EntitySysUser> rows = pair.getKey();"
    out.println "               public Long total = pair.getValue();"
    out.println "               public void run(){"
    out.println "               }"
    out.println "            });"
    out.println "        }"
    out.println ""
    out.println "        @RequestMapping(value = \"/item/{id}\")"
    out.println "        public BaseResult item(@PathVariable String id) {"
    out.println "            return new BaseResult(service$className.findOne(id));"
    out.println "        }"
    out.println ""
    out.println "        @RequestMapping(value = \"/del\")"
    out.println "        public BaseResult del(@RequestBody List<String> param) {"
    out.println "            service$className.delete(param);"
    out.println "            return new BaseResult(param.size());"
    out.println "        }"
    out.println ""
    out.println "        @RequestMapping(value = \"/save\")"
    out.println "        public BaseResult save(@RequestBody @Valid EntitySysUser param) {"
    out.println "            return new BaseResult(service"+$className+".save(param));"
    out.println "        }"
    out.println "    }"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        fields += [[
                           name   : javaName(col.getName(), false),
                           colname: col.getName(),
                           type   : typeStr,
                           annos  : """
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
    capitalize || s.length() == 1 ? s : Case.LOWER.apply(s[0]) + s[1..-1]
}
