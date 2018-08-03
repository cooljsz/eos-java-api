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
import java.nio.charset.Charset;
import java.util.Arrays;


public class EOSCreateAccount {

    final static String IP = "192.168.12.132";
    final static String account1 = "user1";
    final static String account2 = "user2";

    //创建钱包
    public String createWallet(String walletName) {
        System.out.println("createWalletName:" + walletName);
        String url = "http://" + IP + ":8888/v1/wallet/create";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "text/plain; charset=UTF-8");

        httpPost.setEntity(new StringEntity("\"" + walletName + "\"", Charset.forName("UTF-8")));
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                System.err.println("Create Wallet Method failed:" + httpResponse.getStatusLine());
            } else {
                String walletPrivteKey = EntityUtils.toString(httpResponse.getEntity());
                System.out.println("walletPrivateKey:" + walletPrivteKey);
                walletPrivteKey = walletPrivteKey.replace("\"", "");
                return walletPrivteKey;
            }


        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return null;
    }

    //解锁钱包
    public boolean unlockWallet(String walletName, String password) {
        System.out.println("Unlock Wallet Name:" + walletName);
        System.out.println("Unlock Password:" + password);
        String url = "http://" + IP + ":8888/v1/wallet/unlock";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "text/plain; charset=UTF-8");
//        httpPost.setHeader("Accept", "text/plain");
        httpPost.setEntity(new StringEntity("[\"" + walletName + "\",\"" + password + "\"]", Charset.forName("UTF-8")));
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                System.err.println("Unlock Wallet Method failed:" + httpResponse.getStatusLine());
            } else {
                String walletPrivteKey = EntityUtils.toString(httpResponse.getEntity());
                System.out.println("walletUnlock:" + walletName);
                return true;
            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return false;
    }


    //根据钱包的秘钥生成公钥
    public String createPublicKey(String walletName, String walletPWD) {
        System.out.println("createAccountKey:" + walletName);
        String url = "http://" + IP + ":8888/v1/wallet/create_key";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "text/plain; charset=UTF-8");
