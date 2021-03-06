package cn.qssq666.robot.bean;
import cn.qssq666.CoreLibrary0;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Field;

import cn.qssq666.db.DBUtils;
import cn.qssq666.db.anotation.DBIgnore;
import cn.qssq666.db.anotation.Table;
import cn.qssq666.robot.business.RobotContentProvider;
import cn.qssq666.robot.interfaces.TwoDataHolder;
import cn.qssq666.robot.plugin.sdk.interfaces.IGroupConfig;
import cn.qssq666.robot.utils.DBHelper;
import cn.qssq666.robot.utils.ParseUtils;

/**
 * Created by qssq on 2017/12/20 qssq666@foxmail.com
 */

@Table("groupconfig")
public class GroupWhiteNameBean extends AccountBean implements TwoDataHolder, Cloneable, IGroupConfig {
    public boolean isCurrentGroupAdmin() {
        return isCurrentGroupAdmin;
    }

    @DBIgnore
    private boolean isCurrentGroupAdmin;

    public void setAllowGenerateCardMsg(boolean allowGenerateCardMsg) {
        this.allowGenerateCardMsg = allowGenerateCardMsg;
    }

    private boolean allowGenerateCardMsg = true;

    public boolean isEnglishdialogue() {
        return englishdialogue;
    }

    public void setEnglishdialogue(boolean englishdialogue) {
        this.englishdialogue = englishdialogue;
    }

    private boolean englishdialogue;

    public String getAdmins() {
        return admins;
    }

    public void setAdmins(String admins) {
        this.admins = admins;
    }

    private String admins;

    public boolean isReplyTranslate() {
        return replyTranslate;
    }

    public void setReplyTranslate(boolean replyTranslate) {
        this.replyTranslate = replyTranslate;
    }

    private boolean replyTranslate = true;

    public GroupWhiteNameBean() {

    }

    public GroupWhiteNameBean(String account) {
        super(account);
    }

    @Override
    public AccountBean setAccount(String account) {
        super.setAccount(account);
        return GroupWhiteNameBean.this;
    }


    public boolean isDisable() {
        return disable;
    }


    public void setDisable(boolean disable) {
        this.disable = disable;
    }


    public String getPostfix() {
        return postfix;
    }

    public void setPostfix(String postfix) {
        this.postfix = postfix;
    }

    private String postfix = "";

    public boolean isBreaklogic() {
        return breaklogic;
    }

    public void setBreaklogic(boolean breaklogic) {
        this.breaklogic = breaklogic;
    }

    public boolean isCmdsilent() {
        return cmdsilent;
    }

    public void setCmdsilent(boolean cmdsilent) {
        this.cmdsilent = cmdsilent;
    }

    private boolean cmdsilent;
    /**
     * ??????????????????????????????????????????
     */
    private boolean breaklogic;


    public int getPicgagsecond() {
        return picgagsecond;
    }

    public void setPicgagsecond(int picgagsecond) {
        this.picgagsecond = picgagsecond;
    }

    private int picgagsecond = 60 * 5;

    public int getMistakethanwarncount() {
        if (mistakethanwarncount == 0) {
            mistakethanwarncount = getMistakecount() >= 2 ? getMistakecount() / 2 : 30;
        }

        return mistakethanwarncount;
    }

    public void setMistakethanwarncount(int mistakethanwarncount) {
        this.mistakethanwarncount = mistakethanwarncount;
    }

    private int mistakethanwarncount = 3;

    public String getPicgagsecondtip() {
        if (TextUtils.isEmpty(picgagsecondtip)) {
            return "??????????????????";
        }
        return picgagsecondtip;
    }

    public void setPicgagsecondtip(String picgagsecondtip) {
        this.picgagsecondtip = picgagsecondtip;
    }

    private String picgagsecondtip = "?????????????????????";


    /**
     * ????????????????????????
     *
     * @return
     */
    public boolean isBannedaite() {
        return bannedaite;
    }

    public void setBannedaite(boolean bannedaite) {
        this.bannedaite = bannedaite;
    }

    /**
     * ????????????????????? ?????????????????????????????????
     */
    private boolean bannedaite;

    public boolean isAllowqrcode() {
        return allowqrcode;
    }

    public void setAllowqrcode(boolean allowqrcode) {
        this.allowqrcode = allowqrcode;
    }

    private boolean allowqrcode=true;

