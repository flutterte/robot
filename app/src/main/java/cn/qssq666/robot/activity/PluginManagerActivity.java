package cn.qssq666.robot.activity;
import cn.qssq666.CoreLibrary0;import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.qssq666.robot.R;
import cn.qssq666.robot.activity.datamanager.BaseAccountManagerActivity;
import cn.qssq666.robot.adapter.ErrorPluginInterface;
import cn.qssq666.robot.app.AppContext;
import cn.qssq666.robot.asynctask.QssqTask;
import cn.qssq666.robot.bean.PluginBean;
import cn.qssq666.robot.business.RobotContentProvider;
import cn.qssq666.robot.constants.AccountType;
import cn.qssq666.robot.constants.Cns;
import cn.qssq666.robot.event.AccountAddOrChangeEvent;
import cn.qssq666.robot.interfaces.INotify;
import cn.qssq666.robot.plugin.PluginUtils;
import cn.qssq666.robot.plugin.util.QueryPluginModel;
import cn.qssq666.robot.selfplugin.IPluginHolder;
import cn.qssq666.robot.utils.AppUtils;
import cn.qssq666.robot.utils.DialogUtils;

/**
 * Created by qssq on 2018/1/21 qssq666@foxmail.com
 */

public class PluginManagerActivity extends BaseAccountManagerActivity<PluginBean> {

