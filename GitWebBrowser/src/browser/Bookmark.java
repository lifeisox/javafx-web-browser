package browser;

import java.io.Serializable;

/**
 * <p>This class is a bookmark sturcture.</p>
 * 
 * @author Byungseon Kim (Student No: 040808296)
 * 
 */
@SuppressWarnings("serial")
public class Bookmark implements Serializable {
	private String title;
	private String url;

	/**
	 * It is the constructor of Bookmark class
	 * @param title It is a bookmark title.
	 * @param url It is the URL of a bookmark.
	 */
	public Bookmark(String title, String url) {
		this.title = title;
		this.url = url;
	}

	/**
	 * It is the getter of bookmark title.
	 * @return String variable for bookmark title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * It is the setter of bookmark title.
	 * @param title It is the String variable to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * It is the getter of bookmark URL.
	 * @return String variable for bookmark URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * It is the setter of bookmark URL.
	 * @param url It is the String variable to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}
}
