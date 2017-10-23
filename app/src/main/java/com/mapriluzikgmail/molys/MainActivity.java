package com.mapriluzikgmail.molys;

import android.graphics.drawable.AnimationDrawable;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    ImageView[] imageViews = new ImageView[9];
    TextView clockBar;
    AnimationDrawable[] drawables = new AnimationDrawable[imageViews.length];
    Button startBtn;
    LinearLayout titlelayout;
    LinearLayout linear;
    ImageView titleimg;

    boolean isPlay=false;// 트루==게임실행중
    int ms,s;//밀리세컨,세컨

    boolean isPaused=false;

    GameThread gameThread;
    ClockThread clockThread;

    Random rnd = new Random();
    int rndNum;
    TableLayout tableLayout;

    android.os.Handler handler = new android.os.Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            for(int i = 0;i<imageViews.length;i++){
                if(drawables[i].getNumberOfFrames()==18){
                    //catch이미지인지?
                    if(drawables[i].getCurrent()==
                            drawables[i].getFrame(drawables[i].getNumberOfFrames()-1)){

                        drawables[i].stop();
                        imageViews[i].setImageResource(R.drawable.ani_a_show);
                        drawables[i]=(AnimationDrawable)imageViews[i].getDrawable();

                    }
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clockBar = (TextView)findViewById(R.id.clockbar);
        startBtn = (Button)findViewById(R.id.btnStart);

        for(int i =0;i<imageViews.length;i++){

            imageViews[i] =(ImageView)findViewById(R.id.img_011+i);
            drawables[i]=(AnimationDrawable)imageViews[i].getDrawable();
        }

        titleimg = (ImageView)findViewById(R.id.title);
        tableLayout=(TableLayout)findViewById(R.id.table);
        linear=(LinearLayout)findViewById(R.id.linear);
    }

    public void clickThomas(View v){

        ImageView img = (ImageView)v;
        int num = img.getId() - R.id.img_011 ;//클릭된 이미지뷰의 방번호

        img.setClickable(false);
        drawables[num].stop();

        img.setImageResource(R.drawable.ani_a_catch);
        drawables[num]=(AnimationDrawable) img.getDrawable();
        drawables[num].start();
    }


    public void clickStart(View v){




        if(isPlay==false){
            isPlay=true;

            titleimg.setVisibility(View.INVISIBLE);
            startBtn.setVisibility(View.GONE);
            tableLayout.setVisibility(View.VISIBLE);
            linear.setVisibility(View.VISIBLE);

            clockBar.setVisibility(View.VISIBLE);

            clockThread=  new ClockThread();
            gameThread = new GameThread();

            clockThread.start();
            gameThread.start();

        }


    }

    public void clickPause(View v){

        if(isPaused==false&&isPlay==true){

            isPaused=true;

        }

    }


    public void clickResume(View v){

        if(isPlay==true&&isPaused==true){

            isPaused=false;
            gameThread.unPaused();
            clockThread.unPaused();

        }
    }




    class GameThread extends Thread{

        public void unPaused(){
            synchronized (this){
                this.notify();
            }
        }

        @Override
        public void run() {

            while (isPlay){

                if(isPaused){
                    synchronized (this){
                        try { this.wait();} catch (InterruptedException e) {}
                    }
                }



                for(int i=0;i<imageViews.length;i++){

                    if(drawables[i].isRunning()){

                        if(drawables[i].getCurrent()==
                                drawables[i].getFrame(drawables[i].getNumberOfFrames()-1)){
                            //현재그림(drawables[].getcurrent)이 지금가져온
                            // drawables스레드의 마지막그림(drawables[i].getFrame(drawables[i].getNumberOfFrames()-1)이랑
                            // 같다면 스탑


                            drawables[i].stop();
                            imageViews[i].setClickable(false);
                        }
                        handler.sendEmptyMessage(0);
                    }
                }

                rndNum = rnd.nextInt(9);


                imageViews[rndNum].setClickable(true);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawables[rndNum].start();
                    }
                });


                try { Thread.sleep(500);} catch (InterruptedException e) {}

            }//while


        }//run
    }

    class ClockThread extends Thread{

        public void unPaused(){
            synchronized (this){
                this.notify();
            }
        }

        @Override
        public void run() {

            s=20;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {  clockBar.setText(String.format("%02d : %02d",s,ms));}
            });

            while(isPlay){//트루일때만 돈다

                if(isPaused){
                    synchronized (this){
                        try { this.wait();} catch (InterruptedException e) {}
                    }
                }


                if(ms==0){
                    s--;
                    ms=99;
                }
                ms--;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {  clockBar.setText(String.format("%02d : %02d",s,ms));}
                });

                try {Thread.sleep(10);} catch (InterruptedException e) {}

                if(ms==0&&s==0){
                    isPlay=false;
                }

            }//while


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    startBtn.setVisibility(View.VISIBLE);
                    clockBar.setVisibility(View.GONE);

                }
            });



        }
    }

}
