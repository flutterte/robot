package cn.qssq666.robot.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.FileUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import cn.qssq666.db.DBUtils;
import cn.qssq666.robot.BuildConfig;
import cn.qssq666.robot.R;
import cn.qssq666.robot.app.AppContext;
import cn.qssq666.robot.bean.GagAccountBean;
import cn.qssq666.robot.bean.MsgItem;
import cn.qssq666.robot.business.RobotContentProvider;
import cn.qssq666.robot.constants.AppConstants;
import cn.qssq666.robot.constants.Cns;
import cn.qssq666.robot.constants.MsgTypeConstant;
import cn.qssq666.robot.databinding.ActivityDevToolBinding;
import cn.qssq666.robot.interfaces.INotify;
import cn.qssq666.robot.service.RemoteService;
import cn.qssq666.robot.utils.AppUtils;
import cn.qssq666.robot.utils.DialogUtils;
import cn.qssq666.robot.utils.LogUtil;
import cn.qssq666.robot.utils.MediaUtils;
import cn.qssq666.robot.utils.PairFix;
import cn.qssq666.robot.utils.RobotUtil;
import cn.qssq666.robot.utils.SPUtils;

/**
 * Created by qssq on 2017/12/20 qssq666@foxmail.com
 */

public class DevToolActivity extends SuperActivity implements View.OnClickListener {

    private static final String TAG = "DevToolActivity";
    private static final int REQUEST_CODE_SELECT_DB = 2;
    private static final int REQUEST_CODE_SELECT_DB_CUSTOM_PATH = 4;
    private static final int MSG_CODE_APPEND_LOG = 3;
    private static final int MSG_CODE_CURRENT_LOG = 4;
    private ActivityDevToolBinding binding;
    private int istroop;
    private ContentResolver resolver;
    private int mMsgType = MsgTypeConstant.MSG_TYPE_TEXT;

    HashMap<String, Long> sendmsgMap = new HashMap<>();
    private long mStatupTime;
    private SharedPreferences defaultSharedPreferences;
    private EditText evPath;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("???????????????");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dev_tool);

        binding.btnUnregmsgListener.setOnClickListener(this);
        binding.btnRegmsgListener.setOnClickListener(this);
        binding.radiogroup.setOnClickListener(this);


        binding.radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                int index = group.indexOfChild(binding.radiogroup.findViewById(checkedId));
                SPUtils.setValue(AppContext.getInstance(), AppConstants.CONFIG_ISTROOP, index);

                switch (index) {
                    case 0:
                        istroop = 0;
                        mMsgType = MsgTypeConstant.MSG_TYPE_TEXT;
                        break;
                    case 1:
                        mMsgType = MsgTypeConstant.MSG_TYPE_TEXT;
                        istroop = 1;
                        break;
                    case 2:
                        mMsgType = MsgTypeConstant.MSG_TYPE_JOIN_GROUP;
                        istroop = 1;
                        break;
                    case 3:
                        mMsgType = MsgTypeConstant.MSG_TYPE_REDPACKET;
                        istroop = 1;

                        break;
                }


            }
        });
