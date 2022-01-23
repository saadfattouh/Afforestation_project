package com.example.alahsaafforestation.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.model.User;
import com.example.alahsaafforestation.model.VoluntaryService;
import com.example.alahsaafforestation.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AllServicesAdapter extends RecyclerView.Adapter<AllServicesAdapter.ViewHolder> {


    ArrayList<VoluntaryService> services;
    Context context;

    public AllServicesAdapter(Context context, ArrayList<VoluntaryService> services) {
        this.services = services;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.all_services_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VoluntaryService service = services.get(position);

        holder.vName.setText(service.getDescription());

        holder.info.setText(service.getDescription());

        holder.itemView.setOnClickListener(v -> {
            LayoutInflater factory = LayoutInflater.from(context);
            final View view = factory.inflate(R.layout.join_service_confirmation, null);
            final AlertDialog confirmationDialog = new AlertDialog.Builder(context).create();
            confirmationDialog.setView(view);

            TextView yes = view.findViewById(R.id.yes_btn);
            TextView no = view.findViewById(R.id.no_btn);


            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    join_service(service.getId(), SharedPrefManager.getInstance(context).getUserId());
                    //when done dismiss;
                    confirmationDialog.dismiss();

                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmationDialog.dismiss();
                }
            });
            confirmationDialog.show();
        });


    }

    private void join_service(int id, int userId) {

        String url = "http://nawar.scit.co/trees-api/index.php";

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        AndroidNetworking.post(url)
                .addBodyParameter("type", "join_service")
                .addBodyParameter("customer_id", String.valueOf(userId))
                .addBodyParameter("service_id", String.valueOf(id))
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.dismiss();

                        try {
                            //converting response to json object
                            JSONObject obj = response;

                            //if no error in response
                            if (!obj.getBoolean("error")) {

                                Toast.makeText(context, obj.getString("msg"), Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(context, obj.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        pDialog.dismiss();
                        Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public int getItemCount() {
        return services.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public TextView vName;
        public TextView info;

        public ViewHolder(View itemView) {
            super(itemView);
            this.vName = itemView.findViewById(R.id.volunteer_name);
            this.info = itemView.findViewById(R.id.service_info);
        }
    }



}
