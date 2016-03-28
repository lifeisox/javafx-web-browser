package browser;

import java.util.EventListener;
/** 
 * A contract between a BookmarkActionEvent source and listener classes
 */
public interface BookmarkActionListener extends EventListener {
	/**
	 * Called whenever the bookmark button is pressed.
	 * @param actionEvent The object parameter representes title, URL and icon of bookmark.
	 */
	public void onBookmarkButtonClick(BookmarkActionEvent actionEvent);
	/**
	 *  Called whenever the user confirms if a URL exists in bookmark list.
	 * @param actionEvent The object parameter representes title, URL and icon of bookmark.
	 * @return True if it exists, but false
	 */
	public boolean isBookmarkThere(BookmarkActionEvent actionEvent);
}
