package com.poupa.vinylmusicplayer;


import static junit.framework.TestCase.assertEquals;

import com.poupa.vinylmusicplayer.misc.queue.IndexedSong;
import com.poupa.vinylmusicplayer.misc.queue.StaticPlayingQueue;
import com.poupa.vinylmusicplayer.model.Song;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RunWith(JUnit4.class)
public class StaticPlayingQueueTest {

    private final int id_a = 12;
    private final int id_b = 123;
    private final int id_c = 56;
    private final int id_d = 13;
    private final int id_e = 1;
    private final int id_f = 40;

    private void print(StaticPlayingQueue test) {
        System.out.print("original queue: ");
        System.out.println(Arrays.toString(test.getOriginalPlayingQueue().toArray()));

        System.out.print("         queue: ");
        System.out.println(Arrays.toString(test.getPlayingQueue().toArray()));

        System.out.print("         position: ");
        System.out.println(test.getCurrentPosition());
    }

    private StaticPlayingQueue init() {
        StaticPlayingQueue test = new StaticPlayingQueue();

        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(id_a, "a", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song2 = new Song(id_b, "b", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song3 = new Song(id_c, "c", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song4 = new Song(id_d, "d", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

        ArrayList<Song> list = new ArrayList<>();
        list.add(song1);
        list.add(song2);
        list.add(song3);
        list.add(song4);

        test.addAll(list);

        return test;
    }

    private void checkQueuePosition(StaticPlayingQueue test) throws Exception {
        for (int i = 0; i < test.size(); i++) {
            IndexedSong song = test.getPlayingQueue().get(i);

            assertEquals(test.getOriginalPlayingQueue().get(song.index).title, song.title);
        }
    }

    @Test
    public void toggleShuffle() throws Exception {
        // init
        StaticPlayingQueue init = init();
        init.setCurrentPosition(2);

        StaticPlayingQueue test = init();
        test.setCurrentPosition(2);
        System.out.println("Init");
        print(test);

        // test
        System.out.println("Shuffle 1");
        test.setShuffle(StaticPlayingQueue.SHUFFLE_MODE_SHUFFLE);
        print(test);

        assertEquals(0, test.getCurrentPosition());
        checkQueuePosition(test);

        System.out.println("Shuffle 2");
        test.setShuffle(StaticPlayingQueue.SHUFFLE_MODE_NONE);
        print(test);

        assertEquals(init.getCurrentPosition(), test.getCurrentPosition());
        checkQueuePosition(test);

        assertEquals(init.getPlayingQueue().toString(), test.getPlayingQueue().toString());
    }

    @Test
    public void add_Song() throws Exception {
        // init
        StaticPlayingQueue test = init();
        test.setShuffle(StaticPlayingQueue.SHUFFLE_MODE_SHUFFLE);
        System.out.println("Init");
        print(test);

        // test
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(id_e, "e", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

        System.out.println("Add song");
        test.add(song1);
        print(test);

        checkQueuePosition(test);
    }

    @Test
    public void addAfter_Position() throws Exception {
        // init
        StaticPlayingQueue test = init();
        test.setShuffle(StaticPlayingQueue.SHUFFLE_MODE_SHUFFLE);
        System.out.println("Init");
        print(test);

        // test
        int pos = 2;
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(id_e, "e", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

        System.out.println("Add after position: "+pos);
        test.addAfter(pos, song1);
        print(test);

        checkQueuePosition(test);
    }

    @Test
    public void addSongBackTo() throws Exception {
        // init
        StaticPlayingQueue test = init();
        test.setShuffle(StaticPlayingQueue.SHUFFLE_MODE_SHUFFLE);
        System.out.println("Init");
        print(test);

        StaticPlayingQueue init = new StaticPlayingQueue(test.getPlayingQueue(), test.getOriginalPlayingQueue(), test.getCurrentPosition(), test.getShuffleMode(), test.getRepeatMode());

        // test
        int pos = 2;

        System.out.println("Remove position: "+pos);
        IndexedSong song = test.getPlayingQueue().get(pos);
        test.remove(pos);
        print(test);

        System.out.println("Add back to position: "+pos);
        test.addSongBackTo(pos, song);
        print(test);

        checkQueuePosition(test);
        assertEquals(init.getPlayingQueue().toString(), test.getPlayingQueue().toString());
    }

    @Test
    public void addAll_Songs() throws Exception {
        // init
        StaticPlayingQueue test = init();

        print(test);

        System.out.println("Shuffle");
        test.setShuffle(StaticPlayingQueue.SHUFFLE_MODE_SHUFFLE);
        print(test);

        // test
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(id_e, "e", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song2 = new Song(id_f, "f", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

        ArrayList<Song> list = new ArrayList<>();
        list.add(song1);
        list.add(song2);

        System.out.println("AddAll songs");
        test.addAll(list);
        print(test);

        checkQueuePosition(test);
    }

    @Test
    public void addAllAfter_Position() throws Exception {
        // init
        StaticPlayingQueue test = init();

        print(test);

        System.out.println("Shuffle");
        test.setShuffle(StaticPlayingQueue.SHUFFLE_MODE_SHUFFLE);
        print(test);

        // test
        int pos = 2;
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(id_e, "e", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song2 = new Song(id_f, "f", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

        ArrayList<Song> list = new ArrayList<>();
        list.add(song1);
        list.add(song2);

        System.out.println("AddAll after position: "+pos);
        test.addAllAfter(pos, list);
        print(test);

        checkQueuePosition(test);
    }

    @Test
    public void addAllAfter_EmptyQueue() throws Exception {
        // init, with empty queue
        StaticPlayingQueue test = init();
        test.clear();

        // test
        int pos = 0;
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(id_e, "e", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song2 = new Song(id_f, "f", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

        ArrayList<Song> list = new ArrayList<>();
        list.add(song1);
        list.add(song2);

        System.out.println("AddAll after position: "+pos);
        test.addAllAfter(pos, list);
        print(test);

        checkQueuePosition(test);
    }

    @Test
    public void move() throws Exception {
        // init
        StaticPlayingQueue test = init();

        print(test);

        // test
        int from = 3;
        int to = 1;

        System.out.println("Move from: "+from+" to: "+to);
        test.move(from, to);
        print(test);

        checkQueuePosition(test);

        System.out.println("Shuffle");
        test.setShuffle(StaticPlayingQueue.SHUFFLE_MODE_SHUFFLE);
        print(test);

        System.out.println("Move from: "+from+" to: "+to);
        test.move(from, to);
        print(test);

        checkQueuePosition(test);
    }

    @Test
    public void remove() throws Exception {
        // init
        StaticPlayingQueue test = init();

        print(test);

        System.out.println("Shuffle");
        test.setShuffle(StaticPlayingQueue.SHUFFLE_MODE_SHUFFLE);
        print(test);

        // test
        int pos = 2;

        test.remove(pos);

        System.out.println("Remove position: "+pos);
        print(test);

        checkQueuePosition(test);
    }

    @Test
    public void removeSongs_OnCurrentPosition() throws Exception {
        // init
        StaticPlayingQueue test = init();

        // test
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(id_a, "a", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song2 = new Song(id_c, "c", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

        test.setCurrentPosition(2);
        print(test);

        ArrayList<Song> list = new ArrayList<>();
        list.add(song1);
        list.add(song2);

        System.out.println("RemoveSongs");
        boolean hasPositionChanged = (test.removeSongs(list) != -1);
        print(test);

        assertEquals(true, hasPositionChanged);
        checkQueuePosition(test);
    }

    @Test
    public void removeSongs_OtherThanCurrentPosition() throws Exception {
        // init
        StaticPlayingQueue test = init();

        // test
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(id_a, "a", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song2 = new Song(id_c, "c", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

        test.setCurrentPosition(1);
        print(test);

        ArrayList<Song> list = new ArrayList<>();
        list.add(song1);
        list.add(song2);

        System.out.println("RemoveSongs");
        boolean hasPositionChanged = (test.removeSongs(list) != -1);
        print(test);

        assertEquals(false, hasPositionChanged);
        checkQueuePosition(test);
    }
}
