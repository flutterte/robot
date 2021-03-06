package cn.qssq666.robot.bean;
import cn.qssq666.CoreLibrary0;import cn.qssq666.robot.plugin.sdk.interfaces.IMsgModel;
import cn.qssq666.robot.utils.DateUtils;

/**
 * Created by luozheng on 2017/3/5.  qssq.space
 */

public class MsgItem implements Cloneable,IMsgModel {

    private long messageID;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MsgItem item = (MsgItem) o;

        if (senderuin != null ? !senderuin.equals(item.senderuin) : item.senderuin != null)
            return false;
        return frienduin != null ? frienduin.equals(item.frienduin) : item.frienduin == null;
    }

    @Override
    public int hashCode() {
        int result = senderuin != null ? senderuin.hashCode() : 0;
        result = 31 * result + (frienduin != null ? frienduin.hashCode() : 0);
        return result;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String nickname;

    public int getIstroop() {
        return istroop;
    }

    public MsgItem setIstroop(int istroop) {
        this.istroop = istroop;
        return this;
    }

    public String getSenderuin() {
        return senderuin;
    }

    public MsgItem setSenderuin(String senderuin) {
        this.senderuin = senderuin;
        return this;
    }

    public String getFrienduin() {
        return frienduin;
    }

    public MsgItem setFrienduin(String frienduin) {
        this.frienduin = frienduin;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public MsgItem setMessage(String message) {
        this.message = message;
        return this;
    }

    public int getType() {
        return type;
    }

    @Override
    public long getMessageID() {
        return this.messageID;
    }

    public MsgItem setType(int type) {
        this.type = type;
        return this;
    }

    int istroop;
    String senderuin;
    String frienduin;
    String message;

    public String getExtrajson() {
        return extrajson;
    }

    public void setExtrajson(String extrajson) {
        this.extrajson = extrajson;
    }

    String extrajson;


    public String getExtstr() {
        return extstr;
    }

    public void setExtstr(String extstr) {
        this.extstr = extstr;
    }

    String extstr;

    public String getSelfuin() {
        return selfuin == null ? "" : selfuin;
    }

    public void setSelfuin(String selfuin) {
        this.selfuin = selfuin;
    }

    String selfuin;
    int type;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    String version;

    public String getApptype() {
        return apptype;
    }

    public void setApptype(String apptype) {
        this.apptype = apptype;
    }

    private String apptype;

    public long getTime() {
        return time;
    }

    public MsgItem setTime(long time) {
        this.time = time;
        return this;
    }

    long time;

    public int getCode() {
        return code;
    }

    public MsgItem setCode(int code) {
        this.code = code;
        return this;
    }

    @Override
    public void setUniversalAction(int type, Object obj) {

    }

    @Override
    public Object getUniversalAction(int type) {
        return null;
    }


    /**
     * ??????code=0????????? ?????? ??????
     */
    int code;

    @Override
    public String toString() {
        return "{" +
                "??????='" + nickname + '\'' +
                ", ???????????????=" + istroop +
                ", ?????????='" + senderuin + '\'' +
                ", ??????='" + frienduin + '\'' +
                ", ?????????='" + message + '\'' +
                ", ????????????='" + selfuin + '\'' +
                ", ????????????=" + type +
                ", ??????=" + DateUtils.getTime(time) +
                ", ?????????=" + code +
                '}';

    }

    public String toSimpleString() {
        return "[" + nickname + "(" + senderuin + "):" + message + "]";
    }

    @Override
    public MsgItem clone() {
        MsgItem obj = null;
        try {
            obj = (MsgItem) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void setMessagID(long id) {
        this.messageID=id;
    }
}
