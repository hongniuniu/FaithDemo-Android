package vn.tungdx.mediapicker;

import java.util.List;

/**
 * @author TUNGDX
 */

/**
 * Listener for select media item.
 */
public interface MediaSelectedListener {
	void onHasNoSelected();

	void onHasSelected(List<MediaItem> mediaSelectedList);
}
