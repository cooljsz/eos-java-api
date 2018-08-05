package com.jc.jcbd.eos;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        argsJson.put("quantity", amount.setScale(4, RoundingMode.HALF_DOWN).toString() + " " + sysmble);
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

    public String transfer(String from, String to, String memo) {
        EOSCreateAccount eosCreateAccount = new EOSCreateAccount();
        boolean isUnlock = eosCreateAccount.unlockWallet(Parameters.WALLET_NAME, Parameters.WALLET_PWD_KEY);
        if (isUnlock) {
            System.out.println("Wallet is unlocked:" + Parameters.WALLET_NAME);
        }

        EOSTransfer eosTransfer = new EOSTransfer();

        //创建账号
        String binargs = eosTransfer.sc_transfer(Parameters.accountName, from, to, new BigDecimal(0.0001), "JCB", memo);
        System.out.println("New account creator:" + Parameters.accountCreator + "   new account name:" + Parameters.accountName + "   account public key:" + Parameters.ACCOUNT_PUB_KEY);

        //获得链信息
        JSONObject chainInfoJson = eosCreateAccount.getChainInfo();
        String chainID = chainInfoJson.get("chain_id").toString();
        System.out.println("chainInfoJson:" + chainInfoJson);
        long head_block_num = Long.valueOf(chainInfoJson.get("head_block_num").toString());
        System.out.println("head_block_num:" + head_block_num);

        //获得最新的块信息
        JSONObject blockInfoJson = eosCreateAccount.getBlock(head_block_num);
        System.out.println("block info:" + blockInfoJson);
        //生成块的时间往后延迟1分钟
        String timestamp = blockInfoJson.get("timestamp").toString();
        System.out.println("timestamp:" + timestamp);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Date date;
        try {
            date = df.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("时间转换异常,目标格式:yyyy-MM-dd'T'HH:mm:ss.SSS 传入时间:" + timestamp);
            return null;
        }
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(date);
        rightNow.add(Calendar.MINUTE, 1);//时间加1分钟
        Date dt1 = rightNow.getTime();
        String expiration = df.format(dt1);
        //获得相关块信息
        long ref_block_prefix = Long.valueOf(blockInfoJson.get("ref_block_prefix").toString());

        //解锁账户,账户一定时间不操作会自动锁定
        eosCreateAccount.unlockWallet(Parameters.WALLET_NAME, Parameters.WALLET_PWD_KEY);

        //查看签名所需使用的PublicKey
        JSONArray requiredKeys = eosCreateAccount.getRequiredKeys(head_block_num, ref_block_prefix, expiration, Parameters.accountName, "transfer", Parameters.accountName, binargs, new String[]{Parameters.WALLET_PUB_KEY, Parameters.ACCOUNT_PUB_KEY, Parameters.EOSIO_PUB_KEY});
        String pubKey;
        if (requiredKeys != null && requiredKeys.length() > 0) {
            pubKey = requiredKeys.get(0).toString();
        } else {
            pubKey = Parameters.ACCOUNT_PUB_KEY;
        }

        //对交易签名
        JSONObject signaturesJson = eosCreateAccount.sc_signTransaction(head_block_num, ref_block_prefix, expiration, Parameters.accountName, "transfer", Parameters.accountName, binargs, pubKey, chainID);
        String signatures = signaturesJson.get("signatures").toString();
        signatures = signatures.substring(2, signatures.length() - 2);
//        long signBlockNum = Long.valueOf(signaturesJson.get("ref_block_num").toString());
        //推送事务
        JSONObject transactionJson = eosCreateAccount.sc_pushTransaction(head_block_num, ref_block_prefix, expiration, Parameters.accountName, "transfer", Parameters.accountName, binargs, signatures);
        String transaction_id = transactionJson.get("transaction_id").toString();
        System.out.println(transactionJson);
        System.out.println(transaction_id);

        return transaction_id;
    }

    //根据transaction_id获取blockid
    public String getMemo(String transactionID) {
        String url = "http://" + IP + ":8888/v1/history/get_transaction";
        HttpPost httpPost = new HttpPost(url);
        JSONObject transactionIDJson = new JSONObject();
        transactionIDJson.put("id", transactionID);
        httpPost.setEntity(new StringEntity(transactionIDJson.toString(), Charset.forName("UTF-8")));
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        JSONObject transactionJson = new JSONObject();
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("getBlockID Method failed:" + httpResponse.getStatusLine());
            } else {
                String transactionInfo = EntityUtils.toString(httpResponse.getEntity());
                transactionJson = new JSONObject(transactionInfo);
                System.out.println("Transaction Info Json:" + transactionJson);
                JSONObject trxJson1 = (JSONObject) transactionJson.get("trx");
                JSONObject trxJson2 = (JSONObject) trxJson1.get("trx");
                JSONArray actionsArray = (JSONArray) trxJson2.get("actions");
                JSONObject dataJson = (JSONObject) actionsArray.getJSONObject(0).get("data");
                String memo = dataJson.get("memo").toString();
                return memo;
            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return null;
    }
}
