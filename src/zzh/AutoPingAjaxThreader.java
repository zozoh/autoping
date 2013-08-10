package zzh;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.nutz.lang.Lang;
import org.nutz.log.Log;
import org.nutz.log.Logs;

public class AutoPingAjaxThreader implements Runnable {

    private static final Log log = Logs.get();

    private AutoPingContext c;

    private ExecutorService service;

    public AutoPingAjaxThreader(AutoPingContext c) {
        this.c = c;
        service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()
                                               * c.getInt("threadn", 50));
    }

    @Override
    public void run() {
        while (true) {
            // 为所有的 Atom 设置运行线程
            List<AutoPingAtom> atoms = c.atoms();
            if (null != atoms)
                for (AutoPingAtom atom : atoms) {
                    if (!atom.isSubmit() && atom.isReady() && !atom.isDone()) {
                        log.debugf(" - run atom : %d : %s", atom.getCpid(), atom.getCpTitle());
                        service.execute(atom);
                        atom.setSubmit(true);
                    }
                }
            // 每两秒检查一次
            Lang.wait(c.lock(), 2000);
        }
    }

}
