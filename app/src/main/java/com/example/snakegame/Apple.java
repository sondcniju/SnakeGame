package com.example.snakegame;

import static com.example.snakegame.GameView.sizeElementMap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.ArrayList;

public class Apple {
    private Bitmap bm;
    private int x, y;
    private Rect r;

    public Apple(Bitmap bm, int x, int y) {
        this.bm = bm;
        this.x = x;
        this.y = y;
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(bm, x, y, null);
    }

    public void reset(int nx, int ny){
        this.x = nx;
        this.y = ny;
    }

    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        this.bm = bm;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Rect getR() {
        return new Rect(this.x, this.y, this.x+ sizeElementMap, this.y+ sizeElementMap);
    }

    public void setR(Rect r) {
        this.r = r;
    }

    public ArrayList<Rect> getBigAppleRects() {
        ArrayList<Rect> rects = new ArrayList<>();
        int radius = getBm().getWidth() / 2;
        for (int x = getX(); x < getX() + getBm().getWidth(); x += sizeElementMap) {
            for (int y = getY(); y < getY() + getBm().getHeight(); y += sizeElementMap) {
                rects.add(new Rect(x, y, x + sizeElementMap, y + sizeElementMap));
            }
        }
        return rects;
    }
}
