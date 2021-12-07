package com.example.test6;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class WritingsRequest extends StringRequest  {

    final static private String URL = "http://222.104.194.26/Write.php";
    private Map<String, String> parameters;

    public WritingsRequest(String name, String pw, String title, String content, Response.Listener<String> listener) {
        super(Method.POST,URL,listener,null);
        parameters = new HashMap<>();
        parameters.put("name",name);
        parameters.put("pw",pw);
        parameters.put("title",title);
        parameters.put("content",content);
    }

    @Override
    public Map<String,String> getParams() throws AuthFailureError {
        return parameters;
    }

}
