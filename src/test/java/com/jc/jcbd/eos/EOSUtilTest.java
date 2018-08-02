package com.jc.jcbd.eos;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)//按方法顺序进行测试
public class EOSUtilTest {

    final static String walletName = "cooljsz32223";
    //生成的账户用密钥对
    final static String accountPWDKey = "5Jbou6fLHESG84rGFpKD7hbz8Ze4DqTgdK1E4Rz8xkKitnfgf13";
    final static String accountPubKey = "EOS7sAqdTaFxxMjsXyqqBfsBKMWcKZh7jCWbSTxVTqXMGfTg5bwXK";
    //测试链eosio密钥对
    final static String eosioPWDKey = "5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3";
    final static String eosioPubKey = "EOS6MRyAjQq8ud7hVNYcfnVPJqcVpscN5So8BhtHuGYqET5GDW5CV";
    static String walletPWDKey = "PW5JvCL45D8WnNVEuB71iaSanLUa9A1WAh116oP7Lt2QJ588u5R1V";
    static String walletPubKey = "EOS83bJ7BwAvLU2njwkWr38xfY4CKSqeQapKanTVgKcMnNiR6a1Kf";

    @BeforeClass
    public static void beforeClass() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
    }

    @Test
    public void a0_createWalletTest() {
        EOSUtil eosUtil = new EOSUtil();
        String walletPrivateKey = eosUtil.createWallet(walletName);
        System.out.println("createWalletName:" + walletName + "    privateKey:" + walletPrivateKey);
        walletPWDKey = walletPrivateKey;
    }

    @Test
    public void a1_unlockWalletTest() {
        EOSUtil eosUtil = new EOSUtil();
        boolean isUnlock = eosUtil.unlockWallet(walletName, walletPWDKey);
        if (isUnlock) {
            System.out.println("Wallet is unlocked:" + walletName);
        }
    }

    @Test
    public void a2_createPublicKeyTest() {
        EOSUtil eosUtil = new EOSUtil();
        String publicKey = eosUtil.createPublicKey(walletName, walletPWDKey);
        System.out.println("Public Key:" + publicKey);
        walletPubKey = publicKey;
    }

    @Test
    public void a3_importKeyTest() {
        EOSUtil eosUtil = new EOSUtil();
        eosUtil.walletImportKey(walletName, eosioPWDKey);
    }

    @Test
    public void a4_listKeypairsTest() {
        EOSUtil eosUtil = new EOSUtil();
        eosUtil.listWalletKeys(walletName, walletPWDKey);
    }

    @Test
    public void a5_createAccountTest() {
        //测试链创建账号时需要将eosio的秘钥导入到钱包当中,否则会出现390003错误,创建账号时注意使用的公钥需要时eosio的
        EOSUtil eosUtil = new EOSUtil();
        String accountCreator = "eosio";
        String accountName = "cooljsz12345";

        //创建账号
        String binargs = eosUtil.sc_newaccoount(accountCreator, accountName, accountPubKey);
        System.out.println("New account creator:" + accountCreator + "   new account name:" + accountName + "   account public key:" + accountPubKey);

        //获得链信息
        JSONObject chainInfoJson = eosUtil.getChainInfo();
        String chainID = chainInfoJson.get("chain_id").toString();
        System.out.println("chainInfoJson:" + chainInfoJson);
        long head_block_num = Long.valueOf(chainInfoJson.get("head_block_num").toString());
        System.out.println("head_block_num:" + head_block_num);

        //获得最新的块信息
        JSONObject blockInfoJson = eosUtil.getBlock(head_block_num);
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
        boolean isUnlock = eosUtil.unlockWallet(walletName, walletPWDKey);

        //查看签名所需使用的PublicKey
        JSONObject requiredKeys = eosUtil.getRequiredKeys(head_block_num, ref_block_prefix, expiration, accountCreator, accountName, binargs, new String[]{walletPubKey, accountPubKey, eosioPubKey});
//        String pubKey = ((String[]) requiredKeys.get("required_keys"))[0];

        //对交易签名
        JSONObject signaturesJson = eosUtil.sc_signTransaction(head_block_num, ref_block_prefix, expiration, accountCreator, accountName, binargs, eosioPubKey, chainID);
        String signatures = signaturesJson.get("signatures").toString();
        signatures = signatures.substring(2, signatures.length() - 2);
//        long signBlockNum = Long.valueOf(signaturesJson.get("ref_block_num").toString());
        //推送事务
        JSONObject transactionJson = eosUtil.sc_pushTransaction(head_block_num, ref_block_prefix, expiration, accountCreator, accountName, binargs, signatures);
        String transaction_id = transactionJson.get("transaction_id").toString();
        System.out.println(transaction_id);
    }


}
