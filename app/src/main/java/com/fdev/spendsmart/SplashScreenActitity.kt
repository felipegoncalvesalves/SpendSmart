package com.fdev.spendsmart

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.splash_screen)

        // Define um temporizador para iniciar a próxima atividade após 2 segundos
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Encerra esta atividade para que o usuário não possa voltar para a tela de inicialização
        }, 2000) // Tempo em milissegundos (2 segundos)
    }
}
