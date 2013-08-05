package zzh;

import java.util.List;

import org.nutz.lang.Lang;

public class AutoPingAjaxThreader implements Runnable {

    private AutoPingContext c;

    public AutoPingAjaxThreader(AutoPingContext c) {
        this.c = c;
    }

    @Override
    public void run() {
        while (true) {
            // 为所有的 Atom 设置运行线程
            List<AutoPingAtom> atoms = c.atoms();
            if (null != atoms)
                for (AutoPingAtom atom : atoms) {
                    if (atom.isReady() && !atom.isDone()) {
                        atom.appendThread();
                    }
                }
            // 每两秒检查一次
            Lang.wait(c.lock(), 2000);
        }
    }

}
