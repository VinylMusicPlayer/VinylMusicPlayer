package com.poupa.vinylmusicplayer.misc.queue;


public class DynamicElement {
    public final static DynamicElement EMPTY_ELEMENT = new DynamicElement("", "", ""); // Should be put into an interface

    public String firstLine;
    public String secondLine;
    public String iconText;

    public final static int INVALID_ICON = -1;
    public int icon;

    public DynamicElement(String firstLine, String secondLine, String iconText) {
        this(firstLine, secondLine, INVALID_ICON, iconText);
    }

    public DynamicElement(String firstLine, String secondLine, int icon) {
        this(firstLine, secondLine, icon, null);
    }

    private DynamicElement(String firstLine, String secondLine, int icon, String iconText) {
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.icon = icon;
        this.iconText = iconText;
    }
}
