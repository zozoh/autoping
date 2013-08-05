package zzh;

import java.io.File;
import java.util.List;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;

public class AutoPingContext extends PropertiesProxy {

    public AutoPingContext(String path) {
        super(path);
    }

    /**
     * 各个 Runner 的同步锁
     */
    private Object lock = new Object();

    /**
     * 各个 runner 要加入的操作原子，默认为 null，表示禁止操作<br>
     * 如果 runner 决定运行了，他会为其设置一个 atoms 列表
     */
    private List<AutoPingAtom> atoms;

    public synchronized void setAtoms(List<AutoPingAtom> atoms) {
        this.atoms = atoms;
    }

    public synchronized List<AutoPingAtom> atoms() {
        return atoms;
    }

    public Object lock() {
        return lock;
    }

    /**
     * @return 根据配置项 "hosts" 为每个创建一个 runner
     */
    public AutoPingRunner[] getRunners() {
        String[] hosts = Strings.splitIgnoreBlank(get("hosts"), "\n");
        if (null == hosts || hosts.length == 0) {
            throw Lang.makeThrow("hosts is empty!");
        }
        AutoPingRunner[] rs = new AutoPingRunner[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            rs[i] = new AutoPingRunner(this, hosts[i]);
        }
        return rs;
    }

    /**
     * @return 数据根目录
     */
    public File getDataHome() {
        String path = trim("data-home", "~/tmp/autoping");
        return Files.createDirIfNoExists(path);
    }

    /**
     * @return 每次监控的时间间隔，单位毫秒
     */
    public long getWatchInterval() {
        return this.getLong("watch-interval", 30) * 60 * 1000;
    }

    /**
     * @return 检查是否请求完成的时间间隔，单位毫秒
     */
    public long getCheckInterval() {
        return this.getLong("check-interval", 3) * 1000;
    }

    public String getUrlPrefix() {
        return trim("url-prefix");
    }

    public String getUrlPing(String host) {
        Segment seg = Segments.create(trim("url-ping"));
        seg.set("host", host);
        return getUrlPrefix() + seg.toString();
    }

}
