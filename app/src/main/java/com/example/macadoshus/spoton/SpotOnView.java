package com.example.macadoshus.spoton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import android.app.AlertDialog.Builder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import android.os.Handler;

import com.google.gson.Gson;


/**
 * Created by Macadoshus on 5/7/2017.
 */
public class SpotOnView extends View{

    private static final String HIGH_SCORE = "HIGH_SCORE";
    private SharedPreferences preferences;

    private int spotsTouched;
    private int score;
    private int level;
    private int viewWidth;
    private int viewHeight;
    private long animationTime;
    private boolean gameOver;
    private boolean gamePaused;
    private boolean dialogDisplayed;
    private int highScore;

    private final Queue<ImageView> spots = new ConcurrentLinkedQueue<>();
    private final Queue<Animator> animators = new ConcurrentLinkedQueue<>();

    private TextView highScoreTextView;
    private TextView currentScoreTextView;
    private TextView levelTextView;
    private LinearLayout livesLinearLayout;
    private RelativeLayout relativeLayout;
    private Resources resources;
    private LayoutInflater layoutInflater;

    private static final int INITIAL_ANIMATION_DURATION = 6000;
    private static final Random random = new Random();
    private static final int SPOT_DIAMETER = 100;
    private static final float SCALE_X = 0.25f;
    private static final float SCALE_Y = 0.25f;
    private static final int INITIAL_SPOTS = 5;
    private static final int SPOT_DELAY = 500;
    private static final int LIVES = 3;
    private static final int MAX_LIVES = 7;
    private static final int NEW_LEVEL = 10;
    private Handler spotHandler;

    private static final int HIT_SOUND_ID = 1;
    private static final int MISS_SOUND_ID = 2;
    private static final int DISAPPREAR_SOUND_ID = 3;
    private static final int SOUND_PRIORITY = 1;
    private static final int SOUND_QUALITY = 100;
    private static final int MAX_STREAMS = 4;
    private SoundPool soundPool;
    private int volume;
    private Map<Integer, Integer> soundMap;

    //list of highscores
   private HighScoresQueue newQueue = new HighScoresQueue();

    public SpotOnView(Context context, SharedPreferences sharedPreferences, RelativeLayout parentLayout) {
        super(context);

        preferences = sharedPreferences;
        highScore = preferences.getInt(HIGH_SCORE, 0);

        resources = context.getResources();

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        relativeLayout = parentLayout;
        livesLinearLayout = (LinearLayout) relativeLayout.findViewById(R.id.lifeLinearLayout);
        highScoreTextView = (TextView) relativeLayout.findViewById(R.id.highScoreTextView);
        currentScoreTextView = (TextView) relativeLayout.findViewById(R.id.scoreTextView);
        levelTextView = (TextView) relativeLayout.findViewById(R.id.levelTextView);

        if(preferences.contains("HighScoreList")) {
            Gson gson = new Gson();
            String json = preferences.getString("HighScoreList", "");
            newQueue = gson.fromJson(json, newQueue.getClass());
        }

        spotHandler = new Handler();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh){
        viewWidth = width;
        viewHeight = height;
    }

    public void createList(){

    }


    public void pause(){
        gamePaused = true;
        soundPool.release();
        soundPool = null;
        cancelAnimations();
    }

    public boolean isGameOver(){
        return gameOver;
    }

    private void cancelAnimations(){
        for(Animator animator : animators)
            animator.cancel();

        for(ImageView view : spots)
            relativeLayout.removeView(view);

        spotHandler.removeCallbacks(addSpotRunnable);
        animators.clear();
        spots.clear();
    }

    public void resume(Context context){
        gamePaused = false;
        initializeSoundEffects(context);

        if(!dialogDisplayed)
            resetGame();
    }

    private void resetGame(){
        spots.clear();
        animators.clear();
        livesLinearLayout.removeAllViews();

        animationTime = INITIAL_ANIMATION_DURATION;
        spotsTouched = 0;
        score = 0;
        level = 1;
        gameOver = false;
        displayScores();

        for(int i = 0; i < LIVES; i++){
            livesLinearLayout.addView((ImageView) layoutInflater.inflate(R.layout.life, null));
        }

        for(int i = 1; i<= INITIAL_SPOTS; ++i){
            spotHandler.postDelayed(addSpotRunnable, i * SPOT_DELAY);
        }
    }