    public String getUpgradeinfo() {
        return upgradeinfo;
    }

    public void setUpgradeinfo(String upgradeinfo) {
        this.upgradeinfo = upgradeinfo;
    }

    private String upgradeinfo;

    public boolean isLocalword() {
        return localword;
    }

    public void setLocalword(boolean localword) {
        this.localword = localword;
    }


    public boolean isNetword() {
        return netword;
    }

    public void setNetword(boolean netword) {
        this.netword = netword;
    }


    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    private boolean admin = true;
    private boolean localword = true;

    public boolean isKickviolations() {
        return kickviolations;
    }

    public void setKickviolations(boolean kickviolations) {
        this.kickviolations = kickviolations;
    }

    public boolean isKickviolationsforver() {
        return kickviolationsforver;
    }

    public void setKickviolationsforver(boolean kickviolationsforver) {
        this.kickviolationsforver = kickviolationsforver;
    }

    private boolean kickviolations = false;
    private boolean kickviolationsforver = false;
    private boolean netword = true;
    private boolean bannedword = true;

    public String getVideogagtip() {
        return videogagtip;
    }

    public void setVideogagtip(String videogagtip) {
        this.videogagtip = videogagtip;
    }

    public int getVideogagminute() {
        return videogagminute;
    }

    public void setVideogagminute(int videogagminute) {
        this.videogagminute = videogagminute;
    }

    private String videogagtip;
    private int videogagminute;


    public boolean isBannevideo() {
        return bannevideo;
    }

    public void setBannevideo(boolean bannevideo) {
        this.bannevideo = bannevideo;
    }

    private boolean bannevideo = false;
    private boolean nicknameban = false;
    private boolean illegalnickname = false;
    private boolean frequentmsg = true;

    public long getBanredpacketminute() {
        return banredpacketminute;
    }

    public void setBanredpacketminute(long banredpacketminute) {
        this.banredpacketminute = banredpacketminute;
    }

    private boolean banpasswordredpacket = false;


    public String getBanredpackettip() {

        if (TextUtils.isEmpty(banredpackettip)) {
            return "??????????????????";
        }
        return banredpackettip;
    }

    public void setBanredpackettip(String banredpackettip) {
        this.banredpackettip = banredpackettip;
    }

    private long banredpacketminute;
    private String banredpackettip = "???????????????%s?????????!";

    public boolean isBanvoiceredpacket() {
        return banvoiceredpacket;
    }

    public void setBanvoiceredpacket(boolean banvoiceredpacket) {
        this.banvoiceredpacket = banvoiceredpacket;
    }

    public boolean isBannormalredpacket() {
        return bannormalredpacket;
    }

    public void setBannormalredpacket(boolean bannormalredpacket) {
        this.bannormalredpacket = bannormalredpacket;
    }

    public boolean isBanexclusiveredpacket() {
        return banexclusiveredpacket;
    }

    public void setBanexclusiveredpacket(boolean banexclusiveredpacket) {
        this.banexclusiveredpacket = banexclusiveredpacket;
    }

    private boolean banvoiceredpacket = false;
    private boolean bannormalredpacket = false;
    private boolean banexclusiveredpacket = false;
    private boolean banvoice = false;
    private boolean bancardmsg = false;

    public boolean isNeedaite() {
        return needaite;
    }

    public void setNeedaite(boolean needaite) {
        this.needaite = needaite;
    }

    public boolean isReplayatperson() {
        return replayatperson;
    }

    public void setReplayatperson(boolean replayatperson) {
        this.replayatperson = replayatperson;
    }

    private boolean needaite;
    private boolean replayatperson;

    public boolean isSelfcmdnotneedaite() {
        return selfcmdnotneedaite;
    }

    public void setSelfcmdnotneedaite(boolean selfcmdnotneedaite) {
        this.selfcmdnotneedaite = selfcmdnotneedaite;
    }

    private boolean selfcmdnotneedaite = false;

    public boolean isAllowmusic() {
        return allowmusic;
    }

    public void setAllowmusic(boolean allowmusic) {
        this.allowmusic = allowmusic;
    }

    private boolean allowmusic = true;

    public boolean isAllowModifyCard() {
        return allowModifyCard;
    }

    public void setAllowModifyCard(boolean allowModifyCard) {
        this.allowModifyCard = allowModifyCard;
    }

    private boolean allowModifyCard = true;

