package com.example.alahsaafforestation.sellerfragments;

import android.Manifest;
import android.app.ProgressDialog;
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

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.bumptech.glide.Glide;
import com.example.alahsaafforestation.R;
import com.example.alahsaafforestation.api.Constants;
import com.example.alahsaafforestation.utils.DateUtils;
import com.example.alahsaafforestation.utils.SharedPrefManager;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;


public class SellerAddNewProduct extends Fragment {

    public static final String TAG = "sellerAddProduct";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;

    EditText mNameET;
    EditText mDescriptionET;
    Spinner mDayChooser;
    Spinner mMonthChooser;
    Spinner mYearChooser;

    Spinner mQuantityChooser;
    Spinner mPriceChooser;
    Spinner mCategoryChooser;

    ImageView mUploadImageBtn;

    Button mSaveBtn;
    Button mCancel;

    //related to results
    int monthDays = 0;
    boolean isLeapYear = false;
    int selectedYear =-1;
    int selectedMonth = -1;
    int selectedDay = -1;

    int selectedPrice = -1;
    int selectedQuantity = -1;
    String selectedCategory = null;

    String productDescription = null;

    private static final int PICK_IMAGE_REQUEST = 1;
    Uri imageUri;
    String filePath;
    Bitmap bitmap;




    public SellerAddNewProduct() {
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
        return inflater.inflate(R.layout.fragment_seller_add_new_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);


        AndroidNetworking.initialize(requireContext());


        setUpDateChoosers();
        setUpPriceQualityChoosers();
        setUpCategoryChooser();

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validInput()){
                    saveProduct();
                }
            }
        });

        mCancel.setOnClickListener(v -> getActivity().onBackPressed());

        mUploadImageBtn.setOnClickListener(v -> requestRead());


    }



    private boolean validInput() {

        return true;
    }


    private void bindViews(View v) {

        mNameET = v.findViewById(R.id.name_edit_text);
        mDescriptionET = v.findViewById(R.id.description_edit_text);
        mDayChooser = v.findViewById(R.id.day_chooser);
        mDayChooser.setClickable(false);
        mMonthChooser = v.findViewById(R.id.month_chooser);
        mMonthChooser.setClickable(false);
        mYearChooser = v.findViewById(R.id.year_chooser);
        mQuantityChooser = v.findViewById(R.id.quantity_chooser);
        mPriceChooser = v.findViewById(R.id.price_chooser);
        mCategoryChooser = v.findViewById(R.id.category_chooser);
        mUploadImageBtn = v.findViewById(R.id.image_upload_btn);
        mSaveBtn = v.findViewById(R.id.save_new_product_btn);
        mCancel = v.findViewById(R.id.cancel_adding_product_btn);
    }

    private void setUpDateChoosers() {

        //setting up the years spinner with some values
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        ArrayList<Integer> yearEntries = new ArrayList<>();
        for(int i = 1980; i <= thisYear; i++){
            yearEntries.add(i);
        }

        // Creating adapter for spinner
        ArrayAdapter<Integer> yearsAdapter = new ArrayAdapter<Integer>(getContext(), R.layout.support_simple_spinner_dropdown_item, yearEntries);
        // Drop down layout style - list view with radio button
        yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        mYearChooser.setAdapter(yearsAdapter);

        mYearChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = yearEntries.get(position);
                if(DateUtils.isLeapYear(selectedYear)){
                    isLeapYear = true;
                }else
                    isLeapYear = false;

                mMonthChooser.setClickable(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "you need to choose the year first !", Toast.LENGTH_SHORT).show();
            }
        });

        mMonthChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = position+1;
                if(selectedMonth == 2){
                    if(isLeapYear)
                        monthDays = 29;
                    else
                        monthDays = 28;
                }else
                    monthDays = DateUtils.getMonthDays(selectedMonth);



                ArrayList<Integer> daysRange = new ArrayList<>();
                for(int i = 1; i <= monthDays; i++){
                    daysRange.add(i);
                }

                // Creating adapter for spinner days spinner but don't set any entries until we know what month user has chosen
                ArrayAdapter<Integer> daysAdapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, daysRange);
                // Drop down layout style - list view with radio button
                daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                mDayChooser.setAdapter(daysAdapter);

                mDayChooser.setClickable(true);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "you need to choose a month before selecting a day !", Toast.LENGTH_SHORT).show();
            }
        });


        mDayChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDay = position+1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "you need to choose the specified day of planting!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setUpPriceQualityChoosers(){

        mPriceChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPrice = position+1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "you need to select the product price !", Toast.LENGTH_SHORT).show();
            }
        });

        mQuantityChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedQuantity = position+1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "you need to select the quantity of this product in your warehouse !", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setUpCategoryChooser() {
        String[] categories = getResources().getStringArray(R.array.plants_categories);

        mCategoryChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "you need to select the product price !", Toast.LENGTH_SHORT).show();
            }
        });
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
            }
            else
            {
                Toast.makeText(getContext(),"no image selected", Toast.LENGTH_LONG).show();
            }

            mUploadImageBtn.setBackgroundResource(R.color.white);

            Toast.makeText(getContext(), "done", Toast.LENGTH_SHORT).show();
            Glide.with(this)
                    .load(bitmap)
                    .into(mUploadImageBtn);
        }
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



    void saveProduct(){

        final ProgressDialog pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Processing Please wait...");
        pDialog.show();

        File file = new File(filePath);
        String name = mNameET.getText().toString();
        String description = mDescriptionET.getText().toString();
        String plantingDate = selectedYear+"-"+selectedMonth+"-"+selectedDay;
        String plantingAddress = "";
        String price = String.valueOf(selectedPrice);
        String quantity = String.valueOf(selectedQuantity);


        AndroidNetworking.upload(Constants.BASE_URL)
                .addMultipartParameter("type", "add_product")
                .addMultipartFile("image", file)
                .addMultipartParameter("name", name)
                .addMultipartParameter("description", description)
                .addMultipartParameter("planting_date", plantingDate)
                .addMultipartParameter("planting_address", plantingAddress)
                .addMultipartParameter("price", price)
                .addMultipartParameter("quantity", quantity)
                .addMultipartParameter("category", selectedCategory)
                .addMultipartParameter("seller_id", String.valueOf(SharedPrefManager.getInstance(getContext()).getUserId()))
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
                        Log.e("Result", response.toString());
                        Toast.makeText(requireContext(), "added successfully", Toast.LENGTH_SHORT).show();

                        requireActivity().onBackPressed();
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
