package com.example.alahsaafforestation.fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.adapters.ProductsAdapter;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.listeners.OnSearchActivatedListener;
import com.example.alahsaafforestation.model.Product;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class ProductsFragment extends Fragment {

    public static final String TAG = "customerProducts";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;

    private static final int PICK_IMAGE_REQUEST = 1;
    Uri imageUri;
    String filePath;
    Bitmap bitmap;


    TextInputEditText mSearchET;
    String query;
    ImageView mUploadBtn;

    RecyclerView mProductsListView;
    List<Product> products;
    ProductsAdapter productsAdapter;

    OnSearchActivatedListener onSearchListener;

    RequestQueue queue;


    public ProductsFragment() {
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
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSearchET = view.findViewById(R.id.search_edit_text);
        mUploadBtn = view.findViewById(R.id.upload_btn);
        mProductsListView = view.findViewById(R.id.products_list);

        //working with search edit text.
        //changing label of "enter" button into search instead
        mSearchET.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);

        mSearchET.setOnEditorActionListener((v, actionId, event) -> {
            if (event == null) {
                query = mSearchET.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    // Capture soft enters in a singleLine EditText that is the last EditText.
                    if(!query.isEmpty()){
                        getAllProducts(query);
                        onSearchListener.onSearch();
                    }
                }
                else if (actionId==EditorInfo.IME_ACTION_NEXT)
                {
                    // Capture soft enters in other singleLine EditTexts
                    if(!query.isEmpty()){
                        getAllProducts(query);
                        onSearchListener.onSearch();
                    }
                }
                else return false;  // Let system handle all other null KeyEvents
            } else
                return false;

            return false;
        });

        mUploadBtn.setOnClickListener(v -> {
            performImageSearch();
            onSearchListener.onSearch();
        });


        getAllProducts("");

    }

    private void performImageSearch() {
        requestRead();
    }

    //..................Methods for File Chooser.................
    public void requestRead() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            openFileChooser();
        }
    }

    public void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            Uri picUri = imageUri;
            filePath = getPath(picUri);
            if (filePath != null) {
                bitmap = BitmapFactory.decodeFile(filePath);
                Log.d("filePath", String.valueOf(filePath));
                Toast.makeText(getContext(), "started searching", Toast.LENGTH_SHORT).show();
                searchByImage();
            }
            else
            {
                Toast.makeText(getContext(),"no image selected", Toast.LENGTH_LONG).show();
            }



        }
    }

    private void searchByImage() {
        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        File file = new File(filePath);


        AndroidNetworking.upload(Constants.BASE_URL)
                .addMultipartParameter("type", "search_by_image")
                .addMultipartFile("image", file)
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                    }
                })
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
                                //getting the user from the response
                                JSONArray productsArray = obj.getJSONArray("data");
                                products = new ArrayList<>();
                                Product product;
                                for (int i = 0; i < productsArray.length(); i++){
                                    JSONObject productJson = productsArray.getJSONObject(i);
                                    product = new Product(
                                            productJson.getInt("id"),
                                            productJson.getInt("seller_id"),
                                            productJson.getString("seller_name"),
                                            productJson.getString("image"),
                                            productJson.getString("name"),
                                            productJson.getString("description"),
                                            productJson.getString("planting_date"),
                                            productJson.getString("planting_address"),
                                            Double.parseDouble(productJson.getString("price").trim()),
                                            productJson.getInt("quantity"),
                                            productJson.getString("category")
                                    );
                                    products.add(product);
                                }

                                productsAdapter = new ProductsAdapter(getContext(), products);
                                mProductsListView.setAdapter(productsAdapter);

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
                        Log.e("Error", error.getMessage());
                        pDialog.hide();
                    }
                });
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContext().getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
    //..............................................................................



    public List<Product> getProductsByName(String name) {
        return products;
    }


    public void getAllProducts(String query){

        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        String url = Constants.PRODUCTS_ALL_URL + "&name="+query;

        products = new ArrayList<>();

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
                                            productJson.getInt("seller_id"),
                                            productJson.getString("seller_name"),
                                            productJson.getString("image"),
                                            productJson.getString("name"),
                                            productJson.getString("description"),
                                            productJson.getString("planting_date"),
                                            productJson.getString("planting_address"),
                                            Double.parseDouble(productJson.getString("price").trim()),
                                            productJson.getInt("quantity"),
                                            productJson.getString("category")
                                    );
                                    products.add(product);
                                }

                                productsAdapter = new ProductsAdapter(getContext(), products);
                                mProductsListView.setAdapter(productsAdapter);

                            } else {
                                Toast.makeText(getContext(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                                productsAdapter = new ProductsAdapter(getContext(), products);
                                mProductsListView.setAdapter(productsAdapter);
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSearchActivatedListener) {
            onSearchListener = (OnSearchActivatedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onSearchListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSearchListener = null;
    }

}