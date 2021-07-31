package com.poupa.vinylmusicplayer;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import com.poupa.vinylmusicplayer.helper.ShuffleHelper;
import com.poupa.vinylmusicplayer.misc.queue.PositionSong;
import com.poupa.vinylmusicplayer.misc.queue.ShufflingQueue;
import com.poupa.vinylmusicplayer.model.Song;

import org.junit.runners.JUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertEquals;


@RunWith(JUnit4.class)
public class ShufflingQueueTest {

    private void print(ShufflingQueue test) {
        System.out.print("original queue: ");
        System.out.println(Arrays.toString(test.queue.getAllPreviousState().toArray()));

        System.out.print("         queue: ");
        System.out.println(Arrays.toString(test.queue.getAll().toArray()));
    }

    private ShufflingQueue init() {
        ShufflingQueue test = new ShufflingQueue();

        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(12, "a", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song2 = new Song(12, "b", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song3 = new Song(12, "c", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song4 = new Song(12, "d", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

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

        ShufflingQueue test = init();
        System.out.println("Init");
        print(test);

        // test
        System.out.println("Shuffle 1");
        test.toggleShuffle();
        print(test);

        checkQueuePosition(test);

        System.out.println("Shuffle 2");
        test.toggleShuffle();
        print(test);

        checkQueuePosition(test);

        assertEquals(init.queue.toString(), test.queue.toString());
    }

    @Test
    public void add_Song() throws Exception {
        // init
        ShufflingQueue test = init();
        test.toggleShuffle();
        System.out.println("Init");
        print(test);

        // test
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(12, "e", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

        System.out.println("Add song");
        test.add(song1);
        print(test);

        checkQueuePosition(test);
    }

    @Test
    public void add_Position() throws Exception {
        // init
        ShufflingQueue test = init();
        test.toggleShuffle();
        System.out.println("Init");
        print(test);

        // test
        int pos = 2;
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(12, "e", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

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
        test.toggleShuffle();
        print(test);

        // test
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(12, "e", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song2 = new Song(12, "f", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

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
        test.toggleShuffle();
        print(test);

        // test
        int pos = 2;
        List<String> artistName = new ArrayList<>();
        Song song1 = new Song(12, "e", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);
        Song song2 = new Song(12, "f", 0, 2012, 50, "", 0, 0, 0, "", 0, artistName);

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
        test.toggleShuffle();
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
        test.toggleShuffle();
        print(test);

        // test
        int pos = 2;

        test.remove(pos);

        System.out.println("Remove position: "+pos);
        print(test);

        checkQueuePosition(test);
    }
}
