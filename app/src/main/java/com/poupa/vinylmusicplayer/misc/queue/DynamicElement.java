package com.poupa.vinylmusicplayer.misc.queue;


public class DynamicElement {
    public final static DynamicElement EMPTY_ELEMENT = new DynamicElement("", "", ""); // Should be put into an interface

    public String firstLine;
    public String secondLine;
    public String icon;

    public DynamicElement(String firstLine, String secondLine, String icon) {
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.icon = icon;
    }
}
