package com.example.alahsaafforestation.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.model.Pharmacy;


import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class PharmaciesAdapter extends RecyclerView.Adapter<PharmaciesAdapter.ViewHolder>{


    Context context;
    private List<Pharmacy> pharmacies;

    // RecyclerView recyclerView;
    public PharmaciesAdapter(Context context, List<Pharmacy> pharmacies) {
            this.context = context;
            this.pharmacies = pharmacies;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //the usual shit
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View listItem= layoutInflater.inflate(R.layout.pharmacy_list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(listItem);

            return viewHolder;
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            Pharmacy pharmacy = pharmacies.get(position);

            if(pharmacy.getImageUrl() == null){
            Glide.with(context)
                .load(context.getResources().getDrawable(R.drawable.ic_pharmacy))
                .into(holder.image);
            }else{
            Glide.with(context)
                .load(pharmacy.getImageUrl())
                .into(holder.image);
            }

            holder.name.setText(pharmacy.getName());

            holder.pharmacistName.setText(pharmacy.getPharmacistName());

            holder.infoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater factory = LayoutInflater.from(context);
                    final View infoDialogView = factory.inflate(R.layout.pharmacy_info_dialog, null);
                    final AlertDialog infoDialog = new AlertDialog.Builder(context).create();
                    infoDialog.setView(infoDialogView);

                    //to look rounded
                    infoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    TextView name = infoDialogView.findViewById(R.id.pharmacy_name);
                    name.setText(pharmacy.getName());
                    TextView pharmacistName = infoDialogView.findViewById(R.id.pharmacist_name);
                    pharmacistName.append(pharmacy.getPharmacistName());
                    TextView address = infoDialogView.findViewById(R.id.address);
                    address.append(pharmacy.getAddress());
                    TextView phone = infoDialogView.findViewById(R.id.phone);
                    phone.append(" : "+pharmacy.getPhone());
                    TextView opensAt = infoDialogView.findViewById(R.id.opens_at);
                    opensAt.append(pharmacy.getOpensAt());
                    TextView closesAt = infoDialogView.findViewById(R.id.closes_at);
                    closesAt.append(pharmacy.getCloseAt());

                    infoDialogView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //your business logic
                            infoDialog.dismiss();
                        }
                    });
                    infoDialog.show();
                }
            });

    }

    @Override
    public int getItemCount() {
            return pharmacies.size();
            }

    public void filterList(String query) {
        // clear old list
        if(pharmacies.isEmpty())
            return;
        ArrayList<Pharmacy> pharmaciesCopy = new ArrayList<>(pharmacies);
        pharmacies.clear();

        if(query.isEmpty()){
            pharmacies.addAll(pharmaciesCopy);
        } else{
            query = query.toLowerCase();
            for(Pharmacy item: pharmaciesCopy){
                if(item.getName().toLowerCase().contains(query)
                        || item.getPhone().toLowerCase().contains(query)
                        || item.getAddress().toLowerCase().contains(query)
                        || item.getPharmacistName().toLowerCase().contains(query)){

                    pharmacies.add(item);
                }
            }
        }

        // notify adapter
        notifyDataSetChanged();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name;
        public TextView pharmacistName;
        public ImageView infoBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.image);
            this.name = itemView.findViewById(R.id.pharmacy_name_text_view);
            this.pharmacistName = itemView.findViewById(R.id.pharmacist_name_text_view);
            this.infoBtn = itemView.findViewById(R.id.info_btn);
        }
    }
}
