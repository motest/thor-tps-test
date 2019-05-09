package com.vechain.thorclient.console;

import com.alibaba.fastjson.JSON;
import com.vechain.thorclient.clients.AccountClient;
import com.vechain.thorclient.clients.TransactionClient;
import com.vechain.thorclient.core.model.blockchain.Account;
import com.vechain.thorclient.core.model.blockchain.NodeProvider;
import com.vechain.thorclient.core.model.blockchain.Receipt;
import com.vechain.thorclient.core.model.blockchain.Transaction;
import com.vechain.thorclient.core.model.clients.Address;
import com.vechain.thorclient.core.model.clients.Revision;
import com.vechain.thorclient.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionConsole {

	/**
	 * args: [getTransactionRecipient] [txId] [node url]
	 * 
	 * @param args
	 */
	public static void getTransactionRecipient(String[] args) {

		if (args.length < 3 || StringUtils.isBlank(args[2])) {
			System.out.println("You have input invalid parameters.");
			System.exit(0);
		}
		String txId = args[1];
		String nodeUrl = args[2];
		if (StringUtils.isBlank(nodeUrl) && !nodeUrl.startsWith("http")) {
			System.out.println("You have input invalid parameters.");
			System.exit(0);
		}
		NodeProvider nodeProvider = NodeProvider.getNodeProvider();
		nodeProvider.setProvider(nodeUrl);
		nodeProvider.setTimeout(5000);
		Receipt receipt = TransactionClient.getTransactionReceipt(txId, null);
		System.out.println("Receipt:" + JSON.toJSONString(receipt));
	}

	public static void getTransaction(String[] args) {
		if (args.length < 3 || StringUtils.isBlank(args[2])) {
			System.out.println("You have input invalid parameters.");
			System.exit(0);
		}
		String txId = args[1];
		String nodeUrl = args[2];
		if (StringUtils.isBlank(nodeUrl) && !nodeUrl.startsWith("http")) {
			System.out.println("You have input invalid parameters.");
			System.exit(0);
		}
		NodeProvider nodeProvider = NodeProvider.getNodeProvider();
		nodeProvider.setProvider(nodeUrl);
		nodeProvider.setTimeout(5000);
		Transaction transaction = TransactionClient.getTransaction(txId, true, null);
		System.out.println("Transaction:" + JSON.toJSONString(transaction));
	}

	public static void sendRawTransaction(String[] args) throws IOException {
		if (args.length < 3) {
			System.out.println("You have input invalid parameters.");
			System.exit(0);
		}
		String result = ConsoleUtils.sendRawVETTx(args[2]);
		System.out.println("Send Result:");
		System.out.println(result);
	}

	/**
	 * transferVet
	 * 
	 * @param args
	 *            server-url to amount chainTag privateKey
	 * @throws Exception
	 */
	public static void transferVet(String[] args) throws Exception {
		String privateKey;
		if (args.length < 6) {
			System.out.println("You have input invalid parameters.");
			System.exit(0);
		}
		privateKey = args[5];

		List<String[]> transactionList = new ArrayList<String[]>();
		String[] tranfs = new String[4];
		tranfs[0] = args[2];
		tranfs[1] = args[3];
		tranfs[2] = args[4];
		tranfs[3] = null;
		transactionList.add(tranfs);
		String result = ConsoleUtils.doSignVETTx(transactionList, privateKey, true);
		System.out.println(result);

	}

	/**
	 * transferVet
	 * 
	 * @param args
	 *            server-url to amount chainTag privateKey gaslimit(options)
	 * @throws Exception
	 */
	public static void transferVtho(String[] args) throws Exception {
		String privateKey;
		if (args.length < 6) {
			System.out.println("You have input invalid parameters.");
			System.exit(0);
		}
		privateKey = args[5];

		List<String[]> transactionList = new ArrayList<String[]>();
		String[] tranfs = new String[4];
		tranfs[0] = args[2];
		tranfs[1] = args[3];
		tranfs[2] = args[4];
		tranfs[3] = null;
		transactionList.add(tranfs);
		String result = ConsoleUtils.doSignVTHOTx(transactionList, privateKey, true,
				tranfs.length > 6 ? Integer.parseInt(tranfs[6]) : null);
		System.out.println(result);
	}

	public static void getBalance(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("You have input invalid parameters.");
			System.exit(0);
		}
		Address address = Address.fromHexString(args[2]);
		Account account = AccountClient.getAccountInfo(address, Revision.BEST);
		System.out.println(JSON.toJSONString(account));
	}
}
