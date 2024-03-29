package com.example.musicapplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.musicapplication.MusicService.MusicBinder;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
	ArrayList<Songs> songList;
	ListView songView;
	MusicService musicService;
	Intent playIntent;
	boolean musicBound=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Songs>();
        
        SongAdapter songAdptr = new SongAdapter(this, songList);
        songView.setAdapter(songAdptr);
        
        getSongsList();
        Collections.sort(songList, new Comparator<Songs>(){
        	  public int compare(Songs a, Songs b){
        	    return a.getTitle().compareTo(b.getTitle());
        	  }
        	});
    }
    private ServiceConnection musicConnection = new ServiceConnection(){
    	 
    	  @Override
    	  public void onServiceConnected(ComponentName name, IBinder service) {
    	    MusicBinder binder = (MusicBinder)service;
    	   
    	    musicService = binder.getService();
    	   
    	    musicService.setList(songList);
    	    musicBound = true;
    	  }
    	 
    	  @Override
    	  public void onServiceDisconnected(ComponentName name) {
    	    musicBound = false;
    	  }
    	};
    	
    	

    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		 if(playIntent==null)
		 {
			    playIntent = new Intent(this, MusicService.class);
			    bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			    startService(playIntent);
	     }
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.action_shuffle:
    	  //shuffle
    	  break;
    	case R.id.action_end:
    	  stopService(playIntent);
    	  musicService=null;
    	  System.exit(0);
    	  break;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
      stopService(playIntent);
      musicService=null;
      super.onDestroy();
    }

	public void getSongsList() {
    	ContentResolver musicResolver = getContentResolver();
    	Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    	Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
    	
    	if(musicCursor!=null && musicCursor.moveToFirst()){
    		  //get columns
    		  int titleColumn = musicCursor.getColumnIndex
    		    (android.provider.MediaStore.Audio.Media.TITLE);
    		  int idColumn = musicCursor.getColumnIndex
    		    (android.provider.MediaStore.Audio.Media._ID);
    		  int artistColumn = musicCursor.getColumnIndex
    		    (android.provider.MediaStore.Audio.Media.ARTIST);
    		  //add songs to list
    		  do {
    		    long thisId = musicCursor.getLong(idColumn);
    		    String thisTitle = musicCursor.getString(titleColumn);
    		    String thisArtist = musicCursor.getString(artistColumn);
    		    songList.add(new Songs(thisId, thisTitle, thisArtist));
    		  }
    		  while (musicCursor.moveToNext());
    		}
    	}
    
	public void songPicked(View view){
		  musicService.setSong(Integer.parseInt(view.getTag().toString()));
		  musicService.playSong();
		}
    
}