    public boolean isAllowMenu() {
        return allowMenu;
    }

    public void setAllowMenu(boolean allowMenu) {
        this.allowMenu = allowMenu;
    }

    private boolean allowMenu;

    public boolean isAllowtext2pic() {
        return allowtext2pic;
    }

    public void setAllowtext2pic(boolean allowtext2pic) {
        this.allowtext2pic = allowtext2pic;
    }

    public boolean isAllowsearchpic() {
        return allowsearchpic;
    }

    public void setAllowsearchpic(boolean allowsearchpic) {
        this.allowsearchpic = allowsearchpic;
    }

    private boolean allowtext2pic = true;

    public boolean isAllowzan() {
        return allowzan;
    }

    public void setAllowzan(boolean allowzan) {
        this.allowzan = allowzan;
    }

    private boolean allowzan = true;
    private boolean allowsearchpic = true;
    private boolean bancall = false;
    private boolean banpic = false;

    public boolean isRevokemsg() {
        return revokemsg;
    }

    public void setRevokemsg(boolean revokemsg) {
        this.revokemsg = revokemsg;
    }

    private boolean revokemsg = true;

    public boolean isAllowTranslate() {
        return allowTranslate;
    }

    public void setAllowTranslate(boolean allowTranslate) {
        this.allowTranslate = allowTranslate;
    }

    private boolean allowTranslate = true;
    private boolean joingroupreply = true;
    private String joingroupword = "????????????$group???,??????????????????!";
    private String remark = "";

    public boolean isAutornamecard() {
        return autornamecard;
    }

    public void setAutornamecard(boolean autornamecard) {
        this.autornamecard = autornamecard;
    }

    /**
     * ????????????????????????????????????????????????
     */
    private boolean autornamecard = true;

    public String getNameCardvarTemplete() {
        return nameCardvarTemplete;
    }

    public void setNameCardvarTemplete(String nameCardvarTemplete) {
        this.nameCardvarTemplete = nameCardvarTemplete;
    }

    private String nameCardvarTemplete = "$nickname-$area-$phone-N";

    public int getNotparamgagminute() {
        return notparamgagminute = notparamgagminute == 0 ? 1 : notparamgagminute;
    }

    public void setNotparamgagminute(int notparamgagminute) {
        this.notparamgagminute = notparamgagminute;
    }

    private int notparamgagminute = 1;
    private String groupnickanmekeyword = "^([\\u4e00-\\u9fa5_a-zA-Z0-9]{1,5})[\\_\\-\\--]([\\u4e00-\\u9fa5]{1,5})[\\_\\-\\--]([\\u4e00-\\u9fa5_a-zA-Z0-9]{1,12})[\\_\\-\\--][R|N]$";// INGNOE_INCLUDE

    public boolean isBancardmsg() {
        return bancardmsg;
    }

    public void setBancardmsg(boolean bancardmsg) {
        this.bancardmsg = bancardmsg;
    }

    public String getVoicegagtip() {
        if (TextUtils.isEmpty(voicegagtip)) {
            return "??????????????????";
        }
        return voicegagtip;
    }

    public void setVoicegagtip(String voicegagtip) {
        this.voicegagtip = voicegagtip;
    }

    public long getVoicegagminute() {
        return voicegagminute;
    }

    public void setVoicegagminute(long voicegagminute) {
        this.voicegagminute = voicegagminute;
    }

    private String voicegagtip = "?????????????????????";

    public String getCardmsggagtip() {
        if (TextUtils.isEmpty(cardmsggagtip)) {
            return "????????????????????????";
        }
        return cardmsggagtip;
    }

    public void setCardmsggagtip(String cardmsggagtip) {
        this.cardmsggagtip = cardmsggagtip;
    }

    public long getCardmsgminute() {
        return cardmsgminute;
    }

    public void setCardmsgminute(long cardmsgminute) {
        this.cardmsgminute = cardmsgminute;
    }

    private String cardmsggagtip = "???????????????????????????";
    private long voicegagminute = 5;
    private long cardmsgminute = 5;
    @Deprecated
    private String groupnickanmekeyword1 = "";
    /**
     * ?????????
     */
    private int groupnickanmegagtime = 60;

    public int getMistakecount() {
        return mistakecount;
    }

    public void setMistakecount(int mistakecount) {
        this.mistakecount = mistakecount;
    }

    /**
     * ????????????????????????
     */
    private int mistakecount = 30;

