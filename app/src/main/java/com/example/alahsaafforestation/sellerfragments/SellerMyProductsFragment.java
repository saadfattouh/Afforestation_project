package com.example.alahsaafforestation.sellerfragments;

import android.app.ProgressDialog;
import android.content.Context;
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
import com.example.alahsaafforestation.adapters.SellerMyProductsAdapter;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.model.Product;
import com.example.alahsaafforestation.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SellerMyProductsFragment extends Fragment {

    public static final String TAG = "sellerMyProducts";


    RecyclerView mMyProductsList;
    ArrayList<Product> myProducts;
    SellerMyProductsAdapter mMyProductsAdapter;

    OnAddProductClicked addProductListener;

    Button mAddProductBtn;

    public SellerMyProductsFragment() {
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
        return inflater.inflate(R.layout.fragment_seller_my_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAddProductBtn = view.findViewById(R.id.add_product_btn);
        mMyProductsList = view.findViewById(R.id.seller_my_products_list);

        mAddProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addProductListener.onAddProductClicked();
            }
        });

        getMyProducts();

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SellerMyProductsFragment.OnAddProductClicked) {
            addProductListener = (SellerMyProductsFragment.OnAddProductClicked) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        addProductListener = null;
    }

    public interface OnAddProductClicked{
        void onAddProductClicked();
    }


    private void getMyProducts(){

        String url = Constants.PRODUCTS_BY_SELLER_ID;
        url += "&id="+ SharedPrefManager.getInstance(getContext()).getUserId();
        //if everything is fine
        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        myProducts = new ArrayList<>();

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
                                JSONArray productsArray = obj.getJSONArray("data");
                                Product product;
                                for (int i = 0; i < productsArray.length(); i++){
                                    JSONObject productJson = productsArray.getJSONObject(i);
                                    product = new Product(
                                            productJson.getInt("id"),
                                            productJson.getString("name"),
                                            Double.parseDouble(productJson.getString("price").trim()),
                                            productJson.getInt("quantity")
                                    );
                                    myProducts.add(product);
                                }

                                mMyProductsAdapter = new SellerMyProductsAdapter(myProducts, getContext());
                                mMyProductsList.setAdapter(mMyProductsAdapter);

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