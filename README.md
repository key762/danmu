# 简介 | danmu

> 此项目是为了保存在线弹幕为ass文件,实现离线看剧也有弹幕。
> 目前支持腾讯视频，可用于JELLYFIN,EMBY,PLEX等等。
> 支持定时任务以及弹幕ass自定义命名,支持日期函数。


# 配置
> 配置说明如下,使用下边的连接查看启用的定时任务
> http://127.0.0.1:8099/job
```yaml
server:
  port: 8099
task:
  danmu:
    - desc: 庆余年第一季 #任务描述
      enable: false #是否启用
      immediately: false  #启动后是否立即执行一次
      cron: 5 * * * * ? #定时任务
      delMark: 腾讯弹幕 #删除旧的ass文件包含的标志,为空则不删除
      reName: 腾讯弹幕($@{FormatDate(${time},yyyy-MM-dd)}) #用于保存处理完成后的文件名,${time}标识当前时间,FormatDate时间函数
      source: #腾讯视频的播放页面,如果多页就需要配多个URL
        - https://v.qq.com/x/cover/rjae621myqca41h/i0032qxbi2v.html
        - https://v.qq.com/x/cover/rjae621myqca41h/v0033usud3k.html
      path: C:\Users\zxp21\Videos\弹幕\庆余年 (2019)\Season 1 #保存到本地的路径
```