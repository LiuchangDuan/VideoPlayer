package com.example.videoplayer;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2016/10/25.
 */
public class VideoItem {

    String name;
    String path;
    Bitmap thumb;
    String createdTime;

    public VideoItem(String strPath, String strName, String createdTime) {
        this.path = strPath;
        this.name = strName;

        /**
         * 获取到的视频文件创建的时间是Unix时间戳
         * 类似于1464152901这样的数字
         * 它是从1970年1月1日时开始，所经过的秒数
         * 此处将其转化为“年月日时分”这种可读的形式
         */
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy年MM月dd日HH时mm分");
        Date date = new Date(Long.valueOf(createdTime) * 1000);
        this.createdTime = simpleDateFormat.format(date);
        // 获取视频的缩略图
        /**
         * 这是Android SDK提供的一个利用视频文件地址获取视频缩略图的工具
         * 图片生成的尺寸可以通过第二个参数设置
         * MINI_KIND 表示小的缩略图
         * FULL_SCREEN_KIND 表示大尺寸的缩略图
         * MICRO_KIND 表示超小图的缩略图
         */
//        this.thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);

    }

    // 将创建缩略图的功能独立出来
    void createThumb() {
        if (this.thumb == null) {
            this.thumb = ThumbnailUtils.createVideoThumbnail(this.path, MediaStore.Images.Thumbnails.MINI_KIND);
        }
    }

    // 释放
    void releaseThumb() {
        if (this.thumb != null) {
            this.thumb.recycle();
            this.thumb = null;
        }
    }

    /**
     * 只要文件所在的路径是相同的
     * 就认为这两个比较项指的是同一个
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        VideoItem another = (VideoItem) o;
        return another.path.equals(this.path);
    }
}
