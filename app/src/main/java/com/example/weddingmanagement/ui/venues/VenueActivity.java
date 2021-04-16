package com.example.weddingmanagement.ui.venues;


import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wedding.BaseActivity;
import com.example.wedding.R;
import com.example.wedding.adapters.CommentsVenueAdapter;
import com.example.wedding.models.Comments.Comment;
import com.example.wedding.models.Response.AverageRatings;
import com.example.wedding.models.Response.BookingResponse;
import com.example.wedding.models.Response.CommentsResponse;
import com.example.wedding.models.Response.DatesResponse;
import com.example.wedding.models.Response.RateVenuesResponse;
import com.example.wedding.models.Response.VenueResponse;
import com.example.wedding.models.Response.WeddingResponse;
import com.example.wedding.models.Venues.Location;
import com.example.wedding.services.NotificationChannel;
import com.example.wedding.utils.Constants;
import com.example.wedding.utils.Validation;
import com.example.wedding.viewmodels.ViewModelProviderFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jama.carouselview.CarouselView;
import com.jama.carouselview.CarouselViewListener;
import com.jama.carouselview.enums.IndicatorAnimationType;
import com.jama.carouselview.enums.OffsetType;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hari.bounceview.BounceView;

public class VenueActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener {

    ImageView venueimg;
    TextView venuename, venuelocation, venuedesc,venuetype, veg, nonveg, indoor, outdoor ;
    private View star1, star2, star3, star4, star5;
    private List<Comment> commentsList = new ArrayList<>();
    private List<Location> locationlist = new ArrayList<>();
    private RecyclerView commentsView, venuephoto;
    private GoogleMap mMap;
    private EditText commenttxt;
    private Button submitcomment;
    private Fragment venuemap;
    private List<String> albumlist = new ArrayList<>();
    String address = "Softwarica College of IT and E-Commerce";
    double lat = 27.706442;
    double lng = 85.330093;
    SharedPreferences msharedpreferences;

    private Button booknowvenue;


    NotificationManagerCompat notificationManagerCompat;

    private ArrayList<String> weddingnames = new ArrayList<>();
    private ArrayList<String> weddingidlist = new ArrayList<>();

    private List<View> venueStarList = new ArrayList<>();
    private List<View> userStarList = new ArrayList<>();

    private Spinner weddingsSpinner, datesSpinner;

    private VenuesViewModel viewModel;
    private String id;
    private String weddingid = "";
    private String bookingdate = "";
    private String token;
    Dialog dialog;

    @Inject
     ViewModelProviderFactory providerFactory;

    private View venuestar1, venuestar2, venuestar3, venuestar4, venuestar5;
    private Button booknow;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue);

        Toolbar toolbar = (Toolbar)  findViewById(R.id.toolbar2);
        toolbar.setTitle("Venue");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = ProgressDialog.show(this, "",
                "Loading. Please Wait...", true);

        progressDialog.show();

        venueimg = findViewById(R.id.venueimg);
        venuename = findViewById(R.id.venuename);
        venuelocation = findViewById(R.id.venuelocation);
        commentsView = findViewById(R.id.commentsview);
        venuedesc = findViewById(R.id.venuedesc);
        venuetype = findViewById(R.id.venuetypefield);
        veg = findViewById(R.id.veg);
        nonveg = findViewById(R.id.nonveg);
        indoor = findViewById(R.id.indoor);
        outdoor = findViewById(R.id.outdoor);
        commenttxt = findViewById(R.id.commenttxt);
        submitcomment = findViewById(R.id.submitcomment);
        venuestar1 = findViewById(R.id.venuestar1);
        venuestar2 = findViewById(R.id.venuestar2);
        venuestar3 = findViewById(R.id.venuestar3);
        venuestar4 = findViewById(R.id.venuestar4);
        venuestar5 = findViewById(R.id.venuestar5);
        booknowvenue = findViewById(R.id.booknowvenue);
//        venuephoto = findViewById(R.id.venuesphoto);


        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);








        notificationManagerCompat = NotificationManagerCompat.from(this);
        NotificationChannel createChannel = new NotificationChannel(this);
        createChannel.createChannel();

        dialog = new Dialog(VenueActivity.this,R.style.AppTheme_ModalTheme);

        DisplayMetrics dm = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;



        dialog.setContentView(R.layout.activity_book_now_modal);

        BounceView.addAnimTo(dialog);


