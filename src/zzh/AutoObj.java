package zzh;

import java.sql.Timestamp;

import org.nutz.dao.entity.annotation.*;

@Table("t_ping")
@Index(name = "t_ping_index_host", fields = "host", unique = false)
@PK({"host", "pingTime", "checkpoint"})
public class AutoObj {

    @Column
    private String host;

    @Column("ping_time")
    private Timestamp pingTime;

    @Column
    private String checkpoint;

    @Column
    private String result;

    @Column("min_rtt")
    private float minRtt;

    @Column("avg_rtt")
    private float avgRtt;

    @Column("max_rtt")
    private float maxRtt;

    @Column("ipv4")
    private String ipv4;

    @Column("it")
    private Timestamp insertTime;

    @Column("itms")
    private long insertMs;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Timestamp getPingTime() {
        return pingTime;
    }

    public void setPingTime(Timestamp pingTime) {
        this.pingTime = pingTime;
    }

    public String getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(String checkpoint) {
        this.checkpoint = checkpoint;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public float getMinRtt() {
        return minRtt;
    }

    public void setMinRtt(float minRtt) {
        this.minRtt = minRtt;
    }

    public float getAvgRtt() {
        return avgRtt;
    }

    public void setAvgRtt(float avgRtt) {
        this.avgRtt = avgRtt;
    }

    public float getMaxRtt() {
        return maxRtt;
    }

    public void setMaxRtt(float maxRtt) {
        this.maxRtt = maxRtt;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public Timestamp getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Timestamp insertTime) {
        this.insertTime = insertTime;
    }

    public long getInsertMs() {
        return insertMs;
    }

    public void setInsertMs(long insertMs) {
        this.insertMs = insertMs;
    }

}
