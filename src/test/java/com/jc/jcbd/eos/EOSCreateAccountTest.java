package com.jc.jcbd.eos;

import org.json.JSONArray;
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
public class EOSCreateAccountTest {

    final static String WALLET_NAME = "cooljsz32223";
    //生成的账户用密钥对
    final static String ACCOUNT_PWD_KEY = "5Jbou6fLHESG84rGFpKD7hbz8Ze4DqTgdK1E4Rz8xkKitnfgf13";
    final static String ACCOUNT_PUB_KEY = "EOS7sAqdTaFxxMjsXyqqBfsBKMWcKZh7jCWbSTxVTqXMGfTg5bwXK";
    //测试链eosio密钥对
    final static String EOSIO_PWD_KEY = "5KQwrPbwdL6PhXujxW37FSSQZ1JiwsST4cqQzDeyXtP79zkvFD3";
    final static String EOSIO_PUB_KEY = "EOS6MRyAjQq8ud7hVNYcfnVPJqcVpscN5So8BhtHuGYqET5GDW5CV";
    //生成的钱包账号
    static String WALLET_PWD_KEY = "PW5K3ycKEnTY3Fgee5jcThUmjoggeoE3v3hYJLfsw3dtU17GgMKwW";
    static String WALLET_PUB_KEY = "EOS5kJo8JYk1aM9VhGHVCc2aM5S4uQ9EpU4aB1KzDwpA4c7fnoBFa";

    @BeforeClass
    public static void beforeClass() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
    }

    @Test
    public void a0_createWalletTest() {
        EOSCreateAccount eosCreateAccount = new EOSCreateAccount();
        String walletPrivateKey = eosCreateAccount.createWallet(WALLET_NAME);
        System.out.println("createWalletName:" + WALLET_NAME + "    privateKey:" + walletPrivateKey);
        WALLET_PWD_KEY = walletPrivateKey;
    }

    @Test
    public void a1_unlockWalletTest() {
        EOSCreateAccount eosCreateAccount = new EOSCreateAccount();
        boolean isUnlock = eosCreateAccount.unlockWallet(WALLET_NAME, WALLET_PWD_KEY);
        if (isUnlock) {
            System.out.println("Wallet is unlocked:" + WALLET_NAME);
        }
    }

    @Test
    public void a2_createPublicKeyTest() {
        EOSCreateAccount eosCreateAccount = new EOSCreateAccount();
        String publicKey = eosCreateAccount.createPublicKey(WALLET_NAME, WALLET_PWD_KEY);
        System.out.println("Public Key:" + publicKey);
        WALLET_PUB_KEY = publicKey;
    }

    @Test
    public void a3_1_importKeyTest() {
        EOSCreateAccount eosCreateAccount = new EOSCreateAccount();
        eosCreateAccount.walletImportKey(WALLET_NAME, EOSIO_PWD_KEY);
    }

    @Test
    public void a3_2_importKeyTest() {
        EOSCreateAccount eosCreateAccount = new EOSCreateAccount();
        eosCreateAccount.walletImportKey(WALLET_NAME, ACCOUNT_PWD_KEY);
    }

    @Test
    public void a4_listKeypairsTest() {
        EOSCreateAccount eosCreateAccount = new EOSCreateAccount();
        eosCreateAccount.listWalletKeys(WALLET_NAME, WALLET_PWD_KEY);
    }

    @Test
    public void a5_createAccountTest() {
        //测试链创建账号时需要将eosio的秘钥导入到钱包当中,否则会出现390003错误,创建账号时注意使用的公钥需要时eosio的
        EOSCreateAccount eosCreateAccount = new EOSCreateAccount();
        String accountCreator = "eosio";
        String accountName = "contractuser";

        //创建账号
        String binargs = eosCreateAccount.sc_newaccoount(accountCreator, accountName, ACCOUNT_PUB_KEY);
        System.out.println("New account creator:" + accountCreator + "   new account name:" + accountName + "   account public key:" + ACCOUNT_PUB_KEY);

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
        boolean isUnlock = eosCreateAccount.unlockWallet(WALLET_NAME, WALLET_PWD_KEY);

        //查看签名所需使用的PublicKey
        JSONArray requiredKeys = eosCreateAccount.getRequiredKeys(head_block_num, ref_block_prefix, expiration, accountCreator,"newaccount", accountName, binargs, new String[]{WALLET_PUB_KEY, ACCOUNT_PUB_KEY, EOSIO_PUB_KEY});
        String pubKey;
        if (requiredKeys!=null&&requiredKeys.length()>0) {
            pubKey = requiredKeys.get(0).toString();
        }else
        {
            pubKey = EOSIO_PUB_KEY;
        }

        //对交易签名
        JSONObject signaturesJson = eosCreateAccount.sc_signTransaction(head_block_num, ref_block_prefix, expiration, accountCreator,"newaccount", accountName, binargs, pubKey, chainID);
        String signatures = signaturesJson.get("signatures").toString();
        signatures = signatures.substring(2, signatures.length() - 2);
//        long signBlockNum = Long.valueOf(signaturesJson.get("ref_block_num").toString());
        //推送事务
        JSONObject transactionJson = eosCreateAccount.sc_pushTransaction(head_block_num, ref_block_prefix, expiration, accountCreator,"newaccount", accountName, binargs, signatures);
        String transaction_id = transactionJson.get("transaction_id").toString();
        System.out.println(transaction_id);
    }


}
