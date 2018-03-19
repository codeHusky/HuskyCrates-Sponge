package com.codehusky.huskycrates.crate;

/**
 * Keys are objects that contain an identifier (for the key) and the display item for such key, if that applies.
 * Keys can also be virtual, meaning they have no real-world item, allowing for quest-like features.
 */
public class Key {
    private String id;
    private Boolean isVirtual;
    private Item displayItem;
}
