package browser;

import java.util.EventObject;

import javafx.scene.image.ImageView;
/**
 * An event that represents the things relate to bookmark action
 */
public class BookmarkActionEvent extends EventObject {
	private static final long serialVersionUID = 5718257236792830620L;
	private String title;
	private String url;
	private ImageView icon;

	public BookmarkActionEvent(Object source, String title, String url, ImageView icon) {
		super(source);
		this.title = title;
		this.url = url;
		this.icon = icon;
	}
	/**
	 * It is the getter of bookmark title.
	 * @return String variable for bookmark title
	 */
	public String getTitle() { return title; }
	/**
	 * It is the getter of bookmark URL.
	 * @return String variable for bookmark URL
	 */
	public String getUrl() { return url; }
	/**
	 * It is the getter of bookmark icon.
	 * @return ImageView variable for bookmark icon
	 */
	public ImageView getIcon() { return icon; }
}
