package com.jc.jcbd.eos;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;

public class EOSTransfer {

    final static String IP = "192.168.12.132";
    //创建账号
    public String sc_transfer(String contractName, String from, String to, BigDecimal amount, String sysmble, String memo) {
        System.out.println("Transfer contractName:" + contractName);
        System.out.println("Transfer Account from:" + from);
        System.out.println("Transfer Account to:" + to);
        System.out.println("Transfer Account amount:" + amount);
        System.out.println("Transfer Account sysmble:" + sysmble);
        System.out.println("Transfer Account memo:" + memo);

        String url = "http://" + IP + ":8888/v1/chain/abi_json_to_bin";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");

        JSONObject argsJson = new JSONObject();
        argsJson.put("from", from);
        argsJson.put("to", to);
        argsJson.put("quantity", amount.setScale(4, RoundingMode.HALF_DOWN).toString()+" "+sysmble);
        argsJson.put("memo", memo);

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("code", contractName);
        jsonParam.put("action", "transfer");
        jsonParam.put("args", argsJson);

        String jsonStr = jsonParam.toString();
        httpPost.setEntity(new StringEntity(jsonStr, Charset.forName("UTF-8")));
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("transfer Method failed:" + httpResponse.getStatusLine());
            } else {
                String binargs = EntityUtils.toString(httpResponse.getEntity());
                JSONObject resultJson = new JSONObject(binargs);
                binargs = resultJson.get("binargs").toString();
                System.out.println("transfer binargs:" + binargs);
                return binargs;
            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return null;
    }
}
