layui.use(['table', 'dropdown'], function () {
    var table = layui.table;
    var dropdown = layui.dropdown;
    // 创建渲染实例
    table.render({
        title: '例程文件',
        elem: '#test',
        url: '/execute/list',
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
                field: 'routineName',
                width: 210,
                maxWidth: 250,
                title: '例程'
            },
            {
                field: 'status',
                width: 80,
                maxWidth: 80,
                title: '状态'
            },
            {
                field: 'start',
                title: '开始'
            },
            {
                field: 'end',
                title: '结束'
            },
            {
                fixed: 'right', title: '操作',
                width: 150,
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
        }
        ;
    });


    // 行的删除操作
    table.on('tool(test)', function (obj) { // 双击 toolDouble
        var data = obj.data; // 获得当前行数据
        var $ = layui.jquery;
        var layer = layui.layer;
        if (obj.event === 'delete') {
            layer.confirm('真的删除 [' + data.id + ' -' + data.routineName + '] 么', function (index) {
                // 向服务端发送删除指令
                $.ajax({
                    url: "/execute/delete/" + data.id, // 请求的URL
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