package cn.qssq666.robot.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import cn.qssq666.robot.R;
import cn.qssq666.robot.adapter.CodeSymobolAdapter;
import cn.qssq666.robot.adapter.DefaultAdapter;
import cn.qssq666.robot.app.AppContext;
import cn.qssq666.robot.bean.CodeSymobolBean;
import cn.qssq666.robot.business.RobotContentProvider;
import cn.qssq666.robot.constants.AppConstants;
import cn.qssq666.robot.constants.Cns;
import cn.qssq666.robot.constants.IntentCns;
import cn.qssq666.robot.databinding.ActivityEditScriptBinding;
import cn.qssq666.robot.event.RefreshEvent;
import cn.qssq666.robot.ide.LanguageCode;
import cn.qssq666.robot.ide.interfaces.IDEApi;
import cn.qssq666.robot.interfaces.INotify;
import cn.qssq666.robot.plugin.lua.util.LuaPluginUtil;
import cn.qssq666.robot.receiver.RunCodeReceiver;
import cn.qssq666.robot.utils.AppUtils;
import cn.qssq666.robot.utils.DialogUtils;
import cn.qssq666.robot.utils.LogUtil;
import cn.qssq666.robot.utils.QssqTaskFix;

/**
 * Created by qssq on 2018/11/14 qssq666@foxmail.com
 */
public abstract class BaseEditCodeActivity extends SuperActivity {

    private static final String TAG_ = "EditCodeActivity";
    protected ActivityEditScriptBinding binding;
    protected String _path;
    protected boolean mEnableRun = true;
    protected Uri uri;
    private IDEApi _IDE;
    private RunCodeReceiver runCodeReceiver;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.w(TAG_, "onNewIntent");

    }

    private static ArrayList getWindowViews() {
        try {
            View rootView = null;
            Class wmgClass = Class.forName("android.view.WindowManagerGlobal");
            Object wmgInstnace = wmgClass.getMethod("getInstance").invoke(null, (Object[]) null);
            Field mViewsField = wmgClass.getDeclaredField("mViews");
            mViewsField.setAccessible(true);
            ArrayList o = (ArrayList) mViewsField.get(wmgInstnace);
            return o;

//            private final ArrayList<View> mViews = new ArrayList<View>();

          /*  Method getViewRootNames = wmgClass.getMethod("getViewRootNames");
            Method getRootView = wmgClass.getMethod("getRootView", String.class);
            String[] rootViewNames = (String[])getViewRootNames.invoke(wmgInstnace, (Object[])null);

            for(String viewName : rootViewNames) {
                rootView = (View)getRootView.invoke(wmgInstnace, viewName);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_script);
        View codeView = onCreateCodeView(binding.codeviewContainer);
        _IDE = (IDEApi) codeView;
        binding.codeviewContainer.addView(codeView);
           /*     <cn.qssq666.robot.ui.CodeView
            android:id="@+id/codeview"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:layout_weight="1">

        </cn.qssq666.robot.ui.CodeView>
*/

        binding.toolbar.setTitle("????????????");
        runCodeReceiver = new RunCodeReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

//                getWindowManager().getDefaultDisplay().
//                ViewGroup decorView1 = (ViewGroup) decorView;
                List<View> windowViews = getWindowViews();
                if (windowViews != null && windowViews.size() > 0 && windowViews.get(windowViews.size() - 1) != getWindow().getDecorView()) {
//                    BaseEditCodeActivity.super.onBackPressed();
                    View view = windowViews.get(windowViews.size() - 1); //parent = android.view.ViewRootImpl
//                    ViewRootImpl
                    getWindowManager().removeView(view);
                }

                if (Cns.RUN_CODE_BROADCAST.equals(intent.getAction())) {
                    execRun();
                    Toast.makeText(context, "????????????..", Toast.LENGTH_SHORT).show();
                } else if (Cns.RUN_SIMULATOR_CODE_BROADCAST.equals(intent.getAction())) {
                    execSimulator();
                    Toast.makeText(context, "??????????????????..", Toast.LENGTH_SHORT).show();
                }
            }
        }; //getWindowManager=WindowManagerImpl ?????? ????????? mGlobal =WindowManagerGlobal????????? ->mParams??????????????????wrapxwrap?????????????????? mRoots  mViews????????????????????????????????????????????? ????????????.
        IntentFilter filter = new IntentFilter();
        filter.addAction(Cns.RUN_CODE_BROADCAST);
        filter.addAction(Cns.RUN_SIMULATOR_CODE_BROADCAST);
        this.registerReceiver(runCodeReceiver, filter);
