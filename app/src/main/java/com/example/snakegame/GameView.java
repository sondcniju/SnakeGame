package com.example.snakegame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends View {
    private MediaPlayer mediaPlayer;
    private Bitmap bmGrass1, bmGrass2, bmSnake1, bmApple;
    private ArrayList<Grass> arrGrass = new ArrayList<>();
    private int w = 12, h = 21;
    public static int sizeElementMap = 75 * Constants.SCREEN_WIDTH / 1080;
    private Snake snake;
    private Apple apple;
    private Handler handler;
    private Runnable r;
    private boolean move = false;
    private float mx, my;
    public static boolean isPlaying = false;
    public static int score = 0, bestScore = 0;
    private Context context;
    private int soundEat, soundDie;
    private float volume;
    private boolean loadedsound;
    private SoundPool soundPool;

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        SharedPreferences sp = context.getSharedPreferences("gamesetting", Context.MODE_PRIVATE);
        // Initialize the MediaPlayer for background music
        mediaPlayer = MediaPlayer.create(context, R.raw.background);
        mediaPlayer.setLooping(true); // Loop the background music
        if (sp != null) {
            bestScore = sp.getInt("bestscore", 0);
        }
        //tạo map
        bmGrass1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.grass);
        bmGrass1 = Bitmap.createScaledBitmap(bmGrass1, sizeElementMap, sizeElementMap, true);
        bmGrass2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.grass03);
        bmGrass2 = Bitmap.createScaledBitmap(bmGrass2, sizeElementMap, sizeElementMap, true);
        bmSnake1 = BitmapFactory.decodeResource(this.getResources(), R.drawable.snake1);
        bmSnake1 = Bitmap.createScaledBitmap(bmSnake1, 14 * sizeElementMap, sizeElementMap, true);
        bmApple = BitmapFactory.decodeResource(this.getResources(), R.drawable.apple);
        bmApple = Bitmap.createScaledBitmap(bmApple, sizeElementMap, sizeElementMap, true);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if ((j + i) % 2 == 0) {
                    arrGrass.add(new Grass(bmGrass1, j * bmGrass1.getWidth() + Constants.SCREEN_WIDTH / 2 - (w / 2) * bmGrass1.getWidth(), i * bmGrass1.getHeight() + 50 * Constants.SCREEN_HEIGHT / 1920, bmGrass1.getWidth(), bmGrass1.getHeight()));
                } else {
                    arrGrass.add(new Grass(bmGrass2, j * bmGrass2.getWidth() + Constants.SCREEN_WIDTH / 2 - (w / 2) * bmGrass2.getWidth(), i * bmGrass2.getHeight() + 50 * Constants.SCREEN_HEIGHT / 1920, bmGrass2.getWidth(), bmGrass2.getHeight()));
                }
            }
        }
        snake = new Snake(bmSnake1, arrGrass.get(126).getX(), arrGrass.get(126).getY(), 4);
        apple = new Apple(bmApple, arrGrass.get(randomApple()[0]).getX(), arrGrass.get(randomApple()[1]).getY());
        handler = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };
        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(audioAttributes).setMaxStreams(5);
            this.soundPool = builder.build();
        } else {
            soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loadedsound = true;
            }
        });
        soundEat = this.soundPool.load(context, R.raw.eat, 1);
        soundDie = this.soundPool.load(context, R.raw.die, 1);
    }

    private int[] snakeSkinResources = {
            R.drawable.snake1,
            R.drawable.snake2,
            R.drawable.snake3,
            R.drawable.snake4
    };

    public void setSnakeSkin(int skinIndex) {
        if (skinIndex >= 0 && skinIndex < snakeSkinResources.length) {
            Bitmap snakeSkin = BitmapFactory.decodeResource(getResources(), snakeSkinResources[skinIndex]);
            bmSnake1 = Bitmap.createScaledBitmap(snakeSkin, 14 * sizeElementMap, sizeElementMap, true);
            snake.setBm(bmSnake1);
        }
    }

    private int[] randomApple() {
        int[] xy = new int[2];
        Random r = new Random();
        xy[0] = r.nextInt(arrGrass.size() - 1);
        xy[1] = r.nextInt(arrGrass.size() - 1);
        Rect rect = new Rect(arrGrass.get(xy[0]).getX(), arrGrass.get(xy[1]).getY(), arrGrass.get(xy[0]).getX() + sizeElementMap, arrGrass.get(xy[1]).getY() + sizeElementMap);
        boolean check = true;
        while (check) {
            check = false;
            for (int i = 0; i < snake.getArrPartSnake().size(); i++) {
                if (rect.intersect(snake.getArrPartSnake().get(i).getrBody())) {
                    check = true;
                    xy[0] = r.nextInt(arrGrass.size() - 1);
                    xy[1] = r.nextInt(arrGrass.size() - 1);
                    rect = new Rect(arrGrass.get(xy[0]).getX(), arrGrass.get(xy[1]).getY(), arrGrass.get(xy[0]).getX() + sizeElementMap, arrGrass.get(xy[1]).getY() + sizeElementMap);
                }
            }
        }
        return xy;
    }

    @Override
    // điều khiển rắn
    public boolean onTouchEvent(MotionEvent event) {
        int a = event.getActionMasked();
        switch (a) {
            case MotionEvent.ACTION_MOVE: {
                if (move == false) {
                    mx = event.getX();
                    my = event.getY();
                    move = true;
                } else {
                    if (mx - event.getX() > 100 && !snake.isMove_right()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMove_left(true);
                        isPlaying = true;
                        MainActivity.img_swipe.setVisibility(INVISIBLE);
                    } else if (event.getX() - mx > 100 && !snake.isMove_left()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMove_right(true);
                        isPlaying = true;
                        MainActivity.img_swipe.setVisibility(INVISIBLE);
                    } else if (event.getY() - my > 100 && !snake.isMove_up()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMove_down(true);
                        isPlaying = true;
                        MainActivity.img_swipe.setVisibility(INVISIBLE);
                    } else if (my - event.getY() > 100 && !snake.isMove_down()) {
                        mx = event.getX();
                        my = event.getY();
                        this.snake.setMove_up(true);
                        isPlaying = true;
                        MainActivity.img_swipe.setVisibility(INVISIBLE);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                mx = 0;
                my = 0;
                move = false;
                break;
            }
        }
        return true;
    }

    private int timeCount = 0;
    private int initialDelay = 500; // Tốc độ ban đầu (500ms)
    private int deldayTime = initialDelay;
    private int appleCounterForBigApple = 0; // Đếm số lần ăn táo
    private int bigAppleInterval = 3; // Số lần cần ăn táo để có quả táo lớn (ban đầu là 3)

    public void draw(Canvas canvas) {
        super.draw(canvas);
        //điều kiện để điều khiển và vẽ thân theo đầu rắn và điều kiện kết thúc game
        canvas.drawColor(0xFF065700);
        for (int i = 0; i < arrGrass.size(); i++) {
            canvas.drawBitmap(arrGrass.get(i).getBm(), arrGrass.get(i).getX(), arrGrass.get(i).getY(), null);
        }
        if (isPlaying) {
            snake.update();
//            if(snake.getArrPartSnake().get(0).getX() < this.arrGrass.get(0).getX()
//                ||snake.getArrPartSnake().get(0).getY() < this.arrGrass.get(0).getY()
//                ||snake.getArrPartSnake().get(0).getY()+sizeElementMap>this.arrGrass.get(this.arrGrass.size()-1).getY() + sizeElementMap
//                    ||snake.getArrPartSnake().get(0).getX()+sizeElementMap>this.arrGrass.get(this.arrGrass.size()-1).getX() + sizeElementMap){
//                gameOver();

            for (int i = 1; i < snake.getArrPartSnake().size(); i++) {
                if (snake.getArrPartSnake().get(0).getrBody().intersect(snake.getArrPartSnake().get(i).getrBody())) {
                    gameOver();
                }
            }
            if (snake.getArrPartSnake().get(0).getX() < this.arrGrass.get(0).getX()) {
                snake.getArrPartSnake().get(0).setX(this.arrGrass.get(this.arrGrass.size() - 1).getX());
            } else if (snake.getArrPartSnake().get(0).getX() + sizeElementMap > this.arrGrass.get(this.arrGrass.size() - 1).getX() + sizeElementMap) {
                snake.getArrPartSnake().get(0).setX(this.arrGrass.get(0).getX());
            }

            if (snake.getArrPartSnake().get(0).getY() < this.arrGrass.get(0).getY()) {
                snake.getArrPartSnake().get(0).setY(this.arrGrass.get(this.arrGrass.size() - 1).getY());
            } else if (snake.getArrPartSnake().get(0).getY() + sizeElementMap > this.arrGrass.get(this.arrGrass.size() - 1).getY() + sizeElementMap) {
                snake.getArrPartSnake().get(0).setY(this.arrGrass.get(0).getY());
            }
        }
        snake.drawSnake(canvas);
        Rect snakeHead = new Rect(
                snake.getArrPartSnake().get(0).getX(),
                snake.getArrPartSnake().get(0).getY(),
                snake.getArrPartSnake().get(0).getX() + sizeElementMap,
                snake.getArrPartSnake().get(0).getY() + sizeElementMap
        );

        ArrayList<Rect> bigAppleRects = apple.getBigAppleRects();
        boolean hitBigApple = false;
        for (Rect rect : bigAppleRects) {
            if (snakeHead.intersect(rect)) {
                hitBigApple = true;
                break;
            }
        }

        if (hitBigApple) {
            if (loadedsound) {
                int streamId = this.soundPool.play(this.soundEat, (float) 0.5, (float) 0.5, 1, 0, 1f);
            }
            apple.reset(arrGrass.get(randomApple()[0]).getX(), arrGrass.get(randomApple()[1]).getY());
            snake.addPart();

            appleCounterForBigApple++;

            if (appleCounterForBigApple >= bigAppleInterval) {
                apple.setBm(Bitmap.createScaledBitmap(bmApple, sizeElementMap * 2, sizeElementMap * 2, true));
                score += 2; // Tăng điểm lên 2 khi ăn quả táo to
                appleCounterForBigApple = 0; // Đặt lại appleCounterForBigApple về 0 sau khi ăn quả táo to
                bigAppleInterval++;
            } else {
                apple.setBm(Bitmap.createScaledBitmap(bmApple, sizeElementMap, sizeElementMap, true));
                score++; // Tăng điểm lên 1 khi ăn quả táo bình thường
            }

            MainActivity.txt_score.setText(score + "");
            if (score > bestScore) {
                bestScore = score;
                SharedPreferences sp = context.getSharedPreferences("gamesetting", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("bestscore", bestScore);
                editor.apply();
                MainActivity.txt_best_score.setText(bestScore + "");
            }
        }
        apple.draw(canvas);
        timeCount++;
        if (timeCount % 500 == 0) {
            deldayTime -= 10;
        }
        handler.postDelayed(r, deldayTime);
        if (deldayTime < initialDelay / 2) {
            deldayTime = initialDelay;
        }
    }
    private void startBackgroundMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    private void stopBackgroundMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void gameOver() {
        isPlaying = false;
        MainActivity.dialogScore.show();
        MainActivity.txt_dialog_best_score.setText(bestScore + "");
        MainActivity.txt_dialog_score.setText(score + "");
        if (loadedsound) {
            int streamId = this.soundPool.play(this.soundDie, (float) 0.5, (float) 0.5, 1, 0, 1f);
        }
        handler.removeCallbacks(r); // Hủy bỏ handler.postDelayed() cũ
        deldayTime = initialDelay; // Đặt lại tốc độ về mức ban đầu
        timeCount = 0; // Đặt lại biến timeCount về 0
        handler.postDelayed(r, deldayTime); // Đặt lịch cho resetRunnable mới với tốc độ ban đầu
        appleCounterForBigApple = 0;
        stopBackgroundMusic();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Stop background music and release resources when the view is detached from window
        stopBackgroundMusic();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Start background music when the view is attached to window
        startBackgroundMusic();
    }
    public void reset() {
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if ((j + i) % 2 == 0) {
                    arrGrass.add(new Grass(bmGrass1, j * bmGrass1.getWidth() + Constants.SCREEN_WIDTH / 2 - (w / 2) * bmGrass1.getWidth(), i * bmGrass1.getHeight() + 50 * Constants.SCREEN_HEIGHT / 1920, bmGrass1.getWidth(), bmGrass1.getHeight()));
                } else {
                    arrGrass.add(new Grass(bmGrass2, j * bmGrass2.getWidth() + Constants.SCREEN_WIDTH / 2 - (w / 2) * bmGrass2.getWidth(), i * bmGrass2.getHeight() + 50 * Constants.SCREEN_HEIGHT / 1920, bmGrass2.getWidth(), bmGrass2.getHeight()));
                }
            }
        }
        snake = new Snake(bmSnake1, arrGrass.get(126).getX(), arrGrass.get(126).getY(), 4);
        apple = new Apple(bmApple, arrGrass.get(randomApple()[0]).getX(), arrGrass.get(randomApple()[1]).getY());
        score = 0;
    }
}