    private void initializeSoundEffects(Context context){

        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, SOUND_QUALITY);

        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);

        soundMap = new HashMap<Integer, Integer>();

        soundMap.put(HIT_SOUND_ID, soundPool.load(context, R.raw.hit, SOUND_PRIORITY));
        soundMap.put(MISS_SOUND_ID, soundPool.load(context, R.raw.miss, SOUND_PRIORITY));
        soundMap.put(DISAPPREAR_SOUND_ID, soundPool.load(context, R.raw.disappear, SOUND_PRIORITY));
    }

    private void displayScores(){
        highScoreTextView.setText(resources.getString(R.string.highscore) + " " + highScore);
        currentScoreTextView.setText(resources.getString(R.string.score) + " " + score);
        levelTextView.setText(resources.getString(R.string.level)+ " " + level);
    }

    public HighScoresQueue getHighscoreList(){
        return  newQueue;
    }

    private Runnable addSpotRunnable = new Runnable() {
        @Override
        public void run() {
            addNewSpot();
        }
    };

    public void addNewSpot(){
        int x = random.nextInt(viewWidth - SPOT_DIAMETER);
        int y = random.nextInt(viewHeight - SPOT_DIAMETER);
        int x2 = random.nextInt(viewWidth - SPOT_DIAMETER);
        int y2 = random.nextInt(viewHeight - SPOT_DIAMETER);

        final ImageView spot = (ImageView) layoutInflater.inflate(R.layout.untouched, null);
        spots.add(spot);
        spot.setLayoutParams(new RelativeLayout.LayoutParams(SPOT_DIAMETER, SPOT_DIAMETER));
        spot.setImageResource(random.nextInt(2)==0 ? R.drawable.green_spot : R.drawable.red_spot);
        spot.setX(x);
        spot.setY(y);
        spot.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        touchedSpot(spot);
                    }
                }
        );
        relativeLayout.addView(spot);

        spot.animate().x(x2).y(y2).scaleX(SCALE_X).scaleY(SCALE_Y).setDuration(animationTime).setListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        animators.add(animation);
                    }
                    @Override
                    public void onAnimationEnd(Animator animation){
                        animators.remove(animation);

                        if(!gamePaused && spots.contains(spot)){
                            missedSpot(spot);
                        }
                    }
                }
        );

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(soundPool != null){
            soundPool.play(MISS_SOUND_ID, volume, volume, SOUND_PRIORITY, 0, 1f);
        }

        score -= 15 * level;
        score = Math.max(score, 0);
        displayScores();
        return true;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static boolean checkImageResource(Context ctx, ImageView imageView,
                                             int imageResource) {
        boolean result = false;

        if (ctx != null && imageView != null && imageView.getDrawable() != null) {
            Drawable.ConstantState constantState;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                constantState = ctx.getResources()
                        .getDrawable(imageResource, ctx.getTheme())
                        .getConstantState();
            } else {
                constantState = ctx.getResources().getDrawable(imageResource)
                        .getConstantState();
            }

            if (imageView.getDrawable().getConstantState() == constantState) {
                result = true;
            }
        }

        return result;
    }

    public void touchedSpot(ImageView spot){
        if(checkImageResource(spot.getContext(), spot, R.drawable.green_spot)){


            relativeLayout.removeView(spot);
            spots.remove(spot);

            ++spotsTouched;
            score += 10 * level;

            if(soundPool != null){
                soundPool.play(HIT_SOUND_ID, volume, volume, SOUND_PRIORITY, 0, 1f);
            }

            if(spotsTouched % 10 == 0){
                ++level;
                animationTime *= 0.95;

                if(livesLinearLayout.getChildCount() < MAX_LIVES){
                    ImageView life = (ImageView) layoutInflater.inflate(R.layout.life, null);
                    livesLinearLayout.addView(life);
                }
            }

            displayScores();

            if(!gameOver)
                addNewSpot();

        }
        else
            spot.setImageResource(R.drawable.green_spot);
    }

    public void missedSpot(ImageView spot){
        spots.remove(spot);
        relativeLayout.removeView(spot);

        if(gameOver)
            return;

        if(soundPool != null)
            soundPool.play(DISAPPREAR_SOUND_ID, volume, volume, SOUND_PRIORITY, 0, 1f);

        if(livesLinearLayout.getChildCount() == 0) {
            gameOver = true;
            if (score > highScore) {
                newQueue.highScoresQueue.add(new HighScores(score , new Date()));
                SharedPreferences.Editor editor = preferences.edit();
                Gson gson = new Gson();
                String json = gson.toJson(newQueue);
                editor.putString("HighScoreList", json);
                editor.putInt(HIGH_SCORE, score);
                editor.commit();
                highScore = score;
            }else if(newQueue.greaterThan(score)){

            }

            cancelAnimations();

            Builder dialogBuilder = new AlertDialog.Builder(getContext());
            dialogBuilder.setTitle(R.string.game_over);
            dialogBuilder.setMessage("Score: " + score);
            dialogBuilder.setPositiveButton(R.string.reset_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    displayScores();
                    dialogDisplayed = false;
                    resetGame();
                }
            });
            dialogDisplayed = true;
            dialogBuilder.show();
        }
        else{
            livesLinearLayout.removeViewAt(livesLinearLayout.getChildCount() - 1);
            addNewSpot();
        }
    }
}
