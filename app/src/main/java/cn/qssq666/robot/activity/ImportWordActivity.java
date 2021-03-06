package cn.qssq666.robot.activity;
import cn.qssq666.CoreLibrary0;import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cn.qssq666.robot.R;
import cn.qssq666.robot.app.AppContext;
import cn.qssq666.robot.asynctask.QssqTask;
import cn.qssq666.robot.bean.CheckSplitInfo;
import cn.qssq666.robot.bean.ReplyWordBean;
import cn.qssq666.robot.constants.AppConstants;
import cn.qssq666.robot.constants.FieldCns;
import cn.qssq666.robot.databinding.ActivityImportWordBinding;
import cn.qssq666.robot.event.WordEvent;
import cn.qssq666.robot.utils.AppUtils;
import cn.qssq666.robot.utils.DBHelper;
import cn.qssq666.robot.utils.DialogUtils;
import cn.qssq666.robot.utils.EncodingDetect;
import cn.qssq666.robot.utils.LogUtil;
import cn.qssq666.robot.utils.QssqTaskFix;
import cn.qssq666.robot.utils.SPUtils;

/**
 * Created by luozheng on 2017/3/13.  qssq.space
 */

public class ImportWordActivity extends SuperActivity implements View.OnClickListener {

    private static final int REQUEST_CHOOSE_FILE = 2;
    private ActivityImportWordBinding binding;
    private AutoCompleteTextView evWordPath;
    private TextView tvLogView;
    private boolean mCanncel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_import_word);
        setSupportActionBar(binding.toolbar);
        binding.btnSubmit.setText("????????????");
        binding.btnSubmit.setOnClickListener(this);
        binding.getRoot().findViewById(R.id.btn_tab_path).setOnClickListener(this);
        binding.btnSelectPath.setOnClickListener(this);
        binding.btnSmartReadFlag.setOnClickListener(this);
        evWordPath = ((AutoCompleteTextView) findViewById(R.id.ev_wordtext_path));

        evWordPath.setText(SPUtils.getString(this, AppConstants.CONFIG_WORD_IMPORT_PATH));
        binding.evSplitFlag.setText(SPUtils.getString(this, AppConstants.CONFIG_WORDS_SPLIT_FLAG));
        binding.evAskSplitFlag.setText(SPUtils.getString(this, AppConstants.CONFIG_ASK_AND_ANASWER_SPLIT_FLAG));
        evWordPath.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                File file = new File(s + "");
                SPUtils.setValue(AppContext.getInstance(), AppConstants.CONFIG_WORD_IMPORT_PATH, file.getAbsolutePath());
                updateFileExistFlag(file.getAbsoluteFile(), evWordPath);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        binding.evSplitFlag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SPUtils.setValue(AppContext.getInstance(), AppConstants.CONFIG_WORDS_SPLIT_FLAG,s+"");

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        binding.evAskSplitFlag.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SPUtils.setValue(AppContext.getInstance(), AppConstants.CONFIG_ASK_AND_ANASWER_SPLIT_FLAG, ""+s);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        tvLogView = ((TextView) binding.getRoot().findViewById(R.id.tv_log_view));


    }

    public static String formatVar(String str) {

        String current;
        current = "[rnrn]";
        if (str.contains(current)) {

            return str.replace(current, "\r\n\r\n");
        }
        current = "[rn]";
        if (str.contains(current)) {
            return str.replace(current, "\r\n");
        }
        current = "[n]";

        if (str.contains(current)) {
            return str.replace(current, "\n");
        }
        return str;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:


            {
                tvLogView.setText("");

                final String wordSplit = formatVar(binding.evSplitFlag.getText().toString());
                if (TextUtils.isEmpty(wordSplit)) {
                    AppContext.showToast("???????????????????????????????????????");
                    return;
                }

                final String askAndAnswerSplit = formatVar(binding.evAskSplitFlag.getText().toString());
                if (TextUtils.isEmpty(askAndAnswerSplit)) {
                    AppContext.showToast("?????????????????????????????????");
                    return;
                }
                final File file = new File(evWordPath.getText().toString());
                if (!file.exists()) {
                    AppContext.showToast("???????????????????????????,????????????/?????????" + file.getName() + "?????????");
                    return;
                } else {

                    final ProgressDialog progressDialog = DialogUtils.getProgressDialog(this);
                    progressDialog.show();
                    progressDialog.setCancelable(true);
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mCanncel = true;
                        }
                    });


                    new QssqTaskFix<Object, Object>(new QssqTaskFix.ICallBackImp() {

                        @Override
                        public void onSetQssqTask(QssqTaskFix taskFix) {
                            super.onSetQssqTask(taskFix);
                        }


                        @Override
                        public Object onRunBackgroundThread(Object[] params) {


                            List<ReplyWordBean> wordBeans = new ArrayList<>();
                            try {


                                String javaEncode = EncodingDetect.getJavaEncode(file.getAbsolutePath());
                                if (javaEncode != null) {
                                    javaEncode = "gbk";
                                }


                                BufferedReader bre = new BufferedReader(new InputStreamReader(new FileInputStream(file), javaEncode));//??????????????????bre??????????????????????????????

                                String str;
                                boolean isMultiMode = (binding.evSplitFlag.getText().toString().equals("[rnrn]") && binding.evAskSplitFlag.getText().toString().equals("[rn]"))
                                        || (binding.evSplitFlag.getText().toString().equals("[nn]") && binding.evAskSplitFlag.getText().toString().equals("[n]"));


                                if (isMultiMode || binding.evSplitFlag.getText().toString().endsWith("n]")) {

                                    ReplyWordBean replyWordBean = null;
                                    int line = 1;
                                    while ((str = bre.readLine()) != null) // ????????????????????????????????????????????????


                                    {


                                        if (isMultiMode) {
                                            if (TextUtils.isEmpty(str)) {

                                                replyWordBean = new ReplyWordBean();
                                                wordBeans.add(replyWordBean);

                                            } else {


                                                if (line == 1) {
                                                    replyWordBean = new ReplyWordBean();
                                                    wordBeans.add(replyWordBean);
                                                }

                                                if (TextUtils.isEmpty(replyWordBean.getAsk())) {
                                                    replyWordBean.setAsk(str);
                                                } else if (TextUtils.isEmpty(replyWordBean.getAnswer())) {
                                                    replyWordBean.setAnswer(str);
                                                } else {
                                                    LogUtil.writeLog("????????????" + "?????????" + line + ":" + str);
                                                }
                                            }
                                        } else if (binding.evSplitFlag.getText().toString().equals("[n]") || binding.evSplitFlag.getText().toString().equals("[rn]")) {
                                            replyWordBean = new ReplyWordBean();

                                            String split = formatVar(binding.evAskSplitFlag.getText().toString());
                                            if (TextUtils.isEmpty(str)) {
                                                LogUtil.writeLog("?????????" + str);
                                                continue;
                                            }
                                            String[] words = str.split(split);
                                            if (words != null && words.length == 2) {

                                                replyWordBean.setAsk(words[0]);
                                                replyWordBean.setAnswer(words[1]);


                                            } else {
                                                if (words == null) {
                                                    throw new RuntimeException("???????????????" + line + ":" + str + ",??????????????????");
                                                } else {
                                                    throw new RuntimeException("???????????????" + line + ":" + str + ",???????????????????????????????????????2????????????" + words.length);
                                                }
                                            }


                                        }

                                        LogUtil.writeLog("read line " + line + ":" + str);
                                        line++;


                                        getQssqTask().publishProgressProxy("??????" + str);


                                        if (mCanncel) {
                                            return null;
                                        }
                                    }


                                } else {


                                    getQssqTask().publishProgressProxy("?????????????????????????????????,?????????????????????????????????,??????????????????");
                                    String strs = FileUtils.readFileToString(file, javaEncode);


                                    String[] splitWord = strs.split(wordSplit);
                                    getQssqTask().publishProgressProxy("?????????????????????" + splitWord + "???");
                                    for (int i = 0; i < splitWord.length; i++) {

                                        String currentWord = splitWord[i];
                                        getQssqTask().publishProgressProxy("?????????" + (1 + i) + "???\n" + currentWord + "");
                                        String[] askAndAswerSplit = currentWord.split(askAndAnswerSplit);

                                        if (askAndAswerSplit != null && askAndAswerSplit.length == 2) {
                                            ReplyWordBean replyWordBean = new ReplyWordBean();
                                            replyWordBean.setAsk(askAndAswerSplit[0]);
                                            replyWordBean.setAnswer(askAndAswerSplit[1]);
                                        } else {
                                            if (askAndAswerSplit == null) {
                                                throw new RuntimeException("???????????????" + (1 + i) + ":" + currentWord + ",??????????????????");
                                            } else {
                                                throw new RuntimeException("???????????????" + (1 + i) + ":" + currentWord + ",???????????????????????????????????????2????????????" + askAndAswerSplit.length);
                                            }
                                        }

                                    }
                                }


                            } catch (Exception e) {

                                return e;

                            }
                            StringBuilder sb = new StringBuilder();

                            if (wordBeans != null) {


                                getQssqTask().publishProgressProxy("?????????????????????");

                                try {

                                    for (int i = 0; i < wordBeans.size(); i++) {

                                        ReplyWordBean wordBean = wordBeans.get(i);

                                        if(wordBean==null ||wordBean.getAnswer()==null || wordBean.getAsk()==null){
                                            sb.append("[????????????]??? "+(1+i)+"?????????->"+wordBean+"\n");
                                            continue;
                                        }
                                        ReplyWordBean dbBean = DBHelper.getKeyWordDBUtil(AppContext.getDbUtils()).queryByColumn(ReplyWordBean.class, FieldCns.ASK, wordBean.getAsk().toString());

                                        if (dbBean != null) {

                                            if (dbBean.getAnswer().equals(dbBean.getAnswer())) {
                                                getQssqTask().publishProgressProxy("???" + (1 + i) + "??????????????????");
                                                sb.append("[?????????]???:" + wordBean.getAsk() + "???????????????" + dbBean.getAnswer() + "?????????\n");

                                            } else {
                                                dbBean.setAnswer(wordBean.getAnswer() + "," + wordBean.getAnswer());
                                                long result = DBHelper.getKeyWordDBUtil(AppContext.getDbUtils()).update(dbBean);

                                                if (result > 0) {
                                                getQssqTask().publishProgressProxy("?????????" + (1 + i) + "???????????????");
                                                    sb.append("[????????????]???:" + wordBean.getAsk() + "?????????,???????????????" + dbBean.getAnswer() + "\n");

                                                } else {

                                                getQssqTask().publishProgressProxy("?????????" + (1 + i) + "???????????????");
                                                    sb.append("[????????????]???:" + wordBean.getAsk() + "?????????,???????????????" + dbBean.getAnswer() + "\n");
                                                }
                                            }

                                        } else {

                                            long result = DBHelper.getKeyWordDBUtil(AppContext.getDbUtils()).insert(wordBean);

                                            if (result > 0) {
                                            getQssqTask().publishProgressProxy("?????????" + (1 + i) + "???????????????");
                                                sb.append("[????????????]???:" + wordBean.getAsk() + ",???" + wordBean.getAnswer() + "?????????\n");

                                            } else {

                                            getQssqTask().publishProgressProxy("?????????" + (1 + i) + "???????????????");
                                                sb.append("[????????????]???:" + wordBean.getAsk() + ",???" + wordBean.getAnswer() + "?????????\n");
                                            }
                                        }


                                    }

                                    return sb.toString();
                                } catch (Exception e) {
                                    Log.e("????????????", "??????????????????", e);
                                    return e;
                                }


                            } else {
                                return "????????????";
                            }


                        }

                        @Override
                        public void onRunFinish(Object o) {


                            if (o instanceof String) {
                                tvLogView.setText(o+"");


                                WordEvent wordEvent=new WordEvent();
                                wordEvent.setAll(true);
                                EventBus.getDefault().post(wordEvent);
                                DialogUtils.showDialog(ImportWordActivity.this, o + "");



                            }
                            if (o instanceof List) {

                                List<ReplyWordBean> wordBeans = (List<ReplyWordBean>) o;
                                for (ReplyWordBean wordBean : wordBeans) {
                                    LogUtil.writeLog("dump??????", wordBean + "");

                                }

                            } else if (o instanceof Exception) {
                                tvLogView.setText("????????????"+Log.getStackTraceString((Throwable) o));
                                DialogUtils.showDialog(ImportWordActivity.this, "????????????\n" + ((Exception) o).getMessage());
                            }
                            progressDialog.dismiss();

                        }


                        @Override
                        public void onProgressUpdate(Object o) {
                            progressDialog.setMessage(o + "");

                        }
                    }).execute();


                }

            }
            break;


            case R.id.btn_smart_read_flag:

            {


                final File file = new File(evWordPath.getText().toString());
                if (!file.exists()) {
                    AppContext.showToast("???????????????????????????,????????????/?????????" + file.getName() + "?????????");
                    return;
                } else {


                    final ProgressDialog progressDialog = DialogUtils.getProgressDialog(this);
                    progressDialog.show();
                    progressDialog.setCancelable(true);
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mCanncel = true;
                        }
                    });


                    new QssqTask<>(new QssqTask.ICallBack<CheckSplitInfo>() {
                        @Override
                        public CheckSplitInfo onRunBackgroundThread() {
                            final CheckSplitInfo checkSplitInfo = new CheckSplitInfo();
                            int line = 1;

                            try {

                                //{"_id":{"$oid":"593a63cc38b976fd719e15fe"},"account":"994957859","info":"a860046034431553","count":0,"createdAt":{"$date":"2017-06-09T09:01:00.317Z"},"updatedAt":{"$date":"2017-06-09T09:01:00.317Z"}}
                                // {"_id":{"$oid":"593a63cc45b2d9b83c3ba0f2"},"account":"994957859","info":"a860046034431553","count":0,"createdAt":{"$date":"2017-06-09T09:01:00.726Z"},"updatedAt":{"$date":"2017-06-09T09:01:00.726Z"}}


                                String javaEncode = EncodingDetect.getJavaEncode(file.getAbsolutePath());
                                if (javaEncode != null) {
                                    javaEncode = "gbk";
                                }
                                BufferedReader breFirst = new BufferedReader(new InputStreamReader(new FileInputStream(file), javaEncode));//??????????????????bre??????????????????????????????

                                long readLength = file.length() > 6000 ? 6000 : file.length();
                                char[] tt = new char[(int) readLength];
                                int read = breFirst.read(tt, 0, (int) readLength);
                                String readTmpStr = "";
                                if (read != -1) {
                                    readTmpStr = String.valueOf(tt);

                                }

                                LogUtil.writeLog("?????????????????????" + readTmpStr);


                                BufferedReader bre = new BufferedReader(new InputStreamReader(new FileInputStream(file), javaEncode));
                                //new BufferedReader(new FileInputStream(file), "UTF-8"));//??????????????????bre??????????????????????????????
//                                BufferedReader bre = new BufferedReader(new FileReader(file));//??????????????????bre??????????????????????????????

                                String str;


                                boolean lastLineIsEmpty = false;
                                while ((str = bre.readLine()) != null) // ????????????????????????????????????????????????
                                {


                                    if (mCanncel) {
                                        break;
                                    }

                                    LogUtil.writeLog("line" + line + ":" + str);

//                                    FileUtils.read


                                    if (lastLineIsEmpty) {


                                        {
                                            int rnIndex = readTmpStr.indexOf("\r\n\r\n");

                                            if (rnIndex == -1) {

                                                rnIndex = readTmpStr.indexOf("\r\n");
                                                if (rnIndex != -1) {
                                                    checkSplitInfo.wordSplit = "[rn]";

                                                }

                                            } else {

                                                checkSplitInfo.wordSplit = "[rnrn]";
                                            }


                                        }


                                        {


                                            int rnIndex = readTmpStr.indexOf("\r\n");

                                            if (rnIndex == -1) {

                                                rnIndex = readTmpStr.indexOf("\n");
                                                if (rnIndex != -1) {
                                                    checkSplitInfo.askanswerSplit = "[n]";

                                                }

                                            } else {

                                                checkSplitInfo.askanswerSplit = "[rn]";
                                            }
                                            if (checkSplitInfo.wordSplit != null) {

                                                break;
                                            }
                                        }


                                    } else {

                                        if (str.indexOf(",") != -1) {
                                            checkSplitInfo.askanswerSplit = ",";
                                        } else if (str.indexOf(",") != -1) {
                                            checkSplitInfo.askanswerSplit = "|";
                                        } else if (str.indexOf("-") != -1) {
                                            checkSplitInfo.askanswerSplit = "-";
                                        } else {


                                        }


                                        int i = str.indexOf("\n");
                                        if (i != -1) {

                                            if (i < readTmpStr.indexOf(str)) {

                                                checkSplitInfo.wordSplit = "[n]";
                                            }


                                        }

                                        i = str.indexOf("\r\n");
                                        if (i != -1) {

                                            if (i < readTmpStr.indexOf(str)) {

                                                checkSplitInfo.wordSplit = "[rn]";
                                            }


                                        }

                                    }


                                    if (TextUtils.isEmpty(str)) {
                                        lastLineIsEmpty = true;
                                    }

                                    line++;

                                    if (line > 8) {
                                        break;
                                    }
                                }
                                ;
                            } catch (IOException e) {
                            }
                            return checkSplitInfo;


                        }

                        @Override
                        public void onRunFinish(CheckSplitInfo o) {

                            progressDialog.dismiss();
                            if (o.wordSplit == null) {
                                AppContext.showToast("???????????????????????????");
                            } else if (o.askanswerSplit == null) {
                                AppContext.showToast("????????????????????????????????????");
                            } else {
                                binding.evSplitFlag.setText(o.wordSplit);
                                binding.evAskSplitFlag.setText(o.askanswerSplit);

                                AppContext.showToast("????????????,????????????????????????" + o.wordSplit + "\n????????????????????????:" + o.askanswerSplit);
                            }


                        }

                    }).execute();


                }

            }
            break;


            case R.id.btn_tab_path:

            {

                File file = new File(evWordPath.getText().toString());
                if (!file.exists()) {
                    AppContext.showToast("???????????????????????????,????????????/?????????" + file.getName() + "?????????");
                } else {
                    File parentFile = file.isFile() ? file.getParentFile() : file;

                    File[] list = parentFile.listFiles();
                    if (list != null && list.length > 0) {
                        if (lastTabPosition < list.length) {


                        } else {
                            if (list.length == 1) {
                                evWordPath.setError("??????????????????");
                            }
                            lastTabPosition = 0;
                        }
                    } else {
                        AppContext.showToast("??????" + parentFile.getName() + "?????????????????????");
                        ;
                        return;
                    }

                    evWordPath.setText(list[lastTabPosition].getAbsolutePath());
                    lastTabPosition++;

                }
            }
            break;
            case R.id.btn_select_path:
                AppUtils.chooseFile(this, REQUEST_CHOOSE_FILE);
                break;

        }

    }

    private static int lastTabPosition = 0;

    protected static void updateFileExistFlag(File file, AutoCompleteTextView completeTextView) {

        if (file != null && file.exists()) {
            if (file.isFile()) {
                completeTextView.setError(null);

            } else {
                completeTextView.setError("????????????");

            }
        } else {
            completeTextView.setError("????????????");
        }
    }


    public static String parsePath(Context context, Uri uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        } else {
//        }else if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    String string = cursor.getString(column_index);
                    cursor.close();
                    return string;
                }
            } catch (Exception e) {
                // Eat it
            }
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the Uri of the selected file
                Uri uri = data.getData();
                if (uri == null) {
                    AppContext.showToast("???????????????????????????,???????????????");
                    return;
                }

                String path = parsePath(this, uri);
                if (path == null) {
                    AppContext.showToast("????????????????????????:Uri: " + uri + "");
                    return;
                }


                lastTabPosition = 0;
                evWordPath.setText(path);

            } else {
            }


            super.onActivityResult(requestCode, resultCode, data);
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_word_import_help, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help_install_plugin) {
            DialogUtils.showDialog(this,"??????????????????????????????????????? \\r\\n?????????[rn]??????,???\\n??????[n]??????,???????????? ??????????????????,????????????????????????????????????????????????,???????????????????????????!\n????????????????????????????????????????????????,????????????????????????????????????????????????????????????,???????????????????????????,????????? ??????qq????????? ????????????qq??? $u ,????????????????????????????????????$robotname ???????????????$username ????????????????????????????????????,??????????????????????????????????????????" );
            return true;
        }else if(id==R.id.action_suggest) {
            DialogUtils.showDialog(this,"?????????????????????????????????,???????????????????????????????????????,?????????????????????????????????????????????????????????????????????,???????????????????????????" );
            return true;

        }

        return super.onOptionsItemSelected(item);
    }
}
