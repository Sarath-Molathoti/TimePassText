package ga.sarathmolathoti.timepasstexttpt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import static io.opencensus.tags.TagValue.MAX_LENGTH;

public class SettingsActivity extends AppCompatActivity {

    //Widgets Instance variables.
    private TextView mName;
    private TextView mStatus;
    private CircleImageView mDisplayImage;
    private Button mStatusBtn;
    private Button mImageBtn;

    //Firebase Database Instance.
    private DatabaseReference mUserDatabase;

    //Firebase User Instance.
    private FirebaseUser mCurrentUser;

    //storage firebase
    private StorageReference mImageStorage;

    private static final int GALLERY_PICK = 1;

    //when the user starts to upload a profile pic
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Finding Widgets with their Unique Id's
        mName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
        mStatusBtn = (Button) findViewById(R.id.settings_status_btn);
        mImageBtn = (Button) findViewById(R.id.settings_image_btn);

        mImageStorage = FirebaseStorage.getInstance().getReference();//pointing to the root of the storage

        //Setting up an OnClickListener on Change Status Button.
        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting the status from the realtime database and storing it in a string.
                String status_value = mStatus.getText().toString();

                //Making an Intent to Move the user to SettingsActivity --> StatusActivity.
                Intent statusIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("string_value", status_value);
                startActivity(statusIntent);
            }
        });

        //Getting The Instance of Current User.
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //Getting The UID of the Current User.
        String current_uid = mCurrentUser.getUid();

        //Populating the RealTimeDatabase with USERS --> UID.
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //Setting Up An ValueEventListener.
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Getting The Values(Child of USERS) and storing them in a String.
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                //Setting up in the UI.
                mName.setText(name);
                mStatus.setText(status);

                Picasso.get().load(image).into(mDisplayImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Setting up an OnClickListener on an ImageButton.
        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Using an Intent to get all the images from the gallery.
                Intent galleryIntent = new Intent();

                //Setting up the path of the image source.
                galleryIntent.setType("image/*");

                //Getting the content(images) from the above path.
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                //Returning the Activity with a result(selected Image).
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"), GALLERY_PICK);

                /*CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);*/
            }
        });
    }

    //Overriding a Method to get the result.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Checking if the selected image is true or not and the result is Ok or not.
        if (requestCode==GALLERY_PICK && resultCode==RESULT_OK){

            //Getting the Data of the Image and saving it in a Uri.
            Uri imageUri = data.getData();//uri of the image

            //Instantiating the cropImage feature and setting the ratio in 1:1.
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        //Checking if the image is cropped or not.
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            //Checking if the result is Ok or not, if yes we will store the image in a uri.
            if (resultCode == RESULT_OK) {
                //when the user starts uploading profile pic
                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("please wait while we upload and process the image.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();//uri of the cropped image

                //getting userid of the user to use it as profile pic name
                final String current_user_id = mCurrentUser.getUid();

                //Saving the image in the Firebase Storage and naming the child with the UID.
                StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");

                //We Will setup an OnCompleteListener to store the image in the desired location in the storage.
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            //String download_url = mImageStorage.getDownloadUrl().getResult().toString();
                            //String download_url = task.getResult().getUploadSessionUri().toString();

                            mImageStorage.child("profile_images").child(current_user_id + ".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String downloadUrl = uri.toString();

                                    mUserDatabase.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                mProgressDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this, "Successfully Uploaded.", Toast.LENGTH_SHORT).show();

                                            }

                                        }
                                    });

                                }
                            });
                        }

                        else {
                            Toast.makeText(SettingsActivity.this, "Error in Uploading profile pic", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

}
