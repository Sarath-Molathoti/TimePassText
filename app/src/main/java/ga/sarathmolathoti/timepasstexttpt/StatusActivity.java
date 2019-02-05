package ga.sarathmolathoti.timepasstexttpt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mStatus;
    private Button mSaveBtn;
    //database
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //progress
    private ProgressDialog mSavingStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Retrieving uid of current user
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //Toolbar
        mToolbar = (Toolbar) findViewById(R.id.status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Update Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //taking status from settings activity
        String status_value = getIntent().getStringExtra("string_value");

        mStatus = (EditText) findViewById(R.id.status_change_edt);
        mSaveBtn = (Button) findViewById(R.id.status_save_btn);

        mStatus.setText(status_value);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //progress
                mSavingStatus = new ProgressDialog(StatusActivity.this);
                mSavingStatus.setTitle("Saving Changes");
                mSavingStatus.setMessage("Please wait a second");
                mSavingStatus.show();

                String status = mStatus.getText().toString();

                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            mSavingStatus.dismiss();


                        }
                        else {
                            Toast.makeText(getApplicationContext(), "can't save", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }
}