//this.getWindowManager().
        CodeSymobolAdapter adapter = new CodeSymobolAdapter();
        adapter.setList(getDefaultSource());
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerview.setAdapter(adapter);
        adapter.setOnItemClickListener(new DefaultAdapter.OnItemClickListener<CodeSymobolBean>() {
            @Override
            public void onItemClick(CodeSymobolBean model, View view, int position) {
                if (model.getCodeLocation() == CodeSymobolBean.CodeLocation.LINE_START) {
//                        getCodeView().setSelection(, );
                } else if (model.getCodeLocation() == CodeSymobolBean.CodeLocation.LINE_END) {

                }
                String content = model.getContent();
                Log.w(TAG_, "???????????????:" + content);
                getCodeView().requestCodeFocus();
                getCodeView().paste(content);
            }
        });
        setSupportActionBar(binding.toolbar);
        Log.w(TAG_, "onCreate");

        Intent intent = getIntent();
        if (intent != null) {
            _path = intent.getStringExtra(IntentCns.INTENT_FILE_PATH);
            if (_path != null) {

                loadLuaFileFromPath();

            } else {
                uri = intent.getData();
                if (uri != null) {

                    String scheme = uri.getScheme();
                    Cursor query = getContentResolver().query(uri, null, null, null, null);
                    if (query == null) {
                        final String path = uri.getPath();
                        openStreamBy(uri, path);
                        AppContext.showToast("?????????????????????????????????????????????????????????!");
                        return;
                    }
                    Cursor returnCursor = query;
                    /*
                     * Get the column indexes of the data in the Cursor,
                     * move to the first row in the Cursor, get the data,
                     * and display it.
                     */
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    String name = returnCursor.getString(nameIndex);
                    long size = returnCursor.getLong(sizeIndex);
                    returnCursor.close();
                    Log.w(TAG_, "?????????:" + name + ",size:" + Formatter.formatFileSize(AppContext.getInstance(), size));

//                if (uri == null || !"content".equals(scheme)) {

                    String pathTmp = AppUtils.getRealFilePath(this, uri);

                    final String path = uri.getPath();

//https://developer.android.google.cn/training/sharing/receive
                    //https://developer.android.google.cn/training/secure-file-sharing/retrieve-info
                    if (!TextUtils.isEmpty(pathTmp) && new File(pathTmp).exists()) {
                        _path = pathTmp;
                        Log.w(TAG_, "??????:" + pathTmp + ",uri:" + uri + ",sccheme:" + scheme);
                        loadLuaFileFromPath();
                        return;
                    } else {


                        if (size > 1024 * 1024 * 2) {
                            DialogUtils.showConfirmDialog(this, "???????????????(" + Formatter.formatFileSize(AppContext.getInstance(), size) + ")???????????????????", new INotify<Void>() {
                                @Override
                                public void onNotify(Void param) {
                                    openStreamBy(uri, path);
                                }
                            });
                        }

                        openStreamBy(uri, path);


                    }
//                }
                }
            }

        }
    }

    abstract View onCreateCodeView(FrameLayout codeviewContainer);

    abstract protected List<CodeSymobolBean> getDefaultSource();

    private void openStreamBy(Uri uri, String path) {
        String currentPath = !path.contains(".") ? getExtName() : path;
        updateEnableCode(currentPath);


        final ProgressDialog progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setMessage("?????????..");
        progressDialog.show();
//                        uri.
        new QssqTaskFix<Uri, Object>(new QssqTaskFix.ICallBackImp<Uri, Object>() {
            @Override
            public Object onRunBackgroundThread(Uri... params) {

                try {
                    String strContentByUri = AppUtils.getStrContentByUri(BaseEditCodeActivity.this, params[0]);
                    return strContentByUri;
                } catch (Throwable e) {
                    return e;
                }
            }

            @Override
            public void onRunFinish(Object s) {
                progressDialog.dismiss();

                if (s != null && s instanceof String) {

                    setCode(s + "");
                    DialogUtils.showDialog(BaseEditCodeActivity.this, "??????????????????,????????????????????????????????????????????????" + getCurrentRobotPluginDir());
                } else if (s instanceof Exception) {

                    Toast.makeText(BaseEditCodeActivity.this, "??????????????????" + Log.getStackTraceString(((Exception) s)), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(BaseEditCodeActivity.this, "??????????????????", Toast.LENGTH_LONG).show();

                }
            }
        }).execute(uri);
    }

    protected abstract String getCurrentRobotPluginDir();

    private void loadLuaFileFromPath() {
        final File filePath = new File(_path);
        if (!filePath.exists()) {
            DialogUtils.showDialog(this, "??????" + _path + "?????????!");
        } else {
            if (filePath.length() > (1024 * 1024 * 2)) {//??????2M
                DialogUtils.showConfirmDialog(this, "????????????,(" + Formatter.formatFileSize(AppContext.getInstance(), filePath.length()) + ")???????????????????", new INotify<Void>() {
                    @Override
                    public void onNotify(Void param) {
                        openFile(filePath);
                    }
                });
            } else {
                openFile(filePath);

            }

        }
    }

    private void openFile(final File filePath) {
        String currentPath = _path == null ? "" : _path;
        updateEnableCode(currentPath);

        final ProgressDialog progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setMessage("?????????..");
        progressDialog.show();
        new QssqTaskFix<File, Object>(new QssqTaskFix.ICallBackImp<File, Object>() {
            @Override
            public Object onRunBackgroundThread(File... params) {

                try {
                    return FileUtils.readFileToString(filePath, "utf-8");
                } catch (Exception e) {

                    e.printStackTrace();
                    return e;
                }  catch (Error e) {
                    e.printStackTrace();
                    System.gc();
                    return e;
                }catch (Throwable e) {
                    e.printStackTrace();
                    return e;
                }
            }

            @Override
            public void onRunFinish(Object s) {
                progressDialog.dismiss();

                if (s != null && s instanceof String) {

                    setCode(s + "");
                } else if (s instanceof OutOfMemoryError) {
                    System.gc();
                    Toast.makeText(BaseEditCodeActivity.this, "????????????!,????????????????????????????????????! " + s, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BaseEditCodeActivity.this, "" + s, Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(new File(_path));
    }

    public void setCode(String s) {
        getCodeView().setTextCode(s);
        if (lastText == null) {
            lastText = s;

        }
    }

    protected IDEApi getCodeView() {
        return _IDE;
    }

    private String lastText;

    private void updateEnableCode(String currentPath) {
        if (currentPath.endsWith("lua")) {
            getCodeView().setLang(LanguageCode.LUA);
            mEnableRun = true;

        } else if (currentPath.endsWith("js") || currentPath.endsWith("jsx") || currentPath.endsWith("es") || currentPath.endsWith("es6")) {

            getCodeView().setLang(LanguageCode.JAVASCRIPT);
            mEnableRun = true;
        } else if (currentPath.endsWith("java")) {

            getCodeView().setLang(LanguageCode.JAVA);
            mEnableRun = false;
        } else if (currentPath.endsWith("c")) {

            getCodeView().setLang(LanguageCode.C);
            mEnableRun = false;
        } else if (currentPath.endsWith("sh")) {
            getCodeView().setLang(LanguageCode.SHELL);
            mEnableRun = false;
        } else if (currentPath.endsWith("smali")) {
            mEnableRun = false;
            getCodeView().setLang(LanguageCode.SHELL);
        } else if (currentPath.endsWith("apk") || currentPath.endsWith("mp4") || currentPath.endsWith("mp3") || currentPath.endsWith("avi")) {
            mEnableRun = false;
            getCodeView().setLang(LanguageCode.NONE);
        } else if (currentPath.endsWith(".md")) {
            mEnableRun = false;
            getCodeView().setLang(LanguageCode.MARKDOWN);

        } else {
            File file = new File(currentPath);
            if (file.getName().contains("lua")) {
                getCodeView().setLang(LanguageCode.LUA);
                mEnableRun = false;
            } else if (file.getName().contains("js")) {
                getCodeView().setLang(LanguageCode.JAVASCRIPT);
                mEnableRun = true;
            } else if (!currentPath.contains("lua")) {
                getCodeView().setLang(LanguageCode.LUA);
                mEnableRun = false;
            } else {
                mEnableRun = true;
            }
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_reload) {

            if (_path == null) {
                Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                lastText = null;
                loadLuaFileFromPath();
                RobotContentProvider.getInstance().initLuaPlugin();
            }

            Toast.makeText(this, "????????????,???????????????..", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.action_format) {

            getCodeView().formatCode();
        } else if (id == R.id.action_run) {

            if (execRun()) return true;
            return true;
        } else if (id == R.id.action_save) {
            if (doCheckAndSave(new INotify() {
                @Override
                public void onNotify(Object param) {
                    EventBus.getDefault().post(new RefreshEvent().setUpdateUi(false));

                    Toast.makeText(BaseEditCodeActivity.this, "???????????????..", Toast.LENGTH_SHORT).show();
                }
            })) return true;
        } else if (id == R.id.action_insert) {

            String[] items = new String[]{"??????????????????", "??????????????????", "??????????????????????????????", "??????????????????"};
            DialogUtils.showMenuDialog(this, "??????", items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String string = doInsertCodeBy(which);

                    AppUtils.copy(AppContext.getInstance(), "" + string);
                    Toast.makeText(BaseEditCodeActivity.this, "????????????????????????????????????????????????????????????????????????????????????????????????!", Toast.LENGTH_SHORT).show();
                }
            });
//            getCodeView().

        } else if (id == R.id.action_simulation) {

            execSimulator();
        } else if (id == R.id.action_undo) {
            getCodeView().undo();
        } else if (id == R.id.action_jiqiao) {
            DialogUtils.showDialog(this, "??????????????????????????????????????????????????????????????????????????????????????????????????????[??????/??????/??????" + getCurrentScriptTypeName() + "???????????????]???!??????qq?????????????????????????????????????????????????????????QQ??????????????????????????????????????????/tencent/QQfile_recv????????????????????????????????????" + getCurrentScriptExNme() + "***.txt????????????qq???????????????,???????????????????????????!");
        } else if (id == R.id.action_as_plugin_parse) {

            item.setVisible(false);
            invalidateOptionsMenu();
            mEnableRun = true;
            setDefaultLanguage();
            setCode(getCodeView().getTextCode());
        } else if (id == R.id.action_rename) {
            if (_path == null || !new File(_path).exists()) {
                Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
            } else {

                final File srcFile = new File(_path);
                DialogUtils.showEditDialog(this, "??????????????????(???????????????)", "?????????", srcFile.getName(), new INotify<String>() {
                    @Override
                    public void onNotify(String param) {
                        File destFile = new File(srcFile.getParentFile(), param + "");
                        if (destFile.exists()) {
                            Toast.makeText(BaseEditCodeActivity.this, "??????????????????????????????!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            FileUtils.moveFile(srcFile, destFile);
                            Toast.makeText(BaseEditCodeActivity.this, "????????????!", Toast.LENGTH_SHORT).show();
                            _path = destFile.getAbsolutePath();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(BaseEditCodeActivity.this, "????????????" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }

                });
            }
        } else if (id == R.id.action_open_log) {

//                if(?????????????????????Activity.isInMultiWindowMode()??????????????????????????????????????????
            final Intent intent = new Intent(this, LogActivity.class);
            LogUtil.filterTag = "LUAEngine";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                //Activity.enterPictureInPictureMode()

                //????????????Activity.isInPictureInPictureMode()????????????????????????????????????)

//                Rect bounds = new Rect(500, 300, 0, 0);//xml????????????

// Set the bounds as an activity option. https://developer.android.google.cn/guide/topics/ui/multi-window

//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);//?????????????????? ??????????????????
//                ActivityOptions options = ActivityOptions.makeBasic();
                final BaseEditCodeActivity that = BaseEditCodeActivity.this;
                if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                    DialogUtils.showConfirmDialog(this, "?????????????????????????????????????????????", new INotify<Void>() {
                        @Override
                        public void onNotify(Void param) {
                            intent.putExtra(AppConstants.INTENT_FORM_CODE_VIEW, true);
                            startActivity(intent);

                        }
                    }, new INotify<Void>() {
                        @Override
                        public void onNotify(Void param) {
                            startActivity(intent);//??????????????????

                        }
                    });
                    return true;
                } else {
                    if (!this.isInMultiWindowMode()) {
                        Toast.makeText(this, "????????????????????????????????????????????????!", Toast.LENGTH_SHORT).show();
                    } else if (!this.isInPictureInPictureMode()) {
                        Toast.makeText(this, "???????????????????????????????????????????????????????????????!", Toast.LENGTH_SHORT).show();
                    }
                    startActivity(intent);

                }

//                options.setLaunchBounds(bounds);//??????????????????????????????????????????

            } else {
                Toast.makeText(this, "????????????android????????????????????????????????????????????????????????? ????????????!", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//???????????????????????????singleInstance????????????????

            return true;
        }
        return true;

    }

    protected abstract String getCurrentScriptExNme();

    protected abstract String getCurrentScriptTypeName();

    public abstract void execSimulator();

    public abstract String doInsertCodeBy(int which);

    public abstract boolean execRun();

    protected abstract void setDefaultLanguage();

    public boolean doCheckAndSave() {
        return doCheckAndSave(null);
    }

    public boolean doCheckAndSave(final INotify iNotify) {
        if (TextUtils.isEmpty(_path)) {
            AlertDialog alertDialog = DialogUtils.showEditDialog(this, "???????????????????????????", "??????", new INotify<String>() {
                @Override
                public void onNotify(String param) {
                    File file = new File(LuaPluginUtil.getDefaultPath(AppContext.getInstance()), param + getExtName());
                    if (file.exists()) {
                        DialogUtils.showDialog(BaseEditCodeActivity.this, "???????????????,????????????????????????!");
                    } else {
                        _path = file.getAbsolutePath();
                        AlertDialog alertDialog1 = DialogUtils.showDialog(BaseEditCodeActivity.this, "???????????????:" + file.getAbsolutePath() + "", "");
                        alertDialog1.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                callSave(iNotify);
                            }
                        });
                    }
                }
            });
            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (iNotify != null) {
                        iNotify.onNotify(false);
                    }
                }
            });
            return true;
        }

        callSave(iNotify);
        return false;
    }

    public void callSave() {
        callSave(null);
    }

    public void callSave(final INotify iNotify) {
        final ProgressDialog progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setMessage("?????????..");
        progressDialog.show();
        new QssqTaskFix<File, Object>(new QssqTaskFix.ICallBackImp<File, Object>() {
            @Override
            public Object onRunBackgroundThread(File... params) {

                try {
                    FileUtils.writeStringToFile(new File(_path), getCodeView().getTextCode(), "utf-8");

                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return e;
                }
            }

            @Override
            public void onRunFinish(Object s) {


                progressDialog.dismiss();
                if (s != null && s instanceof Boolean) {
                    lastText = getCodeView().getTextCode();
                    if (iNotify != null) {
                        iNotify.onNotify(true);
                        return;
                    }

                    Toast.makeText(BaseEditCodeActivity.this, "????????????,?????????????????????????????????!", Toast.LENGTH_SHORT).show();

                } else {
                    DialogUtils.showDialog(BaseEditCodeActivity.this, "????????????!" + Log.getStackTraceString((Throwable) s));
                }
            }
        }).execute(new File(_path));
    }

    protected String getExtName() {
        if (_path != null) {
            return FilenameUtils.getExtension(_path);
        } else if (uri != null) {
            String path = uri.getPath();
            return FilenameUtils.getExtension(path);
        }
        return ".lua";
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateMenu(menu);
        return super.onPrepareOptionsMenu(menu);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_code_edit, menu);

//        updateMenu(menu);
        return true;
    }

    public void updateMenu(Menu menu) {
        if (!mEnableRun) {
            menu.findItem(R.id.action_run).setVisible(false);
            menu.findItem(R.id.action_simulation).setVisible(false);
            menu.findItem(R.id.action_as_plugin_parse).setVisible(true);
        } else {

            menu.findItem(R.id.action_run).setVisible(true);
            menu.findItem(R.id.action_simulation).setVisible(true);
            menu.findItem(R.id.action_as_plugin_parse).setVisible(false);
        }

        if (_path == null) {
            menu.findItem(R.id.action_rename).setVisible(false);
            menu.findItem(R.id.action_reload).setVisible(false);
        } else {
            menu.findItem(R.id.action_rename).setVisible(true);


            menu.findItem(R.id.action_reload).setVisible(true);


        }


    }


    //TODO ??????????????????????????????

    /*

      at android.graphics.Paint.nGetTextAdvances(Native method)
  at android.graphics.Paint.measureText(Paint.java:1805)
  at cn.qssq666.robot.ide.mycode.android.FreeScrollingTextField.getCharAdvance(FreeScrollingTextField.java:872)
  at cn.qssq666.robot.ide.mycode.android.FreeScrollingTextField.getCharExtent(FreeScrollingTextField.java:1084)
  at cn.qssq666.robot.ide.mycode.android.FreeScrollingTextField.getBoundingBox(FreeScrollingTextField.java:1110)
     */

    @Override
    public void onBackPressed() {
        String text = getCodeView().getTextCode();
        if (text != null && lastText != null && !text.equals(lastText)) {
            DialogUtils.showConfirmDialog(this, "?????????????", new INotify<Void>() {
                @Override
                public void onNotify(Void param) {
                    doCheckAndSave(new INotify() {
                        @Override
                        public void onNotify(Object param) {

                            EventBus.getDefault().post(new RefreshEvent().setUpdateUi(true));
                            finish();
                        }
                    });
                }
            }, new INotify<Void>() {
                @Override
                public void onNotify(Void param) {
                    finish();
                }
            });
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(runCodeReceiver);
    }

}
