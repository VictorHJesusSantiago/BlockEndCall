package com.blockendcall.android.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.blockendcall.android.databinding.ActivityPrivacyPolicyBinding;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ActivityPrivacyPolicyBinding binding;

    private static final String POLICY_TEXT =
            "POLÍTICA DE PRIVACIDADE - BlockEndCall\n\n" +
            "Última atualização: 2025\n\n" +
            "1. DADOS QUE COLETAMOS\n" +
            "Coletamos: nome, e-mail, número de telefone (opcional), reportes de spam enviados e log de chamadas bloqueadas no dispositivo.\n\n" +
            "2. COMO USAMOS SEUS DADOS\n" +
            "Seus dados são usados para:\n" +
            "- Identificar e bloquear números de spam para você e a comunidade\n" +
            "- Atribuir pontos de reputação e conquistas\n" +
            "- Melhorar a precisão da nossa base de dados comunitária\n\n" +
            "3. COMPARTILHAMENTO DE DADOS\n" +
            "Os reportes que você faz são compartilhados de forma anonimizada com a comunidade. Nunca vendemos seus dados pessoais a terceiros.\n\n" +
            "4. SEUS DIREITOS (LGPD)\n" +
            "Conforme a Lei Geral de Proteção de Dados (Lei nº 13.709/2018), você tem direito a:\n" +
            "- Acesso aos seus dados pessoais\n" +
            "- Correção de dados incompletos ou incorretos\n" +
            "- Exclusão dos seus dados (opção disponível no app)\n" +
            "- Portabilidade dos dados (exportação disponível no app)\n" +
            "- Revogação do consentimento a qualquer momento\n\n" +
            "5. RETENÇÃO DE DADOS\n" +
            "Mantemos seus dados enquanto sua conta estiver ativa. Após exclusão da conta, dados pessoais são anonimizados em até 30 dias.\n\n" +
            "6. SEGURANÇA\n" +
            "Utilizamos criptografia TLS para todas as comunicações. Senhas são armazenadas com hash bcrypt.\n\n" +
            "7. CONTATO\n" +
            "Para exercer seus direitos ou esclarecer dúvidas, entre em contato:\n" +
            "E-mail: privacidade@blockendcall.com\n\n" +
            "8. ALTERAÇÕES\n" +
            "Podemos atualizar esta política. Você será notificado por e-mail sobre mudanças significativas.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.tvContent.setText(POLICY_TEXT);
    }

    @Override public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
