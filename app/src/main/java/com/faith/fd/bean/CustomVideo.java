package com.faith.fd.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by dafan on 2016/4/17 0017.
 */
public class CustomVideo implements Parcelable {
  @SerializedName("id") @Expose private Integer id = 0;
  @SerializedName("name") @Expose private String name = "";
  @SerializedName("original") @Expose private String original = "";
  @SerializedName("image") @Expose private String image = "";
  @SerializedName("rep_width") @Expose private Integer repWidth = 0;
  @SerializedName("rep_height") @Expose private Integer repHeight = 0;
  /**
   * 视频编辑时间
   */
  private long time = 0;
  /**
   * 视频是否被选中
   */
  private boolean isCheck = false;
  /**
   * 视频保存在本地的路径
   */
  private String localVideoPath = "";
  /**
   * 图片保存在本地的路径
   */
  private String localImagePath = "";
  /**
   * 视频编辑的信息
   */
  private String customInfo = "";
  /**
   * 视频编辑后的网络地址
   */
  private String originalCustom = "";

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOriginal() {
    return original;
  }

  public void setOriginal(String original) {
    this.original = original;
  }

  public String getImage() {
    return image;
  }

  public void setImage(String image) {
    this.image = image;
  }

  public Integer getRepWidth() {
    return repWidth;
  }

  public void setRepWidth(Integer repWidth) {
    this.repWidth = repWidth;
  }

  public Integer getRepHeight() {
    return repHeight;
  }

  public void setRepHeight(Integer repHeight) {
    this.repHeight = repHeight;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public boolean isCheck() {
    return isCheck;
  }

  public void setCheck(boolean check) {
    isCheck = check;
  }

  public String getLocalVideoPath() {
    return localVideoPath;
  }

  public void setLocalVideoPath(String localVideoPath) {
    this.localVideoPath = localVideoPath;
  }

  public String getLocalImagePath() {
    return localImagePath;
  }

  public void setLocalImagePath(String localImagePath) {
    this.localImagePath = localImagePath;
  }

  public String getCustomInfo() {
    return customInfo;
  }

  public void setCustomInfo(String customInfo) {
    this.customInfo = customInfo;
  }

  public String getOriginalCustom() {
    return originalCustom;
  }

  public void setOriginalCustom(String originalCustom) {
    this.originalCustom = originalCustom;
  }

  @Override public String toString() {
    return "CustomVideo{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", original='"
        + original
        + '\''
        + ", image='"
        + image
        + '\''
        + ", repWidth="
        + repWidth
        + ", repHeight="
        + repHeight
        + ", time="
        + time
        + ", isCheck="
        + isCheck
        + ", localVideoPath='"
        + localVideoPath
        + '\''
        + ", localImagePath='"
        + localImagePath
        + '\''
        + ", customInfo='"
        + customInfo
        + '\''
        + ", originalCustom='"
        + originalCustom
        + '\''
        + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(this.id);
    dest.writeString(this.name);
    dest.writeString(this.original);
    dest.writeString(this.image);
    dest.writeValue(this.repWidth);
    dest.writeValue(this.repHeight);
    dest.writeLong(this.time);
    dest.writeByte(this.isCheck ? (byte) 1 : (byte) 0);
    dest.writeString(this.localVideoPath);
    dest.writeString(this.localImagePath);
    dest.writeString(this.customInfo);
    dest.writeString(this.originalCustom);
  }

  public CustomVideo() {
  }

  protected CustomVideo(Parcel in) {
    this.id = (Integer) in.readValue(Integer.class.getClassLoader());
    this.name = in.readString();
    this.original = in.readString();
    this.image = in.readString();
    this.repWidth = (Integer) in.readValue(Integer.class.getClassLoader());
    this.repHeight = (Integer) in.readValue(Integer.class.getClassLoader());
    this.time = in.readLong();
    this.isCheck = in.readByte() != 0;
    this.localVideoPath = in.readString();
    this.localImagePath = in.readString();
    this.customInfo = in.readString();
    this.originalCustom = in.readString();
  }

  public static final Creator<CustomVideo> CREATOR = new Creator<CustomVideo>() {
    @Override public CustomVideo createFromParcel(Parcel source) {
      return new CustomVideo(source);
    }

    @Override public CustomVideo[] newArray(int size) {
      return new CustomVideo[size];
    }
  };
}