    /**
     * ^[\u4e00-\u9fa5]{1,2}\-[\u4e00-\u9fa5]{1,2}\-NR|R$
     */
    public String getGroupnicknamegagtip() {
        if (TextUtils.isEmpty(groupnicknamegagtip)) {
            return "";
        }
        return groupnicknamegagtip;
    }

    public void setGroupnicknamegagtip(String groupnicknamegagtip) {
        this.groupnicknamegagtip = groupnicknamegagtip;
    }

    /**
     * ???????????????????????????
     */
    private String groupnicknamegagtip = "??????$u?????????$nickname ????????????!????????????$any ????????????????????????root????????????-??????-??????4A-N??????????????????4??????????????????4?????????1????????????????????????????????????10??????????????????-???????????????R???N(??????root)";


    //            "??????$uin?????????$nickname ?????????!????????????$any????????????????????????root????????????-??????-??????4A-N??????????????????4??????????????????4?????????1????????????????????????????????????10??????????????????-???????????????R???N(??????root)";
    private boolean redpackettitlebanedword;


    public boolean isAccumlativegagdata() {
        return accumlativegagdata;
    }

    public void setAccumlativegagdata(boolean accumlativegagdata) {
        this.accumlativegagdata = accumlativegagdata;
    }

    /**
     * ??????????????????
     */
    private boolean accumlativegagdata;

    public boolean isOnlyrecordwordgagcount() {
        return onlyrecordwordgagcount;
    }

    public void setOnlyrecordwordgagcount(boolean onlyrecordwordgagcount) {
        this.onlyrecordwordgagcount = onlyrecordwordgagcount;
    }

    private boolean onlyrecordwordgagcount;

    public String getCountthantip() {
        return TextUtils.isEmpty(countthantip) ? "??????????????????,??????" : countthantip;
    }

    public void setCountthantip(String countthantip) {
        this.countthantip = countthantip;
    }

    private String countthantip = " ?????????%d??? ??????????????????%d???????????????????????????";
    /**
     * ???
     */
    private int frequentmsgduratiion = 30;
    /**
     * ???????????????
     */
    private int frequentmsgcount = 30;

    public boolean isFitercommand() {
        return fitercommand;
    }

    public void setFitercommand(boolean fitercommand) {
        this.fitercommand = fitercommand;
    }

    /**
     * ??????????????????
     */

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????
     */
    private boolean fitercommand;
    private int frequentmsggagtime = 60 * 60;

    public boolean isBannedword() {
        return bannedword;
    }

    public void setBannedword(boolean bannedword) {
        this.bannedword = bannedword;
    }


    /**
     * ?????????????????????
     *
     * @return
     */
    public boolean isNicknameban() {
        return nicknameban;
    }

    public void setNicknameban(boolean nicknameban) {
        this.nicknameban = nicknameban;
    }


    public boolean isIllegalnickname() {
        return illegalnickname;
    }

    public void setIllegalnickname(boolean illegalnickname) {
        this.illegalnickname = illegalnickname;
    }


    public boolean isFrequentmsg() {
        return frequentmsg;
    }

    public void setFrequentmsg(boolean frequentmsg) {
        this.frequentmsg = frequentmsg;
    }


    public boolean isBanpasswordredpacket() {
        return banpasswordredpacket;
    }

    public void setBanpasswordredpacket(boolean banpasswordredpacket) {
        this.banpasswordredpacket = banpasswordredpacket;
    }


    public boolean isBanvoice() {
        return banvoice;
    }

    public void setBanvoice(boolean banvoice) {
        this.banvoice = banvoice;
    }

    //   @Bindable
    public boolean isBancall() {
        return bancall;
    }

    public void setBancall(boolean bancall) {
        this.bancall = bancall;
        //    notifyPropertyChanged(BR.bancall);
    }

    //   @Bindable
    public boolean isBanpic() {
        return banpic;
    }

    public void setBanpic(boolean banpic) {
        this.banpic = banpic;
        //     notifyPropertyChanged(BR.banpic);
    }


    //  @Bindable
    public boolean isJoingroupreply() {
        return joingroupreply;
    }

    public void setJoingroupreply(boolean joingroupreply) {
        this.joingroupreply = joingroupreply;
        //     notifyPropertyChanged(BR.joingroupreply);
    }


    //  @Bindable
    public String getJoingroupword() {
        return joingroupword;
    }

