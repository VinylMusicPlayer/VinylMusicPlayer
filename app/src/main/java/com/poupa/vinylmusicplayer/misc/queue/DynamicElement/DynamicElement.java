package com.poupa.vinylmusicplayer.misc.queue.DynamicElement;


/** Provide data used to show the user what will be loaded into the playing queue when it end */
public class DynamicElement {
    public String firstLine;
    public String secondLine;

    /** only set if {@link DynamicElement#icon} is not as they are put into the same place in {@link com.poupa.vinylmusicplayer.databinding.ItemListBinding} */
    public String iconText;

    public final static int INVALID_ICON = -1;
    /** @see DynamicElement#iconText */
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
