package com.faith.fd.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dafan on 2016/11/22 0022.
 */

public class VideoItem implements Parcelable {
  private String path;
  private String frame;
  private int voice = 100;
  private long duration = 0L;
  private int index = -1;

  public int getVoice() {
    return voice;
  }

  public float getVoice2() {
    return voice * 1.0f / 100;
  }

  public void setVoice(int voice) {
    this.voice = voice;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getFrame() {
    return frame;
  }

  public void setFrame(String frame) {
    this.frame = frame;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.path);
    dest.writeString(this.frame);
    dest.writeInt(this.voice);
    dest.writeLong(this.duration);
    dest.writeInt(this.index);
  }

  public VideoItem() {
  }

  protected VideoItem(Parcel in) {
    this.path = in.readString();
    this.frame = in.readString();
    this.voice = in.readInt();
    this.duration = in.readLong();
    this.index = in.readInt();
  }

  public static final Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
    @Override public VideoItem createFromParcel(Parcel source) {
      return new VideoItem(source);
    }

    @Override public VideoItem[] newArray(int size) {
      return new VideoItem[size];
    }
  };
}
