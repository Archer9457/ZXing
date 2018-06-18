package com.xys.zxinglib;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xys.libzxing.zxing.activity.CaptureActivity;
import com.xys.libzxing.zxing.encoding.EncodingUtils;

public class MainActivity extends AppCompatActivity {

    private TextView resultTextView;
    private EditText qrStrEditText;
    private ImageView qrImgImageView;
    private CheckBox mCheckBox;
    private static final int CAMERA_PERMISSION_CODE = 1;
    private String password="";
    private String netWorkType="";
    private String netWorkName="";
    private WifiAdmin wifiAdmin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTextView = (TextView) this.findViewById(R.id.tv_scan_result);
        qrStrEditText = (EditText) this.findViewById(R.id.et_qr_string);
        qrImgImageView = (ImageView) this.findViewById(R.id.iv_qr_image);
        mCheckBox = (CheckBox) findViewById(R.id.logo);

        Button scanBarCodeButton = (Button) this.findViewById(R.id.btn_scan_barcode);
        scanBarCodeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //打开扫描界面扫描条形码或二维码
                Intent openCameraIntent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(openCameraIntent, 0);
            }
        });

        Button generateQRCodeButton = (Button) this.findViewById(R.id.btn_add_qrcode);
        generateQRCodeButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String contentString = qrStrEditText.getText().toString();
                if (!contentString.equals("")) {
                    //根据字符串生成二维码图片并显示在界面上，第二个参数为图片的大小（350*350）
                    Bitmap qrCodeBitmap = EncodingUtils.createQRCode(contentString, 350, 350,
                            mCheckBox.isChecked() ?
                                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher) :
                                    null);
                    qrImgImageView.setImageBitmap(qrCodeBitmap);
                } else {
                    Toast.makeText(MainActivity.this, "Text can not be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        checkPermission();
        wifiAdmin = new WifiAdmin(MainActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            resultTextView.setText(scanResult);
            netWorkConnect(scanResult); //获取扫描信息
        }
    }

    private void netWorkConnect(String strResult) {
        if (strResult.contains("P:") && strResult.contains("T:")) {// 自动连接wifi
            Log.e("扫描返回的结果----->", strResult);// 还是要判断
            String passwordTemp = strResult.substring(strResult
                    .indexOf("P:"));
            password = passwordTemp.substring(2,
                    passwordTemp.indexOf(";"));
            String netWorkTypeTemp = strResult.substring(strResult
                    .indexOf("T:"));
            netWorkType = netWorkTypeTemp.substring(2,
                    netWorkTypeTemp.indexOf(";"));
            String netWorkNameTemp = strResult.substring(strResult
                    .indexOf("S:"));
            netWorkName = netWorkNameTemp.substring(2,
                    netWorkNameTemp.indexOf(";"));

            if (!wifiAdmin.mWifiManager.isWifiEnabled()) {
                Toast.makeText(this, "开启wifi设置", Toast.LENGTH_LONG)
                        .show();
                wifiAdmin.openWifi();
            }
            Dialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("扫描到可用wifi")
                    .setMessage("wifi名：" + netWorkName)
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    // TODO Auto-generated method stub
                                }
                            })
                    .setPositiveButton("加入此wifi ",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    int net_type = 0x13;
                                    if (netWorkType
                                            .compareToIgnoreCase("wpa") == 0) {
                                        net_type = WifiAdmin.TYPE_WPA;// wpa
                                    } else if (netWorkType
                                            .compareToIgnoreCase("wep") == 0) {
                                        net_type = WifiAdmin.TYPE_WEP;// wep
                                    } else {
                                        net_type = WifiAdmin.TYPE_NO_PASSWD;// 无加密
                                    }
                                    wifiAdmin.addNetwork(netWorkName,
                                            password,
                                            net_type);
                                    Log.e("解析的数据----->",
                                            "networkname: "
                                                    + netWorkName + " "
                                                    + "password: "
                                                    + password
                                                    + " netWorkType: "
                                                    + net_type);
                                }
                            }).create();
            alertDialog.show();

        }
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

    }
}