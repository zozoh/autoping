package zzh;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.log.Log;
import org.nutz.log.Logs;

/**
 * 根据 http://cloudmonitor.ca.com/ 提供的接口定期监视一下几个域的速度
 * 
 * <ul>
 * <li>cloudmonitor 的网页给出一个 JS 调用，可以知道 AJAX 的URL
 * <li>模拟这些 AJAX URL 就能得到一个 "Okay:;144.8:;147.4:;155.5:;211.151.124.250" 的字符串
 * <li>分析这个字符串，就能得到监控的列表
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AutoPing {

    private static final Log log = Logs.get();

    private static AutoPingContext c;

    public static void main(String[] args) {

        // 获取配置信息
        log.info("load properties file ...");
        String confPath = args.length > 0 ? args[0] : "autoping.properties";
        c = new AutoPingContext(confPath);

        // 启动 AJAX 线程控制器
        log.info("start threader ...");
        Thread at = new Thread(new AutoPingAjaxThreader(c));
        at.setName("AJAX_THREADER");
        at.start();

        // 生成 runner
        log.info("create runners");
        AutoPingRunner[] runners = c.getRunners();

        // 让 runner 都启动起来
        log.info("create threads");
        Thread[] ts = new Thread[runners.length];
        for (int i = 0; i < runners.length; i++) {
            ts[i] = new Thread(runners[i]);
            ts[i].setName(runners[i].getDisplayName());
        }

        // 通知 runner 们都启动
        log.info("start them ...");
        for (Thread t : ts)
            t.start();

        // 开始运行主逻辑
        log.info("entry main loop ...");
        HashMap<String, AutoPingRunner> rm = new HashMap<String, AutoPingRunner>();
        for (AutoPingRunner r : runners) {
            rm.put(r.getDisplayName(), r);
        }
        run(rm);

    }

    /**
     * 主逻辑是个无限循环
     */
    private static void run(HashMap<String, AutoPingRunner> rm) {
        while (true) {
            log.info(Strings.dup('~', 80));
            log.info("entry main loop ..");
            // 生成 atoms 容器，开始获取数据
            if (null == c.atoms()) {
                log.info("create atoms");
                c.setAtoms(new LinkedList<AutoPingAtom>());
            }
            // 这个分支进入，就诡异了
            else {
                Lang.makeThrow("!!! fuck, atoms is not be cleared!");
            }

            // 通知所有的 runner 开始运行
            log.info("notify all runner ...");
            Lang.notifyAll(c.lock());

            // 每 3 秒检查一下所有的 atoms 是否都完成了
            List<AutoPingAtom> atoms = c.atoms();
            while (true) {
                boolean allDone = atoms.size() > 0;
                // 如果有内容了，则开始监视
                int doneCount = 0;
                if (allDone) {
                    for (AutoPingAtom atom : atoms) {
                        if (atom.isDone()) {
                            allDone &= true;
                            doneCount++;
                            if (atom.isError()) {
                                log.warnf("!!! %s : %s : %s",
                                          atom.getDisplayName(),
                                          atom.getCpid(),
                                          atom.getResult());
                            }
                        } else {
                            allDone = false;
                        }
                    }
                }
                if (allDone)
                    break;

                // 睡 3 秒
                log.infof("check fail (%d/%d), wait %d ms check again",
                          doneCount,
                          atoms.size(),
                          c.getCheckInterval());
                Lang.sleep(c.getCheckInterval());
            }

            // 如果都完成，那么就循环生成相应的文件，生成文件的时间以现在来计算
            log.info("done, generate results:");

            // 开始写文件
            for (AutoPingRunner r : rm.values())
                r.beginAppend();

            // 写入结果...
            for (AutoPingAtom atom : atoms) {
                log.infof("%s >> %s", atom.getResult(), atom.getDisplayName());
                AutoPingRunner r = rm.get(atom.getDisplayName());
                r.appendLine(atom.getCpTitle(), atom.getResult().split(":;"));
            }

            // 逐个生成文件
            Date now = Times.now();
            for (AutoPingRunner r : rm.values())
                r.writeCsv(now);

            // 清除上一次检查的列表
            log.info(Strings.dup('~', 80));
            log.info("clean atoms");
            c.setAtoms(null);
            System.gc();

            // 全部搞定，睡一下
            log.infof("sleep %d ms", c.getWatchInterval());
            Lang.sleep(c.getWatchInterval());
        }
    }
}
