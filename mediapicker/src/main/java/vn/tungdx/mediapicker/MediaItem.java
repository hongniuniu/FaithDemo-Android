package vn.tungdx.mediapicker;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import vn.tungdx.mediapicker.activities.MediaPickerActivity;
import vn.tungdx.mediapicker.utils.MediaUtils;

/**
 * @author TUNGDX
 */

/**
 * Contains information of photo or video that selected and return back in
 * {@link MediaPickerActivity}
 */
public class MediaItem implements Parcelable {
	public static final int PHOTO = 1;
	public static final int VIDEO = 2;
	private int type;
	private Uri uriCropped;
	private Uri uriOrigin;
	private long duration;

	/**
	 * @param mediaType Whether {@link #PHOTO} or {@link #VIDEO}
	 * @param uriOrigin {@link Uri} of media item.
	 */
	public MediaItem(int mediaType, Uri uriOrigin) {
		this.type = mediaType;
		this.uriOrigin = uriOrigin;
	}

	/**
	 * @return type of media item. Whether {@link #PHOTO} or {@link #VIDEO}
	 */
	public int getType() {
		return type;
	}

	/**
	 * Set type of media.
	 *
	 * @param type is {@link #PHOTO} or {@link #VIDEO}
	 */
	public void setType(int type) {
		this.type = type;
	}

	public Uri getUriCropped() {
		return uriCropped;
	}

	public void setUriCropped(Uri uriCropped) {
		this.uriCropped = uriCropped;
	}

	public Uri getUriOrigin() {
		return uriOrigin;
	}

	public void setUriOrigin(Uri uriOrigin) {
		this.uriOrigin = uriOrigin;
	}

	public boolean isVideo() {
		return type == VIDEO;
	}

	public boolean isPhoto() {
		return type == PHOTO;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((uriCropped == null) ? 0 : uriCropped.hashCode());
		result = prime * result
				+ ((uriOrigin == null) ? 0 : uriOrigin.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MediaItem other = (MediaItem) obj;
		if (uriCropped == null) {
			if (other.uriCropped != null)
				return false;
		} else if (!uriCropped.equals(other.uriCropped))
			return false;
		if (uriOrigin == null) {
			if (other.uriOrigin != null)
				return false;
		} else if (!uriOrigin.equals(other.uriOrigin))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MediaItem{" +
				"type=" + type +
				", uriCropped=" + uriCropped +
				", uriOrigin=" + uriOrigin +
				", duration=" + duration +
				'}';
	}

	/**
	 * @param context
	 * @return Path of origin file.
	 */
	public String getPathOrigin(Context context) {
		return getPathFromUri(context, uriOrigin);
	}

	/**
	 * @param context
	 * @return Path of cropped file.
	 */
	public String getPathCropped(Context context) {
		return getPathFromUri(context, uriCropped);
	}

	private String getPathFromUri(Context context, Uri uri) {
		if (uri == null)
			return null;
		String scheme = uri.getScheme();
		if (scheme.equals(ContentResolver.SCHEME_FILE)) {
			return uri.getPath();
		} else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
			if (isPhoto()) {
				return MediaUtils.getRealImagePathFromURI(
						context.getContentResolver(), uri);
			} else {
				return MediaUtils.getRealVideoPathFromURI(
						context.getContentResolver(), uri);
			}
		}
		return uri.toString();
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.type);
		dest.writeParcelable(this.uriCropped, flags);
		dest.writeParcelable(this.uriOrigin, flags);
		dest.writeLong(this.duration);
	}

	protected MediaItem(Parcel in) {
		this.type = in.readInt();
		this.uriCropped = in.readParcelable(Uri.class.getClassLoader());
		this.uriOrigin = in.readParcelable(Uri.class.getClassLoader());
		this.duration = in.readLong();
	}

	public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
		@Override
		public MediaItem createFromParcel(Parcel source) {
			return new MediaItem(source);
		}

		@Override
		public MediaItem[] newArray(int size) {
			return new MediaItem[size];
		}
	};
}