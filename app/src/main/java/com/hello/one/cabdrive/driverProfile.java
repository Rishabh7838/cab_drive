package com.hello.one.cabdrive;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class driverProfile extends AppCompatActivity {
    private TextInputEditText mNameField,mNumberField,mCarField;
    private Button mSubmit,mBack;
    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;
    private Uri resultUri;
    private String userId,mName,mNumber,mProfileImageUrl="",mCar,mService,imageId="";
    private ImageView mProf;
    private ProgressDialog mProgressBar;
    private RadioGroup mRadioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);
        mProgressBar = new ProgressDialog(this);
        mNameField = (TextInputEditText) findViewById(R.id.driveName);
        mNumberField = (TextInputEditText) findViewById(R.id.driveNumber);
        mCarField = (TextInputEditText) findViewById(R.id.driveCar);
        mProf = (ImageView) findViewById(R.id.driveprof);
        mSubmit = (Button) findViewById(R.id.drivesub);
       // mBack = (Button) findViewById(R.id.driveback);
        mRadioGroup= (RadioGroup) findViewById(R.id.radioGroup);
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);

        getUserInfo();
        mProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new  Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(resultUri!=null){
                    imageId = resultUri.toString().trim();
                }
                else{
                    imageId = mProfileImageUrl.trim();
                }

                if (mNameField.getText().toString().trim().length()>0 && numberValidator(mNumberField.getText().toString().trim()) && imageId.length()>0 && mCarField.getText().toString().trim().length()>0 && mRadioGroup.getCheckedRadioButtonId()!=-1)
                {
                 //   finish();
                    saveUserInformation();

                }
                else {
                    if(!numberValidator(mNumberField.getText().toString().trim())){
                        mNumberField.setError("Write A Valid Number");
                    }
                    Toast.makeText(driverProfile.this, "Fill All the fields", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private Boolean numberValidator(String email){
        Pattern pattern;
        Matcher matcher;
        final String Number_Pattern = "^(?:(?:\\+|0{0,2})91(\\s*[\\-]\\s*)?|[0]?)?[789]\\d{9}$";
        pattern = Pattern.compile(Number_Pattern);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
    private void getUserInfo()
    {
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() >0)
                {
                    Map<String,Object> map = (Map<String,Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mName = map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if(map.get("number")!=null){
                        mNumber = map.get("number").toString();
                        mNumberField.setText(mNumber);
                    }
                    if(map.get("car")!=null){
                        mCar = map.get("car").toString();
                        mCarField.setText(mCar);
                    }
                    if(map.get("Services")!=null){
                        mService = map.get("Services").toString();
                        switch (mService)
                        {
                            case "DP_X" :
                                mRadioGroup.check(R.id.DPX);
                                break;
                            case "DP_Black" :
                                mRadioGroup.check(R.id.DPBlack);
                                break;
                            case "DP_XL" :
                                mRadioGroup.check(R.id.DPXL);
                                break;
                        }
                    }
                    if(map.get("ProfileImageUrl")!=null){
                        mProfileImageUrl = map.get("ProfileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(mProf);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void saveUserInformation() {
        mName = mNameField.getText().toString();
        mNumber = mNumberField.getText().toString();
        mCar = mCarField.getText().toString();

        int selectId = mRadioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = (RadioButton) findViewById(selectId);
        if(radioButton.getText()== null){
            return;
        }

        mService= radioButton.getText().toString();
        Map userInfo = new HashMap();
        userInfo.put("name",mName);
        userInfo.put("number",mNumber);
        userInfo.put("car",mCar);
        userInfo.put("Services",mService);
        mDriverDatabase.updateChildren(userInfo);
        if(resultUri!=null)
        {
            mProgressBar.setMessage("Submiting data...");
            mProgressBar.show();
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userId);
            Bitmap bitmap = null;
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(driverProfile.this,"Profile failure",Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();

                    Map imageMap = new HashMap();
                    imageMap.put("ProfileImageUrl",downloadUri.toString());
                    mDriverDatabase.updateChildren(imageMap);
                 //   finish();
                    Intent submit = new Intent(driverProfile.this, DriverMapsActivity.class);
                    startActivity(submit);
                    finish();
                    finish();
                    return;
                }
            });


        }else {
            finish();
        }
        Toast.makeText(driverProfile.this,"Data submited",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode == Activity.RESULT_OK)
        {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProf.setImageURI(resultUri);
           // finish();
        }
    }

}
