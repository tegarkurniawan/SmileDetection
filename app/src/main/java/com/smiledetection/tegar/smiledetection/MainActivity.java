package com.smiledetection.tegar.smiledetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    //variable
    private static final int RQS_LOADIMAGE = 1;
    private Button btnLoadGambar, btnDeteksiWajah;
    private ImageView imgView;
    private Bitmap myBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //inisialisasi view
        btnLoadGambar = (Button) findViewById(R.id.btnLoadGambar);
        btnDeteksiWajah = (Button) findViewById(R.id.btnDeteksiWajah);
        imgView = (ImageView) findViewById(R.id.img_view);

        //perintah untuk mengambil gambar
        btnLoadGambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                //melanjutkan ke intent lain dgn proses bitmap
                startActivityForResult(intent, RQS_LOADIMAGE);
            }
        });

        //perintah button deteksi wajah
        btnDeteksiWajah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBitmap == null) {
                    Toast.makeText(MainActivity.this,
                            "Anda belum memilih gambar yang akan dideteksi",
                            Toast.LENGTH_LONG).show();
                } else {
                    deteksiWajah();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQS_LOADIMAGE
                && resultCode == RESULT_OK) {

            if (myBitmap != null) {
                myBitmap.recycle();
            }

            try {
                InputStream inputStream =
                        getContentResolver().openInputStream(data.getData());
                myBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                imgView.setImageBitmap(myBitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //method untuk deteksi wajah
    private void deteksiWajah() {

        //Mendeteksi wajah
        FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext())
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS).build();

        //Deteksi apakah Play Service terdapat Mobile Vision API
        if (!faceDetector.isOperational()) {
            Toast.makeText(MainActivity.this,
                    "Mobile Vision API tidak tersedia!",
                    Toast.LENGTH_LONG).show();

        } else {
            float ID_TEXT_SIZE = (float) myBitmap.getWidth() / 15;

            //Membuat gambar kotak untuk wajah yang terdeteksi
            Paint myRectPaint = new Paint();
            //tebal garis
            myRectPaint.setStrokeWidth(5);
            //warna garis
            myRectPaint.setColor(Color.RED);
            //style paint hanya sebuah garis
            myRectPaint.setStyle(Paint.Style.STROKE);

            //Membuat paint untuk nilai score
            Paint mIdPaint = new Paint();
            mIdPaint.setColor(Color.BLUE);
            mIdPaint.setTextSize(ID_TEXT_SIZE);
            //Membuat canvas untuk meletakan gambar dan kotak yang kita buat
            Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas tempCanvas = new Canvas(tempBitmap);
            tempCanvas.drawBitmap(myBitmap, 0, 0, null);

            Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
            SparseArray<Face> faces = faceDetector.detect(frame);

            //Mendeteksi apakah ada wajah atau tidak
            if (faces.size() == 0) {
                Toast.makeText(MainActivity.this,
                        "Tidak ada wajah yang terdeteksi!",
                        Toast.LENGTH_LONG).show();
            } else {
                float ID_Y_OFFSET = 50.0f;

                //Menggambar kotak pada wajah
                for (int i = 0; i < faces.size(); i++) {
                    Face thisFace = faces.valueAt(i);
                    float x1 = thisFace.getPosition().x;
                    float y1 = thisFace.getPosition().y;
                    float x2 = x1 + thisFace.getWidth();
                    float y2 = y1 + thisFace.getHeight();
                    tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);

                    //mengambil nilai probabilitas senyum
                    String score = String.format("%.2f", thisFace.getIsSmilingProbability() * 100);

                    //menambahkan pada canvas
                    tempCanvas.drawText(score, x1, y2 + ID_Y_OFFSET, mIdPaint);
                }

                //menampilkan hasil wajah yang sudah terdeteksi dengan kotak
                imgView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

                //menampilkan informasi selesai deteksi wajah
                Toast.makeText(MainActivity.this, "Deteksi Wajah Berhasil", Toast.LENGTH_LONG).show();
            }
        }

    }

}