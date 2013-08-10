package zzh;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;

/**
 * 封装了一次 HTTP 请求
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AutoPingAtom implements Atom {

    private static final Log log = Logs.get();

    private String displayName;

    private String url;

    private int cpid;

    private String cpTitle;

    // 请求完毕的结果
    private String result;

    private boolean submit;

    public AutoPingAtom(String displayName, String url) {
        this.displayName = displayName;
        this.url = url;
        this.submit = false;
    }

    public boolean isSubmit() {
        return submit;
    }

    public void setSubmit(boolean submit) {
        this.submit = submit;
    }

    @Override
    public void run() {
        while (!isDone()) {
            try {
                // log.debugf(" atom >http> %s", url);
                Response re = Http.get(url);
                // 如果请求成功
                if (re.isOK()) {
                    String str = Strings.trim(re.getContent());
                    // 空内容，错误
                    if (Strings.isBlank(str)) {
                        result = "!Blank result";
                    }
                    //
                    else {
                        result = str;
                    }
                }
                // 如果请求失败
                else {
                    log.warnf("  !! fail for '%s' @ %s", displayName, url);
                    result = "!Error!" + re.getStatus();
                }
            }
            catch (Exception e) {
                log.warn("atom error!", e);
            }
        }
    }

    public int getCpid() {
        return cpid;
    }

    public void setCpid(int cpid) {
        this.cpid = cpid;
    }

    public String getCpTitle() {
        return cpTitle;
    }

    public void setCpTitle(String cpTitle) {
        this.cpTitle = cpTitle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getResult() {
        return result;
    }

    /**
     * @return 是否准备好了 cpid 和 cpTitle
     */
    public boolean isReady() {
        return cpid > 0 && !Strings.isBlank(cpTitle);
    }

    public boolean isDone() {
        return !Strings.isBlank(result);
    }

    public boolean isError() {
        return Strings.startsWithChar(result, '!');
    }

}
