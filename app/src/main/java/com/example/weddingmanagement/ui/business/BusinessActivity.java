package com.example.weddingmanagement.ui.business;

import android.app.Dialog;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wedding.BaseActivity;
import com.example.wedding.R;
import com.example.wedding.adapters.AlbumAdapter;
import com.example.wedding.models.Business.Location;
import com.example.wedding.models.Response.BookingResponse;
import com.example.wedding.models.Response.BusinessResponse;
import com.example.wedding.models.Response.DatesResponse;
import com.example.wedding.models.Response.WeddingResponse;
import com.example.wedding.services.NotificationChannel;
import com.example.wedding.utils.Constants;
import com.example.wedding.viewmodels.ViewModelProviderFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hari.bounceview.BounceView;

public class BusinessActivity extends BaseActivity  implements OnMapReadyCallback {


    private TextView businessname, businesslocation, businessdesc, businesstype, businessprice;
    private ImageView businessimage;
    private ImageView businesstypeimg;
    private  BusinessViewModel viewModel;

    private Button bookbtn;

    String address = "Softwarica College of IT and E-Commerce";
    double lat = 27.706442;
    double lng = 85.330093;
    private Fragment businessmap;
    private List<Location> locationlist = new ArrayList<>();
    private GoogleMap mMap;
    private RecyclerView businessphotos;

    private List<String> albumlist = new ArrayList<>();
    Dialog dialog;

    private Button booknow;
    private Spinner weddingsSpinner, datesSpinner;
    private String weddingid = "";
    private String bookingdate = "";
    private ArrayList<String> weddingnames = new ArrayList<>();
    private ArrayList<String> weddingidlist = new ArrayList<>();
        String token;
    String businessid;
    private ProgressDialog progressDialog;

    NotificationManagerCompat notificationManagerCompat;

    @Inject
    ViewModelProviderFactory providerFactory;


