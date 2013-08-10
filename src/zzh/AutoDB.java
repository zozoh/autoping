package zzh;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.castor.Castors;
import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;
import org.nutz.dao.impl.SimpleDataSource;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.Disks;
import org.nutz.lang.util.FileVisitor;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * 把 AutoPing 生成的 CSV 文件统统加入到数据表中
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AutoDB {

    private static final Log log = Logs.get();

    private static SimpleDateFormat DF = new SimpleDateFormat("yyMMdd_HHmmss");

    /**
     * 主要的业务逻辑，循环 dataHome 里的 csv 文件
     * 
     * @param dao
     * @param dataHome
     * @param count
     */
    public static void exec(final Dao dao, File dataHome, final int[] count) {
        // 访问数据目录
        Disks.visitFile(dataHome, new FileVisitor() {

            /**
             * 从给定位置读取一个字符串，一直读取到 " 或者 ' 结束。 这取决与 off 指定的字符是什么
             * 
             * @param cs
             *            字符串数组
             * @param off
             *            开始的读取的位置
             * @return 读取结束的位置 (指向结束的 " 或者 ')
             */
            private int findStrEnd(char[] cs, int off) {
                char c = cs[off];
                for (int i = off + 1; i < cs.length; i++) {
                    if (cs[i] == c)
                        return i;
                }
                return cs.length;
            }

            private int findStrBegin(char[] cs, int off) {
                for (int i = off; i < cs.length; i++) {
                    if (cs[i] == '"' || cs[i] == '\'') {
                        return i;
                    }
                }
                return -1;
            }

            public void visit(File f) {
                // 仅处理 csv 文件
                if (!f.getName().endsWith(".csv"))
                    return;

                // 根据文件名解析 host 和 时间
                String str = Files.getMajorName(f);
                int pos = str.indexOf('_');
                String host = str.substring(0, pos);
                Date tm = Times.parseWithoutException(DF, str.substring(pos + 1));
                Timestamp pingTime = new Timestamp(tm.getTime());
                log.debugf("process : %s : %s", host, Castors.me().castToString(tm));

                // 解析文件内容
                BufferedReader br = Streams.buffr(Streams.fileInr(f));
                String line = null;
                try {
                    while (null != (line = br.readLine())) {
                        // 忽略空行
                        if (Strings.isBlank(line))
                            continue;
                        // 忽略无效记录
                        if (line.startsWith("--,"))
                            continue;
                        // 开始解析
                        char[] cs = line.toCharArray();
                        List<String> cols = new ArrayList<String>();
                        int off = this.findStrBegin(cs, 0);
                        while (off != -1) {
                            int end = this.findStrEnd(cs, off);
                            String col = new String(cs, off + 1, end - off - 1);
                            cols.add(col);
                            off = this.findStrBegin(cs, end + 1);
                        }
                        if (cols.isEmpty())
                            continue;
                        // 生成 POJO
                        AutoObj obj = new AutoObj();
                        obj.setHost(host);
                        obj.setPingTime(pingTime);
                        obj.setCheckpoint(cols.get(0));
                        obj.setResult(cols.get(1));
                        obj.setMinRtt(Castors.me()
                                             .castTo(cols.get(2).replace(",", ""), float.class));
                        obj.setAvgRtt(Castors.me()
                                             .castTo(cols.get(3).replace(",", ""), float.class));
                        obj.setMaxRtt(Castors.me()
                                             .castTo(cols.get(4).replace(",", ""), float.class));
                        obj.setIpv4(cols.get(5));

                        // System.out.println(Json.toJson(obj,
                        // JsonFormat.compact()));

                        // 记录总数
                        count[0]++;
                        // 没有记录，那么添加
                        if (dao.fetchx(AutoObj.class,
                                       obj.getHost(),
                                       obj.getPingTime(),
                                       obj.getCheckpoint()) == null) {
                            obj.setInsertMs(System.currentTimeMillis());
                            obj.setInsertTime(new Timestamp(obj.getInsertMs()));
                            dao.insert(obj);
                            System.out.printf(" +%d:%d+ %s\n",
                                              count[0],
                                              count[1],
                                              Json.toJson(obj, JsonFormat.compact()));
                            // 记录增加数
                            count[1]++;
                        }
                        // 否则打印一下
                        else {
                            System.out.printf(" !!skip %d:%d!! %s\n",
                                              count[0],
                                              count[1],
                                              Json.toJson(obj, JsonFormat.compact()));
                        }

                    }
                }
                catch (IOException e) {
                    throw Lang.wrapThrow(e);
                }

            }
        }, null);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        // 获取配置信息
        log.info("load properties file ...");
        String confPath = args.length > 0 ? args[0] : "autodb.properties";
        PropertiesProxy pp = new PropertiesProxy(confPath);

        // 获取数据目录
        File dataHome = Files.findFile(pp.get("data-home"));
        if (dataHome == null || !dataHome.isDirectory()) {
            log.fatalf("fail to found data-home: %s", pp.get("data-home"));
            System.exit(0);
        }

        // 读取并创建 datasource 和 dao
        log.info("create Dao");
        SimpleDataSource ds = new SimpleDataSource();
        ds.setJdbcUrl(pp.get("db-url"));
        ds.setUsername(pp.get("db-username"));
        ds.setPassword(pp.get("db-password"));

        Dao dao = new NutDao(ds);

        // 检查数据表
        dao.create(AutoObj.class, false);

        // 执行业务操作
        try {
            int[] count = new int[2];
            exec(dao, dataHome, count);
            log.infof("walk %d line, add new %d records", count[0], count[1]);
        }
        catch (Exception e) {
            log.warnf("Some error happend!", e);
        }

        // 关闭数据源
        log.info("close Dao");
        ds.close();

        log.info("All Done ^_^");
    }

}
