package com.example.test6;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {

    final static private String URL = "http://222.104.194.26/Register.php";
    private Map<String, String> parameters;

    public RegisterRequest(String userID, String userPassword, String userNAME, String userGender, String userStudent, String userPhone, Response.Listener<String> listener) {
        super(Method.POST,URL,listener,null);
        parameters = new HashMap<>();
        parameters.put("userID",userID);
        parameters.put("userPassword",userPassword);
        parameters.put("userNAME",userNAME);
        parameters.put("userGender",userGender);
        parameters.put("userStudent",userStudent);
        parameters.put("userPhone",userPhone);
    }

    @Override
    public Map<String,String> getParams() throws AuthFailureError {
        return parameters;
    }

}
