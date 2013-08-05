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

> 如果你是 Linux 用户，打开 bin/autoping 文件，修改

    AUTOPING=~/tmp/autoping
    JAVA_RT=~/opt/java/jdk/jre/lib/rt.jar

> 这两个环境变量，AUTOPING 就写你解压的目录，"JAVA__RT" 你需要
