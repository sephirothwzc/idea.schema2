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
packageName = "com.sephiroth.jpademo.jpadao;"
beforName = "JPA_"
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
    new File(dir, beforName + className + ".json").withPrintWriter { out -> generate(out, className, fields,table.getName()) }
}

def generate(out, className, fields ,tablename) {
    out.println "'use strict'"
    out.println ""
    // i18n映射 ml
    out.println "  /**\n" +
            "  * @Author: 吴占超\n" +
            "  * @Description: JSON\n" +
            "  * @Date: Create in \n" +
            "  * @Modified By:\n" +
            "  */"
    out.println "  var ml$className = {"
    out.println "    /* ml界面 */"
    out.println "    error_selone: '请选择一条记录',"
    out.println "    msg_saved: '保存成功',"
    out.println "    exportall: '导出格式(所有)',"
    out.println "    exportnew: '导出格式(本页)',"
    out.println "    exportsel: '导出格式(选中)',"
    out.println "    vmname: '页面描述，自己修改',"
    out.println "    ai_title: '新增页面描述，自己修改',"
    out.println "    ei_title: '编辑页面描述，自己修改',"
    out.println "    di_title: '查看页面描述，自己修改',"
    out.println "    add: '新增',"
    out.println "    upd: '修改',"
    out.println "    del: '删除',"
    out.println "    look: '查看',"
    out.println "    btnquery: '查询',"
    out.println "    btnreset: '重置',"

    out.println "    /* fields */"
    def s = 0
    fields.each() {
        s = s+1
        def str = (s == fields.size() )? "    $it.name : '$it.comment'" : "    $it.name : '$it.comment',"
        out.println str
    }
    out.println "  }"
    out.println ""
    // i18n映射 ei
    out.println "  /**\n" +
            "  * @Author: 吴占超\n" +
            "  * @Description: JSON\n" +
            "  * @Date: Create in \n" +
            "  * @Modified By:\n" +
            "  */"
    out.println "  var ei$className = {"
    out.println "    /* ei界面 */"
    out.println "    btnsave: '保存',"
    out.println "    btncancel: '取消',"
    out.println "    savescuess: '保存成功！'"

    out.println "    /* fields */"
    s = 0
    fields.each() {
        s = s+1
        def str = (s == fields.size() )? "    $it.name : '$it.comment'" : "    $it.name : '$it.comment',"
        out.println str
    }
    out.println "  }"
    out.println ""
    out.println "columns = {"
    sc = 0
    fields.each() {
        def str = """{
    field: '$it.name',
    title: this.vmtag.$it.name,
    //editable: true,
    """
        def vis = it.comment == null ? "visible: false," : ""
        str = str + vis
        str = str + "    sortable: true }"
        sc = sc+1
        def dh = (sc == fields.size() )? "" : ","
        out.println str + dh
    }
    out.println "}"
    out.println ""

    // 生成 mock 列表管理
    out.println "    // #region $className"
    out.println "    ms.mock('$className/manager', function (params) {"
    out.println "        var fields = JSON.parse(params.body);"
    out.println "        var dttime = '/Date('+new Date().getTime()+')/';"
    out.println "        return ms.mock({"
    out.println "            'total': 100,"
    out.println "            'rows|10': [{"
    out.println "                            'id|+1': 1, //主键guid"
    sc = 0
    fields.each() {
        if (it.name != 'id') {
            def cmt = "'"+it.comment+"'"
            if(it.type == "java.sql.Timestamp" || it.type == "java.sql.Date" || it.type == "java.sql.Time") {
                cmt = "dttime"
            }
        def str = "                            '$it.name': fields.$it.name != undefined && fields.$it.name != '' ? fields.$it.name : $cmt"
            sc = sc+1
            def dh = (sc == fields.size() )? "" : ","
            out.println str + dh + " // $it.comment"
        }
    }
    out.println "                        }]"
    out.println "        });"
    out.println "    });"
    out.println "    // #endregion $className"
    // 生成 mock 编辑管理
    out.println "    // #region $className edit"
    out.println "    ms.mock('$className/item', function (params) {"
    out.println "        var fields = params.body;"
    out.println "        return ms.mock({"
    out.println "            'state': 1,"
    out.println "            'data': {"
    sc = 0
    fields.each() {
        def str = "                            '$it.name': '$it.comment'"
        sc = sc+1
        def dh = (sc == fields.size() )? "" : ","
        out.println str + dh + " // $it.comment"
    }
    out.println "                        }"
    out.println "        });"
    out.println "    });"
    out.println "    // #endregion $className edit"
    // 生成 mock 保存管理
    out.println "    // #region $className save"
    out.println "    ms.mock('$className/save', function (params) {"
    out.println "        var fields = params.body;"
    out.println "        return ms.mock({"
    out.println "            'state': 1,"
    out.println "            'data': {"
    sc = 0
    fields.each() {
        def str = "                            '$it.name': '$it.comment'"
        sc = sc+1
        def dh = (sc == fields.size() )? "" : ","
        out.println str + dh + " // $it.comment"
    }
    out.println "                        }"
    out.println "        });"
    out.println "    });"
    out.println "    // #endregion $className save"
    out.println ""
    // 生成 mock 删除管理
    out.println "    // #region $className del"
    out.println "    ms.mock('$className/del', function (params) {"
    out.println "        var fields = params.body;"
    out.println "        return ms.mock({"
    out.println "            'state': 1,"
    out.println "            'data': '删除成功'"
    out.println "        });"
    out.println "    });"
    out.println "    // #endregion $className del"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        fields += [[
                           name : javaName(col.getName(), false),
                           colname : col.getName(),
                           type : typeStr,
                           comment : col.comment,
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
