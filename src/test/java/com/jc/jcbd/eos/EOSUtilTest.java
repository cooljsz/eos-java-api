package com.jc.jcbd.eos;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)//按方法名进行测试
public class EOSUtilTest {

    final static String walletName = "cooljsz32223";
    final static String walletPWD = "PW5JvCL45D8WnNVEuB71iaSanLUa9A1WAh116oP7Lt2QJ588u5R1V";
    final static String publicKey = "EOS83bJ7BwAvLU2njwkWr38xfY4CKSqeQapKanTVgKcMnNiR6a1Kf";

    //5Jbou6fLHESG84rGFpKD7hbz8Ze4DqTgdK1E4Rz8xkKitnfgf13
    //EOS7sAqdTaFxxMjsXyqqBfsBKMWcKZh7jCWbSTxVTqXMGfTg5bwXK

    //eosio
    //5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3
    //EOS6MRyAjQq8ud7hVNYcfnVPJqcVpscN5So8BhtHuGYqET5GDW5CV

    @BeforeClass
    public static void beforeClass() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
    }

    @Test
    public void createWalletTest() {
        EOSUtil eosUtil = new EOSUtil();

        String walletPrivateKey = eosUtil.createWallet(walletName);
        System.out.println("createWalletName:" + walletName + "    privateKey:" + walletPrivateKey);
    }

    @Test
    public void unlockWalletTest() {
        EOSUtil eosUtil = new EOSUtil();
        boolean isUnlock = eosUtil.unlockWallet(walletName, walletPWD);
        if (isUnlock) {
            System.out.println("Wallet is unlocked:" + walletName);
        }
    }

    @Test
    public void createPublicKeyTest() {
        EOSUtil eosUtil = new EOSUtil();
        String publicKey = eosUtil.createPublicKey(walletName, walletPWD);
        System.out.println("Public Key:" + publicKey);
    }

    @Test
    public void createAccountTest() {
        EOSUtil eosUtil = new EOSUtil();
        String creator = "eosio";
        String accountName = "cooljsz12345";
        //创建账号
        String binargs = eosUtil.sc_newaccoount(creator, accountName, publicKey);
        System.out.println("Public Key:" + publicKey);
        //获得链信息
        JSONObject chainInfoJson = eosUtil.getChainInfo();
        String chainID = chainInfoJson.get("chain_id").toString();
        System.out.println("chainInfoJson:" + chainInfoJson);
        long head_block_num = Long.valueOf(chainInfoJson.get("head_block_num").toString());
        System.out.println("head_block_num:" + head_block_num);
        //获得最新的块信息
        JSONObject blockInfoJson = eosUtil.getBlock(head_block_num);
        String timestamp = blockInfoJson.get("timestamp").toString();
        System.out.println("timestamp:" + timestamp);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        Date date;
        try {
            date = df.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            System.err.println("时间转换异常,目标格式:yyyy-MM-dd'T'HH:mm:ss.SSS 传入时间:" + timestamp);
            return;
        }
        Calendar rightNow = Calendar.getInstance();
        rightNow.setTime(date);
        rightNow.add(Calendar.MINUTE, 1);//时间加1分钟
        Date dt1 = rightNow.getTime();
        String expiration = df.format(dt1);
        long ref_block_prefix = Long.valueOf(blockInfoJson.get("ref_block_prefix").toString());
        boolean isUnlock = eosUtil.unlockWallet(walletName, walletPWD);
        //查看签名所需使用的PublicKey
        JSONObject requiredKeys = eosUtil.getRequiredKeys(head_block_num, ref_block_prefix, expiration, creator, accountName, binargs, new String[]{publicKey, "EOS7sAqdTaFxxMjsXyqqBfsBKMWcKZh7jCWbSTxVTqXMGfTg5bwXK"});
//        String pubKey = ((String[]) requiredKeys.get("required_keys"))[0];
        //对交易签名
        JSONObject signaturesJson = eosUtil.sc_signTransaction(head_block_num, ref_block_prefix, expiration, creator, accountName, binargs, "EOS6MRyAjQq8ud7hVNYcfnVPJqcVpscN5So8BhtHuGYqET5GDW5CV", chainID);
        String signatures = signaturesJson.get("signatures").toString();
        signatures = signatures.substring(2, signatures.length() - 2);
//        long signBlockNum = Long.valueOf(signaturesJson.get("ref_block_num").toString());
        //推送事务
        JSONObject transactionJson = eosUtil.sc_pushTransaction(head_block_num, ref_block_prefix, expiration, creator, accountName, binargs, signatures);
        String transaction_id = transactionJson.get("transaction_id").toString();
        System.out.println(transaction_id);
    }

    @Test
    public void listKeypairsTest() {
        EOSUtil eosUtil = new EOSUtil();
        eosUtil.listWalletKeys(walletName, walletPWD);
    }

    @Test
    public void importKeyTest() {
        EOSUtil eosUtil = new EOSUtil();
        eosUtil.walletImportKey(walletName, "5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3");
    }


}
