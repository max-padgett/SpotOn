package com.example.macadoshus.spoton;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Macadoshus on 5/7/2017.
 */

public class HighScores implements Serializable{
    private int highscore;
    private Date date;

    HighScores(int h, Date d){
        this.date = d;
        this.highscore = h;
    }

    public int getHighscore(){
        return highscore;
    }

    @Override
    public String toString(){
        return highscore + "\t on " + date.toString() + "\n";
    }
}
