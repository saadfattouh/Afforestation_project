package com.example.alahsaafforestation.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.model.SellerOrder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SellerCustomerOrdersAdapter extends RecyclerView.Adapter<SellerCustomerOrdersAdapter.ViewHolder> {

    ArrayList<SellerOrder> orders;
    Context context;

    public SellerCustomerOrdersAdapter(Context context, ArrayList<SellerOrder> orders) {
        this.orders = orders;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.product_request_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        SellerOrder order = orders.get(position);

        holder.customerName.setText(String.valueOf(order.getCustomerName()));

        holder.price.setText(String.valueOf(order.getPrice()));

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrderStatus(order.getId(), Constants.ORDER_STATUS_ACCEPTED);
                removeOrder(position);
            }
        });

        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrderStatus(order.getId(), Constants.ORDER_STATUS_REJECTED);
                removeOrder(position);
            }
        });

    }

    private void updateOrderStatus(int orderId, int status) {
        String url = "http://nawar.scit.co/trees-api/index.php";

        final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        AndroidNetworking.post(url)
                .addBodyParameter("type", "update_order_status")
                .addBodyParameter("cart_id", String.valueOf(orderId))
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
                            Toast.makeText(context, obj.getString("msg"), Toast.LENGTH_SHORT).show();

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
        return orders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public TextView customerName;
        public TextView price;
        public ImageView accept;
        public ImageView reject;

        public ViewHolder(View itemView) {
            super(itemView);
            this.customerName = itemView.findViewById(R.id.customer_name);
            this.price = itemView.findViewById(R.id.price);
            this.accept = itemView.findViewById(R.id.accept_btn);
            this.reject =itemView.findViewById(R.id.reject_btn);
        }
    }

    private void removeOrder(int index) {
        orders.remove(index);
        notifyItemRemoved(index);
    }


}