//        httpPost.setHeader("Accept", "text/plain");
        httpPost.setEntity(new StringEntity("[\"" + walletName + "\",\"K1\"]", Charset.forName("UTF-8")));
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                System.err.println("Create Key Method failed:" + httpResponse.getStatusLine());
            } else {
                String walletPublicKey = EntityUtils.toString(httpResponse.getEntity());
                walletPublicKey = walletPublicKey.replace("\"", "");
                System.out.println("Wallet Public Key:" + walletPublicKey);
                return walletPublicKey;
            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return null;
    }

    //创建账号
    public String sc_newaccoount(String creator, String accountName, String publicKey) {
        System.out.println("Create Account creator:" + creator);
        System.out.println("Create Account accountName:" + accountName);
        System.out.println("Create Account publicKey:" + publicKey);

        String url = "http://" + IP + ":8888/v1/chain/abi_json_to_bin";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");

        JSONObject ownerKeysJson = new JSONObject();
        ownerKeysJson.put("key", publicKey);
        ownerKeysJson.put("weight", 1);
        JSONObject ownerJson = new JSONObject();
        ownerJson.put("threshold", 1);
        ownerJson.put("keys", new JSONObject[]{ownerKeysJson});
        String[] nullArray = new String[0];
        ownerJson.put("accounts", nullArray);
        ownerJson.put("waits", nullArray);

        JSONObject activeKeysJson = new JSONObject();
        activeKeysJson.put("key", publicKey);
        activeKeysJson.put("weight", 1);
        JSONObject activeJson = new JSONObject();
        activeJson.put("threshold", 1);
        activeJson.put("keys", new JSONObject[]{activeKeysJson});
        activeJson.put("accounts", nullArray);
        activeJson.put("waits", nullArray);

        JSONObject argsJson = new JSONObject();
        argsJson.put("creator", creator);
        argsJson.put("name", accountName);
        argsJson.put("owner", ownerJson);
        argsJson.put("active", activeJson);

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("code", "eosio");
        jsonParam.put("action", "newaccount");
        jsonParam.put("args", argsJson);

        String jsonStr = jsonParam.toString();
        httpPost.setEntity(new StringEntity(jsonStr, Charset.forName("UTF-8")));
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Create Account Method failed:" + httpResponse.getStatusLine());
            } else {
                String binargs = EntityUtils.toString(httpResponse.getEntity());
                JSONObject resultJson = new JSONObject(binargs);
                binargs = resultJson.get("binargs").toString();
                System.out.println("binargs:" + binargs);
                return binargs;
            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return null;
    }

    //获取链信息
    public JSONObject getChainInfo() {
        String url = "http://" + IP + ":8888/v1/chain/get_info";
        HttpPost httpPost = new HttpPost(url);
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("getChainInfo Method failed:" + httpResponse.getStatusLine());
            } else {
                String chainInfo = EntityUtils.toString(httpResponse.getEntity());
                JSONObject chainJson = new JSONObject(chainInfo);
                System.out.println("chain Info Json:" + chainJson);
                return chainJson;
            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return null;
    }

    //获取区块信息
    public JSONObject getBlock(long block_num_or_id) {
        String url = "http://" + IP + ":8888/v1/chain/get_block";
        HttpPost httpPost = new HttpPost(url);
        JSONObject blockNumberJson = new JSONObject();
        blockNumberJson.put("block_num_or_id", block_num_or_id);
        httpPost.setEntity(new StringEntity(blockNumberJson.toString(), Charset.forName("UTF-8")));
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("getBlock Method failed:" + httpResponse.getStatusLine());
            } else {
                String chainInfo = EntityUtils.toString(httpResponse.getEntity());
                JSONObject chainJson = new JSONObject(chainInfo);
                System.out.println("Block Info Json:" + chainJson);
                return chainJson;
            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return null;
    }

    //查看所需公钥
    public JSONArray getRequiredKeys(long ref_block_num, long ref_block_prefix, String expiration, String creator, String actionName,String accountName, String binargs, String[] publicKeys) {
        System.out.println("getRequiredKeys ref_block_num:" + ref_block_num);
        System.out.println("getRequiredKeys ref_block_prefix:" + ref_block_prefix);
        System.out.println("getRequiredKeys expiration:" + expiration);
        System.out.println("getRequiredKeys creator:" + creator);
        System.out.println("getRequiredKeys accountName:" + accountName);
        System.out.println("getRequiredKeys binargs:" + binargs);
        System.out.println("getRequiredKeys publicKey:" + Arrays.toString(publicKeys));

        String url = "http://" + IP + ":8888/v1/chain/get_required_keys";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");

        JSONObject authorizationJson = new JSONObject();
        authorizationJson.put("actor", creator);
        authorizationJson.put("permission", "active");

        JSONObject actionsJson = new JSONObject();
        actionsJson.put("account", creator);
        actionsJson.put("name", actionName);
        actionsJson.put("authorization", new JSONObject[]{authorizationJson});
        actionsJson.put("data", binargs);

        JSONObject transctionJson = new JSONObject();
        transctionJson.put("actions", new JSONObject[]{actionsJson});
        transctionJson.put("context_free_actions", new JSONObject[0]);
        transctionJson.put("context_free_data", new JSONObject[0]);
        transctionJson.put("delay_sec", 0);
        transctionJson.put("expiration", expiration);
        transctionJson.put("max_cpu_usage_ms", 0);
        transctionJson.put("max_net_usage_words", 0);
        transctionJson.put("ref_block_num", ref_block_num);
        transctionJson.put("ref_block_prefix", ref_block_prefix);
        transctionJson.put("signatures", new String[0]);

        JSONObject paraJson = new JSONObject();
        paraJson.put("available_keys", publicKeys);
        paraJson.put("transaction", transctionJson);

        String jsonStr = paraJson.toString();
        System.out.println(jsonStr);
        httpPost.setEntity(new StringEntity(jsonStr, Charset.forName("UTF-8")));
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Get Required Keys Method failed:" + httpResponse.getStatusLine());
            } else {
                String transaction = EntityUtils.toString(httpResponse.getEntity());
                JSONObject resultJson = new JSONObject(transaction);
                String required_keys = resultJson.get("required_keys").toString();
                JSONArray jsonArray = new JSONArray(required_keys);
                System.out.println("required_keys:" + required_keys);
                return jsonArray;
            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return null;
    }


    //签名新建账号的交易
    public JSONObject sc_signTransaction(long ref_block_num, long ref_block_prefix, String expiration, String creator,String actionName, String accountName, String binargs, String creatorPublicKey, String chainID) {
        System.out.println("Sign Transaction ref_block_num:" + ref_block_num);
        System.out.println("Sign Transaction ref_block_prefix:" + ref_block_prefix);
        System.out.println("Sign Transaction expiration:" + expiration);
        System.out.println("Sign Transaction creator:" + creator);
        System.out.println("Sign Transaction accountName:" + accountName);
        System.out.println("Sign Transaction binargs:" + binargs);
        System.out.println("Sign Transaction creatorPublicKey:" + creatorPublicKey);

        String url = "http://" + IP + ":8888/v1/wallet/sign_transaction";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");

        JSONObject authorizationJson = new JSONObject();
        authorizationJson.put("actor", creator);
        authorizationJson.put("permission", "active");

        JSONObject actionsJson = new JSONObject();
        actionsJson.put("account", creator);
        actionsJson.put("name", actionName);
        actionsJson.put("authorization", new JSONObject[]{authorizationJson});
        actionsJson.put("data", binargs);

        JSONObject paraJson = new JSONObject();
        paraJson.put("ref_block_num", ref_block_num);
        paraJson.put("ref_block_prefix", ref_block_prefix);
        paraJson.put("expiration", expiration);
        paraJson.put("actions", new JSONObject[]{actionsJson});
        paraJson.put("signatures", new JSONObject[0]);

        String[] pubKeyJson = new String[3];
        pubKeyJson[0] = paraJson.toString();
        pubKeyJson[1] = Arrays.toString(new String[]{"\"" + creatorPublicKey + "\""});
        pubKeyJson[2] = "\"" + chainID + "\"";


        String jsonStr = Arrays.toString(pubKeyJson);
        httpPost.setEntity(new StringEntity(jsonStr, Charset.forName("UTF-8")));
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                System.err.println("Sign Transaction Method failed:" + httpResponse.getStatusLine());
            } else {
                String signatures = EntityUtils.toString(httpResponse.getEntity());
                JSONObject resultJson = new JSONObject(signatures);
                binargs = resultJson.get("signatures").toString();
                System.out.println("signatures:" + binargs);
                return resultJson;
            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return null;
    }

    //签名新建账号的交易
    public JSONObject sc_pushTransaction(long ref_block_num, long ref_block_prefix, String expiration, String creator,String actionName, String accountName, String binargs, String signatures) {
        System.out.println("Push Transaction ref_block_num:" + ref_block_num);
        System.out.println("Push Transaction ref_block_prefix:" + ref_block_prefix);
        System.out.println("Push Transaction expiration:" + expiration);
        System.out.println("Push Transaction creator:" + creator);
        System.out.println("Push Transaction accountName:" + accountName);
        System.out.println("Push Transaction binargs:" + binargs);
        System.out.println("Push Transaction signatures:" + signatures);

        String url = "http://" + IP + ":8888/v1/chain/push_transaction";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");

        JSONObject authorizationJson = new JSONObject();
        authorizationJson.put("actor", creator);
        authorizationJson.put("permission", "active");

        JSONObject actionsJson = new JSONObject();
        actionsJson.put("account", creator);
        actionsJson.put("name", actionName);
        actionsJson.put("authorization", new JSONObject[]{authorizationJson});
        actionsJson.put("data", binargs);

        JSONObject transctionJson = new JSONObject();
        transctionJson.put("ref_block_num", ref_block_num);
        transctionJson.put("ref_block_prefix", ref_block_prefix);
        transctionJson.put("expiration", expiration);
        transctionJson.put("actions", new JSONObject[]{actionsJson});

        JSONObject paraJson = new JSONObject();
        paraJson.put("signatures", new String[]{signatures});
        paraJson.put("transaction", transctionJson);
        paraJson.put("compression", "none");

        String jsonStr = paraJson.toString();
        System.out.println(jsonStr);
        httpPost.setEntity(new StringEntity(jsonStr, Charset.forName("UTF-8")));
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_ACCEPTED) {
                System.err.println("push Transaction Method failed:" + httpResponse.getStatusLine());
            } else {
                String transaction = EntityUtils.toString(httpResponse.getEntity());
                JSONObject resultJson = new JSONObject(transaction);
                String transaction_id = resultJson.get("transaction_id").toString();
                System.out.println("transaction:" + transaction_id);
                return resultJson;
            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return null;
    }


    //列出所有的key对
    public String listWalletKeys(String walletName, String walletPSW) {
        String url = "http://" + IP + ":8888/v1/wallet/list_keys";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
        JSONObject walletInfo = new JSONObject();
        walletInfo.put(walletName, walletPSW);
        String jsonStr = Arrays.toString(new String[]{"\"" + walletName + "\"", "\"" + walletPSW + "\""});
        System.out.println(jsonStr);
        httpPost.setEntity(new StringEntity(jsonStr, Charset.forName("UTF-8")));
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("listWalletKeys Method failed:" + httpResponse.getStatusLine());
            } else {
                String keyPairs = EntityUtils.toString(httpResponse.getEntity());
                System.out.println("listWalletKeys:" + keyPairs);
                return keyPairs;
            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
        return null;
    }

    //导入私钥
    public void walletImportKey(String walletName, String privateKey) {
        String url = "http://" + IP + ":8888/v1/wallet/import_key";
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "text/plain; charset=UTF-8");
        String jsonStr = Arrays.toString(new String[]{"\"" + walletName + "\"", "\"" + privateKey + "\""});
        httpPost.setEntity(new StringEntity(jsonStr, Charset.forName("UTF-8")));
        System.out.println(jsonStr);
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                System.err.println("walletImportKey Method failed:" + httpResponse.getStatusLine());
            } else {
                String keyPairs = EntityUtils.toString(httpResponse.getEntity());
                System.out.println("walletImportKey:" + keyPairs);

            }
        } catch (ClientProtocolException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        } catch (IOException e) {
            System.out.println("http请求失败，uri{" + url + "},exception:" + e);
        }
    }

    //发币

    //转账


}
