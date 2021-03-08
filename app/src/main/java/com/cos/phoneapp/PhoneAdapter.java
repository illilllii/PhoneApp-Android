package com.cos.phoneapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 어댑터와 RecyclerView와 연결 (Databinding 사용금지) (MVVM 사용금지)
public class PhoneAdapter extends RecyclerView.Adapter<PhoneAdapter.MyViewHolder> {

    private static final String TAG = "PhoneAdapter";

    private static List<Phone> phones = new ArrayList<>();
    private PhoneService phoneService;

    public PhoneAdapter() {
        phoneService = PhoneService.retrofit.create(PhoneService.class);
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.phone_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setItem(phones.get(position));
    }

    @Override
    public int getItemCount() {
        return phones.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvTel;
        private TextInputEditText etName, etTel;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvTel = itemView.findViewById(R.id.tv_tel);

            itemView.setOnClickListener(view -> {
                View dialog = View.inflate(view.getContext(), R.layout.update_dialog_item, null);

                etName = dialog.findViewById(R.id.et_name);
                etTel = dialog.findViewById(R.id.et_tel);

                Call<CMRespDto<Phone>> call = phoneService.findById(phones.get(getAdapterPosition()).getId());
                call.enqueue(new Callback<CMRespDto<Phone>>() {
                    @Override
                    public void onResponse(Call<CMRespDto<Phone>> call, Response<CMRespDto<Phone>> response) {
                        CMRespDto<Phone> cmRespDto = response.body();
                        etName.setText(cmRespDto.getData().getName());
                        etTel.setText(cmRespDto.getData().getTel());
                    }

                    @Override
                    public void onFailure(Call<CMRespDto<Phone>> call, Throwable t) {
                        Log.d(TAG, "onFailure: findById() 실패");
                    }
                });

                AlertDialog.Builder dlg = new AlertDialog.Builder(view.getContext());
                dlg.setTitle("연락처 수정");
                dlg.setView(dialog);
                dlg.setNegativeButton("수정", (dialogInterface, i) -> {
                    phones.get(getAdapterPosition()).setName(etName.getText().toString());
                    phones.get(getAdapterPosition()).setTel(etTel.getText().toString());
                    update(getAdapterPosition());
                });
                dlg.setPositiveButton("삭제", (dialogInterface, i) -> {
                   delete(getAdapterPosition());
                });
                dlg.show();
            });
        }

        public void setItem(Phone phone) {
            tvName.setText(phone.getName());
            tvTel.setText(phone.getTel());
        }

    }

    private void update(int i) {
        Call<CMRespDto<Phone>> call = phoneService.update(phones.get(i).getId(), phones.get(i));
        call.enqueue(new Callback<CMRespDto<Phone>>() {
            @Override
            public void onResponse(Call<CMRespDto<Phone>> call, Response<CMRespDto<Phone>> response) {
                CMRespDto<Phone> cmRespDto = response.body();
                phones.set(i, cmRespDto.getData());
                notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<CMRespDto<Phone>> call, Throwable t) {
                Log.d(TAG, "onFailure: update() 실패");
            }
        });
    }

    private void delete(int i) {
        Call<CMRespDto> call = phoneService.delete(phones.get(i).getId());
        call.enqueue(new Callback<CMRespDto>() {
            @Override
            public void onResponse(Call<CMRespDto> call, Response<CMRespDto> response) {
                CMRespDto cmRespDto = response.body();
                if(cmRespDto.getCode() == 1) {
                    phones.remove(i);
                    notifyDataSetChanged();
                    Log.d(TAG, "onResponse: 삭제성공");

                }
            }

            @Override
            public void onFailure(Call<CMRespDto> call, Throwable t) {
                Log.d(TAG, "onFailure: delete() 실패");
            }
        });
    }
}
