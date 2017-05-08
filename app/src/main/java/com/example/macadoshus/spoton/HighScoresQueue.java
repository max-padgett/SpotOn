package com.example.macadoshus.spoton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Macadoshus on 5/7/2017.
 */

public class HighScoresQueue {

    public List<HighScores> highScoresQueue = new ArrayList<>(10);

    public boolean greaterThan(int n) {
            if (highScoresQueue.size() < 10) {
                highScoresQueue.add(new HighScores(n, new Date()));
                Collections.sort(highScoresQueue, new Comparator<HighScores>() {
                    @Override
                    public int compare(HighScores o1, HighScores o2) {
                        return o1.getHighscore() > o2.getHighscore() ? -1 : (o2.getHighscore() < o2.getHighscore() ) ? 1 : 0;
                    }
                });
                return true;
            }

            for (HighScores h : highScoresQueue) {
                if (h.getHighscore() <= n) {
                    highScoresQueue.set(highScoresQueue.indexOf(h), new HighScores(n, new Date()));
                    return true;
                }
            }

        return false;
    }

        @Override
        public String toString () {
            String list = new String();
            for (HighScores h : highScoresQueue) {
                list += h.toString();
            }
            return list;
        }


}

