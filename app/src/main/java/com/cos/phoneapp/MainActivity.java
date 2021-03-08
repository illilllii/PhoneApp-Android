package com.cos.phoneapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity2";

    private RecyclerView rvPhone;
    private PhoneAdapter phoneAdapter;
    private FloatingActionButton fabSave;
    private TextInputEditText etName, etTel;
    private PhoneService phoneService;
    private List<Phone> phones;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       init();
       download();
       listener();


    }
    private void init() {
        rvPhone = findViewById(R.id.rv_phone);
        phoneAdapter = new PhoneAdapter();
        rvPhone.setAdapter(phoneAdapter);
        fabSave = findViewById(R.id.fab_save);
        phoneService = PhoneService.retrofit.create(PhoneService.class);
        phones = new ArrayList<>();
    }

    private void listener() {
        fabSave.setOnClickListener(view -> {
            View dialog = View.inflate(view.getContext(), R.layout.save_dialog_item, null);
            etName = dialog.findViewById(R.id.et_name);
            etTel = dialog.findViewById(R.id.et_tel);

            AlertDialog.Builder dlg = new AlertDialog.Builder(view.getContext());
            dlg.setTitle("연락처 등록");
            dlg.setView(dialog);
            dlg.setNegativeButton("닫기", null);
            dlg.setPositiveButton("확인", (dialogInterface, i) -> {
                Phone phone = new Phone();
                phone.setName(etName.getText().toString());
                phone.setTel(etTel.getText().toString());

                save(phone);
            });
            dlg.show();
        });
    }
    private void download() {
        Call<CMRespDto<List<Phone>>> call = phoneService.findAll();

        call.enqueue(new Callback<CMRespDto<List<Phone>>>() {
            @Override
            public void onResponse(Call<CMRespDto<List<Phone>>> call, Response<CMRespDto<List<Phone>>> response) {
                CMRespDto<List<Phone>> cmRespDto = response.body();
                phones = cmRespDto.getData();
                // 어댑터에게 넘기기
                phoneAdapter.setPhones(phones);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);
                rvPhone.setLayoutManager(linearLayoutManager);

                rvPhone.setAdapter(phoneAdapter);
                Log.d(TAG, "onResponse: 응답 받은 데이터:"+phones);
            }

            @Override
            public void onFailure(Call<CMRespDto<List<Phone>>> call, Throwable t) {
                Log.d(TAG, "onFailure: findAll() 실패");
            }
        });
    }

    private void save(Phone phone) {
        Call<CMRespDto<Phone>> call = phoneService.save(phone);

        call.enqueue(new Callback<CMRespDto<Phone>>() {
            @Override
            public void onResponse(Call<CMRespDto<Phone>> call, Response<CMRespDto<Phone>> response) {
                CMRespDto<Phone> cmRespDto = response.body();
                Phone phone = cmRespDto.getData();
                phones.add(phone);
                phoneAdapter.setPhones(phones);
            }

            @Override
            public void onFailure(Call<CMRespDto<Phone>> call, Throwable t) {
                Log.d(TAG, "onFailure: save() 실패");
            }
        });
    }
}