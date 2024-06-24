layui.use(['table', 'dropdown'], function () {
    var table = layui.table;
    var dropdown = layui.dropdown;
    // 创建渲染实例
    table.render({
        title: '例程文件',
        elem: '#test',
        url: '/routine/list',
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
                width: 80,
                maxWidth: 150,
                title: '编码', sort: true
            },
            {
                field: 'name',
                width: 210,
                maxWidth: 250,
                title: '名称'
            },
            {
                field: 'type',
                title: '平台',
                templet: '#tranType'
            },
            {
                field: 'resourceName',
                title: '资源'
            },
            // {
            //     field: 'source',
            //     title: '播源'
            // },
            {
                field: 'rename',
                title: '重名'
            },
            {
                field: 'delmark',
                title: '删标'
            },
            // {
            //     field: 'path',
            //     title: '路径'
            // },
            {
                fixed: 'right', title: '操作',
                width: 165,
                minWidth: 165,
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
                    title: '新增例程',
                    type: 2,
                    area: ['60%', '90%'],
                    content: '/routine/add.html',
                    fixed: false, // 不固定
                    maxmin: true,
                    shadeClose: true,
                    btn: ['新增', '取消'],
                    btnAlign: 'c',
                    yes: function (index, layero) {
                        var iframeWin = window[layero.find('iframe')[0]['name']];
                        var elemName = iframeWin.$('#routine-name').val();
                        var elemType = iframeWin.$('#routine-type').val();
                        const elemSource = Array.from(iframeWin.$('#add-text input')).map(input => input.value);
                        var elemRename = iframeWin.$('#routine-rename').val();
                        var elemDelmark = iframeWin.$('#routine-delmark').val();
                        var elemResource = iframeWin.$('#routine-select').val();
                        var elemPath = iframeWin.$('#routine-path').val();
                        if ($.trim(elemName) === '') return iframeWin.$('#routine-name').focus();
                        if ($.trim(elemRename) === '') return iframeWin.$('#routine-rename').focus();
                        if ($.trim(elemResource) === '') return iframeWin.$('#routine-select').focus();
                        // 显示获得的值
                        $.ajax({
                            url: "/routine/check", // 请求的URL
                            type: 'POST', // 请求方法
                            contentType: 'application/json',
                            dataType: 'json', // 返回的数据格式
                            data: JSON.stringify({
                                "name": elemName, "delmark": elemDelmark, "rename": elemRename,
                                "source": JSON.stringify(elemSource), "path": elemPath, "resource": elemResource,
                                "type": elemType,
                            }),
                            success: function (res) {
                                if (res.status === 200) {
                                    $.ajax({
                                        url: "/routine/add", // 请求的URL
                                        type: 'POST', // 请求方法
                                        contentType: 'application/json',
                                        dataType: 'json', // 返回的数据格式
                                        data: JSON.stringify({
                                            "name": elemName,
                                            "delmark": elemDelmark,
                                            "rename": elemRename,
                                            "source": JSON.stringify(elemSource),
                                            "path": elemPath,
                                            "resource": elemResource,
                                            "type": elemType,
                                        }),
                                        success: function (res) {
                                            layer.close(index);
                                            table.reload('test', {where: {},});
                                            layer.msg('新增成功！');
                                        },
                                        error: function () {
                                            layer.msg('新增失败！');
                                        }
                                    });
                                    table.reload('test', {where: {},});
                                    // 关闭弹层
                                    layer.close(index);
                                } else {
                                    layer.msg(res.message);
                                    elemName.focus()
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
                    url: "/routine/delete/" + data.id, // 请求的URL
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
        } else if (obj.event === 'update') {
            // 编辑
            layer.open({
                title: '编辑资源',
                type: 2,
                area: ['60%', '95%'],
                content: '/routine/update.html',
                fixed: false, // 不固定
                maxmin: true,
                shadeClose: true,
                btn: ['编辑', '取消'],
                btnAlign: 'c',
                yes: function (index, layero) {
                    var iframeWin = window[layero.find('iframe')[0]['name']];
                    var elemId = iframeWin.$('#routine-id').val();
                    var elemName = iframeWin.$('#routine-name').val();
                    var elemType = iframeWin.$('#routine-type').val();
                    const elemSource = Array.from(iframeWin.$('#add-text input')).map(input => input.value);
                    var elemRename = iframeWin.$('#routine-rename').val();
                    var elemDelmark = iframeWin.$('#routine-delmark').val();
                    var elemResource = iframeWin.$('#routine-select').val();
                    var elemPath = iframeWin.$('#routine-path').val();
                    if ($.trim(elemName) === '') return iframeWin.$('#routine-name').focus();
                    if ($.trim(elemRename) === '') return iframeWin.$('#routine-rename').focus();
                    if ($.trim(elemResource) === '') return iframeWin.$('#routine-select').focus();
                    // 显示获得的值
                    $.ajax({
                        url: "/routine/update", // 请求的URL
                        type: 'POST', // 请求方法
                        contentType: 'application/json',
                        dataType: 'json', // 返回的数据格式
                        data: JSON.stringify({
                            "id": elemId, "name": elemName, "delmark": elemDelmark, "rename": elemRename,
                            "source": JSON.stringify(elemSource), "path": elemPath, "resource": elemResource,
                            "type": elemType,
                        }),
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
                    //得到iframe页的窗口对象
                    var iframeWin = window[layero.find('iframe')[0]['name']];
                    //执行iframe页的showMsg方法
                    iframeWin.loadSubFolders(obj.data.resourcePath + obj.data.path);
                    body.find('#routine-id').val(obj.data.id);
                    body.find('#routine-name').val(obj.data.name);
                    body.find('#routine-type').val(obj.data.type);
                    let sourceArray = JSON.parse(obj.data.source);
                    sourceArray.forEach((element, index) => {
                        var sourceStr = sourceArray[index];
                        if (index === 0) {
                            body.find('input.folder-select-style[name="data[]"]').val(sourceStr);
                        } else {
                            iframeWin.addRowStr(sourceStr);
                        }
                    });
                    body.find('#routine-rename').val(obj.data.rename);
                    body.find('#routine-delmark').val(obj.data.delmark);
                    body.find('#routine-select').val(obj.data.resource);
                    body.find('#routine-path').val(obj.data.resourcePath + obj.data.path);

                }
            });
        } else if (obj.event === 'execute') {
            layer.confirm('立即执行 [' + data.name + '] 么', function (index) {
                // 向服务端发送删除指令
                $.ajax({
                    url: "/routine/execute/" + data.id, // 请求的URL
                    type: 'GET', // 请求方法
                    dataType: 'json', // 返回的数据格式
                    success: function (res) {
                        if (res.status === 200) {
                            layer.close(index);
                            layer.msg(res.message, {time: 4000});
                        } else {
                            layer.msg(res.message);
                        }
                    },
                    error: function () {
                        layer.msg('执行失败！');
                    }
                });
            });
        }
    });
});