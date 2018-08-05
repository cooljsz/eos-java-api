package com.jc.jcbd.eos;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EOSTransferTest {

    @Test
    public void transferTest() {

        //测试链创建账号时需要将eosio的秘钥导入到钱包当中,否则会出现390003错误,创建账号时注意使用的公钥需要时eosio的
        EOSCreateAccount eosCreateAccount = new EOSCreateAccount();
        boolean isUnlock = eosCreateAccount.unlockWallet(Parameters.WALLET_NAME, Parameters.WALLET_PWD_KEY);
        if (isUnlock) {
            System.out.println("Wallet is unlocked:" + Parameters.WALLET_NAME);
        }

        EOSTransfer eosTransfer = new EOSTransfer();
        //创建账号
        String binargs = eosTransfer.sc_transfer(Parameters.accountName, Parameters.accountName, Parameters.accountCreator, new BigDecimal(0.0001), "JCB", "test");
        System.out.println("contractName:" + Parameters.accountCreator + "   from:" + Parameters.accountName + "   to:" + Parameters.ACCOUNT_PUB_KEY);

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
            return;
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
    }

    @Test
    public void simpleTransferTest()
    {
        EOSTransfer eosTransfer = new EOSTransfer();
        String transactionID = eosTransfer.transfer(Parameters.accountName,Parameters.accountCreator,"测试字符");
        System.out.println(transactionID);
    }

    @Test
    public void getMemoTest() {
        EOSTransfer eosTransfer = new EOSTransfer();
        String memo = eosTransfer.getMemo("153bc65d04564eb4159cfa772a823824cf3c498f12093e213331b96428b4199f");
        System.out.println(memo);
    }
}
