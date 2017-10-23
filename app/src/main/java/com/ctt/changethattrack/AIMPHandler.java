package com.ctt.changethattrack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Acer on 2017-10-22.
 */

public final class AIMPHandler {
    private static int mTimeout = 2000;

    private AIMPHandler(){

    }

    public static void setVolume(String endpoint, int volume) throws TimeoutException, ExecutionException, InterruptedException {
        HTTP task = new HTTP();
        task.execute(endpoint + "/?action=set_volume&volume=" + Integer.toString(volume)).get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static int getVolume(String endpoint) throws TimeoutException, ExecutionException, InterruptedException {
        HTTP task = new HTTP();
        return Integer.parseInt(task.execute(endpoint + "/?action=get_volume").get(mTimeout, TimeUnit.MILLISECONDS));
    }

    public static void playNext(String endpoint) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        task.execute(endpoint + "/?action=player_next").get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static void playPrevious(String endpoint) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        task.execute(endpoint + "/?action=player_prevous").get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static void play(String endpoint) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        task.execute(endpoint + "/?action=player_play").get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static void pause(String endpoint) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        task.execute(endpoint + "/?action=player_pause").get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static void toggleShuffle(String endpoint, boolean currentShuffleTag) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        task.execute(endpoint + "/?action=set_player_status&statusType=shuffle&value=" + (currentShuffleTag ? "0" : "1")).get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static void toggleRepeat(String endpoint, boolean currentRepeatTag) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        task.execute(endpoint + "/?action=set_player_status&statusType=repeat&value=" + (currentRepeatTag ? "0" : "1")).get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static String getSongInfo(String endpoint) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        return task.execute(endpoint + "/?action=get_song_current").get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static void setTrackPosition(String endpoint, int position) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        task.execute(endpoint + "/?action=set_track_position&position=" + Integer.toString(position)).get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static void setCustomStatus(String endpoint, String statusCode, String value) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        task.execute(endpoint + "/?action=set_custom_status&status=" + statusCode + "&value=" + value).get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static String getCustomStatus(String endpoint, String statusCode) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        return task.execute(endpoint + "/?action=get_custom_status&status=" + statusCode).get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static int getTrackPosition(String endpoint) throws InterruptedException, ExecutionException, TimeoutException {
        int position = 0;
        HTTP task = new HTTP();
        try {
            String response = task.execute(endpoint + "/?action=get_track_position").get(mTimeout, TimeUnit.MILLISECONDS);
            JSONObject obj = new JSONObject(response);
            position = obj.getInt("position");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return position;
    }

    public static boolean ping(String endpoint){
        boolean responsive = true;
        HTTP task = new HTTP();
        try{
            task.execute(endpoint).get(mTimeout, TimeUnit.MILLISECONDS);
        }catch(Exception ex){
            responsive = false;
        }
        return responsive;
    }

    public static void setPlayedSong(String endpoint, int id, int position) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        task.execute(endpoint + "/?action=set_song_play&playlist=" + id + "&song=" + position).get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static String getCurrentSong(String endpoint) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        return task.execute(endpoint + "/?action=get_song_current").get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static String getPlaylistList(String endpoint) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        return task.execute(endpoint + "/?action=get_playlist_list").get(mTimeout, TimeUnit.MILLISECONDS);
    }

    public static String getPlaylistSongs(String endpoint, String playlistId) throws InterruptedException, ExecutionException, TimeoutException {
        HTTP task = new HTTP();
        return task.execute(endpoint + "/?action=get_playlist_songs&id=" + playlistId).get(mTimeout, TimeUnit.MILLISECONDS);
    }
}
