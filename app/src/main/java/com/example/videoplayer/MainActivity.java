package com.example.videoplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "VideoPlayer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 指向外部存储的uri
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] searchKey = new String[]{
                MediaStore.Video.Media.TITLE, // 对应文件的标题
                MediaStore.Images.Media.DATA, // 对应文件的存放位置
                MediaStore.Images.Media.DATE_ADDED // 对应文件的创建时间
        };

        // 查询条件：包含 /Video 字段
        String where = MediaStore.Video.Media.DATA + " like \"%" + "/Video" + "%\"";

        String[] keywords = null;

        // 设定查询结果的排序方式 使用默认的排序方式
        String sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;

        ContentResolver resolver = getContentResolver();

        Cursor cursor = resolver.query(uri, searchKey, where, keywords, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 获取视频的存放路径
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                // 获取视频的标题
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                // 获取视频的创建时间
                String createdTime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));

                VideoItem videoItem = new VideoItem(path, name, createdTime);

                Log.d(TAG, "real video found: " + path);

            }
            cursor.close();
        }

    }
}
