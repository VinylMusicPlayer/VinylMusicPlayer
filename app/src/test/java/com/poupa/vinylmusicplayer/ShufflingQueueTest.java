package com.poupa.vinylmusicplayer;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.poupa.vinylmusicplayer.misc.queue.PositionSong;
import com.poupa.vinylmusicplayer.misc.queue.ShufflingQueue;
import com.poupa.vinylmusicplayer.model.Song;

import org.junit.runners.JUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertEquals;


@RunWith(JUnit4.class)
public class ShufflingQueueTest {

    private final int id_a = 12;
    private final int id_b = 123;
    private final int id_c = 56;
    private final int id_d = 13;
    private final int id_e = 1;
    private final int id_f = 40;

    private void print(ShufflingQueue test) {
        System.out.print("original queue: ");
        System.out.println(Arrays.toString(test.queue.getAllPreviousState().toArray()));

        System.out.print("         queue: ");
        System.out.println(Arrays.toString(test.queue.getAll().toArray()));

        System.out.print("         position: ");
        System.out.println(test.getCurrentPosition());
    }

    private ShufflingQueue init() {
        ShufflingQueue test = new ShufflingQueue();

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

    private void checkQueuePosition(ShufflingQueue test) throws Exception {
        for (int i = 0; i < test.queue.getAll().size(); i++) {
            PositionSong song = test.queue.getAll().get(i);

            assertEquals(test.queue.getAllPreviousState().get(song.position).song.title, song.song.title);
        }
    }

    @Test
    public void toggleShuffle() throws Exception {
        // init
        ShufflingQueue init = init();
        init.setCurrentPosition(2);

        ShufflingQueue test = init();
        test.setCurrentPosition(2);
        System.out.println("Init");
        print(test);

        // test
        System.out.println("Shuffle 1");
        test.setShuffle(true);
        print(test);

        assertEquals(0, test.getCurrentPosition());
        checkQueuePosition(test);

        System.out.println("Shuffle 2");
        test.setShuffle(false);
        print(test);

        assertEquals(init.getCurrentPosition(), test.getCurrentPosition());
        checkQueuePosition(test);

        assertEquals(init.queue.toString(), test.queue.toString());
    }

    @Test
    public void add_Song() throws Exception {
        // init
        ShufflingQueue test = init();
        test.setShuffle(true);
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
    public void add_Position() throws Exception {
        // init
        ShufflingQueue test = init();
        test.setShuffle(true);
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
    public void addAll_Songs() throws Exception {
        // init
        ShufflingQueue test = init();

        print(test);

        System.out.println("Shuffle");
        test.setShuffle(true);
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
    public void addAll_Position() throws Exception {
        // init
        ShufflingQueue test = init();

        print(test);

        System.out.println("Shuffle");
        test.setShuffle(true);
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
    public void move() throws Exception {
        // init
        ShufflingQueue test = init();

        print(test);

        // test
        int from = 3;
        int to = 1;

        System.out.println("Move from: "+from+" to: "+to);
        test.move(from, to);
        print(test);

        checkQueuePosition(test);

        System.out.println("Shuffle");
        test.setShuffle(true);
        print(test);

        System.out.println("Move from: "+from+" to: "+to);
        test.move(from, to);
        print(test);

        checkQueuePosition(test);
    }

    @Test
    public void remove() throws Exception {
        // init
        ShufflingQueue test = init();

        print(test);

        System.out.println("Shuffle");
        test.setShuffle(true);
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
        ShufflingQueue test = init();

        print(test);

        // test
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(id_a, "a", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song2 = new Song(id_c, "c", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

        test.setCurrentPosition(2);

        ArrayList<Song> list = new ArrayList<>();
        list.add(song1);
        list.add(song2);

        System.out.println("RemoveSongs");
        boolean hasPositionChanged = test.removeSongs(list);
        print(test);

        assertEquals(true, hasPositionChanged);
        checkQueuePosition(test);
    }

    @Test
    public void removeSongs_OtherThanCurrentPosition() throws Exception {
        // init
        ShufflingQueue test = init();

        print(test);

        // test
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(id_a, "a", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song2 = new Song(id_c, "c", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

        test.setCurrentPosition(1);

        ArrayList<Song> list = new ArrayList<>();
        list.add(song1);
        list.add(song2);

        System.out.println("RemoveSongs");
        boolean hasPositionChanged = test.removeSongs(list);
        print(test);

        assertEquals(false, hasPositionChanged);
        checkQueuePosition(test);
    }
}
