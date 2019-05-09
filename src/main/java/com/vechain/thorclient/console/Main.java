package com.vechain.thorclient.console;

import com.alibaba.fastjson.JSON;
import com.vechain.thorclient.clients.AccountClient;
import com.vechain.thorclient.clients.BlockClient;
import com.vechain.thorclient.clients.BlockchainClient;
import com.vechain.thorclient.clients.TransactionClient;
import com.vechain.thorclient.core.model.blockchain.Account;
import com.vechain.thorclient.core.model.blockchain.Block;
import com.vechain.thorclient.core.model.blockchain.NodeProvider;
import com.vechain.thorclient.core.model.blockchain.TransferResult;
import com.vechain.thorclient.core.model.clients.*;
import com.vechain.thorclient.core.model.clients.base.AbstractToken;
import com.vechain.thorclient.core.wallet.WalletInfo;
import com.vechain.thorclient.utils.*;
import com.vechain.thorclient.utils.crypto.ECKeyPair;

import java.io.Console;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class Main {

	public static void main(String[] args) throws Exception {
		NodeProvider nodeProvider = NodeProvider.getNodeProvider();
		nodeProvider.setProvider("http://127.0.0.1:8669");

		if(args.length>0&&args[0].contains("-log")){
			PrintBlocksInfo();
		}else if(args.length>0&&args[0].contains("-test")){
			CreateTx();
		}
		else{
			System.out.println(" -log   |  print block info");
			System.out.println(" -test  |  start tps test");
		}
	}

	static void CreateTx(){
		try {
			ArrayList<String> txs=new ArrayList<String>();
			Map<String,ECKeyPair> richAccounts=new HashMap<String,ECKeyPair>();
			Map<String,BigDecimal> accountAmount=new HashMap<String,BigDecimal>();
			Map<String,BigDecimal> accountVtho=new HashMap<String,BigDecimal>();
			richAccounts.put("0x7567d83b7b8d80addcb281a71d54fc7b3364ffed",ECKeyPair.create("0xdce1443bd2ef0c2631adc1c67e5c93f13dc23a41c18b536effbbdcbcdb96fb65"));
			richAccounts.put("0xd3ae78222beadb038203be21ed5ce7c9b1bff602",ECKeyPair.create("0x321d6443bc6177273b5abf54210fe806d451d6b7973bccc2384ef78bbcd0bf51"));
			richAccounts.put("0x733b7269443c70de16bbf9b0615307884bcc5636",ECKeyPair.create("0x2d7c882bad2a01105e36dda3646693bc1aaaa45b0ed63fb0ce23c060294f3af2"));
			richAccounts.put("0x115eabb4f62973d0dba138ab7df5c0375ec87256",ECKeyPair.create("0x593537225b037191d322c3b1df585fb1e5100811b71a6f7fc7e29cca1333483e"));
			richAccounts.put("0x199b836d8a57365baccd4f371c1fabb7be77d389",ECKeyPair.create("0xca7b25fc980c759df5f3ce17a3d881d6e19a38e651fc4315fc08917edab41058"));
			richAccounts.put("0x5e4efedf3d71232340280d8bc475421352994b63",ECKeyPair.create("0x88d2d80b12b92feaa0da6d62309463d20408157723f2d7e799b6a74ead9a673b"));
			richAccounts.put("0x29f72dc07224a4c6270407bfd6b8fec559d29f6c",ECKeyPair.create("0xfbb9e7ba5fe9969a71c6599052237b91adeb1e5fc0c96727b66e56ff5d02f9d0"));
			richAccounts.put("0x47109a193c49862c89bd76fe2de3585743dd2bb0",ECKeyPair.create("0x547fb081e73dc2e22b4aae5c60e2970b008ac4fc3073aebc27d41ace9c4f53e9"));
			richAccounts.put("0xa5e255d4c65af201b97210ff4cd9521a46427654",ECKeyPair.create("0xc8c53657e41a8d669349fc287f57457bd746cb1fcfc38cf94d235deb2cfca81b"));
			richAccounts.put("0x0489a3fff1930b85f1d73eff8a4699281aadb558",ECKeyPair.create("0x87e0eba9c86c494d98353800571089f316740b0cb84c9a7cdf2fe5c9997c7966"));

			for (String address : richAccounts.keySet()) {
				Account account= AccountClient.getAccountInfo(address,null);
				accountAmount.put(address,account.VETBalance().getAmount());
				accountVtho.put(address,account.energyBalance().getAmount());
			}

			while(richAccounts.size()>0){

				byte chainTag = BlockchainClient.getChainTag();
				byte[] blockRef = BlockchainClient.getBlockRef(Revision.BEST).toByteArray();

				System.out.println("create tx and sign..., rich account count="+richAccounts.size());

				Map<String,ECKeyPair> newAccounts=new HashMap<String,ECKeyPair>();
				Map<String,Object> successAccounts=new HashMap<String,Object>();
				Map<String,String> vbtTx=new HashMap<String,String>();
				Map<String,String> vthoTx=new HashMap<String,String>();
				for (String address : richAccounts.keySet()) {
					Long nowAmount=accountAmount.get(address).longValue();
					Long nowVtho=accountVtho.get(address).longValue();
					for(int i=0;i<8;i++){
						ECKeyPair newAcc=ECKeyPair.create();
						String toAddress=newAcc.getAddress();
						newAccounts.put(toAddress,newAcc);
						Long amountVet=(nowAmount/9);
						Long amountVtho=(nowVtho/9);
						vbtTx.put(toAddress, CreateVbt(richAccounts.get(address),newAcc.getAddress(), amountVet.toString(),chainTag,blockRef));
						vthoTx.put(toAddress, CreateVtho(richAccounts.get(address),newAcc.getAddress(), amountVtho.toString(),chainTag,blockRef));
					}
				}

				System.out.println("sign completed, count="+vbtTx.size());
				for (String address : vbtTx.keySet()) {
					try{
						TransactionClient.transfer(vbtTx.get(address));
						TransactionClient.transfer(vthoTx.get(address));
						if(newAccounts.containsKey(address)) successAccounts.put(address,null);
					} catch (Exception e) {
						//System.out.println("操作失败 " + e.getMessage());
						//pool is full
					}
				}

				System.out.println("submit completed, count="+successAccounts.size());
				Thread.sleep(5000);
				Integer checkBlockTime=0;
				while(true){
					Integer nowCount=successAccounts.size();
					List<String> accounts=new ArrayList<String>();
					for (String address : successAccounts.keySet()){
						accounts.add(address);
					}
					for (String address : accounts){
						Account account= AccountClient.getAccountInfo(address,null);
						if(account.VETBalance().getAmount().longValue()>0&&account.energyBalance().getAmount().longValue()>0){
							richAccounts.put(address,newAccounts.get(address));
							accountAmount.put(address,account.VETBalance().getAmount());
							accountVtho.put(address,account.energyBalance().getAmount());
							//System.out.println(address+" > "+account.VETBalance().getAmount().longValue());
							successAccounts.remove(address);
						}
					}

					if(successAccounts.size()==0){
						break;
					}else if(successAccounts.size()==nowCount){
						checkBlockTime++;
						if(checkBlockTime>=15){//持续1个区块没有打包
							break;
						}
					}
					else{
						checkBlockTime=0;
					}

					Thread.sleep(1000);
				}

				for (String address : richAccounts.keySet()) {//更新老账户
					if(!newAccounts.containsKey(address)){
						Account account= AccountClient.getAccountInfo(address,null);
						accountAmount.put(address,account.VETBalance().getAmount());
						accountVtho.put(address,account.energyBalance().getAmount());
						//System.out.println(address+" > "+account.VETBalance().getAmount().longValue());
					}
				}
			}

		} catch (Exception e) {
			System.out.println("操作失败 " + e.getMessage());
		}
	}

	static void PrintBlocksInfo(){
		Integer index=0;
		while (true){
			try{
				index++;
				Block block=BlockClient.getBlock(Revision.create(index));
				if(block==null)break;
				if(block.getTransactions().size()==0)continue;
				System.out.println("block index="+index+" , txs="+block.getTransactions().size());
			}
			catch (Exception e){
				break;
			}
		}
	}

	static String CreateVbt(ECKeyPair fromKey,String toAddress,String amount,byte chainTag,byte[] blockRef){
		Amount sendAmount = Amount.createFromToken(AbstractToken.VET);
		sendAmount.setDecimalAmount(amount);
		ToClause clause = TransactionClient.buildVETToClause(Address.fromHexString(toAddress), sendAmount, ToData.ZERO);

		RawTransaction rawTransaction = RawTransactionFactory.getInstance().createRawTransaction(chainTag, blockRef,720, 21000, (byte) 0x0, CryptoUtils.generateTxNonce(), clause);
		TransactionClient.sign(rawTransaction, fromKey);
		return BytesUtils.toHexString(rawTransaction.encode(), Prefix.ZeroLowerX);
	}

	static String CreateVtho(ECKeyPair fromKey,String toAddress,String amount,byte chainTag,byte[] blockRef){
		Amount sendAmount = Amount.createFromToken( ERC20Token.VTHO);
		sendAmount.setDecimalAmount(amount);
		ToClause clause = ERC20Contract.buildTranferToClause(
				ERC20Token.VTHO,
				Address.fromHexString(toAddress),
				sendAmount);
		RawTransaction rawTransaction = RawTransactionFactory.getInstance().createRawTransaction( chainTag, blockRef, 720, 80000, (byte)0x0, CryptoUtils.generateTxNonce(), clause);
		TransactionClient.sign(rawTransaction, fromKey);
		return BytesUtils.toHexString(rawTransaction.encode(), Prefix.ZeroLowerX);
	}
}