    private static final int REQUEST_CODE_SELECT_PLUGIN = 1;
    private boolean mChanage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_plugin_activity, menu);
        return true;
    }

    @Override
    protected int deleteAllFromDb() {
        return PluginUtils.deleteAllPlugin(AppContext.getInstance());
    }

    boolean mNeedUpdate = false;

    @Override
    protected boolean onClickMenu(MenuItem item, int id) {
        if (id == R.id.action_refresh) {
            doDataChanageSuccEvent();
            AppContext.showToast("????????????");
        } else if (id == R.id.action_download_plugin) {

            AppUtils.openWebView(PluginManagerActivity.this, Cns.PLUGIN_DOWNLOAD);

        } else if (id == R.id.action_sdk_help) {//  sdk ??????????????????
            AppUtils.openWebView(PluginManagerActivity.this, Cns.SDK_DEVELOPER_JIANSHU_URL);

        } else if (id == R.id.action_download_plugin_sdk) {//????????????sdk
            AppUtils.openWebView(PluginManagerActivity.this, Cns.SDK_DOWNLOAD_URL);

        } else if (id == R.id.action_help_install_plugin) {//??????????????????
            AppUtils.openWebView(PluginManagerActivity.this, Cns.HELPER_URL);

        } else if (id == R.id.action_plugin_dir) {
            AppContext.showToast("????????????:" + PluginUtils.getDefaultPluginApkPath(this));
        }


    /*

        <item
        android:id="@+id/action_download_plugin_sdk"
        android:orderInCategory="100"
        android:title="????????????SDK"
        app:showAsAction="never" />
   <item
        android:id="@+id/action_download_plugin"
        android:orderInCategory="100"
        android:title="????????????"
        app:showAsAction="ifRoom" />
    <item
        android:id="@+id/action_sdk_help"
        android:orderInCategory="100"
        android:title="??????SDK????????????"
        app:showAsAction="never" />

    <item
        android:id="@+id/action_help_install_plugin"
        android:orderInCategory="100"
        android:title="??????????????????"
        app:showAsAction="never" />


    */
        return true;
    }


    @Override
    protected void onQueryFinish() {
        super.onQueryFinish();
    }

    @Override
    protected int getAdapterType() {
        return AccountType.TYPE_PLUGIN;
    }


    @Override
    protected void doSubmitInsertSuccEvent(PluginBean bean) {
        AccountAddOrChangeEvent event = new AccountAddOrChangeEvent();
        event.setBean(bean);
        event.setType(AccountType.TYPE_PLUGIN);
        event.setPosition(0);
        EventBus.getDefault().post(event);
    }


    @Override
    protected boolean needInterceptLongClick(PluginBean bean, int position) {
        if (bean.getPluginHolder() != null && bean.getPluginHolder().getPluginInterface() instanceof ErrorPluginInterface) {
            final ErrorPluginInterface errorPluginInterface = ((ErrorPluginInterface) bean.getPluginHolder().getPluginInterface());

            final String msg = errorPluginInterface.getErrorMsg();
            DialogUtils.showConfirmDialog(this, "?????????????????????,?????????????????????????????????????????????????????????????????????(?????????????????????,??????????????????????????????????????????????????????????????????)\n??????????????????:" + errorPluginInterface.getErrorFile() + "\n" + msg, "??????????????????", "??????????????????", new INotify<Void>() {
                @Override
                public void onNotify(Void param) {
                    try {
                        Intent shareFileIntent = AppUtils.getShareFileIntent(errorPluginInterface.getErrorFile());
                        PluginManagerActivity.this.startActivity(shareFileIntent);


                    } catch (Exception e) {
                        AppContext.showToast("??????????????????" + e.getMessage());
                    }
                }
            }, new INotify<Void>() {
                @Override
                public void onNotify(Void param) {
                    AppUtils.copy(PluginManagerActivity.this, msg);
                    Toast.makeText(PluginManagerActivity.this, "???????????????", Toast.LENGTH_SHORT).show();

                }
            });
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_PLUGIN) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the Uri of the selected file
                Uri uri = data.getData();
                String path = null;
                try {
                    path = AppUtils.getPath(this, uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "??????????????????,??????????????????????????????", Toast.LENGTH_SHORT).show();
                    return;
                }
                final File file = new File(path);
                if (!file.exists()) {
                    AppContext.showToast("???????????????????????????");

                } else {
                    if (!file.getAbsolutePath().endsWith("apk")
                            && !file.getAbsolutePath().endsWith("zip")
                            && !file.getAbsolutePath().endsWith("jar")
                            && !file.getAbsolutePath().endsWith("qssq")
                            && !file.getAbsolutePath().endsWith("dex")) {

                        AppContext.showToast("???????????????????????????????????????????????????,????????????apk,jar,zip,qssq.dex????????????");
                    } else if (!file.exists()) {
                        AppContext.showToast("???????????????");
                    } else {
                        final File targetFile = new File(PluginUtils.getDefaultPluginApkPath(AppContext.getInstance()), file.getName());
                        if (targetFile.exists()) {
                            targetFile.delete();
                            AppContext.showToast("?????????????????????????????????,????????????");
                        }
                        new QssqTask<Object>(new QssqTask.ICallBack() {
                            @Override
                            public Object onRunBackgroundThread() {
                                try {
                                    FileUtils.copyFile(file, targetFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return e;
                                }
                                return true;
                            }

                            @Override
                            public void onRunFinish(Object o) {
                                if (o instanceof Exception) {
                                    AppContext.showToast("???????????? " + ((Exception) o).getMessage());
                                } else {
                                    mChanage = true;
                                    queryData();
                                }
                            }
                        }).execute();
                    }

                }
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onAddItemClick() {
        AppUtils.chooseFile(this, REQUEST_CODE_SELECT_PLUGIN);

    }

    @Override
    protected String[] getLongPressMenu() {
        return new String[]{"??????",
                "??????",
                "??????",
                "????????????(?????????)"
                , "????????????????????????"
        };
    }

    @Override
    protected void onLongOtherClick(int position, int which) {
        PluginBean bean = adapter.getList().get(position);


        switch (which) {
            case 2:

                Intent shareFileIntent = AppUtils.getShareFileIntent(new File(bean.getPath()));
                try {
                    this.startActivity(shareFileIntent);

                } catch (Exception e) {
                    AppContext.showToast(e.getMessage() + "?????????????????????,???????????????");
                }
                break;
            case 3:
                try {

                    AppUtils.lauchApp(this, bean.getPackageName());


                } catch (Exception e) {
                    AppContext.showToast(this, "????????????,????????????" + bean.getPackageName() + "????????????(?????????????????????,??????????????????????????????????????????.??????????????????????????????????????????)");
                }
                break;
            case 4: {
                FrameLayout rootView = new FrameLayout(this);
                try {
                    View ui = bean.getPluginInterface().onConfigUi(rootView);
                    if (ui == null) {
                        AppContext.showToast(this, "????????????????????????UI????????????");
                        return;
                    }


                    Dialog dialog = new Dialog(this);
                    rootView.setBackgroundColor(Color.WHITE);
                    rootView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    //????????????????????????
                    Window dialogWindow = dialog.getWindow();


                    dialogWindow.setBackgroundDrawable(new ColorDrawable(this.getResources().getColor(android.R.color.transparent)));
                    WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                    dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
                    lp.width = LinearLayout.LayoutParams.MATCH_PARENT;
                    lp.height = LinearLayout.LayoutParams.MATCH_PARENT;
                    lp.gravity = Gravity.TOP;

                    dialogWindow.setAttributes(lp);
                    //???????????????
                    rootView.addView(ui, 0);//?????????????????????????????????
                    dialog.setContentView(rootView);
                    dialog.show();

                } catch (Throwable e) {

                    DialogUtils.showDialog(PluginManagerActivity.this, "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????,????????????\n " + Log.getStackTraceString(e));
                }
            }

            break;
        }
    }


    @Override
    protected void onLongEditClick(int position) {
        AppContext.showToast(this, "??????????????????");
    }


    @Override
    protected long onInsertToDb(PluginBean bean) {
        return 0;
    }


    @Override
    protected void doDataChanageSuccEvent() {
       /* AccountAddOrChangeEvent event = new AccountAddOrChangeEvent();
        event.setType(AccountType.TYPE_PLUGIN);
        EventBus.getDefault().post(event);*/
    }

    @Override
    protected int onDeleteToDb(PluginBean bean) {
        return PluginUtils.deletePlugin(bean.getPluginInterface(), bean.getPath());
    }

    protected void doOnRefresh() {
        mChanage = true;
        queryData();
    }

    @Override
    public void queryData() {
        if (mChanage) {

            mChanage = false;
            final ProgressDialog progressDialog = DialogUtils.getProgressDialog(this);
            progressDialog.setTitle("?????????");
            progressDialog.show();
            RobotContentProvider.getInstance().initJAVAPlugin(new INotify() {
                @Override
                public void onNotify(Object param) {
                    progressDialog.dismiss();
                    reLoadPlugin();
                }
            });
        } else {

            reLoadPlugin();
        }
    }

    private void reLoadPlugin() {
        List<IPluginHolder> pluginList = RobotContentProvider.getInstance().getPluginList();
        List<PluginBean> list = new ArrayList<>();
        for (IPluginHolder pluginModelQuery : pluginList) {
            PluginBean bean = new PluginBean();
            bean.setPath(pluginModelQuery.getPath());
            bean.setPluginHolder(pluginModelQuery);
            list.add(bean);
        }

        File defaultPluginApkPath = PluginUtils.getDefaultPluginApkPath(AppContext.getInstance());


        File[] files = defaultPluginApkPath.listFiles(new LogFileFilter());

        if (files != null) {

            for (File file : files) {
                try {
                    String json = FileUtils.readFileToString(file, "utf-8");

                    if (json.startsWith("{")) {


                        ErrorPluginInterface pluginInterface = new ErrorPluginInterface(file, json);

                        QueryPluginModel pluginHolder = new QueryPluginModel();
                        String path = pluginInterface.getPath();
                        if (path != null && new File(path).exists()) {

                            pluginHolder.setPath(path);
                            pluginHolder.setOfficial(false);
                            pluginHolder.setDisableFlag(true);
                            pluginHolder.setPluginInterface(pluginInterface);


                            PluginBean bean = new PluginBean();
                            bean.setPath(pluginInterface.getPath());
                            bean.setPluginHolder(pluginHolder);
                            list.add(bean);

                        }


//                        list.add()
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        doOnQueryFinish(list);
    }

    public class LogFileFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            String absolutePath = pathname.getAbsolutePath();
            return absolutePath.endsWith(".log");
        }
    }


    @Override
    protected List<PluginBean> queryDataFromDb() {
       /* try {

            List<QueryPluginModel> pluginInterfaces = PluginUtils.loadPlugin(AppContext.getInstance());
            List<PluginBean> list = new ArrayList<>();
            for (QueryPluginModel pluginModelQuery : pluginInterfaces) {
                PluginBean bean = new PluginBean();
                bean.setPath(pluginModelQuery.getPath());
                bean.setPluginInterface(pluginModelQuery.getPluginInterface());
                list.add(bean);
            }
            return list;
        } catch (Throwable e) {
            Log.e("Plugin", "exception", e);
            return null;

        }*/
        throw new RuntimeException("???????????????");


    }


}
