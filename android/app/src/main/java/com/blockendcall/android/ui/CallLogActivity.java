package com.blockendcall.android.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blockendcall.android.R;
import com.blockendcall.android.api.ApiClient;
import com.blockendcall.android.api.BlockedNumberApi;
import com.blockendcall.android.databinding.ActivityCallLogServerBinding;
import com.blockendcall.android.model.ApiResponse;
import com.blockendcall.android.model.NumberCheckResult;
import com.blockendcall.android.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallLogActivity extends AppCompatActivity {

    private static final int PERM_READ_CALL_LOG = 101;

    private ActivityCallLogServerBinding binding;
    private CallLogAdapter adapter;
    private BlockedNumberApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallLogServerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registro de Chamadas");
        }

        SessionManager session = new SessionManager(this);
        api = ApiClient.getApi(session);

        adapter = new CallLogAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        requestCallLogPermission();
    }

    private void requestCallLogPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
            loadCallLog();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALL_LOG}, PERM_READ_CALL_LOG);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == PERM_READ_CALL_LOG && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            loadCallLog();
        } else {
            Toast.makeText(this, "Permissão necessária para ver o registro de chamadas",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void loadCallLog() {
        List<CallLogEntry> entries = new ArrayList<>();
        String[] projection = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_NAME
        };
        Cursor cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI, projection, null, null, CallLog.Calls.DATE + " DESC");
        if (cursor != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            int limit = 0;
            while (cursor.moveToNext() && limit < 50) {
                String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                String cachedName = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));
                entries.add(new CallLogEntry(number, sdf.format(new Date(date)), type, cachedName));
                limit++;
            }
            cursor.close();
        }
        adapter.setData(entries);
        binding.tvEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void verifyNumber(String phone) {
        api.checkNumber(phone).enqueue(new Callback<ApiResponse<NumberCheckResult>>() {
            @Override
            public void onResponse(Call<ApiResponse<NumberCheckResult>> call,
                                   Response<ApiResponse<NumberCheckResult>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    NumberCheckResult r = response.body().getData();
                    String msg = r.isBlocked()
                            ? "SPAM: " + r.getCategory() + " (" + r.getReportCount() + " reportes)"
                            : "Numero limpo";
                    Toast.makeText(CallLogActivity.this, phone + ": " + msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<NumberCheckResult>> call, Throwable t) {
                Toast.makeText(CallLogActivity.this, "Erro ao verificar número", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    static class CallLogEntry {
        final String number, date, name;
        final int type;

        CallLogEntry(String n, String d, int t, String name) {
            number = n;
            date = d;
            type = t;
            this.name = name != null ? name : "";
        }
    }

    class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.VH> {
        private List<CallLogEntry> data = new ArrayList<>();

        void setData(List<CallLogEntry> d) {
            data = d;
            notifyDataSetChanged();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_call_log_entry, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            CallLogEntry e = data.get(pos);
            String typeLabel = e.type == CallLog.Calls.INCOMING_TYPE ? "Recebida" :
                               e.type == CallLog.Calls.OUTGOING_TYPE ? "Realizada" : "Perdida";
            h.tvPhone.setText(e.name.isEmpty() ? e.number : e.name + " (" + e.number + ")");
            h.tvInfo.setText(typeLabel + " - " + e.date);
            h.btnVerify.setOnClickListener(v -> verifyNumber(e.number));
        }

        @Override
        public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvPhone, tvInfo;
            Button btnVerify;

            VH(View v) {
                super(v);
                tvPhone = v.findViewById(R.id.tv_call_phone);
                tvInfo = v.findViewById(R.id.tv_call_info);
                btnVerify = v.findViewById(R.id.btn_verify);
            }
        }
    }
}