//        ((RadioButton) binding.radiogroup.getChildAt(0)).setChecked(true);
        resolver = this.getContentResolver();
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String dbPath = defaultSharedPreferences.getString(Cns.CUSTOM_DB_PATH, "");


        ((TextView) binding.getRoot().findViewById(R.id.tv_db_path_current)).setText(AppContext.getDbUtils().getDbNameAbsolutePath().equals(DBUtils.defaultDBName) ? "/data/data/" + BuildConfig.APPLICATION_ID + "/databases/qssq.db" : DBUtils.dbName);
        binding.evDbPath.setText(dbPath);

        evPath = (EditText) binding.getRoot().findViewById(R.id.ev_db_path);


        evPath.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (TextUtils.isEmpty(s) || new File(s.toString()).exists()) {
                    defaultSharedPreferences.edit().putString(Cns.CUSTOM_DB_PATH, s.toString()).apply();

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.btnSave.setOnClickListener(this);
        binding.btnQuerySendMsg.setOnClickListener(this);
        binding.btnDelete.setOnClickListener(this);
        binding.btnBackupDatabase.setOnClickListener(this);
        binding.btnRestoreDatabase.setOnClickListener(this);
        binding.btnShareDatabase.setOnClickListener(this);
        binding.btnTestSpeed.setOnClickListener(this);
        binding.btnModify.setOnClickListener(this);
        //btn_select_db_path
        binding.btnOpenLog.setOnClickListener(this);
        binding.btnSelectDbPath.setOnClickListener(this);
        binding.btnTestReg.setOnClickListener(this);
        binding.btnTestSimulatorGag.setOnClickListener(this);

        binding.getRoot().findViewById(R.id.btn_fetch_current_qq).setOnClickListener(this);
        binding.getRoot().findViewById(R.id.btn_fetch_senderuin_nickname).setOnClickListener(this);

/*
        ArrayAdapter<String> arr_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
        //????????????
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //???????????????
        spinnerNetWord.setAdapter(arr_adapter);*/
        initRobotNetWordReply();
        initEvContentValue();
        registerListener();

    }

    public void initRobotNetWordReply() {
        binding.btnDropdownlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //ev_reply_secret
                updateReplyWordUi(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        int defaultReplyIndex = AppUtils.getConfigSharePreferences(getApplicationContext()).getInt(Cns.SP_DEFAULT_REPLY_API_INDEX, 0);
        binding.btnDropdownlist.setSelection(defaultReplyIndex);
        updateReplyWordUi(defaultReplyIndex);
    }

    public void updateReplyWordUi(int defaultReplyIndex) {
        binding.evReplySecret.setVisibility(defaultReplyIndex == 0 ? View.VISIBLE : View.GONE);

        String key = AppUtils.getConfigSharePreferences(getApplicationContext()).getString(AppUtils.getRobotReplyKey(defaultReplyIndex), "");
        if (BuildConfig.DEBUG) {

        }
        binding.evTutingKey.setText(key);

        String serret = AppUtils.getConfigSharePreferences(getApplicationContext()).getString(AppUtils.getRobotReplySecret(defaultReplyIndex), "");
        binding.evReplySecret.setText(serret);
    }


    private void initEvContentValue() {

        binding.evNickname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                SPUtils.setValue(DevToolActivity.this, AppConstants.CONFIG_NICKNAME, s.toString());


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.evSelfuin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SPUtils.setValue(DevToolActivity.this, AppConstants.CONFIG_SELFUIN, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.evSenderuin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SPUtils.setValue(DevToolActivity.this, AppConstants.CONFIG_SENDERUIN, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.evFrienduin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SPUtils.setValue(DevToolActivity.this, AppConstants.CONFIG_friendUIN, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.evMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SPUtils.setValue(DevToolActivity.this, AppConstants.CONFIG_MSG, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.evContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SPUtils.setValue(DevToolActivity.this, AppConstants.CONFIG_TEST_CONTENT, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        binding.evContentReg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SPUtils.setValue(DevToolActivity.this, AppConstants.CONFIG_TEST_REG, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.evSelfuin.setText(SPUtils.getValue(this, AppConstants.CONFIG_SELFUIN, "35068264"));
        binding.evNickname.setText(SPUtils.getValue(this, AppConstants.CONFIG_NICKNAME, "????????????"));
        binding.evSenderuin.setText(SPUtils.getValue(this, AppConstants.CONFIG_SENDERUIN, "153016267"));
        binding.evFrienduin.setText(SPUtils.getValue(this, AppConstants.CONFIG_friendUIN, ""));
        binding.evMessage.setText(SPUtils.getValue(this, AppConstants.CONFIG_MSG, "?????????????????????"));
        binding.evContent.setText(SPUtils.getValue(this, AppConstants.CONFIG_TEST_CONTENT, "b33333A"));
        binding.evContentReg.setText(SPUtils.getValue(this, AppConstants.CONFIG_TEST_REG, "[0-9]{3,10}"));


//        binding.evMessage.setText(SPUtils.getValue(this, AppConstants.CONFIG_MSG, "??????QQ?????????????"));
        int index = SPUtils.getValue(AppContext.getInstance(), AppConstants.CONFIG_ISTROOP, 0);
        ((Checkable) binding.radiogroup.getChildAt(index)).setChecked(true);

    }

    boolean hasRegister = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_log:
                Intent intent = new Intent(this, LogActivity.class);
                startActivity(intent);

                break;
            case R.id.btn_delete: {

                String tableName = binding.evTableName.getText().toString();
                if (!AppContext.getDbUtils().tableExistFromDb(AppContext.getDbUtils().getSQLiteDatabaseObj(), tableName)) {

                    Toast.makeText(this, "table not exist " + tableName, Toast.LENGTH_SHORT).show();
                } else {
                    AppContext.getDbUtils().deleteTable(tableName);
                    Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();

                }
            }
            break;
            case R.id.btn_modify: {
                final String tableName = binding.evTableName.getText().toString();

                if (!AppContext.getDbUtils().tableExistFromDb(AppContext.getDbUtils().getSQLiteDatabaseObj(), tableName)) {

                    Toast.makeText(this, "table not exist ", Toast.LENGTH_SHORT).show();
                    return;
                }
                DialogUtils.showEditDialog(this, "????????????????????????table", null, new INotify<String>() {
                    @Override
                    public void onNotify(String param) {
                        AppContext.getDbUtils().modifyTableName(tableName, param);

                    }
                });
            }
            break;
            case R.id.btn_unregmsg_listener:
                unregisterListener();
                break;
            case R.id.btn_regmsg_listener:
                registerListener();

                break;
            case R.id.btn_test_speed:
                if (binding.btnRegmsgListener.isEnabled()) {
                    AppContext.showToast("??????????????????");

                } else {

                    mStatupTime = System.currentTimeMillis();


                    boolean hasTest = SPUtils.getBoolean(this, Cns.FILE_HAS_TEST, false);
                    if (!hasTest) {
                        DialogUtils.showDialog(this, "?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????app ????????????????????????", "", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SPUtils.setValue(AppContext.getInstance(), Cns.FILE_HAS_TEST, true);

                                addMsg(resolver, "??????", binding.evNickname.getText().toString(),
                                        binding.evSelfuin.getText().toString(),
                                        binding.evSelfuin.getText().toString(),
                                        binding.evSelfuin.getText().toString(),
                                        MsgTypeConstant.MSG_TYPE_TEXT, 0);
                            }
                        });
                    } else {


                        addMsg(resolver, "??????", binding.evNickname.getText().toString(),
                                binding.evSelfuin.getText().toString(),
                                binding.evSelfuin.getText().toString(),
                                binding.evSelfuin.getText().toString(),
                                MsgTypeConstant.MSG_TYPE_TEXT, 0);

                    }


                }

                break;
            case R.id.btn_backup_database: {
                File dbFile = this.getApplicationContext().getDatabasePath("qssq.db");
                File backupPath = MediaUtils.getBackupPath();
                File backupPathDbFile = new File(backupPath, "qssq.db");

                try {
                    FileUtils.copyFile(dbFile, backupPathDbFile);
                    Toast.makeText(this, "????????????,????????????:" + backupPathDbFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "????????????" + e.toString(), Toast.LENGTH_SHORT).show();
                }


            }


            break;
            case R.id.btn_restore_database:
                AppUtils.chooseFile(this, REQUEST_CODE_SELECT_DB);


                break;

            case R.id.btn_test_reg: {
                try {
                    String input = binding.evContent.getText().toString();
                    String regex = binding.evContentReg.getText().toString();
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(input);
                    boolean isMaches = m.matches();
                    m = p.matcher(input);
                    boolean find = m.find();
                    StringBuffer sb = new StringBuffer();
                    sb.append("??????????????????:" + isMaches + "\n");
                    sb.append("??????????????????:" + find + "\n");
                    int count = find ? m.groupCount() : 0;
                    for (int i = 0; i <= count; i++) {
                        sb.append("" + (i + 1) + ":" + m.group(i) + "\n");
                    }
                    if (count > 1) {
                        DialogUtils.showDialog(this, sb.toString(), "????????????");
                    } else {
                        Toast.makeText(this, "??????????????????:" + sb.toString(), Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    DialogUtils.showDialog(this, "????????????" + e.getMessage());
                }

            }
            break;
            case R.id.btn_test_simulator_gag: {
                String word = binding.evContent.getText().toString();
                PairFix<GagAccountBean, String> gagAccountBeanStringPair = RobotContentProvider.getInstance().keyMapContainGag(word, false);
                StringBuffer sb = new StringBuffer();
                if (gagAccountBeanStringPair != null && gagAccountBeanStringPair.first != null) {
                    sb.append("?????????:" + gagAccountBeanStringPair.first.getAccount() + "\n");
                    sb.append("????????????:" + gagAccountBeanStringPair.first.isGag() + "\n");
                    sb.append("?????????:" + gagAccountBeanStringPair.first.isKick() + "\n");
                    sb.append("????????????:" + gagAccountBeanStringPair.first.action + "\n");
                    sb.append("???????????????:" + gagAccountBeanStringPair.first.duration + "\n");
                    sb.append("????????????:" + gagAccountBeanStringPair.first.isSilence() + "\n");
                    sb.append("????????????:" + gagAccountBeanStringPair.first.getReason() + "\n");
                } else {
                    sb.append("?????????\n");

                }
                DialogUtils.showDialog(this, "????????????\n" + sb.toString());

            }
            break;
            case R.id.btn_share_database: {
                File dbFile = this.getApplicationContext().getDatabasePath("qssq.db");
                File backupPath = new File(MediaUtils.getBackupPath(), "qssq.db");
                try {
                    FileUtils.copyFile(dbFile, backupPath);
                    Intent shareFileIntent = AppUtils.getShareFileIntent(backupPath);
                    this.startActivity(Intent.createChooser(shareFileIntent, "???????????????"));
//                    this.startActivity(shareFileIntent);
                } catch (Exception e) {
                    AppContext.showToast("??????????????????");
                    ;
                    e.printStackTrace();
                }
            }
            break;
            case R.id.btn_select_db_path:

                AppUtils.chooseFile(this, REQUEST_CODE_SELECT_DB);
                break;

            case R.id.btn_save:

                int selectedItemPosition = binding.btnDropdownlist.getSelectedItemPosition();
                SharedPreferences.Editor edit = AppUtils.getConfigSharePreferences(getApplicationContext()).edit();
                edit.putString(AppUtils.getRobotReplyKey(selectedItemPosition), binding.evTutingKey.getText().toString());
                edit.putString(AppUtils.getRobotReplySecret(selectedItemPosition), binding.evReplySecret.getText().toString());
                /*


        /**
         * Commit your preferences changes back from this Editor to the
         * {@link SharedPreferences} object it is editing.  This atomically
         * performs the requested modifications, replacing whatever is currently
         * in the SharedPreferences.
         *
         * <p>Note that when two editors are modifying preferences at the same
         * time, the last one to call commit wins.
         *
         * <p>If you don't care about the return value and you're
         * using this from your application's main thread, consider
         * using {@link #apply} instead.
         *
         * @return Returns true if the new values were successfully written
         * to persistent storage.
         */
/**

 *????????????????????????????????????????????????@link sharedreferences????????????????????????????????????????????????????????????????????????????????????????????????
 * ?????????????????????????????????????????????????????????????????????????????????commit???????????????
 * <p>????????????????????????????????????????????????????????????????????????????????????????????????@?????? apply()???

 ???????????????????????????*@return??????true

 *???????????????
 */
                edit.putInt(Cns.SP_DEFAULT_REPLY_API_INDEX, selectedItemPosition).apply();
                Uri uri = Uri.withAppendedPath(Uri.parse(MsgTypeConstant.AUTHORITY_CONTENT), RobotContentProvider.ACTION_UPDATE_KEY);
                ContentValues contentvalues = new ContentValues();
                contentvalues.put(Cns.UPDATE_KEY, binding.evTutingKey.getText().toString());
                contentvalues.put(Cns.UPDATE_SECRET, binding.evReplySecret.getText().toString());
                resolver.update(uri, contentvalues, null, null);


                break;

            case R.id.btn_query_send_msg:
                if (TextUtils.isEmpty(binding.evMessage.getText().toString())) {
//                    evfriendUin.setValue("" + req);
                    Toast.makeText(DevToolActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (TextUtils.isEmpty(binding.evMessage.getText().toString())) {

                    binding.evMessage.requestFocus();
                    Toast.makeText(DevToolActivity.this, "????????????", Toast.LENGTH_SHORT).show();

                    return;

                }
                if (istroop == 1 && TextUtils.isEmpty(binding.evFrienduin.getText().toString())) {
                    Toast.makeText(this, "???????????????,???????????????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (istroop == 1 && binding.evFrienduin.getText().toString().equals(binding.evSenderuin.getText().toString())) {
                    Toast.makeText(this, "????????????????????????QQ????????????????????????,???????????????????????????????????????????????????????????????????QQ??????", Toast.LENGTH_SHORT).show();

                    return;
                }
                if (istroop == 1 && binding.evFrienduin.getText().toString().equals(binding.evSenderuin.getText().toString())) {
                    Toast.makeText(this, "????????????????????????????????????????????????????????????????????????????QQ??????????????????????????????,??????????????????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String friendUin = istroop == 0 ? binding.evSenderuin.getText().toString() : binding.evFrienduin.getText().toString();

                JSONObject jsonObject = addMsg(resolver, binding.evMessage.getText().toString(), binding.evNickname.getText().toString(), binding.evSelfuin.getText().toString(), binding.evSenderuin.getText().toString(), friendUin, mMsgType, istroop);
                try {
                    int anInt = jsonObject.getIntValue(MsgTypeConstant.code);
                    if (anInt == 0) {
                        AppContext.getInstance().showToast("?????????..");
                    } else {

                        AppContext.getInstance().showToast("???????????? " + jsonObject.getString(MsgTypeConstant.msg) + ", code=" + anInt);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;

                /*
                      binding.getRoot().findViewById(R.id.btn_fetch_current_qq).setOnClickListener(this);
        binding.getRoot().findViewById(R.id.btn_fetch_senderuin_nickname).setOnClickListener(this);
                 */
            case R.id.btn_fetch_current_qq:

                if (RemoteService.isIsInit()) {
                    String s = RemoteService.queryLoginQQ();
                    if (s != null) {
                        ((EditText) binding.getRoot().findViewById(R.id.ev_selfuin)).setText(s);

                        AppContext.showToast("????????????!");
                        return;
                    }
                }

                AppContext.showToast("????????????!????????????????????????????????????????????????????????????()!");
                break;
            case R.id.btn_fetch_senderuin_nickname:
                if (RemoteService.isIsInit()) {
                    if (TextUtils.isEmpty(binding.evSenderuin.getText().toString())) {
                        binding.evSenderuin.setError("????????????qq");
                        binding.evSenderuin.requestFocus();
                        return;
                    }
                    String s = RemoteService.queryNickname(binding.evSenderuin.getText().toString(), binding.evFrienduin.getText().toString(), istroop);
                    if (s != null) {
                        binding.evNickname.setText(s);

                        AppContext.showToast("????????????!");
                        return;
                    }
                }

                AppContext.showToast("????????????!????????????????????????????????????????????????????????????()!");

                break;
        }
    }

    public static String getUniKey(MsgItem item) {
        return item.getSenderuin() + "" + item.getFrienduin() + "" + item.getIstroop() + "" + item.getType() + "";
    }

    public void registerListener() {
        if (hasRegister) {
            AppContext.showToast("????????????");
            return;
        }
        binding.btnUnregmsgListener.setEnabled(true);
        binding.btnRegmsgListener.setEnabled(false);
        hasRegister = true;
        resolver.registerContentObserver(Uri.parse(MsgTypeConstant.AUTHORITY_CONTENT), true, myobserver);
    }

    public void unregisterListener() {
        if (!hasRegister) {
            AppContext.showToast("????????????");
            return;
        }
        binding.btnUnregmsgListener.setEnabled(false);
        binding.btnRegmsgListener.setEnabled(true);

        resolver.unregisterContentObserver(myobserver);
        hasRegister = false;
        return;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_CODE_APPEND_LOG:
                    binding.evResultLog.append(msg.obj + "\n");
                    break;
                case MSG_CODE_CURRENT_LOG:
                    binding.evResult.setText(msg.obj + "");
                    break;
            }
        }
    };

    public void tencentUri() {
        UriMatcher a = new UriMatcher(-1);
        a.addURI("qq.friendlist", "group/#", 1000);
        a.addURI("qq.friendlist", "friendlist/#", 1001);
        a.addURI("qq.friendlist", "trooplist/#", 1002);
        a.addURI("qq.friendlist", "troopmemberinfo/#/#/#", 1003);
        a.addURI("qq.friendlist", "troopname/#/#", 1004);
        a.addURI("qq.friendlist", "discussinfo/#/#", 1005);
        a.addURI("qq.friendlist", "discussmenberinfo/#/#/#", 1006);
        a.addURI("qq.friendlist", "individuationUserData/#", 1007);

        /*
             call com.tencent.mobileqq.content.FriendListProvider.
             query(android.net.Uri$StringUri=[content://qq.friendlist/friendlist/35068264],[Ljava.lang.String;=[[Ljava.lang.String;@4b1e70f8],java.lang.String=[null],[Ljava.lang.String;=[null],java.lang.String=[null])sko[sko@4b31a0a8]
                                                    null
         */


    }


    public static JSONObject addMsg(ContentResolver resolver, String message, String nickname, String selfuin, String senderuin, String frienduin, int type, int istroop) {
        Uri uri = Uri.withAppendedPath(Uri.parse(MsgTypeConstant.AUTHORITY_CONTENT), RobotContentProvider.ACTION_MSG);
        Log.w(TAG, "sendMsg:" + uri.toString());
        ContentValues values = new ContentValues();
        values.put(MsgTypeConstant.msg, message);
        values.put(MsgTypeConstant.nickname, nickname);
        values.put(MsgTypeConstant.time, new Date().getTime() / 1000);
        values.put(MsgTypeConstant.senderuin, senderuin);
        values.put(MsgTypeConstant.selfuin, selfuin);
        values.put(MsgTypeConstant.frienduin, frienduin);
        values.put(MsgTypeConstant.type, type);
        values.put(MsgTypeConstant.messageID, 0);
        values.put(MsgTypeConstant.apptype, "test");
        values.put(MsgTypeConstant.time, new Date().getTime());
        values.put(MsgTypeConstant.istroop, istroop);
        Uri insert = resolver.insert(uri, values);// java.lang.IllegalArgumentException: Unknown URL content://cn.qssq66.roboot/insert/gad
        if (insert == null) {
            Log.e(TAG, "??????????????????!?????????????????????" + insert);
            try {
                return JSON.parseObject(MsgTypeConstant.ERROR_JSON);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        try {
            return JSON.parseObject(insert.toString());
        } catch (JSONException e) {

            return null;
        }
    }

    ContentObserver myobserver = new ContentObserver(handler) {
        @Override
        public boolean deliverSelfNotifications() {
            Toast.makeText(DevToolActivity.this, "deliverSelfNotifications", Toast.LENGTH_SHORT).show();
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
//                super.onChange(selfChange);
//                Toast.makeText(DevToolActivity.this, "selfChange" + selfChange, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
//                        Toast.makeText(DevToolActivity.this, "selfChange" + selfChange + ",message:" + uri, Toast.LENGTH_SHORT).show();


            Log.w(TAG, "deliverSelfNotifications:" + uri + "," + Thread.currentThread());
            //LogUtil.writeLog("???????????????:onReceive " + uri);
            MsgItem msgItem = RobotUtil.uriToMsgItem(uri);

            if (mStatupTime > 0 && msgItem.getMessage().contains("" + BuildConfig.VERSION_NAME)) {
                AppContext.showToast("????????????:" + (System.currentTimeMillis() - mStatupTime) + "??????");
                mStatupTime = 0;
            }


            String text = msgItem.toString();


            handler.obtainMessage(MSG_CODE_CURRENT_LOG, text).sendToTarget();
            long distance = (System.currentTimeMillis()) - msgItem.getTime();

            String message = msgItem.getMessage();
            if (message.length() > 50) {
                message = message.substring(0, 50);
            }
            handler.obtainMessage(MSG_CODE_APPEND_LOG, String.format("[%dms][%s:%s]", distance, msgItem.getSenderuin(), message)).sendToTarget();


        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hasRegister) {
            hasRegister = false;
            resolver.unregisterContentObserver(myobserver);

        }

    }

    static {
        LogUtil.importPackage();

    }


    public static DataOutputStream executeSu(DataOutputStream os, String cmd) {
        try {
            Process proc;
            if (os == null) {
                proc = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(proc.getOutputStream());
            }

            os.writeBytes(cmd);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "????????????????????????" + e.getMessage());
        }
        return os;

    }


    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {


            Uri uri = data.getData();
            final String path;
            try {
                path = AppUtils.getPath(this, uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "?????????????????????" + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            final File file = new File(path);
            if (!file.exists()) {
                Toast.makeText(this, "???????????????????????????", Toast.LENGTH_SHORT).show();

            } else {


                if (!file.getAbsolutePath().endsWith("db") && !file.getAbsolutePath().endsWith("bak") && !file.getAbsolutePath().endsWith("zip")
                        ) {
                    AppContext.showToast("????????????????????????db ???????????????");
                } else {

                    if (requestCode == REQUEST_CODE_SELECT_DB) {

                        DialogUtils.showConfirmDialog(this, "???????????????????????????????????????????????????????", new INotify<Void>() {
                            @Override
                            public void onNotify(Void param) {
                                File dbFile = AppContext.getInstance().getDatabasePath("qssq.db");
                                try {
                                    FileUtils.copyFile(file, dbFile);
                                    AppContext.showToast("????????????,????????????");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    AppContext.showToast("????????????" + e.toString());
                                }


                            }
                        });

                    } else if (requestCode == REQUEST_CODE_SELECT_DB_CUSTOM_PATH) {

                        DBUtils.dbName = file.getAbsolutePath();
                        evPath.setText(file.getAbsolutePath());

                        //                      defaultSharedPreferences.edit().putString(Cns.CUSTOM_DB_PATH,file.getAbsolutePath()).apply();

                    }
                }


            }
        }
    }
}
