package com.example.weddingmanagement.ui.main.wedding;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wedding.BaseActivity;
import com.example.wedding.R;
import com.example.wedding.adapters.ImageSliderAdapter;
import com.example.wedding.models.Response.DeleteBookingResponse;
import com.example.wedding.models.Response.DeleteResponse;
import com.example.wedding.models.Wedding.AlbumResponse;
import com.example.wedding.models.Wedding.Wedding;
import com.example.wedding.ui.BusinessSearchActivity;
import com.example.wedding.ui.SearchActivity;
import com.example.wedding.ui.business.BusinessActivity;
import com.example.wedding.ui.main.DrawerActivity;
import com.example.wedding.ui.venues.VenueActivity;
import com.example.wedding.utils.Constants;
import com.example.wedding.viewmodels.ViewModelProviderFactory;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import hari.bounceview.BounceView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class WeddingActivity extends BaseActivity {


    private static final String TAG = "WeddingActivity";
    WeddingViewModel viewModel;

    Uri picUri;
    List<Uri> imageUri = new ArrayList<>();
    List<Bitmap> bitmapArrayList = new ArrayList<>();

    private final static int IMAGE_RESULT = 200;
    private Bitmap mBitmap;



    @Inject
    ViewModelProviderFactory providerFactory;

    private ImageView venueimg, photoimg, bakeryimg, beautyimg, venueadd, photoadd, bakeryadd, beautyadd;
    private TextView weddingname, weddingdateshow, wedvenuename, wedphotoname,wedbakeryname, wedbeautyname, deletewedding;
    private RelativeLayout venueoverlay, photooverlay, bakeryoverlay, beautyoverlay;
    private GridLayout weddingGrid;
    private RecyclerView sliderView;
    private Button addtoalbum, gallerybtn, uploadalbum,invitationlink;
    ImageSliderAdapter imgadapter;
    private TextView venuedelete, photodelete, bakerydelete, makeupdelete;
    String token;
    String weddingid;
    private LinearLayout pickedImageContainer;
    ProgressDialog progressDialog;
    Dialog albumdialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wedding);

        Intent intent = getIntent();
        weddingid = intent.getStringExtra("weddingid");

