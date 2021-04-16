package com.example.weddingmanagement.ui.main.wedding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.wedding.BaseActivity;
import com.example.wedding.R;
import com.example.wedding.models.Response.WeddingResponse;
import com.example.wedding.utils.Constants;
import com.example.wedding.utils.Validation;
import com.example.wedding.viewmodels.ViewModelProviderFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.inject.Inject;

public class PlanningActivity extends BaseActivity {

    EditText gname, bname;
    DatePicker weddingdate;
    Button addweddingbtn;
    private String weddate = "";


    private WeddingViewModel viewModel;
    private String token;

    @Inject
    ViewModelProviderFactory providerFactory;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);


        Toolbar toolbar = (Toolbar)  findViewById(R.id.toolbar2);
        toolbar.setTitle("Register Wedding");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        token = sharedPreferences.getString(Constants.TOKEN, null);

        gname = findViewById(R.id.gname);
        bname = findViewById(R.id.bname);
        weddingdate = findViewById(R.id.weddingdate);

        addweddingbtn = findViewById(R.id.regwedbtn);


        viewModel  = ViewModelProviders.of(this, providerFactory).get(WeddingViewModel.class);

        addweddingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateWeddingForm();
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar currentDate = Calendar.getInstance();
        weddate =  dateFormat.format(currentDate.getTime());


        weddingdate.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                weddate =  dateFormat.format(newDate.getTime());
            }
        });

        observeRegWedding();

    }



    private void validateWeddingForm(){
        String groom = gname.getText().toString();
        String bride = bname.getText().toString();

        int err= 0;

        if(!Validation.validateFields(groom)){
            err++;
            gname.setError("Please enter Groom's Name!!");
                    gname.requestFocus();
        }

        if(!Validation.validateFields(bride)){
            err++;
            bname.setError("Please enter Bride's Name!!");
            bname.requestFocus();
        }

        if(!Validation.validateFields(weddate)){
            err++;
        }

        if(err == 0){
            viewModel.registerWedding(token, groom, bride, weddate);
        }
    }


    private void observeRegWedding(){
        viewModel.observeRegisterWedding().observe(this, new Observer<WeddingResponse>() {
                @Override
                public void onChanged(WeddingResponse weddingResponse) {
                    if(weddingResponse.isSuccess()){
                        Intent intent = new Intent(PlanningActivity.this, WeddingActivity.class);
                        intent.putExtra("weddingid", weddingResponse.getWedding().getId());
                        startActivity(intent);
                    }else{
                        Toast.makeText(PlanningActivity.this, "Failed To Register Wedding", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}
