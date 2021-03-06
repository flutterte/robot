/*
 *
 *                     .::::.
 *                   .::::::::.
 *                  :::::::::::  by qssq666@foxmail.com
 *              ..:::::::::::'
 *            '::::::::::::'
 *              .::::::::::
 *         '::::::::::::::..
 *              ..::::::::::::.
 *            ``::::::::::::::::
 *             ::::``:::::::::'        .:::.
 *            ::::'   ':::::'       .::::::::.
 *          .::::'      ::::     .:::::::'::::.
 *         .:::'       :::::  .:::::::::' ':::::.
 *        .::'        :::::.:::::::::'      ':::::.
 *       .::'         ::::::::::::::'         ``::::.
 *   ...:::           ::::::::::::'              ``::.
 *  ```` ':.          ':::::::::'                  ::::..
 *                     '.:::::'                    ':'````..
 *
 */

package cn.qssq666.robot.utils;
import androidx.annotation.NonNull;

import com.loopj.android.http.BinaryHttpResponseHandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

public class DownloadUtils {
    private static final String TAG = "DownloadUtils";

    public static File byte2File(byte[] bytes, File file) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
            bos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return file;
    }

    public interface DownloadListener {
        void onStart(int value);

        void onProgress(int process);

        void onFail(String value);

        void onSuccess(String value);
    }

    public static void downloadFile(String url, final File filePath, @NonNull final DownloadListener downloadListener) {
        com.loopj.android.http.AsyncHttpClient client = new com.loopj.android.http.AsyncHttpClient();
// ??????????????????
        String[] allowedContentTypes = new String[]{"image/png", "image/jpeg", "video/mp4", "application/octet-stream", "tapplication/x-gzip", "application/zip", "application/vnd.android.package-archive"};
        client.get(url, new BinaryHttpResponseHandler(allowedContentTypes) {


            @Override
            public void onSuccess(int statusCode, Header[] headers,
                                  byte[] binaryData) {
                try {
                    if (filePath.exists()) {
                        boolean delete = filePath.delete();
                        if (delete == false) {
                            if (downloadListener != null) {
                                downloadListener.onFail("????????????,????????????????????????????????????");

                            }
                            return;
                        }
                    }

                    filePath.getParentFile().mkdirs();
                    filePath.createNewFile();
                    filePath.setWritable(true);
                    filePath.setExecutable(true);
                    byte2File(binaryData, filePath);
                    if (downloadListener != null) {
                        downloadListener.onSuccess(filePath.getAbsolutePath());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    if (downloadListener != null) {
                        downloadListener.onFail(e.toString());
                    }
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] binaryData, Throwable error) {
                if (downloadListener != null) {
                    downloadListener.onFail(error.toString());
                }
//				Toast.makeText(mContext, "????????????", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                int count = (int) ((bytesWritten * 1.0 / totalSize) * 100);
                if (downloadListener != null) {
                    downloadListener.onProgress(count);
                }
//                Prt.d("?????? Progress>>>>>", bytesWritten + " / " + totalSize);
            }

            @Override
            public void onStart() {
                super.onStart();
                if (downloadListener != null) {
                    downloadListener.onStart(0);
                }
            }

            @Override
            public void onRetry(int retryNo) {
                super.onRetry(retryNo);
                // ??????????????????
            }
        });
    }

    /**
     * ????????????????????? ??????????????????????????????,??????????????????temp.apk??????
     *
     * @return
     */
    public static String getNetFileExtension(String urlPath) {
        int index = urlPath.lastIndexOf("/");
        return index != -1 ? urlPath.substring(index) : "temp.apk";

    }
}
