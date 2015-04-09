package martin.app.bitunion.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import martin.app.bitunion.BUApplication;
import martin.app.bitunion.R;

public class HtmlImageGetter implements Html.ImageGetter {

    private Context mContext;
    private TextView mContainer;

    public HtmlImageGetter(Context c, TextView view) {
        mContext = c;
        mContainer = view;
    }

    @Override
    public Drawable getDrawable(String imgUrl) {
        imgUrl = BUApiHelper.getImageAbsoluteUrl(imgUrl);
        UrlImageDownloader urlDrawable = new UrlImageDownloader(mContext.getResources(), imgUrl);
        urlDrawable.drawable = mContext.getResources().getDrawable(R.drawable.ic_image_white_48dp);
        VolleyImageLoaderFactory.getImageLoader(mContext).get(imgUrl, new VolleyImageListener(mContainer, urlDrawable));
        return urlDrawable;
    }

    private static class VolleyImageListener implements ImageLoader.ImageListener {
        private UrlImageDownloader urlImageDownloader;
        private TextView container;

        private VolleyImageListener(TextView textView, UrlImageDownloader drawable) {
            urlImageDownloader = drawable;
            container = textView;
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            if (response == null || response.getBitmap() == null)
                return;
            Bitmap loadedImage = response.getBitmap();
            int width = loadedImage.getWidth();
            int height = loadedImage.getHeight();

            int newWidth = width;
            int newHeight = height;

            if (width > container.getWidth()) {
                newWidth = container.getWidth();
                newHeight = (newWidth * height) / width;
            }

            Drawable result = new BitmapDrawable(container.getResources(), loadedImage);
            result.setBounds(0, 0, newWidth, newHeight);

            urlImageDownloader.setBounds(0, 0, newWidth, newHeight);
            urlImageDownloader.drawable = result;

            container.invalidate();
        }

        @Override
        public void onErrorResponse(VolleyError error) {

        }
    }

    private static class UrlImageDownloader extends BitmapDrawable {
        public Drawable drawable;

        /**
         * Create a drawable by decoding a bitmap from the given input stream.
         *
         * @param res
         * @param is
         */
        public UrlImageDownloader(Resources res, InputStream is) {
            super(res, is);
        }

        /**
         * Create a drawable by opening a given file path and decoding the bitmap.
         *
         * @param res
         * @param filepath
         */
        public UrlImageDownloader(Resources res, String filepath) {
            super(res, filepath);
            drawable = new BitmapDrawable(res, filepath);
        }

        /**
         * Create drawable from a bitmap, setting initial target density based on
         * the display metrics of the resources.
         *
         * @param res
         * @param bitmap
         */
        public UrlImageDownloader(Resources res, Bitmap bitmap) {
            super(res, bitmap);
        }

        @Override
        public void draw(Canvas canvas) {
            // override the draw to facilitate refresh function later
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }
    }
}
