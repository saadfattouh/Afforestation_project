package com.example.alahsaafforestation.sellerfragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.adapters.SellerCustomerOrdersAdapter;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.model.SellerOrder;
import com.example.alahsaafforestation.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SellerMainFragment extends Fragment {

    public static final String TAG = "sellerMain";

    RecyclerView mCustomersOrdersList;
    ArrayList<SellerOrder> currentOrders;
    SellerCustomerOrdersAdapter mCurrentOrdersAdapter;

    Button mAcceptAllOrders;
    Button mRejectAllOrders;

    int sellerId;


    public SellerMainFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sellerId = SharedPrefManager.getInstance(requireContext()).getUserId();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seller_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCustomersOrdersList = view.findViewById(R.id.customers_orders_list);
        mAcceptAllOrders = view.findViewById(R.id.accept_all_orders_btn);
        mRejectAllOrders = view.findViewById(R.id.reject_all_orders_btn);


        getOrders();

        mAcceptAllOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAllOrders(Constants.ORDER_STATUS_ACCEPTED);
            }
        });

        mRejectAllOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAllOrders(Constants.ORDER_STATUS_REJECTED);
            }
        });

    }


    private void updateAllOrders(int status) {
        String url = "http://nawar.scit.co/trees-api/index.php";

        final ProgressDialog pDialog = new ProgressDialog(requireContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        AndroidNetworking.post(url)
                .addBodyParameter("type", "update_all_orders_status")
                .addBodyParameter("seller_id", String.valueOf(sellerId))
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

                                Toast.makeText(requireContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(requireContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        pDialog.dismiss();
                        Toast.makeText(requireContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    void getOrders(){
        int id = sellerId;

        String url = "http://nawar.scit.co/trees-api/index.php?type=get_orders_by_seller_id&seller_id="+id;

        //if everything is fine
        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        currentOrders = new ArrayList<>();

        AndroidNetworking.get(url)
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

                                //getting the user from the response
                                JSONArray ordersArray = obj.getJSONArray("data");
                                SellerOrder order;
                                for (int i = 0; i < ordersArray.length(); i++){
                                    JSONObject serviceJson = ordersArray.getJSONObject(i);
                                    if(Integer.parseInt(serviceJson.getString("status")) != Constants.ORDER_STATUS_NEW)
                                        continue;
                                    order = new SellerOrder(
                                            Integer.parseInt(serviceJson.getString("id")),
                                            serviceJson.getString("customer_name"),
                                            serviceJson.getInt("total_price"),
                                            serviceJson.getInt("quantity")
                                    );
                                    currentOrders.add(order);
                                }
                                mCurrentOrdersAdapter = new SellerCustomerOrdersAdapter(requireContext(), currentOrders);
                                mCustomersOrdersList.setAdapter(mCurrentOrdersAdapter);

                            } else {
                                Toast.makeText(getContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        pDialog.dismiss();
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}