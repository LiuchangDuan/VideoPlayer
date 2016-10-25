package com.example.videoplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "VideoPlayer";

    private AsyncTask mVideoUpdateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoUpdateTask = new VideoUpdateTask();

        mVideoUpdateTask.execute();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mVideoUpdateTask != null && mVideoUpdateTask.getStatus() == AsyncTask.Status.RUNNING) {
            mVideoUpdateTask.cancel(true);
        }

        mVideoUpdateTask = null;

    }

    /**
     * 不需要为新创建的线程传入参数；所以Param设置成Object；
     * 因为查询的过程很长，所以需要时不时通知主线程查询的状态
     * 每查询到一条，就将视频数据传递给主线程；所以Progress设置成VideoItem；
     * 查询的结果已经在查询的过程中发送给了主线程
     * 全部完成后，不需要再传递什么结果给主线程了，所以Result设置成Void；
     * 将查询视频信息的操作放到doInBackground()中进行，这是一个新创建的工作线程；
     * 工作线程中，每发现一个视频，就通知给主线程；
     */
    private class VideoUpdateTask extends AsyncTask<Object, VideoItem, Void> {

        List<VideoItem> mDataList = new ArrayList<VideoItem>();

        @Override
        protected Void doInBackground(Object... params) {

            // 指向外部存储的uri
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            String[] searchKey = new String[]{
                    MediaStore.Video.Media.TITLE, // 对应文件的标题
                    MediaStore.Images.Media.DATA, // 对应文件的存放位置
                    MediaStore.Images.Media.DATE_ADDED // 对应文件的创建时间
            };

            // 查询条件：包含 /Video 字段
//            String where = MediaStore.Video.Media.DATA + " like \"%" + "/Video" + "%\"";
            String where = MediaStore.Video.Media.DATA + " like \"%" + getString(R.string.search_path) + "%\"";

            String[] keywords = null;

            // 设定查询结果的排序方式 使用默认的排序方式
            String sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;

            ContentResolver resolver = getContentResolver();

            Cursor cursor = resolver.query(uri, searchKey, where, keywords, sortOrder);

            if (cursor != null) {
                while (cursor.moveToNext() && !isCancelled() ) {
                    // 获取视频的存放路径
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    // 获取视频的标题
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                    // 获取视频的创建时间
                    String createdTime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));

                    VideoItem videoItem = new VideoItem(path, name, createdTime);

                    Log.d(TAG, "real video found: " + path);

                    publishProgress(videoItem);

                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(VideoItem... values) {
            VideoItem data = values[0];
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "Task has been finished");
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "Task has been cancelled");
        }

    }

}
