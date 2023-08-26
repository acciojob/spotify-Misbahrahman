package com.driver;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;
    public HashMap<String , User> userHashMap;
    public HashMap<String , Artist> artistHashMap;
    public HashMap<String , Album> albumHashMap;
    public HashMap<String  , Song> songHashMap;
    public HashMap<String  , Playlist> playlistHashMap;
    public HashMap<Integer , List<Song>> songsByLenght;



    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();
        userHashMap = new HashMap<>();
        artistHashMap = new HashMap<>();
        albumHashMap = new HashMap<>();
        songHashMap = new HashMap<>();
        playlistHashMap = new HashMap<>();
        songsByLenght = new HashMap<>();


        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }


    public User createUser(String name, String mobile) {
        User user = new User(name , mobile);
        users.add(user);
        userHashMap.put(user.getMobile(), user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        artistHashMap.put(artist.getName() , artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        if(!artistHashMap.containsKey(artistName))createArtist(artistName);

        Album album = new Album(title);
        albums.add(album);
        albumHashMap.put(album.getTitle(), album);

        if(!artistAlbumMap.containsKey(artistName)){
            List<Album> list = new ArrayList<>();
            list.add(album);
            artistAlbumMap.put(artistHashMap.get(artistName) , list);
        }else{
            List<Album> list = artistAlbumMap.get(artistName);
            list.add(album);
        }

        return album;
    }

    public String createSong(String title, String albumName, int length) throws Exception{
        Song song = new Song(title , length);
        songs.add(song);
        if(songsByLenght.containsKey(length)){
            List<Song> list = songsByLenght.get(length);
            list.add(song);
        }else{
            List<Song> list = new ArrayList<>();
            list.add(song);
            songsByLenght.put(length , list);
        }
        songHashMap.put(song.getTitle() , song);

       try{
           if(!albumHashMap.containsKey(albumName)) {
               throw new Exception(albumName +  "Album Does'nt Exist");
           }
       }catch (Exception e){
           return e.getMessage();
       }


       Album album = albumHashMap.get(albumName);

       if(albumSongMap.containsKey(album)){
            List<Song> list = albumSongMap.get(album);
            list.add(song);
       }else{
            List<Song> list = new ArrayList<>();
            list.add(song);
            albumSongMap.put(album , list);
       }
       return "SuccessFully Added";
    }

    public String createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        try{
            if(!userHashMap.containsKey(mobile))throw new Exception("User does'nt Exist");
        }catch (Exception e){
            return e.getMessage();
        }
        User user = userHashMap.get(mobile);

        Playlist playlist = new Playlist(title);
        playlistHashMap.put(title , playlist);
        //making creator
        creatorPlaylistMap.put(user , playlist);

        //adding Listener
        List<User> arr = new ArrayList<>();
        arr.add(user);
        playlistListenerMap.put(playlist , arr);


        List<Song> list = new ArrayList<>();
        //fetching from Already made map
        if (songsByLenght.containsKey(length)) {
            list.addAll(songsByLenght.get(length));
        }

        playlistSongMap.put(playlist , list);
        return "Sucess " ;
    }

    public String createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
       try {
           if(!userHashMap.containsKey(mobile))throw new Exception("User does'nt Exist");
       }catch (Exception e){
           return e.getMessage();
       }
        User user = userHashMap.get(mobile);
        Playlist playlist = new Playlist(title);
        playlistHashMap.put(title , playlist);
        //making creator
        creatorPlaylistMap.put(user , playlist);

        List<Song> list = new ArrayList<>();
        for(String songTitle : songTitles){
            if(songHashMap.containsKey(songTitle)){
                list.add(songHashMap.get(songTitle));
            }
        }
        playlistSongMap.put(playlist , list);

        //adding Listener
        List<User> arr = new ArrayList<>();
        arr.add(user);
        playlistListenerMap.put(playlist , arr);

        return "Success";
    }

    public ResponseEntity findPlaylist(String mobile, String playlistTitle) throws Exception {
        try {
            if(!userHashMap.containsKey(mobile))throw new Exception("User does'nt Exist");
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage() , HttpStatus.NOT_FOUND);
        }

        try {
            if(!playlistHashMap.containsKey(playlistTitle))throw new Exception("Playlist does not exist");
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage() , HttpStatus.NOT_FOUND);
        }


        Playlist playlist = playlistHashMap.get(playlistTitle);
        User user = userHashMap.get(mobile);

        //updating listenerList
        if(creatorPlaylistMap.containsKey(user) && creatorPlaylistMap.get(user).equals(playlist)){}
        else{
            List<User> list = playlistListenerMap.get(playlist);
            list.add(user);
        }

        return new ResponseEntity<>(playlist , HttpStatus.OK);


    }

    public ResponseEntity likeSong(String mobile, String songTitle) throws Exception {
        try {
            if(!userHashMap.containsKey(mobile))throw new Exception("User does'nt Exist");
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage() , HttpStatus.NOT_FOUND);
        }

        try {
            if(!songHashMap.containsKey(songTitle))throw new Exception("Song does not exist");
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage() , HttpStatus.NOT_FOUND);
        }

        User user = userHashMap.get(mobile);
        Song song = songHashMap.get(songTitle);


        if(songLikeMap.containsKey(song)){
            List<User> list = songLikeMap.get(song);
            if(!list.contains(user))list.add(user);
        }else{
            List<User> list = new ArrayList<>();
            list.add(user);
            songLikeMap.put(song , list);
        }

        song.setLikes(songLikeMap.get(song).size());
        //autoLiking artists
        autoLikeArtists(song);

        return new ResponseEntity<>(song , HttpStatus.OK);
    }

    public void autoLikeArtists(Song song) {
        Album album = getAlbumBySong(song);
        Artist artist = getArtistByAlbum(album);
        artist.setLikes(artist.getLikes() + 1);
    }

    private Artist getArtistByAlbum(Album album) {
        for(Artist x : artistAlbumMap.keySet()){
            for(Album a : artistAlbumMap.get(x)){
                if(a.equals(album))return x;
            }
        }
        return  null;
    }

    private Album getAlbumBySong(Song song) {
        for(Album x : albumSongMap.keySet()){
            for(Song s : albumSongMap.get(x)){
                if(s.equals(song))return x;
            }
        }
        return null;
    }

    public String mostPopularArtist() {
        Collections.sort(artists , (a , b) -> {return b.getLikes() - a.getLikes();});
        Artist artist = artists.get(0);
        return artist.getName();
    }

    public String mostPopularSong() {
        Collections.sort(songs , (a , b) -> {return b.getLikes() - a.getLikes();});
        Song song = songs.get(0);
        return song.getTitle();
    }
}
