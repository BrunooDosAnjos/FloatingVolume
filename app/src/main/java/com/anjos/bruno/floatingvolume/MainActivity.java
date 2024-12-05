package com.anjos.bruno.floatingvolume;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifica se a permissão de sobreposição está habilitada
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Solicita permissão ao usuário
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 100); // 100 é o código da requisição
        } else {
            // Inicia o serviço diretamente se a permissão estiver concedida
            startFloatingService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            // Verifica novamente se a permissão foi concedida
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                startFloatingService();
            } else {
                Toast.makeText(this, "Permissão necessária para exibir os botões flutuantes.", Toast.LENGTH_SHORT).show();
                finish(); // Finaliza a atividade se a permissão não for concedida
            }
        }
    }

    private void startFloatingService() {
        // Inicia o serviço para exibir os botões flutuantes
        Intent serviceIntent = new Intent(this, FloatingButtonService.class);
        startService(serviceIntent);
        finish(); // Finaliza a MainActivity para não ocupar a interface do usuário
    }
}
