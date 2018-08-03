//package com.jc.jcbd.eos;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//import org.json.JSONObject;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//
//public class EOSCreateCurrency {
//
//    public String sc_newaccoount(String creator, long maximumSupply,String currencyName, long arg1,long arg2,long arg3) {
//        String url = "http://" + IP + ":8888/v1/chain/abi_json_to_bin";
//        HttpPost httpPost = new HttpPost(url);
//        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
//
//        JSONObject ownerKeysJson = new JSONObject();
//        ownerKeysJson.put("key", publicKey);
//        ownerKeysJson.put("weight", 1);
//        JSONObject ownerJson = new JSONObject();
//        ownerJson.put("threshold", 1);
//        ownerJson.put("keys", new JSONObject[]{ownerKeysJson});
//        String[] nullArray = new String[0];
//        ownerJson.put("accounts", nullArray);
//        ownerJson.put("waits", nullArray);
//
//        JSONObject activeKeysJson = new JSONObject();
//        activeKeysJson.put("key", publicKey);
//        activeKeysJson.put("weight", 1);
//        JSONObject activeJson = new JSONObject();
//        activeJson.put("threshold", 1);
//        activeJson.put("keys", new JSONObject[]{activeKeysJson});
//        activeJson.put("accounts", nullArray);
//        activeJson.put("waits", nullArray);
//
//        JSONObject argsJson = new JSONObject();
//        argsJson.put("issuer", creator);
//        argsJson.put("maximum_supply", maximumSupply);
//
//        JSONObject jsonParam = new JSONObject();
//        jsonParam.put("code", "eosio.token");
//        jsonParam.put("action", "create");
//        jsonParam.put("args", argsJson);
//
//        String jsonStr = jsonParam.toString();
//        httpPost.setEntity(new StringEntity(jsonStr, Charset.forName("UTF-8")));
//        HttpClient httpClient = HttpClients.createDefault();
//        HttpResponse httpResponse = null;
//        try {
//            httpResponse = httpClient.execute(httpPost);
//            int statusCode = httpResponse.getStatusLine().getStatusCode();
//            if (statusCode != HttpStatus.SC_OK) {
//                System.err.println("Create Account Method failed:" + httpResponse.getStatusLine());
//            } else {
//                String binargs = EntityUtils.toString(httpResponse.getEntity());
//                JSONObject resultJson = new JSONObject(binargs);
//                binargs = resultJson.get("binargs").toString();
//                System.out.println("binargs:" + binargs);
//                return binargs;
//            }
//        } catch (ClientProtocolException e) {
//            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
//        } catch (IOException e) {
//            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
//        }
//        return null;
//        return null;
//    }
//}
