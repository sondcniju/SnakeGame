package com.example.snakegame;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static ImageView img_swipe;
    public static Dialog dialogScore;
    private GameView gv;
    public static TextView txt_score, txt_best_score, txt_dialog_score, txt_dialog_best_score;
    private int currentSkinIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Constants.SCREEN_WIDTH = dm.widthPixels;
        Constants.SCREEN_HEIGHT = dm.heightPixels;
        setContentView(R.layout.activity_main);
        img_swipe = findViewById(R.id.img_swipe);
        gv = findViewById(R.id.gv);
        txt_score = findViewById(R.id.txt_score);
        txt_best_score = findViewById(R.id.txt_best_score);
        dialogScore();
    }

    private void dialogScore() {
        int bestScore = 0;
        SharedPreferences sp = this.getSharedPreferences("gamesetting", Context.MODE_PRIVATE);
        if(sp!=null){
            bestScore = sp.getInt("bestscore",0);
        }
        MainActivity.txt_best_score.setText(bestScore+"");
        dialogScore = new Dialog(this);
        dialogScore.setContentView(R.layout.dialog_start);
        txt_dialog_score = dialogScore.findViewById(R.id.txt_dialog_score);
        txt_dialog_best_score = dialogScore.findViewById(R.id.txt_dialog_best_score);
        txt_dialog_best_score.setText(bestScore + "");
        dialogScore.setCanceledOnTouchOutside(false);
        RelativeLayout rl_start = dialogScore.findViewById(R.id.rl_start);
        RelativeLayout rl_skin = dialogScore.findViewById(R.id.rl_skin);
        rl_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_swipe.setVisibility(View.VISIBLE);
                gv.reset();
                dialogScore.dismiss();
            }
        });
        rl_skin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSkinSelectionDialog();
            }
        });
        dialogScore.show();
    }

    private void showSkinSelectionDialog() {
        final Dialog skinSelectionDialog = new Dialog(this);
        skinSelectionDialog.setContentView(R.layout.layout_skin_selection);

        ImageView iv_skin_option1 = skinSelectionDialog.findViewById(R.id.iv_skin_option1);
        ImageView iv_skin_option2 = skinSelectionDialog.findViewById(R.id.iv_skin_option2);
        ImageView iv_skin_option3 = skinSelectionDialog.findViewById(R.id.iv_skin_option3);
        ImageView iv_skin_option4 = skinSelectionDialog.findViewById(R.id.iv_skin_option4);

        // Thiết lập sự kiện click cho các ô skin
        iv_skin_option1.setOnClickListener(v -> selectSkin(0, skinSelectionDialog));
        iv_skin_option2.setOnClickListener(v -> selectSkin(1, skinSelectionDialog));
        iv_skin_option3.setOnClickListener(v -> selectSkin(2, skinSelectionDialog));
        iv_skin_option4.setOnClickListener(v -> selectSkin(3, skinSelectionDialog));

        skinSelectionDialog.show();
    }

    private void selectSkin(int skinIndex, Dialog skinSelectionDialog) {
        currentSkinIndex = skinIndex;
        skinSelectionDialog.dismiss();
        updateSnakeSkin();
    }

    private void updateSnakeSkin() {

    }
}