    public void setJoingroupword(String joingroupword) {
        this.joingroupword = joingroupword;
        //     notifyPropertyChanged(BR.joingroupword);
    }


    public boolean isRedpackettitlebanedword() {
        return redpackettitlebanedword;
    }

    public void setRedpackettitlebanedword(boolean redpackettitlebanedword) {
        this.redpackettitlebanedword = redpackettitlebanedword;
    }


    //    @Bindable
    public int getFrequentmsgduratiion() {
        return frequentmsgduratiion;
    }

    public void setFrequentmsgduratiion(int frequentmsgduratiion) {
        this.frequentmsgduratiion = frequentmsgduratiion;
//         notifyPropertyChanged(BR.frequentmsgduratiion);
    }

    //   @Bindable
    public int getFrequentmsgcount() {
        return frequentmsgcount;
    }

    public void setFrequentmsgcount(int frequentmsgcount) {
        this.frequentmsgcount = frequentmsgcount;
        //     notifyPropertyChanged(cn.qssq666.robot.BR.frequentmsgcount);
    }

    //    @Bindable
    public int getFrequentmsggagtime() {
        return frequentmsggagtime;
    }

    public void setFrequentmsggagtime(int frequentmsggagtime) {
        this.frequentmsggagtime = frequentmsggagtime;
//        notifyPropertyChanged(cn.qssq666.robot.BR.frequentmsggagtime);
    }


    //    @Bindable
    public String getGroupnickanmekeyword() {
        return groupnickanmekeyword;
    }


    public void setGroupnickanmekeyword(String groupnickanmekeyword) {
        this.groupnickanmekeyword = groupnickanmekeyword;
        //   notifyPropertyChanged(cn.qssq666.robot.BR.groupnickanmekeyword);
    }

    //    @Bindable
    public String getGroupnickanmekeyword1() {
        return groupnickanmekeyword1;
    }

    public void setGroupnickanmekeyword1(String groupnickanmekeyword1) {
        this.groupnickanmekeyword1 = groupnickanmekeyword1;
//        notifyPropertyChanged(cn.qssq666.robot.BR.groupnickanmekeyword1);
    }


