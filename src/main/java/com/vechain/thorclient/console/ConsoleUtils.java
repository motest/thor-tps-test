package com.vechain.thorclient.console;

import com.alibaba.fastjson.JSON;
import com.vechain.thorclient.clients.BlockchainClient;
import com.vechain.thorclient.clients.TransactionClient;
import com.vechain.thorclient.core.model.blockchain.TransferResult;
import com.vechain.thorclient.core.model.clients.*;
import com.vechain.thorclient.core.model.clients.base.AbstractToken;
import com.vechain.thorclient.utils.*;
import com.vechain.thorclient.utils.crypto.ECKeyPair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConsoleUtils {

	public static String doSignVETTx(List<String[]> transactions, String privateKey, boolean isSend)
			throws IOException {

		byte chainTag = 0;
		byte[] blockRef = null;

		List<ToClause> clauses = new ArrayList<ToClause>();
		for (String[] transaction : transactions) {
			Amount amount = Amount.createFromToken(AbstractToken.VET);
			amount.setDecimalAmount(transaction[1]);
			clauses.add(TransactionClient.buildVETToClause(Address.fromHexString(transaction[0]), amount, ToData.ZERO));
			chainTag = BytesUtils.toByteArray(transaction[2])[0];
			if (transaction[3] == null) {
				blockRef = BlockchainClient.getBlockRef(null).toByteArray();
			} else {
				blockRef = BytesUtils.toByteArray(transaction[3]);
			}
		}
		int gas = clauses.size() * 21000;
		RawTransaction rawTransaction = RawTransactionFactory.getInstance().createRawTransaction(chainTag, blockRef,
				720, gas, (byte) 0x0, CryptoUtils.generateTxNonce(), clauses.toArray(new ToClause[0]));
		if (isSend) {
			TransferResult result = TransactionClient.signThenTransfer(rawTransaction, ECKeyPair.create(privateKey));
			return JSON.toJSONString(result);
		} else {
			RawTransaction result = TransactionClient.sign(rawTransaction, ECKeyPair.create(privateKey));
			return BytesUtils.toHexString(result.encode(), Prefix.ZeroLowerX);
		}
	}

	public static String sendRawVETTx(String rawTransaction) throws IOException {
		TransferResult result = TransactionClient.transfer(rawTransaction);
		return JSON.toJSONString(result);
	}


	public static String doSignVTHOTx(List<String[]> transactions, String privateKey, boolean isSend)
			throws IOException {
		return doSignVTHOTx(transactions, privateKey, isSend, null);
	}

	public static String doSignVTHOTx(List<String[]> transactions, String privateKey, boolean isSend, Integer gasLimit)
			throws IOException {

		byte chainTag = 0;
		byte[] blockRef = null;

		List<ToClause> clauses = new ArrayList<ToClause>();
		for (String[] transaction : transactions) {
			Amount amount = Amount.VTHO();
			amount.setDecimalAmount(transaction[1]);
			clauses.add(
					ERC20Contract.buildTranferToClause(ERC20Token.VTHO, Address.fromHexString(transaction[0]), amount));
			chainTag = BytesUtils.toByteArray(transaction[2])[0];
			if (transaction[3] == null) {
				blockRef = BlockchainClient.getBlockRef(null).toByteArray();
			} else {
				blockRef = BytesUtils.toByteArray(transaction[3]);
			}
		}
		if (gasLimit == null) {
			gasLimit = 80000;
		}
		int gas = clauses.size() * gasLimit;
		RawTransaction rawTransaction = RawTransactionFactory.getInstance().createRawTransaction(chainTag, blockRef,
				720, gas, (byte) 0x0, CryptoUtils.generateTxNonce(), clauses.toArray(new ToClause[0]));
		if (isSend) {
			TransferResult result = TransactionClient.signThenTransfer(rawTransaction, ECKeyPair.create(privateKey));
			return JSON.toJSONString(result);
		} else {
			RawTransaction result = TransactionClient.sign(rawTransaction, ECKeyPair.create(privateKey));
			return BytesUtils.toHexString(result.encode(), Prefix.ZeroLowerX);
		}
	}
}
