package com.example.versuspc.snake;

/**
 * Created by VersusPc on 13/02/2018.
 */


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.Random;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


class SnakeEngine extends SurfaceView implements Runnable {
    private Thread thread = null;

    private Context context;

    // Pool de sonidos (NO SUENA , NOSE PORQUE)
    private SoundPool soundPool;
    private int sonidocomer = -1;
    private int sonidomuerte = -1;

    public enum Heading {
        UP, RIGHT, DOWN, LEFT
    }

    // Start by heading to the right
    private Heading heading = Heading.RIGHT;

    // Tamaño de la pantalla
    private int ejeX;
    private int ejeY;

    // Tamaño
    private int tamaño;

    private int serpEjeX;
    private int serpEjeY;

    private int tamBloque;

    // Zona de juego
    private final int BLOQUES = 40;
    private int numBlocksHigh;

    // Frames
    private long represco;
    // Tasa de refresco
    private final long FPS = 10;
    private final long MILLSEGUND = 1000;

    // Puntos
    private int puntos;

    private int[] ubicacionX;
    private int[] ubicacionY;

    //Si esta ejecutandose
    private volatile boolean enJuego;


    //Mirar que es un Canvas
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;

    private Paint dibujar;

    public SnakeEngine(Context context, Point size) {
        super(context);

        context = context;

        ejeX = size.x;
        ejeY = size.y;

        tamBloque = ejeX / BLOQUES;
        numBlocksHigh = ejeY / tamBloque;

        // Añadir sonido
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;
            descriptor = assetManager.openFd("eat_bob.ogg");
            sonidocomer = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("snake_crash.ogg");
            sonidomuerte = soundPool.load(descriptor, 0);

        } catch (IOException e) {
        }


        //Dibujar
        surfaceHolder = getHolder();
        dibujar = new Paint();

        ubicacionX = new int[200];
        ubicacionY = new int[200];

        // Iniciar juego
        nuevojuego();
    }

    @Override
    public void run() {

        while (enJuego) {

            // Refresacar 10 veces por segundo
            if (updateRequired()) {
                update();
                draw();
            }

        }
    }

    public void pause() {
        enJuego = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        enJuego = true;
        thread = new Thread(this);
        thread.start();
    }

    public void nuevojuego() {
        // Empieza con un cuadrado de la Serpiente
        tamaño = 1;
        ubicacionX[0] = BLOQUES / 2;
        ubicacionY[0] = numBlocksHigh / 2;

        //Saca a la Serpiente en pantalla de forma random
        dibujarserpiente();
        puntos = 0;
        represco = System.currentTimeMillis();
    }

    public void dibujarserpiente() {
        Random random = new Random();
        serpEjeX = random.nextInt(BLOQUES - 1) + 1;
        serpEjeY = random.nextInt(numBlocksHigh - 1) + 1;
    }

    private void comerSerpiente() {
        //Incrementar el tamaño
        tamaño++;
        dibujarserpiente();
        //Aumentar los puntos
        puntos = puntos + 1;
        soundPool.play(sonidocomer, 1, 1, 0, 0, 1);
    }

    private void moverSerpiente() {
        // Mover a la Serpiente
        for (int i = tamaño; i > 0; i--) {
            //Mover la serpiente desde atras hacia adelante
            ubicacionX[i] = ubicacionX[i - 1];
            ubicacionY[i] = ubicacionY[i - 1];
        }


        switch (heading) {
            case UP:
                ubicacionY[0]--;
                break;

            case RIGHT:
                ubicacionX[0]++;
                break;

            case DOWN:
                ubicacionY[0]++;
                break;

            case LEFT:
                ubicacionX[0]--;
                break;
        }
    }

    private boolean muerteserpiente() {
        // Muerte de la Serpiente
        boolean dead = false;

        // Si golpea las esquinas
        if (ubicacionX[0] == -1) dead = true;
        if (ubicacionX[0] >= BLOQUES) dead = true;
        if (ubicacionY[0] == -1) dead = true;
        if (ubicacionY[0] == numBlocksHigh) dead = true;

        // Comerse a si mismo
        for (int i = tamaño - 1; i > 0; i--) {
            if ((i > 4) && (ubicacionX[0] == ubicacionX[i]) && (ubicacionY[0] == ubicacionY[i])) {
                dead = true;
            }
        }

        return dead;
    }

    public void update() {
        if (ubicacionX[0] == serpEjeX && ubicacionY[0] == serpEjeY) {
            comerSerpiente();
        }

        moverSerpiente();

        if (muerteserpiente()) {
            //Si muere volver a empezar
            soundPool.play(sonidomuerte, 1, 1, 0, 0, 1);

            nuevojuego();
        }
    }

    public void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            // Fondo
            canvas.drawColor(Color.argb(255, 204, 153, 0));

            // Serpiente
            dibujar.setColor(Color.argb(255, 255, 255, 255));

            // Scale the HUD text
            dibujar.setTextSize(90);
            canvas.drawText("Puntos:" + puntos, 10, 70, dibujar);


            for (int i = 0; i < tamaño; i++) {
                canvas.drawRect(ubicacionX[i] * tamBloque,
                        (ubicacionY[i] * tamBloque),
                        (ubicacionX[i] * tamBloque) + tamBloque,
                        (ubicacionY[i] * tamBloque) + tamBloque,
                        dibujar);
            }

            // Color del objetivo
            dibujar.setColor(Color.argb(255, 255, 0, 0));

            canvas.drawRect(serpEjeX * tamBloque,
                    (serpEjeY * tamBloque),
                    (serpEjeX * tamBloque) + tamBloque,
                    (serpEjeY * tamBloque) + tamBloque,
                    dibujar);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired() {

        if (represco <= System.currentTimeMillis()) {
            represco = System.currentTimeMillis() + MILLSEGUND / FPS;
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= ejeX / 2) {
                    switch (heading) {
                        case UP:
                            heading = Heading.RIGHT;
                            break;
                        case RIGHT:
                            heading = Heading.DOWN;
                            break;
                        case DOWN:
                            heading = Heading.LEFT;
                            break;
                        case LEFT:
                            heading = Heading.UP;
                            break;
                    }
                } else {
                    switch (heading) {
                        case UP:
                            heading = Heading.LEFT;
                            break;
                        case LEFT:
                            heading = Heading.DOWN;
                            break;
                        case DOWN:
                            heading = Heading.RIGHT;
                            break;
                        case RIGHT:
                            heading = Heading.UP;
                            break;
                    }
                }
        }
        return true;
    }
}
