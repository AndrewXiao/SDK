package ble.shecare.com.bledemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChooseDeviceActivity extends AppCompatActivity {

    @BindView(R.id.activity_choose_device)
    RelativeLayout activityChooseDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_device);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_device2, R.id.btn_device3})
    public void onClick(View view) {



        switch (view.getId()) {
            case R.id.btn_device2:
                Intent intent2 = new Intent(this, MainActivity2.class);
                startActivity(intent2);
                break;
            case R.id.btn_device3:
                Intent intent3 = new Intent(this, MainActivity3.class);
                startActivity(intent3);
                break;
        }


    }
}
