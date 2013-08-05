package zzh;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.http.Http;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * 无限休眠，等待主线程唤醒
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AutoPingRunner implements Runnable {

    private static final Log log = Logs.get();

    // 自己的唯一显示名
    private String displayName;

    // 要发送的 URL 得到入口页
    private String url;

    private Pattern ajaxP;

    private String ajaxPrefix;

    private Pattern tdP;

    private String tdPrefix;

    private Pattern titleP;

    // 同步锁
    private Object lock;

    private AutoPingContext c;

    // 输出 csv 文件的缓冲
    private StringBuilder sb;

    public void beginAppend() {
        sb = new StringBuilder("Checkpoint,Result,min.rtt,avg.rtt,max.rtt,IP\n");
    }

    public void appendLine(String title, String[] cols) {
        if (null == cols || 5 != cols.length) {
            sb.append("--,--,--,--,--,--").append('\n');
        } else {
            sb.append('"').append(title).append("\",");
            sb.append(Lang.concatBy("\"%s\"", ",", cols)).append('\n');
        }
    }

    public void writeCsv(Date now) {
        // 生成文件名
        SimpleDateFormat df = new SimpleDateFormat("yyMMdd_HHmmss");
        String str = df.format(now);

        // 保护一下
        if (Strings.isBlank(sb)) {
            log.warnf("skip csv file %s, blank sb!", Castors.me().castToString(now));
        }

        // 创建文件
        File f = Files.getFile(c.getDataHome(), getDisplayName()
                                                + "/"
                                                + getDisplayName()
                                                + "_"
                                                + str
                                                + ".csv");
        Files.createFileIfNoExists(f);

        // 写入内容
        Files.write(f, sb);

    }

    // 运行状态
    private boolean stop;

    public AutoPingRunner(AutoPingContext c, String host) {
        if (host.endsWith(".redatoms.com")) {
            displayName = host.substring(0, host.indexOf(".redatoms.com"));
        } else {
            displayName = host;
        }
        this.url = c.getUrlPing(host);
        this.lock = c.lock();
        this.c = c;
        this.stop = false;

        this.ajaxP = Pattern.compile(c.trim("p-ajax"));
        this.ajaxPrefix = c.trim("p-ajax-prefix");

        this.tdP = Pattern.compile(c.trim("p-td"));
        this.tdPrefix = c.trim("p-td-prefix");

        this.titleP = Pattern.compile(c.trim("p-title"));
    }

    @Override
    public void run() {
        while (!stop) {
            List<AutoPingAtom> atoms = c.atoms();
            // 已经设置了 atoms 则表示主线程认为已经要开始获取数据了，那么我也开始干活 ^_^
            if (null != atoms) {
                // 发送请求获取 HTML
                log.infof("GET HTML >> %s", url);
                String html = Http.get(url).getContent();

                String[] lines = Strings.splitIgnoreBlank(html, "\n");

                // 逐行分析，得到 URL ，以便生成 Atoms
                // 顺便做一个 Map 以便得到后续得到继续分析每个项目的 title
                log.infof("walk %d line HTML", lines.length);
                Map<Integer, AutoPingAtom> atomMap = new HashMap<Integer, AutoPingAtom>();
                for (String line : lines) {
                    // 加快判断速度
                    if (!line.startsWith(this.ajaxPrefix))
                        continue;
                    // 匹配正则表达式
                    Matcher m = ajaxP.matcher(line);
                    if (m.find()) {
                        String ajaxUrl = c.getUrlPrefix() + m.group(2);
                        AutoPingAtom atom = new AutoPingAtom(getDisplayName(), ajaxUrl);
                        atom.setCpid(Integer.parseInt(m.group(4)));
                        log.debugf("found '%s' : cp:%s", m.group(2), m.group(4));
                        atoms.add(atom);
                        atomMap.put(atom.getCpid(), atom);
                    }
                }

                // 得到一个 cpxxx 与 title 映射表，并一次设置所有的 atoms
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    // 加快判断速度
                    if (!line.startsWith(this.tdPrefix))
                        continue;
                    // 匹配正则表达式
                    Matcher m = tdP.matcher(line);
                    if (m.find()) {
                        Integer cpid = Integer.parseInt(m.group(2));
                        AutoPingAtom atom = atomMap.get(cpid);

                        // 咋回事？木有 atom ?
                        if (null == atom) {
                            log.warnf("fail to found atom for '%s'", cpid);
                            continue;
                        }

                        String titleLine = lines[i - 1];
                        m = titleP.matcher(titleLine);
                        if (m.find()) {
                            atom.setCpTitle(m.group(2));
                        }
                        // 没匹配上，报个错
                        else {
                            log.warnf(" ! title line %d : %s", i, titleLine);
                        }
                    }
                }

            }
            // 无限休眠，等待下一次启动
            Lang.wait(lock, 0);
        }
    }

    public String getDisplayName() {
        return displayName;
    }

}
