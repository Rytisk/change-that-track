package com.ctt.changethattrack;

/**
 * Created by Rytis on 2017-10-16.
 */

public class Track {
    private int mPosition;
    private String mName;
    private int mPlaylistId;

    public int getPosition() {
        return mPosition;
    }

    public String getName() {
        return mName;
    }

    public int getPlaylistId() {
        return mPlaylistId;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setPlaylistId(int playlistId) {
        this.mPlaylistId = playlistId;
    }

    public Track(int position, String name, int playlistId) {
        this.mPosition = position;
        this.mName = name;
        this.mPlaylistId = playlistId;
    }

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof Track){
            Track ptr = (Track) v;
            retVal = (ptr.mPosition == this.mPosition) && (ptr.mPlaylistId == this.mPlaylistId);
        }

        return retVal;
    }

    @Override
    public String toString() {
        return mName;
    }
}
