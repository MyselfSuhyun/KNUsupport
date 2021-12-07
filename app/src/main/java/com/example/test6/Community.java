package com.example.test6;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class Community extends AppCompatActivity {


    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);
        webView = findViewById(R.id.webView);
        /* 웹 세팅 작업하기 */
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        /* 리다이렉트 할 때 브라우저 열리는 것 방지 */
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        /* 웹 뷰 띄우기 */
        /*webView.loadUrl("http://knu.ac.kr/wbbs/wbbs/bbs/btin/list.action?btin.page=1&bbs_cde=1&btin.bbs_cde=1&popupDeco=false&menu_idx=67&input_search_type=search_subject&input_search_text=%EA%B3%B5%EC%82%AC&btin.search_type=search_content&btin.search_text=%EA%B3%B5%EC%82%AC");*/
        webView.loadUrl("http://222.104.194.26/index.php");
    }

    public void onBackPressed() {
        if(webView.canGoBack()) webView.goBack();
        else finish();
    }

}
