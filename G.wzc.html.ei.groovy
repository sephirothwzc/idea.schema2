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
// 2018-02-08 v1.0.1 avalon 组件模版生成器
typeMapping = [
        (~/(?i)int/)                      : "number",
        (~/(?i)float|double|decimal|real/): "number",
        (~/(?i)timestamp/)       : "timestamp",
        (~/(?i)datetime/)       : "datetime",
        (~/(?i)date/)                     : "datetime",
        (~/(?i)time/)                     : "string",
        (~/(?i)/)                         : "string"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable && it.getKind() == ObjectKind.TABLE }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = javaName(table.getName(), true)
    def fields = calcFields(table)
    new File(dir, "ei"+className + ".html").withPrintWriter { out -> generate(out, className, fields,table.getName()) }
}

def generate(out, className, fields ,tablename) {
    out.println "<div id=\"page_ei$className\">"
    out.println "    <form class=\"form-horizontal\" role=\"form\" ms-validate=\"validate\">"
    out.println "       <div class=\"row\">"
    fields.each() {
        def dupstr =  "string"
        def fil = ""
        if(it.type == "timestamp") {
            fil = "| tamp2DateStr"
            dupstr = "datetime"
        } else if(it.type == "datetime") {
            fil = "| datehelper"
            dupstr = "datetime"
        }
        out.println "    <div class=\"col-xs-6 form-group\">"
        out.println "    <label class=\"col-xs-2 control-label\" for=\"$it.name\">{{@vmtag.$it.name}}</label>"
        out.println "           <div class=\"col-xs-10\">"
        if (it.type == "datetime") {
            out.println "           <div class=\"input-group date form_datetime \""
            out.println "               data-date=\"\""
            out.println "               data-date-format=\"yyyy-mm-dd hh:ii\">"
        }
        out.println "               <input class=\"form-control\" type=\"text\" ms-attr=\"[@isread, {placeholder:@vmtag.$it.name}]\"  ms-duplex-$dupstr=\"@vmmodel.$it.name$fil\" maxlength=\"25\">"
        if (it.type == "datetime") {
            out.println "           <span class=\"input-group-addon\"><span class=\"glyphicon glyphicon-remove\"></span></span>"
            out.println "           </div>"
        }
        out.println "           </div>"
        out.println "    </div>"
    }
    out.println "        </div>"
    out.println "       <div class=\"row\">"
    out.println "           <div class=\"col-xs-2\">"
    out.println "               <button type=\"button\" class=\"btn btn-primary\" ms-on-click=\"save\" ms-class=\"@isread.readonly?'none':''\">{{@vmtag.btnsave}}</button>"
    out.println "               &nbsp;&nbsp;"
    out.println "               <button type=\"reset\" class=\"btn btn-primary\" ms-on-click=\"closeTab\" ms-class=\"@isread.readonly?'none':''\">{{@vmtag.btncancel}}</button>"
    out.println "           </div>"
    out.println "           <div class=\"col-xs-10\"></div>"
    out.println "        </div>"
    out.println "    </form>"
    out.println "    <style scoped>"
    out.println "    #page_ei$className .none {"
    out.println "        display: none"
    out.println "    }"
    out.println "    </style>"
    out.println "</div>"



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
