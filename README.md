# Auto Ping 简单介绍 


## 运行程序的目录结构
    
    [bin]           # 运行脚本，包括 window 和 linux 脚本
        autoping        # Linux 的运行脚本
        autoping.bat    # Window 的运行脚本
    [jars]          # 依赖了3个jar 文件, 
        autoping.jar    # 程序的主逻辑
        nutz.jar        # 通用程序库
        log4j.jar       # 日志库
    [conf]
        autoping.properties   # 程序的配置文件
        log4j.properties      # 日志格式的文件

## 如何运行
    
> 请先确保 Java 已经被正确安装了

> 下载程序后，你可以解压到你的任意目录，不过你需要修改运行脚本
> 的一个变量，用来指明自己的安装路径。

如果你是 Linux 用户，打开 bin/autoping 文件，修改

    AUTOPING=~/tmp/autoping
    JAVA_RT=~/opt/java/jdk/jre/lib/rt.jar

这两个环境变量，AUTOPING 就写你解压的目录，"JAVA\_RT" 你需要指明
Java 的运行库位置，通常在你安装的 JDK 或者 JRE 的 lib 目录下就能找到

如果你是 Woindows 用户，请打开 bin/autoping.bat，进行相应修改

然后你需要修改配置文件，具体如何修改，请参看下面一节
> 注意!! 尤其重要的修改 "data-home" 这个选项
> 但是 windows 用户主要不要把路径的斜杠写反了，要用 / 来分隔，不要用 \

## 如何修改配置文件

下面给出 conf/autoping.properties 文件的具体字段的含义

    #----------------------------------------------------------
    # 下面三个域名将被定期 ping，一行一个，可以无数个
    # 不过太多，你内存可能会爆掉
    hosts:
    wx.redatoms.com
    sg.redatoms.com
    wsa.sg13-bj.redatoms.com
    #~ End of hosts
    #----------------------------------------------------------
    # 多长时间监视一次，5 表示五分钟
    watch-interval=5
    #----------------------------------------------------------
    # 在 ping 期间，主线程会不断检查是否所有 ping 都返回了
    # 这个检查稍微有点耗资源，因此建议 3 秒检查一次
    check-interval=3
    #----------------------------------------------------------
    # 结果文件存储的主目录
    data-home=~/tmp/autoping
    #----------------------------------------------------------
    # 下面这些属性是用来解析用的正则表达式啥的，请不要修改
    #----------------------------------------------------------
    # the URL pattern for get ping
    url-prefix=http://cloudmonitor.ca.com/en/
    url-ping=ping.php?varghost=${host}&vhost=_&vaction=ping&ping=start
    #----------------------------------------------------------
    p-ajax-prefix=xmlreqGET('api/pingproxy.php?
    p-ajax=^(xmlreqGET[(]')(api/pingproxy[.]php.*)('.*')([0-9]+)(.*)$
    #----------------------------------------------------------
    p-td-prefix=<td class="right-dotted-border"
    p-td:
    ^(.*<td class="right-dotted-border".*><span id="cp)([0-9]+)(">Checking...</span>.*)$
    #~ End p-td
    #----------------------------------------------------------
    p-title:
    ^(<.*>)(.*)(:)(</td>.*)$
    #~ End p-title