//                 RelativeLayout bookmodal = dialog.findViewById(R.id.bookmodal);
//                 bookmodal.setLayoutParams(new RelativeLayout.LayoutParams(
//                         /*width*/ width - 100,
//                         /*height*/ height - 100
//
//                 ));




        booknowvenue.setOnClickListener(new View.OnClickListener() {
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

        booknow = dialog.findViewById(R.id.bookbtn);

        booknow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateBooking();
            }
        });

        msharedpreferences = getSharedPreferences("login", MODE_PRIVATE);

        venueStarList.add(venuestar1);
        venueStarList.add(venuestar2);
        venueStarList.add(venuestar3);
        venueStarList.add(venuestar4);
        venueStarList.add(venuestar5);


        userStarList.add(star1);
        userStarList.add(star2);
        userStarList.add(star3);
        userStarList.add(star4);
        userStarList.add(star5);


//        Log.d("asdasd", "onCreate: " + msharedpreferences.getString(Constants.TOKEN, null));
        token = msharedpreferences.getString(Constants.TOKEN, null);


        Intent intent = getIntent();

        id = intent.getStringExtra("venueid");



        viewModel  = ViewModelProviders.of(this, providerFactory).get(VenuesViewModel.class);



        submitcomment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                submitComment(id, token);
            }
        });

        star1.setOnClickListener(this);
        star2.setOnClickListener(this);
        star3.setOnClickListener(this);
        star4.setOnClickListener(this);
        star5.setOnClickListener(this);


        viewModel.getVenue(id);
        viewModel.getComments(id);
        viewModel.getAvgRatings(id);
        viewModel.getRatingsByUser(id, token);
        viewModel.getWeddings(token);
        viewModel.getVenueDates(id, token);



        observeVenue();
        observeWeddings();
        observeDates();
        observeComments();
        observeAddComments();
        observeAVGRatings();
        observeUserRatings();
        observeRatingsByUser();
        observeBooking();


    }


    //Rating the venue
    private void rateVenue(int rating){
//        Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
        viewModel.rateVenue(id, rating, token);



    }

    //Observing Response from Booking Venue
    private void observeBooking(){
        viewModel.observeBooking().observe(this, new Observer<BookingResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
            @Override
            public void onChanged(BookingResponse bookingResponse) {
                if(bookingResponse.isSuccess()){

                    Toast.makeText(VenueActivity.this, bookingResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    if(bookingResponse.getMessage().equals("Booking Completed")){

                        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
                        Uri geoUri = Uri.parse("geo:"+lat+","+lng+"?q=" + Uri.encode(address));
                        mapIntent.setData(geoUri);
                        PendingIntent mapPendingIntent =
                                PendingIntent.getActivity(VenueActivity.this, 0, mapIntent, 0);

                        NotificationCompat.Action action =
                                new NotificationCompat.Action.Builder(R.drawable.ic_pin,
                                        getString(R.string.title_activity_maps), mapPendingIntent)
                                        .setAllowGeneratedReplies(true)
                                        .build();

                        Notification notification = new NotificationCompat.Builder(VenueActivity.this, NotificationChannel.Channel_One)
                                .setContentTitle("Booked ")
                                .setSmallIcon(R.drawable.ic_wedding_bells)
                                .setContentText("You have booked a venue for " +  bookingResponse.getBookingdate())
                                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                .addAction(R.drawable.ic_pin,
                                        getString(R.string.title_activity_maps), mapPendingIntent)
                                .extend(new NotificationCompat.WearableExtender().addAction(action))
                                .build();
                        notificationManagerCompat.notify(1,notification);
                        Vibrator vibrator   = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(500);


                    }else{

                    }




                }else{
                    Toast.makeText(VenueActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    //Observe Data Available Dates for Wedding Booking
    private void observeDates(){

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
        viewModel.observeDates().observe(this, new Observer<DatesResponse>() {
            @Override
            public void onChanged(DatesResponse datesResponse) {
                if(datesResponse.isSuccess()){


                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(VenueActivity.this, android.R.layout.simple_list_item_1, datesResponse.getDates());
                    datesSpinner.setAdapter(arrayAdapter);

                }
            }
        });
    }

    public void observeRatingsByUser(){
        viewModel.observeRatingsByUser().observe(this, new Observer<RateVenuesResponse>() {
            @Override
            public void onChanged(RateVenuesResponse rateVenuesResponse) {
                if(rateVenuesResponse.isSuccess()){

                    int rating = rateVenuesResponse.getRating().getRating();
                    setUserRatings(rating);
                }
            }
        });
    }

    //Observe change in ratings
    private void observeUserRatings(){
        viewModel.observeUserRatings().observe(this, new Observer<RateVenuesResponse>() {
            @Override
            public void onChanged(RateVenuesResponse rateVenuesResponse) {
                if(rateVenuesResponse.isSuccess()){
                    viewModel.getAvgRatings(id);
                    int rating = rateVenuesResponse.getRating().getRating();
//                    Toast.makeText(VenueActivity.this, "" + rating, Toast.LENGTH_LONG).show();
                    setUserRatings(rating);


                }

            }
        });
    }


    //Observe change in livedata for getting avg ratings of the venue
    private void observeAVGRatings(){
        viewModel.observeAvgRatings().observe(this, new Observer<AverageRatings>() {
            @Override
            public void onChanged(AverageRatings averageRatings) {
                    if(averageRatings.isSuccess()){

                       double avg = averageRatings.getAverageRating();

                        setRatings(avg);

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
        viewModel.observeWeddings().observe(this, new Observer<WeddingResponse>() {
            @Override
            public void onChanged(WeddingResponse weddingResponse) {
                if(weddingResponse.isSuccess()){



                    for (WeddingResponse.GetWedding wedding:
                         weddingResponse.getWeddings()) {

                        weddingnames.add(wedding.getGroomName() + " & " + wedding.getBrideName());
                        weddingidlist.add(wedding.getId());

                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(VenueActivity.this, android.R.layout.simple_list_item_1, weddingnames);
                    weddingsSpinner.setAdapter(arrayAdapter);

                }
            }
        });
    }


    private void setUserRatings(int rating) {

        final int sdk = Build.VERSION.SDK_INT;

        if(sdk < Build.VERSION_CODES.JELLY_BEAN) {

            for(int i = 0; i < rating; i++){
                userStarList.get(i).setBackgroundDrawable(ContextCompat.getDrawable(VenueActivity.this, R.drawable.ic_starrated));

            }
            for(int i = (int) rating; i < userStarList.size(); i++){
                userStarList.get(i).setBackgroundDrawable(ContextCompat.getDrawable(VenueActivity.this, R.drawable.ic_star));

            }


        }else{
            for(int i = 0; i < rating; i++){
                userStarList.get(i).setBackground(ContextCompat.getDrawable(VenueActivity.this, R.drawable.ic_starrated));

            }
            for(int i = (int) rating; i < userStarList.size(); i++){
                userStarList.get(i).setBackground(ContextCompat.getDrawable(VenueActivity.this, R.drawable.ic_star));

            }
        }
    }

//Setting the Ratings Star
    private void setRatings(double avg){

        final int sdk = Build.VERSION.SDK_INT;
        long average = Math.round(avg);

//        Toast.makeText(this, "" + average, Toast.LENGTH_SHORT).show();

        if(sdk < Build.VERSION_CODES.JELLY_BEAN) {

            for(int i = 0; i < average; i++){
                venueStarList.get(i).setBackgroundDrawable(ContextCompat.getDrawable(VenueActivity.this, R.drawable.ic_starrated));

            }
            for(int i = (int) average; i < venueStarList.size(); i++){
                venueStarList.get(i).setBackgroundDrawable(ContextCompat.getDrawable(VenueActivity.this, R.drawable.ic_star));

            }


        }else{
            for(int i = 0; i < average; i++){
                venueStarList.get(i).setBackground(ContextCompat.getDrawable(VenueActivity.this, R.drawable.ic_starrated));

            }
            for(int i = (int) average; i < venueStarList.size(); i++){
                venueStarList.get(i).setBackground(ContextCompat.getDrawable(VenueActivity.this, R.drawable.ic_star));

            }
        }

    }
    //Comment Submit Method
    private void submitComment(String venueid, String token){
        String comment  = commenttxt.getText().toString();

        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(
                getApplicationContext().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(commenttxt.getWindowToken(), 0);

        if(!Validation.validateFields(comment)){
            commenttxt.setError("Comment Must not Be Empty!");
            commenttxt.requestFocus();

        }else{
            viewModel.postComments(venueid,comment, token);
        }
    }

    public void ifCommentSuccessful(){
        commenttxt.setText(null);
        commenttxt.setError(null);
    }

    public void observeAddComments(){
        viewModel.observeAddComments().observe(this, new Observer<CommentsResponse>() {
            @Override
            public void onChanged(CommentsResponse commentsResponse) {
                if(commentsResponse.isSuccess()){
                    ifCommentSuccessful();
                    if(!commentsResponse.getComments().isEmpty()){
                        commentsList.clear();

                        for (Comment comment:
                                commentsResponse.getComments()) {

                            commentsList.add(comment);
                            CommentsVenueAdapter commentsVenueAdapter = new CommentsVenueAdapter(VenueActivity.this, commentsList, viewModel);

                            commentsView.setLayoutManager(new LinearLayoutManager(VenueActivity.this));

                            commentsView.setAdapter(commentsVenueAdapter);

                        }

                    }
                }
            }
        });
    }




    private void observeVenue(){

        viewModel.observeVenue().observe(this, new Observer<VenueResponse>() {
            @Override
            public void onChanged(VenueResponse venueResponse) {
                if(venueResponse.isSuccess()){
                    String uri = Constants.IMAGE_URL + venueResponse.getVenue().get(0).getImg();

                    Picasso.get().load(uri).into(venueimg);

                    venuename.setText(venueResponse.getVenue().get(0).getVenueName());
                    venuelocation.setText(venueResponse.getVenue().get(0).getLocation().getAddress());
                    venuedesc.setText(venueResponse.getVenue().get(0).getVenueDesc());
                    venuetype.setText(venueResponse.getVenue().get(0).getVenueType());
                    veg.setText(venueResponse.getVenue().get(0).getVenuePricing().getVeg());
                    nonveg.setText(venueResponse.getVenue().get(0).getVenuePricing().getNonVeg());
                    indoor.setText(venueResponse.getVenue().get(0).getVenueCapacity().getIndoor());
                    outdoor.setText(venueResponse.getVenue().get(0).getVenueCapacity().getOutdoor());
                    address = venueResponse.getVenue().get(0).getLocation().getAddress();

                    locationlist.add(new Location(null,venueResponse.getVenue().get(0).getLocation().getAddress(), venueResponse.getVenue().get(0).getLocation().getLat(),
                    venueResponse.getVenue().get(0).getLocation().getLng()));

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.venuemap);
                    mapFragment.getMapAsync(VenueActivity.this::onMapReady);


                    for (String albumimage:
                         venueResponse.getVenue().get(0).getAlbum()) {

                        albumlist.add(albumimage);
                    }

                    if(!albumlist.isEmpty()){
                        carousel("venues", venueResponse.getVenue().get(0).getId());

                    }else{

                    }


//                    AlbumAdapter adapter = new AlbumAdapter(albumlist,VenueActivity.this, venueResponse.getVenue().get(0).getId(), "venues");
//                    venuephoto.setLayoutManager(new LinearLayoutManager(VenueActivity.this, LinearLayoutManager.HORIZONTAL, false));
//                    venuephoto.setAdapter(adapter);
                    progressDialog.cancel();
                }
            }
        });


    }


    public void carousel(String type, String venueid){
        CarouselView carouselView = findViewById(R.id.carousel);

        carouselView.setSize(albumlist.size());
        carouselView.setResource(R.layout.album_layout);
        carouselView.setAutoPlay(true);
        carouselView.setIndicatorAnimationType(IndicatorAnimationType.THIN_WORM);
        carouselView.setCarouselOffset(OffsetType.CENTER);
        carouselView.setCarouselViewListener(new CarouselViewListener() {
            @Override
            public void onBindView(View view, int position) {
                // Example here is setting up a full image carousel
                ImageView imageView = view.findViewById(R.id.albumphoto);
                String uri = Constants.IMAGE_URL  + type + "/" + venueid + "/" +  albumlist.get(position);
                Log.d("asd", "onBindView: " + uri);
                Picasso.get().load(uri).into(imageView);

            }
        });
        // After you finish setting up, show the CarouselView
        carouselView.show();
    }


    public void observeComments(){
        viewModel.observeComments().observe(this, new Observer<CommentsResponse>() {
            @Override
            public void onChanged(CommentsResponse commentsResponse) {
                    if(commentsResponse.isSuccess()){
                        if(!commentsResponse.getComments().isEmpty()){

                            for (Comment comment:
                                 commentsResponse.getComments()) {
                                commentsList.clear();
                                commentsList.add(comment);
                                CommentsVenueAdapter commentsVenueAdapter = new CommentsVenueAdapter(VenueActivity.this, commentsList , viewModel);

                                commentsView.setLayoutManager(new LinearLayoutManager(VenueActivity.this));

                                commentsView.setAdapter(commentsVenueAdapter);

                            }

                        }

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
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        for(Location location : locationlist){
            loadLocation(googleMap, location.getLat(), location.getLng(), location.getAddress());
        }

    }

public void loadLocation(GoogleMap googleMap, String lat, String lng, String address){
    mMap = googleMap;
    LatLng place = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
    mMap.addMarker(new MarkerOptions().position(place).title(address));
    mMap.moveCamera(CameraUpdateFactory.newLatLng(place));
    mMap.animateCamera( CameraUpdateFactory.zoomTo(16));

}

    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.star1:
                rateVenue(1);
                break;

            case R.id.star2:
                rateVenue(2);
                break;

            case R.id.star3:
                rateVenue(3);
                break;

            case R.id.star4:
                rateVenue(4);
                break;

            case R.id.star5:
                rateVenue(5);
                break;

        }
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
            viewModel.addBookings("venue", weddingid, id, bookingdate, token );
        }


    }



}