    SharedPreferences msharedpreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);

        Toolbar toolbar = (Toolbar)  findViewById(R.id.toolbar2);
        toolbar.setTitle("Business");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        msharedpreferences = getSharedPreferences("login", MODE_PRIVATE);
        
        notificationManagerCompat = NotificationManagerCompat.from(this);
        NotificationChannel createChannel = new NotificationChannel(this);
        createChannel.createChannel();


        progressDialog = ProgressDialog.show(this, "",
                "Loading. Please Wait...", true);

        progressDialog.show();

        viewInit();

        Intent intent = getIntent();
        businessid = intent.getStringExtra("businessid");

        token = msharedpreferences.getString(Constants.TOKEN, null);

        viewModel = ViewModelProviders.of(this, providerFactory ).get(BusinessViewModel.class);

        viewModel.getBusinessById(businessid);
        viewModel.getWeddings(token);
        viewModel.getAvailableDates(token, businessid);

        dialog = new Dialog(BusinessActivity.this,R.style.AppTheme_ModalTheme);

        dialog.setContentView(R.layout.activity_book_now_modal);

        BounceView.addAnimTo(dialog);


        bookbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        Button bookouter = dialog.findViewById(R.id.bookouter);
        bookouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });



        observeBusiness();
        observeAvailableDates();
        observeWeddings();
        observeBooking();


        booknow = dialog.findViewById(R.id.bookbtn);

        booknow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateBooking();
            }
        });
    }

    private void observeBooking(){
        viewModel.observeBooking().observe(this, new Observer<BookingResponse>() {
            @Override
            public void onChanged(BookingResponse bookingResponse) {
                if(bookingResponse.isSuccess()){

                    Toast.makeText(BusinessActivity.this, bookingResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    if(bookingResponse.getMessage().equals("Booking Completed")){


                        Notification notification = new NotificationCompat.Builder(BusinessActivity.this, NotificationChannel.Channel_One)
                                .setSmallIcon(R.drawable.ic_launcher_background)
                                .setContentTitle("Booked ")
                                .setSmallIcon(R.drawable.ic_wedding_bells)
                                .setContentText("You have booked this business for " +  bookingResponse.getBookingdate())
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                .build();
                        notificationManagerCompat.notify(1,notification);
                        Vibrator vibrator   = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(500);
                }
            }
        }

        });

    }

    private void observeAvailableDates(){
        datesSpinner = dialog.findViewById(R.id.selectdates);

        datesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bookingdate = datesSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bookingdate = "";
            }
        });
        viewModel.observeAvailableDate().observe(this, new Observer<DatesResponse>() {
            @Override
            public void onChanged(DatesResponse datesResponse) {
                if(datesResponse.isSuccess()){


                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(BusinessActivity.this, android.R.layout.simple_list_item_1, datesResponse.getDates());
                    datesSpinner.setAdapter(arrayAdapter);

                }
            }
        });
    }




    public void observeWeddings(){
        weddingsSpinner = dialog.findViewById(R.id.selectwedding);

        weddingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                weddingid = weddingidlist.get(position);
//                Toast.makeText(VenueActivity.this, weddingid, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                weddingid = "";
            }
        });
        viewModel.observewedding().observe(this, new Observer<WeddingResponse>() {
            @Override
            public void onChanged(WeddingResponse weddingResponse) {
                if(weddingResponse.isSuccess()){



                    for (WeddingResponse.GetWedding wedding:
                            weddingResponse.getWeddings()) {

                        weddingnames.add(wedding.getGroomName() + " & " + wedding.getBrideName());
                        weddingidlist.add(wedding.getId());

                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(BusinessActivity.this, android.R.layout.simple_list_item_1, weddingnames);
                    weddingsSpinner.setAdapter(arrayAdapter);


                }
            }
        });
    }

    private void viewInit(){
        businessname = findViewById(R.id.businessname);
        businessdesc = findViewById(R.id.businessdesc);
        businesslocation = findViewById(R.id.businesslocation);
        businessimage = findViewById(R.id.businessimg);
        businesstype = findViewById(R.id.businesstypefield);
        businessprice = findViewById(R.id.businesspricefield);
        bookbtn = findViewById(R.id.booknowbusiness);
        businesstypeimg = findViewById(R.id.businesstypeimg);
        businessphotos = findViewById(R.id.businessphotos);
        booknow = findViewById(R.id.booknowbusiness);

    }

    private void observeBusiness(){
        viewModel.observeBusiness().observe(this, new Observer<BusinessResponse>() {
            @Override
            public void onChanged(BusinessResponse businessResponse) {
                if(businessResponse.isSuccess()){
                    businessname.setText(businessResponse.getBusiness().getBusinessname());
                    businesslocation.setText(businessResponse.getBusiness().getBusinesslocation().getAddress());
                    businessdesc.setText(businessResponse.getBusiness().getBusinessdesc());
                    businesstype.setText(businessResponse.getBusiness().getBusinesstype());

                    String type = businessResponse.getBusiness().getBusinesstype();
                    if(type.equals("Photography")){


                        businesstypeimg.setImageResource(R.drawable.ic_camera);
                    }else if(type.equals("Bakery")){


                        businesstypeimg.setImageResource(R.drawable.ic_cake);
                    }else if(type.equals("Beauty Parlor")){

                        businesstypeimg.setImageResource(R.drawable.ic_makeup);

                    }


                    businessprice.setText(businessResponse.getBusiness().getBusinesspricing());

                    String uri = Constants.IMAGE_URL + businessResponse.getBusiness().getBusinessImage();
                    Picasso.get().load(uri).into(businessimage);

                    String address  = businessResponse.getBusiness().getBusinesslocation().getAddress();
                    String lat = businessResponse.getBusiness().getBusinesslocation().getLat();
                    String lng = businessResponse.getBusiness().getBusinesslocation().getLng();
                    locationlist.add(new Location(address, lat, lng));

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.businessmap);


                    mapFragment.getMapAsync(BusinessActivity.this::onMapReady);

                    for (String photo:
                         businessResponse.getBusiness().getAlbum()) {
                        albumlist.add(photo);
                    }



                    AlbumAdapter adapter = new AlbumAdapter(albumlist, BusinessActivity.this, businessResponse.getBusiness().getId(), "business");
                    businessphotos.setLayoutManager(new LinearLayoutManager(BusinessActivity.this, LinearLayoutManager.HORIZONTAL, false));
                    businessphotos.setAdapter(adapter);

                    progressDialog.cancel();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.logout:
                sessionManager.logOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        for(Location location: locationlist){
            loadLocation(googleMap, location.getLat(), location.getLng(), location.getAddress());
        }
    }


    private void loadLocation(GoogleMap googleMap, String lat, String lng, String address){
        mMap = googleMap;
        LatLng place = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        mMap.addMarker(new MarkerOptions().position(place).title(address));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
        mMap.animateCamera( CameraUpdateFactory.zoomTo(16));

    }

    private void validateBooking(){

        int err= 0;
        if(weddingid.isEmpty()){
            err++;
            Toast.makeText(this, "Select Wedding", Toast.LENGTH_SHORT).show();

        }
        if(bookingdate.isEmpty()){
            err++;
            Toast.makeText(this, "Select Date", Toast.LENGTH_SHORT).show();
        }


        if(err == 0){
            viewModel.addBookings("business", weddingid, businessid, bookingdate, token );
        }


    }
}
