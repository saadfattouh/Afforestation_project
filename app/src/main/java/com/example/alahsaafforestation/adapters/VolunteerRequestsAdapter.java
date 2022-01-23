package com.example.alahsaafforestation.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.example.alahsaafforestation.model.CustomerServices;
import com.example.alahsaafforestation.model.VoluntaryService;
import com.example.alahsaafforestation.utils.SharedPrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VolunteerRequestsAdapter extends RecyclerView.Adapter<VolunteerRequestsAdapter.ViewHolder> {

    Context context;
    ArrayList<CustomerServices> services;

    public VolunteerRequestsAdapter(Context context, ArrayList<CustomerServices> services) {
        this.context = context;
        this.services = services;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.voluntary_service_request_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CustomerServices service = services.get(position);
        VoluntaryService serviceInfo = service.getService();

        holder.customerName.setText(service.getCustomerName());

        holder.serviceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(context);
                final View infoDialogView = factory.inflate(R.layout.service_info_dialog, null);
                final AlertDialog infoDialog = new AlertDialog.Builder(context).create();
                infoDialog.setView(infoDialogView);

                //to look rounded
                infoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                TextView details = infoDialogView.findViewById(R.id.service_details);
                details.append(" : " + serviceInfo.getDescription());

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

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateService(serviceInfo.getId(), service.getCustomer_id(), Constants.SERVICE_STATUS_ACCEPTED);
                removeOrder(position);
            }
        });

        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateService(serviceInfo.getId(), service.getCustomer_id(), Constants.SERVICE_STATUS_REJECTED);
                removeOrder(position);
            }
        });
    }


    private void updateService(int id, int customerId, int status) {
        String url = "http://nawar.scit.co/trees-api/index.php";

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        AndroidNetworking.post(url)
                .addBodyParameter("type", "update_service_status")
                .addBodyParameter("service_id", String.valueOf(id))
                .addBodyParameter("customer_id", String.valueOf(customerId))
                .addBodyParameter("status", String.valueOf(status))
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


        public TextView customerName;
        public ImageView serviceInfo;
        public ImageView accept;
        public ImageView reject;

        public ViewHolder(View itemView) {
            super(itemView);
            this.customerName = itemView.findViewById(R.id.customer_name);
            this.serviceInfo = itemView.findViewById(R.id.service_info);
            this.accept = itemView.findViewById(R.id.accept_btn);
            this.reject =itemView.findViewById(R.id.reject_btn);
        }
    }

    private void removeOrder(int index) {
        services.remove(index);
        notifyItemRemoved(index);
    }

}
