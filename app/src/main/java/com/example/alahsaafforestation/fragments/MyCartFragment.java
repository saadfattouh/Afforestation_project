package com.example.alahsaafforestation.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.adapters.ProductsInCartAdapter;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.offlinedata.CartItemsDB;
import com.example.alahsaafforestation.offlinedata.Myappdatabas;
import com.example.alahsaafforestation.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MyCartFragment extends Fragment implements ProductsInCartAdapter.myCartListener{

    public static final String TAG = "customerMyCart";


    RecyclerView mOrderItemsList;
    ArrayList<CartItemsDB> orders;
    ProductsInCartAdapter productsInCartAdapter;

    Myappdatabas myappdatabas;

    TextView mTotalPriceText;
    Button mConfirmOrderBtn;



    public MyCartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mOrderItemsList = view.findViewById(R.id.products_in_cart_list);
        mTotalPriceText = view.findViewById(R.id.total_price_text_view);
        mConfirmOrderBtn = view.findViewById(R.id.confirm_order_btn);

        myappdatabas = Myappdatabas.getDatabase(getContext());
        orders = new ArrayList<>(myappdatabas.myDao().getItems());

        productsInCartAdapter = new ProductsInCartAdapter(orders, getContext(), this);
        mOrderItemsList.setAdapter(productsInCartAdapter);

        //calculate and set total price
        updateTotalPrice();

        mConfirmOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(productsInCartAdapter.getOrders().isEmpty()){
                    Toast.makeText(getContext(), "no orders to confirm", Toast.LENGTH_SHORT).show();
                }else{
                    Context ctx = requireContext();
                    LayoutInflater factory = LayoutInflater.from(ctx);
                    final View view = factory.inflate(R.layout.credit_card_input_dialog, null);
                    final AlertDialog creditCardCheckerDialog = new AlertDialog.Builder(ctx).create();
                    creditCardCheckerDialog.setView(view);

                    Button save = view.findViewById(R.id.save);

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        confirmOrder();
                        creditCardCheckerDialog.dismiss();
                        }
                    });
                    creditCardCheckerDialog.show();
                }

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //just making assume that dialogs can get fragments into onStop() state
        updateTotalPrice();
    }

    void updateTotalPrice(){
        mTotalPriceText.setText(getContext().getResources().getString(R.string.total) + productsInCartAdapter.getTotalPrice());
    }


    @Override
    public void onChange() {
        updateTotalPrice();
    }

    private JSONArray ordersToSend(){

        JSONArray jsonArray = new JSONArray();
        ArrayList<CartItemsDB> orders = productsInCartAdapter.getOrders();

        for (CartItemsDB item : orders){
            JSONObject order = new JSONObject();
            try {

                order.put("product_id", String.valueOf(item.getId()));
                order.put("quantity", String.valueOf(item.getQuantity()));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(order);
        }

        return jsonArray;
    }

    private void confirmOrder() {

        String URL = Constants.BASE_URL;

        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        AndroidNetworking.post(URL)
                .setPriority(Priority.MEDIUM)
                .addBodyParameter("type", "save_order")
                .addBodyParameter("expired_date", "2021-05-06")
                .addBodyParameter("balance", String.valueOf(productsInCartAdapter.getTotalPrice()))
                .addBodyParameter("customer_id", String.valueOf(SharedPrefManager.getInstance(getContext()).getUserId()))
                .addBodyParameter("status", String.valueOf(Constants.ORDER_STATUS_NEW))
                .addBodyParameter("cart_items", ordersToSend().toString())
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        pDialog.hide();
                        Log.e("Result", response.toString());
                        try {
                            Toast.makeText(getContext(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        myappdatabas.myDao().deleteAll(productsInCartAdapter.getOrders());
                        productsInCartAdapter = new ProductsInCartAdapter(new ArrayList<CartItemsDB>(), getContext(), MyCartFragment.this);
                        mOrderItemsList.setAdapter(productsInCartAdapter);
                        mTotalPriceText.setText("0.0 SAR");
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.e("Error", error.getMessage());
                        pDialog.hide();
                    }
                });

    }
}