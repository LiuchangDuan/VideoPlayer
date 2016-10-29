package com.example.videoplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = "VideoPlayer";

    private AsyncTask mVideoUpdateTask;

    private List<VideoItem> mVideoList;

    private ListView mVideoListView;

    private MenuItem mRefreshMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        this.setTitle();

        mVideoList = new ArrayList<VideoItem>();

        mVideoListView = (ListView) findViewById(R.id.video_list);

        VideoItemAdapter adapter = new VideoItemAdapter(this, R.layout.video_item, mVideoList);

        mVideoListView.setAdapter(adapter);

        mVideoListView.setOnItemClickListener(this);

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        VideoItem item = mVideoList.get(position);
    }

    /**
     *
     * 此方法用于初始化菜单，其中menu参数就是即将要显示的Menu实例
     *
     * @param menu
     * @return 返回true则显示该menu,false 则不显示
     *
     * 只会在第一次初始化菜单时调用
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        // 获取“刷新”菜单项
        mRefreshMenuItem = menu.findItem(R.id.menu_refresh);

        // 当VideoUpdateTask处于运行的状态时，菜单项的标题显示为“停止刷新”
        if (mVideoUpdateTask != null && mVideoUpdateTask.getStatus() == AsyncTask.Status.RUNNING) {
            mRefreshMenuItem.setTitle(R.string.in_refresh);
        } else {
            // 当VideoUpdateTask没有处于运行的状态时，菜单项的标题显示为“刷新”
            mRefreshMenuItem.setTitle(R.string.refresh);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.menu_refresh:
                if (mVideoUpdateTask != null && mVideoUpdateTask.getStatus() == AsyncTask.Status.RUNNING) {
                    // 当VideoUpdateTask处于运行的状态时，取消VideoUpdateTask的工作
                    mVideoUpdateTask.cancel(true);
                    mVideoUpdateTask = null;
                } else {
                    // 当VideoUpdateTask没有处于运行的状态时，启动VideoUpdateTask的工作
                    mVideoUpdateTask = new VideoUpdateTask();
                    mVideoUpdateTask.execute();
                    // 修改菜单项的标题为“停止刷新”
                    if (mRefreshMenuItem != null) {
                        mRefreshMenuItem.setTitle(R.string.in_refresh);
                    }
                }
                break;

            default:
                return super.onContextItemSelected(item);
        }

        return true;
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

//                    publishProgress(videoItem);

                    // 判断出之前没有这个视频，才发送给主线程更新界面
                    if (mVideoList.contains(videoItem) == false) {
                        // 判断需要添加，才创建缩略图
                        videoItem.createThumb();
                        publishProgress(videoItem);
                    }

                    // 保存起来
                    mDataList.add(videoItem);

                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(VideoItem... values) {

            VideoItem data = values[0];

            mVideoList.add(data);

            VideoItemAdapter adapter = (VideoItemAdapter) mVideoListView.getAdapter();

            adapter.notifyDataSetChanged();

        }

        /**
         * 工作完成后会被调用
         * 在主线程中运行
         * 可以在此修改界面
         * @param aVoid
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "Task has been finished");
            updateResult();
        }

        /**
         * 成功取消工作时会被调用
         * 在主线程中运行
         * 可以在此修改界面
         */
        @Override
        protected void onCancelled() {
            Log.d(TAG, "Task has been cancelled");
            updateResult();
        }

        private void updateResult() {
            for (int i = 0; i < mVideoList.size(); i++) {
                if (!mDataList.contains(mVideoList.get(i))) {
                    // 释放缩略图占用的内存资源
                    mVideoList.get(i).releaseThumb();
                    // 从ListView的数据集中移除多余的视频信息
                    mVideoList.remove(i);
                    // 因为移除了一个视频项，下一个视频项的序号就被减小了一个1
                    i--;
                }
            }
            mDataList.clear();

            // 通知ListView数据有改变，需要刷新
            VideoItemAdapter adapter = (VideoItemAdapter) mVideoListView.getAdapter();
            adapter.notifyDataSetChanged();

            // 修改菜单项的标题为“刷新”
            if (mRefreshMenuItem != null) {
                mRefreshMenuItem.setTitle(R.string.refresh);
            }

        }

    }

}
