package net.londatiga.fsq;

import android.app.Dialog;
import android.app.ProgressDialog;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import android.os.Bundle;
import android.util.Log;
import android.content.Context;

import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;

import android.webkit.CookieSyncManager;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Display Foursquare authentication dialog.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 *
 */
public class FoursquareDialog extends Dialog {
	static final float[] DIMENSIONS_LANDSCAPE = {460, 260};
    static final float[] DIMENSIONS_PORTRAIT = {280, 420};
    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                         						ViewGroup.LayoutParams.FILL_PARENT);
    static final int MARGIN = 4;
    static final int PADDING = 2;

    private String mUrl;
    private FsqDialogListener mListener;
    private ProgressDialog mSpinner;
    private WebView mWebView;
    private LinearLayout mContent;
    private TextView mTitle;
 
    private static final String TAG = "Foursquare-WebView";
    
	public FoursquareDialog(Context context, String url, FsqDialogListener listener) {
		super(context);
		
		mUrl		= url;
		mListener	= listener;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSpinner = new ProgressDialog(getContext());
        
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");

        mContent = new LinearLayout(getContext());
        
        mContent.setOrientation(LinearLayout.VERTICAL);
        
        setUpTitle();
        setUpWebView();
        
        Display display 	= getWindow().getWindowManager().getDefaultDisplay();
        final float scale 	= getContext().getResources().getDisplayMetrics().density;
        float[] dimensions 	= (display.getWidth() < display.getHeight()) ? DIMENSIONS_PORTRAIT : DIMENSIONS_LANDSCAPE;
        
        addContentView(mContent, new FrameLayout.LayoutParams((int) (dimensions[0] * scale + 0.5f),
        							(int) (dimensions[1] * scale + 0.5f)));
        
        CookieSyncManager.createInstance(getContext()); 
    	
    	CookieManager cookieManager = CookieManager.getInstance();
    	
    	cookieManager.removeAllCookie();
    }
	
	 private void setUpTitle() {
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        
	        Drawable icon = getContext().getResources().getDrawable(R.drawable.foursquare_icon);
	        
	        mTitle = new TextView(getContext());
	        
	        mTitle.setText("Foursquare");
	        mTitle.setTextColor(Color.WHITE);
	        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
	        mTitle.setBackgroundColor(0xFF0cbadf);
	        mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
	        mTitle.setCompoundDrawablePadding(MARGIN + PADDING);
	        mTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
	        
	        mContent.addView(mTitle);
	    }

	    private void setUpWebView() {
	        mWebView = new WebView(getContext());
	        
	        mWebView.setVerticalScrollBarEnabled(false);
	        mWebView.setHorizontalScrollBarEnabled(false);
	        mWebView.setWebViewClient(new TwitterWebViewClient());
	        mWebView.getSettings().setJavaScriptEnabled(true);
	        mWebView.loadUrl(mUrl);
	        mWebView.setLayoutParams(FILL);
	        
	        mContent.addView(mWebView);
	    }

	    private class TwitterWebViewClient extends WebViewClient {

	        @Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        	Log.d(TAG, "Redirecting URL " + url);
	        	
	        	if (url.startsWith(FoursquareApp.CALLBACK_URL)) {
	        		String urls[] = url.split("=");
	        		
	        		mListener.onComplete(urls[1]);
	        		
	        		FoursquareDialog.this.dismiss();
	        		
	        		return true;	        		
	        	} 
	    
	        	return false;
	        }

	        @Override
	        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
	        	Log.d(TAG, "Page error: " + description);
	        	
	            super.onReceivedError(view, errorCode, description, failingUrl);
	      
	            mListener.onError(description);
	            
	            FoursquareDialog.this.dismiss();
	        }

	        @Override
	        public void onPageStarted(WebView view, String url, Bitmap favicon) {
	            Log.d(TAG, "Loading URL: " + url);
	            super.onPageStarted(view, url, favicon);
	            mSpinner.show();
	        }

	        @Override
	        public void onPageFinished(WebView view, String url) {
	            super.onPageFinished(view, url);
	            String title = mWebView.getTitle();
	            if (title != null && title.length() > 0) {
	                mTitle.setText(title);
	            }
	            mSpinner.dismiss();
	        }

	    }
	    
	public interface FsqDialogListener {
		public abstract void onComplete(String accessToken);
		public abstract void onError(String error);
	}
}