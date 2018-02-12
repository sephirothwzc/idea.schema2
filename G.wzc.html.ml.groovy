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
        (~/(?i)datetime|timestamp/)       : "datetime",
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
    new File(dir, "ml"+className + ".html").withPrintWriter { out -> generate(out, className, fields,table.getName()) }
}

def generate(out, className, fields ,tablename) {

    out.println "<div id=\"page_ml$className\">"
    out.println "    <form class=\"form-horizontal\" role=\"form\" ms-validate=\"validate\">"
    out.println "       <div class=\"row\">"
    fields.each() {
        def dupstr = it.type == "datetime" ? "string": it.type
        out.println "           <div class=\"col-xs-6 form-group\">"
        out.println "               <label class=\"col-xs-2 control-label\" for=\"$it.name\">{{@vmtag.$it.name}}</label>"
        out.println "               <div class=\"col-xs-10\">"
        if (it.type == "datetime") {
            out.println "                   <div class=\"input-group date form_datetime \""
            out.println "                       data-date=\"\""
            out.println "                       data-date-format=\"yyyy-mm-dd hh:ii\">"
        }
        out.println "                       <input class=\"form-control\" type=\"text\" ms-attr=\"{placeholder:@vmtag.$it.name}\" ms-duplex-$dupstr=\"@vmmodel.$it.name\" maxlength=\"25\">"
        if (it.type == "datetime") {
            out.println "                           <span class=\"input-group-addon\"><span class=\"glyphicon glyphicon-remove\"></span></span>"
            out.println "                   </div>"
        }
        out.println "               </div>"
        out.println "           </div>"
    }
    out.println "       </div>"
    out.println "       <div class=\"row\">"
    out.println "           <div class=\"col-xs-2\">"
    out.println "               <button type=\"button\" class=\"btn btn-primary\" ms-on-click=\"refreshTable\">{{@vmtag.btnquery}}</button>"
    out.println "                &nbsp;&nbsp;"
    out.println "               <button type=\"reset\" class=\"btn btn-primary\">{{@vmtag.btnreset}}</button>"
    out.println "           </div>"
    out.println "           <div class=\"col-xs-10\"></div>"
    out.println "       </div>"
    out.println "    </form>"
    out.println "    <div class=\"container-fluid\">"
    out.println "       <div class=\"row-fluid\">"
    out.println "          <div class=\"row\">"
    out.println "          <div class=\"col-xs-12 form-group\">"
    out.println "             <div id=\"ml"+ className +"_toolbar\" class=\"btn-group\">"
    out.println "                 <button type=\"button\" class=\"btn btn-default glyphicon glyphicon-plus\" ms-on-click=\"addItem\">"
    out.println "                     {{@vmtag.add}}"
    out.println "                 </button>"
    out.println "                 <button type=\"button\" class=\"btn btn-default glyphicon glyphicon-pencil\" ms-on-click=\"editItem\">"
    out.println "                     {{@vmtag.upd}}"
    out.println "                 </button>"
    out.println "                 <button type=\"button\" class=\"btn btn-default glyphicon glyphicon-zoom-in\" ms-on-click=\"showItem\">"
    out.println "                     {{@vmtag.look}}"
    out.println "                 </button>"
    out.println "                 <button type=\"button\" class=\"btn btn-default glyphicon glyphicon-minus\" ms-on-click=\"delItem\">"
    out.println "                     {{@vmtag.del}}"
    out.println "                 </button>"
    out.println "                 <div class=\"btn-group\" role=\"group\">"
    out.println "                    <button type=\"button\" class=\"btn btn-default dropdown-toggle glyphicon glyphicon-floppy-save\" data-toggle=\"dropdown\" aria-haspopup=\"true\""
    out.println "                    aria-expanded=\"false\" id=\"etbutton\">"
    out.println "                         {{@exportType}}"
    out.println "                        <span class=\"caret\"></span>"
    out.println "                    </button>"
    out.println "                    <ul class=\"dropdown-menu\">"
    out.println "                        <li>"
    out.println "                            <a href=\"javascript:void(0)\" ms-on-click=\"changExportType(vmtag.exportall)\">{{@vmtag.exportall}}</a>"
    out.println "                        </li>"
    out.println "                        <li>"
    out.println "                            <a href=\"javascript:void(0)\" ms-on-click=\"changExportType(vmtag.exportnew)\">{{@vmtag.exportnew}}</a>"
    out.println "                        </li>"
    out.println "                        <li>"
    out.println "                            <a href=\"javascript:void(0)\" ms-on-click=\"changExportType(vmtag.exportsel)\">{{@vmtag.exportsel}}</a>"
    out.println "                        </li>"
    out.println "                    </ul>"
    out.println "                 </div>"
    out.println "             </div>"
    out.println "          </div>"
    out.println "       </div>"
    out.println "       <table id=\"ml" + className + "_tb\" data-reorderable-columns=\"true\"></table>"
    out.println "    </div>"
    out.println "   </div>"
    out.println "    <style scoped>"
    out.println "    /* bootstrap-select 宽度改为100%自适应 */"
    out.println "    #page_ml$className .bootstrap-select {"
    out.println "        width: 100%;"
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
