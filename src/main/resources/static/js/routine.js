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
                field: 'resourceName',
                title: '资源'
            },
            {
                field: 'rename',
                title: '重名'
            },
            {
                field: 'season',
                title: '剧季'
            },
            {
                field: 'tmdbId',
                templet: function (d) {
                    return '<a style="color: #47bad6" href="https://www.themoviedb.org/tv/' + d.tmdbId + '/season/' + d.season + '">' + d.tmdbId + '</a>';
                },
                title: 'TMDB链接'
            },
            {
                field: 'doubanId',
                templet: function (d) {
                    return '<a style="color: #47bad6" href="https://movie.douban.com/subject/' + d.doubanId + '/">' + d.doubanId + '</a>';
                },
                title: '豆瓣链接'
            },
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
                        var elemSeason = iframeWin.$('#routine-season').val();
                        var elemTmdb = iframeWin.$('#routine-tmdb').val();
                        var elemDouban = iframeWin.$('#routine-douban').val();
                        var elemRename = iframeWin.$('#routine-rename').val();
                        var elemDelmark = iframeWin.$('#routine-delmark').val();
                        var elemPath = iframeWin.$('#routine-path').val();
                        if ($.trim(elemName) === '') return iframeWin.$('#routine-name').focus();
                        if ($.trim(elemSeason) === '') return iframeWin.$('#routine-season').focus();
                        if ($.trim(elemTmdb) === '') return iframeWin.$('#routine-tmdb').focus();
                        if ($.trim(elemDouban) === '') return iframeWin.$('#routine-douban').focus();
                        if ($.trim(elemRename) === '') return iframeWin.$('#routine-rename').focus();
                        if ($.trim(elemPath) === '') return iframeWin.$('#routine-path').focus();
                        // 显示获得的值
                        $.ajax({
                            url: "/routine/add", // 请求的URL
                            type: 'POST', // 请求方法
                            contentType: 'application/json',
                            dataType: 'json', // 返回的数据格式
                            data: JSON.stringify({
                                "name": elemName,
                                "season": elemSeason,
                                "tmdbId": elemTmdb,
                                "doubanId": elemDouban,
                                "rename": elemRename,
                                "delmark": elemDelmark,
                                "path": elemPath
                            }),
                            success: function (res) {
                                if (res.status === 200) {
                                    layer.close(index);
                                    table.reload('test', {where: {},});
                                    layer.msg('新增成功！')
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
                title: '编辑例程',
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
                    var elemSeason = iframeWin.$('#routine-season').val();
                    var elemTmdb = iframeWin.$('#routine-tmdb').val();
                    var elemDouban = iframeWin.$('#routine-douban').val();
                    var elemRename = iframeWin.$('#routine-rename').val();
                    var elemDelmark = iframeWin.$('#routine-delmark').val();
                    if ($.trim(elemName) === '') return iframeWin.$('#routine-name').focus();
                    if ($.trim(elemSeason) === '') return iframeWin.$('#routine-season').focus();
                    if ($.trim(elemTmdb) === '') return iframeWin.$('#routine-tmdb').focus();
                    if ($.trim(elemDouban) === '') return iframeWin.$('#routine-douban').focus();
                    if ($.trim(elemRename) === '') return iframeWin.$('#routine-rename').focus();
                    // 显示获得的值
                    $.ajax({
                        url: "/routine/update", // 请求的URL
                        type: 'POST', // 请求方法
                        contentType: 'application/json',
                        dataType: 'json', // 返回的数据格式
                        data: JSON.stringify({
                            "id": elemId,
                            "name": elemName,
                            "season": elemSeason,
                            "tmdbId": elemTmdb,
                            "doubanId": elemDouban,
                            "rename": elemRename,
                            "delmark": elemDelmark
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
                    body.find('#routine-id').val(obj.data.id);
                    body.find('#routine-name').val(obj.data.name);
                    body.find('#routine-resourceName').val(obj.data.resourceName);
                    body.find('#routine-season').val(obj.data.season);
                    body.find('#routine-tmdb').val(obj.data.tmdbId);
                    body.find('#routine-douban').val(obj.data.doubanId);
                    body.find('#routine-rename').val(obj.data.rename);
                    body.find('#routine-delmark').val(obj.data.delmark);
                    body.find('#routine-path').val(obj.data.path);
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