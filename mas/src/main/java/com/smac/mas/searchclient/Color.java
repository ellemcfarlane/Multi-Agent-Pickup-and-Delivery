package com.smac.mas.searchclient;

import java.util.Locale;

public enum Color {
    Blue,
    Red,
    Cyan,
    Purple,
    Green,
    Orange,
    Pink,
    Grey,
    Lightblue,
    Brown;

    public static Color fromString(String s) {
        switch (s.toLowerCase(Locale.ROOT)) {
            case "blue":
                return Blue;
            case "red":
                return Red;
            case "cyan":
                return Cyan;
            case "purple":
                return Purple;
            case "green":
                return Green;
            case "orange":
                return Orange;
            case "pink":
                return Pink;
            case "grey":
                return Grey;
            case "lightblue":
                return Lightblue;
            case "brown":
                return Brown;
            default:
                return null;
        }
    }

    public static int color2Int(Color color) {

        switch (color) {
            case Blue:
                return 0;
            case Red:
                return 1;
            case Cyan:
                return 2;
            case Purple:
                return 3;
            case Green:
                return 4;
            case Orange:
                return 5;
            case Pink:
                return 6;
            case Grey:
                return 7;
            case Lightblue:
                return 8;
            case Brown:
                return 9;
            default:
                return -1;
        }

    }

}