//        Toast.makeText(this, weddingid, Toast.LENGTH_SHORT).show();

        viewInit();


        Toolbar toolbar = (Toolbar)  findViewById(R.id.toolbar2);
        toolbar.setTitle("Wedding Profile");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        albumdialog = new Dialog(WeddingActivity.this,R.style.AppTheme_ModalTheme);



        albumdialog.setContentView(R.layout.add_album_layout);
        BounceView.addAnimTo(albumdialog);

        uploadalbum = albumdialog.findViewById(R.id.uploadalbum);


        gallerybtn = albumdialog.findViewById(R.id.gallerybtn);


        gallerybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_RESULT);
            }
        });

        addtoalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                albumdialog.show();
            }
        });

        uploadalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadToAlbum();
            }
        });




        SharedPreferences sharedPreferences  = getSharedPreferences("login", MODE_PRIVATE);

        token = sharedPreferences.getString(Constants.TOKEN, null);


        viewModel  = ViewModelProviders.of(this, providerFactory).get(WeddingViewModel.class);

        viewModel.getWedding(weddingid, token);

        observeWedding();
        observeDeleteBookedVenue();
        observeDeleteBookedBusiness();
        observeAlbum();
        observeDeleteWedding();


        deletewedding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });


    }


    private void viewInit(){

        venueimg = findViewById(R.id.venueimage);
        venueadd = findViewById(R.id.venueadd);
        venueoverlay = findViewById(R.id.venueoverlay);
        wedvenuename = findViewById(R.id.wedvenuename);

        photoimg = findViewById(R.id.photoimg);
        photoadd = findViewById(R.id.photoadd);
        photooverlay = findViewById(R.id.photooverlay);
        wedphotoname = findViewById(R.id.wedphotoname);

        bakeryimg = findViewById(R.id.bakeryimg);
        bakeryadd = findViewById(R.id.bakeryadd);
        bakeryoverlay = findViewById(R.id.bakeryoverlay);
        wedbakeryname = findViewById(R.id.wedbakeryname);

        beautyimg = findViewById(R.id.beautyimg);
        beautyadd = findViewById(R.id.beautyimgadd);
        beautyoverlay = findViewById(R.id.beautyoverlay);
        wedbeautyname  = findViewById(R.id.wedbeautyname);

        weddingname = findViewById(R.id.weddingname);
        weddingdateshow = findViewById(R.id.weddingdateshow);
        weddingGrid = findViewById(R.id.weddingGrid);


        sliderView = findViewById(R.id.albumview);


        venuedelete = findViewById(R.id.deletevenue);
        photodelete = findViewById(R.id.deletephoto);
        bakerydelete = findViewById(R.id.deletebakery);
        makeupdelete = findViewById(R.id.deletemakeup);

        deletewedding = findViewById(R.id.deletewedding);

        addtoalbum = findViewById(R.id.addwedalbum);
        invitationlink = findViewById(R.id.invitationlink);


        invitationlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeddingActivity.this, InvitationActivity.class);
                intent.putExtra("weddingid", weddingid);
                startActivity(intent);
            }
        });


        venueadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeddingActivity.this, SearchActivity.class);
                intent.putExtra("search_location", "kathmandu");
                startActivity(intent);

            }
        });


        photoadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeddingActivity.this, BusinessSearchActivity.class);
                intent.putExtra("category", "Photography");
                startActivity(intent);

            }
        });


        bakeryadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeddingActivity.this, BusinessSearchActivity.class);
                intent.putExtra("category", "Bakery");
                startActivity(intent);

            }
        });



        beautyadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeddingActivity.this, BusinessSearchActivity.class);
                intent.putExtra("category", "Beauty Parlor");
                startActivity(intent);

            }
        });








    }
    
    
    private void observeAlbum(){
        viewModel.observeAlbum().observe(this, new Observer<AlbumResponse>() {
            @Override
            public void onChanged(AlbumResponse albumResponse) {
                if(albumResponse.isSuccess()){
                    Toast.makeText(WeddingActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    viewModel.getWedding(weddingid, token);
                    progressDialog.cancel();
                    albumdialog.cancel();
                }else{
                    Log.d(TAG, "onChanged: "+ albumResponse.getMessage() );
                    progressDialog.cancel();

                }
            }
        });
    }


    private void uploadToAlbum(){

    progressDialog = ProgressDialog.show(this, "",
            "Loading. Please Wait...", true);
        try{

            MultipartBody.Part[] multipartTypedOutput = new MultipartBody.Part[bitmapArrayList.size()];

            for(int i = 0; i < bitmapArrayList.size(); i++){
                File filesDir = getApplicationContext().getFilesDir();
                File file = new File(filesDir, "image" + i + ".png");

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmapArrayList.get(i).compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();



                Log.d(TAG, "multipartUpload: "+ file);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);

                 multipartTypedOutput[i] = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

}

            RequestBody sendweddingid = RequestBody.create(MediaType.parse("text/plain"), weddingid);

            viewModel.addToAlbum(token, sendweddingid, multipartTypedOutput);

        }catch (FileNotFoundException e){

        }catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void observeDeleteBookedVenue(){
        viewModel.observeDeleteBookingVenue().observe(this, new Observer<DeleteBookingResponse>() {
            @Override
            public void onChanged(DeleteBookingResponse deleteBookingResponse) {
                if(deleteBookingResponse.isSuccess()){
                    viewModel.getWedding(weddingid, token);
                    Toast.makeText(WeddingActivity.this, "Deleted  Booking", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    private void observeDeleteBookedBusiness(){
        viewModel.observeDeleteBookingBusiness().observe(this, new Observer<DeleteBookingResponse>() {
            @Override
            public void onChanged(DeleteBookingResponse deleteBookingResponse) {
                viewModel.getWedding(weddingid, token);
                Toast.makeText(WeddingActivity.this, "Deleted Booking", Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void observeWedding(){
        viewModel.observeWedding().observe(this, new Observer<Wedding.GetWeddingResponse>() {
            @Override
            public void onChanged(Wedding.GetWeddingResponse getWeddingResponse) {
                if(getWeddingResponse.isSuccess()){
                    weddingname.setText(getWeddingResponse.getWedding().getGroomName() + " & " + getWeddingResponse.getWedding().getBrideName());
                    weddingdateshow.setText(getWeddingResponse.getWedding().getWeddingDate());

                    imgadapter = new ImageSliderAdapter(WeddingActivity.this, getWeddingResponse.getWedding().getAlbum(), getWeddingResponse.getWedding().getId());
                    sliderView.setLayoutManager(new LinearLayoutManager(WeddingActivity.this, LinearLayoutManager.HORIZONTAL, false));

                    sliderView.setAdapter(imgadapter);


                    if(getWeddingResponse.getWedding().getWeddingVenue() != null){
                        String uri = Constants.IMAGE_URL + getWeddingResponse.getWedding().getWeddingVenue().getImg();
                        Picasso.get().load(uri).into(venueimg);

                        deleteBookedVenue(getWeddingResponse.getWedding().getWeddingVenue().getId());


                        venueadd.setVisibility(View.GONE);
                        venueimg.setVisibility(View.VISIBLE);
                        venueoverlay.setVisibility(View.VISIBLE);
                        wedvenuename.setText(getWeddingResponse.getWedding().getWeddingVenue().getVenueName());


                        venueoverlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent venueintent = new Intent(WeddingActivity.this, VenueActivity.class);
                                venueintent.putExtra("venueid", getWeddingResponse.getWedding().getWeddingVenue().getId());
                                startActivity(venueintent);
                            }
                        });


                    }else{
                        venueadd.setVisibility(View.VISIBLE);
                        venueimg.setVisibility(View.GONE);
                        venueoverlay.setVisibility(View.GONE);
                    }

                    if(getWeddingResponse.getWedding().getWeddingPhotography() != null){
                        String uri = Constants.IMAGE_URL + getWeddingResponse.getWedding().getWeddingPhotography().getBusinessImage();
                        Picasso.get().load(uri).into(photoimg);

                        deleteBookedBusiness(getWeddingResponse.getWedding().getWeddingPhotography().getId());

                        photoadd.setVisibility(View.GONE);
                        photoimg.setVisibility(View.VISIBLE);
                        photooverlay.setVisibility(View.VISIBLE);
                        wedphotoname.setText(getWeddingResponse.getWedding().getWeddingPhotography().getBusinessname());

                        photooverlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent venueintent = new Intent(WeddingActivity.this, BusinessActivity.class);
                                venueintent.putExtra("businessid", getWeddingResponse.getWedding().getWeddingPhotography().getId());
                                startActivity(venueintent);
                            }
                        });


                    }
                    else{
                        photoadd.setVisibility(View.VISIBLE);
                        photoimg.setVisibility(View.GONE);
                        photooverlay.setVisibility(View.GONE);
                    }


                    if(getWeddingResponse.getWedding().getWeddingBakery() != null){
                        String uri = Constants.IMAGE_URL + getWeddingResponse.getWedding().getWeddingBakery().getBusinessImage();
                        Picasso.get().load(uri).into(bakeryimg);
                        deleteBookedBusiness(getWeddingResponse.getWedding().getWeddingBakery().getId());
                        bakeryadd.setVisibility(View.GONE);
                        bakeryimg.setVisibility(View.VISIBLE);
                        bakeryoverlay.setVisibility(View.VISIBLE);
                        wedbakeryname.setText(getWeddingResponse.getWedding().getWeddingBakery().getBusinessname());


                        bakeryoverlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent venueintent = new Intent(WeddingActivity.this, BusinessActivity.class);
                                venueintent.putExtra("businessid", getWeddingResponse.getWedding().getWeddingBakery().getId());
                                startActivity(venueintent);
                            }
                        });
                    }
                    else{
                        bakeryadd.setVisibility(View.VISIBLE);
                        bakeryimg.setVisibility(View.GONE);
                        bakeryoverlay.setVisibility(View.GONE);
                    }

                    if(getWeddingResponse.getWedding().getWeddingMakeup() != null){
                        String uri = Constants.IMAGE_URL + getWeddingResponse.getWedding().getWeddingMakeup().getBusinessImage();
                        Picasso.get().load(uri).into(beautyimg);
                        deleteBookedBusiness(getWeddingResponse.getWedding().getWeddingMakeup().getId());
                        beautyadd.setVisibility(View.GONE);
                        beautyimg.setVisibility(View.VISIBLE);
                        beautyoverlay.setVisibility(View.VISIBLE);
                        wedbeautyname.setText(getWeddingResponse.getWedding().getWeddingPhotography().getBusinessname());

                        beautyoverlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent venueintent = new Intent(WeddingActivity.this, BusinessActivity.class);
                                venueintent.putExtra("businessid", getWeddingResponse.getWedding().getWeddingMakeup().getId());
                                startActivity(venueintent);
                            }
                        });
                    }

                    else{
                        beautyadd.setVisibility(View.VISIBLE);
                        beautyimg.setVisibility(View.GONE);
                        beautyoverlay.setVisibility(View.GONE);
                    }




                }
            }
        });
    }


    private void deleteBookedVenue(String venueid){
        venuedelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.deleteBookingsVenue(weddingid,venueid, token);
            }
        });
    }

    private void deleteBookedBusiness(String businessid){
        photodelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.deleteBookingsBusiness(businessid, weddingid, token);
            }
        });


        bakerydelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.deleteBookingsBusiness(businessid, weddingid, token);

            }
        });

        makeupdelete.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                viewModel.deleteBookingsBusiness(businessid, weddingid, token);

                                            }
                                        }
        );



    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ){

            setLandscape(venueimg);
            setLandscape(venueoverlay);
            setLandscape(photoimg);
            setLandscape(photooverlay);
            setLandscape(bakeryimg);
            setLandscape(bakeryoverlay);
            setLandscape(beautyimg);
            setLandscape(beautyoverlay);


        }else{
            setPotrait(venueimg);
            setPotrait(venueoverlay);
            setPotrait(photoimg);
            setPotrait(photooverlay);
            setPotrait(bakeryimg);
            setPotrait(bakeryoverlay);
            setPotrait(beautyimg);
            setPotrait(beautyoverlay);
        }


    }

    public void observeDeleteWedding(){
        viewModel.observeDelete().observe(this, new Observer<DeleteResponse>() {
            @Override
            public void onChanged(DeleteResponse deleteResponse) {
                if(deleteResponse.isSuccess()){

                    finish();

                    startActivity(new Intent(WeddingActivity.this, DrawerActivity.class));
                }
            }
        });
    }

    private void setLandscape(View view){

        int height = view.getHeight();
        int width = view.getWidth();

        view.setLayoutParams(new RelativeLayout.LayoutParams(
                /*width*/ ViewGroup.LayoutParams.MATCH_PARENT,
                /*height*/ height+ 550

        ));
    }
    private void setPotrait(View view){
        int height = view.getHeight();
        int width = view.getWidth();
        view.setLayoutParams(new RelativeLayout.LayoutParams(
                /*width*/  ViewGroup.LayoutParams.MATCH_PARENT,
                /*height*/ height - 550

        ));
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){


            pickedImageContainer = albumdialog.findViewById(R.id.pickimg);

            try {
                // When an Image is picked
                if (requestCode == IMAGE_RESULT && resultCode == RESULT_OK
                        && null != data && null != data.getClipData()) {

                    ClipData mClipData = data.getClipData();

                    Toast.makeText(this, "You picked " +
                                    (mClipData.getItemCount() > 1 ? mClipData.getItemCount() + "Images" :
                                            mClipData.getItemCount() + "Image"),
                            Toast.LENGTH_LONG).show();

                    pickedImageContainer.removeAllViews();

                    int pickedImageCount;


                    for (pickedImageCount = 0; pickedImageCount < mClipData.getItemCount();
                         pickedImageCount++) {

                        ImageView productImageView =
                                new ImageView(this);



                        productImageView.setAdjustViewBounds(true);

                        productImageView.setScaleType(ImageView.ScaleType.FIT_XY);

                        LinearLayout.LayoutParams params = new LinearLayout
                                .LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);


                        params.leftMargin = 50;

                        productImageView.setLayoutParams(params);



                        pickedImageContainer.addView(productImageView);


                        //Getting Image Uri
                        Uri uri = mClipData.getItemAt(pickedImageCount).getUri();
                        Log.d(TAG, "onActivityResult: " + getImagePath(uri));
//                        imageUri.add(uri);

                        String filepath = getImagePath(uri);

                        //Getting the image bitmap from uri

                        mBitmap = BitmapFactory.decodeFile(filepath);

                        bitmapArrayList.add(mBitmap);


                        productImageView.setImageBitmap(mBitmap);
                    }
                } else {
                    Toast.makeText(this, "You haven't picked any Image",
                            Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: Something went wrong " + e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        }
    }






    public String getImagePath(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putParcelable("pic_uri", picUri);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        picUri = savedInstanceState.getParcelable("pic_uri");
    }


    private void showDialog() throws Resources.NotFoundException {

        new AlertDialog.Builder(this)
                .setTitle("Delete Wedding")
                .setMessage("Do you really want to delete this wedding?")
                .setIcon(R.drawable.ic_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                            viewModel.deleteWedding(token, weddingid);
                    }})
                .setNegativeButton(android.R.string.no, null).show();


    }




}
