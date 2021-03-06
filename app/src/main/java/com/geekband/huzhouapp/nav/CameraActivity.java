package com.geekband.huzhouapp.nav;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.geekband.huzhouapp.R;
import com.geekband.huzhouapp.baseadapter.CommonAdapter;
import com.geekband.huzhouapp.baseadapter.ViewHolder;
import com.geekband.huzhouapp.utils.BitmapHelper;
import com.geekband.huzhouapp.utils.FileUtil;
import com.geekband.huzhouapp.utils.FileUtils;
import com.lidroid.xutils.BitmapUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/5/16
 * 相机功能
 */
public class CameraActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ImageButton mPhotograph_imageBtn;
    private ImageButton mVideotape_imageBtn;
    private TextView mCamera_back_textBtn;
    private ArrayList<String> mImagesList;
    private BitmapUtils mBitmapUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mPhotograph_imageBtn = (ImageButton) findViewById(R.id.photograph_imageBtn);
        mVideotape_imageBtn = (ImageButton) findViewById(R.id.videotape_imageBtn);
        mCamera_back_textBtn = (TextView) findViewById(R.id.camera_back_textBtn);

        mPhotograph_imageBtn.setOnClickListener(this);
        mVideotape_imageBtn.setOnClickListener(this);
        mCamera_back_textBtn.setOnClickListener(this);

        mImagesList = FileUtil.getLocalImagePath(this);
        GridView gridView_camera = (GridView) findViewById(R.id.gridView_camera);
        mBitmapUtils = BitmapHelper.getBitmapUtils(this, gridView_camera, 0, 0);
        gridView_camera.setAdapter(new CommonAdapter<String>(this,mImagesList,R.layout.item_camera_list) {
            @Override
            public void convert(ViewHolder viewHolder, String item) {
                mBitmapUtils.display(viewHolder.getView(R.id.imageView_camera),item);
            }
        });
        gridView_camera.setOnItemClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photograph_imageBtn:
                getPhoto(v);
                break;
            case R.id.videotape_imageBtn:
                getVideo(v);
                break;
            case R.id.camera_back_textBtn:
                this.finish();
        }

    }

    public void getPhoto(View view) {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        //下面这句指定调用相机拍照后的照片存储的路径
        String fileName = FileUtils.getCurrentTimeStr() + ".jpg";
        String filePath = Environment.getExternalStorageDirectory() + "/" + "privateCamera" + "/" + "image" + "/";
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File fullFile = new File(file, fileName);
        Uri uri = Uri.fromFile(fullFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        this.startActivity(intent);
    }

    public void getVideo(View view) {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
        String fileName = FileUtils.getCurrentTimeStr() + ".3gp";
        File file = new File(Environment.getExternalStorageDirectory() + "/" + "privateCamera" + "/" + "video" + "/");
        if (!file.exists()) {
            file.mkdirs();
        }
        File fullFile = new File(file, fileName);
        Uri uri = Uri.fromFile(fullFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        this.startActivity(intent);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String tempImage = mImagesList.get(position);
        mImagesList.remove(position);
        mImagesList.add(0, tempImage);
        Intent intent = new Intent(this, AlbumActivity.class);
        intent.setAction("localImages");
        intent.putStringArrayListExtra("localImages", mImagesList);
        startActivity(intent);
    }
}
