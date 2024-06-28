layui.use(['table', 'dropdown'], function () {
    var table = layui.table;
    var dropdown = layui.dropdown;
    // 创建渲染实例
    table.render({
        title: '资源文件',
        elem: '#test',
        url: '/resource/list',
        toolbar: '#toolbarDemo',
        defaultToolbar: ['filter', 'exports', 'print'],
        scrollPos: 'fixed', // 最大高度减去其他容器已占有的高度差
        css: [ // 重设当前表格样式
            '.layui-table-tool-temp{padding-right: 0px;}'
        ].join(''),
        cellMinWidth: 120,
        skin: 'nob',
        even: true,
        // size: 'lg',
        response: {
            statusCode: 200
        },
        page: true,
        parseData: function (res) {
            return {
                "code": res.status, //解析接口状态
                "msg": res.message, //解析提示文本
                "count": res.data.total, //解析数据长度
                "data": res.data.records //解析数据列表
            };
        },
        cols: [[
            {
                field: 'id', fixed: 'left',
                width: 120,
                maxWidth: 150,
                title: '编码', sort: true
            },
            {
                field: 'name',
                width: 180,
                maxWidth: 200,
                title: '名称'
            },
            {
                field: 'path',
                title: '路径'
            },
            {
                fixed: 'right', title: '操作',
                width: 160,
                minWidth: 150,
                toolbar: '#barDemo'
            }
        ]],
        error: function (res, msg) {
            console.log(res, msg)
        }
    });

    // 工具栏事件 刷新和新增
    table.on('toolbar(test)', function (obj) {
        var id = obj.config.id;
        var checkStatus = table.checkStatus(id);
        var othis = lay(this);
        var layer = layui.layer;
        switch (obj.event) {
            case 'getCheckData':
                // 刷新
                table.reload('test', {where: {},});
                layer.msg('刷新成功！');
                break;
            case 'getData':
                var $ = layui.$;
                var layer = layui.layer;
                // 新增
                layer.open({
                    title: '新增资源',
                    type: 2,
                    area: ['60%', '60%'],
                    content: '/resource/add.html',
                    fixed: false, // 不固定
                    maxmin: true,
                    shadeClose: true,
                    btn: ['新增', '取消'],
                    btnAlign: 'c',
                    yes: function (index, layero) {
                        var iframeWin = window[layero.find('iframe')[0]['name']];
                        // 获取输入数据
                        var elemName = iframeWin.$('#resource-name');
                        var elemPath = iframeWin.$('#resource-path');
                        var name = elemName.val();
                        var path = elemPath.val();
                        // 空值检查
                        if ($.trim(name) === '') return elemName.focus();
                        if ($.trim(path) === '') return elemPath.focus();
                        // 调取新增
                        $.ajax({
                            url: "/resource/add", // 请求的URL
                            type: 'POST', // 请求方法
                            dataType: 'json', // 返回的数据格式
                            data: {"name": name, "path": path},
                            success: function (res) {
                                if (res.status === 200) {
                                    layer.close(index);
                                    table.reload('test', {where: {},});
                                    layer.msg('新增成功！');
                                } else {
                                    layer.msg(res.message);
                                }
                            },
                            error: function () {
                                layer.msg('新增失败！');
                            }
                        });
                    }
                });
                break;
        }
        ;
    });


    // 行的删除操作
    table.on('tool(test)', function (obj) { // 双击 toolDouble
        var data = obj.data; // 获得当前行数据
        var $ = layui.jquery;
        var layer = layui.layer;
        if (obj.event === 'delete') {
            layer.confirm('真的删除 [' + data.name + '] 么', function (index) {
                // 向服务端发送删除指令
                $.ajax({
                    url: "/resource/delete/" + data.id, // 请求的URL
                    type: 'GET', // 请求方法
                    dataType: 'json', // 返回的数据格式
                    success: function (res) {
                        if (res.status === 200) {
                            obj.del(); // 删除对应行（tr）的DOM结构
                            layer.close(index);
                            var table = layui.table;
                            table.reload('test', {where: {},});
                            layer.msg('删除成功！');
                        } else {
                            layer.msg(res.message);
                        }
                    },
                    error: function () {
                        layer.msg('删除失败！');
                    }
                });
            });
        } else if (obj.event === 'analysis') {
            layer.confirm('真的解析 [' + data.name + '] 么', function (index) {
                // 向服务端发送删除指令
                $.ajax({
                    url: "/resource/analysis/" + data.id, // 请求的URL
                    type: 'GET', // 请求方法
                    dataType: 'json', // 返回的数据格式
                    success: function (res) {
                        if (res.status === 200) {
                            layer.close(index);
                            layer.msg('资源解析中！');
                        } else {
                            layer.msg(res.message);
                        }
                    },
                    error: function () {
                        layer.msg('解析失败！');
                    }
                });
            });
        } else if (obj.event === 'update') {
            // 编辑
            layer.open({
                title: '编辑资源',
                type: 2,
                area: ['60%', '60%'],
                content: '/resource/update.html',
                fixed: false, // 不固定
                maxmin: true,
                shadeClose: true,
                btn: ['编辑', '取消'],
                btnAlign: 'c',
                yes: function (index, layero) {
                    var iframeWin = window[layero.find('iframe')[0]['name']];
                    var elemId = iframeWin.$('#resource-id');
                    var elemName = iframeWin.$('#resource-name');
                    var elemPath = iframeWin.$('#resource-path');
                    var id = elemId.val();
                    var name = elemName.val();
                    var path = elemPath.val();
                    if ($.trim(name) === '') return elemName.focus();
                    if ($.trim(path) === '') return elemPath.focus();
                    // 显示获得的值
                    $.ajax({
                        url: "/resource/update", // 请求的URL
                        type: 'POST', // 请求方法
                        dataType: 'json', // 返回的数据格式
                        data: {"id": id, "name": name},
                        success: function (res) {
                            if (res.status === 200) {
                                layer.close(index);
                                table.reload('test', {where: {},});
                                layer.msg('编辑成功！')
                            } else {
                                layer.msg(res.message);
                                elemName.focus()
                            }
                        },
                        error: function () {
                            layer.msg('编辑失败！');
                        }
                    });
                },
                success: function (layero, index, that) {
                    let body = layer.getChildFrame('body', index);
                    body.find('#resource-id').val(obj.data.id);
                    body.find('#resource-name').val(obj.data.name);
                    body.find('#resource-path').val(obj.data.path);
                }
            });
        }
    });
});