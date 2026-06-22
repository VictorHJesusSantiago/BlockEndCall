package com.blockendcall.android.ui;

import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blockendcall.android.databinding.ActivityMainBinding;
import com.blockendcall.android.util.SessionManager;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ROLE = 1001;

    private ActivityMainBinding binding;
    private SessionManager session;

    private final ActivityResultLauncher<Intent> roleRequestLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, "Call screening active!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Screening permission denied — calls won't be blocked automatically", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        binding.tvWelcome.setText("Hello, " + session.getUserName() + "!");

        binding.btnViewBlocked.setOnClickListener(v ->
                startActivity(new Intent(this, BlockedListActivity.class)));

        binding.btnReportNumber.setOnClickListener(v ->
                startActivity(new Intent(this, ReportNumberActivity.class)));

        binding.btnEnableScreening.setOnClickListener(v -> requestCallScreeningRole());

        binding.btnLogout.setOnClickListener(v -> logout());

        requestCallScreeningRole();
    }

    private void requestCallScreeningRole() {
        RoleManager roleManager = getSystemService(RoleManager.class);

        if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
            new AlertDialog.Builder(this)
                    .setTitle("Enable Call Blocking")
                    .setMessage("Set BlockEndCall as the call screening app to automatically block spam calls.")
                    .setPositiveButton("Enable", (d, w) -> {
                        Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
                        roleRequestLauncher.launch(intent);
                    })
                    .setNegativeButton("Later", null)
                    .show();
        } else {
            binding.tvScreeningStatus.setText("Call screening: ACTIVE");
        }
    }

    private void logout() {
        session.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
