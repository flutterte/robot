package cn.qssq666.robot.business;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.TimeUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.umeng.analytics.MobclickAgent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import cn.qssq666.db.DBUtils;
import cn.qssq666.robot.BuildConfig;
import cn.qssq666.robot.R;
import cn.qssq666.robot.app.AppContext;
import cn.qssq666.robot.asynctask.QssqTask;
import cn.qssq666.robot.bean.AccountBean;
import cn.qssq666.robot.bean.AdminBean;
import cn.qssq666.robot.bean.AtBean;
import cn.qssq666.robot.bean.DoWhileMsg;
import cn.qssq666.robot.bean.GagAccountBean;
import cn.qssq666.robot.bean.GroupAdaminBean;
import cn.qssq666.robot.bean.GroupAtBean;
import cn.qssq666.robot.bean.GroupWhiteNameBean;
import cn.qssq666.robot.bean.MsgItem;
import cn.qssq666.robot.bean.RedPacketBean;
import cn.qssq666.robot.bean.RedPacketBeanFromServer;
import cn.qssq666.robot.bean.RedpacketBaseInfo;
import cn.qssq666.robot.bean.ReplyWordBean;
import cn.qssq666.robot.bean.RequestBean;
import cn.qssq666.robot.bean.ResultBean;
import cn.qssq666.robot.bean.TwoBean;
import cn.qssq666.robot.bean.VarBean;
import cn.qssq666.robot.bean.ViolationWordRecordBean;
import cn.qssq666.robot.business.module.TranslateQueryImpl;
import cn.qssq666.robot.config.CmdConfig;
import cn.qssq666.robot.config.IGnoreConfig;
import cn.qssq666.robot.config.MemoryIGnoreConfig;
import cn.qssq666.robot.constants.AccountType;
import cn.qssq666.robot.constants.AppConstants;
import cn.qssq666.robot.constants.CardHelper;
import cn.qssq666.robot.constants.Cns;
import cn.qssq666.robot.constants.FieldCns;
import cn.qssq666.robot.constants.IPluginRequestCall;
import cn.qssq666.robot.constants.MsgTypeConstant;
import cn.qssq666.robot.constants.ServiceExecCode;
import cn.qssq666.robot.constants.TuLingType;
import cn.qssq666.robot.constants.UpdateLog;
import cn.qssq666.robot.enums.GAGTYPE;
import cn.qssq666.robot.event.AccountAddOrChangeEvent;
import cn.qssq666.robot.event.BaseSettignEvent;
import cn.qssq666.robot.event.DelegateSendMsg;
import cn.qssq666.robot.event.ForbitUseAdvanceEvent;
import cn.qssq666.robot.event.ForceEvent;
import cn.qssq666.robot.event.GroupConfigEvent;
import cn.qssq666.robot.event.OnUpdateAccountListEvent;
import cn.qssq666.robot.event.WordEvent;
import cn.qssq666.robot.http.HttpUtilRetrofit;
import cn.qssq666.robot.http.api.MoLiAPI;
import cn.qssq666.robot.http.api.TuLingAPI;
import cn.qssq666.robot.interfaces.DelegateSendMsgType;
import cn.qssq666.robot.interfaces.ICmdIntercept;
import cn.qssq666.robot.interfaces.IIntercept;
import cn.qssq666.robot.interfaces.INeedReplayLevel;
import cn.qssq666.robot.interfaces.INotify;
import cn.qssq666.robot.interfaces.OnCareteNotify;
import cn.qssq666.robot.interfaces.RedPacketMessageType;
import cn.qssq666.robot.interfaces.RequestListener;
import cn.qssq666.robot.misc.SQLCns;
import cn.qssq666.robot.plugin.PluginUtils;
import cn.qssq666.robot.plugin.js.util.JSPluginUtil;
import cn.qssq666.robot.plugin.lua.util.LuaPluginUtil;
import cn.qssq666.robot.plugin.sdk.interfaces.AtBeanModelI;
import cn.qssq666.robot.plugin.sdk.interfaces.IGroupConfig;
import cn.qssq666.robot.plugin.sdk.interfaces.IMsgModel;
import cn.qssq666.robot.plugin.sdk.interfaces.PluginControlInterface;
import cn.qssq666.robot.plugin.sdk.interfaces.PluginInterface;
import cn.qssq666.robot.plugin.sdk.myimpl.ConfigQueryImpl;
import cn.qssq666.robot.plugin.sdk.myimpl.PluginControlmpl;
import cn.qssq666.robot.plugin.util.QueryPluginModel;
import cn.qssq666.robot.receiver.CodeUpdateReceiver;
import cn.qssq666.robot.selfplugin.IHostControlApi;
import cn.qssq666.robot.selfplugin.IPluginHolder;
import cn.qssq666.robot.selfplugin.IRobotContentProvider;
import cn.qssq666.robot.service.DaemonService;
import cn.qssq666.robot.service.RemoteService;
import cn.qssq666.robot.utils.AccountUtil;
import cn.qssq666.robot.utils.AppUtils;
import cn.qssq666.robot.utils.BatchUtil;
import cn.qssq666.robot.utils.CheckUtils;
import cn.qssq666.robot.utils.ClearUtil;
import cn.qssq666.robot.utils.ConfigUtils;
import cn.qssq666.robot.utils.DBHelper;
import cn.qssq666.robot.utils.DateUtils;
import cn.qssq666.robot.utils.EncryptPassUtil;
import cn.qssq666.robot.utils.ErrorHelper;
import cn.qssq666.robot.utils.HttpUtil;
import cn.qssq666.robot.utils.InitUtils;
import cn.qssq666.robot.utils.LogUtil;
import cn.qssq666.robot.utils.NetQuery;
import cn.qssq666.robot.utils.NickNameUtils;
import cn.qssq666.robot.utils.PairFix;
import cn.qssq666.robot.utils.ParseUtils;
import cn.qssq666.robot.utils.QssqTaskFix;
import cn.qssq666.robot.utils.RXUtil;
import cn.qssq666.robot.utils.RegexUtils;
import cn.qssq666.robot.utils.RobotFormatUtil;
import cn.qssq666.robot.utils.RobotUtil;
import cn.qssq666.robot.utils.SPUtils;
import cn.qssq666.robot.utils.ShellUtil;
import cn.qssq666.robot.utils.StringUtils;
import cn.qssq666.robot.utils.VarCastUtil;
import cn.qssq666.robot.utils.ZxingUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import static cn.qssq666.robot.utils.DateUtils.TYPE_MS;
import static cn.qssq666.robot.utils.DateUtils.TYPE_SECOND;
import static cn.qssq666.robot.utils.DateUtils.getTimeDistance;

/**
 * Created by luozheng on 2017/2/12.  qssq.space
 */

public class RobotContentProvider extends ContentProvider implements IRobotContentProvider {

    //IGNORE_START
    public static final boolean ENABLE_LOG = BuildConfig.DEBUG;
    private static RobotContentProvider instance;
    private String mLastError;
    private ClassLoader mProxyClassloader;
    public boolean mAllowPluginInterceptEndMsg = false;
    public boolean mAllowReponseSelfCommand = true;
    public IHostControlApi mHostControlApi;
    private boolean mUseChildThread = false;
    private int defaultReplyIndex;
    private MsgItem mItem;


    public PluginControlInterface getPluginControlInterface() {
        return pluginControlInterface;
    }

    private PluginControlInterface pluginControlInterface;

    public ConfigQueryImpl getConfigQueryImpl() {
        return configQueryImpl;
    }

    private ConfigQueryImpl configQueryImpl;

    public static RobotContentProvider getInstance() {
        return instance;
    }

    private String mRobotQQ = null;
    private SharedPreferences sharedPreferences;

    private static final String TAG = "RobotContentProvider";//
    public final static String ACTION_MSG = "insert/msg";//???????????????????????????
    public final static String ACTION_GAD = "insert/gad";//???????????????????????????
    private String mShortUrlTextApiUrl = "http://suo.im/api.php?url=";
    public final static String ACTION_KICK = "insert/kick";
    public final static String ACTION_UPDATE_KEY = "update/key";
    private static UriMatcher _uriMatcher;
    private String robotReplyKey = Cns.DEFAULT_TULING_KEY;
    private String robotReplySecret = "";
    private static final int CODE_MSG = 1;
    private static final int CODE_GAD = 2;
    private static final int CODE_TICK = 3;
    private static final int CODE_UPDATE_KEY = 4;

    public boolean mCfeanbleGroupReply = true;
    public final boolean mCfOnlyReplyWhiteNameGroup = true;
    public boolean mCfNotReplyGroup = true;
    public String mCfOneReplyOneGroupStr = "";
    public String mCfNotReplyGroupStr = "";
    public boolean mCfBaseReplyShowNickName;
    public boolean mCFBaseEnablePlugin = true;
    @Deprecated
    public boolean mCfBaseReplyNeedAite;
    public boolean mCfBaseWhiteNameReplyNotNeedAite;
    public boolean mCfNotWhiteNameReplyIfAite;
    public boolean mCfprivateReply;
    public String mLocalRobotName;
    public String mCfBaseNoReplyNameStr;
    public static DBUtils _dbUtils;
    HashMap<String, String> mKeyWordMap = new HashMap<>();
    public List<GroupWhiteNameBean> mQQGroupWhiteNames = new ArrayList<>();
    public List<AccountBean> mIgnoreQQs = new ArrayList<>();
    public List<AccountBean> mIgnoreGagQQs = new ArrayList<>();
    public List<AdminBean> mSuperManagers = new ArrayList<>();
    public List<GagAccountBean> mGagKeyWords = new ArrayList<>();
    public boolean mCfBaseEnableNetRobotPrivate;
    public boolean mCfBaseEnableNetRobotGroup;
    public String mCfGroupJoinGroupReplyStr;
    public static int musicType;
    public static long mStatupTime;
    public boolean mForceUpdate;
    public boolean mStopUseAdvanceFunc;
    public boolean mCfBaseDisableAtFunction;
    public boolean mCfBaseDisableGag;
    public boolean mCfBaseNetReplyErrorNotWarn;
    public List<IPluginHolder> mPluginList = new ArrayList<>();
    public List<IPluginHolder> mLuaPluginList = new ArrayList<>();
    public List<IPluginHolder> mJSPluginList = new ArrayList<>();
    public boolean mCfBaseDisableStructMsg;
    public boolean mCfBaseEnableLocalWord;
    public boolean mCFBaseEnableCheckKeyWapGag = true;
    public static long mInsertTime;
    public String mReplyPostFix = "";
    /**
     * ???????????? ???????????????????????????.
     */
    private boolean mCfprivateReplyManagrIgnoreRule;
    private boolean interceptNotifyChanage;
    public static String mPackageName;
    private Context mPakcageContext;

    //IGNORE_END
    public static void setLastError(Throwable lastError) {
        RobotContentProvider.getInstance().mLastError = Log.getStackTraceString(lastError);
    }

    public void setProxyResources(Context context, Resources resources) {
        try {
            mPakcageContext = context.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);

//            mPakcageContext = (Context) EncryptUtilN.cxc(context);
            this.mProxyResources = mPakcageContext.getResources();
            LogUtil.writeLog(TAG, "set proxy from createPackageContext " + mProxyResources.getString(R.string.test_read));

        } catch (Throwable e) {
            LogUtil.writeLog(TAG, "may cannot sync modify info .c fuck error 228");
            this.mProxyResources = resources;
            e.printStackTrace();
        }
    }

    @Override
    public void testApi() {
        MsgReCallUtil.notifyTest(this, false, "??????????????????");
        MsgReCallUtil.notifyTest(this, true, "???????????????");
    }

    @Override
    public List<IPluginHolder> getPluginList() {
        return mPluginList;
    }

    public List<IPluginHolder> getLuaPluginList() {
        return mLuaPluginList;
    }

    public List<IPluginHolder> getJSPluginList() {
        return mJSPluginList;
    }

    private Resources mProxyResources;

    public static DBUtils getDbUtils() {
        return _dbUtils;
    }

    @Subscribe
    public void onReceveWordData(WordEvent event) {
        if (event.isEdit()) {
            initWordMap();
        } else {
            doInsertNewKeyBean(event.getBean());
        }
    }

    public void initWordMap() {
        List<ReplyWordBean> list = DBHelper.getKeyWordDBUtil(_dbUtils).queryAll(ReplyWordBean.class);
        initKeyMap(list);
    }

    /**
     * ???????????????????????? ??????QQ?????????????????????
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceveAccountListUpdateEvent(OnUpdateAccountListEvent event) {
        if (event.getType() == AccountType.TYPE_QQ_INOGRE_NAME) {
            mIgnoreQQs.clear();
            mIgnoreQQs.addAll(event.getList());
        }
        if (event.getType() == AccountType.TYPE_QQ_INOGRE_NAME_GAG) {
            mIgnoreGagQQs.clear();
            mIgnoreGagQQs.addAll(event.getList());
        } else if (event.getType() == AccountType.TYPE_QQGROUP_WHITE_NAME) {
            mQQGroupWhiteNames.clear();
            List listNew = event.getList();
            mQQGroupWhiteNames.addAll(listNew);
        }

    }

    @Subscribe
    public void onReceiveDelegateSendMsg(DelegateSendMsg msg) {

        if (BuildConfig.DEBUG) {
            LogUtil.writeLog(TAG, "??????????????????" + msg);
        }
        if (msg.getType() == DelegateSendMsgType.KICK) {
            MsgReCallUtil.notifyKickPersonMsgNoJump(this, msg.getMsgItem(), (Boolean) msg.getObject());
        }
        if (msg.getType() == DelegateSendMsgType.CALL) {
            MsgReCallUtil.notifySendVoiceCall(this, msg.getMsgItem().getSenderuin(), msg.getMsgItem());
        } else if (msg.getType() == DelegateSendMsgType.GAG) {
            MsgReCallUtil.notifyGadPersonMsgNoJump(this, (Long) msg.getObject(), msg.getMsgItem());
        } else if (msg.getType() == DelegateSendMsgType.GROUP) {
            MsgReCallUtil.notifyJoinMsgNoJump(this, msg.getMsgItem(), true);
        } else if (msg.getType() == DelegateSendMsgType.PRIVATE) {
            MsgReCallUtil.notifyJoinMsgNoJump(this, msg.getMsgItem(), true);
        } else if (msg.getType() == DelegateSendMsgType.DEFAULT) {
            MsgReCallUtil.notifyJoinMsgNoJumpDisableAt(this, msg.getMsgItem(), true);
        } else if (msg.getType() == DelegateSendMsgType.AITE) {
            MsgReCallUtil.notifyAtMsgJump(this, msg.getMsgItem().getSenderuin(), msg.getMsgItem().getNickname(), msg.getMsgItem().getMessage(), msg.getMsgItem(), true);
        } else if (msg.getType() == DelegateSendMsgType.SEND_PIC) {
            MsgReCallUtil.notifySendPicMsg(this, (String) msg.getObject(), msg.getMsgItem());
        } else {
            MsgReCallUtil.notifyUniversalMsg(this, msg.getType(), (String) msg.getObject(), msg.getMsgItem(), true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceveAccountDataInsertEvent(AccountAddOrChangeEvent event) {//??????????????????????????? ??? ?????????
        LogUtil.writeLog("??????????????????,??????:" + event.getType());
        if (event.getType() == AccountType.TYPE_QQGROUP_WHITE_NAME) {
            initGroupWhiteNamesFromDb();

        } else if (event.getType() == AccountType.TYPE_QQ_INOGRE_NAME) {
            List<AccountBean> list = DBHelper.getIgnoreQQDBUtil(_dbUtils).queryAll(AccountBean.class);
            iniQQIgnoresMap(list);
        } else if (event.getType() == AccountType.TYPE_QQ_INOGRE_NAME_GAG) {
            List<AccountBean> list = DBHelper.getIgnoreGagDBUtil(_dbUtils).queryAll(AccountBean.class);
            iniQQIgnoresGagMap(list);
        } else if (event.getType() == AccountType.TYPE_SUPER_MANAGER) {
            List<AdminBean> list = DBHelper.getSuperManager(_dbUtils).queryAll(AdminBean.class);
            initSuperManager(list);
        } else if (event.getType() == AccountType.TYPE_GAG) {
            List<GagAccountBean> list = DBHelper.getGagKeyWord(_dbUtils).queryAll(GagAccountBean.class);
            initGagWords(list);
        } else if (event.getType() == AccountType.TYPE_PLUGIN) {
            initJAVAPlugin();
            initLuaPlugin();
            initJavascriptSPlugin();
        } else if (event.getType() == AccountType.TYPE_VAR_MANAGER) {
            //??????????????? ?????? ??????????????????????????????
        } else if (event.getType() == AccountType.TYPE_GROUP_ADMIN) {
            //??????????????? ?????? ??????????????????????????????
        }
    }

    public void initGroupWhiteNamesFromDb() {
        List<GroupWhiteNameBean> list = DBHelper.getQQGroupWhiteNameDBUtil(_dbUtils).queryAll(GroupWhiteNameBean.class);
        iniQQGroupWhiteMap(list);
    }


    @Override
    public boolean onCreate() {
        doOnCreate();
        if (!isAsPluginLoad()) {
            Intent intent = new Intent(getContext(), RemoteService.class);
            getContext().startService(intent);

            try {
                DaemonService.startup(getContext());

            } catch (Throwable e) {
                Log.e(TAG, "multi_dex_error", e);
                //???dex,?????????????????????????????????try,??????????????????
            }

        }
        return true;
    }

    private void doOnCreate() {
        LogUtil.writeLog(TAG, "QSSQ Robot init VERSION CODE:" + BuildConfig.VERSION_CODE + ",as plugin load:" + isAsPluginLoad());
        _uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        _uriMatcher.addURI(Cns.AUTHORITY, ACTION_MSG, CODE_MSG);
        _uriMatcher.addURI(Cns.AUTHORITY, ACTION_GAD, CODE_GAD);
        _uriMatcher.addURI(Cns.AUTHORITY, ACTION_KICK, CODE_TICK);
        _uriMatcher.addURI(Cns.AUTHORITY, ACTION_UPDATE_KEY, CODE_UPDATE_KEY);

        LogUtil.importPackage();
        instance = RobotContentProvider.this;
        mPackageName = getProxyContext().getPackageName();
        long currentTime = new Date().getTime();
        mStatupTime = currentTime;
//        mStatupTime = SPUtils.getValue(getProxyContext(), AppConstants.CONFIG_STARTUPTIME, currentTime);
//        SPUtils.getValue(getProxyContext(), AppConstants.CONFIG_STARTUPTIME, currentTime);//????????????????????????????????????????????????????????????????????????
        if (getProxyContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
          /*  if(EventBus.getDefault().hasSubscriberForEvent(this)){

            }*/
            EventBus.getDefault().register(this);
        } else {
            AppContext.mStartupTime = System.currentTimeMillis();
        }
        if (!isAsPluginLoad()) {
            getContext().registerReceiver(new CodeUpdateReceiver(), new IntentFilter(Cns.UPDATE_CODE_BROADCAST));
//            System.loadLibrary("sqlite3core");//??????????????????
        }
        loadData();
    }

    //TODO ???????????????5.1??????????????????????????????A/libc: Fatal signal 11 (SIGSEGV), code 1, fault addr 0x2829af in tid 5547 (thread_sp_Async)
    @Override
    public void reload() {
        loadData();
    }

    private void loadData() {

        if (isAsPluginLoad()) {
            try {
//                mPakcageContext = (Context) EncryptUtilN.cxc(getProxyContext());
                mPakcageContext = getProxyContext().createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (_dbUtils != null) {
            _dbUtils.close();

        }
        pluginControlInterface = PluginControlmpl.getInstance();
        configQueryImpl = new ConfigQueryImpl();
        _dbUtils = new DBUtils(getProxyContext());
        _dbUtils.setGetDeclared(true);
        InitUtils.initTableAndInsertDefaultValue(_dbUtils);
        LogUtil.writeLog("Robot??????OnCreate");//???appcontext?????????
        initKeyMap(DBHelper.getKeyWordDBUtil(_dbUtils).queryAll(ReplyWordBean.class));
        initGroupWhiteNamesDb();
        initIgnores();
        initSuperManager();
        initGagWords();
        initConfig();
        initJavascriptSPlugin();
        initJAVAPlugin();
        initLuaPlugin();

        String packageName = getProxyContext().getPackageName();
        NickNameUtils.initNicknames(_dbUtils);
        if (packageName.equals(BuildConfig.APPLICATION_ID)) {
            LogUtil.writeLog(TAG, packageName + "??????????????????");
        } else {

            LogUtil.writeLog(TAG, packageName + "???????????????");
        }
    }


    public void initGroupWhiteNamesDb() {
        iniQQGroupWhiteMap(DBHelper.getQQGroupWhiteNameDBUtil(_dbUtils).queryAll(GroupWhiteNameBean.class));
    }

    public void initIgnores() {
        iniQQIgnoresMap(DBHelper.getIgnoreQQDBUtil(_dbUtils).queryAll(AccountBean.class));
    }

    public void initSuperManager() {
        initSuperManager(DBHelper.getSuperManager(_dbUtils).queryAll(AdminBean.class));
    }

    public void initGagWords() {
        initGagWords(DBHelper.getGagKeyWord(_dbUtils).queryAll(GagAccountBean.class));
    }

    public void initLuaPlugin() {
        initLuaPlugin(null);
    }

    public void initLuaPlugin(final INotify iNotify) {
        ConfigQueryImpl.robotContentProvider = this;
        synchronized (PluginUtils.class) {
            if (mLuaPluginList != null) {
                for (IPluginHolder model : mLuaPluginList) {
                    try {
                        model.getPluginInterface().onDestory();

                        if (BuildConfig.DEBUG) {

                            LogUtil.writeLog(TAG, "????????????????????????" + model.getPath());
                        }
                    } catch (Throwable e) {
                        LogUtil.writeLog(TAG, "????????????????????????" + e.getMessage());

                    }
                }
            }
        }
        synchronized (LuaPluginUtil.class) {
            mLuaPluginList.clear();

        }

        new QssqTask<ArrayList<QueryPluginModel>>(new QssqTask.ICallBack<List<QueryPluginModel>>() {
            @Override
            public List<QueryPluginModel> onRunBackgroundThread() {
                List<QueryPluginModel> queryPluginModels = LuaPluginUtil.loadPlugin(getProxyContext(), getProxyClassloader(), new OnCareteNotify() {
                    @Override
                    public boolean onEach(PluginInterface pluginInterface) {
                        pluginInterface.onReceiveControlApi(pluginControlInterface);
                        pluginInterface.onReceiveRobotConfig(configQueryImpl);
                        pluginInterface.onCreate(getProxyContext());
                        return false;
                    }
                });
                return queryPluginModels;
            }

            @Override
            public void onRunFinish(List<QueryPluginModel> o) {
                synchronized (PluginUtils.class) {
                    if (o != null) {
                        mLuaPluginList.addAll(o);
                    }
                    if (iNotify != null) {
                        iNotify.onNotify(null);
                    }
                }

            }
        }).execute();

    }


    public void initJavascriptSPlugin() {
        initJavascriptSPlugin(null);
    }

    public void initJavascriptSPlugin(final INotify iNotify) {
        ConfigQueryImpl.robotContentProvider = this;
        synchronized (mJSPluginList) {
            if (mJSPluginList != null) {
                for (IPluginHolder model : mJSPluginList) {
                    try {
                        model.getPluginInterface().onDestory();

                        if (BuildConfig.DEBUG) {

                            LogUtil.writeLog(TAG, "??????js??????????????????" + model.getPath());
                        }
                    } catch (Throwable e) {
                        LogUtil.writeLog(TAG, "??????js??????????????????" + e.getMessage());

                    }
                }
            }
        }
        synchronized (mJSPluginList) {
            mJSPluginList.clear();

        }


        List<QueryPluginModel> queryPluginModels1 = JSPluginUtil.loadPlugin(getProxyContext(), getProxyClassloader(), new OnCareteNotify() {
            @Override
            public boolean onEach(PluginInterface pluginInterface) {
                pluginInterface.onReceiveControlApi(pluginControlInterface);
                pluginInterface.onReceiveRobotConfig(configQueryImpl);
                pluginInterface.onCreate(getProxyContext());
                return false;
            }
        });
        synchronized (mJSPluginList) {
            if (queryPluginModels1 != null) {
                mJSPluginList.addAll(queryPluginModels1);
            }
            if (iNotify != null) {
                iNotify.onNotify(null);
            }
        }

   /*
        new QssqTask<ArrayList<QueryPluginModel>>(new QssqTask.ICallBack<List<QueryPluginModel>>() {
            @Override
            public List<QueryPluginModel> onRunBackgroundThread() {

                return queryPluginModels;
            }

            @Override
            public void onRunFinish(List<QueryPluginModel> o) {


            }
        }).execute();*/

    }


    public void initJAVAPlugin() {
        initJAVAPlugin(null);
    }

    public void initJAVAPlugin(final INotify iNotify) {
        ConfigQueryImpl.robotContentProvider = this;
        synchronized (PluginUtils.class) {
            if (mPluginList != null) {
                for (IPluginHolder model : mPluginList) {
                    try {
                        model.getPluginInterface().onDestory();

                        if (BuildConfig.DEBUG) {

                            LogUtil.writeLog(TAG, "????????????????????????" + model.getPath());
                        }
                    } catch (Throwable e) {
                        LogUtil.writeLog(TAG, "????????????????????????" + e.getMessage());

                    }
                }
            }
        }
        synchronized (PluginUtils.class) {
            mPluginList.clear();

        }


        new QssqTask<ArrayList<QueryPluginModel>>(new QssqTask.ICallBack<List<QueryPluginModel>>() {

            @Override
            public List<QueryPluginModel> onRunBackgroundThread() {
                List<QueryPluginModel> queryPluginModels = PluginUtils.loadPlugin(getProxyContext(), getProxyClassloader(), new OnCareteNotify() {
                    @Override
                    public boolean onEach(PluginInterface pluginInterface) {
                        pluginInterface.onReceiveControlApi(pluginControlInterface);
                        pluginInterface.onReceiveRobotConfig(configQueryImpl);
                        pluginInterface.onCreate(getProxyContext());
                        return false;
                    }
                });

                return queryPluginModels;
            }

            @Override
            public void onRunFinish(List<QueryPluginModel> o) {
                synchronized (PluginUtils.class) {
                    if (o != null) {
                        mPluginList.addAll(o);
                    }
                    if (iNotify != null) {
                        iNotify.onNotify(null);
                    }
                }

            }
        }).execute();
    }


    private void initKeyMap(List<ReplyWordBean> list) {
        mKeyWordMap.clear();
        if (list != null) {
            for (ReplyWordBean bean : list) {
                doInsertNewKeyBean(bean);
            }
        }
    }

    private void iniQQGroupWhiteMap(List<GroupWhiteNameBean> list) {
        if (list == null) {
            mQQGroupWhiteNames.clear();
            return;
        } else {
            mQQGroupWhiteNames = list;

        }
    }

    private void iniQQIgnoresMap(List<AccountBean> list) {
        if (list == null) {
            mIgnoreQQs.clear();
        } else {
            mIgnoreQQs = list;
        }
       /* for (AccountBean bean : list) {
            mIgnoreQQs.add(bean.getAccount());
        }*/

    }

    private void iniQQIgnoresGagMap(List<AccountBean> list) {
        if (list == null) {
            mIgnoreGagQQs.clear();
        } else {
            mIgnoreGagQQs = list;
        }

    }

    private void initSuperManager(List<AdminBean> list) {
        if (list == null) {

            mSuperManagers.clear();
        } else {
            mSuperManagers = list;
        }


    }

    private void initGagWords(List<GagAccountBean> list) {
        if (list == null) {

            mGagKeyWords.clear();
        } else {
            mGagKeyWords = list;
        }


    }


    private void doInsertNewKeyBean(ReplyWordBean bean) {
        String[] split = bean.getAsk().split(ClearUtil.wordSplit);
        for (String key : split) {
            mKeyWordMap.put(key, bean.getAnswer());
        }
    }

    private void initConfig() {
        sharedPreferences = AppUtils.getConfigSharePreferences(getProxyContext());
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

//        sharedPreferences = getProxyContext().getSharedPreferences(Constants.SP_FILE, Context.MODE_PRIVATE);
        defaultReplyIndex = sharedPreferences.getInt(Cns.SP_DEFAULT_REPLY_API_INDEX, 0);
        String currentKey = sharedPreferences.getString(AppUtils.getRobotReplyKey(defaultReplyIndex), "");
        robotReplySecret = sharedPreferences.getString(AppUtils.getRobotReplySecret(defaultReplyIndex), "");
        if (!TextUtils.isEmpty(currentKey)) {
            LogUtil.writeLog(TAG, "robotReplyKey:" + currentKey);
            robotReplyKey = currentKey;

        } else {
            if (defaultReplyIndex == 1) {
                robotReplyKey = Cns.DEFAULT_TULING_KEY;
            }
        }

        initGroupSpConfig();
        initBaseConfig();
    }


    private void initBaseConfig() {
        mCfprivateReply = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_private_reply), true);
        mUseChildThread = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_use_multi_thread_do_msg), mUseChildThread);
        mAllowPluginInterceptEndMsg = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_allow_intercept_robot_not_aite_final_callmsg), false);
        mAllowReponseSelfCommand = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_robot_self_response_enable_command), mAllowReponseSelfCommand);
        mCfprivateReplyManagrIgnoreRule = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_private_reply_ignore_manager), true);
        mCfBaseReplyShowNickName = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_reply_show_nickname), false);
        mCfBaseReplyNeedAite = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_aite_me_reply), false);
        mCFBaseEnablePlugin = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_enable_plugin), true);
        mCfNotWhiteNameReplyIfAite = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_aite_me_reply_not_whitename_affect), false);
        mCfBaseWhiteNameReplyNotNeedAite = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_white_name_aite_not_need), false);
        IGnoreConfig.distanceNetHistoryTimeIgnore = sharedPreferences.getLong(getResources().getString(R.string.key_base_ignore_second_history_msg), getDefaultIntegerValue(R.integer.key_base_ignore_second_history_msg_duration_second));
        IGnoreConfig.distancedulicateCacheHistory = sharedPreferences.getLong(getResources().getString(R.string.key_base_ignore_than_second_msg), getDefaultIntegerValue(R.integer.key_base_ignore_than_second_msg_duration));
        IGnoreConfig.distanceStatupTimeIgnore = sharedPreferences.getLong(getResources().getString(R.string.key_base_ignore_second_statup_time), getDefaultIntegerValue(R.integer.default_startup_time_distance_ms));
//        IGnoreConfig.distanceStatupTimeIgnore = sharedPreferences.getLong(getResources().getString(R.string.key_base_ignore_second_statup_time), IGnoreConfig.distanceStatupTimeIgnore);
        mCfBaseEnableNetRobotPrivate = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_enable_net_robot_private), true);
        mCfBaseEnableNetRobotGroup = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_enable_net_robot_group), true);
        mCfBaseEnableLocalWord = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_enable_local_reply), true);
        NickNameUtils.disableNickNameCache = !sharedPreferences.getBoolean(getResources().getString(R.string.key_base_enable_nickname_save_db), false);
        mCfBaseDisableAtFunction = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_aite_disible_aite), false);
        mCfBaseDisableGag = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_gag_disible_gag), false);
        mCfBaseNetReplyErrorNotWarn = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_gag_disible_netword_reply_error_not_warn), true);
        mCfBaseDisableStructMsg = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_gag_disible_stuct_msg), false);
        mCFBaseEnableCheckKeyWapGag = sharedPreferences.getBoolean(getResources().getString(R.string.key_base_gag_enable_check_msg), true);
        mCfBaseNoReplyNameStr = sharedPreferences.getString(getResources().getString(R.string.key_base_private_not_reply_person), "35068264");
        mReplyPostFix = sharedPreferences.getString(getResources().getString(R.string.key_base_robot_postfix_word), "");
        mShortUrlTextApiUrl = sharedPreferences.getString(getResources().getString(R.string.key_base_short_url_interface), "");
        musicType = Integer.parseInt(sharedPreferences.getString(getResources().getString(R.string.key_base_robot_music_engine), "0"));
        mLocalRobotName = sharedPreferences.getString(getResources().getString(R.string.key_base_local_var_robot_name), "?????????????????????");
        ClearUtil.wordSplit = sharedPreferences.getString(getResources().getString(R.string.key_base_word_split), ClearUtil.wordSplit);

    }

    private int getDefaultIntegerValue(@IntegerRes int resource) {
        return getResources().getInteger(resource);
    }

    private void initGroupSpConfig() {
        mCfeanbleGroupReply = sharedPreferences.getBoolean(getResources().getString(R.string.key_group_no_draw), mCfeanbleGroupReply);
        IGnoreConfig.groupMsgLessSecondIgnore = sharedPreferences.getLong(getResources().getString(R.string.key_base_group_ignore_less_second_msg), getDefaultIntegerValue(R.integer.default_group_repeat_msg_distance_ignore_ms));
//        mCfOnlyReplyWhiteNameGroup = sharedPreferences.getBoolean(getResources().getString(R.string.key_group_only_draw_group), mCfOnlyReplyWhiteNameGroup);
        mCfOneReplyOneGroupStr = sharedPreferences.getString(getResources().getString(R.string.key_group_only_draw_group_number), "");
        mCfNotReplyGroup = sharedPreferences.getBoolean(getResources().getString(R.string.key_group_not_draw_group), false);
        mCfNotReplyGroupStr = sharedPreferences.getString(getResources().getString(R.string.key_group_not_draw_group_number), "");
        mCfGroupJoinGroupReplyStr = sharedPreferences.getString(getResources().getString(R.string.key_group_join_reply_word), getResources().getString(R.string.default_join_word));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupConfigChange(GroupConfigEvent groupConfig) {
        initGroupSpConfig();
        LogUtil.writeLog("[Grouponfig]" + printGroupConfig());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBaseConfigChange(BaseSettignEvent groupConfig) {
        initBaseConfig();
        LogUtil.writeLog("[Baseonfig]" + printBaseonfig());
    }

    private String printGroupConfig() {
        return "?????????:" + mCfeanbleGroupReply + ",??????????????????" + mCfOnlyReplyWhiteNameGroup + "," + mCfOneReplyOneGroupStr + ",?????????:" + mCfNotReplyGroup + "," + mCfNotReplyGroupStr;
    }

    private String printBaseonfig() {
        return "reply need at:" + mCfBaseReplyNeedAite + ",replyshownickname"
                + mCfBaseReplyShowNickName + "white name mode otheraite enable reply:" + mCfNotWhiteNameReplyIfAite + ",white name not aite:"
                + mCfBaseWhiteNameReplyNotNeedAite;


    }

    @Subscribe
    public void onReveiveForceEvent(ForceEvent event) {
        mForceUpdate = true;
    }

    @Subscribe
    public void onReveiveCheckUpdateFailEvent(ForbitUseAdvanceEvent event) {
        mStopUseAdvanceFunc = true;
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        List<String> value = uri.getQueryParameters("value");
        if (value != null && value.size() > 0) {
            String s = value.get(0);
            s = "????????????????????????" + s;
           /* Cursor cursor = new Cursor() {
            }
            return s*/
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {

        return "";//???????????????reqText
    }


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        if (BuildConfig.DEBUG) {
            mInsertTime = System.currentTimeMillis();
        }
        int match = _uriMatcher.match(uri);//istroop 1000  type -1000 senderuin 83739885 frienduin 577894846
        switch (match) {
            case CODE_MSG:

                LogUtil.writeLog("[?????????]" + values);
                return doMsgLogic(values);
         /*   case CODE_GAD://?????????????????????????????????????????????????????????????????????
                return queryDesDecode(values);
            case CODE_TICK:
                break;*/
            default:
                throw new IllegalArgumentException("uri?????????: " + uri + ",match:" + match);
        }
    }


    @Nullable
    private Uri doMsgLogic(final ContentValues values) {
        if (mForceUpdate) {
            return getFailUri("please update");
        }

        final MsgItem item = RobotUtil.contentValuesToMsgItem(values);


        initSelfAccont(item);
        if ("proxy_send_msg".equals(item.getApptype())) {
            if (mItem != null && !mItem.getSelfuin().equals(item.getSelfuin())) {


                String robot = mItem.getSelfuin();
                if (item.getSenderuin().equals(item.getSelfuin())) {
                    item.setSenderuin(robot);
                }
                item.setSelfuin(robot);


            } else {


            }
            this.notifyChange(RobotUtil.msgItemToUri(item), null);
            return getSuccUri("????????????????????????");
        } else {
            if (RemoteService.isIsInit() && TextUtils.isEmpty(mRobotQQ)) {
                mRobotQQ = RemoteService.queryLoginQQ();
                if (!TextUtils.isEmpty(mRobotQQ) && !mRobotQQ.equals(item.getSelfuin())) {

                    if (item.getSenderuin().equals(item.getSelfuin())) {
                        item.setSenderuin(mRobotQQ);
                    }
                    item.setSelfuin(mRobotQQ);
                }

            }
        }

        mItem = item;
        boolean shouldFromMainThread = (((System.currentTimeMillis() - mStatupTime * 1.0f) / 1000) < 20 || !mUseChildThread || !RemoteService.isIsInit());
        Observable<Uri> objectObservable = Observable.create(new ObservableOnSubscribe<Uri>() {
            @Override
            public void subscribe(ObservableEmitter<Uri> emitter) throws Exception {

                Uri value = doMsgLogicAtThread(item);
             /*   if (BuildConfig.DEBUG) {
                    String name = Thread.currentThread().getName();
                    LogUtil.writeLog("?????????:" + name);
                }*/
                emitter.onNext(value);
            }
        });

        if (BuildConfig.DEBUG && mUseChildThread) {
            objectObservable = objectObservable.subscribeOn(Schedulers.io());
        }
        if (BuildConfig.DEBUG && !mUseChildThread) {
            objectObservable = objectObservable.subscribeOn(AndroidSchedulers.mainThread());
        } else if ("test".equals(item.getApptype()) || shouldFromMainThread) {
            objectObservable = objectObservable.subscribeOn(Schedulers.io());
        } else {
            objectObservable = objectObservable.subscribeOn(Schedulers.io());

        }
        objectObservable.subscribe(new Consumer<Uri>() {
            @Override
            public void accept(Uri o) throws Exception {
                if ("test".equals(item.getApptype())) {
                    LogUtil.writeLog("msg_exe_over:" + o);
                    try {

                        JSONObject jsonObject = new JSONObject(o.toString());
                        String string = jsonObject.optString(MsgTypeConstant.msg);
                        int anInt = jsonObject.getInt(MsgTypeConstant.code);
                        if (anInt == 0) {
                            AppContext.showToast("????????????:" + string);
                        } else {
                            AppContext.showToast("???????????????: " + string + ", code=" + anInt);
                        }
                    } catch (Exception e) {

                    }
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if ("test".equals(item.getApptype())) {

                    AppContext.showToast("???????????????:" + Log.getStackTraceString(throwable));
                } else {
                    MobclickAgent.reportError(getContext(), throwable);
                    LogUtil.writeLog("??????????????????:" + Log.getStackTraceString(throwable));
                }
            }
        });
        return getSuccUri("??????????????????:?????????");

    }

    private Uri doMsgLogicAtThread(MsgItem item) {


        if (isIgnoreAccount(item)) {
            return getFailUri("?????????????????????????????????");

        }
        boolean isManager;

        //????????????????????? ???????????????????????????
        isManager = isManager(item);


        boolean foolMode = item.getMessage().equals(CmdConfig.RESPONSE_All_CMD) && (isManager || item.getSelfuin().equals(item.getSenderuin()));

        String errMsg = filterRepeatMsgNotNullResutnErrMsg(item);
        if (errMsg != null) {
            if (foolMode) {
                MsgReCallUtil.notifyHasDoWhileReply(this, item.setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + errMsg));
            }
            return getFailUri(errMsg);
        }
        boolean doMsg = true;//???????????????????????????


//        Pair<Integer, Uri> needDoWhileMsgIs = null;
        androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair = null;
        boolean isGroupMsg = MsgTyeUtils.isGroupMsg(item);
        if (isGroupMsg) {
            atPair = ConfigUtils.clearAndFetchAtArray(item);


        } else {
            List<GroupAtBean> list = new ArrayList<>();//create empty
            atPair = new androidx.core.util.Pair<>(false, androidx.core.util.Pair.create(false, list));
        }
        boolean selfMsg = MsgTyeUtils.isSelfMsg(item);
        if (selfMsg && !MsgTyeUtils.isRedPacketMsg(item)) {

            GroupWhiteNameBean nameBean = isGroupMsg ? (GroupWhiteNameBean) getGroupConfig(item.getFrienduin()) : null;
            if (nameBean == null) {
                nameBean = new GroupWhiteNameBean();
            }

            boolean b = doCommendLogic(item, true, selfMsg, atPair, INeedReplayLevel.ANY, isGroupMsg, nameBean);
            return getFailUri("??????????????????????????????,????????????????????????????????????,?????????????????????");
        }

        //JS????????????
        Pair<IPluginHolder, Boolean> pluginIntercept = doPluginLogic(item, mJSPluginList, atPair, IPluginRequestCall.FLAG_RECEIVE_MSG);
        if (pluginIntercept.second) {

            if (foolMode) {
                MsgReCallUtil.notifyHasDoWhileReply(this, item.setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "?????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "??????"));
            }
            return getSuccUri("????????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "js????????????,??????????????????????????????,??????????????????");
        }

        //lua????????????
        pluginIntercept = doPluginLogic(item, mLuaPluginList, atPair, IPluginRequestCall.FLAG_RECEIVE_MSG);
        if (pluginIntercept.second) {

            if (foolMode) {
                MsgReCallUtil.notifyHasDoWhileReply(this, item.setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "?????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "??????"));
            }
            return getSuccUri("????????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "Lua????????????,??????????????????????????????,??????????????????");
        }

        //java??????
        pluginIntercept = doPluginLogic(item, mPluginList, atPair, IPluginRequestCall.FLAG_RECEIVE_MSG);
        if (pluginIntercept.second) {

            if (foolMode) {
                MsgReCallUtil.notifyHasDoWhileReply(this, item.setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "?????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "??????"));
            }
            return getSuccUri("????????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "Java????????????,??????????????????????????????,??????????????????");
        }


        DoWhileMsg msgBean = isNeedDoWhileMsg(item, isGroupMsg, isManager);


        if (isGroupMsg && mCfOnlyReplyWhiteNameGroup) {


            return doGroupWhiteNames(item, msgBean, atPair, isManager, selfMsg, foolMode);
        }


//            Pair<Uri, Integer> pair1 = needDoWhileMsg.getPair();
        if (msgBean.getPair().second < INeedReplayLevel.ANY) {//?????????????????????????????????
            if (isManager) {
                boolean result = doCommendLogic(item, selfMsg, isManager, atPair, msgBean.getPair().second, isGroupMsg);


                if (foolMode) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, item.setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "????????????????????????????????????,???????????????:" + result));
                }
            }


            return msgBean.getPair().first;

        } else {

            if (isGroupMsg) {
                FloorUtils.onReceiveNewMsg(_dbUtils, item);//?????????
            }

            if (!isManager && isGroupMsg && mCFBaseEnableCheckKeyWapGag && !mIgnoreGagQQs.contains(item.getSenderuin())) {

                PairFix<GagAccountBean, String> pair = keyMapContainGag(item.getMessage(), false);
                if (pair != null) {
                    doGagImpLogic(item, pair, item.getMessage());//
                }
            }

            boolean result = doCommendLogic(item, isManager, selfMsg, atPair, msgBean.getPair().second, isGroupMsg);
            if (result) {

                if (foolMode) {


                    MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "??????????????????:" + result));
                }
                return getSuccUri("??????????????????????????????");
            }

//????????????????????????????????????.

        }


        if (doMsg) {
            if (isGroupMsg) {//?????????????????????

                String frequentMessage = FrequentMessageDetectionUtils.doCheckFrequentMessage(item, null);
                Uri uri = CheckUtils.doIsNeedRefreshGag(this, frequentMessage, item, null);
                if (uri != null) {
                    return getFailUri(frequentMessage);
                }
                if (!mCfBaseEnableLocalWord) {

                    if (foolMode) {


                        MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "??????????????????????????????"));
                    }
                    return getFailUri("??????????????????????????????");
                }

                Pair<Boolean, Uri> booleanUriPairHasIntercept = doLocalWordSucc(item, atPair, null, isGroupMsg);
                if (booleanUriPairHasIntercept.first) {
                    return booleanUriPairHasIntercept.second;

                }

                if (!mCfBaseEnableNetRobotGroup) {

                    if (foolMode) {


                        MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "??????????????????????????????"));
                    }
                    return getFailUri("??????????????????????????????");
                }


                if (TextUtils.isEmpty(StringUtils.deleteAllSpace(item.getMessage()))) {
                    if (atPair.first) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, IGnoreConfig.EMPTY_REPLY_WORD, item);


                        if (foolMode) {


                            MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + item.getMessage()));
                        }
                        return getSuccUri("??????????????????????????????");


                    } else {


                        if (foolMode) {


                            MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "????????????,????????????,??????????????????"));
                        }
                        return getFailUri("????????????,????????????,??????????????????");

                    }
                } else {

                    if (foolMode) {


                        MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "????????????"));

                    }
                    return getFailUri("????????????");
                }


            } else {

                if (!mCfprivateReply) {

                    if (foolMode) {


                        MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "??????????????????"));
                    }
                    return getFailUri("?????????????????????????????????,????????????????????????????????????,?????????????????????/????????????");
                }


                if (mCfBaseEnableLocalWord) {


                    Pair<Boolean, Uri> booleanUriPairHasIntercept = doLocalWordSucc(item, atPair, null, isGroupMsg);
                    if (booleanUriPairHasIntercept.first) {

                        if (foolMode) {


                            MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "??????????????????" + booleanUriPairHasIntercept.second));
                        }
                        return booleanUriPairHasIntercept.second;

                    }


                } else {

                    if (!mCfBaseEnableNetRobotPrivate) {
                        if (foolMode) {


                            mCfBaseEnableNetRobotPrivate = true;
                            mCfBaseEnableNetRobotGroup = true;
                            mCfBaseEnableLocalWord = true;
                            MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "???????????????????????????????????????,???????????????,???????????????????????????"));

                        }
                        return getFailUri("???????????????????????????????????????");
                    } else {
                        mCfBaseEnableLocalWord = true;

                        if (foolMode) {
                            mCfBaseEnableNetRobotPrivate = true;
                            MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "???????????????????????????,???????????????,???????????????????????????"));

                        }


                    }
                }


                if (mCfBaseEnableNetRobotPrivate) {

                    LogUtil.writeLog("??????????????????" + item);
                    RequestBean requestBean = new RequestBean();
                    requestBean.setKey(robotReplyKey);
                    requestBean.setUserid(item.getSenderuin());
                    String message = item.getMessage();
                    if (TextUtils.isEmpty(message)) {

                        return getSuccUri("waht's your problem?");
                    }

                    queryNetReplyResult(item, requestBean, (GroupWhiteNameBean) new GroupWhiteNameBean().setAccount(item.getFrienduin()));

                    return getSuccUri();
                } else {

                    if (foolMode) {
                        mCfBaseEnableNetRobotPrivate = true;
                        MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "???????????????????????????,???????????????,???????????????????????????"));

                    }


                    return getFailUri("?????????????????????????????????");
                }


            }

        } else {
            return getFailUri("????????????,??????");
        }

    }

    private void initSelfAccont(MsgItem item) {
        if (mRobotQQ == null) {
            if (!TextUtils.isEmpty(item.getSelfuin())) {
                mRobotQQ = item.getSelfuin();
                SPUtils.setValue(getContext(), AppConstants.CONFIG_SELFUIN, mRobotQQ);
            } else {
                mRobotQQ = SPUtils.getValue(getContext(), AppConstants.CONFIG_SELFUIN, "");
            }
        }
        if (TextUtils.isEmpty(item.getSenderuin())) {
            item.setSenderuin(mRobotQQ);
        }

        if (TextUtils.isEmpty(item.getSelfuin())) {
            item.setSelfuin(mRobotQQ);
        }
    }

    public static boolean isIgnoreAccount(MsgItem item) {
        if (item.getSenderuin().equals("1000000")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ???????????????????????????
     *
     * @param item
     * @param msgBean
     * @param atPair
     * @param isManager
     * @param isSelf
     * @param foolMode
     * @return
     */
    private Uri doGroupWhiteNames(final MsgItem item, DoWhileMsg msgBean, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, boolean isManager, boolean isSelf, boolean foolMode) {
        if (msgBean.getWhiteNameBean() != null) {
            final GroupWhiteNameBean whiteNameBean = msgBean.getWhiteNameBean();

            if (item.getType() == MsgTypeConstant.MSG_TYPE_JOIN_GROUP) {


                LogUtil.writeLog("??????????????????, ");
                if (!whiteNameBean.isJoingroupreply()) {
                    LogUtil.writeLog("????????????????????????, ");
                    return getFailUri("?????????????????????????????????");

                }

                String message = item.getMessage();
                String messageStr = (message + "").replace(" ", "");
                int i = messageStr.indexOf(",153016267,");
                if (messageStr.startsWith("???????????????????????????") && i >= 15) {//24
                    MsgReCallUtil.notifyJoinMsgNoJump(this, item.setMessage("?????????????????????boss???????????????,????????????"));
                }


                String messageFilter = item.getMessage();
                //?????????????????? ???????????????                    ##**##1,5,0,6,0,2168531679,icon,0,0,color,0(???474240677)
                //????????? ???????????? ??????????????????????????????TA????????????                 ##**##2,5,4,8,0,153016267,icon,0,0,color,0,19,15,25,153016267,icon,0,0,color,0

                //Util][3ms]??????????????????_??????_.??????note4x ?????? N......N ???????????? ##**##2,5,0,15,0,153016267,icon,0,0,color,0,5,19,27,0,35068264,icon,0,0,color,0?????????cn.qssq666.keeprun_????????????,????????????:false
                String nickname = null;
                nickname = StringUtils.getStrCenter(messageFilter, "?????? ", " ??????");


                if (nickname == null) {
                    nickname = StringUtils.getStrLeft(messageFilter, "???????????????");
                }


                if (nickname == null) {
                    nickname = StringUtils.getStrLeft(messageFilter, "???????????????");
                }


                item.setNickname(nickname);
                ;

                String patternString = null;

                if (messageFilter.contains("??????")) {
//                    patternString     = ".*?\\,[0-9]+\\,([0-9]{5,11})\\,icon\\,[0-9]+\\,[0-9]+\\,color\\,[0-9]+.*?";// ignore_include

                    patternString = ".*?\\,?[0-9]{1,3}\\,?[0-9]{1,3}?\\,?[0-9]{1,3}?\\,?([0-9]{5,12})\\,icon\\,[0-9]{1,3}\\,[0-9]{1,3}\\,color\\,[0-9]{1,3}\\,[0-9]{1,3}?\\,?[0-9]{1,3}?\\,?[0-9]{1,3}?\\,[0-9]{1,3}?\\,?([0-9]{4,12})\\,icon\\,[0-9]{1,3}\\,[0-9]{1,3}.*?";// ignore_include

                } else {
                    //N......N ???????????????                    ##**##1,5,0,8,0,35068264,icon,0,0,color,0

                    patternString = ".*?\\,[0-9]+\\,([0-9]{5,11})\\,icon\\,[0-9]+\\,[0-9]+\\,color\\,[0-9]+.*?";//ignore_include
//                    patternString = ".*?\\,?[0-9]{1,3}\\,?[0-9]{1,3}?\\,?[0-9]{1,3}?\\,?([0-9]{5,12})\\,icon\\,[0-9]{1,3}\\,[0-9]{1,3}\\,color\\,[0-9]{1,3}\\,[0-9]{1,3}?\\,?[0-9]{1,3}?\\,?[0-9]{1,3}?\\,[0-9]{1,3}?\\,?([0-9]{4,12})\\,icon\\,[0-9]{1,3}\\,[0-9]{1,3}.*?";// ignore_include
//                    patternString     = ".*?,color\\,\\,[0-9]+\\,([0-9]{5,11})\\,icon\\,[0-9]+\\,[0-9]+\\,color\\,[0-9]+.*?";// ignore_include

                }
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(messageFilter);
                String sendUin = "";
                if (matcher.find()) {
                    int i1 = matcher.groupCount();

                    sendUin = matcher.group(i1);// ???
                } else {
                    MsgReCallUtil.notifyJoinMsgNoJump(this, item.getMessage(), item);


                    return getFailUri("????????????????????????????????????????????????????????????");
                }
                if (TextUtils.isEmpty(nickname)) {
                    nickname = sendUin;
                }

                item.setSenderuin(sendUin);
                final String finalNickname = nickname;

                item.setNickname(nickname);

                //??????, ??? 1449924790 ????????????$group???,??????????????????!
                String joingroupword = whiteNameBean.getJoingroupword();
                joingroupword = joingroupword.replace("$group", whiteNameBean.getRemark() + "");
                LogUtil.writeLog(String.format("????????????????????????, ??????????????? %s qq %s ??????%s group:%s ", whiteNameBean.getJoingroupword(), item.getSenderuin(), finalNickname, item.getFrienduin()));
                final String finalJoingroupword = joingroupword;
                //js
                Pair<IPluginHolder, Boolean> pluginIntercept = doPluginLogic(item, mJSPluginList, atPair, IPluginRequestCall.FLAG_RECEIVE_JOIN_MSG);
                if (pluginIntercept.second) {

                    if (foolMode) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, item.setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "?????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "??????"));
                    }
                    return getSuccUri("???????????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "JS????????????,??????????????????????????????,??????????????????");
                }
                //lua

                pluginIntercept = doPluginLogic(item, mLuaPluginList, atPair, IPluginRequestCall.FLAG_RECEIVE_JOIN_MSG);
                if (pluginIntercept.second) {

                    if (foolMode) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, item.setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "?????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "??????"));
                    }
                    return getSuccUri("???????????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "Lua????????????,??????????????????????????????,??????????????????");
                }

//java
                pluginIntercept = doPluginLogic(item, mPluginList, atPair, IPluginRequestCall.FLAG_RECEIVE_JOIN_MSG);
                if (pluginIntercept.second) {

                    if (foolMode) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, item.setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "?????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "??????"));
                    }
                    return getSuccUri("???????????????" + pluginIntercept.first.getPluginInterface().getPluginName() + "JAVA????????????,??????????????????????????????,??????????????????");
                }


                //?????????????????????, ??????????????? ????????????$group???,??????????????????! qq 35068264 ??????N......N group:122328962
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MsgReCallUtil.notifyAtMsgJump(RobotContentProvider.this, item.getSenderuin(), finalNickname + "", finalJoingroupword, item);

                    }
                }, 2000);


            } else {
//????????????

                boolean isIgnoreQQ = mIgnoreGagQQs.contains(item.getSenderuin());

                boolean isCurrentGroupAdmin = isManager || isSelf ? false : isCurrentGroupAdminFromDb(msgBean.getWhiteNameBean(), item.getSenderuin(), item.getFrienduin(), false);//??????????????????????????????

                boolean checkMode = true;
                checkModeTag:
                while (checkMode) {

                    checkMode = false;

                    if (item.getType() == MsgTypeConstant.MSG_TYPE_REDPACKET_1 || item.getType() == MsgTypeConstant.MSG_TYPE_REDPACKET) {

                        if (!whiteNameBean.isAdmin()) {

                            return getSuccUri("????????????????????????????????????");
                        }

                        if (isManager || isIgnoreQQ || isCurrentGroupAdmin) {
                            return getSuccUri("??????????????????????????????????????????");
                        }


                        if (msgBean.getObject() instanceof RedpacketBaseInfo) {
                            RedpacketBaseInfo info = (RedpacketBaseInfo) msgBean.getObject();
                            int redpacketType = info.getType();


                            if (whiteNameBean.isRedpackettitlebanedword()) {
                                boolean b = keyMapContainGagFromRedpacket(item, info.getTitle(), info.getType());//??????????????????

                                LogUtil.writeLog(String.format("???%s ?????????????????????????????????: %b,", item.getFrienduin(), b));
                                if (b) {
                                    CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.REDPACKET, "????????????????????????: " + info.getTitle());

                                    return getFailUri("??????????????????");
                                }

                            } else {
                                LogUtil.writeLog(String.format("???%s??????????????????????????????", item.getFrienduin()));
                            }

                            if (redpacketType == RedPacketMessageType.PASSWORD) {
                                if (whiteNameBean.isBanpasswordredpacket()) {
                                    MsgReCallUtil.notifyGadPersonMsgNoJump(this, item.getFrienduin(), item.getSenderuin(), ParseUtils.parseGagStr2Secound("1???"), item);


                                    if (!TextUtils.isEmpty(whiteNameBean.getBanredpackettip())) {

                                        CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.REDPACKET, "???????????????????????????: ");

                                        String typeMsg = String.format(whiteNameBean.getBanredpackettip(), "??????");
                                        if (whiteNameBean.isBannedaite()) {

                                            MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), item.getNickname(), "" + typeMsg, item);
                                        } else {

                                            MsgReCallUtil.notifyHasDoWhileReply(this, typeMsg, item);
                                        }
                                        LogUtil.writeLog(String.format("???%s?????? %s????????????????????? %s ", item.getFrienduin(), item.getSenderuin(), typeMsg));

                                    }


                                    if (whiteNameBean.isBreaklogic()) {
                                        return getFailUri("???????????????");
                                    }
                                } else {
                                    LogUtil.writeLog(String.format("???%s???????????????????????????", item.getFrienduin()));
                                }


                            } else if (MsgTyeUtils.isVoiceRedPacket(redpacketType)) {
                                if (whiteNameBean.isBanvoiceredpacket()) {
                                    MsgReCallUtil.notifyGadPersonMsgNoJump(this, item.getFrienduin(), item.getSenderuin(), ParseUtils.parseGagStr2Secound("1???"), item);

                                    if (!TextUtils.isEmpty(whiteNameBean.getBanredpackettip())) {

                                        String typeMsg = String.format(whiteNameBean.getBanredpackettip(), "????????????");
                                        if (whiteNameBean.isBannedaite()) {

                                            MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), item.getNickname(), "" + typeMsg, item);
                                        } else {
                                            MsgReCallUtil.notifyHasDoWhileReply(this, typeMsg, item);

                                        }

                                        LogUtil.writeLog(String.format("???%s?????? %s??????????????? %s", item.getFrienduin(), item.getSenderuin(), typeMsg));
                                        CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.REDPACKET, "?????????????????????????????????: ");


                                    }


                                    if (whiteNameBean.isBreaklogic()) {
                                        return getFailUri("???????????????");
                                    }

                                } else {
                                    LogUtil.writeLog(String.format("???%s?????????????????????????????????", item.getFrienduin()));
                                }

                            } else if (redpacketType == RedPacketMessageType.EXCLUSIVE) {

                                if (!whiteNameBean.isAdmin()) {

                                    return getSuccUri("????????????????????????????????????zhuanshu ");
                                }

                                if (isManager || isIgnoreQQ || isCurrentGroupAdmin) {
                                    return getSuccUri("??????????????????????????????");
                                }


                                if (whiteNameBean.isBanexclusiveredpacket()) {


                                    MsgReCallUtil.notifyGadPersonMsgNoJump(this, item.getFrienduin(), item.getSenderuin(), ParseUtils.parseGagStr2Secound("1???"), item);

                                    if (!TextUtils.isEmpty(whiteNameBean.getBanredpackettip())) {

                                        String typeMsg = String.format(whiteNameBean.getBanredpackettip(), "??????");
                                        CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.REDPACKET, "???????????????????????????: ");


                                        if (whiteNameBean.isBannedaite()) {

                                            MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), item.getNickname(), "" + typeMsg, item);
                                        } else {

                                            MsgReCallUtil.notifyHasDoWhileReply(this, typeMsg, item);
                                        }

                                        LogUtil.writeLog(String.format("???%s?????? %s??????????????? %s", item.getFrienduin(), item.getSenderuin(), typeMsg));
                                    }

                                    if (whiteNameBean.isBreaklogic()) {
                                        return getFailUri("???????????????");
                                    }


                                } else {
                                    LogUtil.writeLog(String.format("???%s?????????????????????????????????", item.getFrienduin()));
                                }

                            } else if (redpacketType == RedPacketMessageType.NORMAL) {
                                if (!whiteNameBean.isAdmin()) {

                                    return getSuccUri("????????????");
                                }

                                if (isManager || isIgnoreQQ || isCurrentGroupAdmin) {
                                    return getSuccUri("????????????");
                                }


                                if (whiteNameBean.isBanexclusiveredpacket()) {
                                    MsgReCallUtil.notifyGadPersonMsgNoJump(this, item.getFrienduin(), item.getSenderuin(), ParseUtils.parseGagStr2Secound("1???"), item);


                                    if (!TextUtils.isEmpty(whiteNameBean.getBanredpackettip())) {
                                        String typeMsg = String.format(whiteNameBean.getBanredpackettip(), "??????");
                                        if (whiteNameBean.isBannedaite()) {

                                            MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), item.getNickname(), "" + typeMsg, item);
                                        } else {

                                            MsgReCallUtil.notifyHasDoWhileReply(this, typeMsg, item);
                                        }


                                        LogUtil.writeLog(String.format("???%s?????? %s??????????????? %s", item.getFrienduin(), item.getSenderuin(), typeMsg));
                                        CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.REDPACKET, "?????????????????????: ");


                                    }

                                    if (whiteNameBean.isBreaklogic()) {
                                        return getFailUri("???????????????");
                                    }


                                } else {
                                    LogUtil.writeLog(String.format("???%s?????????????????????????????????", item.getFrienduin()));
                                }

                            }


                        }

                    } else {

                        if (BuildConfig.DEBUG && item.getMessage().contains("[??????]")) {
                            LogUtil.writeLog("??????????????????...");
                        }

                        if (!whiteNameBean.isAdmin()) {

                            break checkModeTag;
                        }

                        if (isManager || isIgnoreQQ || isCurrentGroupAdmin) {
                            break checkModeTag;
                        }


                        if (MsgTyeUtils.isPicMsg(item)) {

                            LogUtil.writeLog(String.format("???%s?????????%s????????? ,????????????:%d", item.getFrienduin(), item.getSenderuin(), item.getType()));

                            if (whiteNameBean.isBanpic()) {

                                int minute = whiteNameBean.getPicgagsecond();
                                if (minute < 1) {
                                    LogUtil.writeLoge("????????????????????????????????????,??????????????????60 " + minute);
                                } else {

                                    MsgItem gagItem = item.clone();
                                    MsgReCallUtil.notifyGadPersonMsgNoJump(this, gagItem.getFrienduin(), gagItem.getSenderuin(), minute * 60, gagItem);
                                    if (whiteNameBean.isRevokemsg()) {
                                        MsgItem revokeItem = item.clone();
                                        MsgReCallUtil.notifyRevokeMsgJump(this, revokeItem.getFrienduin(), revokeItem.getSenderuin(), revokeItem.getMessageID(), revokeItem);
                                    }
                                    CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.OTHER_Violation, "?????????????????????: ");


                                    if (!TextUtils.isEmpty(whiteNameBean.getPicgagsecondtip())) {
                                        if (whiteNameBean.isBannedaite()) {

                                            MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), item.getNickname(), "" + whiteNameBean.getPicgagsecondtip(), item);
                                        } else {

                                            MsgReCallUtil.notifyHasDoWhileReply(this, "" + whiteNameBean.getPicgagsecondtip(), item);
                                        }

                                    }

                                }

//                                MsgReCallUtil.notifyGadPersonMsgNoJump(this, picgagsecond, item);
                                String format = String.format("???%s ??????%s???????????????????????????", item.getFrienduin(), item.getSenderuin());
                                LogUtil.writeLog(format);
                                return getFailUri(format);
                            } else {
                                LogUtil.writeLog(String.format("%s ???  ??????????????????????????????", item.getFrienduin()));

                                if (TextUtils.isEmpty(item.getMessage())) {
                                    return getFailUri("??????????????????");
                                }
                            }


                        } else if (MsgTyeUtils.isVideoMsg(item)) {


                            if (!whiteNameBean.isAdmin()) {
                                return getSuccUri("video msg ignore");
                            }

                            if (isManager || isIgnoreQQ || isCurrentGroupAdmin) {
                                return getSuccUri("video msg ignore");
                            }


                            if (whiteNameBean.isBannevideo()) {
                                long minute = whiteNameBean.getVoicegagminute();
                                if (minute < 1) {
                                    LogUtil.writeLoge("?????????video?????? ????????????minute??????,?????????????????? " + minute + "??????");
                                } else {

                                    MsgReCallUtil.notifyGadPersonMsgNoJump(this, item.getFrienduin(), item.getSenderuin(), minute * 60, item);

                                    CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.OTHER_Violation, "?????????????????????: ");
                                    if (whiteNameBean.isRevokemsg()) {
                                        MsgReCallUtil.notifyRevokeMsgJump(this, item.getFrienduin(), item.getSenderuin(), item.getMessageID(), item);
                                    }

                                    if (!TextUtils.isEmpty(whiteNameBean.getVideogagtip())) {
                                        if (whiteNameBean.isBannedaite()) {

                                            MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), item.getNickname(), "" + whiteNameBean.getVideogagtip(), item);
                                        } else {


                                            MsgReCallUtil.notifyHasDoWhileReply(this, "" + whiteNameBean.getVideogagtip(), item);
                                        }

                                    }


                                }


                                String format = String.format("???%s ??????%s????????????video?????????", item.getFrienduin(), item.getSenderuin());
                                LogUtil.writeLog(format);
                                return getFailUri(format);
                            } else {
                                return getSuccUri("video msg ignore");
                            }


                        } else if (item.getType() == MsgTypeConstant.MSG_TYPE_STRUCT_MSG) {
                            if (whiteNameBean.isBancardmsg()) {
                                long minute = whiteNameBean.getCardmsgminute();
                                if (minute < 1) {
                                    LogUtil.writeLoge("????????????????????? ?????????????????????,?????????????????? " + minute + "??????");
                                }

                                MsgReCallUtil.notifyGadPersonMsgNoJump(this, item.getFrienduin(), item.getSenderuin(), minute * 60, item);

                                CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.OTHER_Violation, "?????????????????????: ");

                                if (whiteNameBean.isRevokemsg()) {
                                    MsgReCallUtil.notifyRevokeMsgJump(this, item.getFrienduin(), item.getSenderuin(), item.getMessageID(), item);
                                }
                                if (!TextUtils.isEmpty(whiteNameBean.getCardmsggagtip())) {
                                    if (whiteNameBean.isBannedaite()) {

                                        MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), item.getNickname(), "" + whiteNameBean.getCardmsggagtip(), item);

                                    } else {

                                        MsgReCallUtil.notifyHasDoWhileReply(this, "" + whiteNameBean.getCardmsggagtip(), item);
                                    }

                                }


                                String format = String.format("???%s ??????%s???????????????????????????", item.getFrienduin(), item.getSenderuin());
                                LogUtil.writeLog(format);
                                return getFailUri(format);


                            } else {

                                return getSuccUri("??????????????????");
                            }


                        } else if (item.getType() == MsgTypeConstant.MSG_TYPE_MEDIA_PTT) {

                            if (!whiteNameBean.isAdmin()) {

                                return getSuccUri("????????????");
                            }

                            if (isManager || isIgnoreQQ || isCurrentGroupAdmin) {
                                return getSuccUri("????????????");
                            }


                            if (whiteNameBean.isBanvoice()) {


                                long minute = whiteNameBean.getVoicegagminute();
                                if (minute < 1) {
                                    LogUtil.writeLoge("??????????????? ?????????????????????,?????????????????? " + minute + "??????");
                                } else {
                                    MsgReCallUtil.notifyGadPersonMsgNoJump(this, item.getFrienduin(), item.getSenderuin(), minute * 60, item);

                                    CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.VOICE, "???????????????: ");
                                    if (whiteNameBean.isRevokemsg()) {
                                        MsgReCallUtil.notifyRevokeMsgJump(this, item.getFrienduin(), item.getSenderuin(), item.getMessageID(), item);
                                    }

                                    if (!TextUtils.isEmpty(whiteNameBean.getVoicegagtip())) {
                                        if (whiteNameBean.isBannedaite()) {

                                            MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), item.getNickname(), "" + whiteNameBean.getVoicegagtip(), item);

                                        } else {
                                            MsgReCallUtil.notifyHasDoWhileReply(this, "" + whiteNameBean.getVoicegagtip(), item);

                                        }
                                    }

                                    String format = String.format("???%s ??????%s???????????????????????????", item.getFrienduin(), item.getSenderuin());
                                    LogUtil.writeLog(format);
                                    return getFailUri(format);
                                }

                            } else {

                                return getSuccUri("??????????????????");
                            }


                        }


                        if (!whiteNameBean.isAdmin()) {

                            break checkModeTag;
                        }

                        if (isManager || isIgnoreQQ || isCurrentGroupAdmin) {
                            break checkModeTag;
                        }


                        if (whiteNameBean.isFrequentmsg()) {

                            String frequentMessage = FrequentMessageDetectionUtils.doCheckFrequentMessage(item, whiteNameBean);


                            Uri uri = CheckUtils.doIsNeedRefreshGag(this, frequentMessage, item, whiteNameBean);


                            if (uri != null) {

                                CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.frequent_Violation, "????????????: ");
                                if (whiteNameBean.isRevokemsg()) {
                                    MsgReCallUtil.notifyRevokeMsgJump(this, item.getFrienduin(), item.getSenderuin(), item.getMessageID(), item);
                                }

                                return getFailUri(frequentMessage);
                            }
                        }

                        if (whiteNameBean.isNicknameban()) {

                            PairFix<GagAccountBean, String> pair = keyMapContainGag(item.getNickname(), false);
                            if (pair != null) {
                                //doCheckGagCountThan
                                CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.NICKNAME_Violation, "" + item.getNickname() + " ?????????????????????");


                                doGagImpLogic(item, pair, item.getNickname(), " ??????????????????????????????");//
                                LogUtil.writeLog(String.format("???%s ????????????????????? %s ?????????", item.getFrienduin(), pair.first.getAccount() + ""));
                                return getFailUri("??????????????????");

                            } else {
                                LogUtil.writeLog("????????????");

                            }


                        } else {
                            LogUtil.writeLog(String.format("???%s ??????????????????????????????", item.getFrienduin()));
                        }

                        if (whiteNameBean.isIllegalnickname()) {//???????????????
                            String groupnickanmekeyword = whiteNameBean.getGroupnickanmekeyword();

                            if (TextUtils.isEmpty(groupnickanmekeyword)) {
                                LogUtil.writeLoge(String.format("???%s ???????????????????????????", item.getFrienduin()));
                            }
                            boolean matches;

                            boolean isMatch = false;
                            try {

                                Pattern pattern = Pattern.compile(groupnickanmekeyword);
                                Matcher matcher = pattern.matcher(item.getNickname());
                                if (matcher.find()) {

                                    if (matcher.groupCount() >= 2) {

                                        int current = 1;
                                        ArrayList<String> checkList = new ArrayList<String>(matcher.groupCount());
                                        boolean repeatWord = false;
                                        nickcheck:
                                        while (current <= matcher.groupCount()) {
                                            String currentStr = matcher.group(current);
                                            if (checkList.contains(currentStr)) {
                                                repeatWord = true;
                                                String error = "??????????????????????????????????????????????????? " + item.getNickname();

                                                LogUtil.writeLoge(error);
                                                break nickcheck;
                                            } else {
                                                checkList.add(currentStr);
                                                current++;

                                            }

                                        }


                                        if (repeatWord) {
                                            isMatch = false;

                                        } else {

                                            isMatch = true;
                                        }


                                    } else {
                                        isMatch = true;
                                    }

                                }


//                                matches = item.getNickname().matches(groupnickanmekeyword);


                            } catch (PatternSyntaxException e) {

                                String error = "????????????????????????????????????????????? " + groupnickanmekeyword + ",??????:" + item.getNickname();

                                LogUtil.writeLoge(error);
                                return getFailUri(error);
                            }
                            if (isMatch) {
                                LogUtil.writeLog(String.format("???%s ????????????", item.getFrienduin()));

                            } else {
                                MsgItem clone = item.clone();
//                                MsgReCallUtil.notifyGadPersonMsgNoJump(this, item.getFrienduin(), item.getSenderuin(), whiteNameBean.getGroupnickanmegagtime() * 60, item);
                                CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.NICKNAME_FORMAT, "????????????????????????: " + whiteNameBean.getGroupnicknamegagtip());


                                if (!TextUtils.isEmpty(whiteNameBean.getGroupnicknamegagtip())) {


                                    String message = VarCastUtil.parseStr(clone, _dbUtils, whiteNameBean.getGroupnicknamegagtip(), whiteNameBean.getGroupnickanmegagtime() + "");
                                    clone.setMessage(message);

                                } else {
                                    clone.setMessage(String.format("%s ?????????(%s)?????????,??????%d??????", item.getSenderuin() + "(" + item.getNickname() + ")", item.getNickname(), whiteNameBean.getGroupnickanmegagtime()));

                                }

                                if (RemoteService.isIsInit()) {
                                    String s = RemoteService.gagUser(clone.getFrienduin(), clone.getSenderuin(), whiteNameBean.getGroupnickanmegagtime() * 60);
                                    if (s == null) {
                                        MsgItem gagitem = item.clone();
                                        MsgReCallUtil.notifyGadPersonMsgNoJump(this, whiteNameBean.getGroupnickanmegagtime() * 60, gagitem);
                                    } else {
                                        clone.setMessage(clone.getMessage() + "\n????????????:" + s);

                                    }
                                } else {
                                    MsgItem gagitem = item.clone();
                                    MsgReCallUtil.notifyGadPersonMsgNoJump(this, whiteNameBean.getGroupnickanmegagtime() * 60, gagitem);

                                }


                                if (whiteNameBean.isAutornamecard() && !TextUtils.isEmpty(whiteNameBean.getNameCardvarTemplete())) {//??????????????????
                                    String[] keys = new String[]{"$city", "$qnickname", "$province", "$phone"};

                                    String[] values = null;


                                    if (RemoteService.isIsInit()) {
                                        String nickname = RemoteService.queryNickname(item.getSenderuin(), item.getSenderuin(), 0);
                                        if (!TextUtils.isEmpty(nickname) && !item.getSenderuin().equals(nickname)) {
                                            item.setNickname(nickname);
                                            clone.setNickname(nickname);
                                        }

                                        //                                        queryQQCard
                                        Map<String, String> map = RemoteService.queryQQCard(item.getSenderuin());

                                        if (map != null) {
//                                            String nickname = RemoteService.queryNickname(item.getSenderuin(), item.getSenderuin(), 0);


                                            values = new String[4];
                                            values[0] = StringUtils.selectStr(map.get("city"), "?????????");
                                            values[1] = nickname;//province
                                            values[2] = StringUtils.selectStr(map.get("province"), "?????????");//province
                                            values[3] = map.get("city");

                                        }

//                                        String nickname = RemoteService.queryNickname(item.getSenderuin(), item.getSelfuin(), 0);
                                    } else {

//

                                    }

                                    if (values == null) {

                                        values = new String[]{"???", item.getNickname(), "???", "????????????"};
                                    }

                                    String text = VarCastUtil.parseStr(item, _dbUtils, whiteNameBean.getNameCardvarTemplete(), keys, values);
                                    clone.setMessage(clone.getMessage() + "\n??????????????????:" + text);
                                    MsgReCallUtil.notifyRequestModifyName(this, item.clone(), text);
                                    //nameCardvarTemplete
                                }

                                if (whiteNameBean.isBannedaite()) {//????????????
                                    //??????????????????????????????
                                    MsgReCallUtil.notifyAtMsgJump(this, clone.getSenderuin(), clone.getNickname(), clone.getMessage(), clone);

                                } else {
                                    MsgReCallUtil.notifyHasDoWhileReply(this, clone.getMessage(), whiteNameBean.getPostfix(), clone);

                                }


//                                String.format("%s ?????????%s?????????,??????%d?????????????????????:%s", item.getSenderuin(), item.getNickname(), whiteNameBean.getGroupnickanmegagtime(), whiteNameBean.getGroupnicknamegagtip())
//                                String msg=String.format("%s ?????????%s?????????,??????%d?????????????????????:%s", item.getSenderuin(), item.getNickname(), whiteNameBean.getGroupnickanmegagtime(), whiteNameBean.getGroupnicknamegagtip());

                                if (whiteNameBean.isBreaklogic()) {

                                    return getFailUri("??????????????????????????? ");
                                }
                            }


                        } else {


                            LogUtil.writeLog(String.format("???%s?????????????????????????????????", item.getFrienduin()));
                        }


                        if (whiteNameBean.isBannedword()) {

                            PairFix<GagAccountBean, String> pair = keyMapContainGag(item.getMessage(), false);
                            if (pair != null) {

                                LogUtil.writeLog("??????????????????????????? " + pair.first.getAccount() + "," + item.getFrienduin());


                                CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.SEND_TEXT, "???????????????: " + item.getMessage());
                                pair.first = pair.first.clone();
                                if (!pair.first.isKick()) {

                                    if (whiteNameBean != null && whiteNameBean.isKickviolations()) {
//                                        pair.first = pair.first.clone();
                                        pair.first.setAction(whiteNameBean.isKickviolationsforver() ? GAGTYPE.KICK_FORVER : GAGTYPE.KICK);
                                    }
                                }
                                if (whiteNameBean.isRevokemsg()) {
                                    if (RemoteService.isIsInit()) {
                                        String s = RemoteService.revokeMsg(item.getFrienduin(), item.getFrienduin(), "", item.getMessageID());
                                        if (s != null) {

                                            pair.first.setTarget("\n????????????:" + s);
                                        } else {
                                            MsgReCallUtil.notifyRevokeMsgJump(this, item.getFrienduin(), item.getSenderuin(), item.getMessageID(), item);
                                        }
                                    } else {
                                        MsgReCallUtil.notifyRevokeMsgJump(this, item.getFrienduin(), item.getSenderuin(), item.getMessageID(), item);
                                    }
                                }
                                doGagImpLogic(item, pair, item.getMessage());//

                                if (whiteNameBean.isBreaklogic()) {

                                    return getFailUri("?????????????????? " + pair.first.getAccount());
                                }
                            } else {
                                LogUtil.writeLog("?????????????????????????????? " + item.getMessage() + "," + item.getFrienduin());
                            }


                        } else {
                            LogUtil.writeLog(String.format("???%s?????????????????????", item.getFrienduin()));
                        }


                    }


                    break checkModeTag;


                }//??????where???????????????

//                    FloorUtils.onReceiveNewMsg(_dbUtils, item);//?????????


//                boolean result = doCommendLogic(item, isManager, atPair, INeedReplayLevel.ANY);
                boolean result = doCommendLogic(item, isManager, isSelf, atPair, msgBean.getPair().second, true, whiteNameBean);


                if (result) {
                    LogUtil.writeLog("????????????????????????" + item.getMessage());

                    if (foolMode) {
                        whiteNameBean.setNeedaite(false);
                        MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "????????? ??????????????????"));
                    }

                    return getSuccUri("??????????????????????????????");
                }

                if (whiteNameBean.isNeedaite()) {
                    if (atPair == null || atPair.second.first == false) {
                        return getFailUri(String.format("???????????? %s??????????????????????????????????????????????????????", item.getFrienduin()));
                    } else {
                        LogUtil.writeLog("??????????????????????????????,???????????????????????????");
                    }
                }

//????????????????????????????????????.

                if (atPair.first) {
                    if (atPair.second.first == false) {

                        return getFailUri("?????????????????????");
                    }
                    LogUtil.writeLog("??????" + item.getMessage());
                } else {
                    if (whiteNameBean.isNeedaite()) {
                        if (foolMode) {
                            whiteNameBean.setNeedaite(false);
                            MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "?????????????????????????????????,????????????????????????" + AppConstants.ACTION_TEMP_FORVER));


                        }


                        return getFailUri("????????????/???????????????????????????????????????,??????????????????");
                    }
                }


                if (whiteNameBean.isLocalword()) {
                    Pair<Boolean, Uri> booleanUriPairHasIntercept = doLocalWordSucc(item, atPair, whiteNameBean, true);
                    if (booleanUriPairHasIntercept.first) {
                        LogUtil.writeLog("????????????????????????" + item.getMessage());

                        if (foolMode) {
                            MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "???????????????????????? " + booleanUriPairHasIntercept.second));


                        }

                        return booleanUriPairHasIntercept.second;

                    } else {
                        LogUtil.writeLog("?????????????????????????????????" + item.getMessage());
                    }
                } else {


                    LogUtil.writeLog("?????????????????????????????? " + item.getMessage() + "," + item.getFrienduin());
                }

                if (whiteNameBean.isNetword()) {
                    {


                        if (foolMode) {

                            whiteNameBean.setNetword(true);
                            MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "??????????????????????????????,?????????????????????,?????????????????????key "));

                        }
                        LogUtil.writeLog("????????????????????????" + item);
                        RequestBean requestBean = new RequestBean();
                        requestBean.setKey(robotReplyKey);
                        requestBean.setUserid(item.getSenderuin());
                        String message = item.getMessage();
                        if (TextUtils.isEmpty(message)) {
                            MsgReCallUtil.smartReplyMsg("What's your problem?", true, whiteNameBean, item);
                            return getSuccUri("empty msg");

                        }
                        requestBean.setInfo(message);
                        queryNetReplyResult(item, requestBean, whiteNameBean, (bean -> {

                            item.setMessage(bean.getDetailMsg() + whiteNameBean.getPostfix());

                            if (whiteNameBean.isFitercommand()) {

                                Pair<String, String> param = CmdConfig.fitParam(bean.getDetailMsg());
                                if (param != null) {
                                    LogUtil.writeLog("???????????????????????????????????????,????????????");

                                }


                            } else {
                                LogUtil.writeLog("???????????????????????????????????????????????????");
                            }

                            if (whiteNameBean.isReplayatperson()) {//????????????????????????????????????????????????

                                MsgReCallUtil.notifyAtMsgJump(RobotContentProvider.this, item.getSenderuin(), item.getNickname(), item.getMessage(), item);//???????????????

                            } else {

                                MsgReCallUtil.notifyJoinMsgNoJumpDisableAt(RobotContentProvider.this, item);//???????????????

                            }

                            return true;


                        }));


                    }


                } else {

                    if (foolMode) {

                        whiteNameBean.setNetword(true);
                        MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "??????????????????????????????????????? " + AppConstants.ACTION_TEMP_FORVER));

                    }
                    LogUtil.writeLog("??????????????????????????????,??????????????? " + item.getMessage() + "," + item.getFrienduin());
                }


                return getSuccUri();


            }


        } else {

            if ((isManager && (atPair != null && atPair.second.first)) || foolMode) {


                boolean pass = false;
                switch (item.getMessage().trim()) {
                    case CmdConfig.ADD_WHITE_NAMES:
                    case CmdConfig.ADD_WHITE_NAMES_1:
                    case CmdConfig.ADD_WHITE_NAMES_2:
                    case CmdConfig.ADD_WHITE_NAMES_3:
                        pass = true;
                    default:
                        if (foolMode || pass) {

                            GroupWhiteNameBean bean = (GroupWhiteNameBean) new GroupWhiteNameBean().setAccount(item.getFrienduin());
                            long insert = DBHelper.getQQGroupWhiteNameDBUtil(_dbUtils).insert(bean);
                            mQQGroupWhiteNames.add(bean);
                            String msg = "???" + item.getFrienduin() + "?????????????????????,?????????????????????=" + (insert > 0);

                            if (foolMode) {


                                MsgReCallUtil.notifyHasDoWhileReply(this, item.clone().setMessage(AppConstants.ACTION_OPERA_ALL_RESPONSE_NAME + "?????????" + item.getFrienduin() + "???????????????????????????"));
                            } else {

                                MsgReCallUtil.notifyJoinMsgNoJump(this, "" + msg, item);

                            }


                            return msgBean.getPair().first;
                        }

                }

            } else {
                if (foolMode) {


                }

            }


        }

        return msgBean.getPair().first;
    }

    private Pair<Boolean, Uri> doLocalWordSucc(MsgItem item, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, GroupWhiteNameBean nameBean, boolean isgroupMsg) {

        if (isgroupMsg) {
            if (nameBean != null && nameBean.isNeedaite()) {
                if (atPair.first == false) {
                    LogUtil.writeLog("????????????aite,but ????????????,??????????????? ????????????" + item.getSenderuin() + "?????????" + item.getMessage());
                    return Pair.create(true, getFailUri("????????????"));
                } else {
                }
            }

        }


        if (item.getMessage().contains("?????????") && item.getMessage().length() <= 5) {
            MsgReCallUtil.notifyJoinMsgNoJump(this, "??????????????????,??????????????????????????????????????????????????????,??????????????????,????????????????????????????????????!", item);
            return Pair.create(false, getSuccUri("??????"));
        }


        if ((nameBean == null) || (nameBean != null && nameBean.isLocalword())) {

            String keyReplyWord = keyMapContainLocalReply(item.getMessage());//??????????????????

            if (keyReplyWord != null) {
                keyReplyWord = VarCastUtil.parseStr(item, _dbUtils, keyReplyWord);
                if (nameBean != null) {

                    item.setMessage(keyReplyWord + nameBean.getPostfix());
                } else {
                    item.setMessage(keyReplyWord);

                }

                if (nameBean == null) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, keyReplyWord, item);

                } else {

                    if (nameBean.isReplayatperson()) {//????????????????????????????????????????????????
                        MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), item.getNickname(), item.getMessage(), item);//???????????????

                    } else {

                        MsgReCallUtil.notifyJoinMsgNoJumpDisableAt(this, item);//???????????????

                    }
                }

            }
            boolean needLocalReply = keyReplyWord != null ? true : false;

            return Pair.create(needLocalReply, needLocalReply ? getSuccUri("????????????????????????") : getFailUri("????????????????????????"));
        } else {
            return Pair.create(false, getFailUri("?????????????????????"));
        }


    }

    /**
     * ???true ???????????????????????????????????????null????????????????????????????????????????????????????????????
     *
     * @param item
     * @param isManager
     * @return ???????????? ????????????
     */
    private DoWhileMsg isNeedDoWhileMsg(final MsgItem item, boolean isgroupMsg, boolean isManager) {
        RedpacketBaseInfo redpacketBaseInfo = null;
        if (MsgTyeUtils.isRedPacketMsg(item)) {
            LogUtil.writeLog("???????????????,????????????????????????");
            redpacketBaseInfo = doRedPacketMsgLogic(item);
//            return new DoWhileMsg().setPairX(Pair.create(INeedReplayLevel.REDPACKET, getSuccUri("??????????????????")));
        }

        if (isgroupMsg)//?????????
        {

            DoWhileMsg pair = CheckUtils.checkGroupMsg(this, _dbUtils, item, isgroupMsg);
            if (pair != null) {
                pair.setObject(redpacketBaseInfo);
            }

            return pair;
         /*   if (pair.first != null) {
               LogUtil.writeLog("????????????????????? ????????????" + item.getSenderuin() + "?????????" + item.getMessage());
                return Pair.create(INeedReplayLevel.INTERCEPT_ALL, pair.first);
            } else {
                return Pair.create(INeedReplayLevel.ANY, null);
            }*/


        } else if (MsgTyeUtils.isPrivateMsg(item)) {

            if (!mCfprivateReply) {
                if (!mCfprivateReplyManagrIgnoreRule) {

                    return new DoWhileMsg(Pair.create(getFailUri("?????????????????????"), INeedReplayLevel.PRIVATE_NOT_REPLY));
                } else {
                    if (!isManager(item)) {

                        return new DoWhileMsg(Pair.create(getFailUri("?????????????????????(?????????????????????)"), INeedReplayLevel.PRIVATE_NOT_REPLY));
                    }
                }
                LogUtil.writeLog("????????????????????? ????????????" + item.getSenderuin() + "?????????" + item.getMessage());
            }

            if (MemoryIGnoreConfig.isTempIgnorePerson(item.getFrienduin())) {
                return new DoWhileMsg(Pair.create(getFailUri("??????QQ?????????????????????" + item.getFrienduin()), INeedReplayLevel.INTERCEPT_ALL));
            }

            if (AccountUtil.isContainAccount(mIgnoreQQs, item.getFrienduin(), true)) {
//                { mIgnoreQQs.contains(item.getFrienduin())) {
                return new DoWhileMsg(Pair.create(getFailUri("??????QQ????????????????????????" + item.getFrienduin()), INeedReplayLevel.INTERCEPT_ALL));
            }

            Uri uri = null;
            return new DoWhileMsg().setPairX(Pair.create(INeedReplayLevel.ANY, uri));


        } else if (isgroupMsg) {
            FloorUtils.onReceiveNewMsg(_dbUtils, item);

            if (!isManager(item)) {//????????????
                FrequentMessageDetectionUtils.doCheckFrequentMessage(item, null);
            }
            return new DoWhileMsg().setPairX(Pair.create(INeedReplayLevel.PICTURE, getSuccUri("????????????")));

            //??????????????????
        } else if (MsgTyeUtils.isRedPacketMsg(item)) {
            LogUtil.writeLog("??????????????????");
            doRedPacketMsgLogic(item);
            return new DoWhileMsg().setPairX(Pair.create(INeedReplayLevel.REDPACKET, getSuccUri("??????????????????")));

        } else if (MsgTyeUtils.isJoinGroupMsg(item)) {

            LogUtil.writeLog("??????????????????");

            String message = item.getMessage();
            String messageStr = (message + "").replaceAll(" ", "");
            int i = messageStr.indexOf(",153016267,");
            if (messageStr.startsWith("???????????????????????????") && i >= 15) {//24
                MsgReCallUtil.notifyJoinMsgNoJump(this, item.setMessage("?????????????????????boss???????????????,????????????"));
            }
//????????? ??????????????????????????????TA????????????                    ##**##2,5,0,3,0,2369830331,icon,0,0,color,0,19,10,20,2369830331,icon,0,0,color,0

            GroupWhiteNameBean account = AccountUtil.findAccount(mQQGroupWhiteNames, item.getFrienduin(), true);
            if (!mCfOnlyReplyWhiteNameGroup || account != null) {
//???????????????????????????????????????????????????
                if (TextUtils.isEmpty(mCfGroupJoinGroupReplyStr)) {
                    LogUtil.writeLog("????????????????????????,???????????????????????????");
                    return new DoWhileMsg().setPairX(Pair.create(INeedReplayLevel.INTERCEPT_ALL, getSuccUri("??????????????????")));
                }
                String messageFilter = item.getMessage();
                //?????????????????? ???????????????                    ##**##1,5,0,6,0,2168531679,icon,0,0,color,0(???474240677)
                //????????? ???????????? ??????????????????????????????TA????????????                    ##**##2,5,4,8,0,153016267,icon,0,0,color,0,19,15,25,153016267,icon,0,0,color,0
                String nickname = null;
                nickname = StringUtils.getStrCenter(messageFilter, "???", "???????????????");
                if (nickname == null) {
                    nickname = StringUtils.getStrLeft(messageFilter, "???????????????");
                }
                if (nickname == null) {
                    nickname = StringUtils.getStrLeft(messageFilter, "???????????????");
                }
                item.setNickname(nickname);
                ;

                String patternString = ".*?\\,[0-9]+\\,([0-9]{5,11})\\,icon\\,[0-9]+\\,[0-9]+\\,color\\,[0-9]+.*?";// ignore_include
                Pattern pattern = Pattern.compile(patternString);
                Matcher matcher = pattern.matcher(messageFilter);
                String sendUin = "";
                if (matcher.find()) {
                    sendUin = matcher.group(1);// ???
                } else {
                    MsgReCallUtil.notifyJoinMsgNoJump(this, item.getMessage(), item);
                    return new DoWhileMsg().setPairX(Pair.create(INeedReplayLevel.INTERCEPT_ALL, getFailUri("????????????????????????????????????????????????????????????")));
                }
                if (TextUtils.isEmpty(nickname)) {
                    nickname = sendUin;
                }

                item.setSenderuin(sendUin);
                item.setMessage(mCfGroupJoinGroupReplyStr);
//                String nickname=NickNameUtils.queryMatchNickname(item.getFrienduin(),item.getSenderuin(),false);
                final String finalNickname = nickname;
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MsgReCallUtil.notifyAtMsgJump(RobotContentProvider.this, item.getSenderuin(), finalNickname + "", mCfGroupJoinGroupReplyStr, item);

                    }
                }, 2000);

            }


            return new DoWhileMsg().setPairX(Pair.create(INeedReplayLevel.INTERCEPT_ALL, getFailUri("????????????,??????")));
        } else {

//                 else if (MsgTyeUtils.isUnKnowType(item)) {
            LogUtil.writeLog("??????????????????????????????????????????????????????????????????????????????, type:" + item.getType() + ",istroop:" + item.getIstroop() + ",message:" + item.getMessage() + ",????????????" + item.getSenderuin() + "??????" + item.getNickname());
            return new DoWhileMsg().setPairX(Pair.create(INeedReplayLevel.INTERCEPT_ALL, getFailUri("??????????????????????????????????????????????????????????????????????????????")));

        }

    }

    public Pair<IPluginHolder, Boolean> doPluginLogic(IMsgModel item, List<IPluginHolder> pluginList, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, int callFlag) {

        if (!mCFBaseEnablePlugin) {
            LogUtil.writeLog("?????????????????????");
            return Pair.create(null, false);
        } else {
            if (pluginList != null) {
                List<IPluginHolder> tempModels = pluginList;
                for (IPluginHolder model : tempModels) {
                    boolean disable = model.isDisable();
                    if (!disable) {

    /*
    25.954 5586-5664/? E/Xposed: java.lang.NoSuchMethodError: com.tencent.common.app.QFixApplicationImpl#getRuntime()#bestmatch
        at androidx.support.v4.app.ed.findMethodBestMatch(ProguardQSSQ:440)
        at androidx.support.v4.app.ed.findMethodBestMatch(ProguardQSSQ:447)
        at androidx.support.v4.app.ed.callMethod(ProguardQSSQ:201) queryNickName
     */
                        try {

                            boolean isNeedIntercept;

                            if (callFlag == IPluginRequestCall.FLAG_RECEIVE_MSG_DO_END) {

                                if (mAllowPluginInterceptEndMsg) {

                                    if (model.hasNewControlApiMethod()) {

                                        isNeedIntercept = model.getPluginInterface().onReceiveRobotFinalCallMsgIsNeedIntercept(item, atPair == null ? null : (List<AtBeanModelI>) atPair.second, atPair == null ? false : atPair.first, atPair == null || atPair.second == null ? false : atPair.second.first);
                                        if (isNeedIntercept) {
                                            return Pair.create(model, isNeedIntercept);
                                        }

                                    }

                                }


                            } else if (callFlag == IPluginRequestCall.FLAG_RECEIVE_JOIN_MSG) {


                                if (model.hasNewControlApiMethod()) {

                                    isNeedIntercept = model.getPluginInterface().onReceiveOtherIntercept(item, IPluginRequestCall.FLAG_RECEIVE_JOIN_MSG);
                                    if (isNeedIntercept) {
                                        return Pair.create(model, isNeedIntercept);

                                    }
                                }


                            } else {


                                if (model.hasNewControlApiMethod()) {

                                    boolean hasAite = atPair != null && atPair.second != null && atPair.second.first;
                                    boolean hasAiteMe = hasAite && atPair.second.first;
                                    List list = atPair != null && atPair.second != null ? atPair.second.second : null;
                                    isNeedIntercept = model.getPluginInterface().onReceiveMsgIsNeedIntercept(item, list, hasAite, hasAiteMe);
                                } else {
                                    isNeedIntercept = model.getPluginInterface().onReceiveMsgIsNeedIntercept(item);
                                }


                                LogUtil.writeLog("????????????" + item.getMessage() + "?????????" + model.getPluginInterface().getPackageName() + "_" + model.getPluginInterface().getPluginName() + ",????????????:" + isNeedIntercept);
                                if (isNeedIntercept) {
                                    return Pair.create(model, true);
                                } else {
                                    LogUtil.writeLog(model.getPluginInterface().getPluginName() + "???????????????,??????");

                                }


                            }


                        } catch (Throwable e) {
                            mLastError = "????????????????????????[" + model.getPath() + "]msg:" + item.toString() + "," + e.toString() + "->" + Log.getStackTraceString(e);
                            Log.e(LogUtil.TAG, mLastError);

                        }
                    }

                }
            }
        }
        return Pair.create(null, false);
    }


    public Uri doGagImpLogic(IMsgModel item, PairFix<GagAccountBean, String> pair, String word) {
        return doGagImpLogic(item, pair, word, "");
    }

    private Uri doGagImpLogic(IMsgModel item, PairFix<GagAccountBean, String> pair, String word, String action) {
        String uniqueKey = RobotUtil.getUniqueKey(item);
        synchronized (recentQAndGroupReplatMaps) {
            recentQAndGroupReplatMaps.remove(uniqueKey);
        }

        GagAccountBean gagAccountBean = pair.first;

        LogUtil.writeLog("???????????????" + pair.second + ",????????????,????????????:" + DateUtils.getGagTime(pair.first.getDuration()) + ",?????????:" + gagAccountBean.getAction());
        IMsgModel gagItem = item.clone();
        if (gagAccountBean.getAction() == GAGTYPE.KICK || gagAccountBean.getAction() == GAGTYPE.KICK_FORVER) {


            if (!TextUtils.isEmpty(word) && TaskUtils.isRecentPasswordMsg(word)) {//??????????????????????????????.
             /*   TaskUtils.joinTask(word, gagItem, GAGTYPE.KICK);

                if (!gagAccountBean.isSilence()) {
                    notifyJoinReplaceMsgJump(Cns.formatNickname(gagItem.getSenderuin(), gagItem.getNickname()) + "???????????? ?????????" + pair.second + ",????????????" + "??????:" + (gagAccountBean.getAction() == GAGTYPE.KICK_FORVER) + " action," + gagAccountBean.getAction(), item);
                }*/

                MsgReCallUtil.notifyJoinReplaceMsgJump(this, "?????????????????????????????????????????????????????????" + NickNameUtils.formatNickname(_dbUtils, item) + "?????????,????????????", item);


            } else {


                String reanson = getGagReason(pair, gagAccountBean, word, action);

                MsgReCallUtil.notifyGadPersonMsgNoJump(this, 10 * 1000, item.clone());//?????????
                if (gagAccountBean.getDuration() == 0) {
                    MsgReCallUtil.notifyKickPersonMsgNoJump(this, gagItem, gagAccountBean.getAction() == GAGTYPE.KICK_FORVER);
                    if (!gagAccountBean.isSilence()) {
                        String nickname = NickNameUtils.formatNickname(_dbUtils, gagItem);
                        String msg = nickname + action + reanson + ",????????????" + "??????:" + (gagAccountBean.getAction() == GAGTYPE.KICK_FORVER) + ",?????????????????????:??????";
                        MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), nickname, msg, item);

                    }

                } else {

                    if (!gagAccountBean.isSilence()) {


                        String nickname = NickNameUtils.formatNickname(_dbUtils, gagItem);
                        String taskname = Math.abs(word.hashCode()) + "";
                        String msg = nickname + action + reanson + ",????????????" + "??????:" + (gagAccountBean.getAction() == GAGTYPE.KICK_FORVER) + ",?????????????????????:" + DateUtils.getGagTime(gagAccountBean.getDuration()) + ",???????????????\n????????????:" + taskname;
                        MsgReCallUtil.notifyAtMsgJump(this, gagItem.getSenderuin(), nickname, msg, item);

                        TaskUtils.insertRedpacketKickTask(RobotContentProvider.this, taskname, item, ParseUtils.parseSecondToMs(gagAccountBean.getDuration()));

                    }
                }
            }

            return getSuccUri(action + "???????????????,????????????" + pair.second + "??????:" + (gagAccountBean.getAction() == GAGTYPE.KICK_FORVER));
        } else {


            if (TextUtils.isEmpty(action)) {

                if (!TextUtils.isEmpty(word) && TaskUtils.isRecentPasswordMsg(word)) {//??????????????????????????????.

                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "?????????????????????????????????????????????????????????" + NickNameUtils.formatNickname(_dbUtils, item) + "?????????,????????????", item);


                    return getSuccUri("???????????????????????????" + pair.second);
                }

            }

            if (RemoteService.isIsInit()) {
                String s = RemoteService.gagUser(gagItem.getFrienduin(), gagItem.getSenderuin(), ParseUtils.parseGagStr2Secound(gagAccountBean.getDuration() + ""));
                if (s != null) {
                    pair.first.setTarget(pair.first.getTarget() + "\n????????????:" + s);
                } else {
                    MsgReCallUtil.notifyGadPersonMsgNoJump(this, ParseUtils.parseGagStr2Secound(gagAccountBean.getDuration() + ""), gagItem);
                }
            } else {
                MsgReCallUtil.notifyGadPersonMsgNoJump(this, ParseUtils.parseGagStr2Secound(gagAccountBean.getDuration() + ""), gagItem);

            }

            if (!gagAccountBean.isSilence() || !TextUtils.isEmpty(action)) {//????????????action???????????????????????????????????????????????????????????????????????????
                String nickname = NickNameUtils.formatNickname(_dbUtils, gagItem);

                String reanson = getGagReason(pair, gagAccountBean, word, action);

                //todo


                MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), nickname, nickname + action + reanson + " ????????????:" + DateUtils.getGagTime(gagAccountBean.getDuration()), item);


            }
            return getSuccUri("???????????????,????????????" + pair.second);
        }
    }

    //pair second?????????????????????first ???gagbean??????
    private String getGagReason(PairFix<GagAccountBean, String> pair, GagAccountBean gagAccountBean, String word, String action) {
        boolean notReason = TextUtils.isEmpty(gagAccountBean.getReason());
        if (!notReason) {
            return "????????????: " + gagAccountBean.getReason() + gagAccountBean.getTarget();
        }
        String string = "?????????????????????:" + pair.second + " ";
        if (word.length() <= 10 || BuildConfig.DEBUG_APP) {
            string += "??????????????????:" + word + "\n";
        }
        if (!TextUtils.isEmpty(action)) {
            string += "????????????:" + action + " " + gagAccountBean.getTarget();
        } else {

            string += "????????????:?????? " + gagAccountBean.getTarget();
        }
        return string;
    }

    private RedpacketBaseInfo doRedPacketMsgLogic(MsgItem item) {
        RedPacketBean bean = new RedPacketBean();
        bean.setIstroop(item.getIstroop());
        bean.setQq(item.getSenderuin());
        bean.setQqgroup(item.getFrienduin());
        String message = item.getMessage();
        if (AppUtils.isJSONObject(message)) {
            RedPacketBeanFromServer packetBeanFromServer = null;
            try {
                packetBeanFromServer = JSON.parseObject(item.getMessage(), RedPacketBeanFromServer.class);

            } catch (Exception e) {
                LogUtil.writeLog("?????????????????????,??????");
                return null;
            }
            bean.setMoney(packetBeanFromServer.getMoney());
            bean.setResult(packetBeanFromServer.getResult());
            bean.setMessage(packetBeanFromServer.getMsg());
            bean.setNickname(packetBeanFromServer.getNickname());
            bean.setGroupnickname(packetBeanFromServer.getGroupnickname());


            boolean groupMsg = item.getIstroop() == 1;
            boolean manager = isManager(item);
            String title = packetBeanFromServer.getTitle();


            if (groupMsg && !manager) {

                LogUtil.writeLog("???????????????????????????" + title + ",???" + item.getNickname() + "??????");
//                keyMapContainGagFromRedpacket(item, title, packetBeanFromServer.getType());


            } else {
                LogUtil.writeLog("??????????????????????????????????????????????????????:" + groupMsg + ",???????????????:" + manager + ",title:" + title);
            }
            if (message != null) {

                if (message.contains("?????????")) {
                    bean.setResult(RedPacketBean.RESULT_NOT_DRAW_SUCC);

                } else if (message.contains("?????????")) {
                    bean.setResult(RedPacketBean.NOT_ENABLE_REDPACKET);
                } else {
                    bean.setResult(RedPacketBean.DRAW_SUCC);

                }
            }
            bean.setMessage(message);
            DBHelper.getRedPacketBUtil(_dbUtils).insert(bean);


            RedpacketBaseInfo redpacketBaseInfo = new RedpacketBaseInfo();
            redpacketBaseInfo.setTitle(title);
            redpacketBaseInfo.setType(packetBeanFromServer.getType());

            return redpacketBaseInfo;


        } else {
            boolean groupMsg = item.getIstroop() == 1;
            boolean manager = isManager(item);
            String title = item.getMessage();
            int redpacketType = -1;
            if (groupMsg && !manager) {

                if (title.contains("??????")) {
                    redpacketType = RedPacketMessageType.PASSWORD;
                } else if (title.contains("??????")) {
                    redpacketType = RedPacketMessageType.EXCLUSIVE;
                } else if (title.contains("??????")) {
                    redpacketType = RedPacketMessageType.NORMAL;
                } else if (title.contains("??????")) {
                    redpacketType = RedPacketMessageType.VOICE_PASSWORD;
                } else {//????????????
                    redpacketType = RedPacketMessageType.LUCK;
                }
//                keyMapContainGagFromRedpacket(item, title, redpacketType);//??????????????????

            } else {
                LogUtil.writeLog("??????????????????????????????????????????????????????:" + groupMsg + ",???????????????:" + manager + ",title:" + title);
            }


            if (message != null) {

                if (message.contains("?????????")) {
                    bean.setResult(RedPacketBean.RESULT_NOT_DRAW_SUCC);

                } else if (message.contains("?????????")) {
                    bean.setResult(RedPacketBean.NOT_ENABLE_REDPACKET);
                } else {
                    bean.setResult(RedPacketBean.DRAW_SUCC);

                }
            }


            RedpacketBaseInfo redpacketBaseInfo = new RedpacketBaseInfo();
            redpacketBaseInfo.setTitle(title);
            redpacketBaseInfo.setType(redpacketType);

            return redpacketBaseInfo;

        }


    }


    /**
     * ??????long????????????
     */

    static ArrayMap<String, Long> recentQAndGroupReplatMaps = new ArrayMap<>();

    public Handler getHandler() {

        if (sHandler == null) {
            sHandler = new Handler(Looper.getMainLooper());
        }
        return sHandler;
    }

    public Runnable getTempRunnable() {
        if (tempRunnable == null) {
            tempRunnable = new Runnable() {
                @Override
                public void run() {
                    IGnoreConfig.tempNoDrawPerson = "";
                }
            };
        }

        return tempRunnable;
    }

    private Runnable tempRunnable;
    Handler sHandler;

    private String filterRepeatMsgNotNullResutnErrMsg(MsgItem item) {

        if (MsgTyeUtils.isRobotSelftMsg(item)) {
            return null;

        }
     /*   if (MsgTyeUtils.isGroupMsg(item) && isManager(item)) {
            return null;

        }*/
        String errMsg = null;
        if (item.getSenderuin().equals(IGnoreConfig.tempNoDrawPerson) && !isManager(item)) {
            errMsg = "??????qq:" + item.getSenderuin() + ",?????????????????????????????????!??????" + IGnoreConfig.MAX_SHUAPIN_TIME_SECOND_MS + "???????????????";
            LogUtil.writeLog(errMsg);
            return errMsg;
        }

        long nowTime = AppUtils.getNowTime();
        String uniqueKey = RobotUtil.getUniqueKey(item);
        Long s = null;
        synchronized (recentQAndGroupReplatMaps) {
            s = recentQAndGroupReplatMaps.get(uniqueKey);
            if (recentQAndGroupReplatMaps.size() > (1500 + (1000 * new Random().nextInt(5)))) {
                String key = recentQAndGroupReplatMaps.keyAt(0);
//            Long timeFirst = recentQAndGroupReplatMaps.get(key);
                recentQAndGroupReplatMaps.removeAt(0);
            }

        }

        if (s != null && s > 0) {
            long timeDistance = getTimeDistance(TYPE_SECOND, nowTime, s.longValue());

            if (IGnoreConfig.distancedulicateCacheHistory > 0 && timeDistance <= IGnoreConfig.distancedulicateCacheHistory) {// ?????????????????? ?????????????????????????????????QQ?????????????????????
                //EncyptUtil.HOOKLog("??????????????????" + message + ",??????,????????????????????????" + timeDistance);
                //?????????bug.

             /*   if (!isManager(item) && MsgTyeUtils.isGroupMsg(item)) {
                    DoWhileMsg uriBooleanPair = CheckUtils.checkGroupMsg(this, _dbUtils, item);
                    if (uriBooleanPair.getPair().second >= INeedReplayLevel.ANY) {//???????????????????????????????????????????????? ??????????????? ??????????????????

                        String s1 = FrequentMessageDetectionUtils.doCheckFrequentMessage(item, null);
                        CheckUtils.doIsNeedRefreshGag(this, s1, item);

                    }
                }*/

                errMsg = "??????????????????(??????????????????QQ,????????????????????????)?????????:" + timeDistance + ",????????????????????????:" + IGnoreConfig.distancedulicateCacheHistory + ",????????????:" + DateUtils.getTime(s.longValue()) + ",??????????????????" + recentQAndGroupReplatMaps.size() + "," + item.getMessage();


                if (MsgTyeUtils.isGroupMsg(item)) {

                    if (mCfOnlyReplyWhiteNameGroup) {
                        GroupWhiteNameBean whiteNameBean = AccountUtil.findAccount(mQQGroupWhiteNames, item.getFrienduin(), false);
                        if (whiteNameBean != null) {

                            String frequentMessage = FrequentMessageDetectionUtils.doCheckFrequentMessage(item, whiteNameBean);


                            Uri uri = CheckUtils.doIsNeedRefreshGag(this, frequentMessage, item, whiteNameBean);


                            if (uri != null) {

                                CheckUtils.doCheckGagCountThan(_dbUtils, this, whiteNameBean, item, CheckUtils.ViolationType.frequent_Violation, "????????????: ");

                                if (whiteNameBean.isRevokemsg()) {
                                    MsgReCallUtil.notifyRevokeMsgJump(this, item.getFrienduin(), item.getSenderuin(), item.getMessageID(), item);
                                }
                                LogUtil.writeLog(errMsg);

                                return uri + "-" + errMsg;
                            }
                        } else {

                        }
                    }
                }


                LogUtil.writeLog(errMsg);
                return errMsg;//????????????????????????????????????????????? ???????????????????????????????????????????????????
            } else {
                //EncyptUtil.HOOKLog("??????????????????" + message + ",?????????,???????????????????????? TIME:" + timeDistance);
            }
        }


//        if(IGnoreConfig.distanceNetHistoryTimeIgnore)

        long nettimeDistance = getTimeDistance(TYPE_SECOND, nowTime, item.getTime());

        if (IGnoreConfig.distanceNetHistoryTimeIgnore > 0 && nettimeDistance >= IGnoreConfig.distanceNetHistoryTimeIgnore) {//?????? ????????????????????????????????????
            errMsg = "??????????????????(??????????????????????????????????????????)?????????:" + nettimeDistance + ",??????????????????????????????" + IGnoreConfig.distanceNetHistoryTimeIgnore + "?????????,????????????:" + DateUtils.getTime(item.getTime());
            LogUtil.writeLog(errMsg);

            return errMsg;
        } else {

            LogUtil.writeLog("????????????????????? ???????????????:" + nettimeDistance + "??? ?????????" + IGnoreConfig.distanceNetHistoryTimeIgnore + "???");
        }


        long statupTimeDistance = getTimeDistance(TYPE_MS, item.getTime(), mStatupTime);//????????????????????????,??????????????????

        if (IGnoreConfig.distanceStatupTimeIgnore > 0) {
            if (statupTimeDistance > 0 && statupTimeDistance <= IGnoreConfig.distanceStatupTimeIgnore && !MsgTyeUtils.isRedPacketMsg(item)) {//????????????????????????????????????
                errMsg = "??????APP????????????(APP???????????????????????????????????????,????????????????????????)??????????????????:" + statupTimeDistance + "????????????????????????????????????:" + IGnoreConfig.distanceStatupTimeIgnore + ",APP????????????:" + DateUtils.getTime(mStatupTime) + ",????????????:" + DateUtils.getTime(nowTime) + ",timeStamp:" + nowTime + ",hashCode:" + this.getClass().hashCode();
                LogUtil.writeLog(errMsg);
                return errMsg;
            } else {
            }

        }
        synchronized (recentQAndGroupReplatMaps) {
            recentQAndGroupReplatMaps.put(uniqueKey, nowTime);
        }
//       RobotUtil.writeLog("??????????????????????????????????????????:" + statupTimeDistance);

        return null;
    }

    @Deprecated
    private void notifyGroupJoinWord(MsgItem item) {
//        String word = getRandomWordFromString(mCfGroupJoinGroupReplyStr);
//        word = word.replace("$name", item.getNickname());
//        notifyAtMsgJump(item.setMessage(item.getMessage() + "(???" + item.getFrienduin() + ")"));
    }

    private Uri getSuccUri() {
        return getSuccUri(null);
    }

    public Uri getSuccUri(String msg) {
        return Uri.parse(RobotUtil.genResultJSONStrign("??????" + (msg == null ? "" : msg), 0));
    }

    public static Uri getFailUri(String fail) {
        LogUtil.writeLog("??????FailUri " + fail);
        return Uri.parse(RobotUtil.genResultJSONStrign(fail, -1));
    }

    public String keyMapContainLocalReply(String word) {
        if (!word.contains(ClearUtil.wordSplit)) {
            String s = mKeyWordMap.get(word);
            if (s != null) {
                String result = getRandomWordFromString(s);
                return result;
            }


        }


        for (Map.Entry<String, String> entry : mKeyWordMap.entrySet()) {


            String key = entry.getKey();

           /* String regxKey = RobotUtil.isRegxKey(key);//???????????? ??????
            if (regxKey != null) {

                Pattern p = Pattern.compile(regxKey);
                Matcher m = p.matcher(word.toUpperCase());
                boolean b = m.find();
                if (b) {
                    return Pair.create(bean, keyWord);
                } else {
                    continue;

                }

            }*/


            if (word != null && word.contains(key)) {
                String result = getRandomWordFromString(entry.getValue());
//                LogUtil.writeLog(TAG, "???????????????????????????:" + entry.getKey() + "?????????:" + result);
                return result;
            } else {
//                LogUtil.writeLog(TAG, "?????????????????????:" + entry.getKey() + "?????????");

            }
        }
        return null;
    }

    /**
     * ?????????????????????
     *
     * @param
     * @param word
     * @param type
     * @return
     */
    public boolean keyMapContainGagFromRedpacket(MsgItem item, String word, int type) {


        if (type == RedPacketMessageType.PASSWORD) {


            PairFix<GagAccountBean, String> gagAccountBeanStringPair = keyMapContainGag(word, true);

            if (gagAccountBeanStringPair != null) {

                String nickname = NickNameUtils.formatNickname(_dbUtils, item);
                String msg = "??????" + nickname + "???????????????????????????????????????`" + gagAccountBeanStringPair.second + "`??????????????????,??????" + IGnoreConfig.REDPACKET_KICK_DELAY_TIME_MINUTE + "???????????????,?????????????????????,?????????????????????????????????????????????????????????" + CmdConfig.CLEAR_TASK + word + "??????????????????";
                LogUtil.writeLog(msg);
                MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), nickname, msg, item);
//                notifyJoinReplaceMsgJump(msg, item);
                MsgItem gagItem = item.clone();
                long gagDuration = ParseUtils.parseGagStr2Secound(IGnoreConfig.REDPACKET_GAG_DURATION);
                LogUtil.writeLog("????????????duration:" + msg);
                MsgReCallUtil.notifyGadPersonMsgNoJump(this, gagDuration, gagItem);
                TaskUtils.insertRedpacketKickTask(RobotContentProvider.this, word, item, ParseUtils.parseMinuteToMs(IGnoreConfig.REDPACKET_KICK_DELAY_TIME_MINUTE));
                return true;
            } else {
                return false;
            }

        } else if (!TextUtils.isEmpty(word)) {//??????????????????????????????????????????
            PairFix<GagAccountBean, String> gagAccountBeanStringPair = keyMapContainGag(word, true);
            if (gagAccountBeanStringPair != null) {
                Uri uri = doGagImpLogic(item, gagAccountBeanStringPair, word, "???????????????????????????");
                return true;
            }
        }
        return false;


    }

    public PairFix<GagAccountBean, String> keyMapContainGag(String word, boolean fromRedpacket) {
        String fixWord = word.replaceAll(" ", "");
        for (GagAccountBean bean : mGagKeyWords) {

            PairFix<GagAccountBean, String> keyWord = getGagAccountBeanStringPairFixByGagAccountBean(fixWord, bean);
            if (keyWord != null) return keyWord;
        }
        return null;
    }

    @org.jetbrains.annotations.Nullable
    public PairFix<GagAccountBean, String> getGagAccountBeanStringPairFixByGagAccountBean(String fixWord, GagAccountBean bean) {
        if (bean.isDisable()) {
            return null;
        }


        String account = bean.getAccount();
        String fullRegFix = RobotUtil.isRegxFullKey(account);//????????????
        boolean isFullRegx = fullRegFix != null;//??????????????????
        String globalRegStr = RobotUtil.isGlobalReg(account);//?????????,?????????????????????
        boolean isGlobalReg = isFullRegx == false && globalRegStr != null;
        boolean isRegKey = false;//?????????????????????
        isRegKey = false;
        if (isFullRegx) {
            account = fullRegFix;
        } else if (isGlobalReg) {
            account = globalRegStr;
            isRegKey = true;
        } else {
            String regxKey = RobotUtil.isRegxKey(account);
            if (regxKey != null) {
                account = regxKey;
                isRegKey = true;
            }
        }
        String[] split = isGlobalReg ? new String[]{account} : account.split(ClearUtil.wordSplit);
        for (String keyWord : split) {
            if (isRegKey) {

                try {


                    Pattern p = Pattern.compile(keyWord);
                    Matcher m = p.matcher(fixWord);
                    boolean b = m.find();
                    if (b) {
                        return PairFix.create(bean, keyWord);
                    } else {
                        continue;

                    }

                } catch (Exception e) {
                    Log.e(LogUtil.TAG, "????????????????????????????????????????????????????????????" + fixWord + ",??????????????????" + keyWord + "????????????:" + e.getMessage());
                    continue;

                }

            }

            if (keyWord.length() == 1) {
                if (fixWord.contains(keyWord)) {

                    if (fixWord.length() == 1) {
                        return PairFix.create(bean, keyWord);
                    } else {

                        if (fixWord.startsWith(keyWord) && fixWord.endsWith(keyWord)) {
                            return PairFix.create(bean, keyWord);//?????????????????? ??? ???
                        }
                    }
                }
            } else if (keyWord.contains(Cns.DEFAULT_GAG_SHUAPIN)) {//????????????????????????????????????????????????
                if (fixWord.contains("\n\n\n\n\n\n\n")) {
                    int strSignCount = StringUtils.getStrSignCount(fixWord, "\n");
                    if (strSignCount >= 5) {
                        if (strSignCount <= 30) {
                            bean.setDuration(strSignCount * 60);
                        } else if (strSignCount <= 100) {
                            bean.setDuration(strSignCount * 120);
                        } else if (strSignCount <= 100) {
                            bean.setDuration(strSignCount * 240);
                        } else {
                            bean.setDuration(ParseUtils.parseGagStr2Secound("30???"));
                        }

                    }
                    return PairFix.create(bean, "????????????");
                } else {
                    continue;
                }

            } else if (RegexUtils.checkIsContainEnglish(keyWord)) {
                if (fixWord.contains(keyWord)) {
                    return PairFix.create(bean, keyWord);

                } else {
                    continue;
                }

            }
            /*else if (fixWord.toUpperCase().contains(keyWord.toUpperCase())) {
                return PairFix.create(bean, keyWord);
            }*/
            else {

                if (fixWord.contains(keyWord)) {
//                    if (fixWord.toUpperCase().contains(keyWord)) {


                    return PairFix.create(bean, keyWord);
                } else {

                    if (isFullRegx) {
                        //???(.*?)???
                        StringBuffer sbFixReg = new StringBuffer();
                        char[] chars = keyWord.toUpperCase().toCharArray();
                        for (int i = 0; i < chars.length; i++) {
                            sbFixReg.append(chars[i]);
                            if (i != chars.length - 1) {
                                sbFixReg.append("(.*?)");

                            }
                        }
                        if (chars.length >= 2) {
                            sbFixReg.insert(0, "(.*?)");
                            sbFixReg.append("(.*?)");
                        }

                        String regex = sbFixReg.toString();

                        try {
                            Pattern p = Pattern.compile(regex);
                            Matcher m = p.matcher(fixWord.toUpperCase());
                            boolean b = m.find();
//                   RobotUtil.writeLog("????????????:" + regex + ",??????" + fixWord + ",??????:" + b);
                            if (b) {
                                return PairFix.create(bean, keyWord);
                            }

                        } catch (Exception e) {
                            Log.e(LogUtil.TAG, "??????????????????????????????????????????????????????" + fixWord + ",???????????????:" + regex + "????????????:" + e.getMessage());
                            continue;
                        }

                    }
                }


            }

            /*
            String reg="((?<=(???))[^?????????]+)";
Pattern pattern =Pattern.compile(reg);
Matcher m=pattern.matcher("?????????????????????????????????????????????");
while(m.find()){
System.out.println(m.group());//??????????????????????????????
}
m=pattern.matcher("???????????????????????????????????????");
while(m.find()){
System.out.println(m.group());//??????????????????????????????
}
             */
        }
        return null;
    }


    public String getRandomWordFromString(String str) {
        String[] split = str.split(ClearUtil.wordSplit);
        int index = (int) (Math.random() * split.length);//????????????1 ???????????????
        String temp = split[index];
        return temp;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = _uriMatcher.match(uri);
        switch (match) {
            case CODE_UPDATE_KEY:
                String key = values.getAsString(Cns.UPDATE_KEY);
                String secret = values.getAsString(Cns.UPDATE_SECRET);
                robotReplyKey = key == null || key == "" ? Cns.DEFAULT_TULING_KEY : key;
                robotReplySecret = secret;
                SharedPreferences.Editor edit = sharedPreferences.edit();
                defaultReplyIndex = sharedPreferences.getInt(Cns.SP_DEFAULT_REPLY_API_INDEX, 0);
                Toast.makeText(getProxyContext(), "update key succ key=" + key + ",index:" + defaultReplyIndex, Toast.LENGTH_SHORT).show();

                break;
        }
        return -1;
    }

    @Override
    public String getPluginInfo() {
        return "????????????:" + BuildConfig.BUILD_TIME_STR + "_????????????:" + BuildConfig.VERSION_NAME + "_????????????:" + BuildConfig.BUILD_TYPE + "?????????????????????:" + mCfprivateReply;
    }

    @Override
    public View showOperaUi(ViewGroup viewGroup) {
      /*  XmlResourceParser layout = getResources().getLayout(R.layout.as_plugin_plugin_list);
//        LayoutInflater from = LayoutInflater.from(getProxyContext());

//        ClassloaderContext classloaderContext=new ClassloaderContext(getProxyContext(),getRobotContext(),this.getClass().getClassLoader());
        ClassloadContextThemeWrapper classloaderContext = new ClassloadContextThemeWrapper(getRobotContext(), this.getClass().getClassLoader(), 0);
        LayoutInflater from = LayoutInflater.from(classloaderContext);
//        LayoutInflater from = LayoutInflater.from(getRobotContext());
        return from.inflate(layout, viewGroup, false);

        */
        return null;
    }


    @Override
    public String toString() {
        return "" + getPluginInfo();
    }

    public void queryNetReplyResult(final MsgItem msgItem, RequestBean bean, GroupWhiteNameBean whiteNameBean) {
        queryNetReplyResult(msgItem, bean, whiteNameBean, null);
    }

    public void queryNetReplyResult(final MsgItem msgItem, final RequestBean bean, GroupWhiteNameBean whiteNameBean, final IIntercept<ResultBean> intercept) {

        if (msgItem.getMessage().contains("??????") || msgItem.getMessage().contains("??????") || msgItem.getMessage().contains("??????") || msgItem.getMessage().contains("??????")) {
            String tep = "???????????????,??????????????????,????????????,??????????????????,??????????????????????";
            MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.this, tep, msgItem);

        }
        if (defaultReplyIndex == 0) {

            if (whiteNameBean != null && whiteNameBean.isEnglishdialogue() && RegexUtils.isEnglishWord(msgItem.getMessage())) {
                Observable.create(new ObservableOnSubscribe<MsgItem>() {
                    @Override
                    public void subscribe(ObservableEmitter<MsgItem> emitter) throws Exception {
                        emitter.onNext(msgItem);
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(RXUtil.mapEnglish2ChineseTranslateFunctionWord()).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String str) throws Exception {
                        msgItem.setMessage(str);
                        bean.setInfo(str);
                        doMoLi(RobotContentProvider.this, msgItem, bean, whiteNameBean, intercept);//??????????????????????????????????????????NULL
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        doMoLi(RobotContentProvider.this, msgItem, bean, whiteNameBean, intercept);//??????????????????????????????????????????NULL

                    }
                }, new Action() {

                    @Override
                    public void run() throws Exception {

                    }
                });
            } else {
                doMoLi(this, msgItem, bean, whiteNameBean, intercept);//??????????????????????????????????????????NULL
            }

        } else if (defaultReplyIndex == 1) {

            if (whiteNameBean != null && whiteNameBean.isEnglishdialogue() && RegexUtils.isEnglishWord(msgItem.getMessage())) {
                Observable.create(new ObservableOnSubscribe<MsgItem>() {
                    @Override
                    public void subscribe(ObservableEmitter<MsgItem> emitter) throws Exception {
                        emitter.onNext(msgItem);
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).map(RXUtil.mapEnglish2ChineseTranslateFunctionWord()).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String str) throws Exception {
                        msgItem.setMessage(str);
                        bean.setInfo(str);
                        doTuLing(RobotContentProvider.this, msgItem, bean, whiteNameBean, intercept);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        doTuLing(RobotContentProvider.this, msgItem, bean, whiteNameBean, intercept);
                    }
                }, new Action() {

                    @Override
                    public void run() throws Exception {

                    }
                });
            } else {
                doTuLing(RobotContentProvider.this, msgItem, bean, whiteNameBean, intercept);
            }


        }


    }


    public static void doMoLi(final RobotContentProvider robotContentProvider, final MsgItem msgItem, RequestBean bean, GroupWhiteNameBean whiteNameBean, final IIntercept<ResultBean> intercept) {


        Observable.create(new ObservableOnSubscribe<ResultBean>() {
            @Override
            public void subscribe(ObservableEmitter<ResultBean> emitter) throws Exception {
                Retrofit retrofit = HttpUtilRetrofit.buildRetrofit(Cns.ROBOT_REPLY_MOLI_DOMAIN);
                MoLiAPI projectAPI = retrofit.create(MoLiAPI.class);
                HashMap<String, String> forms = new HashMap<>();
                String ask = bean.getInfo();
                if (ask.contains("???????????????")) {
                    bean.setInfo("???????????????");
                } else if (ask.contains("????????????")) {
                    bean.setInfo("????????????");
                } else if (ask.contains("????????????")) {
                    bean.setInfo("????????????");
                } else if (ask.contains("??????")) {
                    bean.setInfo("??????");
                }
                forms.put("question", bean.getInfo().trim());
                forms.put("limit", (new Random().nextInt(3)) + "");
                forms.put("api_key", robotContentProvider.robotReplyKey);
                forms.put("api_secret", robotContentProvider.robotReplySecret);
                Call<String> call1 = projectAPI.query(forms);
                Response<String> response = call1.execute();
                String str = response.body();
                ResultBean resultBean = new ResultBean();
                if (str.startsWith("{")) {
                    str = str.replace("&nbsp;", " ");
                    str = str.replace("&amp;", "&");
                    resultBean.setNeedTranslate(false);
                    if (bean.getInfo().contains("??????")) {
                        JSONObject jsonObject = new JSONObject(str);
                        StringBuffer sb = new StringBuffer();
                        sb.append("??????:" + jsonObject.optString("title"));
                        sb.append("\n??????:" + jsonObject.optString("content"));
                        resultBean.setText(sb.toString());
                    } else if (bean.getInfo().contains("????????????")) {
                        JSONObject jsonObject = new JSONObject(str);
                        StringBuffer sb = new StringBuffer();
                        sb.append("??????????????????" + jsonObject.optString("number2") + "???");
                        RobotFormatUtil.appendIfNotNull("??????", "haohua", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "qianyu", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("????????????", "shiyi", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("????????????", "jieqian", jsonObject, sb);
                        resultBean.setText(sb.toString());
                    } else if (bean.getInfo().contains("??????")) {
                        JSONObject jsonObject = new JSONObject(str);
                        StringBuffer sb = new StringBuffer();
                        sb.append("??????????????????" + jsonObject.optString("number2") + "???");
                        RobotFormatUtil.appendIfNotNull("??????", "haohua", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "shiyi", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "jieqian", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "zhushi", jsonObject, sb);
                        resultBean.setText(sb.toString());
                    } else if (bean.getInfo().contains("??????")) {//???????????????
                        JSONObject jsonObject = new JSONObject(str);
                        StringBuffer sb = new StringBuffer();
                        sb.append("??????????????????" + jsonObject.optString("number2") + "???");
//                        RobotFormatUtil.appendIfNotNull("??????", "haohua", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "qianyu", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "zhushi", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "jieqian", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "jieshuo", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "jieguo", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "hunyin", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "shiye", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "gongming", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "shiwu", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("????????????", "cwyj", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "yuntu", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "jiaoyi", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "qiucai", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "liujia", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "susong", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "jibin", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("???????????????", "moushi", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "hhzsy", jsonObject, sb);
                        RobotFormatUtil.appendIfNotNull("??????", "yuntu", jsonObject, sb);
                        resultBean.setText(sb.toString());
                    } else {
                        JSONObject jsonObject = new JSONObject(str);
                        JSONArray names = jsonObject.names();
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < names.length(); i++) {
                            String key = names.getString(i);
                            sb.append(key + ":" + jsonObject.getString(key));
                        }
                        resultBean.setText(sb.toString());

                    }
                } else {
                    resultBean.setText(str);
                }
                resultBean.setCode(TuLingType.NORMAL);
                emitter.onNext(resultBean);
            }
        }).subscribeOn(Schedulers.io())
                .map(new Function<ResultBean, ResultBean>() {
                    @Override
                    public ResultBean apply(ResultBean resultBean) throws Exception {
                        if ((resultBean.getText() + "").contains("QQ")) {
                            String tep = "???????????????,??????????????????,????????????,???????????????????";
                            resultBean.setText(tep);
                        } else if (RegexUtils.isContaineQQOrPhone(resultBean.getText())) {
                            resultBean.setText("?????????????????????[" + msgItem.getMessage() + "]?????????????????????????????????????????????,???QQ ????????????,????????????????????????????????????!");

                        } else if (resultBean.getText().contains("http")) {
                            resultBean.setText("?????????????????????????????????????????????..");
                        }
                        return resultBean;
                    }
                }).map(RXUtil.mapTranslateFunction(whiteNameBean))
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ResultBean>() {
            @Override
            public void accept(ResultBean resultBean) throws Exception {

                if (robotContentProvider.mAllowReponseSelfCommand) {
                    if (resultBean.getText().length() < 10) {
                        resultBean.setText("." + resultBean.getText());

                    }
                }

                if (resultBean.getText().equals("[cqname]")) {
                    String tep = "?????????????????????,??????????????????????????????????????????????????????->???????????????key??????";
                    resultBean.setText(tep);
                }


                resultBean.setText(resultBean.getText().replace("[cqname]", RobotContentProvider.getInstance().mLocalRobotName));
                resultBean.setText(resultBean.getText().replace("[name]", NickNameUtils.formatNickname(msgItem)));

                if (resultBean.getCode() == TuLingType.NORMAL || resultBean.getCode() == TuLingType.LINK) {
                    String message = resultBean.getDetailMsg();


                    if (intercept != null && intercept.isNeedIntercept(resultBean)) {

                        return;
                    }
                    if (MsgTypeConstant.MSG_ISTROP_GROUP_PRIVATE_MSG_1 == msgItem.getIstroop() || MsgTypeConstant.MSG_ISTROOP__GROUP_PRIVATE_MSG == msgItem.getIstroop() || 0 == msgItem.getIstroop()) {
                        if (!msgItem.getSenderuin().equals(msgItem.getSelfuin()) && !msgItem.getFrienduin().equals(msgItem.getSelfuin())) {
                            //
                            msgItem.setExtrajson(msgItem.getSelfuin());//????????????????????????????????????

                        }
                    }
                    MsgReCallUtil.notifyHasDoWhileReply(robotContentProvider, message, msgItem);


                } else if (ErrorHelper.isNotSupportMsgType(resultBean.getCode())) {
//                    MsgItem msgItem1 = msgItem.setMessage("??????????????????????????????" + msgItem.getMessage() + "" + resultBean.getText()).setCode(-1);
//                    notifyHasDoWhileReply(msgItem1);
                    LogUtil.writeLog("?????????????????????" + resultBean);

                } else {
//                    String msg = msgItem.setMessage("??????????????????????????????" + msgItem.getMessage() + ",type:" + msgItem.getType() + "  " + msgItem.getMessage() + ErrorHelper.codeToMessage(resultBean.getCode())).setCode(-1;
                    LogUtil.writeLog("????????????????????? -e" + resultBean);
//                    notifyHasDoWhileReply());
                }
                LogUtil.writeLog("onResponse" + Thread.currentThread());

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.e(TAG, "????????????", throwable);
                if (!RobotContentProvider.getInstance().mCfBaseNetReplyErrorNotWarn) {
                    MsgReCallUtil.notifyHasDoWhileReply(robotContentProvider, "???????????? ????????????" + throwable.toString(), msgItem);

                }

            }
        });


/*

        FormBody formBody = new FormBody.Builder()//??????????????????  urlencoded
                .add("question", bean.getInfo())
                .add("limit", (new Random().nextInt(3)) + "")
                .add("api_key", robotContentProvider.robotReplyKey)
                .add("api_secret", robotContentProvider.robotReplySecret).build();
        final Request request = new Request.Builder()
                .url(Cns.ROBOT_REPLY_MOLI_URL)
                .post(formBody)
                .build();
        OkHttpClient mOkHttpClient = new OkHttpClient();
        Call call = mOkHttpClient.newCall(request);


        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                LogUtil.writeLog("fail" + call + Log.getStackTraceString(e));
                MsgReCallUtil.notifyHasDoWhileReply(robotContentProvider, msgItem.setMessage("" + e.getMessage()).setCode(Errors.NET_ERR));
            }

            @Override
            public void onResponse(@NonNull Call call, Response response) throws IOException {
                String str = response.body().string();

                ResultBean resultBean = new ResultBean();
                resultBean.setText(str);
                resultBean.setCode(TuLingType.NORMAL);

                if (robotContentProvider.mAllowReponseSelfCommand) {
                    if (resultBean.getText().length() < 10) {
                        resultBean.setText("." + resultBean.getText());

                    }
                }

                if ((resultBean.getText() + "").contains("QQ")) {
                    String tep = "???????????????,??????????????????,????????????,???????????????????";
                    resultBean.setText(tep);
                } else if (resultBean.getText().equals("[cqname]")) {
                    String tep = "?????????????????????,??????????????????????????????????????????????????????->???????????????key??????";
                    resultBean.setText(tep);
                }

                if (RegexUtils.isContaineQQOrPhone(resultBean.getText())) {
                    resultBean.setText("?????????????????????[" + msgItem.getMessage() + "]?????????????????????????????????????????????,???QQ ????????????,????????????????????????????????????!");


                }
                resultBean.setText(resultBean.getText().replace("[cqname]", RobotContentProvider.getInstance().mLocalRobotName));
                resultBean.setText(resultBean.getText().replace("[name]", NickNameUtils.formatNickname(msgItem)));

                if (resultBean.getCode() == TuLingType.NORMAL || resultBean.getCode() == TuLingType.LINK) {
                    String message = resultBean.getDetailMsg();


                    if (intercept != null && intercept.isNeedIntercept(resultBean)) {

                        return;
                    }
                    if (MsgTypeConstant.MSG_ISTROP_GROUP_PRIVATE_MSG_1 == msgItem.getIstroop() || MsgTypeConstant.MSG_ISTROOP__GROUP_PRIVATE_MSG == msgItem.getIstroop() || 0 == msgItem.getIstroop()) {
                        if (!msgItem.getSenderuin().equals(msgItem.getSelfuin()) && !msgItem.getFrienduin().equals(msgItem.getSelfuin())) {
                            //
                            msgItem.setExtrajson(msgItem.getSelfuin());//????????????????????????????????????

                        }
                    }
                    MsgReCallUtil.notifyHasDoWhileReply(robotContentProvider, message, msgItem);


                } else if (ErrorHelper.isNotSupportMsgType(resultBean.getCode())) {
//                    MsgItem msgItem1 = msgItem.setMessage("??????????????????????????????" + msgItem.getMessage() + "" + resultBean.getText()).setCode(-1);
//                    notifyHasDoWhileReply(msgItem1);
                    LogUtil.writeLog("?????????????????????" + resultBean);

                } else {
//                    String msg = msgItem.setMessage("??????????????????????????????" + msgItem.getMessage() + ",type:" + msgItem.getType() + "  " + msgItem.getMessage() + ErrorHelper.codeToMessage(resultBean.getCode())).setCode(-1;
                    LogUtil.writeLog("????????????????????? -e" + resultBean);
//                    notifyHasDoWhileReply());
                }
                LogUtil.writeLog("onResponse" + str + Thread.currentThread());


            }

        });
*/
    }


    public static void doTuLing(final RobotContentProvider robotContentProvider, final MsgItem msgItem, RequestBean bean, GroupWhiteNameBean whiteNameBean, final IIntercept<ResultBean> intercept) {

        Retrofit retrofit = HttpUtilRetrofit.buildRetrofit(Cns.ROBOT_REPLY_TULING_URL);
        retrofit.create(TuLingAPI.class).query(bean.getKey(), bean.getInfo().trim(), bean.getUserid())
                .subscribeOn(Schedulers.io())
                .map((s -> {
                    return JSON.parseObject(s, ResultBean.class);
                }))

                .map(RXUtil.mapTranslateFunction(whiteNameBean))//???????????????????????? ???????????? ???
                .observeOn(AndroidSchedulers.mainThread())

//                .retryWhen(new RetryWithDelay(3, 3000))

                /*    .onExceptionResumeNext(new Observable<String>() {
                        @Override
                        protected void subscribeActual(Observer<? super String> observer) {
                            observer.onNext("?????????????????????");
                            observer.onComplete();
                        }
                    })*/
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ResultBean>() {
            @Override
            public void accept(ResultBean resultBean) throws Exception {

                if (robotContentProvider.mAllowReponseSelfCommand) {
                    if (resultBean.getText().length() < 10) {
                        resultBean.setText("\t" + resultBean.getText());

                    }
                }

                if ((resultBean.getText() + "").contains("QQ")) {
                    String tep = "???????????????,??????????????????,????????????,???????????????????";
                    resultBean.setText(tep);
                }

                if (RegexUtils.isContaineQQOrPhone(resultBean.getText())) {
                    resultBean.setText("?????????????????????[" + msgItem.getMessage() + "]?????????????????????????????????????????????,???QQ ????????????,????????????????????????????????????!");


                }


                if (resultBean.getCode() == TuLingType.NORMAL || resultBean.getCode() == TuLingType.LINK) {
                    String message = resultBean.getDetailMsg();


                    if (intercept != null && intercept.isNeedIntercept(resultBean)) {

                        return;
                    }
                    if (MsgTypeConstant.MSG_ISTROP_GROUP_PRIVATE_MSG_1 == msgItem.getIstroop() || MsgTypeConstant.MSG_ISTROOP__GROUP_PRIVATE_MSG == msgItem.getIstroop() || 0 == msgItem.getIstroop()) {
                        if (!msgItem.getSenderuin().equals(msgItem.getSelfuin()) && !msgItem.getFrienduin().equals(msgItem.getSelfuin())) {
                            //
                            msgItem.setExtrajson(msgItem.getSelfuin());//????????????????????????????????????

                        }
                    }
                    MsgReCallUtil.notifyHasDoWhileReply(robotContentProvider, message, msgItem);


                } else if (ErrorHelper.isNotSupportMsgType(resultBean.getCode())) {
//                    MsgItem msgItem1 = msgItem.setMessage("??????????????????????????????" + msgItem.getMessage() + "" + resultBean.getText()).setCode(-1);
//                    notifyHasDoWhileReply(msgItem1);
                    LogUtil.writeLog("?????????????????????" + resultBean);

                } else {
//                    String msg = msgItem.setMessage("??????????????????????????????" + msgItem.getMessage() + ",type:" + msgItem.getType() + "  " + msgItem.getMessage() + ErrorHelper.codeToMessage(resultBean.getCode())).setCode(-1;
                    LogUtil.writeLog("????????????????????? -e" + resultBean);
//                    notifyHasDoWhileReply());
                }
                LogUtil.writeLog("onResponse" + resultBean.getText() + Thread.currentThread());
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.e(TAG, "????????????", throwable);
                if (!RobotContentProvider.getInstance().mCfBaseNetReplyErrorNotWarn) {
                    MsgReCallUtil.notifyHasDoWhileReply(robotContentProvider, "???????????? ????????????" + throwable.toString(), msgItem);

                }
            }
        });


         /*       .retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                        return Observable.error(throwableObservable);
                    }
                });*/

/*
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(MsgTypeConstant.info, bean.getInfo() + "");
            jsonObject.put(MsgTypeConstant.key, bean.getKey() + "");
            jsonObject.put(MsgTypeConstant.userid, bean.getUserid() + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient mOkHttpClient = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody formBody = RequestBody.create(mediaType, jsonObject.toString());
        final Request request = new Request.Builder()
                .url(Cns.ROBOT_REPLY_TULING_URL)
                .post(formBody)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {

                LogUtil.writeLog("fail" + call + Log.getStackTraceString(e));
                MsgReCallUtil.notifyHasDoWhileReply(robotContentProvider, msgItem.setMessage("" + e.getMessage()).setCode(Errors.NET_ERR));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String str = response.body().string();

                ResultBean resultBean = JSON.parseObject(str, ResultBean.class);

                if (robotContentProvider.mAllowReponseSelfCommand) {
                    if (resultBean.getText().length() < 10) {
                        resultBean.setText("\t" + resultBean.getText());

                    }
                }

                if ((resultBean.getText() + "").contains("QQ")) {
                    String tep = "???????????????,??????????????????,????????????,???????????????????";
                    resultBean.setText(tep);
                }

                if (RegexUtils.isContaineQQOrPhone(resultBean.getText())) {
                    resultBean.setText("?????????????????????[" + msgItem.getMessage() + "]?????????????????????????????????????????????,???QQ ????????????,????????????????????????????????????!");


                }


                if (resultBean.getCode() == TuLingType.NORMAL || resultBean.getCode() == TuLingType.LINK) {
                    String message = resultBean.getDetailMsg();


                    if (intercept != null && intercept.isNeedIntercept(resultBean)) {

                        return;
                    }
                    if (MsgTypeConstant.MSG_ISTROP_GROUP_PRIVATE_MSG_1 == msgItem.getIstroop() || MsgTypeConstant.MSG_ISTROOP__GROUP_PRIVATE_MSG == msgItem.getIstroop() || 0 == msgItem.getIstroop()) {
                        if (!msgItem.getSenderuin().equals(msgItem.getSelfuin()) && !msgItem.getFrienduin().equals(msgItem.getSelfuin())) {
                            //
                            msgItem.setExtrajson(msgItem.getSelfuin());//????????????????????????????????????

                        }
                    }
                    MsgReCallUtil.notifyHasDoWhileReply(robotContentProvider, message, msgItem);


                } else if (ErrorHelper.isNotSupportMsgType(resultBean.getCode())) {
//                    MsgItem msgItem1 = msgItem.setMessage("??????????????????????????????" + msgItem.getMessage() + "" + resultBean.getText()).setCode(-1);
//                    notifyHasDoWhileReply(msgItem1);
                    LogUtil.writeLog("?????????????????????" + resultBean);

                } else {
//                    String msg = msgItem.setMessage("??????????????????????????????" + msgItem.getMessage() + ",type:" + msgItem.getType() + "  " + msgItem.getMessage() + ErrorHelper.codeToMessage(resultBean.getCode())).setCode(-1;
                    LogUtil.writeLog("????????????????????? -e" + resultBean);
//                    notifyHasDoWhileReply());
                }
                LogUtil.writeLog("onResponse" + str + Thread.currentThread());
            }

        });*/
    }


    /**
     * ????????????????????? ???????????? ?????????item?????????????????????????????? ??? ??????????????????
     *
     * @return
     */
  /*  @Deprecated
    private MsgItem fixMsgItem2ResponseResultItem(String replycontent, MsgItem msgItem) {
      if (mCfBaseReplyShowNickName && msgItem.getIstroop() == 1) {
            replycontent = "@" + msgItem.getNickname() + " " + replycontent;
        }
        return msgItem.setMessage(replycontent);
    }*/
    public boolean isManager(IMsgModel item) {
        if (item.getSenderuin().equals(item.getSelfuin())) {
            return true;
        }
        if (item.getIstroop() == 1000) {

            return isManager(item.getFrienduin());//?????????frienduin
        } else {

            return isManager(item.getSenderuin());
        }

    }

    public boolean isManager(String qq) {
        if (Cns.DEFAULT_QQ_SMALL_ADMIN.equals(qq))//??????????????????????????????????????????????????????????
        {
            return true;
        }

        boolean containAccount = AccountUtil.isContainAccount(mSuperManagers, qq);
        return containAccount;

    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param me
     * @param bean
     * @return
     */
    public boolean isManagerLestThanOrEqalMe(MsgItem item, AdminBean me, AdminBean bean) {

        if (me == null && item.getSenderuin().equals(Cns.DEFAULT_QQ_SMALL_ADMIN)) {
            return true;
        }
        if (item.getSelfuin().equals(item.getSenderuin())) {
            return true;//???????????????????????????????????????????????????????????????
        }

        if (bean == null) {
            return false;//?????????????????????????????????,
        }
        if (bean.getLevel() < me.getLevel()) {
            return true;
        }

        return false;

    }


    /**
     * ????????????????????????????????????
     *
     * @param args
     * @param position
     * @return
     */
    public String getCurrentArgAndAfter(String[] args, int position) {

        if (ParamParseUtil.isInvalidArgument(args, position)) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = position; i < args.length; i++) {
            sb.append(args[i]);
            if (i != args.length && args.length > 1) {//????????????????????????????????????????????????.
                sb.append(" ");
            }
        }
        return sb.toString();

    }

    public static final int sCmdPposition = 0;
    public static String mTempIGgnoresManager = null;

    public boolean doCommendLogic(MsgItem item, boolean isManager, boolean isSelf, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, Integer flag, boolean isgroupMsg) {

        GroupWhiteNameBean nameBean = new GroupWhiteNameBean();
        nameBean.setAccount(item.getFrienduin());
        return doCommendLogic(item, isManager, isSelf, atPair, flag, isgroupMsg, nameBean);
    }

    public boolean doCommendLogic(final MsgItem item, boolean isManager, boolean isSelf, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, Integer flag, final boolean isgroupMsg, final GroupWhiteNameBean nameBean) {


        if (item.getMessage() == null) {
            return false;

        }
        if (!isSelf && nameBean != null && nameBean.isCmdsilent() && !isManager && AppContext.isVip()) {
            LogUtil.writeLog("??????????????????????????????????????????!.");
            return true;
        }


        if (isSelf) {
            if (!mAllowReponseSelfCommand) {
                LogUtil.writeLog("??????????????????????????????.");
                return true;
            }
            if (item.getMessage().length() > 5) {


               /* if (RegexUtils.checkIsChinese(item.getMessage())) {
                    LogUtil.writeLog("????????????????????????" + item.getMessage());
                    return true;

                }


                if (!RegexUtils.checkIsContainNumber(item.getMessage())) {
                    LogUtil.writeLog("????????????????????????,???????????????" + item.getMessage());
                    return false;

                }*/
            }


        }


        //??????????????????????????????


        String beforeArg = item.getMessage();
        final Pair<String, String> param = CmdConfig.fitParam(item.getMessage());
        if (param == null) {
            return false;
        } else {
            if (MsgTyeUtils.isSelfMsg(item)) {//???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if (isgroupMsg) {
                    FloorUtils.onReceiveNewMsg(_dbUtils, item);
                }
            }
        }
//        //??????????????????
//        String[] tempArr = message.split(" ");

        String[] args;
        String argStr = param.second;
        if (TextUtils.isEmpty(argStr)) {
            args = new String[]{};
        } else {
            args = argStr.split(" ");
        }

        String commend = param.first;

        if (!TextUtils.isEmpty(mTempIGgnoresManager) && item.getSenderuin().equals(mTempIGgnoresManager)) {
            if (!isManager) {
                return false;
            }
            if (CmdConfig.IGNORE_TEMP_IGNORE_ME_DISABLE.contains(commend)) {
                mTempIGgnoresManager = null;
                MsgReCallUtil.notifyJoinReplaceMsgJump(this, "???????????????" + NickNameUtils.formatNickname(item) + "???????????????", item);
            }
            return true;
        }
        commend = commend.toLowerCase();

        switch (commend) {

            case CmdConfig.LIST_WHITE_NAME: {

                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return false;
                }
                StringBuffer sb = new StringBuffer();
                sb.append("??????????????????:\n");
                for (AccountBean groupWhiteName : mQQGroupWhiteNames) {


                    sb.append(groupWhiteName.getAccount() + ":");
                    sb.append((groupWhiteName.isDisable() ? "[??????]" : "[??????]") + "\n");
                }
                MsgReCallUtil.notifyHasDoWhileReply(this, "" + sb.toString(), item);
            }
            break;
            case CmdConfig.HELP:
            case CmdConfig.HELP_3:
                if (!StringUtils.isEqualStr(commend, beforeArg)) {
                    return false;
                }
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }
                MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????????????????????????????????????????????????????,?????????????????????,????????????????????????????????????" + CmdConfig.CMD, item);
                break;
            case CmdConfig.CMD:
            case CmdConfig.HELP_MENU:
            case CmdConfig.CMD1: {


                if (!StringUtils.isEqualStr(commend, beforeArg)) {

                    return false;
                }
                if (nameBean != null && nameBean.isAllowMenu()) {

                } else if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {


                    return true;
                }
                String message = CmdConfig.printSupportCmd();
                MsgReCallUtil.notifyHasDoWhileReply(this, message, item);
            }
            break;

            case CmdConfig.REVOKE_MSG:
            case CmdConfig.REVOKE_MSG_1: {

                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean, true)) {
                    return true;
                }
                if (!isgroupMsg) {
                    return false;
                }
                String arg1 = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                String arg2 = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgSecond);
                String account;
                String fixArg;
                int revokeCount = 1;
                if (atPair != null && atPair.second != null && atPair.second.second != null && atPair.second.second.size() > 0) {

//                            MsgReCallUtil.notifyJoinReplaceMsgJump(this, "????????????????????????QQ" + atPair.second.second.get(0).getAccount() + "?????????!", item);
                    account = atPair.second.second.get(0).getAccount();
                    fixArg = ParamParseUtil.mergeParameters(args, ParamParseUtil.sArgFirst);

                } else {
                    if (arg1 != null && (arg1.equals("?????????") || arg1.equals("robot") || arg1.equals("??????"))) {
                        arg1 = item.getSelfuin();
                    }
                    if ("????????????".equals(arg1)) {
                        account = "0";

                        if (RemoteService.isIsInit()) {
                            String s = RemoteService.revokeMsg(item.getFrienduin(), account, 100, 0);
                            if (s != null) {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????????????????:" + s, item);
                                return true;
                            }
                        }
                        MsgReCallUtil.notifyRevokeMsgJumpByCount(this, item.getFrienduin(), account, 100, item);
                        MsgReCallUtil.notifyHasDoWhileReply(this, "????????????,???????????????????????????????????????", item);
                        return true;
                    } else if ("?????????".equals(arg1)) {
                        account = "0";

                        if (RemoteService.isIsInit()) {
                            int revokecount = 1;
                            revokeCount = ParseUtils.parseInt(arg2, revokeCount);
                            String s = RemoteService.revokeMsg(item.getFrienduin(), account, revokeCount, 0);
                            if (s != null) {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????????????????" + revokeCount + "???,????????????:" + s, item);
                                return true;
                            }
                        }
                        MsgReCallUtil.notifyRevokeMsgJumpByCount(this, item.getFrienduin(), account, revokeCount, item);
                        MsgReCallUtil.notifyHasDoWhileReply(this, "????????????,?????????????????????" + revokeCount + "?????????", item);
                        return true;
                    }
                    if ("??????".equals(arg1)) {
                        account = FloorUtils.getFloorQQ(item.getFrienduin());
                        fixArg = arg1;
                    } else if (TextUtils.isEmpty(arg1)) {

                        account = FloorUtils.getFloorQQ(item.getFrienduin());
                        fixArg = "";
                    } else if (RegexUtils.checkNoSignDigit(arg1)) {
                        if (FloorUtils.isFloorData(arg1)) {
                            account = FloorUtils.getQQByMsgItemFromValue1(item, arg1);
                        } else {
                            account = arg1;
                        }
                        fixArg = arg2;
                    } else {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????????????????,?????????????????? ???????????????\n[???????????? ????????????/??????]\n[??????/????????? ????????????/??????]\n[?????? ????????????/??????]\n[QQ ????????????/??????]\n[??????????????????????????????????????????]\n[????????? ????????????]", item);
                        return true;

                    }


                }

                if (TextUtils.isEmpty(account)) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????????????????????????????????????????????", item);
                    return true;
                }

                String tip = "";


                if (fixArg.equals("all") || fixArg.equals("??????") || fixArg.contains("??????")) {
                    revokeCount = 500;
                    tip = "??????";
                } else if (RegexUtils.checkNoSignDigit(fixArg)) {//????????????
                    revokeCount = ParseUtils.parseInt(fixArg);
                    tip = "??????" + revokeCount + "???";
                } else {
                    if (!TextUtils.isEmpty(fixArg)) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????????????????,??????'" + fixArg + "'?????????", item);
                        return true;

                    }
                }
                if (RemoteService.isIsInit()) {
                    String s = RemoteService.revokeMsg(item.getFrienduin(), account, revokeCount, item.getMessageID());
                    if (s != null) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "" + NickNameUtils.formatNickname(item.getFrienduin(), account) + "???" + tip + "??????????????????:" + s, item);
                        return true;
                    }
                }

                MsgReCallUtil.notifyRevokeMsgJumpByCount(this, item.getFrienduin(), account, revokeCount, item);
                MsgReCallUtil.notifyHasDoWhileReply(this, "????????????,??????" + NickNameUtils.formatNickname(item.getFrienduin(), account) + "???" + tip + "??????", item);

                return true;

            }
            case CmdConfig.ADD_MANAGER: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean, true)) {
                    return true;
                }

                String arg1 = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);

                if (TextUtils.isEmpty(arg1)) {


                    if (atPair != null && atPair.second != null && atPair.second.second != null && atPair.second.second.size() > 0) {

//                            MsgReCallUtil.notifyJoinReplaceMsgJump(this, "????????????????????????QQ" + atPair.second.second.get(0).getAccount() + "?????????!", item);
                        arg1 = atPair.second.second.get(0).getAccount();


                    } else {

                        MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????????????????", item);
                        return true;
                    }


                } else if (RegexUtils.checkNoSignDigit(arg1)) {
                    arg1 = FloorUtils.getQQByMsgItemFromValue1(item, arg1);

                    if (TextUtils.isEmpty(arg1)) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????!", item);
                        return true;
                    }

                } else {

                    MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????????????????,??????????????????!", item);
                }


                AdminBean bean = new AdminBean();
                AdminBean accountMe = (AdminBean) AccountUtil.findAccount(mSuperManagers, item.getSenderuin(), false);
                int level = 0;
                if (accountMe == null) {
                    if (!item.getSenderuin().equals(Cns.DEFAULT_QQ_SMALL_ADMIN)) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????????????????,?????????????????????????????????", item);
                        return true;

                    } else {
                        level = -2;
                    }
                } else {
                    bean.setLevel(accountMe.getLevel() - 1);
                }

                bean.setLevel(level);
                bean.setAccount(arg1);
                long insert = DBHelper.getSuperManager(AppContext.getDbUtils()).insert(bean);
                if (insert > 0) {
                    mSuperManagers.add(bean);
                    MsgReCallUtil.notifyHasDoWhileReply(this, "????????????,?????????" + NickNameUtils.formatNickname(item.getFrienduin(), arg1) + "?????????", item);
                    ;//????????????????????????????????????????????????????????????
                } else {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "???" + NickNameUtils.formatNickname(item.getFrienduin(), arg1) + "?????????????????????,???????????????", item);
                    ;
                }
                return true;


            }

            case CmdConfig.DELETE_MANAGER: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean, true)) {
                    return true;
                }

                String arg1 = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);


                arg1 = FloorUtils.getQQByMsgItemFromValue1(item, arg1);


                if (TextUtils.isEmpty(arg1)) {


                    if (atPair != null && atPair.second != null && atPair.second.second != null && atPair.second.second.size() > 0) {

//                            MsgReCallUtil.notifyJoinReplaceMsgJump(this, "????????????????????????QQ" + atPair.second.second.get(0).getAccount() + "?????????!", item);
                        arg1 = atPair.second.second.get(0).getAccount();


                    } else {

                        MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????????????????", item);
                    }

                    return true;

                } else {

                    if (!RegexUtils.checkNoSignDigit(arg1)) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????", item);
                        return true;

                    }

                }


                AdminBean accountDelte = (AdminBean) AccountUtil.findAccount(mSuperManagers, arg1, false);
                if (accountDelte == null) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????" + NickNameUtils.formatNickname(item.getFrienduin(), arg1) + ",??????????????????????????????", item);
                    return true;
                }

                AdminBean accountMe = (AdminBean) AccountUtil.findAccount(mSuperManagers, item.getSenderuin(), false);
                if (accountMe == null && !item.getSenderuin().equals(Cns.DEFAULT_QQ_SMALL_ADMIN)) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????????????????,?????????????????????????????????", item);
                    return true;
                }
                if (item.getSenderuin().equals(accountDelte.getAccount())) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, NickNameUtils.formatNickname(item) + " ???????????????????????????????????????", item);

                }

                if (!isManagerLestThanOrEqalMe(item, accountMe, accountDelte)) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + NickNameUtils.formatNickname(item) + " ?????????,?????????????????????" + NickNameUtils.formatNickname(item.getFrienduin(), accountDelte.getAccount()) + ",?????????????????????????????????,?????????????????????" + accountMe.getLevel() + ",?????????????????????:" + accountDelte.getLevel() + "(?????????????????????????????????)", item)
                    ;
                    return true;
                }
                int i = DBHelper.getSuperManager(AppContext.getDbUtils()).deleteByColumn(AdminBean.class, FieldCns.FIELD_ACCOUNT, arg1);
                if (i > 0) {
                    AccountUtil.removeAccount(mSuperManagers, arg1);
                    MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????" + NickNameUtils.formatNickname(item.getFrienduin(), arg1) + "??????", item)
                    ;

                } else {

                    MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????" + NickNameUtils.formatNickname(item.getFrienduin(), arg1) + "??????,????????????????????????", item);
                    ;
                }
                return true;
            }

//            case CmdConfig.SUPER_MAANGER:

            case CmdConfig.MAANGER_ALL: {
                if (args.length > 1 && isSelf) {//??????????????????.
                    return false;
                }


                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }

                StringBuffer sb = new StringBuffer();
                sb.append("[?????????????????????]\n???????????????:\n");
                for (AccountBean bean : mSuperManagers) {
                    sb.append(bean.getAccount() + ":");
                    sb.append((bean.isDisable() ? "[??????]" : "[??????]") + "\n");
                }

                if (isgroupMsg && nameBean != null) {
                    sb.append("???????????????(?????????):\n");
                    if (nameBean.getAdmins() != null) {
                        String[] split = nameBean.getAdmins().split(",");
                        if (split.length > 0) {
                            for (String s : split) {
                                sb.append(s + ",??????:" + NickNameUtils.queryMatchNickname(nameBean.getAccount(), s, true) + "\n");
                            }

                        } else {

                            sb.append("???\n");
                        }
                    } else {
                        sb.append("???\n");
                    }
                }
                if (isgroupMsg && nameBean != null) {
                    sb.append("???????????????(???????????????????????????):\n");

                    List<GroupAdaminBean> groupAdaminBeans = DBHelper.getGroupAdminTableUtil(_dbUtils).queryAllByFieldLike(GroupAdaminBean.class, FieldCns.FIELD_GROUP, "" + nameBean.getAccount());
                    if (groupAdaminBeans != null && groupAdaminBeans.size() > 0) {
                        for (int i = 0; i < groupAdaminBeans.size(); i++) {
                            String account = groupAdaminBeans.get(i).getAccount();
                            sb.append(account + ",??????:" + NickNameUtils.queryMatchNickname(nameBean.getAccount(), account, true) + "\n");
                        }
                    } else {
                        sb.append("???\n");
                    }

                }
                sb.append("???????????????(??????):\n");
                if (isgroupMsg && nameBean != null && RemoteService.isIsInit()) {
                    String troopowneruin = RemoteService.queryGroupField(nameBean.getAccount(), "troopowneruin");
                    if (troopowneruin != null) {
                        sb.append(troopowneruin + ",??????:" + NickNameUtils.queryMatchNickname(nameBean.getAccount(), troopowneruin, true) + "\n");
                    }
                    String administrator = RemoteService.queryGroupField(nameBean.getAccount(), "Administrator");
                    if (administrator != null) {
                        String[] split = administrator.split("\\|");//ignore_include
                        if (split.length > 0) {
                            for (String s : split) {
                                sb.append(s + ",??????:" + NickNameUtils.queryMatchNickname(nameBean.getAccount(), s, true) + "\n");
                            }
                        }
                    }
                    sb.append("\n");

                } else {
                    sb.append("???????????????????????????????????????\n");
                }
                MsgReCallUtil.notifyHasDoWhileReply(this, "" + sb.toString(), item);

                return true;
            }
            case CmdConfig.SUPER_MAANGER_CMD1: {


                if (args.length > 1 && isSelf) {//??????????????????.
                    return false;
                }

                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }

                StringBuffer sb = new StringBuffer();
                sb.append("[???????????????]\n");
                for (AccountBean bean : mSuperManagers) {
                    sb.append(bean.getAccount() + ":");
                    sb.append((bean.isDisable() ? "[??????]" : "[??????]") + "\n");
                }
                MsgReCallUtil.notifyHasDoWhileReply(this, "" + sb.toString(), item);


            }

            break;

            case CmdConfig.ADD_CURRENT_GROUP_MAANAGER: {


                if (args.length > 1 && isSelf) {//??????????????????.
                    return false;
                }

                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }
                if (!isgroupMsg) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, Cns.TIP_PLEASE_GROUP_CHAT, item);
                    return true;


                }
                String arg1 = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                if (TextUtils.isEmpty(arg1)) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????????????????", item);
                    return true;
                }

                if (!RegexUtils.iseQQ(arg1)) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????QQ", item);
                    return true;
                }
                String admins = nameBean.getAdmins();
                boolean containByArray = AccountUtil.isContainByArray(arg1, ",", admins);
                if (containByArray) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "??????,?????????" + NickNameUtils.queryMatchNickname(item.getFrienduin(), arg1, false) + "?????????!", item);
                    return true;
                } else {
                    nameBean.setAdmins(AccountUtil.addValueByArray(arg1, ",", admins));
                    int update = DBHelper.getQQGroupWhiteNameDBUtil(_dbUtils).update(nameBean);
                    MsgReCallUtil.notifyHasDoWhileReply(this, "????????????:" + ((update > 0) ? "??????" : "??????"), item)
                    ;
                    return true;
                }

            }


            case CmdConfig.REMOVE_CURRENT_GROUP_MAANAGER: {


                if (args.length > 1 && isSelf) {//??????????????????.
                    return false;
                }

                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }
                if (!isgroupMsg) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, Cns.TIP_PLEASE_GROUP_CHAT, item);
                    return true;


                }
                String arg1 = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                if (TextUtils.isEmpty(arg1)) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????????????????", item);
                    return true;
                }

                if (!RegexUtils.iseQQ(arg1)) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????QQ", item);
                    return true;
                }
                String admins = nameBean.getAdmins();
                boolean containByArray = AccountUtil.isContainByArray(arg1, ",", admins);
                if (!containByArray) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "??????,?????????" + NickNameUtils.queryMatchNickname(item.getFrienduin(), arg1, false) + "?????????!", item);
                    return true;
                } else {
                    nameBean.setAdmins(AccountUtil.removeValueByArray(arg1, ",", admins));
                    int update = DBHelper.getQQGroupWhiteNameDBUtil(_dbUtils).update(nameBean);
                    MsgReCallUtil.notifyHasDoWhileReply(this, "????????????:" + ((update > 0) ? "??????" : "??????"), item);
                    return true;
                }

            }

            case CmdConfig.TEST_URL: {
                if (!isManager) {
                    return false;
                }
                String arg1 = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);

                if (TextUtils.isEmpty(arg1)) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????", item);
                } else {
                    HttpUtil.queryData(arg1, new RequestListener() {
                        @Override
                        public void onSuccess(String str) {

                            if (str.length() < 150) {

                                MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.this, "????????????:" + str, item);
                            } else {
                                MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.this, "????????????,???50/" + str.length() + "????????????:" + str.substring(0, 150), item);

                            }
                        }

                        @Override
                        public void onFail(String str) {
                            MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.this, "????????????:" + str, item);
                        }
                    });
                }

            }
            return true;
            case CmdConfig.FECTCH_MUSIC:
            case CmdConfig.FECTCH_MUSIC1:
            case CmdConfig.FECTCH_MUSIC2: {


                if (isgroupMsg && nameBean != null) {
                    if (!nameBean.isAllowmusic()) {
                        if (StringUtils.isEqualStr(beforeArg, commend)) {
                            MsgReCallUtil.smartReplyMsg(AppConstants.ACTION_OPERA_TIP + commend + AppConstants.FUNC_IS_DISABLE_TIP, isgroupMsg, nameBean, item);
                            return true;

                        }
                        return false;
                    } else {

                    }
                } else {
                    if (flag < INeedReplayLevel.INTERCEPT_ALL_HEIGHT) {//????????????????????????????????????????????????????????????????????????????????????true????????????
                        return true;
                    }

                    if (isNeedIgnoreNormalCommand(item, atPair, flag, true, isSelf, isgroupMsg, nameBean)) {
                        if (ConfigUtils.IsNeedAt(nameBean) && atPair.second.first == false) {//??????????????????????????? . ????????????2?????????????????????????????????

                            return false;
                        }
                    }
                }

                if (nameBean != null && nameBean.isNeedaite()) {
                    if (nameBean.isSelfcmdnotneedaite() || atPair.second.first) {
                        //????????????????????????????????????????????????????????????????????????????????????????????????
                    } else {

                        return true;
                    }
                }
                if (atPair != null && atPair.first && atPair.second.first == false) {
                    List<GroupAtBean> atBeans = atPair.second.second;
                    for (GroupAtBean atBean : atBeans) {

                        if (isManager(atBean.getAccount())) {
                            return true;//??????????????? ???????????????????????????2????????????????????????,???????????????
                        }
                    }
                }

                MusicMoudle.onReceiveMusic(this, item, argStr, atPair, args, isManager);


            }
            return true;

            case CmdConfig.SEARCH_2:

            case CmdConfig.SEARCH_1:
            case CmdConfig.SEARCH: {
                if (isgroupMsg && nameBean != null) {
                    if (!nameBean.isAllowsearchpic()) {
                        LogUtil.writeLog("????????????????????????");
                        if (StringUtils.isEqualStr(beforeArg, commend)) {
                            MsgReCallUtil.smartReplyMsg(AppConstants.ACTION_OPERA_TIP + commend + AppConstants.FUNC_IS_DISABLE_TIP, isgroupMsg, nameBean, item);
                            return true;

                        }
                        if (!isManager) {
                            return false;

                        }
                    } else {

                    }
                } else {
                    if (flag < INeedReplayLevel.INTERCEPT_ALL_HEIGHT) {//????????????????????????????????????????????????????????????????????????????????????true????????????
                        return true;
                    }


                    if (isgroupMsg && nameBean != null) {
                        boolean isCurrentGroupAdmin = isCurrentGroupAdminFromDb(nameBean, item.getSenderuin(), item.getFrienduin());
                        nameBean.setIsCurrentGroupAdmin(isCurrentGroupAdmin);

                    }
                    if (isNeedIgnoreNormalCommand(item, atPair, flag, true, isSelf, isgroupMsg, nameBean)) {
                        if (ConfigUtils.IsNeedAt(nameBean) && atPair.second.first == false) {//??????????????????????????? . ????????????2?????????????????????????????????

                            return false;
                        }
                    }
                }

                if (nameBean != null && nameBean.isNeedaite()) {
                    if (nameBean.isSelfcmdnotneedaite() || atPair.second.first) {
                        //????????????????????????????????????????????????????????????????????????????????????????????????
                    } else {

                        return true;
                    }
                }
                if (atPair != null && atPair.first && atPair.second.first == false) {
                    List<GroupAtBean> atBeans = atPair.second.second;
                    for (GroupAtBean atBean : atBeans) {

                        if (isManager(atBean.getAccount())) {
                            if (atBean.getAccount().startsWith("694")) {

                            } else {
                                return true;//??????????????? ???????????????????????????2????????????????????????,???????????????

                            }
                        }
                    }
                }

                String text = ParamParseUtil.mergeParameters(args, ParamParseUtil.sArgFirst);
                if (CmdConfig.SEARCH_2.equals(commend)) {
                    SearchPluginMainImpl.doSendCacheDir(item, text);
                    return true;

                }
                if (TextUtils.isEmpty(text)) {
                    MsgReCallUtil.smartReplyMsg("????????????????????????,??????" + CmdConfig.SEARCH_2 + "?????????????????????????????????????????????", isgroupMsg, nameBean, item);
                } else {

                    SearchPluginMainImpl.doSearchPicLogic(item, text);
                }


            }
            return true;

            case CmdConfig.TRANSLATE: {
                if (isgroupMsg && nameBean != null) {
                    if (!nameBean.isAllowTranslate()) {
                        LogUtil.writeLog("?????????????????????");
                        if (StringUtils.isEqualStr(beforeArg, commend)) {
                            MsgReCallUtil.smartReplyMsg(AppConstants.ACTION_OPERA_TIP + commend + AppConstants.FUNC_IS_DISABLE_TIP, isgroupMsg, nameBean, item);
                            return true;
                        }
                        if (!isManager) {
                            return false;

                        }
                    } else {

                    }
                } else {
                    if (flag < INeedReplayLevel.INTERCEPT_ALL_HEIGHT) {//????????????????????????????????????????????????????????????????????????????????????true????????????
                        return true;
                    }
                    if (isgroupMsg && nameBean != null) {
                        boolean isCurrentGroupAdmin = isCurrentGroupAdminFromDb(nameBean, item.getSenderuin(), item.getFrienduin());
                        nameBean.setIsCurrentGroupAdmin(isCurrentGroupAdmin);

                    }
                    if (isNeedIgnoreNormalCommand(item, atPair, flag, true, isSelf, isgroupMsg, nameBean)) {
                        if (ConfigUtils.IsNeedAt(nameBean) && atPair.second.first == false) {//??????????????????????????? . ????????????2?????????????????????????????????
                            return false;
                        }
                    }
                }

                if (nameBean != null && nameBean.isNeedaite()) {
                    if (nameBean.isSelfcmdnotneedaite() || atPair.second.first) {
                        //????????????????????????????????????????????????????????????????????????????????????????????????
                    } else {

                        return true;
                    }
                }
                if (atPair != null && atPair.first && atPair.second.first == false) {
                    List<GroupAtBean> atBeans = atPair.second.second;
                    for (GroupAtBean atBean : atBeans) {

                        if (isManager(atBean.getAccount())) {
                            if (atBean.getAccount().startsWith("694")) {

                            } else {
                                return true;//??????????????? ???????????????????????????2????????????????????????,???????????????

                            }
                        }
                    }
                }


                String text = ParamParseUtil.mergeParameters(args, ParamParseUtil.sArgFirst);
                if (TextUtils.isEmpty(text)) {
                    MsgReCallUtil.smartReplyMsg("???????????????????????????", isgroupMsg, nameBean, item);
                } else {
                    BaseQueryImpl.getInstance(TranslateQueryImpl.class).doAction(item, isgroupMsg, nameBean, args, atPair, text);
                }


            }
            return true;
            case CmdConfig.CARD_MSG: {
                if (isgroupMsg && nameBean != null) {
                    if (!nameBean.isAllowGenerateCardMsg()) {
                        LogUtil.writeLog("?????????????????????");
                        if (StringUtils.isEqualStr(beforeArg, commend)) {
                            MsgReCallUtil.smartReplyMsg(AppConstants.ACTION_OPERA_TIP + commend + AppConstants.FUNC_IS_DISABLE_TIP, isgroupMsg, nameBean, item);
                            return true;

                        }
                        if (!isManager) {
                            return false;

                        }

                    } else {

                    }
                } else {
                    if (flag < INeedReplayLevel.INTERCEPT_ALL_HEIGHT) {//????????????????????????????????????????????????????????????????????????????????????true????????????
                        return true;
                    }

                    if (isNeedIgnoreNormalCommand(item, atPair, flag, true, isSelf, isgroupMsg, nameBean)) {
                        if (ConfigUtils.IsNeedAt(nameBean) && atPair.second.first == false) {//??????????????????????????? . ????????????2?????????????????????????????????

                            return false;
                        }
                    }
                }

                if (nameBean != null && nameBean.isNeedaite()) {
                    if (nameBean.isSelfcmdnotneedaite() || atPair.second.first) {
                        //????????????????????????????????????????????????????????????????????????????????????????????????
                    } else {

                        return true;
                    }
                }
                if (atPair != null && atPair.first && atPair.second.first == false) {
                    List<GroupAtBean> atBeans = atPair.second.second;
                    for (GroupAtBean atBean : atBeans) {

                        if (isManager(atBean.getAccount())) {
                            if (atBean.getAccount().startsWith("694")) {

                            } else {
                                return true;//??????????????? ???????????????????????????2????????????????????????,???????????????

                            }
                        }
                    }
                }


                String text = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);

                doCardLogic(item, args, text, 0);


            }
            return true;
            case CmdConfig.TEXT_2PIC:
            case CmdConfig.TEXT_2PIC_1: {


                if (isgroupMsg && nameBean != null) {
                    if (!nameBean.isAllowtext2pic()) {
                        if (StringUtils.isEqualStr(beforeArg, commend)) {
                            MsgReCallUtil.smartReplyMsg(AppConstants.ACTION_OPERA_TIP + commend + AppConstants.FUNC_IS_DISABLE_TIP, isgroupMsg, nameBean, item);
                            return true;

                        }
                        LogUtil.writeLog("??????????????????????????????");
                        if (!isManager) {
                            return false;

                        }
                    } else {

                    }
                } else {
                    if (flag < INeedReplayLevel.INTERCEPT_ALL_HEIGHT) {//????????????????????????????????????????????????????????????????????????????????????true????????????
                        return true;
                    }

                    if (isNeedIgnoreNormalCommand(item, atPair, flag, true, isSelf, isgroupMsg, nameBean)) {
                        if (ConfigUtils.IsNeedAt(nameBean) && atPair.second.first == false) {//??????????????????????????? . ????????????2?????????????????????????????????

                            return false;
                        }
                    }
                }

                if (nameBean != null && nameBean.isNeedaite()) {
                    if (nameBean.isSelfcmdnotneedaite() || atPair.second.first) {
                        //????????????????????????????????????????????????????????????????????????????????????????????????
                    } else {

                        return true;
                    }
                }
                if (atPair != null && atPair.first && atPair.second.first == false) {
                    List<GroupAtBean> atBeans = atPair.second.second;
                    for (GroupAtBean atBean : atBeans) {

                        if (isManager(atBean.getAccount())) {
                            if (atBean.getAccount().startsWith("694")) {

                            } else {
                                return true;//??????????????? ???????????????????????????2????????????????????????,???????????????

                            }
                        }
                    }
                }


                String fontColor = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                String bgColor = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgSecond);

                int megreePosition = ParamParseUtil.sArgFirst;
                if (bgColor != null && bgColor.startsWith("#")) {
                    megreePosition = ParamParseUtil.sArgThrid;
                } else if (fontColor != null && fontColor.startsWith("#")) {
                    megreePosition = ParamParseUtil.sArgSecond;
                }


                String text = ParamParseUtil.mergeParameters(args, megreePosition);


                if (TextUtils.isEmpty(text)) {
                    MsgReCallUtil.smartReplyMsg("???????????????,?????????????????????,????????????????????????????????????????????????????????????????????????16??????????????????,?????????[" + CmdConfig.TEXT_2PIC + "#ff0000 #000000 ????????????????????? ?????????????????????]?????????????????????????????????????????????????????????????????????", isgroupMsg, nameBean, item);
                } else {
                    SearchPluginMainImpl.doText2PicLogic(item, fontColor, bgColor, text);
                }


            }

            return true;
            case CmdConfig.QRCODE:
            case CmdConfig.QRCODE_1: {


                if (isgroupMsg && nameBean != null) {
                    if (!nameBean.isAllowqrcode()) {
                        if (StringUtils.isEqualStr(beforeArg, commend)) {
                            MsgReCallUtil.smartReplyMsg(AppConstants.ACTION_OPERA_TIP + commend + AppConstants.FUNC_IS_DISABLE_TIP, isgroupMsg, nameBean, item);
                            return true;

                        }
                        if (!isManager) {
                            return false;

                        }
                    } else {

                    }
                } else {
                    if (flag < INeedReplayLevel.INTERCEPT_ALL_HEIGHT) {//????????????????????????????????????????????????????????????????????????????????????true????????????
                        return true;
                    }

                    if (isNeedIgnoreNormalCommand(item, atPair, flag, true, isSelf, isgroupMsg, nameBean)) {
                        if (ConfigUtils.IsNeedAt(nameBean) && atPair.second.first == false) {//??????????????????????????? . ????????????2?????????????????????????????????

                            return false;
                        }
                    }
                }

                if (nameBean != null && nameBean.isNeedaite()) {
                    if (nameBean.isSelfcmdnotneedaite() || atPair.second.first) {
                        //????????????????????????????????????????????????????????????????????????????????????????????????
                    } else {

                        return true;
                    }
                }
                if (atPair != null && atPair.first && atPair.second.first == false) {
                    List<GroupAtBean> atBeans = atPair.second.second;
                    for (GroupAtBean atBean : atBeans) {

                        if (isManager(atBean.getAccount())) {
                            if (atBean.getAccount().startsWith("350")) {

                            } else {
                                return true;//??????????????? ???????????????????????????2????????????????????????,???????????????

                            }
                        }
                    }
                }
           /*     String qrcodeColor = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                String bgColor = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgSecond);


                int megreePosition = ParamParseUtil.sArgFirst;
                if (bgColor != null && bgColor.startsWith("#")) {
                    megreePosition = ParamParseUtil.sArgThrid;
                } else if (qrcodeColor != null && qrcodeColor.startsWith("#")) {
                    megreePosition = ParamParseUtil.sArgSecond;
                }


                String text =null;
                if(qrcodeColor!=null&&bgColor!=null){
                    text=    ParamParseUtil.mergeParameters(args, megreePosition);
                }else{
                    qrcodeColor="#ff0000";
                    bgColor="#ffffff";
                    text=ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                }
            */
                String text = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                if (TextUtils.isEmpty(text)) {
                    MsgReCallUtil.smartReplyMsg("????????????????????????????????????", isgroupMsg, nameBean, item);
                } else {

                    new QssqTaskFix<Void, Void>(new QssqTaskFix.ICallBackImp() {
                        @Override
                        public Object onRunBackgroundThread(Object[] params) {
                            try {
                                String path = ZxingUtil.bitmap2qrcodeFile(text);
                                return path;

                            } catch (Throwable e) {
                                return e;
                            }
                        }

                        @Override
                        public void onRunFinish(Object o) {
                            if (o instanceof Throwable) {

                                MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), "????????????" + o, item);
                            } else {

                                MsgReCallUtil.notifySendPicMsg(RobotContentProvider.getInstance(), o + "", item);
                            }

                        }
                    }).execute();
                }

            }

            return true;

            case CmdConfig.ADD_LIKE: {
                boolean isCurrentGroupAdmin = false;

                if (isgroupMsg && nameBean != null) {
                    isCurrentGroupAdmin = isCurrentGroupAdminFromDb(nameBean, item.getSenderuin(), item.getFrienduin());
                    nameBean.setIsCurrentGroupAdmin(isCurrentGroupAdmin);

                }
                if (isgroupMsg && nameBean != null) {
                    if (!nameBean.isAllowzan()) {
                        if (!isManager && StringUtils.isEqualStr(beforeArg, commend)) {
                            MsgReCallUtil.smartReplyMsg(
                                    AppConstants.ACTION_OPERA_TIP + commend + AppConstants.FUNC_IS_DISABLE_TIP, isgroupMsg, nameBean, item);

                            return true;

                        }


                        LogUtil.writeLog("?????????????????????");
                        if (!isManager && !isCurrentGroupAdmin) {
                            return false;

                        }
                    } else {

                    }
                } else {
                    if (flag < INeedReplayLevel.INTERCEPT_ALL_HEIGHT) {//????????????????????????????????????????????????????????????????????????????????????true????????????
                        return true;
                    }

                    if (isNeedIgnoreNormalCommand(item, atPair, flag, true, isSelf, isgroupMsg, nameBean)) {
                        if (ConfigUtils.IsNeedAt(nameBean) && atPair.second.first == false) {//??????????????????????????? . ????????????2?????????????????????????????????

                            return false;
                        }
                    }
                }

                if (nameBean != null && nameBean.isNeedaite()) {
                    if (nameBean.isSelfcmdnotneedaite() || atPair.second.first) {
                        //????????????????????????????????????????????????????????????????????????????????????????????????
                    } else {

                        return true;
                    }
                }
        /*        if (atPair != null && atPair.first && atPair.second.first == false) {
                    List<GroupAtBean> atBeans = atPair.second.second;
                    for (GroupAtBean atBean : atBeans) {

                        if (isManager(atBean.getAccount())) {
                            if (atBean.getAccount().startsWith("694")) {

                            } else {
                                return true;//??????????????? ???????????????????????????2????????????????????????,???????????????

                            }
                        }
                    }
                }*/


                String argZan = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                String qq;
                int count = 1;


                if (atPair != null && atPair.second != null && atPair.second.second != null && atPair.second.second.size() > 0) {
                    if (!isManager && !isCurrentGroupAdmin) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????????????????QQ", item);
                        return true;
                    }
                    if (atPair.second.second.size() == 1) {
                        qq = atPair.second.second.get(0).getAccount();//?????????????????????
                        if (argZan != null && RegexUtils.checkNoSignDigit(argZan)) {
                            count = ParseUtils.parseInt(argZan);

                        }

                    } else {


                        count = ParseUtils.parseInt(ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst), 1);

                        final int finalCount = count;
                        FloorMultiUtils.doMultiFloorEachLogicFromAt(this, item, atPair.second.second, item.getFrienduin(), new FloorMultiUtils.IMultiEachCallBack<MsgItem>() {
                            @Override
                            public boolean onEachDoAndisIgnore(MsgItem bean) {

                                if (MsgTyeUtils.isSelfMsg(bean)) {
                                    return true;
                                }
                                MsgReCallUtil.notifyZanPerson(RobotContentProvider.getInstance(), bean, bean.getSenderuin(), finalCount);
                                return false;
                            }

                            @Override
                            public void onEnd(List<AtBean> atBeanList, String info) {

                                info = info + " \n?????????" + NickNameUtils.queryMatchNicknameAndNullReturnDefault(item.getFrienduin(), item.getSenderuin(), item.getNickname()) + "???????????????" + finalCount + "???????????????????????????!";
                                if (ConfigUtils.isDisableAtFunction(RobotContentProvider.getInstance())) {
                                    MsgReCallUtil.notifyJoinMsgNoJumpDisableAt(RobotContentProvider.getInstance(), info, item);
                                } else {

                                    MsgReCallUtil.notifyAtMsgJumpB(RobotContentProvider.getInstance(), info, atBeanList, item);
                                }

                            }

                            @Override
                            public void onFailEnd() {
                                MsgReCallUtil.smartReplyMsg("?????????????????????,Nobody!!!!????????????????????????!", isgroupMsg, nameBean, item);
                            }

                        });

                        return true;

                    }


                } else {

                    if (TextUtils.isEmpty(argZan)) {

                        if (isgroupMsg) {
                            qq = isSelf ? FloorUtils.getFloorQQ(item.getFrienduin(), 1) : item.getSenderuin();

                        } else {

                            if (isSelf) {
                                MsgReCallUtil.smartReplyMsg("????????????????????????", isgroupMsg, nameBean, item);
                                return true;
                            } else {
                                qq = item.getSenderuin();

                            }
                        }
                    } else if (RegexUtils.checkNoSignDigit(argZan)) {
                        int value = ParseUtils.parseInt(argZan);
                        if (value <= 20) {
                            count = value;
                            qq = item.getSenderuin();
                        } else {

                            if (!isManager && !isCurrentGroupAdmin) {
                                MsgReCallUtil.smartReplyMsg("?????????????????????????????????20??????,???????????????QQ", isgroupMsg, nameBean, item);
                                return true;
                            } else {

                                if (argZan.length() > 4) {
                                    qq = argZan;

                                    count = ParseUtils.parseInt(ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgSecond), 1);

                                } else {


                                    MsgReCallUtil.smartReplyMsg("???????????????20???!", isgroupMsg, nameBean, item);
                                    return true;
                                }

                            }


                        }


                    } else {

                        if ((isManager || isCurrentGroupAdmin) && isgroupMsg) {
                            Pair<Integer, Integer> pair = FloorUtils.parseMultiFloorData(argZan);
                            if (pair != null) {
                                List<MsgItem> floors = FloorUtils.getFloors(item.getFrienduin(), pair.first, pair.second);

                                if (floors == null) {
                                    MsgReCallUtil.smartReplyMsg("??????????????????!!", isgroupMsg, nameBean, item);
                                    return false;
                                } else {
                                    final int finalCount = ParseUtils.parseInt(ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgSecond), 1);
                                    FloorMultiUtils.doMultiFloorEachLogic(this, item, floors, item.getFrienduin(), new FloorMultiUtils.IMultiEachCallBack<MsgItem>() {
                                        @Override
                                        public boolean onEachDoAndisIgnore(MsgItem bean) {

                                            if (MsgTyeUtils.isSelfMsg(bean)) {
                                                return true;
                                            }
                                            MsgReCallUtil.notifyZanPerson(RobotContentProvider.getInstance(), bean, bean.getSenderuin(), finalCount);
                                            return false;
                                        }

                                        @Override
                                        public void onEnd(List<AtBean> atBeanList, String info) {


                                            info = info + " \n?????????" + NickNameUtils.queryMatchNicknameAndNullReturnDefault(item.getFrienduin(), item.getSenderuin(), item.getNickname()) +
                                                    "???????????????" + finalCount + "??????,??????????????????!";

                                            if (ConfigUtils.isDisableAtFunction(RobotContentProvider.getInstance())) {
                                                MsgReCallUtil.notifyJoinMsgNoJumpDisableAt(RobotContentProvider.getInstance(), info, item);
                                            } else {

                                                MsgReCallUtil.notifyAtMsgJumpB(RobotContentProvider.getInstance(), info, atBeanList, item);
                                            }

                                        }

                                        @Override
                                        public void onFailEnd() {
                                            MsgReCallUtil.smartReplyMsg("?????????????????????,Nobody!!!!????????????????????????!", isgroupMsg, nameBean, item);
                                        }
                                    });
                                    return true;

                                }
                            }
                        }
                        MsgReCallUtil.smartReplyMsg("?????????????????????????????????!!", isgroupMsg, nameBean, item);
                        return true;
                    }


                }

                if ((count < 0 || count > 20) && !isManager && !isCurrentGroupAdmin) {
                    count = 1;
                }


                boolean hasVote = false;
                String msg = "";
                if (RemoteService.isIsInit()) {
                    String s = RemoteService.addLike(count, new String[]{item.getSelfuin(), qq});
                    if (s != null) {

                        com.alibaba.fastjson.JSONObject object = JSON.parseObject(s);
                        int code = object.getIntValue("code");
                        msg = object.getString("msg");
                        if (code != 0) {
                            MsgReCallUtil.smartReplyMsg(" \n" + msg, isgroupMsg, nameBean, item);
                            return true;

                        } else {
                            msg = "{" + msg + "}";
                        }
                        hasVote = true;

                    } else {

                    }

                }

                if (!hasVote) {
                    MsgReCallUtil.notifyZanPerson(this, item.clone(), qq, count);

                }
                String s = NickNameUtils.queryMatchNickname(item.getFrienduin(), qq, false);
                if (qq.equals(item.getSenderuin())) {
                    MsgReCallUtil.smartReplyMsg(" ??????" + count + "??????!?????????!\n" + msg, isgroupMsg, nameBean, item);

                } else {
                    MsgReCallUtil.smartReplyMsg("???" + s + " ?????????" + count + "??????!\n" + msg, isgroupMsg, nameBean, item);

                }


            }

            return true;
            case CmdConfig.MODIFY_CARD_NAME:
            case CmdConfig.MODIFY_CARD_NAME1:
            case CmdConfig.MODIFY_CARD_NAME2:
            case CmdConfig.MODIFY_CARD_NAME3: {
                if (!nameBean.isAllowModifyCard()) {
                    LogUtil.writeLog(commend + "???????????????");


                    if (StringUtils.isEqualStr(beforeArg, commend)) {
                        MsgReCallUtil.smartReplyMsg(AppConstants.ACTION_OPERA_TIP + commend + AppConstants.FUNC_IS_DISABLE_TIP, isgroupMsg, nameBean, item);
                        return true;

                    }

                    return false;
                } else {

                }


                if (isgroupMsg && nameBean != null) {
                } else {
                    if (flag < INeedReplayLevel.INTERCEPT_ALL_HEIGHT) {//????????????????????????????????????????????????????????????????????????????????????true????????????
                        return true;
                    }

                    if (isNeedIgnoreNormalCommand(item, atPair, flag, true, isSelf, isgroupMsg, nameBean)) {
                        if (ConfigUtils.IsNeedAt(nameBean) && atPair.second.first == false) {//??????????????????????????? . ????????????2?????????????????????????????????

                            return false;
                        }
                    }
                }

                if (nameBean != null && nameBean.isNeedaite()) {
                    if (nameBean.isSelfcmdnotneedaite() || atPair.second.first) {
                        //????????????????????????????????????????????????????????????????????????????????????????????????
                    } else {


                        return true;
                    }
                }
                if (!isgroupMsg) {
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "??????????????????????????????!", item);
                    return true;
                }


                String modifyName = "";
                String mondifyQQ = "";
                String beforeNickname = "";

                String arg1Str = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                if (TextUtils.isEmpty(arg1Str)) {
                    MsgReCallUtil.smartReplyMsg("????????????[??????????????? |qq ???????????????]", isgroupMsg, nameBean, item);
                    return true;

                } else {


                    String secondArg = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgSecond);
                    if (TextUtils.isEmpty(secondArg) || atPair != null) {


                        if (atPair != null && atPair.second != null && atPair.second.second != null && atPair.second.second.size() > 0) {

                            if (!isManager) {
                                MsgReCallUtil.notifyJoinReplaceMsgJump(this, "????????????????????????QQ" + atPair.second.second.get(0).getAccount() + "?????????!", item);
                                return true;
                            } else {
                                if (atPair.second.second.size() > 1) {
                                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "???????????????????????????????????????!", item);
                                    return true;

                                } else {
                                    GroupAtBean groupAtBean = atPair.second.second.get(0);
                                    mondifyQQ = groupAtBean.getAccount();
                                    beforeNickname = groupAtBean.getNickname();
                                    modifyName = ParamParseUtil.mergeParameters(args, ParamParseUtil.sArgFirst);
                                }
                            }


                        } else {


                            beforeNickname = item.getNickname();
                            mondifyQQ = item.getSenderuin();
                            modifyName = arg1Str;


                        }

                    } else {
                        if (!isManager) {

                            MsgReCallUtil.notifyJoinReplaceMsgJump(this, "????????????????????????????????????!", item);
                            return true;
                        }
                        mondifyQQ = arg1Str;

                        if (!RegexUtils.checkDigit(mondifyQQ)) {

                            MsgReCallUtil.notifyJoinReplaceMsgJump(this, mondifyQQ + "?????????????????????", item);
                            return true;
                        } else if (FloorUtils.isFloorData(mondifyQQ)) {//???????????????
                            String floorQQ = FloorUtils.getFloorQQ(item.getFrienduin(), mondifyQQ);
                            if (floorQQ != null) {
                                mondifyQQ = floorQQ;

                            } else {
                                MsgReCallUtil.notifyJoinReplaceMsgJump(this, FloorUtils.getFloorInputDataInValidMsg(mondifyQQ), item);
                                return true;
                            }

                        }


                        modifyName = secondArg;
                        beforeNickname = NickNameUtils.queryMatchNickname(item.getFrienduin(), mondifyQQ);

                    }


                    if (!isManager && nameBean != null && nameBean.getGroupnickanmekeyword() != null) {

                        try {

                            boolean matches = item.getNickname().matches(nameBean.getGroupnickanmekeyword());
                        } catch (PatternSyntaxException e) {

                            MsgReCallUtil.notifyJoinReplaceMsgJump(this, "??????????????????????????????????????????!", item);
                            return true;
                        }
                    }

                    if (!item.getSenderuin().equals(mondifyQQ)) {
                        AdminBean accountMe = (AdminBean) AccountUtil.findAccount(mSuperManagers, item.getSenderuin(), false);
                        AdminBean accountHe = (AdminBean) AccountUtil.findAccount(mSuperManagers, mondifyQQ, false);

                        if (accountHe != null && accountMe != null) {

                            if (accountMe.getLevel() <= accountHe.getLevel()) {

                                MsgReCallUtil.notifyJoinReplaceMsgJump(this, "?????????????????????" + mondifyQQ + "?????????,?????????????????????????????????!", item);
                                return true;
                            }
                        }

                    }


                    MsgItem clone = item.clone();
                    clone.setSenderuin(mondifyQQ);


                    MsgReCallUtil.notifyRequestModifyName(this, clone, modifyName);
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, AppConstants.ACTION_OPERA_NAME + "???????????????\n" + "[??????]" + mondifyQQ + "\n[????????????]" + beforeNickname + "\n[????????????]" + modifyName, item);

                }


            }

            return true;

            case CmdConfig.LIST_QQ_IGNORES: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }

                if (!isManager) {
                    MsgReCallUtil.notifyNotManagerMsg(this, item);
                    return true;
                }
                StringBuffer sb = new StringBuffer();
                sb.append("???????????????QQ:\n");
                for (AccountBean mIgnoreQQ : mIgnoreQQs) {

                    sb.append(NickNameUtils.formatNickname(mIgnoreQQ.getAccount(), mIgnoreQQ.getAccount()) + ":");
                    sb.append((mIgnoreQQ.isDisable() ? "[??????]" : "[??????]") + "\n");
                }
                MsgReCallUtil.notifyHasDoWhileReply(this, "" + sb.toString(), item);
            }
            return true;
            case CmdConfig.IGNORE_TEMP_IGNORE_ME: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }
                String arg1RobotQQ = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                String qqSenderUin = getCurrentArgAndAfter(args, ParamParseUtil.sArgSecond);
                qqSenderUin = TextUtils.isEmpty(qqSenderUin) ? item.getSenderuin() : qqSenderUin;
                mTempIGgnoresManager = qqSenderUin;
                String nickname = item.getSenderuin().equals(qqSenderUin) ? item.getNickname() : qqSenderUin + "";

                if (TextUtils.isEmpty(arg1RobotQQ)) {
                    arg1RobotQQ = item.getSelfuin();
                }
                if (item.getSelfuin().equals(arg1RobotQQ) || item.getNickname().equals(arg1RobotQQ)) {
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "???????????????????????????" + nickname + "?????????,????????????" + CmdConfig.IGNORE_TEMP_IGNORE_ME_DISABLE, item);
                } else {
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "????????????????????????" + nickname + "?????????(????????????????????????????????????????????????????????????????????????),??????????????????????????????QQ????????????????????????QQ,????????????:" + CmdConfig.IGNORE_TEMP_IGNORE_ME + "" + item.getSelfuin() + " " + item.getSenderuin() + ",?????????????????????????????????????????????QQ???????????????", item);
                }
            }
            return true;
            case CmdConfig.CLEAR_TASK: {
                if (flag < INeedReplayLevel.ANY) {
                    return false;
                }
                if (!isManager) {
                    MsgReCallUtil.notifyNotManagerMsg(this, item);
                    return true;
                }
                String first = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);

                if (TextUtils.isEmpty(first)) {
                    int i = TaskUtils.clrearAllTask();
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "??????????????????????????????,??????:" + i, item);
                } else {
                    int count = TaskUtils.clrearTaskByTitleAndMatchAllKey(first);
                    if (count <= 0) {
                        count = TaskUtils.clrearTaskByTitleAndFetchCount(first);
                    }
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "???????????????" + first + "???QQ ?????? ???????????????????????????,????????????:" + count, item);
                }

            }

            return true;
            case CmdConfig.IGNORE_TEMP_IGNORE_ME_DISABLE: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }
                String temp = mTempIGgnoresManager;
                if (temp == null) {
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "?????????????????????????????????", item);
                } else {

                    mTempIGgnoresManager = null;
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "?????????????????????" + NickNameUtils.formatNickname(temp, temp) + "???????????????", item);
                }
            }
            break;
            case CmdConfig.IGNORE_PINGBI:


                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }
                if (isgroupMsg) {
                    if (args.length > 0) {
                        return false;
                    }
                    MemoryIGnoreConfig.addIgnoreGroupNo(item.getFrienduin(), item);
                    String msg = "???" + item.getFrienduin() + "????????? ????????????";
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);
                } else {
                    String account = args.length > 0 ? args[0] : item.getFrienduin();
                    if (RegexUtils.checkIsContainNumber(account)) {
                        return false;
                    }

                    MemoryIGnoreConfig.addIgnoreGroupNo(account, item);
                    String msg = "QQ_IGNORES" + account + "????????? ????????????";
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);
                }
                break;


            case CmdConfig.ADD_GAG: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }
                //           GagAccountBean object = new GagAccountBean(keyWord, duration, silence, action);
                String ask = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                String answer = getCurrentArgAndAfter(args, ParamParseUtil.sArgSecond);
                // return DBHelper.getGagKeyWord(AppContext.dbUtils).insert(accountBean);
                if (ask == null) {

                    String msg = "?????????????????????,?????????????????????????????????!";
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);

                } else if (answer == null) {


                    String msg = "?????????????????????,????????????????????????";
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);

                } else {

                    List<GagAccountBean> list = DBHelper.getGagKeyWord(AppContext.getDbUtils()).queryAllIsDesc(GagAccountBean.class, true, FieldCns.FIELD_ACCOUNT);
                    for (GagAccountBean accountBean : list) {
                        if (accountBean.getAccount().contains(ask)) {
                            String msg = "????????????|" + accountBean.getAccount();
                            MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);
                            return true;
                        }
                    }
                    long l = ParseUtils.parseGagStr2Secound(answer);
                    GagAccountBean object = new GagAccountBean(ask, l, false, GAGTYPE.GAG);
                    long result = DBHelper.getGagKeyWord(AppContext.dbUtils).insert(object);
                    String msg = "";
                    if (result > 0) {
                        object.setId((int) result);
                        doInsertNewGagBean(object);
                        msg = String.format(AppConstants.ACTION_OPERA_NAME + "????????????\n?????????:%s\n????????????:%s", ask, answer);
                    } else {
                        msg = AppConstants.ACTION_OPERA_NAME + "?????????,??????????????? inser effect count:" + result;
                    }
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);
                }

            }
            break;
            case CmdConfig.DEL_GAG: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }
                String ask = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                // return DBHelper.getGagKeyWord(AppContext.dbUtils).insert(accountBean);
                if (ask == null) {

                    String msg = "?????????????????????!";
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);

                } else {

                    List<GagAccountBean> list = DBHelper.getGagKeyWord(AppContext.getDbUtils()).queryAllIsDesc(GagAccountBean.class, true, FieldCns.FIELD_ACCOUNT);
                    ListIterator<GagAccountBean> gagAccountBeanListIterator = list.listIterator();
                    StringBuilder sb = new StringBuilder();
                    sb.append("??????" + ask + "|");
                    boolean find=false;
                    while (gagAccountBeanListIterator.hasNext()) {
                        GagAccountBean accountBean = gagAccountBeanListIterator.next();
                        if (accountBean.getAccount().contains(ask)) {
                            find=true;
                            if (accountBean.getAccount().equals(ask)) {

                                long result = DBHelper.getGagKeyWord(AppContext.dbUtils).deleteById(GagAccountBean.class, accountBean.getId());
                                String msg = "";
                                sb.append("?????????????????????,id:" + accountBean.getId()+",????????????:"+ DateUtils.getGagTime(accountBean.getDuration()));
                                if (result > 0) {
                                    gagAccountBeanListIterator.remove();
                                    msg = "??????";
                                } else {
                                    msg = "??????,status:" + result;
                                }
                                sb.append(msg);
                            } else {

                                String replace = accountBean.getAccount().replace(ClearUtil.wordSplit + ask, "");
                                replace = accountBean.getAccount().replace(ask + ClearUtil.wordSplit, "");
                                replace = accountBean.getAccount().replace(ClearUtil.wordSplit + ClearUtil.wordSplit, ClearUtil.wordSplit);
                                sb.append(",???????????????????????????????????????" + ask + ",id:" + accountBean.getId() +",????????????:"+ DateUtils.getGagTime(accountBean.getDuration())+",????????????????????????:" + replace);

                                accountBean.setAccount(replace);
                                String msg;
                                long result = DBHelper.getGagKeyWord(AppContext.dbUtils).update(accountBean);
                                if (result > 0) {
//                                    gagAccountBeanListIterator.remove();
                                    msg = "";
                                } else {
                                    msg = "???,????????????,status:" + result;
                                }
                                sb.append(msg);

                            }


                        }
                    }
                    if (!find) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "????????????????????????" + ask, item);

                    } else {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "" + sb.toString(), item);

                    }

                }

            }
            break;
            case CmdConfig.ADD_WORD_CMD: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }
                String ask = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                String answer = getCurrentArgAndAfter(args, ParamParseUtil.sArgSecond);
                if (ask == null) {

                    String msg = "??????????????????,??????????????????[???]?????????????????????????????????,?????????????????????????????????!";
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);

                } else if (answer == null) {


                    String msg = "??????????????????,??????????????????[???]?????????????????????????????????,?????????????????????????????????!";
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);

                } else {


                    ReplyWordBean bean = DBHelper.getKeyWordDBUtil(AppContext.getDbUtils()).queryByColumn(ReplyWordBean.class, FieldCns.ASK, ask);
                    if (bean == null) {

                        List<ReplyWordBean> wordBeans = DBHelper.getKeyWordDBUtil(AppContext.getDbUtils()).queryAllByFieldLike(ReplyWordBean.class, FieldCns.ASK, ask);

                        if (wordBeans != null) {
                            for (ReplyWordBean wordBeanLike : wordBeans) {

                                HashSet<String> strings = ClearUtil.word2HashSet(ClearUtil.wordSplit, wordBeanLike.getAsk());
                                if (strings != null) {
                                    boolean remove = strings.remove(ask);
                                    if (remove) {
                                        bean = wordBeanLike;
                                        break;
                                    }
                                }

                            }
                        }
                    }


                    String msg;
                    if (bean != null) {
                        msg = "?????????????????? id" + bean.getId() + "???[" + bean.getAsk() + "]\n?????????[" + bean.getAnswer() + "]\n??????????????????????????????,?????????????????????????????????????????????" + ClearUtil.wordSplit + "????????????";
                    } else {

                        bean = new ReplyWordBean(ask, answer);
                        long result = DBHelper.getKeyWordDBUtil(AppContext.getDbUtils()).insert(bean);
                        if (result > 0) {
                            bean.setId((int) result);
                            doInsertNewKeyBean(bean);
                            msg = String.format(AppConstants.ACTION_OPERA_NAME + "??????????????????\n???:%s\n???:%s", ask, answer);
                        } else {
                            msg = AppConstants.ACTION_OPERA_NAME + "??????????????????,??????????????? inser effect count:" + result;
                        }
                    }


                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);
                }

            }
            break;

            case CmdConfig.UPDATE_WORD_CMD: {

                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }
                String ask = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                String answer = getCurrentArgAndAfter(args, ParamParseUtil.sArgSecond);
                if (ask == null) {

                    String msg = "??????????????????,??????????????????[???]?????????????????????????????????";
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);

                } else if (answer == null) {


                    String msg = "??????????????????,??????????????????[???]?????????????????????????????????";


                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);

                } else {

                    ReplyWordBean bean = DBHelper.getKeyWordDBUtil(AppContext.getDbUtils()).queryByColumn(ReplyWordBean.class, FieldCns.ASK, ask);

                    String msg;
                    if (bean == null) {
                        msg = "?????????????????????,??????????????????????????????";
                    } else {

                        bean.setAnswer(answer);
                        long result = DBHelper.getKeyWordDBUtil(AppContext.getDbUtils()).update(bean);

                        if (result > 0) {
                            bean.setId((int) result);
                            initWordMap();
                            msg = String.format("??????????????????\n???:%s\n???:%s", ask, answer);
                        } else {
                            msg = "??????????????????,???????????????  effect count:" + result;
                        }
                    }


                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);
                }

            }
            break;

            case CmdConfig.FLOOR:
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }


                if (args.length == 0) {
                    if (isgroupMsg) {

                        MsgReCallUtil.notifyJoinReplaceMsgJump(this, FloorUtils.printFloorData(item.getFrienduin(), 20), item);
                    } else {
                        MsgReCallUtil.notifyJoinReplaceMsgJump(this, "????????????????????????", item);

                    }

                } else {
                    String group = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, FloorUtils.printFloorData(group, 20), item);
                }

                break;
            case CmdConfig.GAG:
            case CmdConfig.GAG1:
            case CmdConfig.BIZUI:
            case CmdConfig.GAG_SHUTUP: {


                boolean isCurrentGroupAdmin = false;
                if (isgroupMsg && nameBean != null) {
                    isCurrentGroupAdmin = isCurrentGroupAdminFromDb(nameBean, item.getSenderuin(), item.getFrienduin());
                    nameBean.setIsCurrentGroupAdmin(isCurrentGroupAdmin);

                }

                if (isNeedIgnoreXManagerCommand(item, atPair, flag, isManager, nameBean, true)) {
                    return true;
                }
    /*            String arg0 = getArgByArgArr(args, sArgFirst);
                String arg1 = getArgByArgArr(args, sArgSecond);
                String arg2 = getArgByArgArr(args, sArgSecond);*/


                if (isgroupMsg) {
                    return doGagFromGroupMsgCmd(item, isManager, args, atPair, nameBean);
                } else {
                    return doGagCmdPrivateMsgCmd(item, isManager, args);
                }


            }
            case CmdConfig.TASK: {


                if (isNeedIgnoreXManagerCommand(item, atPair, flag, isManager, nameBean)) {
                    return true;
                }


                if (args.length == 0) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????????????????????????? ??????????????????????????????????????????????????????\n??????:\n[?????? QQ 1?????? 5??????]??????5??????????????????qq 1??????\n [??? QQ 0 5??????]??????5???????????????????????????Q,?????????,\n[????????? QQ ????????? 5??????]??????5????????????????????????????????????QQ" +
                            "\n??????????????????[" + CmdConfig.CLEAR_TASK + " ??????ID]", item);
                } else {
                    String firstCmd = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);//????????????


                    if (firstCmd != null) {
                        if (CmdConfig.LIJI_EXECUTE.equals(firstCmd)) {

                            //immediate execution
                            if (TaskUtils.hasTask()) {
                                int i = TaskUtils.immediateExecute();
                                MsgReCallUtil.notifyHasDoWhileReply(this, "????????????,?????????" + i + "?????????", item);

                            } else {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "??????,??????????????????", item);

                            }
                            return true;
                        }
                    }
                    String sendOrGroup;
                    String senderuin;
                    String currentArg1;
                    String currentArg2;
                    boolean multiOpera = false;
                    if (isgroupMsg && atPair.first && atPair.second.second != null && atPair.second.second.size() > 0) {
                        sendOrGroup = item.getFrienduin();
                        senderuin = atPair.second.second.get(0).getSenderuin();
                        currentArg1 = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgSecond);// ??????
                        currentArg2 = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgThrid);
                        multiOpera = true;

                    } else {


                        sendOrGroup = isgroupMsg ? item.getFrienduin() : ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgSecond);
                        senderuin = ParamParseUtil.getArgByArgArr(args, isgroupMsg ? ParamParseUtil.sArgSecond : ParamParseUtil.sArgThrid);
                        currentArg1 = ParamParseUtil.getArgByArgArr(args, isgroupMsg ? ParamParseUtil.sArgThrid : ParamParseUtil.sArgFourth);// ??????
                        currentArg2 = ParamParseUtil.getArgByArgArr(args, isgroupMsg ? ParamParseUtil.sArgFourth : ParamParseUtil.sArgFifth);

                        if (isgroupMsg) {
                            if (FloorUtils.isFloorData(senderuin)) {
                                senderuin = FloorUtils.getFloorQQ(sendOrGroup, senderuin);
                            }
                        }


                    }


                    if (TextUtils.isEmpty(sendOrGroup)) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "??????,??????????????????,??????????????????", item);
                        return true;
                    }
                    if (TextUtils.isEmpty(senderuin)) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "??????,??????????????????,????????????QQ", item);
                        return true;
                    }
                    if (TextUtils.isEmpty(currentArg1)) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "??????,??????????????????,?????????????????? (?????? ?????????????????????,??????????????????????????????????????????????????????????????????????????????)", item);

                        return true;

                    }

                    if (TextUtils.isEmpty(currentArg2) && !firstCmd.equals(CmdConfig.SEND_MSG)) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "??????,??????????????????,??????????????????,??????????????????????????????????????????,????????????????????????????????????", item);

                        return true;

                    }

                    long exeucuteTimeSecond = ParseUtils.parseGagStr2Secound(currentArg2);
                    MsgItem clone = item.clone();

                    clone.setIstroop(senderuin.equals(sendOrGroup) ? 0 : 1);
                    clone.setFrienduin(sendOrGroup);
                    clone.setNickname(" ");
                    clone.setSenderuin(senderuin);
                    String cancelCmd = (senderuin + sendOrGroup + currentArg1 + currentArg2 + item.getIstroop()).hashCode() + "";
                    cancelCmd = cancelCmd.replace("-", "");

                    innergag:
                    switch (firstCmd) {
                        case CmdConfig.GAG:
                        case CmdConfig.GAG1:
                        case CmdConfig.GAG_SHUTUP: {
                            long gagTime = ParseUtils.parseGagStr2Secound(currentArg1);


                            if (gagTime < 0) {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "????????????????????????", item);
                            }


                            if (exeucuteTimeSecond < 60) {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "????????????????????????,???????????????" + exeucuteTimeSecond + "????????????", item);
                            }


                            if (multiOpera) {

                                TaskUtils.insertGagMultiTaskFromAtMsg(this, cancelCmd, clone, atPair.second.second, exeucuteTimeSecond * 1000, gagTime);

                                MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????" +
                                        "\n????????????:" + DateUtils.getGagTime(exeucuteTimeSecond) + "???" +
                                        (isgroupMsg ? "" : "\n?????????:" + sendOrGroup) +
                                        "\n??????:?????????????????????(??????:" + atPair.second.second.size() + ")" +
                                        "\n??????:??????[" + CmdConfig.CLEAR_TASK + cancelCmd + "]??????????????????", item);
                            } else {


                                TaskUtils.insertGagTask(this, cancelCmd, clone, exeucuteTimeSecond * 1000, gagTime);


                                MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????" +
                                        "\n????????????:" + DateUtils.getGagTime(exeucuteTimeSecond) + "???" +
                                        (isgroupMsg ? "" : "\n?????????:" + sendOrGroup) +
                                        "\n??????:??????QQ" + senderuin + " " + DateUtils.getGagTime(gagTime) + "  " +
                                        "\n??????:??????[" + CmdConfig.CLEAR_TASK + cancelCmd + "]??????????????????", item);

                            }


                        }

                        break innergag;


                        case CmdConfig.KICK:
                        case CmdConfig.KICK_1:
                        case CmdConfig.KICK_2: {


                            boolean forver = ParseUtils.parseBoolean(currentArg1);

                            TaskUtils.insertRedpacketKickTask(this, cancelCmd, clone, exeucuteTimeSecond * 1000, forver);

                            MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????" +
                                    "\n????????????:" + DateUtils.getGagTime(exeucuteTimeSecond) + "???" +
                                    "\n??????:??????" + sendOrGroup + "?????????" + senderuin + "" +
                                    "\n????????????:" + forver + "" +
                                    "\n??????:??????" + CmdConfig.CLEAR_TASK + cancelCmd + "??????????????????", item);


                        }
                        break innergag;
                        case CmdConfig.SEND_MSG: {
                            String messageContent = "";
                            if (senderuin != null) {
                                if (clone.getIstroop() == 1) {

                                    messageContent = currentArg1;
                                    clone.setIstroop(0);
                                    clone.setSenderuin(senderuin);
                                    clone.setFrienduin(senderuin);

                                } else {
                                    clone.setSenderuin(sendOrGroup);
                                    clone.setFrienduin(sendOrGroup);
                                    exeucuteTimeSecond = ParseUtils.parseGagStr2Secound(currentArg2);

//                                    senderuin=senderuin;
                                    messageContent = senderuin;

                                }


                            }


                            TaskUtils.insertSendMsgKickTask(this, cancelCmd, clone, exeucuteTimeSecond * 1000, messageContent);

                            if (clone.getIstroop() == 1) {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????????????????" +
                                        "\n????????????:\n" + DateUtils.getGagTime(exeucuteTimeSecond) + "???" +
                                        "\n??????:????????????" + messageContent + "??????" + sendOrGroup + "\n??????:??????" + CmdConfig.CLEAR_TASK + cancelCmd + "??????????????????", item);


                            } else {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????????????????" +
                                        "\n????????????:" +
                                        "\n" + DateUtils.getGagTime(exeucuteTimeSecond) + "???" +
                                        "\n??????:????????????" + messageContent + "?????????" + senderuin + "" +
                                        "\n??????:??????" + CmdConfig.CLEAR_TASK + cancelCmd + "??????????????????", item);

                            }


                        }
                        break innergag;
                        default:


                            if (isSelf && !RegexUtils.checkIsContainNumber(argStr)) {//??????????????????????????????????????????????????????????????????
                                return false;

                            }

                            MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????" + firstCmd + " ????????? ??????????????????????????????????????????", item);


                            return true;


                    }
                    return true;
                }
            }

            break;

            case CmdConfig.WEIGUI_: {

                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }


                if (!isgroupMsg) {
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "?????????????????????????????????", item);
                } else {


                    String childCmd = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                    if (TextUtils.isEmpty(childCmd)) {
                        MsgReCallUtil.notifyJoinReplaceMsgJump(this, "????????????????????? [?????? QQ] [?????? QQ|???|(????????????????????????)]", item);

                    } else {


                        GroupAtBean groupAtBean = ConfigUtils.fetchLastAtBean(item, atPair);
                        String qq = groupAtBean != null ? groupAtBean.getAccount() : ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgSecond);
                        child:
                        switch (childCmd) {

                            case "??????":

                                if (TextUtils.isEmpty(qq)) {
                                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "??????????????????", item);
                                } else {
                                    int violationCount = ViolationRecordUtil.getViolationCount(_dbUtils, item.getFrienduin(), qq);

                                    List<ViolationWordRecordBean> violationWordRecordBeans = null;
                                    if (violationCount >= 0) {
                                        violationWordRecordBeans = ViolationWordHistoryRecordUtil.queryRecord(_dbUtils, item.getFrienduin(), qq);
                                    }

                                    StringBuffer sb = new StringBuffer();
                                    if (groupAtBean != null) {

                                        sb.append("" + groupAtBean.getNickname());
                                    }
                                    sb.append(" QQ" + qq + "????????????:" + violationCount + "\n");
                                    if (violationWordRecordBeans != null) {
                                        sb.append("???????????????10?????????\n");

                                        int size = violationWordRecordBeans.size() > 10 ? 10 : violationWordRecordBeans.size();
                                        for (ViolationWordRecordBean bean : violationWordRecordBeans) {
                                            sb.append(DateUtils.getTime(bean.getTime()) + ":" + bean.getWord() + "\n");
                                        }

                                    }


                                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "" + sb.toString(), item);

                                }

                                break child;
                            case "??????"://IGNORE_INCLUDE
                                if (TextUtils.isEmpty(qq)) {

//                                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "?????????????????????", item);
                                    ViolationRecordUtil.clearAll(_dbUtils);
                                    ViolationWordHistoryRecordUtil.clearAll(_dbUtils);
                                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "?????????????????????????????? ????????????????????????", item);


                                } else if (qq.equals("???")) {
                                    int i = ViolationRecordUtil.resetViolationCount(_dbUtils, item.getFrienduin());
                                    int i1 = ViolationWordHistoryRecordUtil.clearRecord(_dbUtils, item.getFrienduin());
                                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "??????????????????????????????????????????" + (i > 0) + " ??????????????????????????????" + (i1 > 0) + ",????????????:" + i + ",????????????:" + i1, item);


                                } else {
                                    int i = ViolationRecordUtil.resetViolationCount(_dbUtils, item.getFrienduin(), qq);
                                    int i1 = ViolationWordHistoryRecordUtil.clearRecord(_dbUtils, item.getFrienduin(), qq);
                                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, "??????QQ ??????????????????????????????" + (i > 0) + " ??????????????????????????????" + (i1 > 0) + ",????????????:" + i + ",????????????:" + i1, item);


                                }


                                break child;
                            default://???????????????????????????


                                MsgReCallUtil.notifyJoinReplaceMsgJump(this, "?????????????????????[" + childCmd + "]?????????????????????????", item);

                                return true;
                        }
                    }


                }


            }


            break;
            case CmdConfig.PLUGIN_INFO: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return true;
                }


                StringBuffer sb = new StringBuffer();
                sb.append("?????????java????????????" + mPluginList.size() + "\n");
                for (int i = 0; i < mPluginList.size(); i++) {
                    IPluginHolder pluginModel = mPluginList.get(i);
                    PluginInterface pluginInterface = pluginModel.getPluginInterface();
                    sb.append("???" + (1 + i) + "???" + pluginInterface.getPluginName() + " " + pluginInterface.getVersionName() + "-" + pluginInterface.getVersionCode() + "\n");
                    sb.append("??????:" + pluginInterface.getAuthorName() + "\n????????????:" + pluginInterface.getBuildTime() + "\n????????????:" + pluginModel.getPath() + "\n????????????:" + (pluginModel.isDisable() ? "???" : "???"));
                    sb.append("\n");
                    sb.append("\n");

                }

                sb.append("?????????js????????????" + mPluginList.size() + "\n");
                for (int i = 0; i < mJSPluginList.size(); i++) {
                    IPluginHolder pluginModel = mJSPluginList.get(i);
                    PluginInterface pluginInterface = pluginModel.getPluginInterface();
                    sb.append("???" + (1 + i) + "???" + pluginInterface.getPluginName() + " " + pluginInterface.getVersionName() + "-" + pluginInterface.getVersionCode() + "\n");
                    sb.append("??????:" + pluginInterface.getAuthorName() + "\n????????????:" + pluginInterface.getBuildTime() + "\n????????????:" + pluginModel.getPath() + "\n????????????:" + (pluginModel.isDisable() ? "???" : "???"));
                    sb.append("\n");
                    sb.append("\n");

                }
                sb.append("?????????lua????????????" + mPluginList.size() + "\n");
                for (int i = 0; i < mLuaPluginList.size(); i++) {
                    IPluginHolder pluginModel = mLuaPluginList.get(i);
                    PluginInterface pluginInterface = pluginModel.getPluginInterface();
                    sb.append("???" + (1 + i) + "???" + pluginInterface.getPluginName() + " " + pluginInterface.getVersionName() + "-" + pluginInterface.getVersionCode() + "\n");
                    sb.append("??????:" + pluginInterface.getAuthorName() + "\n????????????:" + pluginInterface.getBuildTime() + "\n????????????:" + pluginModel.getPath() + "\n????????????:" + (pluginModel.isDisable() ? "???" : "???"));
                    sb.append("\n");
                    sb.append("\n");

                }
                MsgReCallUtil.notifyJoinReplaceMsgJump(this, sb.toString(), item);

            }
            break;
            case CmdConfig.AITE_CMD: {

                if (isgroupMsg && nameBean != null) {
                    boolean isCurrentGroupAdmin = isCurrentGroupAdminFromDb(nameBean, item.getSenderuin(), item.getFrienduin());
                    nameBean.setIsCurrentGroupAdmin(isCurrentGroupAdmin);

                }
                if (isNeedIgnoreXManagerCommand(item, atPair, flag, isManager, nameBean, true)) {
                    return true;
                }


                if (isgroupMsg) {
                    String group = item.getFrienduin();
                    if (doAtCmdByGroupMsg(item, isManager, args, group)) {
                        return true;
                    }


                } else if (MsgTyeUtils.isPrivateMsg(item)) {
                    String group = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                    if (TextUtils.isEmpty(group)) {
                        MsgReCallUtil.notifyJoinReplaceMsgJump(this, "??????????????????????????????????????????", item);
                        return true;


                    }
                    if (doAtCmdByPrivateMsg(item, isManager, args, group)) {
                        return true;
                    }


                }


            }
            break;
            case CmdConfig.CONFIG: {
                if (isNeedIgnoreXManagerCommand(item, atPair, flag, isManager, nameBean)) {
                    return true;
                }

                String argFirst = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                if (TextUtils.isEmpty(argFirst)) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????????????????[" + String.format("%s %s %s %s|%s %s %s %s %s %s %s %s %s %s %s %s %s  %s(?????????????????????????????????????????????????????????)",
                            CmdConfig.ChildCmd.CONFIG_ADD_VAR,
                            CmdConfig.ChildCmd.CONFIG_DELETE_VAR,
                            CmdConfig.ChildCmd.CONFIG_MODIFY_VAR,
                            CmdConfig.ChildCmd.CONFIG_MODIFY,
                            CmdConfig.ChildCmd.CONFIG_GROUP_INFO,
                            CmdConfig.ChildCmd.CONFIG_USER_CARD,
                            CmdConfig.FECTCH_MUSIC,
                            CmdConfig.ChildCmd.CONFIG_PRINT,
                            CmdConfig.ChildCmd.CONFIG_RELOAD,
                            CmdConfig.ChildCmd.CONFIG_SHOW,
                            CmdConfig.ChildCmd.CONFIG_CARD,
                            CmdConfig.ChildCmd.CONFIG_USER_CARD,
                            CmdConfig.ChildCmd.CONFIG_EXIT_DISCUSSION,
                            CmdConfig.ChildCmd.CONFIG_EXIT_GROUP,
                            CmdConfig.ChildCmd.CONFIG_CAST_URI_DECODE,
                            CmdConfig.ChildCmd.CONFIG_CAST_URI_DECODE,
                            CmdConfig.ChildCmd.CONFIG_WEB_ENCODE,
                            "\n" + CmdConfig.CONFIG + "" + CmdConfig.ChildCmd.CONFIG_RELOAD +
                                    "\n" + CmdConfig.CONFIG + "" + CmdConfig.ChildCmd.CONFIG_RESTART +
                                    "\n" + CmdConfig.CONFIG + "" + CmdConfig.ChildCmd.CONFIG_MODIFY +
                                    "\n" + CmdConfig.CONFIG + CmdConfig.ChildCmd.CONFIG_SHOW +
                                    "\n" + CmdConfig.CONFIG + CmdConfig.ChildCmd.CONFIG_EXIT_GROUP +
                                    "\n" + CmdConfig.CONFIG + CmdConfig.ChildCmd.CONFIG_EXIT_DISCUSSION +
                                    "\n" + CmdConfig.CONFIG + CmdConfig.ChildCmd.CONFIG_EXECUTE + " shell??????" +
                                    "\n" + CmdConfig.CONFIG + CmdConfig.ChildCmd.CONFIG_OPEN + "  schame???url" +
                                    "\n" + CmdConfig.CONFIG + CmdConfig.ChildCmd.CONFIG_SCREENCAP + "  ??????" +
                                    "\n" + CmdConfig.CONFIG + CmdConfig.ChildCmd.CONFIG_LAUNCHER_APP + "  ????????????" +
                                    CmdConfig.ChildCmd.CONFIG_SQL) + "????????????ui?????????????????????????????????????????????????????????????????????????????????????????????????????????,??????ui??????????????????????????????????????????????????????.]\n????????????\n" +
                            "\n" + CmdConfig.CONFIG + CmdConfig.ChildCmd.CONFIG_SQL + " select * from $????????? limit 0,1(??????????????????`" + CmdConfig.CONFIG + "SQL`" + ")" +
                            "", item);


                    return true;
                }

                String argSecond = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgSecond);
                switch (argFirst) {
                    case CmdConfig.ChildCmd.CONFIG_KILL:
                        android.os.Process.killProcess(Process.myPid());
                        return true;
                    case CmdConfig.ChildCmd.CONFIG_RESTART:
                        AppUtils.restartApp(AppContext.getContext());
                        return true;
                    case CmdConfig.FECTCH_MUSIC:
                        if (TextUtils.isEmpty(argSecond)) {

                            MsgReCallUtil.notifyHasDoWhileReply(this, String.format("???????????? %s%s??????,?????????????????????????????????", CmdConfig.CONFIG, CmdConfig.FECTCH_MUSIC), item);


                            return true;

                        }

                        String[] array = getResources().getStringArray(R.array.musicoptions);
                        for (int i = 0; i < array.length; i++) {

                            int currentNo = i + 1;

                            String engine = array[i];
                            if (argSecond.equals(currentNo + "") || argSecond.equals(engine)) {
                                musicType = i;
                                ConfigUtils.saveAppUIPerference(getProxyContext(), getResources().getString(R.string.key_base_robot_music_engine), musicType + "");
                                MsgReCallUtil.notifyHasDoWhileReply(this, String.format("?????????%s??????????????????,??????%s ????????? ???????????????", engine, CmdConfig.FECTCH_MUSIC), item);
                                return true;
                            }
                        }

                        MsgReCallUtil.notifyHasDoWhileReply(this, String.format("??????????????????????????????????????????????????????????????? ,?????????%d???????????????,???????????? %s%s?????????1-%d???????????????????????????,?????????????????????????????????", array.length, CmdConfig.CONFIG, CmdConfig.FECTCH_MUSIC, array.length), item);


                        break;
                    case CmdConfig.ChildCmd.CONFIG_USER_CARD:
                        if (!isAsPluginLoad() && !RemoteService.isIsInit()) {
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????????????????????????????????????????????????????????????????????????????", item);
                        } else {

                            if (isAsPluginLoad() && mHostControlApi == null) {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????Q++??????1.0.6????????????????????????", item);
                            } else {

                                String qq = ParamParseUtil.getArgByArgArr(args, 1);

                                if (TextUtils.isEmpty(qq)) {
                                    qq = item.getSenderuin();
                                }


                                if (!RegexUtils.checkIsContainNumber(qq)) {

                                    MsgReCallUtil.notifyHasDoWhileReply(this, "?????????qq", item);
                                    return true;
                                }


                                {


                                    StringBuilder sb = new StringBuilder();

                                    String nickname = NickNameUtils.queryNicknameFromHost(qq, item.getFrienduin(), 0);
                                    String robotnickname = NickNameUtils.queryNicknameFromHost(item.getSelfuin(), item.getFrienduin(), 0);

                                    if (isgroupMsg) {
                                        String groupnickname = NickNameUtils.queryNicknameFromHost(qq, item.getFrienduin(), 1);
                                        String groupnicknameRobot = NickNameUtils.queryNicknameFromHost(item.getSelfuin(), item.getFrienduin(), 1);
                                        sb.append("??????????????????:" + groupnickname + "\n");
                                        sb.append("???????????????:" + robotnickname + "\n");
                                        sb.append("?????????:" + groupnickname + "\n");
                                        sb.append("??????:" + nickname + "\n");


                                    } else {

                                        sb.append("???????????????:" + robotnickname + "\n");
                                        sb.append("??????:" + nickname + "\n");


                                    }


                                    Map map = null;
                                    try {
                                        map = RemoteService.getClientICallBack().queryClientData(ServiceExecCode.QUERY_USER_INFO, 1, false, qq, null, null);
                                        if (map != null && ParseUtils.parseInt(map.get("code")) == 0) {


                                            /*

                                                 map.put("bAvailVoteCnt", bAvailVoteCnt + "");
                map.put("bHaveVotedCnt", bHaveVotedCnt + "");
                map.put("strSpaceName", strSpaceName + "");
                                             */
                                            if (ParseUtils.parseInt(map.get("code")) == 0) {
                                                sb.append("QQ??????:" + map.get("level") + "\n");
                                                sb.append("??????:" + map.get("nickname") + "\n");
                                                sb.append("??????:" + map.get("dynamic") + "\n");
                                                sb.append("??????:" + map.get("strSpaceName") + "\n");
                                                sb.append("???????????????" + map.get("bHaveVotedCnt") + "??????\n");//Voted??????
                                                sb.append("??????????????????:" + map.get("bAvailVoteCnt") + "??????\n");//Voted??????
//                                                sb.append("????????????:" + map.get("remark") + "\n");

                                                String addressinfo = (String) map.get("addressinfo");
                                                if (!TextUtils.isEmpty(addressinfo)) {
                                                    sb.append("??????:" + addressinfo + "\n");

                                                }
                                                String province = (String) map.get("province");
                                                if (!TextUtils.isEmpty(province)) {
                                                    sb.append("???:" + province + "\n");

                                                }
                                                String city = (String) map.get("city");

                                                if (!TextUtils.isEmpty(province)) {
                                                    sb.append("???:" + city + "\n");

                                                }


                                                MsgReCallUtil.notifyHasDoWhileReply(this, sb.toString(), item);

                                            } else {

                                                MsgReCallUtil.notifyHasDoWhileReply(this, "???????????? " + map.get("msg"), item);
                                            }


                                        }


                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                    MsgReCallUtil.notifyHasDoWhileReply(this, sb.toString(), item);

                                }


                            }


                        }


                        return true;
                    case CmdConfig.ChildCmd.CONFIG_OPEN: {
                        String s1 = ParamParseUtil.mergeParameters(args, 1);
                        if (TextUtils.isEmpty(s1)) {

                            MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), "??????????????????????????????scheme??????url!", item);
                            return true;
                        }
                        try {
                            AppUtils.openWebView(AppUtils.getApplication(), s1);
                        } catch (Exception e) {
                            MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), "????????????!" + e.getMessage(), item);

                        }
                    }

                    return true;
                    case CmdConfig.ChildCmd.CONFIG_LAUNCHER_APP: {
                        String s1 = ParamParseUtil.mergeParameters(args, 1);
                        if (TextUtils.isEmpty(s1)) {

                            MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), "?????????????????????????????????!", item);
                            return true;
                        }
                        try {
                            AppUtils.lauchApp(AppUtils.getApplication(), s1);
                        } catch (Exception e) {
                            MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), "????????????!" + e.getMessage(), item);

                        }
                    }

                    return true;
                    case CmdConfig.ChildCmd.CONFIG_EXECUTE:
                        String s1 = ParamParseUtil.mergeParameters(args, 1);
                        if (TextUtils.isEmpty(s1)) {

                            MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), "???????????????????????????!", item);
                            return true;
                        }

                        new QssqTaskFix<String, String>(new QssqTaskFix.ICallBackImp<String, String>() {
                            @Override
                            public String onRunBackgroundThread(String[] params) {
                                final Pair<String, Exception> s = ShellUtil.executeAndFetchResultPair(params[0], new ICmdIntercept<String>() {
                                    @Override
                                    public boolean isNeedIntercept(String str) {
                                        MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), str, item);
                                        return false;
                                    }

                                    @Override
                                    public void onComplete(String name) {

                                    }
                                });

                                if (s.first != null) {
                                    return s.first;
                                } else if (s.second != null) {
                                    return "[????????????]" + s.second.getMessage();
                                }

                                return "????????????";

                            }

                            @Override
                            public void onRunFinish(String o) {


                                if (o != null) {
                                    MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), o, item);

                                }


                            }
                        }).execute(s1);

                        return true;
                    case CmdConfig.ChildCmd.CONFIG_QQINFO: {

                        if (!RemoteService.isIsInit()) {
                            MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????????????????????????????????????????", item);
                        } else {
                            String account = ParamParseUtil.getArgByArgArr(args, 1);
                            if (TextUtils.isEmpty(account)) {
                                account = item.getSenderuin();
                            }

                            Map map = RemoteService.queryQQCard(account);
                            if (map == null) {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????", item);
                            } else {

                                StringBuilder sb = new StringBuilder();
                                if (ParseUtils.parseInt(map.get("code")) == 0) {
                                    sb.append("??????:" + map.get("nickname")); //strQzoneHeader ?????? ??????????????????????????????
                                    sb.append("\n??????:" + map.get("level"));
                                    sb.append("\n??????:" + map.get("school"));
                                    sb.append("\n??????:" + map.get("phone"));
                                    sb.append("\n??????:" + map.get("remark"));
                                    sb.append("\n??????:" + map.get("dynamic"));
                                    sb.append("\n??????:" + map.get("addressinfo"));
                                    sb.append("\n???:" + map.get("province"));
                                    sb.append("\n???:" + map.get("city"));
                                    sb.append("\nAvailVoteCnt:" + map.get("bAvailVoteCnt"));
                                    sb.append("\n?????????:" + map.get("bHaveVotedCnt"));
                                    sb.append("\nSuperVIP:" + map.get("bSuperVipOpen"));
                                    sb.append("\n??????:" + map.get("strSpaceName"));
                                    MsgReCallUtil.notifyHasDoWhileReply(this, sb.toString(), item);

                                } else {

                                    MsgReCallUtil.notifyHasDoWhileReply(this, "???????????? " + map.get("msg"), item);
                                }


                            }


                        }


                    }
                    return true;
                    case CmdConfig.ChildCmd.CONFIG_GROUP_INFO:
                        if (!RemoteService.isIsInit()) {
                            MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????????????????????????????????????????", item);
                        } else {


                            String group = ParamParseUtil.getArgByArgArr(args, 1);

                            if (TextUtils.isEmpty(group)) {
                                if (isgroupMsg) {
                                    group = item.getFrienduin();
                                } else {
                                    MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????????????????", item);
                                    return true;
                                }

                            }


                            if (!RegexUtils.checkIsContainNumber(group)) {

                                MsgReCallUtil.notifyHasDoWhileReply(this, "????????????????????????", item);
                                return true;
                            }


                            Map map = RemoteService.queryGroupInfo(group);
                            if (map == null) {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "????????????,?????????????????????????????????????????????", item);
                            } else {

                                StringBuilder sb = new StringBuilder();
                                if (ParseUtils.parseInt(map.get("code")) == 0) {



                                        /*
                                          map.put("troopname", troopname + "");
                map.put("troopuin", troopuin + "");
                                         */
                                    sb.append("??????:" + map.get("troopname") + "\n");
                                    sb.append("??????QQ:" + map.get("troopowneruin") + "\n");
                                    sb.append("?????????:" + map.get("managers") + "\n");
                                    sb.append("?????????:" + map.get("groupclass") + "\n");
                                    sb.append("?????????:" + (map.get("troopintro") + "").replaceAll("\n", " ") + "\n");
                                    sb.append("??????????????????:" + map.get("membermaxnum") + "\n");
                                    sb.append("??????????????????:" + map.get("membernum") + "\n");
                                    sb.append("????????????:" + map.get("troopstartlevel") + "\n");
                                    sb.append("????????????:" + map.get("jsontroopproblem") + "\n");
                                    sb.append("????????????:" + map.get("joinTroopAnswer") + "\n");
                                    sb.append("???????????????:" + DateUtils.getTimeYmd(Long.parseLong(map.get("troopcreatetime") + "") * 1000l) + "\n");
                                    sb.append("?????????:" + (map.get("tags") + "").replaceAll("\n", " ") + "\n");
                                    sb.append("??????????????????:" + map.get("paymoney") + "\n");
                                    sb.append("?????????:" + map.get("groupLocation") + "\n");


                                        /*
                 map.put("jsontroopproblem", jsontroopproblem + "");
                map.put("joinTroopAnswer", joinTroopAnswer + "");
                map.put("troopname", troopname + "");
                map.put("managers", managers);
                map.put("hassettroopname", hassettroopname + "");
                map.put("hassettroophead", hassettroophead + "");
                map.put("troopowneruin", troopowneruin + "");//??????qq
                map.put("groupLocation", groupLocation + "");//?????????
                map.put("troopintro", figertroopmemo + "");//?????????
                map.put("membernum", wMemberNum + "");//?????????
                map.put("membermaxnum", wMemberMaxNum + "");//?????????
                map.put("paymoney", paymoney + "");//?????????
                map.put("troopuin", troopuin + "");
                map.put("tags", tags + "");
                map.put("groupclass", groupclass + "");
                map.put("troopstartlevel", startlevel + "");
                                         */

                                    MsgReCallUtil.notifyHasDoWhileReply(this, sb.toString(), item);

                                } else {

                                    MsgReCallUtil.notifyHasDoWhileReply(this, "???????????? " + map.get("msg"), item);
                                }


                            }


                        }


                        return true;
                    case CmdConfig.ChildCmd.CONFIG_CARD: {

                        doCardLogic(item, args, argSecond, 1);


                    }
                    return true;
                    case CmdConfig.ChildCmd.CONFIG_EXIT_DISCUSSION:
                        if (MsgTyeUtils.isGroupMsg(item)) {


                            {

                                String msgCurrent = ParamParseUtil.getArgByArgArr(args, 1);

                                MsgReCallUtil.notifyHasDoWhileReply(this, TextUtils.isEmpty(msgCurrent) ? " ???????????????,?????????!" : msgCurrent, item);

                                getHandler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        MsgReCallUtil.notifyRequestExitDiscussionJump(RobotContentProvider.this, item.clone(), item.getFrienduin());

                                    }
                                }, 5000);


                            }


                        } else {

                            MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????,????????????", item);

                        }
                        return true;
                    case CmdConfig.ChildCmd.CONFIG_EXIT_GROUP:
                        if (MsgTyeUtils.isGroupMsg(item)) {


                            {

                                String msgCurrent = ParamParseUtil.getArgByArgArr(args, 1);

                                MsgReCallUtil.notifyHasDoWhileReply(this, TextUtils.isEmpty(msgCurrent) ? " ????????????,?????????!" : msgCurrent, item);

                                getHandler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        MsgReCallUtil.notifyRequestExitGroupJump(RobotContentProvider.this, item.clone(), item.getFrienduin());

                                    }
                                }, 5000);


                            }


                        } else {

                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????,????????????", item);

                        }
                        return true;

                    case CmdConfig.ChildCmd.CONFIG_CAST_URI_DECODE: {
                        String second = ParamParseUtil.getArgByArgArr(args, 2);
                        String encode = second == null ? Charset.defaultCharset().name() : argSecond;
                        String value = second == null ? argSecond : second;

                        try {
                            String msg = URLDecoder.decode(value, encode);
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????:" + msg + ",??????:" + encode, item);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????? ??????????????????:" + encode + ",???????????????:" + value, item);

                        }

                        return true;
                    }

                    case CmdConfig.ChildCmd.CONFIG_CAST_URI_ENCODE: {
                        String second = ParamParseUtil.mergeParameters(args, 2);
                        String encode = second == null ? Charset.defaultCharset().name() : argSecond;
                        String value = second == null ? argSecond : second;
                        try {
                            String msg = URLEncoder.encode(value, encode);
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????:" + msg + ",??????:" + encode, item);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????? ??????????????????:" + encode + ",??????????????????:" + value, item);

                        }

                        return true;
                    }
                    case CmdConfig.ChildCmd.CONFIG_WEB_ENCODE: {
                        String second = ParamParseUtil.mergeParameters(args, 1);

                        try {
                            String msg = StringEscapeUtils.escapeHtml4(second);
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????:" + msg, item);
                        } catch (Exception e) {
                            e.printStackTrace();
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????? " + e.getMessage(), item);

                        }

                        return true;
                    }
                    case CmdConfig.ChildCmd.CONFIG_WEB_DECODE: {
                        String second = ParamParseUtil.mergeParameters(args, 1);

                        try {
                            String msg = StringEscapeUtils.unescapeHtml4(second);
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????:" + msg, item);
                        } catch (Exception e) {
                            e.printStackTrace();
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????? " + e.getMessage(), item);

                        }

                        return true;
                    }
                    case CmdConfig.ChildCmd.CONFIG_JSON_ENCODE: {
                        String second = ParamParseUtil.mergeParameters(args, 1);

                        try {
                            String msg = StringEscapeUtils.escapeJson(second);
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????:" + msg, item);
                        } catch (Exception e) {
                            e.printStackTrace();
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????? " + e.getMessage(), item);

                        }

                        return true;
                    }
                    case CmdConfig.ChildCmd.CONFIG_DELETE_VAR: {

                        String name = ParamParseUtil.getArgByArgArr(args, 1);

                        if (TextUtils.isEmpty(name)) {
                            MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????????????????", item);


                        } else {

                            int result = DBHelper.getVarTableUtil(_dbUtils).deleteByColumn(VarBean.class, "name", name);
                            MsgReCallUtil.notifyHasDoWhileReply(this, result > 0 ? "????????????,??????:" + result + "???" : "????????????,?????????????????????????????????!", item);

                        }

                    }


                    return true;
                    case CmdConfig.ChildCmd.CONFIG_MODIFY_VAR: {

//                        String name = ParamParseUtil.getArgByArgArr(args, 1);
                        String name = ParamParseUtil.mergeParameters(args, 1, args.length - 1);

                        if (TextUtils.isEmpty(name)) {
                            MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????????????????,???|??????", item);


                        } else {

                            String[] split = name.split("\\|");//ignore_include

                            if (split.length == 2) {


                                String varName = split[0];
                                String varValue = split[1];

                                List<VarBean> names = DBHelper.getVarTableUtil(_dbUtils).queryAllByField(VarBean.class, "name", varName);

                                if (names != null && names.size() > 0) {

                                    int succcount = 0;
                                    for (VarBean bean : names) {
                                        bean.setValue(varValue);


                                        long id = DBHelper.getVarTableUtil(_dbUtils).update(bean);
                                        if (id > 0) {
                                            succcount++;
                                        }
                                    }
                                    if (succcount > 0) {
                                        MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????!!\n?????????" + varName + "\n?????????:" + varValue + "\n????????????:" + succcount + "\n????????????:" + names.size(), item);

                                    } else {

                                        MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????!!\n?????????" + varName + "\n?????????" + varValue + "\n????????????:??????", item);
                                    }

                                } else {


                                    MsgReCallUtil.notifyHasDoWhileReply(this, "?????????" + varName + "?????????", item);
                                }


                            } else {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????,??????????????? ???????????????2???,???????????????" + split.length + "??? ??????????????????|?????????????????????:" + CmdConfig.CONFIG + " " + CmdConfig.ChildCmd.CONFIG_ADD_VAR + " ??????|???????????? ????????????" + CmdConfig.CONFIG + " " + CmdConfig.ChildCmd.CONFIG_PRINT + " $?????? ????????????????????????!", item);

                            }
                        }

                    }


                    return true;
                    case CmdConfig.ChildCmd.CONFIG_ADD_VAR: {

//                        String name = ParamParseUtil.getArgByArgArr(args, 1);
                        String name = ParamParseUtil.mergeParameters(args, 1, args.length - 1);

                        if (TextUtils.isEmpty(name)) {
                            MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????????????????,???|??????", item);


                        } else {

                            String[] split = name.split("\\|");//ignore_include

                            if (split.length == 2) {


                                String varName = split[0];
                                String varValue = split[1];

                                List<VarBean> names = DBHelper.getVarTableUtil(_dbUtils).queryAllByField(VarBean.class, "name", varName);

                                if (names != null && names.size() > 0) {
                                    MsgReCallUtil.notifyHasDoWhileReply(this, "?????????" + varName + "?????????!??????:" + names.size() + "???.", item);

                                } else {
                                    VarBean varBean = new VarBean();
                                    varBean.setName(varName);
                                    varBean.setValue(varValue);
                                    long count = DBHelper.getVarTableUtil(_dbUtils).insert(varBean);

                                    if (count > 0) {
                                        MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????!!\n?????????" + varName + "\n?????????:" + varValue + "\n?????????ID:" + count, item);

                                    } else {

                                        MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????!!\n?????????" + varName + "\n?????????" + varValue + "\n????????????:??????", item);
                                    }

                                }


                            } else {
                                MsgReCallUtil.notifyHasDoWhileReply(this, "??????????????????,??????????????? ???????????????2???,???????????????" + split.length + "??? ??????????????????|?????????????????????:" + CmdConfig.CONFIG + " " + CmdConfig.ChildCmd.CONFIG_ADD_VAR + " ??????|???????????? ????????????" + CmdConfig.CONFIG + " " + CmdConfig.ChildCmd.CONFIG_PRINT + " $?????? ????????????????????????!", item);

                            }
                        }

                    }


                    return true;
                    case CmdConfig.ChildCmd.CONFIG_PRINT:
                        String printContent = ParamParseUtil.mergeParameters(args, 1, args.length - 1);

                        if (TextUtils.isEmpty(printContent)) {
                            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????????????????,???????????????????????????????????????! \n???????????????:$????????? ??????$?????????(???????????????0,??????1) \n?????????a?????????????????? a(??????1=%s ??????2=%s)???????????????????????????" + CmdConfig.CONFIG + CmdConfig.ChildCmd.CONFIG_PRINT + " a(??????,??????)?????????????????? ??????1=?????? ??????2=?????? \n??????:%s????????????????????????????????????????????????.", item);
                        } else {
                            printContent = VarCastUtil.parseStr(item, _dbUtils, printContent);
                            MsgReCallUtil.notifyHasDoWhileReply(this, printContent, item);
                        }
                        return true;
                    case CmdConfig.ChildCmd.CONFIG_SQL:

                        if (TextUtils.isEmpty(argSecond)) {

                            MsgReCallUtil.notifyHasDoWhileReply(this, "sql????????????:\n????????????$g=?????? $u=QQ $s=???????????????QQ " +
                                            "\n???[?????????:groupconfig,luckmoney:???????????????,var:?????????,vr:????????????,vwr:??????????????????????????????,floor:????????????]" +
                                            "[" + DBHelper.getSuperManager(getDbUtils()).getTableName(AdminBean.class) + ":?????????," +
                                            "[" + DBHelper.getQQGroupManagerDBUtil(getDbUtils()).getTableName(TwoBean.class) + ":?????????," +
                                            "," + DBHelper.getKeyWordDBUtil(getDbUtils()).getTableName(ReplyWordBean.class) + ":?????????," +
                                            "," + DBHelper.getVarTableUtil(getDbUtils()).getTableName(VarBean.class) + ":?????????]" +
                                            "\n?????????????????????" +
                                            "\n" + CmdConfig.CONFIG + "SQL select * from groupconfig where account=" + "\"$g\"" +// ignore_include
                                            "\n???????????????" +
                                            "\n" + CmdConfig.CONFIG + "SQL select * from admin" +// ignore_include
                                            "\n??????????????????" +
                                            "\n" + CmdConfig.CONFIG + "SQL " + SQLCns.SQL_CONSTANT_DISENABLE_NET_WORK +

                                            "\n?????????????????????????????????" +
                                            "\n" + CmdConfig.CONFIG + "SQL update " + DBHelper.getGagKeyWord(getDbUtils()).getTableName(GagAccountBean.class) + " set duration=60" +
                                            "\n???????????????????????????" +
                                            "\n" + CmdConfig.CONFIG + "SQL update groupconfig set breaklogic=1" +

                                            "\n????????????" +
                                            "\n" + CmdConfig.CONFIG + "SQL delete from " + DBHelper.getKeyWordDBUtil(getDbUtils()).getTableName(ReplyWordBean.class) + " where answer like %??????% or ask like %??????%" +
                                            "\n????????????name" +
                                            "\n" + CmdConfig.CONFIG + "SQL insert into " + DBHelper.getVarTableUtil(getDbUtils()).getTableName(VarBean.class) + "(name,value) values('??????" + new Random().nextInt(500) + "','??????????????????')" +
                                            " \n?????????????????????,?????????????????????????????????\n" +
                                            "-width 10??????????????????????????????10" +
                                            "\n -fontlength ?????????????????????????????????????????????????????? " +
                                            "\n-format web ???????????????????????? ?????????????????????????????????????????????????????????????????????,??????????????????",
                                    "\n????????????????????????:" + CmdConfig.CONFIG + CmdConfig.ChildCmd.CONFIG_RELOAD,
                                    item);


                            return true;
                        }

                        try {
                            int maxSpaceWidth = 100;
                            int fontMaxWidth = 100;

                            int startPosition = 1;
                            boolean byNetPrint = false;
                            boolean findFormat = false;
                            boolean ignoreVar = false;

                            argloop:
                            for (int i = startPosition; i < args.length; i++) {

                                String current = args[i];
                                if (current.trim().equals("-width")) {
                                    String value = ParamParseUtil.getArgByArgArr(args, i + 1);
                                    maxSpaceWidth = ParseUtils.parseInt(value);
                                    startPosition = i + 2;
                                    findFormat = true;
                                } else if (current.trim().equals("-igvar")) {//??????
                                    String value = ParamParseUtil.getArgByArgArr(args, i + 1);
                                    ignoreVar = ParseUtils.parseBoolean(value);
                                    startPosition = i + 2;
                                    findFormat = true;
                                } else if (current.trim().equals("-fontlength")) {
                                    String value = ParamParseUtil.getArgByArgArr(args, i + 1);
                                    fontMaxWidth = ParseUtils.parseInt(value);
                                    startPosition = i + 2;
                                    findFormat = true;
                                } else if (current.trim().equals("-format")) {
                                    String value = ParamParseUtil.getArgByArgArr(args, i + 1);
                                    if ("web".equals(value)) {
                                        byNetPrint = true;

                                        if (maxSpaceWidth <= 10) {
                                            maxSpaceWidth = 50;
                                        }

                                        if (fontMaxWidth <= 8) {
                                            fontMaxWidth = 30;
                                        }
                                    }
                                    findFormat = true;

                                    startPosition = i + 2;//?????????????????????????????????????????????+2
                                }

                                if (i >= startPosition + 3) {//???????????????????????????+2 ?????????????????????????????? findFormat ????????????????????????
                                    break argloop;
                                }

                            }

                            if (byNetPrint) {

                            }

                            argSecond = ParamParseUtil.mergeParameters(args, startPosition, args.length - 1);

                            if (!ignoreVar) {
                                // ignore_start
                                argSecond = VarCastUtil.parseStr(item, _dbUtils, argSecond);

                                // ignore_end
                            }


                            if (argSecond.contains("select")) {

                                if (BuildConfig.DEBUG) {
                                    LogUtil.writeLog(TAG, "SQL:" + param);
                                }


                                final int finalMaxSpaceWidth = maxSpaceWidth;
                                final boolean finalByNetPrint = byNetPrint;
                                final int finalFontMaxWidth = fontMaxWidth;
                                final String finalArgSecond = argSecond;
                                new QssqTask<Object>(new QssqTask.ICallBack() {
                                    @Override
                                    public Object onRunBackgroundThread() {

                                        try {

                                            DBUtils.HashMapDBInfo info = AppContext.getDbUtils().queryAllSaveCollections(finalArgSecond);

                                            // ?????? ?????????
                                            StringBuffer sbMapHeader = new StringBuffer();
                                            if (finalByNetPrint) {
                                                sbMapHeader.append("<table border=\'1\' width=\'50%\' align=\"center\">");// ignore_include

                                                sbMapHeader.append("<caption>"
                                                        //ignore-end
                                                        + "????????????????????????????????????"
                                                        + "</caption>");
                                            }

                                            //????????????????????????map ???????????????buffer
                                            List<StringBuffer> lines = new ArrayList<>();//???stringbuffer????????????????????????????????????index?????????????????? ???????????????map. ??????????????????????????????????????????????????????


                                            int columnIndex = 0;


                                            HashMap<String, String> rowKeys = info.getMaxRow();//values?????????????????????????????????
                                            if (rowKeys != null) {


                                                rowloop:
                                                for (Map.Entry<String, String> entry : rowKeys.entrySet()) {

                                                    String key = entry.getKey();

                                                    //????????????
                                                    if (finalByNetPrint) {

                                                        if (columnIndex == 0) {
                                                            sbMapHeader.append("<tr>");//?????????
                                                        } else {

                                                        }


                                                        sbMapHeader.append("<th>");
                                                        sbMapHeader.append(key);//????????????

                                                        sbMapHeader.append("</th>");//????????????


                                                        if (columnIndex == info.getMaxRow().size() - 1) {//????????????

                                                            sbMapHeader.append("</tr>");
                                                        }

                                                    } else if (info.getList().size() <= 5 && info.getMaxRow().size() > 3) {
                                                        sbMapHeader.append("" + entry.getKey() + ":\n");
                                                        sbMapHeader.append("[");
                                                        boolean beforeHasContain = false;
                                                        for (int i = 0; i < info.getList().size(); i++) {
                                                            LinkedHashMap<String, String> map = info.getList().get(i);
                                                            String s = map.get(entry.getKey());
                                                            if (!TextUtils.isEmpty(s)) {
                                                                if (beforeHasContain) {
                                                                    sbMapHeader.append(",");
                                                                    beforeHasContain = true;
                                                                }
                                                                sbMapHeader.append(s);
                                                            }


                                                        }
                                                        sbMapHeader.append("]");
                                                        sbMapHeader.append("\n");

                                                        continue;

                                                    } else {

                                                        String format = String.format("%-" + finalMaxSpaceWidth + "s", ParseUtils.parseMaxLengStr(key, finalFontMaxWidth));

                                                        sbMapHeader.append(format);//????????????
                                                    }


                                                    int row = 0;
                                                    //????????????columnIndex??? ??????????????????????????? ??????????????????

                                                    for (HashMap<String, String> map : info.getList()) { //????????? ????????????????????????????????? ???????????????  ?????????????????????????????????????????????????????????stringbunffer?????????????????????????????????.
                                                        //??????????????? ???????????? ?????? ???????????????????????????????????????????????? ????????????  ??????????????????stringbuffer????????????

                                                        StringBuffer sbCurrent = null;
                                                        if (columnIndex == 0) {//????????????????????? ????????????????????????add

                                                            sbCurrent = new StringBuffer();

                                                            lines.add(sbCurrent);//?????????

                                                        } else {

                                                            sbCurrent = lines.get(row); //???2??? ???2??????????????????  ??????????????? ???2??????

                                                        }

                                                        String value = map.get(key);

                                                        if (finalByNetPrint) {
                                                            if (columnIndex == 0) {

                                                                sbCurrent.append("<tr>");
                                                            }
                                                            sbCurrent.append("<td>");
                                                            sbCurrent.append(value);
                                                            sbCurrent.append("</td>");
                                                            if (columnIndex == info.getMaxRow().size() - 1) {

                                                                sbCurrent.append("</tr>");
                                                            }


                                                        } else {

                                                            String valueFormat = String.format("%-" + finalMaxSpaceWidth + "s", ParseUtils.parseMaxLengStr(value, finalFontMaxWidth));
                                                            sbCurrent.append(valueFormat);

                                                        }

                                                        row++;
                                                        LogUtil.writeLog(TAG, "?????????" + columnIndex + "???" + row + "????????????:" + sbCurrent.toString());
                                                    }

                                                    columnIndex++;


                                                }

                                            }


                                            if (finalByNetPrint) {

//                                                sbMapHeader.append("<br/>");//??????????????????
                                            } else {

                                                sbMapHeader.append("\n");
                                            }


                                            for (StringBuffer line : lines) {


                                                if (finalByNetPrint) {

                                                    sbMapHeader.append(line);//???????????????br??????

//                                                    sbMapHeader.append("<br/>");//??????????????????
                                                } else {
                                                    sbMapHeader.append(line + "\n");

                                                }

                                            }


                                            if (finalByNetPrint) {

                                                sbMapHeader.append("</table>");

                                            }

                                            String s = sbMapHeader.toString();
                                            if (finalByNetPrint) {
//https://blog.csdn.net/frankcheng5143/article/details/52939082
                                                NetQuery netQuery = new NetQuery();
                                                String urlMy = Cns.DOMAIN + "/robot/sql.html?info=" + AppUtils.encodeUrl(EncryptPassUtil.encryption("" + s, "lozn.top_luozheng"));
                                                ;
//                                                String urlMy = Cns.DOMAIN + "/robot/sql.html?info=" + EncryptPassUtil.encryption(""+s, "lozn.top_luozheng");;
                                                //   String urlMy = Cns.DOMAIN + "/robot/sql.html?info=" + AppUtils.encodeUrl(s);
                                                String urlRequest = mShortUrlTextApiUrl = "" + urlMy;
                                                //   String urlRequest="http://suo.im/api.php?url=" + urlMy;

                                                if (TextUtils.isEmpty(mShortUrlTextApiUrl)) {
                                                    String result = netQuery.sendGet(urlRequest);

                                                    return "????????????,?????????" + result + "????????????";
                                                } else {
                                                    return "????????????,?????????" + urlMy + "????????????";
                                                }

                                            } else {

                                                return "????????????:\n" + s;
                                            }


                                        } catch (Exception e) {

                                            if (BuildConfig.DEBUG) {
                                                LogUtil.writeLog(TAG, "exception:" + Log.getStackTraceString(e));
                                            }
                                            return e;
                                        }

                                    }

                                    @Override
                                    public void onRunFinish(Object list) {
                                        if (list instanceof Exception) {
                                            if (BuildConfig.DEBUG) {
                                                LogUtil.writeLog(TAG, list.toString());
                                                MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.this, "" + ((Exception) list).getMessage() + ",?????????sql??????:" + finalArgSecond, item.clone());
                                            }
                                        } else {


                                            MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.this, "" + list, item);

                                        }
                                    }
                                }).execute();


                                // MsgReCallUtil.notifyHasDoWhileReply(this, "?????????????????????????????????", item);

                            } else {

                                _dbUtils.execSQL(argSecond);
                                MsgReCallUtil.notifyHasDoWhileReply(this, "????????????,??????????????????,???????????????sql: " + argSecond + " ??????????????????????????????,?????????????????????????????????", item);
                            }


                        } catch (Exception e) {

                            MsgReCallUtil.notifyHasDoWhileReply(this, "sql:" + argSecond + "\n????????????," + e.getMessage(), item);
                        }
                        return true;

                    case CmdConfig.ChildCmd.CONFIG_RELOAD:
                        initGroupSpConfig();
                        initBaseConfig();
                        initGroupWhiteNamesFromDb();
                        initIgnores();
                        initSuperManager();
                        initJAVAPlugin();
                        initJavascriptSPlugin();
                        initLuaPlugin();
                        NickNameUtils.clearFromMemory();
                        initGagWords();
                        MsgReCallUtil.notifyHasDoWhileReply(this, "????????????,?????????????????????????????????????????????????????????????????????????????????????????????????????????", item);


                        return true;
                    case CmdConfig.ChildCmd.CONFIG_VIEW_PIC: {

                        String arg = ParamParseUtil.mergeParameters(args, 1);
                        if (TextUtils.isEmpty(arg)) {
                            MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), "??????????????????????????????????????????", item);
                        } else {
                            if (new File(arg).exists()) {
                                MsgReCallUtil.notifySendPicMsg(RobotContentProvider.getInstance(), arg + "", item);

                            } else {
                                MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), "????????????" + arg + "?????????", item);

                            }

                        }
                        return true;
                    }
                    case CmdConfig.ChildCmd.CONFIG_SCREENCAP: {

                        String arg = ParamParseUtil.mergeParameters(args, 1);

                        new QssqTaskFix<String, String>(new QssqTaskFix.ICallBackImp<String, String>() {
                            @Override
                            public String onRunBackgroundThread(String[] params) {
                                String path = TextUtils.isEmpty(params[0] + "") ? "/sdcard/robot.jpg" : params[0] + "";
                                File picFile = new File(path);
                                if (picFile.exists()) {
                                    picFile.delete();
                                }
                                String cmd = "screencap -p " + path;
                                final Pair<String, Exception> s = ShellUtil.executeAndFetchResultPair(cmd, new ICmdIntercept<String>() {
                                    @Override
                                    public boolean isNeedIntercept(String str) {
                                        MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), str, item);
                                        return false;
                                    }

                                    @Override
                                    public void onComplete(String name) {
                                        if (picFile.exists() && "?????????".equals(name)) {
                                            MsgReCallUtil.notifySendPicMsg(RobotContentProvider.getInstance(), picFile.getAbsolutePath() + "", item);

                                        }

                                    }
                                });

                                if (s.first != null) {
                                    return s.first;
                                } else if (s.second != null) {
                                    return "[????????????]" + s.second.getMessage();
                                }

                                return "????????????";

                            }

                            @Override
                            public void onRunFinish(String o) {


                                if (o != null) {
                                    MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), o, item);

                                }


                            }
                        }).execute(arg);
                    }
                    return true;
//                        screencap -p /sdcard/screenshots/01.png
                    case CmdConfig.ChildCmd.CONFIG_MODIFY:
                        String argValue = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgThrid);
                        String argByArgArrType = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFourth);
                        Object writeValue = null;
                        if (TextUtils.isEmpty(argSecond) || TextUtils.isEmpty(argValue)) {

                            MsgReCallUtil.notifyHasDoWhileReply(this, "????????????2???????????? ??????????????????(??????boolean?????????true???false,?????????????????? ???????????????????????????????????????????????????????????????[string,strings(,??????),float,int,boolean,long],????????????????????????int ???????????????????????????int,?????????????????????????????????????????????`" + CmdConfig.ChildCmd.CONFIG_SHOW + "`??????)?????????????????????????????????????????????,????????????:" + argSecond, item);
                            return true;

                        }


//                            String argSecond = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgSecond);
                        if (argValue.equalsIgnoreCase("null")) {
                            argValue = "";
                        }
                        Object beforeObj = sharedPreferences.getAll().get(argSecond);
                        boolean containKey = beforeObj != null;
                        String type = "??????";
                        boolean result = false;
                        if (argValue.toLowerCase().equals("true") || argValue.toLowerCase().equals("false") || (argByArgArrType != null && argByArgArrType.equals("boolean"))) {
                            sharedPreferences.edit().apply();

                            writeValue = ParseUtils.parseBoolean(argValue);
                            result = sharedPreferences.edit().putBoolean(argSecond, (Boolean) writeValue).commit();
                            type = "boolean";

                        } else if (argByArgArrType != null && argByArgArrType.equals("int")) {
                            sharedPreferences.edit().apply();
                            writeValue = ParseUtils.parseInt(argValue);
                            result = sharedPreferences.edit().putInt(argSecond, (Integer) writeValue).commit();
                            type = "int";

                        } else if (argByArgArrType != null && argByArgArrType.toLowerCase().equals("strings")) {
                            sharedPreferences.edit().apply();
                            Set<String> set = new TreeSet();
                            String[] split = argValue != null ? argValue.split(",") : null;
                            if (split != null) {
                                for (String s : split) {
                                    set.add(s);
                                }

                            }

                            writeValue = set;
                            result = sharedPreferences.edit().putStringSet(argSecond, (Set<String>) writeValue).commit();
                            type = "strings";

                        } else if (argByArgArrType != null && argByArgArrType.equals("long")) {
                            sharedPreferences.edit().apply();
                            writeValue = ParseUtils.parseLong(argValue);
                            result = sharedPreferences.edit().putLong(argSecond, (Long) writeValue).commit();
                            type = "long";

                        } else if (argByArgArrType != null && argByArgArrType.equals("float")) {
                            sharedPreferences.edit().apply();
                            writeValue = ParseUtils.parseFloat(argValue);
                            result = sharedPreferences.edit().putFloat(argSecond, (Float) writeValue).commit();
                            type = "float";

                        } else {

                            if (containKey || argByArgArrType != null && argByArgArrType.toLowerCase().equals("string")) {//???????????????????????????
                                try {
                                    type = "string";

                                    writeValue = argValue;
                                    boolean writeConfig = writeConfig(beforeObj, argSecond, (String) writeValue);


                                    if (writeConfig) {


                                    } else {

                                        MsgReCallUtil.notifyHasDoWhileReply(this, "????????????\n???????????????:" + beforeObj.getClass() + ",????????????????????????????????????bug???????????????", item);
                                        return true;
                                    }

                                } catch (Exception e) {
                                    MsgReCallUtil.notifyHasDoWhileReply(this, "????????????\n??????????????????,???????????????" + e.getMessage() + "", item);

                                }


                            } else {

                                MsgReCallUtil.notifyJoinMsgNoJumpDisableAt(this, "???????????????" + argSecond + "?????????????????????,?????????????????????????????????????????????????????? string??????boolean????????????", item);
                                return true;
                            }
                        }

                        MsgReCallUtil.notifyHasDoWhileReply(this, "????????????\n??????????????????:" + containKey
                                + "\n????????????:" + result
                                + "\n?????? :" + argSecond
                                + "\n?????? :" + writeValue
                                + "\n????????????:" + type + "\n????????????:" + beforeObj, item);


                        return true;
                    case CmdConfig.ChildCmd.CONFIG_SHOW:
                        StringBuffer sb = new StringBuffer();


                        Set<? extends Map.Entry<String, ?>> set = sharedPreferences.getAll().entrySet();
                        sb.append("?????????" + set.size() + "?????????(??????????????????????????????)\n");
                        for (Map.Entry<String, ?> entry : set) {

                            if (!entry.getKey().startsWith("key_")) {
                                continue;
                            }
                            String typeCurrent = "";
                            if (entry.getValue() == null) {
                                typeCurrent = "??????";
                            } else {
                                typeCurrent = entry.getValue().getClass().getSimpleName();
                            }
                            sb.append(String.format("??????:%s\n??????:%s\n??????:%s\n==========\n", entry.getKey(), entry.getValue(), typeCurrent));
                        }

                        MsgReCallUtil.notifyHasDoWhileReply(this, "" + sb.toString(), item);

                        return true;
                }


                MsgReCallUtil.notifyHasDoWhileReply(this, "????????????,?????????????????????????????????????????????????????" + argFirst + ",?????????:" + argSecond, item);
            }

            return true;

            case CmdConfig.KICK:
            case CmdConfig.KICK_1:
            case CmdConfig.KICK_2: {
                if (atPair != null && atPair.first == false) {

                    String first = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                    if (!TextUtils.isEmpty(first) && !RegexUtils.checkIsContainNumber(first)) {
                        return false;
                    }
                }

                if (isgroupMsg && nameBean != null) {
                    boolean isCurrentGroupAdmin = isSelf || isCurrentGroupAdminFromDb(nameBean, item.getSenderuin(), item.getFrienduin());
                    nameBean.setIsCurrentGroupAdmin(isCurrentGroupAdmin);

                }
                if (isNeedIgnoreXManagerCommand(item, atPair, flag, isManager, nameBean, true)) {
                    return false;
                }
                if (isgroupMsg) {

                    doKickFromGroupMsgCmd(item, isManager, nameBean, args, atPair);
                } else {
                    doKickCmdPrivateMsgCmd(item, isManager, nameBean, args);
                }
                return true;

            }
            case CmdConfig.VOICE_CALL: {
                if (atPair != null && atPair.first == false) {

                    String first = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                    if (!TextUtils.isEmpty(first) && !RegexUtils.checkIsContainNumber(first)) {
                        return false;
                    }
                }

                if (isgroupMsg && nameBean != null) {
                    boolean isCurrentGroupAdmin = isSelf || isCurrentGroupAdminFromDb(nameBean, item.getSenderuin(), item.getFrienduin());
                    nameBean.setIsCurrentGroupAdmin(isCurrentGroupAdmin);

                }
                if (isNeedIgnoreXManagerCommand(item, atPair, flag, isManager, nameBean, true)) {
                    return false;
                }
                String qq = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                if (TextUtils.isEmpty(qq)) {

                    String msg = "?????????????????????????????????qq??????";
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);
                    return true;
                }
                if (!RegexUtils.iseQQ(qq)) {
                    String msg = "?????????qq??????";
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);
                    return true;
                }
                MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????" + qq + "??????????????????????????????!", item);
                MsgReCallUtil.notifySendVoiceCall(this, qq, item);

                return true;

            }

            case CmdConfig.DELETE_WORD_CMD:
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return false;
                }

                String ask = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                if (ask == null) {

                    String msg = "??????????????????,??????????????????[???]?????????????????????????????????";
                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);

                } else {
                    ReplyWordBean bean = DBHelper.getKeyWordDBUtil(AppContext.getDbUtils()).queryByColumn(ReplyWordBean.class, FieldCns.ASK, ask);
                    String msg;
                    if (bean == null) {
                        msg = "??????????????? ????????????";
                    } else {

                        int result = DBHelper.getKeyWordDBUtil(AppContext.getDbUtils()).deleteById(ReplyWordBean.class, bean.getId());
                        if (result > 0) {
                            bean.setId((int) result);
                            msg = String.format("??????????????????\n???:%s\n???:%s", ask, bean.getAnswer());
                            initWordMap();
                        } else {
                            msg = "????????????????????????,??????????????? inser effect count:" + result;
                        }
                    }

                    MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);
                }


                break;
            case CmdConfig.IGNORE_QUXIAO_PIGNBI:
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return false;
                }
                if (isgroupMsg) {
                    String account = args.length > 0 ? args[0] : item.getFrienduin();
                    MemoryIGnoreConfig.removeIgnoreGroupNo(account);
                    String msg = "???" + account + "??????????????? ????????????";
                    MsgReCallUtil.notifyJoinMsgNoJump(this, "" + msg, item);
                } else {
                    String account = args.length > 0 ? args[0] : item.getFrienduin();
                    MemoryIGnoreConfig.removeIgnorePerson(account);
                    String msg = "QQ_IGNORES" + account + "??????????????? ????????????";
                    MsgReCallUtil.notifyJoinMsgNoJump(this, "" + msg, item);
                }
                break;

            case CmdConfig.SHOW_JIANRONG: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return false;
                }

                String apptype = item.getApptype();
                StringBuffer sb = new StringBuffer();
                if (TextUtils.isEmpty(apptype)) {
                    sb.append("????????????????????????(??????????????????[???????????????????????????])\n");
                } else {
                    if (apptype.contains("plugin")) {
                        sb.append("????????????:??????");
                        int index = apptype.lastIndexOf("_");
                        boolean supportMusic = false;
                        int code = Integer.parseInt(apptype.substring(index + 1, apptype.length()));
                        if (code >= 70) {
                            supportMusic = true;
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");
                        } else if (code == 69) {
                            if (apptype.contains("1.5.2"))
                                sb.append("??????????????????:" + "??????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");

                        } else if (code >= 68) {

                            sb.append("??????????????????:" + "?????????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");

                        } else if (code == 56) {
                            sb.append("????????????:??????(bug)");
                            sb.append("\n");
                            sb.append("??????????????????:" + "?????????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");
                        } else {
                            sb.append("??????????????????:" + "?????????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");


                        }
                    } else if (apptype.contains("insert")) {

                        sb.append("????????????:??????");
                        sb.append("\n");
                        int index = apptype.lastIndexOf("_");
                        boolean supportMusic = false;
                        int code = Integer.parseInt(apptype.substring(index, apptype.length()));
                        if (code > 56) {
                            sb.append("??????????????????:" + "??????");
                            sb.append("\n");

                            sb.append("??????????????????:" + "??????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");
                        } else {

                            sb.append("??????????????????:" + "??????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");
                            sb.append("\n");
                            sb.append("??????????????????:" + "??????");

                        }

                        return true;


                    }
                }
                MsgReCallUtil.notifyJoinMsgNoJump(this, "" + sb.toString(), item);
                return true;
            }

            case CmdConfig.VERSION_UPDATE: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return false;
                }
                String argByArgArr = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                if (!TextUtils.isEmpty(argByArgArr)) {
                    return false;
                }
                StringBuffer sb = new StringBuffer();
                sb.append("?????????????????????:" + BuildConfig.VERSION_NAME + " build " + BuildConfig.VERSION_CODE + "\n");
                sb.append("????????????:" + UpdateLog.getLastLog() + "\n");
                MsgReCallUtil.notifyJoinMsgNoJump(this, "" + sb.toString(), item);
                break;
            }
            case CmdConfig.WIFI_ADB: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return false;
                }
                StringBuffer sb = new StringBuffer();
                String wifiIP = AppUtils.getWifiIP();
                sb.append("\nIP??????" + wifiIP);
                sb.append("\nWIFI???:" + AppUtils.getSSID() + "");
                sb.append("\n????????????:adb connect " + wifiIP + ":5555\n");
                long start = System.currentTimeMillis();
                new QssqTaskFix<StringBuffer, String>(new QssqTaskFix.ICallBackImp<StringBuffer, String>() {
                    @Override
                    public String onRunBackgroundThread(StringBuffer[] params) {
                        StringBuffer sb = params[0];
                        boolean[] waiting = {true, true};
                        Pair<String, Exception> stringExceptionPair = ShellUtil.executeAndFetchResultPair(new String[]{"echo before adb.tcp.port;getprop service.adb.tcp.port;setprop service.adb.tcp.port 5555;echo setport over;stop adbd;echo stop adb over;start adbd;echo start adb over!"}, new ICmdIntercept<String>() {
                            @Override
                            public boolean isNeedIntercept(String bean) {
                                sb.append(bean);
                                return false;
                            }

                            @Override
                            public void onComplete(String name) {
                                if (name != null && name.contains("??????")) {

                                    waiting[0] = false;
                                } else {
                                    waiting[1] = false;

                                }
                                long end = System.currentTimeMillis();

                                sb.append("[" + name + "]" + "??????[??????" + (end - start) + "ms]\n");

                            }
                        }, false);

                      /*  ShellUtil.executeAndFetchResultPair(new String[]{"setprop service.adb.tcp.port 5555"}, new ICmdIntercept<String>() {
                            @Override
                            public boolean isNeedIntercept(String bean) {
                                sb.append(bean);
                                return false;
                            }

                            @Override
                            public void onComplete() {
                                sb.append("????????????????????????\n");

                            }
                        }, false);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ShellUtil.executeAndFetchResultPair(new String[]{"start adbd"}, new ICmdIntercept<String>() {
                            @Override
                            public boolean isNeedIntercept(String bean) {
                                sb.append(bean);
                                return false;
                            }

                            @Override
                            public void onComplete() {
                                wait[0] = false;
                                sb.append("??????adb??????\n");

                            }
                        }, false);*/

                        try {
                            long waitTime = 0;
                            while ((waiting[0] || waiting[1]) && waitTime < 10000) {
                                waitTime += 10;
                                Thread.sleep(10);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sb.insert(0, stringExceptionPair.first);
                        return sb.toString();

                    }

                    @Override
                    public void onRunFinish(String o) {


                        if (o != null) {
                            item.setMessage(o);
                            MsgReCallUtil.notifyHasDoWhileReply(RobotContentProvider.getInstance(), o, item);

                        }


                    }
                }).execute(sb);


//                MsgReCallUtil.notifyJoinMsgNoJump(this, "" + sb.toString(), item);
                break;
            }
            case CmdConfig.STATE_INFO: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return false;
                }
                String argByArgArr = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                if (!TextUtils.isEmpty(argByArgArr)) {
                    return false;
                }
                StringBuffer sb = new StringBuffer();
                sb.append("?????????????????????:" + BuildConfig.VERSION_NAME + " build " + BuildConfig.VERSION_CODE + "\n");
                sb.append("????????????:" + BuildConfig.BUILD_TIME_STR + "\n");
                long distance = System.currentTimeMillis() - AppContext.getStartupTime();
                sb.append("????????????????????????:" + DateUtils.generateTimeDetail(distance) + "\n");
                sb.append("????????????:" + item.getApptype() + "\n");
                sb.append("?????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(isAsPluginLoad()) + "\n");
                sb.append("???????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(RemoteService.isIsInit()) + "\n");
                sb.append("????????????:" + RemoteService.getProcessName() + "\n");
                sb.append("??????????????????:" + Thread.currentThread().getName() + "\n");
                sb.append("???????????????:" + item.getVersion() + "\n");
                sb.append("IP??????" + AppUtils.getWifiIP() + "\n");
                sb.append("WIFI???:" + AppUtils.getSSID() + "\n");
                sb.append("??????:" + " " + Build.MODEL + "\nSDK_INT:" + Build.VERSION.SDK_INT);

                MsgReCallUtil.notifyJoinMsgNoJump(this, "" + sb.toString(), item);
                break;
            }
            case CmdConfig.VERSION:

                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return false;
                }
                String argByArgArr = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
                if (!TextUtils.isEmpty(argByArgArr)) {
                    return false;
                }

                StringBuffer sb = new StringBuffer();
                sb.append("(BWN)?????????????????????:" + BuildConfig.VERSION_NAME + " build " + BuildConfig.VERSION_CODE + "\n");
                sb.append("????????????:" + BuildConfig.BUILD_TIME_STR + "\n");
                long distance = System.currentTimeMillis() - AppContext.getStartupTime();
                sb.append("????????????????????????:" + DateUtils.generateTimeDetail(distance) + "\n");
                sb.append("????????????:" + item.getApptype() + "\n");
                sb.append("?????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(isAsPluginLoad()) + "\n");
                sb.append("???????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(RemoteService.isIsInit()) + "\n");
                sb.append("????????????:" + RemoteService.getProcessName() + "\n");
                sb.append("??????????????????:" + Thread.currentThread().getName() + "\n");
                sb.append("Q/TIM?????????:" + item.getVersion() + "\n");

                sb.append("?????????????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(mCfprivateReply) + "\n");
                sb.append("???????????????????????????????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(mCfprivateReplyManagrIgnoreRule) + "\n");
                sb.append("??????????????????????????????????????????????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(mCfBaseEnableNetRobotPrivate) + "\n");
                sb.append("??????????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(mCfBaseEnableLocalWord));
                sb.append("\n");
                sb.append("????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(mCfBaseDisableStructMsg));
                sb.append("\n");
                String[] stringArray = getResources().getStringArray(R.array.musicoptions);
                if (musicType <= stringArray.length - 1) {

                    sb.append("????????????:" + stringArray[musicType]);
                } else {
                    sb.append("?????????????????? :" + musicType + ",maxLength:" + stringArray.length);
                }
                sb.append("\n");
                sb.append("?????????????????????????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(mCfBaseWhiteNameReplyNotNeedAite));
                sb.append("\n");
/*                sb.append("??????????????????????????????????????????:" + mCfNotWhiteNameReplyIfAite);
                sb.append("\n");*/
/*                sb.append("?????????????????????????????????(??????):" + mCfBaseReplyShowNickName);
                sb.append("\n");*/
                sb.append("????????????????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(mCfeanbleGroupReply));
                sb.append("\n");
       /*         sb.append("??????????????????????????????????????????:" + mCfBaseEnableNetRobotGroup);
                sb.append("\n");*/
   /*             sb.append("??????????????????????????????:" + mCfBaseEnableNetRobotGroup);
                sb.append("\n");*/
                sb.append("?????????????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(mCfOnlyReplyWhiteNameGroup) + "\n");
/*                sb.append("??????????????????????????????:" + ConfigUtils.replyNeedAt(this));
                sb.append("\n");*/
//                sb.append("??????????????????????????????(??????/?????????/??????????????????):" + mCFBaseEnableCheckKeyWapGag + "\n");

                if (isgroupMsg) {

                    sb.append("??????????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(MemoryIGnoreConfig.isIgnoreGroupNo(item.getFrienduin())) + "\n");
                    //groupMsgLessSecondIgnore
                    sb.append("????????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(AccountUtil.isContainAccount(mQQGroupWhiteNames, item.getFrienduin(), true)) + "\n");

                    if (mCfOnlyReplyWhiteNameGroup) {
                        GroupWhiteNameBean bean = AccountUtil.findAccount(mQQGroupWhiteNames, item.getFrienduin(), false);
                        if (bean != null) {
                            sb.append("???????????????????????????\n" + bean.getConfig());
                            sb.append("???????????????????????????\n");

                        }
                    }
//                            mQQGroupWhiteNames.contains(item.getFrienduin()));
                } else if (MsgTyeUtils.isPrivateMsg(item)) {
                    sb.append("??????QQ?????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(MemoryIGnoreConfig.isTempIgnorePerson(item.getFrienduin())) + "\n");
                    sb.append("??????QQ?????????????????????:" + ParseUtils.parseBoolean2ChineseBooleanStr(AccountUtil.isContainAccount(mIgnoreQQs, item.getFrienduin(), true)) + "\n");
//                    sb.append("??????QQ?????????????????????:" + mIgnoreQQs.contains(item.getFrienduin()));
                }


                sb.append("??????????????????????????????:" + IGnoreConfig.distanceNetHistoryTimeIgnore + "\n");
                sb.append("?????????????????????:" + IGnoreConfig.distancedulicateCacheHistory + "\n");
                sb.append("???????????????????????????:" + IGnoreConfig.groupMsgLessSecondIgnore + "\n");
                sb.append("??????????????????????????????:" + IGnoreConfig.distanceStatupTimeIgnore + "\n");
                sb.append("\n");

           /*     IGnoreConfig.distanceNetHistoryTimeIgnore = sharedPreferences.getLong(getResources().getString(R.string.key_base_ignore_second_history_msg), IGnoreConfig.distanceNetHistoryTimeIgnore);
                IGnoreConfig.distancedulicateCacheHistory = sharedPreferences.getLong(getResources().getString(R.string.key_base_ignore_than_second_msg), getDefaultIntegerValue(R.integer.key_base_ignore_than_second_msg_duration));
                IGnoreConfig.distanceStatupTimeIgnore = sharedPreferences.getLong(getResources().getString(R.string.key_base_ignore_second_statup_time), getDefaultIntegerValue(R.integer.default_startup_time_distance_ms));

*/

                MsgReCallUtil.notifyJoinMsgNoJump(this, "" + sb.toString(), item);
                break;
            case CmdConfig.CLEAR_PINBI:
            case CmdConfig.CLEAR_PINBI_1: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return false;
                }
                int sizePserson = MemoryIGnoreConfig.getIgnorePersonMap().size();
                int sizeGroup = MemoryIGnoreConfig.getIgnoreGroupMap().size();
                MemoryIGnoreConfig.getIgnoreGroupMap().clear();
                MemoryIGnoreConfig.getIgnorePersonMap().clear();
                String msg = "???????????????,???" + sizeGroup + "???,QQ_IGNORES" + sizePserson + "???";
                MsgReCallUtil.notifyJoinMsgNoJump(this, "" + msg, item);
            }
            break;
            case CmdConfig.ADD_WHITE_NAMES:
            case CmdConfig.ADD_WHITE_NAMES_1:
            case CmdConfig.ADD_WHITE_NAMES_2:
            case CmdConfig.ADD_WHITE_NAMES_3: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return false;
                }
                if (MsgTyeUtils.isPrivateMsg(item)) {
                    {
                        if (args.length == 0) {
                            String msg = Cns.PRIVATE_MSG_MUST_INCLUDE_QQGROUP;
                            MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);
                            return false;
                        }
                    }
                }
                String account = args.length > 0 ? args[0] : item.getFrienduin();

                if (AccountUtil.isContainAccount(mQQGroupWhiteNames, account, false)) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "????????????" + account + "???????????????, ??????????????????!", item);
                    return false;
                } else {
                    GroupWhiteNameBean bean = (GroupWhiteNameBean) new GroupWhiteNameBean().setAccount(account);
                    long insert = DBHelper.getQQGroupWhiteNameDBUtil(_dbUtils).insert(bean);
                    mQQGroupWhiteNames.add(bean);
                    String msg = "???" + account + "?????????????????????,?????????????????????=" + ParseUtils.parseBoolean2ChineseBooleanStr((insert > 0));
                    MsgReCallUtil.notifyJoinMsgNoJump(this, "" + msg, item);

                }
            }
            break;
            case CmdConfig.REMOVE_WHITE_NAMES:
            case CmdConfig.REMOVE_WHITE_NAMES_1:
            case CmdConfig.REMOVE_WHITE_NAMES_2:
            case CmdConfig.REMOVE_WHITE_NAMES_3:
            case CmdConfig.REMOVE_WHITE_NAMES_4:
            case CmdConfig.REMOVE_WHITE_NAMES_5:
            case CmdConfig.REMOVE_WHITE_NAMES_6: {
                if (isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isgroupMsg, nameBean)) {
                    return false;
                }
                if (MsgTyeUtils.isPrivateMsg(item)) {
                    if (args.length == 0) {
                        String msg = Cns.PRIVATE_MSG_MUST_INCLUDE_QQGROUP;
                        MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);
                        return false;
                    }
                }
                String account = args.length > 0 ? args[0] : item.getFrienduin();

                boolean remove = AccountUtil.removeAccount(mQQGroupWhiteNames, account);
                String msg = null;
                if (remove) {

                    int i = DBHelper.getQQGroupWhiteNameDBUtil(AppContext.getDbUtils()).deleteByColumn(GroupWhiteNameBean.class, FieldCns.FIELD_ACCOUNT, account);
                    msg = "???" + account + "??????????????????????????????,????????????,??????:" + (i > 0);


                } else {
                    msg = "" + account + "????????????????????? ????????????";

                }
                MsgReCallUtil.notifyJoinMsgNoJump(this, "" + msg, item);

            }
            break;
            default:
                if (BuildConfig.DEBUG) {
                    LogUtil.writeLog("?????????????????????,?????????????????????");
                }
                return false;
        }

        return true;
    }

    private void doInsertNewGagBean(GagAccountBean object) {
        mGagKeyWords.add(object);
    }

    private boolean doCardLogic(MsgItem item, String[] args, String firstArg, int startPosition) {
        boolean notSafeCheck = false;
        if (TextUtils.isEmpty(firstArg)) {


        /*    String msg = String.format(CardHelper.demo, "???????????? ?????? xml??????",
                    "??????????????????",
                    "???????????? ?????? ????????? xml??????",
                    "????????????"
                    , "????????????"
                    , "???????????????");*/
            MsgReCallUtil.notifyHasDoWhileReply(this, "????????????????????????????????????xml????????????????????????????????????????????????????????????????????????????????????????????????????????? \n" + AppConstants.EXAMPLE_FORMAT, item);

            return true;
        } else if (firstArg.equals("??????")) {


       /*     MusicCardInfo cardInfo=new MusicCardInfo();
            cardInfo.setActionData("alipays://platformapi/startapp?saId=10000007&lientVersion=3.7.0.0718&qrcode=https://qr.alipay.com/c1x09104vwt0xobp1vmrr63");
            cardInfo.setAudioCover(Cns.DEFAULT_ROBOT_ICON);
            cardInfo.setDuration(3000);
            cardInfo.setAuthor("????????????");
            cardInfo.setMusictitle("????????????");
            cardInfo.setSharesource("?????????????????????");
            cardInfo.setActionData("http://fs.open.kugou.com/d9a0a78fb63f6bd82831395bf18f35bd/5b892e90/G052/M00/1A/15/1IYBAFaeC92AVl3UAEU5UA5cCgc155.mp3");
            cardInfo.setUrl("http://lozn.top");;
            cardInfo.setTitlebrief("??????");

            cardInfo.setExtraStr("notbody");*/
            String msg = String.format(CardHelper.demo, "?????????????????????", "http://lozn.top", "????????????/?????????", "????????????", "??????????????????", "???????????????");
            MsgReCallUtil.notifyHasDoWhileReply(this, "" + msg, item);

            return true;
        } else if (firstArg.equals("?????????")) {
            notSafeCheck = true;

        }


        String infos = ParamParseUtil.mergeParameters(args, notSafeCheck ? startPosition + 1 : startPosition);

        infos = VarCastUtil.parseStr(item, _dbUtils, infos);
        if (infos == null || infos.indexOf(">") == -1 || infos.indexOf("version") == -1 || infos.indexOf("<") == -1 || infos.indexOf("<?xml") == -1) {

            String tip = "";
            if (infos != null && infos.indexOf("$") != -1) {
                tip = "(?????????????????????????????????xml??????,????????????????????????:???" + infos + "??????????????????????????????)";
            }
            MsgReCallUtil.notifyHasDoWhileReply(this, "???????????????????????????xml?????????,\n" + AppConstants.EXAMPLE_FORMAT + "\n\n" + tip, item);
            return true;
        }

        if (!notSafeCheck) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            //????????????DocumentBuilder?????????
            //??????DocumentBuilder??????
            try {
                DocumentBuilder db = null;
                db = dbf.newDocumentBuilder();
                Document document = db.parse(new InputSource(new ByteArrayInputStream(infos.getBytes(), 0, infos.getBytes().length)));

                NodeList nodelist = document.getElementsByTagName("msg");
                if (nodelist == null) {
                    MsgReCallUtil.notifyHasDoWhileReply(this, "????????????msg??????", item);
                    return true;
                } else {
                    MsgReCallUtil.notifMusicCardJump(RobotContentProvider.this, item, infos);
                }

            } catch (Exception e) {
                String message = e.getMessage();

                MsgReCallUtil.notifyHasDoWhileReply(this, "????????????" + message
                        , item);


            }
        } else {
            MsgReCallUtil.notifMusicCardJump(RobotContentProvider.this, item, infos);


        }
        return false;
    }

    public boolean isNeedIgnoreManagerCommand(MsgItem item, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, Integer flag, boolean isManager, boolean isGroupMsg, GroupWhiteNameBean nameBean) {


        return isNeedIgnoreManagerCommand(item, atPair, flag, isManager, isGroupMsg, nameBean, false);
    }

    public boolean isNeedIgnoreManagerCommand(MsgItem
                                                      item, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, Integer
                                                      flag, boolean isManager, boolean isGroupMsg, GroupWhiteNameBean nameBean, boolean allowAt) {


        if (flag < INeedReplayLevel.ANY) {//????????????????????????????????????????????????????????????????????????????????????true????????????
            if (!isGroupMsg) {

                if (isManager) {
                    return false;//????????????????????????????????????
                } else {
                    return true;//??????????????????????????????????????????????????????
                }
            }
            if (atPair.first && atPair.second.first) {
            } else {

                return true;
            }

        }

        if (!isManager) {
            if (item != null) {

                boolean needAt = ConfigUtils.IsNeedAt(nameBean);
                if ((needAt && atPair.second.first) || !needAt) {//???????????????????????????????????????,?????????????????????????????????????????????,?????????????????? ????????????????????????????????????????????????
                    MsgReCallUtil.notifyNotManagerMsg(this, item);
                }
            }
            return true;
        } else {


            if (atPair.first && atPair.second.first == false) {//??????????????????????????????????????????????????? ?????????????????????
                if (allowAt) {
                    return false;
                }
                return true;
            } else {
                if (ConfigUtils.IsNeedAt(nameBean)) {

                    if (nameBean != null && nameBean.isSelfcmdnotneedaite() == false) {
                        if (!item.getSenderuin().equals(item.getSelfuin())) {//???????????????????????????????????????
                            return true;
                        }
                    }
                }
            }

        }

        return false;


    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param item
     * @param atPair
     * @param flag
     * @param isManager
     * @param selfMsg
     * @param isgroupMsg
     * @param nameBean
     * @return
     */

    public boolean isNeedIgnoreNormalCommand(MsgItem
                                                     item, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, Integer
                                                     flag, boolean isManager, boolean selfMsg, boolean isgroupMsg, GroupWhiteNameBean nameBean) {


        if (flag < INeedReplayLevel.INTERCEPT_ALL_HEIGHT) {//????????????????????????????????????????????????????????????????????????????????????true????????????
            if (!isgroupMsg) {//???????????????????????????
                return true;
            }
            if (atPair.first && atPair.second.first) {
                if (!isManager && !(nameBean != null && nameBean.isCurrentGroupAdmin())) {
                    return true;//????????????????????????????????????
                } else {
                }
            } else {

                return true;
            }

        } else {
            if (!isgroupMsg) {
                return false;//?????????????????????  ??????????????????????????????
            }
        }


        if (ConfigUtils.IsNeedAt(nameBean)) {
            if (atPair.second.first == false) {

                if (nameBean != null && nameBean.isSelfcmdnotneedaite() == false) {//???????????? ?????????????????????????????????
                    if (!selfMsg) {//???????????????????????????????????????
                        return true;
                    }
                }
            }

        }
        if (atPair.first && atPair.second.first == false) {//??????????????????????????????????????????????????? ?????????????????????

            if (selfMsg) {
                return false;
            }
            return true;
        } else {

            return false;
        }


    }

    public boolean isNeedIgnoreXManagerCommand(MsgItem
                                                       item, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, Integer
                                                       flag, boolean isManager, GroupWhiteNameBean nameBean) {

        return isNeedIgnoreXManagerCommand(item, atPair, flag, isManager, nameBean, false);

    }

    /**
     * ??????????????????????????????????????????
     *
     * @param item
     * @param atPair
     * @param flag
     * @param isManager
     * @param nameBean
     * @return
     */
    public boolean isNeedIgnoreXManagerCommand(MsgItem
                                                       item, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, Integer
                                                       flag, boolean isManager, GroupWhiteNameBean nameBean, boolean checkLocalManager) {

        if (flag < INeedReplayLevel.INTERCEPT_ALL_HEIGHT) {//????????????????????????????????????????????????????????????????????????????????????true????????????
            if (atPair.first && atPair.second.first) {
                if (!isManager && !(checkLocalManager && nameBean != null && nameBean.isCurrentGroupAdmin())) {
                    return true;//????????????????????????????????????
                } else {

                }
            } else {

                return true;
            }

        } else {
            if (!isManager && !(checkLocalManager && nameBean != null && nameBean.isCurrentGroupAdmin())) {
                if (item != null) {

                    boolean needAt = ConfigUtils.IsNeedAt(nameBean);
                    if ((needAt && atPair.second.first) || !needAt) {//???????????????????????????????????????,?????????????????????????????????????????????,?????????????????? ????????????????????????????????????????????????
                        MsgReCallUtil.notifyNotManagerMsg(this, item);
                    }
                }
                return true;
            } else {

            }
        }


        if (ConfigUtils.IsNeedAt(nameBean)) {
            if (atPair.second.first == false && nameBean != null && nameBean.isSelfcmdnotneedaite() == false) {

                //???????????? ??? ???????????????????????????????????????????????????????????????
                return true;
            }

        }
        return false;


    }


    private boolean doGagCmdPrivateMsgCmd(MsgItem item, boolean isManager, String[] args) {

        String group = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
        if (!verifyPrivateMsgGroupParam(group)) {
            return true;
        }

        return doGagCmd(item, isManager, args, group, ParamParseUtil.sArgSecond, ParamParseUtil.sArgThrid, false, null, null);
    }

    private boolean doGagFromGroupMsgCmd(MsgItem item, boolean isManager, String[]
            args, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, GroupWhiteNameBean
                                                 nameBean) {
        return doGagCmd(item, isManager, args, item.getFrienduin(), ParamParseUtil.sArgFirst, ParamParseUtil.sArgSecond, true, atPair, nameBean);
    }

    private boolean doKickCmdPrivateMsgCmd(MsgItem item, boolean isManager, GroupWhiteNameBean nameBean, String[] args) {

        String group = ParamParseUtil.getArgByArgArr(args, ParamParseUtil.sArgFirst);
        if (!verifyPrivateMsgGroupParam(group)) {
            return false;
        }

        return doKickCmd(item, isManager, nameBean, args, group, ParamParseUtil.sArgSecond, ParamParseUtil.sArgThrid, null);
    }

    private boolean verifyPrivateMsgGroupParam(String group) {
        if (TextUtils.isEmpty(group) || !RegexUtils.checkNoSignDigit(group)) {
            return false;
        }
        return true;
    }

    private boolean doKickFromGroupMsgCmd(MsgItem item, boolean isManager, GroupWhiteNameBean nameBean, String[]
            args, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair) {
        return doKickCmd(item, isManager, nameBean, args, item.getFrienduin(), ParamParseUtil.sArgFirst, ParamParseUtil.sArgSecond, atPair);

    }

    private boolean doKickCmd(MsgItem item, boolean isManager, GroupWhiteNameBean nameBean, String[] args, String group, int accountIndex,
                              int forverIndex, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair) {


        String account = ParamParseUtil.getArgByArgArr(args, accountIndex);
        if (args.length == 1) {


        }
        boolean forver = ParseUtils.parseBoolean(ParamParseUtil.getArgByArgArr(args, forverIndex));


        if (atPair != null && atPair.first) {//?????????????????????????????????????????????
            forver = ParseUtils.parseBoolean(account);
            boolean issucc = FloorMultiUtils.doMultiKickLogicByAt(RobotContentProvider.this, isManager, nameBean, item, atPair.second.second, group, forver);
            if (!issucc) {
                String nickname;
                if (ConfigUtils.isDisableAtFunction(this)) {
                    nickname = NickNameUtils.formatNicknameFromNickName(group, item.getNickname());
                } else {
                    nickname = item.getNickname();
                }


                if (atPair.second.second.size() == 1) {
                    forver = ParseUtils.parseBoolean(ParamParseUtil.getArgByArgArr(args, forverIndex));
                    account = atPair.second.second.get(0).getAccount();
                } else {
                    MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), nickname, "????????????,??????????????????????????????", item);
                    return true;

                }
            } else {
                return true;
            }

        } else if (FloorUtils.isFloorData(account)) {//???????????????
            String floorQQ = FloorUtils.getFloorQQ(group, account);
            if (floorQQ != null) {
                account = floorQQ;

            } else {
                MsgReCallUtil.notifyJoinReplaceMsgJump(this, FloorUtils.getFloorInputDataInValidMsg(account), item);
                return false;
            }

        } else {


            Pair<Integer, Integer> pair = FloorUtils.parseMultiFloorData(account);
            if (pair != null) {
                List<MsgItem> floors = FloorUtils.getFloors(group, pair.first, pair.second);

                FloorMultiUtils.doMultiKickLogic(RobotContentProvider.this, isManager, nameBean, item, floors, group, forver);
                return false;
            } else {

                if (!RegexUtils.checkNoSignDigit(account)) {//???????????????.
                    return false;
                }
            }
        }

        if (TextUtils.isEmpty(account)) {
            MsgReCallUtil.notifyJoinReplaceMsgJump(this, AppConstants.ACTION_OPERA_NAME_FORBID + "????????????" + account + ",????????????????????????!", item);
            return true;
        }


        if (!item.getSenderuin().equals(account)) {

            AdminBean accountMe = (AdminBean) AccountUtil.findAccount(mSuperManagers, item.getSenderuin(), false);
            AdminBean accountHe = (AdminBean) AccountUtil.findAccount(mSuperManagers, account, false);

            if (accountHe != null && accountMe != null) {

                if (accountMe.getLevel() <= accountHe.getLevel()) {
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, AppConstants.ACTION_OPERA_NAME_FORBID + "????????????" + account + ",??????????????????????????????????????????!", item);
                    return true;
                }
            } else if (accountMe == null) {


                if (accountHe != null && isManager == false) {
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, AppConstants.ACTION_OPERA_NAME_FORBID + "????????????" + account + ",???????????????????????????,??????????????????????????????!", item);
                    return true;
                } else if (isCurrentGroupAdminFromDb(nameBean, account, group)) {
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, AppConstants.ACTION_OPERA_NAME_FORBID + "????????????" + account + ",???????????????????????????????????????!", item);
                    return true;

                }


            }

        }


        MsgItem kickItem = item.clone();
        kickItem.setIstroop(1);
        kickItem.setSenderuin(account + "");
        kickItem.setFrienduin(group);
        kickItem.setMessage(forver + "");
        kickItem.setNickname("" + account);//???????????????,
        MsgReCallUtil.notifyKickPersonMsgNoJump(this, kickItem, forver);
        kickItem.setNickname(NickNameUtils.formatNickname(kickItem));//??????????????????
        String nickname;//??????????????????????????????
        if (ConfigUtils.isDisableAtFunction(this)) {
            nickname = NickNameUtils.formatNicknameFromNickName(account, kickItem.getNickname());
        } else {
            nickname = kickItem.getNickname();
        }
        MsgReCallUtil.notifyJoinReplaceMsgJump(this, "?????????????????????:" + nickname + ",????????????:" + kickItem.getMessage(), item);
        return true;
    }

    public boolean isCurrentGroupAdminFromDb(GroupWhiteNameBean nameBean, String qq, String group) {
        return isCurrentGroupAdminFromDb(nameBean, qq, group, false);
    }

    public boolean isCurrentGroupAdminFromDb(GroupWhiteNameBean nameBean, String qq, String group, boolean checkService) {
        if (nameBean == null) {

            return false;
        }
        if (qq == null) {
            return false;
        }
        String admins = nameBean.getAdmins();
        if (admins != null && nameBean.getAccount().equals(group)) {
            LogUtil.writeLog("??????????????????????????????:" + admins);
            String[] split = admins.split(",");
            for (String currentQQ : split) {
                if (qq.equals(currentQQ)) {
                    return true;
                }
            }
        }
        GroupAdaminBean groupAdaminBean = DBHelper.getGroupAdminTableUtil(_dbUtils).queryByColumn(GroupAdaminBean.class, FieldCns.FIELD_ACCOUNT, qq);

        if (groupAdaminBean != null) {
            String groups = groupAdaminBean.getGroups();
            LogUtil.writeLog("??????QQ??????????????????:" + groups);
            if (!TextUtils.isEmpty(groups)) {
                String[] split = groups.split(",");
                for (String currentGroup : split) {
                    if (currentGroup.equals(group)) {
                        return true;
                    }
                }
            }
        }


        if (checkService && RemoteService.isIsInit()) {//???????????????????????????????????????..
            String administrator = RemoteService.queryGroupField(group, "Administrator");
            if (!TextUtils.isEmpty(administrator)) {
                LogUtil.writeLog("?????????????????????:" + administrator);
                String[] split = administrator.split("\\|");//ignore_include
                for (String currentAdmin : split) {

                    if (currentAdmin.equals(qq)) {
                        return true;
                    }

                }
            }
            String qunzhu = RemoteService.queryGroupField(group, "troopowneruin");
            if (!TextUtils.isEmpty(qunzhu) && qunzhu.equals(qq)) {

                return true;
            }
        }


        return false;
    }


    private boolean doGagCmd(MsgItem item, boolean isManager, String[] args, String group, int accountIndex,
                             int gagTimeIndex, boolean groupMsg, androidx.core.util.Pair<Boolean, androidx.core.util.Pair<Boolean, List<GroupAtBean>>> atPair, GroupWhiteNameBean
                                     nameBean) {


        String paramStr1 = ParamParseUtil.getArgByArgArr(args, accountIndex);


        //????????????bug
        if (groupMsg && item.getSelfuin().equals(item.getSenderuin()) && args.length >= 1 && paramStr1 != null && paramStr1.length() > 3 && !RegexUtils.checkIsContainNumber(paramStr1)) {

            return false;

        }

        if (paramStr1 != null && isManager) {//???????????????????????????????????????
            paramStr1 = paramStr1.replace("??????", AppConstants.ALL_PERSON_FLAG);
            paramStr1 = paramStr1.replace("?????????", AppConstants.ALL_PERSON_FLAG);


        }


        String paramStr2 = ParamParseUtil.getArgByArgArr(args, gagTimeIndex);

        if (paramStr1 != null && paramStr1.equals(AppConstants.ALL_PERSON_FLAG)) {
            if (TextUtils.isEmpty(paramStr2)) {
                paramStr2 = "153016267???";
            }
        }
        String account = paramStr1;

        long gagTime;

        if (atPair != null && atPair.first) {//?????????????????????????????????????????????
            if (TextUtils.isEmpty(paramStr1) && nameBean != null) {
                gagTime = formatGagTime(nameBean.getNotparamgagminute() + "??????");

            } else {

                gagTime = formatGagTime(paramStr1);
            }

            boolean issucc = FloorMultiUtils.doMultiAtGagLogic(RobotContentProvider.this, isManager, nameBean, item, atPair.second.second, group, gagTime);
            if (!issucc) {
                String nickname;
                if (ConfigUtils.isDisableAtFunction(this)) {
                    nickname = NickNameUtils.formatNicknameFromNickName(group, item.getNickname());
                } else {
                    nickname = item.getNickname();
                }
                if (atPair.second.second.size() == 1) {

                    account = atPair.second.second.get(0).getAccount();
                    if (TextUtils.isEmpty(paramStr1) && nameBean != null) {
                        gagTime = formatGagTime(nameBean.getNotparamgagminute() + "??????");

                    } else {
                        gagTime = formatGagTime(paramStr1);

                    }
                } else {
                    MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), nickname, "????????????,?????????????????????????????????", item);
                    return true;

                }
            } else {
                return true;
            }


        } else if (FloorUtils.isFloorData(account)) {//???????????????
            if (TextUtils.isEmpty(paramStr2) && nameBean != null) {
                gagTime = formatGagTime(nameBean.getNotparamgagminute() + "??????");

            } else {
                gagTime = formatGagTime(paramStr2);

            }
            String floorQQ = FloorUtils.getFloorQQ(group, account);
            if (floorQQ != null) {
                account = floorQQ;

            } else {
                MsgReCallUtil.notifyJoinReplaceMsgJump(this, FloorUtils.getFloorInputDataInValidMsg(account), item);
                return true;
            }

        } else {

            Pair<Integer, Integer> pair = FloorUtils.parseMultiFloorData(account);
            if (pair != null) {//?????????????????????

                if (TextUtils.isEmpty(paramStr2) && nameBean != null) {
                    gagTime = formatGagTime(nameBean.getNotparamgagminute() + "??????");

                } else {

                    gagTime = formatGagTime(paramStr2);
                }
                List<MsgItem> floors = FloorUtils.getFloors(group, pair.first, pair.second);

                boolean b = FloorMultiUtils.doMultiGagLogic(RobotContentProvider.this, isManager, nameBean, item, floors, group, gagTime);
                if (!b) {
                    String nickname;
                    if (ConfigUtils.isDisableAtFunction(this)) {
                        nickname = NickNameUtils.formatNicknameFromNickName(group, item.getNickname());
                    } else {
                        nickname = item.getNickname();
                    }
                    MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), nickname, "????????????,??????????????????????????????????????????????????????", item);


                }

                return true;
            } else {
                //?????????????????????????????????????????? ????????????

                if (!RegexUtils.checkNoSignDigit(account)) {//???????????????.??????????????????????????????
                    String arg = account;
                    gagTime = ParseUtils.parseGagStr2Secound(arg);
                    account = FloorUtils.getFloorQQ(group);
                    if (TextUtils.isEmpty(account)) {
                        MsgReCallUtil.notifyHasDoWhileReply(this, "????????????,?????????????????????,?????????????????????", item);
                        return true;
                    }
                } else {//????????????qq

                    if (TextUtils.isEmpty(paramStr2) && nameBean != null) {
                        gagTime = formatGagTime(nameBean.getNotparamgagminute() + "??????");

                    } else {

                        gagTime = formatGagTime(paramStr2);
                    }
                }

            }
        }


        if (!item.getSenderuin().equals(account)) {
            AdminBean accountMe = (AdminBean) AccountUtil.findAccount(mSuperManagers, item.getSenderuin(), false);
            AdminBean accountHe = (AdminBean) AccountUtil.findAccount(mSuperManagers, account, false);

            if (accountHe != null && accountMe != null) {

                if (accountMe.getLevel() <= accountHe.getLevel()) {
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, AppConstants.ACTION_OPERA_NAME_FORBID + "????????????" + account + ",??????????????????????????????????????????!", item);
                    return true;
                }
            } else if (accountMe == null) {
                if (accountHe != null) {
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, AppConstants.ACTION_OPERA_NAME_FORBID + "????????????" + account + ",?????????????????????????????????????????????????????????!", item);
                    return true;

                } else if (isCurrentGroupAdminFromDb(nameBean, account, group)) {
                    MsgReCallUtil.notifyJoinReplaceMsgJump(this, AppConstants.ACTION_OPERA_NAME_FORBID + "????????????" + account + ",???????????????????????????????????????!", item);
                    return true;

                }

            } else {

            }

        }


        MsgItem gagMsgItem = item.clone();
        gagMsgItem.setIstroop(1);
        gagMsgItem.setSenderuin(account);
        gagMsgItem.setFrienduin(group);
        gagMsgItem.setNickname(NickNameUtils.queryMatchNickname(group, account, false));
        gagMsgItem.setMessage(gagTime + "");
        item.setNickname(gagMsgItem.getNickname());
        //???????????????????????????
        item.setFrienduin(group);
        item.setIstroop(1);

        //
        String result = "";
        if (RemoteService.isIsInit()) {
            String s = RemoteService.gagUser(gagMsgItem.getFrienduin(), gagMsgItem.getSenderuin(), gagTime);
            if (s != null) {
                result = "??????:" + s;
            } else {
                MsgReCallUtil.notifyGadPersonMsgNoJump(this, gagTime, gagMsgItem);
            }
        } else {
            MsgReCallUtil.notifyGadPersonMsgNoJump(this, gagTime, gagMsgItem);
        }

        String nickname;
        if (ConfigUtils.isDisableAtFunction(this)) {
            nickname = NickNameUtils.formatNicknameFromNickName(gagMsgItem.getSenderuin(), gagMsgItem.getNickname());
        } else {
            nickname = gagMsgItem.getNickname();
            if (gagMsgItem.getSenderuin().equals(nickname)) {
                gagMsgItem.setNickname("qq" + nickname + "");
            }
        }
        String message;
        if (gagTime <= 0) {
            if (AppConstants.ALL_PERSON_FLAG.equals(gagMsgItem.getSenderuin())) {
                message = AppConstants.ACTION_OPERA_NAME + "??????????????????";

            } else {
                message = AppConstants.ACTION_OPERA_NAME + "??????" + gagMsgItem.getNickname() + "??????";

            }
            //            MsgReCallUtil.notifyAtMsgJump(this, gagMsgItem.getSenderuin(), gagMsgItem.getNickname(), , item);
        } else {
            if (AppConstants.ALL_PERSON_FLAG.equals(gagMsgItem.getSenderuin())) {
                message = AppConstants.ACTION_OPERA_NAME + "??????????????????";
            } else {
                message = AppConstants.ACTION_OPERA_NAME + "??????" + gagMsgItem.getNickname() + ",????????????" + DateUtils.getGagTime(gagTime);

            }
        }
        if (!TextUtils.isEmpty(result)) {
            message = message + "\n" + result;
        }
        if (groupMsg && nameBean != null && nameBean.isReplayatperson()) {//????????????????????????????????????????????????

            MsgReCallUtil.notifyAtMsgJump(RobotContentProvider.this, gagMsgItem.getSenderuin(), gagMsgItem.getNickname(), message, item);

        } else {

            MsgItem msgItem = item.setSenderuin(gagMsgItem.getSenderuin());
            msgItem.setMessage(message);
            msgItem.setNickname(nickname);
            MsgReCallUtil.notifyJoinMsgNoJumpDisableAt(RobotContentProvider.this, msgItem);

        }

        return true;
    }

    private long formatGagTime(String gagTimeStr) {
        long gagTime;
        if (TextUtils.isEmpty(gagTimeStr)) {
            gagTime = ParseUtils.parseGagStr2Secound(IGnoreConfig.DEFAULT_GAG_TIME_STR);
        } else {
            gagTime = ParseUtils.parseGagStr2Secound(gagTimeStr);
        }
        return gagTime;

    }


    public boolean doAtCmdByGroupMsg(MsgItem item, boolean isManager, String[] args, String group) {
        return doAtCmd(item, isManager, args, group, ParamParseUtil.sArgFirst, ParamParseUtil.sArgSecond);
    }

    public boolean doAtCmdByPrivateMsg(MsgItem item, boolean isManager, String[] args, String group) {


        if (!verifyPrivateMsgGroupParam(group)) {
            return false;
        }
        item.setFrienduin(group);//??????????????????????????????????????????????????????
        item.setIstroop(1);//????????????????????????????????????

        return doAtCmd(item, isManager, args, group, ParamParseUtil.sArgSecond, ParamParseUtil.sArgThrid);
    }

    public boolean doAtCmd(MsgItem item, boolean isManager, String[] args, String group, int gagPersonArgPosition,
                           int gagMsgPosition) {
        String first;
        first = ParamParseUtil.getArgByArgArr(args, gagPersonArgPosition);
        String second = ParamParseUtil.getArgByArgArr(args, gagMsgPosition);
        second = TextUtils.isEmpty(second) ? "????????????????????????,?????????????????????,????????????" : VarCastUtil.parseStr(item, _dbUtils, second);
        if ("??????".equals(first)) {
            if (!isManager) {
                MsgReCallUtil.notifyJoinMsgNoJump(this, "?????????????????????,????????????????????????", item);
                return true;
            }
            String nickname = "????????????";
            MsgReCallUtil.notifyAtMsgJump(this, "0", nickname, second, item);

        } else if (FloorUtils.isFloorData(first)) {

            String tempQQ = FloorUtils.getFloorQQ(group, first);
            if (tempQQ != null) {
                String nickname = NickNameUtils.queryMatchNickname(group, tempQQ, false);
                MsgReCallUtil.notifyAtMsgJump(this, tempQQ, nickname, second, item);
            } else {
                MsgReCallUtil.notifyJoinReplaceMsgJump(this, FloorUtils.getFloorInputDataInValidMsg(tempQQ), item);
                return true;
            }


        } else {

            Pair<Integer, Integer> pair = FloorUtils.parseMultiFloorData(first);
            if (pair != null) {
                List<MsgItem> floors = FloorUtils.getFloors(group, pair.first, pair.second);
                if (floors != null) {
                    BatchUtil.atFloorData(this, item, floors, second);
                } else {

                    MsgReCallUtil.notifyJoinMsgNoJump(this, "??????????????????????????????,??????????????????", item);
                }
                return true;
            } else {
                if (!RegexUtils.checkNoSignDigit(first)) {
                    String nickname = NickNameUtils.queryMatchNickname(group, item.getSenderuin(), false);
                    MsgReCallUtil.notifyAtMsgJump(this, item.getSenderuin(), nickname, "???????????????????????????????????????", item);
                } else {


                    String nickname = NickNameUtils.queryMatchNickname(group, first, false);
                    MsgReCallUtil.notifyAtMsgJump(this, first, nickname, second, item);
                }

            }

        }
        return false;
    }


    static {
        LogUtil.importPackage();

    }


    @Override
    public void notifyChange(@NonNull Uri uri, @Nullable IRobotContentProvider observer) {


        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).notifyChange(uri, observer);
        }
        if (interceptNotifyChanage) {
            return;
        }
        getProxyContext().getContentResolver().notifyChange(uri, null);
    }

    @Override
    public boolean getBooleanConfig(String key) {
        if (sharedPreferences == null) {
            initConfig();
        }
        return sharedPreferences.getBoolean(key, false);
    }

    @Override
    public void reloadSharedPreferences() {

        initConfig();
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    @Override
    public boolean writeConfig(Object beforeObj, String key, String value) {
        boolean result = false;
        if (beforeObj instanceof Integer) {
            result = sharedPreferences.edit().putInt(key, Integer.parseInt(value)).commit();

        } else if (beforeObj instanceof Long) {
            result = sharedPreferences.edit().putLong(key, Long.parseLong(value)).commit();

        } else if (beforeObj instanceof String) {
            result = sharedPreferences.edit().putString(key, value).commit();

        } else if (beforeObj instanceof Boolean) {
            result = sharedPreferences.edit().putBoolean(key, Boolean.parseBoolean(value)).commit();

        } else if (beforeObj instanceof Set) {
            String[] split = value.split(",");
            TreeSet<String> strings = new TreeSet<>();
            for (String s : split) {
                strings.add(s);
            }
            result = sharedPreferences.edit().putStringSet(key, strings).commit();


        }
        return result;

    }

    @Override
    public SQLiteDatabase getRobotDb() {
        return _dbUtils.getDb();
    }

    @Override
    public void reloadPlugin(final Handler.Callback callback) {
        initJAVAPlugin(new INotify() {
            @Override
            public void onNotify(Object param) {

                if (callback != null) {
                    Message message = getHandler().obtainMessage(0, mPluginList == null ? 0 : mPluginList.size(), 0);
                    callback.handleMessage(message);
                }

            }
        });
        initLuaPlugin();
        initJavascriptSPlugin();
    }

    @Override
    public void writeBooleanConfig(String key, boolean isChecked) {

        SharedPreferences.Editor edit = sharedPreferences.edit();

        boolean commit = edit.putBoolean(key, isChecked).commit();
        edit.apply();
        if (commit == false) {
            mLastError = "??????" + key + "????????????";
        }
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @return
     */
    @Override

    public boolean hasDisablePlugin(IPluginHolder holder) {
        File path = new File(holder.getPath());
//        return sharedPreferences.getBoolean("disable_plugin_"+pluginInterface.get);

        return new File(path.getParentFile(), "" + path.getName() + ".disable").exists();
    }


    public static boolean writePluginErrorLog(File pluginPath, String error) {


//        return sharedPreferences.getBoolean("disable_plugin_"+pluginInterface.get);

        File file = new File(pluginPath.getParentFile(), "" + pluginPath.getName() + ".log");

        if (error == null) {
            if (file.exists()) {
                return file.delete();
            }
        }

        try {
            FileUtils.write(file, error, "utf-8");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }


    public String readPluginErrorLog(File pluginPath, String error) {
//        return sharedPreferences.getBoolean("disable_plugin_"+pluginInterface.get);

        File file = new File(pluginPath.getParentFile(), "" + pluginPath.getName() + ".log");

        try {
            return FileUtils.readFileToString(file, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public boolean disablePlugin(IPluginHolder holder, boolean isChecked) {

        File path = new File(holder.getPath());
        File file = new File(path.getParentFile(), "" + path.getName() + ".disable");
        boolean result = false;
        if (isChecked) {

            try {
                file.createNewFile();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            }
        } else {
            if (file.exists()) {

                result = file.delete();
            } else {
                result = true;
            }
        }
        holder.setDisableFlag(result);
        return result;
    }

    @Override
    public void addObserver(IContentProviderNotify providerNotify) {
        observers.add(providerNotify);
    }

    @Override
    public void clearObserver() {
        observers.clear();


    }

    @Override
    public boolean onAttachIHostControlApi(IHostControlApi hostControlApi) {
        mHostControlApi = hostControlApi;
        return false;

    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            String callingPackage = getCallingPackage();

        }
//        int callingPid = Binder.getCallingPid();
        return super.call(method, arg, extras);
    }

    List<IRobotContentProvider.IContentProviderNotify> observers = new ArrayList<>();

    @Override
    public Context getProxyContext() {

        if (mInnerProxyCoontext != null) {
            return mInnerProxyCoontext;
        }
        return this.getContext();
    }

    @Override
    public ClassLoader getProxyClassloader() {
        if (mProxyClassloader != null) {
            return mProxyClassloader;

        } else {
            return RobotContentProvider.class.getClassLoader();
        }
    }

    @Override
    public Context getRobotContext() {

        if (mPakcageContext != null) {
            return mPakcageContext;
        }
        if (mInnerProxyCoontext != null) {
            return mInnerProxyCoontext;
        }
        return this.getContext();
    }

    @Override
    public String getLastErrorMsg() {
        return mLastError;
    }


    public void setProxyContext(Context context) {
        this.mInnerProxyCoontext = context;

    }

    @Override
    public void setProxyClassloader(ClassLoader classloader) {
        this.mProxyClassloader = classloader;
    }

    @Override
    public void interceptNotifyChanage(boolean intercept) {
        this.interceptNotifyChanage = intercept;
    }


    private Context mInnerProxyCoontext;

    public boolean isDisableAtFunction() {
        return mCfBaseDisableAtFunction;
    }

    public boolean replyShowNickname() {
        return mCfBaseReplyShowNickName;
    }

    public boolean isDisableSuperFunction() {
        return mStopUseAdvanceFunc;
    }


    public Resources getResources() {

        if (getRobotContext() != null) {
            return getRobotContext().getResources();
        }

        if (mProxyResources == null || getProxyContext().getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            return getProxyContext().getResources();
        }


        return mProxyResources;

    }


    public boolean isAsPluginLoad() {
        return !getProxyContext().getPackageName().equals(BuildConfig.APPLICATION_ID);
    }

    public IGroupConfig getGroupConfig(String group) {


        GroupWhiteNameBean account = AccountUtil.findAccount(mQQGroupWhiteNames, group, false);

        return account;
    }
}




    /*
        if (code == 100000) {

        } else if (code == 200000) {

        } else if (code == 200000) {

        } else if (code == 302000) {

        } else if (code == 308000) {

        } else if (code == 313000) {

        } else if (code == 314000) {

        } else {
           /* 40001	??????key??????
            40002	????????????info
       8
            40004	??????????????????????????????
            40007	??????????????????*/
/*

 */
