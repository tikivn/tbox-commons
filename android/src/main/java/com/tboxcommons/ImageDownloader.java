package com.tboxcommons;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
  private ImageDownloaderCallback delegate;

  protected ImageDownloader(ImageDownloaderCallback delegate) {
    this.delegate = delegate;
  }

  @Override
  protected Bitmap doInBackground(String... params) {
    return getBitmapFromURL(params[0]);
  }

  private Bitmap getBitmapFromURL(String src) {
    try {
      URL url = new URL(src);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.connect();
      InputStream input = connection.getInputStream();
      Bitmap myBitmap = BitmapFactory.decodeStream(input);
      return myBitmap;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  //operation is finished, update the UI with bitmap
  @Override
  protected void onPostExecute(Bitmap bitmap) {
    if (this.delegate != null) {
      this.delegate.downloadCompleted((bitmap));
    }
  }
}