    //    @Bindable
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
//        notifyPropertyChanged(BR.remark);
    }


    /**
     * ???????????????
     *
     * @return
     */
    //  @Bindable
    public int getGroupnickanmegagtime() {
        return groupnickanmegagtime;
    }

    public void setGroupnickanmegagtime(int groupnickanmegagtime) {
        this.groupnickanmegagtime = groupnickanmegagtime;
    }


    @Override
    public String getShowTitle() {
        return "??????:" + getAccount();
    }

    @Override
    public String getShowContent() {


        return "??????:" + getRemark();
    }


    public String toStringJSON() {
        return JSON.toJSONString(this);
    }

    @Override
    public String toString() {
        return super.toString() + JSON.toJSONString(this);
    }

    public String getConfig() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("?????????:%s\n", remark));
        sb.append(String.format("????????????:%b\n", disable));
        sb.append(String.format("????????????????????????:%b\n", admin));
        sb.append(String.format("??????????????????:%b\n", isNeedaite()));
        sb.append(String.format("?????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(replayatperson)));
        sb.append(String.format("??????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(isAllowmusic()) + ""));
        sb.append(String.format("????????????????????????:%b\n", localword));
        sb.append(String.format("????????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(netword)));
        sb.append(String.format("???????????????????????????:%b\n",ParseUtils.parseBoolean2ChineseBooleanStr( admin)));
        sb.append(String.format("?????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(banexclusiveredpacket)));
        sb.append(String.format("?????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(bannormalredpacket)));
        sb.append(String.format("?????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(banvoiceredpacket)));
        sb.append(String.format("?????????????????????:%b\n",ParseUtils.parseBoolean2ChineseBooleanStr( banpasswordredpacket)));
        sb.append(String.format("?????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(banpic)));
        sb.append(String.format("?????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(banvoice)));
        sb.append(String.format("?????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(bannevideo)));
        sb.append(String.format("?????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(bancardmsg)));
        sb.append(String.format("???????????????????????????:%b\n",ParseUtils.parseBoolean2ChineseBooleanStr( accumlativegagdata)));
        sb.append(String.format("?????????????????????:%b\n", isOnlyrecordwordgagcount()));
        sb.append(String.format("????????????:%b\n",ParseUtils.parseBoolean2ChineseBooleanStr( frequentmsg)));
        sb.append(String.format("????????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(nicknameban)));
        sb.append(String.format("?????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(bannedword)));
        sb.append(String.format("?????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(illegalnickname)));
        sb.append(String.format("??????????????????:%b\n",ParseUtils.parseBoolean2ChineseBooleanStr( redpackettitlebanedword)));
        sb.append(String.format("?????????????????????:%b\n", ParseUtils.parseBoolean2ChineseBooleanStr(joingroupreply)));
        sb.append(String.format("??????????????????:%s\n", postfix));
        sb.append(String.format("????????????????????????:%d\n", mistakecount));
        sb.append(String.format("?????????????????????????????????:%d\n", notparamgagminute));
        return sb.toString();
    }
/*
    @Override
    public String toString() {
        return super.toString() + "GroupWhiteNameBean{" +
                "enable1=" + enable1 +
                ", enable2=" + enable2 +
                ", enable3=" + enable3 +
                ", enable4=" + enable4 +
                ", enable5=" + enable5 +
                ", enable6=" + enable6 +
                ", enable7=" + enable7 +
                ", enable8=" + enable8 +
                ", enable9=" + enable9 +
                ", enable10=" + enable10 +
                ", enable11=" + enable11 +
                ", enable12=" + enable12 +
                ", enable13=" + enable13 +
                ", enable14=" + enable14 +
                ", enable15=" + enable15 +
                ", enable16=" + enable16 +
                ", enable17=" + enable17 +
                ", enable18=" + enable18 +
                ", enable19=" + enable19 +
                ", enable20=" + enable20 +
                ", enable21=" + enable21 +
                ", enable22=" + enable22 +
                ", enable23=" + enable23 +
                ", enable24=" + enable24 +
                ", enable25=" + enable25 +
                ", upgradeinfo='" + upgradeinfo + '\'' +
                ", admin=" + admin +
                ", localword=" + localword +
                ", netword=" + netword +
                ", bannedword=" + bannedword +
                ", nicknameban=" + nicknameban +
                ", illegalnickname=" + illegalnickname +
                ", frequentmsg=" + frequentmsg +
                ", banpasswordredpacket=" + banpasswordredpacket +
                ", banvoice=" + banvoice +
                ", bancall=" + bancall +
                ", banpic=" + banpic +
                ", joingroupreply=" + joingroupreply +
                ", joingroupword='" + joingroupword + '\'' +
                ", remark='" + remark + '\'' +
                ", groupnickanmekeyword='" + groupnickanmekeyword + '\'' +
                ", groupnickanmekeyword1='" + groupnickanmekeyword1 + '\'' +
                ", groupnickanmegagtime=" + groupnickanmegagtime +
                ", redpackettitlebanedword=" + redpackettitlebanedword +
                ", frequentmsgduratiion=" + frequentmsgduratiion +
                ", frequentmsgcount=" + frequentmsgcount +
                ", frequentmsggagtime=" + frequentmsggagtime +
                '}';
    }
*/


    @Override
    public GroupWhiteNameBean clone() {
        GroupWhiteNameBean obj = null;
        try {
            obj = (GroupWhiteNameBean) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return obj;
    }


    @Override
    public boolean universalQueryBoolean(int type, Object... args) {
        return false;
    }

    @Override
    public Object universalQueryByFieldName(String name) {
        Field field = null;
        try {
            field.setAccessible(true);
            field = this.getClass().getField(name);
            field.setAccessible(true);
            return field.get(this);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public boolean universalsetFieldValue(String name, Object value) {
        Field field = null;
        try {
            field.setAccessible(true);
            field = this.getClass().getField(name);
            field.setAccessible(true);
            field.set(this, value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        return false;
    }


    @Override
    public String universalQueryString(int type, Object... args) {
        return null;
    }

    @Override
    public boolean universalSaveInfo() {

        DBUtils dbUtils = RobotContentProvider.getDbUtils();
        int update = DBHelper.getQQGroupWhiteNameDBUtil(dbUtils).update(this);
        return update > 0;
    }

    public boolean isAllowGenerateCardMsg() {
        return allowGenerateCardMsg;
    }

    public void setIsCurrentGroupAdmin(boolean isCurrentGroupAdmin) {
        this.isCurrentGroupAdmin = isCurrentGroupAdmin;
    }
}
