package com.example.test6;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class LoginRequest extends StringRequest {

    final static private String URL = "http://222.104.194.26/Login.php";
    private Map<String, String> parameters;

    public LoginRequest(String userID, Response.Listener<String> listener) {
        super(Method.POST,URL,listener,null);
        parameters = new HashMap<>();
        parameters.put("userID",userID);
    }

    @Override
    public Map<String,String> getParams() {
        return parameters;
    }

}
