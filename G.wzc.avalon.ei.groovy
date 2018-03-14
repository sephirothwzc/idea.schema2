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
        (~/(?i)timestamp/)                : "timestamp",
        (~/(?i)datetime/)                 : "datetime",
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
    new File(dir, "ei" + className + ".js").withPrintWriter { out -> generate(out, className, fields, table.getName()) }
}

def generate(out, className, fields, tablename) {
    out.println "    'use strict'(function() {"
    out.println "        define(["
    out.println "                'appi18n',"
    out.println "                ifhtml + 'apfj/" + className + ".html'"
    out.println "        ], function(geti18n, pagehtml) {"
    out.println "            // 获取多语言设置"
    out.println "            var i18ndata = geti18n('ei$className')"
    out.println "            var vmid = 'ms-ei$className'"
    out.println "            avalon.component(vmid, {"
    out.println "                template: pagehtml,"
    out.println "                defaults: {"
    out.println "                    // 多语言设置"
    out.println "                    vmtag: i18ndata,"
    out.println "                    // 表单model对象"
    out.println "                    vmmodel: {"
    def sc = 0
    fields.each() {
        sc = sc + 1
        def dh = (sc == fields.size()) ? "" : ","
        out.println "                        $it.name:''$dh// $it.comment"
    }
    out.println "                    },"
    out.println "                    // form参数设置是否只读 单元素对象只支持属性赋值"
    out.println "                    isread: {},"
    out.println "                    // 校验数据的准确性默认异常"
    out.println "                    hasErrList: {"
    out.println "                        // userName: 'has-error'"
    out.println "                    },"
    out.println "                    // form校验"
    out.println "                    validate: {"
    out.println "                        onError: function(reasons) {"
    out.println "                            var tempvm = vmHelper.getVM(vmid)"
    out.println "                            reasons.forEach(function(reason) {"
    out.println "                                tempvm.hasErrList[reason.element.id] = 'has-error'"
    out.println "                                reason.element.title = reason.getMessage()"
    out.println "                                avalon.log(reason.getMessage())"
    out.println "                            })"
    out.println "                        },"
    out.println "                        onSuccess: function(reasons) {"
    out.println "                            reasons.forEach(function(reason) {"
    out.println "                                var tempvm = vmHelper.getVM(vmid)"
    out.println "                                tempvm.hasErrList[reason.element.id] = ''"
    out.println "                            })"
    out.println "                        },"
    out.println "                        onValidateAll: function(reasons) {"
    out.println "                            if (reasons.length) {"
    out.println "                                var msg = jslinq(reasons)"
    out.println "                                        .select(function(p) {"
    out.println "                                    return p.getMessage()"
    out.println "                                })"
    out.println "                                        .toList()"
    out.println "                                        .toString()"
    out.println "                                avalon.log('表单没有通过:' + msg)"
    out.println "                            } else {"
    out.println "                                vmHelper.getVM(vmid).save()"
    out.println "                            }"
    out.println "                        }"
    out.println "                    },"
    out.println "                    // 初始化"
    out.println "                    onReady: function(e) {"
    out.println "                        this.pageInit() // 必须执行pageInit，禁止删除"
    out.println "                        // 获取URL参数"
    out.println "                        var query = vmHelper.getUrlQuery()"
    out.println "                        var tempvm = this"
    out.println "                        if (!query.id) return"
    out.println "                        // 根据id返回数组字典对象 如果id为空则返回结构"
    out.println "                        fH_json({"
    out.println "                            url: '$className/item/' + query.id,"
    out.println "                            success: function(data) {"
    out.println "                                tempvm.vmmodel = data"
    out.println "                                // 取消默认显示校验错误效果"
    out.println "                                for (var value in tempvm.hasErrList) {"
    out.println "                                    tempvm.hasErrList[value] = ''"
    out.println "                                }"
    out.println "                            }"
    out.println "                        })"
    out.println "                        // 界面只读，按钮隐藏"
    out.println "                        tempvm.isread = query.isread == 1 ? { readonly: true } : {}"
    out.println "                    },"
    out.println "                    // 组件中，必须初始化的代码"
    out.println "                    pageInit: function() {"
    out.println "                        \$('.selectpicker').selectpicker('refresh') // 组件内使用select，需要主动更新UI来初始化数据"
    out.println "                        \$('.form_datetime').datetimepicker({"
    out.println "                            language: 'zh-CN', // 选择语言"
    out.println "                            format: 'yyyy-mm-dd', // 显示格式"
    out.println "                            todayHighlight: 1, // 今天高亮"
    out.println "                            minView: 'month', // 设置只显示到月份"
    out.println "                            startView: 2,"
    out.println "                            forceParse: 0,"
    out.println "                            showMeridian: 1,"
    out.println "                            autoclose: 1 // 选择后自动关闭"
    out.println "                        })"
    out.println "                    },"
    out.println "                    // 保存数据"
    out.println "                    save: function() {"
    out.println "                        var tempvm = this"
    out.println "                        fH_json({"
    out.println "                            url: '$className/save',"
    out.println "                            data: tempvm.vmmodel,"
    out.println "                            success: function(data) {"
    out.println "                                layer.alert(tempvm.vmtag.savescuess)"
    out.println "                                tempvm.vmmodel = data"
    out.println "                                tempvm.closeTab()"
    out.println "                                // 刷新父页列表数据"
    out.println "                                var pvm = vmHelper.getVM('ms-ml$className')"
    out.println "                                if (pvm) pvm.itemCallback()"
    out.println "                            }"
    out.println "                        })"
    out.println "                    },"
    out.println "                    // 关闭当前页签"
    out.println "                    closeTab: function() {"
    out.println "                        vmHelper.tabClose('ei$className')"
    out.println "                    }"
    out.println "                }"
    out.println "            })"
    out.println "        })"
    out.println "    })()"
    out.println ""
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        fields += [[
                           name   : javaName(col.getName(), false),
                           colname: col.getName(),
                           type   : typeStr,
                           comment: col.comment,
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
