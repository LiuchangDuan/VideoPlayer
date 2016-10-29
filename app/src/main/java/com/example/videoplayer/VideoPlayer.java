package com.example.videoplayer;

import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * Created by Administrator on 2016/10/29.
 */
public class VideoPlayer extends AppCompatActivity {

    private VideoView mVideoView;

    // 用于记录当前播放的位置
    private int mLastPlayedTime;

    private final String LAST_PLAYED_TIME = "LAST_TIME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();

        String path = uri.getPath();

        if (path == null) {
            exit();
            return;
        }

        setContentView(R.layout.activity_video_player);

        // 如果为竖屏
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            String[] searchKey = new String[] {
                MediaStore.Video.Media.TITLE, // 视频标题
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT, // 视频文件大小
                MediaStore.Video.Media.SIZE, // 视频尺寸
                MediaStore.Video.Media.DATE_ADDED // 视频创建时间
            };

            String where = MediaStore.Video.Media.DATA + " = '" + path + "'";

            String[] keywords = null;

            String sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;

            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, searchKey, where, keywords, sortOrder);

            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToNext();

                    String createdTime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                    int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)); // The size of the file in bytes
                    int width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH));
                    int height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT));
                    VideoItem item = new VideoItem(path, name, createdTime);

                    TextView title = (TextView) findViewById(R.id.video_title);
                    title.setText(item.name);

                    TextView created = (TextView) findViewById(R.id.video_create_time);
                    created.setText(item.createdTime);

                    TextView screen = (TextView) findViewById(R.id.video_width_height);
                    screen.setText(width + "*" + height);

                    TextView fileSize = (TextView) findViewById(R.id.video_size);
                    fileSize.setText(String.valueOf(size / 1024 / 1024) + "M");

                } else {
                    TextView title = (TextView) findViewById(R.id.video_title);
                    title.setText(R.string.unknown);

                    TextView created = (TextView) findViewById(R.id.video_create_time);
                    created.setText(R.string.unknown);

                    TextView screen = (TextView) findViewById(R.id.video_width_height);
                    screen.setText(R.string.unknown);

                    TextView fileSize = (TextView) findViewById(R.id.video_size);
                    fileSize.setText(R.string.unknown);
                }
                cursor.close();
            }
        } else {
            // 全屏设置
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // 隐藏状态栏和导航栏
            // 隐藏ActionBar
            getSupportActionBar().hide();
        }

        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setVideoPath(path);

        MediaController controller = new MediaController(this);
        mVideoView.setMediaController(controller);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // 保存当前播放的位置
        outState.putInt(LAST_PLAYED_TIME, mVideoView.getCurrentPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mLastPlayedTime = savedInstanceState.getInt(LAST_PLAYED_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停播放视频
        mVideoView.pause();
        // 记录当前播放的位置
        mLastPlayedTime = mVideoView.getCurrentPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mVideoView.start();
        if (mLastPlayedTime > 0) {
            mVideoView.seekTo(mLastPlayedTime);
        }
    }

    private void exit() {
        Toast.makeText(this, R.string.no_playing_target, Toast.LENGTH_SHORT).show();
        finish();
    }

}
