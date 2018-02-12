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
    new File(dir, "ml"+className + ".js").withPrintWriter { out -> generate(out, className, fields,table.getName()) }
}

def generate(out, className, fields ,tablename) {
    out.println "'use strict';"
    out.println "(function () {"
    out.println "    define(['appi18n', ifhtml + 'xxx/ml"+className+".html'], function (geti18n, pagehtml) {"
    out.println "        // 加载多语言文本数据"
    out.println "        var i18ndata = geti18n('ml$className');"
    out.println "        //\$.fn.editable.defaults.mode = 'inline';"
    out.println "        avalon.component('ms-ml$className', {"
    out.println "            template: pagehtml,"
    out.println "            defaults: {"
    out.println "                // 界面多语言"
    out.println "                vmtag: i18ndata,"
    out.println "                // 表格数据导出文件的类型选择"
    out.println "                exportType: i18ndata.exportall, //basic', 'all', 'selected'."
    out.println "                // 参数对象用于列表查询"
    out.println "                modelParam: {"
    fields.each() {
        out.println "                    $it.name:'',// $it.comment"
    }
    out.println "                    // 以下是分页用缺省字段"
    out.println "                    pageSize: 10,"
    out.println "                    pageNumber: 1,"
    out.println "                    sortName: '',"
    out.println "                    sortOrder: 'asc'"
    out.println "                },"
    out.println "                // 组件初始化"
    out.println "                onReady: function (e) {"
    out.println "                    this.pageInit(); // 必须执行pageInit，禁止删除"
    out.println "                    this.initTable();"
    out.println "                },"
    out.println "                // 组件中，必须初始化的代码"
    out.println "                pageInit: function () {"
    out.println "                    \$('.selectpicker').selectpicker('refresh'); // 组件内使用select，需要主动更新UI来初始化数据"
    out.println "                    \$('.form_datetime').datetimepicker({"
    out.println "                        language: 'zh-CN', // 选择语言"
    out.println "                        format: 'yyyy-mm-dd', // 显示格式"
    out.println "                        todayHighlight: 1, // 今天高亮"
    out.println "                        minView: 'month', // 设置只显示到月份"
    out.println "                        startView: 2,"
    out.println "                        forceParse: 0,"
    out.println "                        showMeridian: 1,"
    out.println "                        autoclose: 1 // 选择后自动关闭"
    out.println "                    });"
    out.println "                },"
    out.println "                // 表格-加载数据"
    out.println "                initTable: function () {"
    out.println "                    var ddate = new Date();"
    out.println "                    var tableExportName = this.vmtag.vmname + '_' + ddate.getFullYear() + (ddate.getMonth() + 1) + ddate.getDate();"
    out.println ""
    out.println "                    var param = {"
    out.println "                        fileName: tableExportName,"
    out.println "                        tablename: '#ml" + className + "_tb',"
    out.println "                        url: '$className/manager',"
    out.println "                        queryParams: this.queryParams,"
    out.println "                        columns: [{"
    out.println "                                      checkbox: true"
    out.println "                                  },"
    def sc = 0
    fields.each() {
        sc = sc+1
        def dh = (sc == fields.size() )? "" : ","
        def vis = it.comment == null? "false":"true"
        out.println "                                  {"
        out.println "                                      field: '$it.name',"
        out.println "                                      title: this.vmtag.$it.name,"
        out.println "                                      //editable: true,"
        out.println "                                      visible: $vis,"
        if (it.type == "datetime"){
            out.println "                                      formatter: function (value, row, index) {return avalon.filters.datehelper(value);},"
        } else if(it.type == "timestamp") {
            out.println "                                      formatter: function (value, row, index) {return avalon.filters.tamp2DateStr(value);},"
        }
        out.println "                                      sortable: true"
        out.println "                                  }$dh"
    }
    out.println "                        ],"
    out.println "                        detailView: false,"
    out.println "                        toolbar: '#ml" + className + "_toolbar'"
    out.println "                    }"
    out.println "                    // #region 初始化table 查询加载"
    out.println "                    // 初始化客户表格"
    out.println "                    var oTable = tableHelper(param);"
    out.println "                    oTable.Init();"
    out.println "                    // #endregion"
    out.println "                },"
    out.println "                // 表格-配置查询参数"
    out.println "                queryParams: function (params) {"
    out.println "                    this.modelParam.pageSize = params.pageSize;"
    out.println "                    this.modelParam.pageNumber = params.pageNumber;"
    out.println "                    this.modelParam.sortName = params.sortName;"
    out.println "                    this.modelParam.sortOrder = params.sortOrder;"
    out.println "                    return this.modelParam;"
    out.println "                },"
    out.println "                // 表格-刷新数据"
    out.println "                refreshTable: function () {"
    out.println "                    \$('#ml"+ className+ "_tb').bootstrapTable('refresh');"
    out.println "                },"
    out.println "                // 表格-更改导出方式"
    out.println "                changExportType: function (types) {"
    out.println "                    this.exportType = types;"
    out.println "                    if (types == this.vmtag.exportall) {"
    out.println "                        \$('#ml"+ className + "_tb').bootstrapTable('refreshOptions', {"
    out.println "                            exportDataType: 'all'"
    out.println "                        });"
    out.println "                    } else if (types == this.vmtag.exportnew) {"
    out.println "                        \$('#ml"+ className + "_tb').bootstrapTable('refreshOptions', {"
    out.println "                            exportDataType: 'basic'"
    out.println "                        });"
    out.println "                    } else if (types == this.vmtag.exportsel) {"
    out.println "                        \$('#ml"+ className +"_tb').bootstrapTable('refreshOptions', {"
    out.println "                            exportDataType: 'selected'"
    out.println "                        });"
    out.println "                    }"
    out.println "                },"
    out.println ""
    out.println "                // 查看明细"
    out.println "                showItem: function () {"
    out.println "                    var data = \$('#ml"+ className +"_tb').bootstrapTable('getAllSelections');"
    out.println "                    if (data.length <= 0 || data.length > 1) {"
    out.println "                        layer.alert(this.vmtag.error.selone, {"
    out.println "                            icon: 0,"
    out.println "                            closeBtn: 1"
    out.println "                        });"
    out.println "                        return;"
    out.println "                    }"
    out.println "                    vmHelper.tabOpen('xxx/ei$className', {"
    out.println "                        title: this.vmtag.di_title"
    out.println "                    }, 'isread=1&id=' + data[0].id);"
    out.println "                },"
    out.println "                // 新增"
    out.println "                addItem: function () {"
    out.println "                    vmHelper.tabOpen('xxx/ei$className', {"
    out.println "                        title: this.vmtag.ei_title"
    out.println "                    });"
    out.println "                },"
    out.println "                // 编辑"
    out.println "                editItem: function () {"
    out.println "                    var data = \$('#ml" + className + "_tb').bootstrapTable('getAllSelections');"
    out.println "                    if (data.length <= 0 || data.length > 1) {"
    out.println "                        layer.alert(this.vmtag.error.selone, {"
    out.println "                            icon: 0,"
    out.println "                            closeBtn: 1"
    out.println "                        });"
    out.println "                        return;"
    out.println "                    }"
    out.println "                    vmHelper.tabOpen('xxx/ei$className', {"
    out.println "                        title: this.vmtag.ei_title2"
    out.println "                    }, 'id=' + data[0].id);"
    out.println "                },"
    out.println "                // 删除"
    out.println "                delItem: function () {"
    out.println "                    var data = \$('#ml" + className + "_tb').bootstrapTable('getAllSelections');"
    out.println "                    if (data.length < 1 || data.length > 1) {"
    out.println "                        layer.alert(this.vmtag.error.selone, {"
    out.println "                            icon: 0,"
    out.println "                            closeBtn: 1"
    out.println "                        });"
    out.println "                        return;"
    out.println "                    }"
    out.println "                    var idList = jslinq(data).select(function (p) {"
    out.println "                        return p.id"
    out.println "                    }).toList();"
    out.println ""
    out.println "                    var tempvm = this;"
    out.println "                    fH_json('$className/del', {"
    out.println "                        itemIdList: idList"
    out.println "                    }, function (data) {"
    out.println "                        layer.alert(data);"
    out.println "                        tempvm.refreshTable();"
    out.println "                    }, 'post');"
    out.println "                },"
    out.println "                // 编辑界面保存后回调列表界面函数（该函数为样例）"
    out.println "                itemCallback: function () {"
    out.println "                    this.refreshTable();"
    out.println "                }"
    out.println "            }"
    out.println "        })"
    out.println "    })"
    out.println "})();"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        fields += [[
                           name : javaName(col.getName(), false),
                           colname : col.getName(),
                           type : typeStr,
                           comment: col.comment,
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