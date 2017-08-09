package com.tistory.puzzleleaf.rankofalcohol.animation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tistory.puzzleleaf.rankofalcohol.fb.FbDataBase;
import com.tistory.puzzleleaf.rankofalcohol.util.mode.ModePreference;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.MouseEvent;

/**
 * Created by cmtyx on 2017-04-08.
 */

public class MainAnimation extends PApplet implements ChatMode.OnChatMessageListener {

    int maxStar = 500;
    int starLimit = 300;
    int moonSize;
    int moonAlphSize;
    int moonShadowSize;

    //그림자 이동 관련
    float location = 0;
    float moveSpeed;
    float startShadow;
    float endShadow;

    PFont font;

    Star myStar[];
    ArrayList<Point> myArr;

    private static final String MODE = "MODE";
    private int modeCheck = 1;

    //Mode
    private ModeBroadCastReceiver modeBroadCastReceiver;
    private ChatMode chatMode;
    private DisplayMode displayMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatModeInit();
        displayModeInit();
        broadCastInit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(modeBroadCastReceiver);
    }

    @Override
    public void settings() {
        super.settings();
        fullScreen();
    }

    public void setup() {
        myArr = new ArrayList(); // 글자
        myStar = new Star[maxStar];

        textAlign(CENTER);
        font = loadFont("휴먼가는샘체-70.vlw");

        for(int i=0;i<maxStar;i++){
            myStar[i] = new Star();
        }

        moonSize = width/4;
        moonAlphSize = moonSize+30;
        moonShadowSize = moonSize+100;

        startShadow = moonSize+width/2+100;
        endShadow = width/2-moonSize-100;
        location = startShadow;
        moveSpeed = width/800;


    }

    public void draw()
    {
        background(7,20,29,100);
        starAnimation();
        chatMessageAnimation();
        displayModeAnimation();

    }

    private void displayModeAnimation(){
        if(modeCheck!=4) {
            return;
        }
        pushMatrix();
        translate(width / 2, height / 2);
        rotate(HALF_PI);
        fill(displayMode.getrColor(),displayMode.getgColor(),displayMode.getbColor(),random(0,255));
        textSize(height / 8);
        text(displayMode.getText(), 0, 0);
        popMatrix();
    }


    //달 애니메이션
    private void moonAnimation(){
        drawMoon();
        drawShadow(location,height/4);
        location-=moveSpeed;

        if(location<endShadow){
            location = startShadow;
        }
    }

    // 별 반짝임 (기본 애니메이션)
    private void starAnimation(){
        for(int i=0;i<starLimit;i++){
            myStar[i].shine();
            myStar[i].display();
        }
    }

    //채팅 메시지
    private void chatMessageAnimation(){
        if(modeCheck!=2){
            return;
        }
        for (int i = 0; i < myArr.size(); i++) {
            try {
                myArr.get(i).update();
                myArr.get(i).display();
            }catch (Exception e){

            }
        }
    }

    @Override
    public void mousePressed() {
        super.mousePressed();
        if(mouseY<height/2) {
            moveSpeed += 1;
        }else{
            if(starLimit<maxStar-1) {
                starLimit += 50;
            }
        }
    }

    void drawMoon(){
        for(int i=1; i<50; i++) {
            fill(245,10);
            ellipse(width/2, height/4,moonAlphSize-i*5,moonAlphSize+30-i*5);
            ellipse(width/2, height/4,moonSize,moonSize);
        }
    }

    void drawShadow(float x, float y) {
        for(int i=1; i<30; i++) {
            fill(7,20,29,100);
            ellipse(x, y,moonShadowSize-i*5,moonShadowSize-i*5);
        }
    }

    void setText(String txt)
    {
        if(myArr!=null) {
            myArr.clear();
        }
        PGraphics pg = createGraphics(width, height);
        pg.beginDraw();
        pg.fill(0);
        pg.textAlign(CENTER);
        pg.textFont(font,width/6);
        pg.text(txt,width/2,height/4);
        pg.endDraw();
        pg.loadPixels();

        int val = width/150;

        for(int h=0;h<height;h+=val) {
            for (int w = 0; w < width; w += val) {
                if (pg.pixels[width * h + w] != 0) {
                    Point temp = new Point(w, h);
                    myArr.add(temp);
                }
            }
        }
        pg.bitmap.recycle();
        pg.dispose();

    }

    //별 객체
    class Star {
        PVector location;
        float brightness;
        float starSize;
        float light;
        float starMinSize = 2;
        float starMaxSize = 5;

        Star() {
            noStroke();
            location = new PVector(random(width),random(height));
            starSize = random(starMinSize,starMaxSize);
            light = random(3,10);
            brightness =30;
        }

        void display() {
            for(int i=0;i<starSize;i++) {
                fill(255,brightness-i*5);
                ellipse(location.x,location.y,i,i);
            }
        }

        //빛나는 효과
        void shine() {
            if(brightness>255){
                brightness =255;
                light = random(-3,-10);
            }
            if(brightness<0) {
                location = new PVector(random(width),random(height));
                light = random(3,10);
                starSize = random(starMinSize,starMaxSize);
                brightness =0 ;
            }
            brightness +=light;

        }

    }

    class Point
    {
        PVector pos;
        PVector des;
        PVector vel;
        PVector acc;

        int speed = (int)random(8,16);
        float size = random(3,7);

        Point(float x, float y) {
            pos = new PVector(random(0,width),random(0,height));
            des = new PVector(x,y);
            vel = new PVector(0,0);
            acc = new PVector(0,0);
        }

        void display() {
            fill(255);
            ellipse(pos.x,pos.y,size,size);
        }

        void update() {
            float distance = dist(pos.x,pos.y,des.x,des.y);
            float proximityMult = (float) 1.0;

            if(distance<50) {
                proximityMult = distance/50;
            }

            //벡터의 방향과 크기를 정해준다.
            PVector toward = new PVector(this.des.x,this.des.y);
            toward.sub(pos);
            toward.normalize();
            toward.mult(proximityMult*speed);


            PVector steer = new PVector(toward.x,toward.y);
            steer.sub(vel);


            acc.add(steer);
            vel.add(acc);
            pos.add(vel);

            acc.mult(0);
        }
    }


    private void chatModeInit(){
        chatMode = new ChatMode(this);
        chatMode.chatModeListenerInit();
    }
    private void displayModeInit(){
        displayMode = new DisplayMode(getActivity().getApplicationContext());
    }

    //ChatMode 실시간 리스너
    @Override
    public void onChatValueListener(String value) {
        setText(value);
    }

    private void broadCastInit(){
        modeBroadCastReceiver = new ModeBroadCastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MODE);
        getActivity().registerReceiver(modeBroadCastReceiver,filter);
    }


    private class ModeBroadCastReceiver extends BroadcastReceiver{

        private static final int MODE_BASIC = 1;
        private static final int MODE_CHAT = 2;
        private static final int MODE_GAME = 3;
        private static final int MODE_DISPLAY = 4;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(MODE)){
                modeCheck = intent.getIntExtra("mode",MODE_BASIC);
                switch (modeCheck){
                    case MODE_BASIC:
                        chatMode.dataLoadRemove();
                        if(font!=null) {
                            setText("");
                        }
                        break;
                    case MODE_CHAT:
                        chatMode.dataLoadInit();
                        break;
                    case MODE_GAME:
                        break;
                    case MODE_DISPLAY:
                        displayMode.displayDataLoad();
                        Log.d("qwe", String.valueOf(modeCheck));
                        break;
                }

            }

        }
    }
}
