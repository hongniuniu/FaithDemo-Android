package com.faith.fd.bean;

import com.lzy.imagepicker.bean.ImageItem;

/**
 * Created by dafan on 2016/11/18 0018.
 */

public class Puzzlebean {
  public static final int TYPE_DEFAULT = 0;
  public static final int TYPE_VIDEO = 1;
  public static final int TYPE_IMAGE = 2;

  private int index;
  private int type;
  private ImageItem imageItem;
  private VideoItem videoItem;

  public static Puzzlebean defa(int index) {
    Puzzlebean puzzlebean = new Puzzlebean();
    puzzlebean.setType(TYPE_DEFAULT);
    puzzlebean.setIndex(index);
    return puzzlebean;
  }

  public static Puzzlebean video(int index, VideoItem videoItem) {
    Puzzlebean puzzlebean = new Puzzlebean();
    puzzlebean.setIndex(index);
    puzzlebean.setType(TYPE_VIDEO);
    puzzlebean.setVideoItem(videoItem);
    return puzzlebean;
  }

  public static Puzzlebean iamge(int index, ImageItem imageItem) {
    Puzzlebean puzzlebean = new Puzzlebean();
    puzzlebean.setIndex(index);
    puzzlebean.setType(TYPE_IMAGE);
    puzzlebean.setImageItem(imageItem);
    return puzzlebean;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public ImageItem getImageItem() {
    return imageItem;
  }

  public void setImageItem(ImageItem imageItem) {
    this.imageItem = imageItem;
  }

  public VideoItem getVideoItem() {
    return videoItem;
  }

  public void setVideoItem(VideoItem videoItem) {
    this.videoItem = videoItem;
  }
}
