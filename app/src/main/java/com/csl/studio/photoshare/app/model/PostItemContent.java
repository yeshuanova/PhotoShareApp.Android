package com.csl.studio.photoshare.app.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample post_content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PostItemContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<PostItem> ITEMS = new ArrayList<PostItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, PostItem> ITEM_MAP = new HashMap<String, PostItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(PostItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.user_uid, item);
    }

    private static PostItem createDummyItem(int position) {
        return new PostItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

}
