package com.anjos.bruno.floatingvolume;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class FloatingButtonService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private Handler volumeHandler = new Handler();
    private Runnable volumeRunnable;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inflate o layout dos botões flutuantes
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_buttons, null);

        // Parâmetros do layout para exibição flutuante
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT
        );

        // Posicionar os botões à direita, centralizados verticalmente
        params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        params.x = 50; // Distância da borda direita
        params.y = 0;  // Centralizado verticalmente

        // Gerenciador de janelas
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingView, params);

        // Configuração dos botões
        ImageView btnIncrease = floatingView.findViewById(R.id.btn_increase);
        ImageView btnDecrease = floatingView.findViewById(R.id.btn_decrease);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Configurar eventos de pressionar e segurar nos botões
        btnIncrease.setOnTouchListener(new VolumeTouchListener(audioManager, AudioManager.ADJUST_RAISE));
        btnDecrease.setOnTouchListener(new VolumeTouchListener(audioManager, AudioManager.ADJUST_LOWER));

        // Permitir arrastar os botões
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Registrar a posição inicial
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Atualizar apenas o eixo Y para limitar o movimento vertical
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }


        });
    }

    /**
     * Listener personalizado para lidar com eventos de toque.
     */
    private class VolumeTouchListener implements View.OnTouchListener {
        private final AudioManager audioManager;
        private final int direction;

        public VolumeTouchListener(AudioManager audioManager, int direction) {
            this.audioManager = audioManager;
            this.direction = direction;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Iniciar o aumento ou diminuição contínua do volume
                    startVolumeAdjustment();
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Parar o ajuste de volume
                    stopVolumeAdjustment();
                    return true;
            }
            return false;
        }

        private void startVolumeAdjustment() {
            volumeRunnable = new Runnable() {
                @Override
                public void run() {
                    audioManager.adjustVolume(direction, AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_PLAY_SOUND);
                    volumeHandler.postDelayed(this, 200); // Ajusta o volume a cada 200ms
                }
            };
            volumeHandler.post(volumeRunnable);
        }

        private void stopVolumeAdjustment() {
            if (volumeRunnable != null) {
                volumeHandler.removeCallbacks(volumeRunnable);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
        if (volumeHandler != null) volumeHandler.removeCallbacksAndMessages(null);
    }
}
