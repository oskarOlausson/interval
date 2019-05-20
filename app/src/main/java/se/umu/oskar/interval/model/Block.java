package se.umu.oskar.interval.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Block implements Parcelable {
    private String name;
    private int timeMaxInSeconds;
    private int color;
    private boolean pause = false;
    private int id;
    private static int idCount = 0;

    public Block(String name, int timeInSeconds, int color) {
        this.name = name;
        this.timeMaxInSeconds = timeInSeconds;
        this.color = color;
        this.id = idCount++;
    }

    public void setTime(int newMax) {
        this.timeMaxInSeconds = newMax;
    }

    public int color() {
        return color;
    }

    public int timeInSeconds() {
        return timeMaxInSeconds;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        //alphabetical order
        parcel.writeInt(color);
        parcel.writeInt(id);
        parcel.writeByte(boolToByte(pause));
        parcel.writeInt(timeMaxInSeconds);
        parcel.writeString(name);
    }

    private static byte boolToByte(boolean bool) {
        return bool ? (byte) 1 : (byte) 0;
    }

    private static boolean byteToBool(byte b) {
        return b != (byte) 0;
    }

    public static final Parcelable.Creator<Block> CREATOR = new Parcelable.Creator<Block>() {
        @Override
        public Block createFromParcel(Parcel parcel) {
            //alphabetical order
            int color = parcel.readInt();
            int id = parcel.readInt();
            byte pauseByte = parcel.readByte();
            int timeMaxInSeconds = parcel.readInt();
            String title = parcel.readString();

            Block block = new Block(title, timeMaxInSeconds, color);
            block.setIsPause(byteToBool(pauseByte));
            block.id = id;

            return block;
        }

        @Override
        public Block[] newArray(int i) {
            return new Block[0];
        }
    };

    public String name() {
        return name;
    }

    public boolean isPause() {
        return pause;
    }

    void setIsPause(boolean pause) {
        this.pause = pause;
    }

    public Block copy() {
        Block b = new Block(name, timeMaxInSeconds, color);
        b.id = id;
        b.pause = pause;
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != this.getClass()) return false;
        Block b = (Block) o;
        return b.id == id;
    }

    public int id() {
        return id;
    }

    public String blockToLine() {
        String quote = "\"";
        String space = " ";

        return quote + name + quote + space +
                timeMaxInSeconds + space +
                color + space +
                isPause() + "\n";
    }

    private static final Pattern pattern = Pattern.compile("\"([^\"]*)\" (-?\\d+) (-?\\d+) ((true|false)$)");

    public static Block lineToBlock(String line) {
        line = line.trim();

        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String name = matcher.group(1);

            int timeInSeconds = Integer.parseInt(matcher.group(2));
            int color = Integer.parseInt(matcher.group(3));
            boolean isPause = Boolean.parseBoolean(matcher.group(4));


            Block block = new Block(name, timeInSeconds, color);
            block.pause = isPause;
            return block;
        } else return null;
    }
}
