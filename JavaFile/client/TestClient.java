package org.fisco.bcos.asset.client;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fisco.bcos.asset.contract.Test;
import org.fisco.bcos.asset.contract.Test.RegisterEventEventResponse;
import org.fisco.bcos.asset.contract.Test.TransferEventEventResponse;
import org.fisco.bcos.asset.contract.Test.IncMoneyEventEventResponse;
import org.fisco.bcos.asset.contract.Test.DecMoneyEventEventResponse;
import org.fisco.bcos.asset.contract.Test.SendTxEventEventResponse;
import org.fisco.bcos.asset.contract.Test.FinanceEventEventResponse;
import org.fisco.bcos.asset.contract.Test.PayEventEventResponse;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.Keys;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tuples.generated.Tuple2;
import org.fisco.bcos.web3j.tuples.generated.Tuple4;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class TestClient {

	static Logger logger = LoggerFactory.getLogger(TestClient.class);

	private Web3j web3j;

	private Credentials credentials;

	public Web3j getWeb3j() {
		return web3j;
	}

	public void setWeb3j(Web3j web3j) {
		this.web3j = web3j;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public void recordTestAddr(String address) throws FileNotFoundException, IOException {
		Properties prop = new Properties();
		prop.setProperty("address", address);
		final Resource contractResource = new ClassPathResource("contract.properties");
		FileOutputStream fileOutputStream = new FileOutputStream(contractResource.getFile());
		prop.store(fileOutputStream, "contract address");
	}

	public String loadTestAddr() throws Exception {
		// load Test contact address from contract.properties
		Properties prop = new Properties();
		final Resource contractResource = new ClassPathResource("contract.properties");
		prop.load(contractResource.getInputStream());

		String contractAddress = prop.getProperty("address");
		if (contractAddress == null || contractAddress.trim().equals("")) {
			throw new Exception(" load Test contract address failed, please deploy it first. ");
		}
		logger.info(" load Test address from contract.properties, address is {}", contractAddress);
		return contractAddress;
	}

	public void initialize() throws Exception {

		// init the Service
		@SuppressWarnings("resource")
		ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		Service service = context.getBean(Service.class);
		service.run();

		ChannelEthereumService channelEthereumService = new ChannelEthereumService();
		channelEthereumService.setChannelService(service);
		Web3j web3j = Web3j.build(channelEthereumService, 1);

		// init Credentials
		Credentials credentials = Credentials.create(Keys.createEcKeyPair());

		setCredentials(credentials);
		setWeb3j(web3j);

		logger.debug(" web3j is " + web3j + " ,credentials is " + credentials);
	}

	private static BigInteger gasPrice = new BigInteger("30000000");
	private static BigInteger gasLimit = new BigInteger("30000000");

	public void deployTestAndRecordAddr() {

		try {
			Test test = Test.deploy(web3j, credentials, new StaticGasProvider(gasPrice, gasLimit)).send();
			System.out.println(" deploy Test success, contract address is " + test.getContractAddress());

			recordTestAddr(test.getContractAddress());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println(" deploy Test contract failed, error message is  " + e.getMessage());
		}
	}

	public void incCompanyMoneyTest(String account,BigInteger amount){
		try {
			String contractAddress = loadTestAddr();

			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = test.incCompanyMoney(account, amount).send();
			List<IncMoneyEventEventResponse> response = test.getIncMoneyEventEvents(receipt);
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" increase money success => final value of %s: %s \n",account, response.get(0).amount.toString());
				} else if (response.get(0).ret.compareTo(new BigInteger("1")) == 0){
					System.out.printf(" increase money failed.Please try again!\nret code is %s \n",
							account,response.get(0).ret.toString());
				}
				else {
					System.out.printf(" increase money failed: Company: %s not found!\n ret code is %s \n",
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" incBankMoneyTest exception, error message is {}", e.getMessage());
			System.out.printf(" increase bank money failed, error message is %s\n", e.getMessage());
		}
	}
	public void decCompanyMoneyTest(String account,BigInteger amount){
		try {
			String contractAddress = loadTestAddr();

			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = test.decCompanyMoney(account, amount).send();
			List<DecMoneyEventEventResponse> response = test.getDecMoneyEventEvents(receipt);
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" decrease money success => final value of %s: %s \n",account, response.get(0).amount.toString());
				} else if (response.get(0).ret.compareTo(new BigInteger("1")) == 0){
					System.out.printf(" decrease money failed.Please try again!\nret code is %s \n",
							account,response.get(0).ret.toString());
				}
				else if (response.get(0).ret.compareTo(new BigInteger("-1")) == 0){
					System.out.printf(" decrease bank money failed: Company: %s not found!\n ret code is %s \n",account,
							response.get(0).ret.toString());
				}else {
					System.out.printf(" decrease bank money failed: money of company: %s not enough to decrease!\n ret code is %s \n",account,
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" decBankMoneyTest exception, error message is {}", e.getMessage());
			System.out.printf(" decrease bank money failed, error message is %s\n", e.getMessage());
		}
	}
	public void checkTestAccount(String account) {
		try {
			String contractAddress = loadTestAddr();

			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			Tuple2<BigInteger, BigInteger> result = test.findCompany(account).send();
			if (result.getValue1().compareTo(new BigInteger("0")) == 0) {
				System.out.printf(" account: %s, value: %s \n", account, result.getValue2());
			} else {
				System.out.printf(" account:%s is not exist \n", account);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.error(" ifAccountExists exception, error message is {}", e.getMessage());

			System.out.printf(" check account existence failed, error message is %s\n", e.getMessage());
		}
	}
	public void checkTestAccount(String account,String testAddress) {
		try {
			String contractAddress = loadTestAddr();

			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			Tuple2<BigInteger, BigInteger> result = test.findCompany(account,testAddress).send();
			if (result.getValue1().compareTo(new BigInteger("0")) == 0) {
				System.out.printf(" account: %s, value: %s \n", account, result.getValue2());
			} else {
				System.out.printf(" account:%s is not exist \n", account);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.error(" ifAccountExists exception, error message is {}", e.getMessage());

			System.out.printf(" check account existence failed, error message is %s\n", e.getMessage());
		}
	}

	public void findTestAccountID(String account) {
		try {
			String contractAddress = loadTestAddr();

			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			Tuple2<BigInteger, BigInteger> result = test.findCompanyID(account).send();
			if (result.getValue1().compareTo(new BigInteger("0")) == 0) {
				System.out.printf(" account: %s, accountID: %s \n", account, result.getValue2());
			} else {
				System.out.printf(" account: %s is not exist \n", account);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.error(" accountID exception, error message is {}", e.getMessage());

			System.out.printf(" find accountID failed, error message is %s\n", e.getMessage());
		}
	}

	public void allMoneyTestAccount(String from_account,String to_account) {
		try {
			String contractAddress = loadTestAddr();

			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			Tuple2<BigInteger, BigInteger> result = test.allReceiptsMoney(from_account,to_account).send();
			if (result.getValue1().compareTo(new BigInteger("0")) == 0) {
				System.out.printf(" from_account: %s, to_account: %s, total value of the receipts: %s \n", from_account,to_account, result.getValue2());
			} else {
				System.out.printf(" Please make sure both accounts are members of the system. \n");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.error(" accountID exception, error message is {}", e.getMessage());

			System.out.printf(" find accountID failed, error message is %s\n", e.getMessage());
		}
	}
	public void findTestReceiptByID(BigInteger rcptID) {
		try {
			String contractAddress = loadTestAddr();

			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			Tuple4<BigInteger, String,String,BigInteger> result = test.findReceiptByID(rcptID).send();
			if (result.getValue1().compareTo(new BigInteger("0")) == 0) {
				System.out.printf(" from_account: %s, to_account: %s, value of the receipt: %s \n", result.getValue2(),result.getValue3(), result.getValue4());
			} else {
				System.out.printf(" receipt corresponding to %s not found!\n",rcptID.toString());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			logger.error(" findTestReceiptByID exception, error message is {}", e.getMessage());

			System.out.printf(" find receipt failed, error message is %s\n", e.getMessage());
		}
	}

	public void registerTestAccount(String account, String address,BigInteger amount,BigInteger role) {
		try {
			String contractAddress = loadTestAddr();

			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = test.registerCompany(account, address,amount,role).send();
			List<RegisterEventEventResponse> response = test.getRegisterEventEvents(receipt);
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" register account success => account: %s, value: %s \n", account,
							amount);
				} else if(response.get(0).ret.compareTo(new BigInteger("1")) == 0) {
					System.out.printf(" register account failed, ret code is %s \n",
							response.get(0).ret.toString());
				}else {
					System.out.printf(" Account already exists!\n");
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" registerTestAccount exception, error message is {}", e.getMessage());
			System.out.printf(" register account failed, error message is %s\n", e.getMessage());
		}
	}
	public void sendTxTestAccount(String fromTestAccount, String toTestAccount, BigInteger amount,BigInteger beginTime,BigInteger endTime) {
		try {
			String contractAddress = loadTestAddr();
			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = test.sendTxs(fromTestAccount, toTestAccount, amount,beginTime,endTime).send();
			List<SendTxEventEventResponse> response = test.getSendTxEventEvents(receipt);
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" send transaction success => from_account: %s, to_account: %s, amount: %s \nReceipt beginTime: %s  endTime: %s\n",
							fromTestAccount, toTestAccount, amount,beginTime.toString(),endTime.toString());
				} else if(response.get(0).ret.compareTo(new BigInteger("1")) == 0){
					System.out.printf(" send transaction failed.Please try again.\n ret code is %s \n",
							response.get(0).ret.toString());
				}else {
					System.out.printf(" send transaction failed, Please make sure both accounts are members of the system.\nret code is %s \n",
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" sendTxTestAccount exception, error message is {}", e.getMessage());
			System.out.printf(" send transaction failed, error message is %s\n", e.getMessage());
		}
	}
	public void transferTest(String sourceAccount, String fromTestAccount, String toTestAccount, BigInteger amount) {
		try {
			String contractAddress = loadTestAddr();
			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = test.transfer(sourceAccount,fromTestAccount, toTestAccount, amount).send();
			List<TransferEventEventResponse> response = test.getTransferEventEvents(receipt);
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" transfer success => from_test: %s, to_test: %s, amount: %s \n",
							fromTestAccount, toTestAccount, amount);
				} else if (response.get(0).ret.compareTo(new BigInteger("1")) == 0){
					System.out.printf(" transfer account failed:Money in the receipts not enough to transfer!\n ret code is %s \n",
							response.get(0).ret.toString());
				}else {
					System.out.printf(" transfer failed, Please make sure all the accounts are members of the system.\nret code is %s \n",
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" transferTest exception, error message is {}", e.getMessage());
			System.out.printf(" transfer failed, error message is %s\n", e.getMessage());
		}
	}

	public void financeTestAccount(String account, BigInteger rcptID) {
		try {
			String contractAddress = loadTestAddr();
			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = test.financing(account, rcptID).send();
			Tuple4<BigInteger, String,String,BigInteger> amount = test.findReceiptByID(rcptID).send();
			List<FinanceEventEventResponse> response = test.getFinanceEventEvents(receipt);
			
			if (!response.isEmpty() ) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0
					&& amount.getValue1().compareTo(new BigInteger("0")) == 0){
					System.out.printf(" finance success => account: %s, rcptID: %s, amount: %s \n",
							account, rcptID.toString(), amount.getValue4());
				} else if (response.get(0).ret.compareTo(new BigInteger("1")) == 0){
					System.out.printf(" finance failed.Please try again!\n ret code is %s \n",
							response.get(0).ret.toString());
				}else if (response.get(0).ret.compareTo(new BigInteger("-1")) == 0){
					System.out.printf(" finance failed: account not found!.\nret code is %s \n",
							response.get(0).ret.toString());
				}else if (response.get(0).ret.compareTo(new BigInteger("-2")) ==0){
					System.out.printf(" finance failed: receipt not found!\nret code is %s \n",
							response.get(0).ret.toString());
				}else if (response.get(0).ret.compareTo(new BigInteger("-3")) == 0){
					System.out.printf(" finance failed: The receiver of the receipt is not the account.\nret code is %s \n",
							response.get(0).ret.toString());
				}else if (response.get(0).ret.compareTo(new BigInteger("-4")) == 0){
					System.out.printf(" finance failed: Disabled receipt!\nret code is %s \n",
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" financeTestAccount exception, error message is {}", e.getMessage());
			System.out.printf(" finance failed, error message is %s\n", e.getMessage());
		}
	}

	public void payTestAccount(String fromTestAccount, String toTestAccount, BigInteger amount) {
		try {
			String contractAddress = loadTestAddr();
			Test test = Test.load(contractAddress, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
			TransactionReceipt receipt = test.pay(fromTestAccount, toTestAccount, amount).send();
			List<PayEventEventResponse> response = test.getPayEventEvents(receipt);
			if (!response.isEmpty()) {
				if (response.get(0).ret.compareTo(new BigInteger("0")) == 0) {
					System.out.printf(" pay receipts success => All receipts paid! No Money left! Total Amount: %s \n",
						amount);
				} else if (response.get(0).ret.compareTo(new BigInteger("1")) == 0){
					System.out.printf(" pay receipts success => All receipts paid! Total Amount: %s\n",
							response.get(0).amount.toString());
				} else {
					System.out.printf(" pay receipts failed, Please make sure both accounts are members of the system.\nret code is %s \n",
							response.get(0).ret.toString());
				}
			} else {
				System.out.println(" event log not found, maybe transaction not exec. ");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

			logger.error(" payTestAccount exception, error message is {}", e.getMessage());
			System.out.printf(" pay receipts failed, error message is %s\n", e.getMessage());
		}
	}


	public static void Usage() {
		System.out.println(" Usage:");
		System.out.println("\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient deploy");
		System.out.println("\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient incMoney [account] [amount]");
		System.out.println("\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient decMoney [account] [amount]");
		System.out.println(
				"\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient ifAccountExists [account] ([address])");
		System.out.println(
				"\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient accountID [account]");
		System.out.println(
				"\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient findReceipt [receiptID]");
		System.out.println(
				"\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient register [account] [address] [amount] [role]");
		System.out.println(
				"\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient sendTx [fromAccount] [toAccount] [amount] [beginTime] [endTime]");
		System.out.println(
				"\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient transfer [sourceAccount] [fromAccount] [toAccount] [amount]");
		System.out.println(
				"\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient allReceiptsMoney [fromAccount] [toAccount]");
		System.out.println(
				"\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient finance [account] [receiptID]");
		System.out.println(
				"\t java -cp conf/:lib/*:apps/* org.fisco.bcos.test.client.TestClient pay [FromAccount] [toAccount] [amount]");
		System.exit(0);
	}

	public static void main(String[] args) throws Exception {

		if (args.length < 1) {
			Usage();
		}

		TestClient client = new TestClient();
		client.initialize();

		switch (args[0]) {
		case "deploy":
			client.deployTestAndRecordAddr();
			break;
		case "incMoney":
			if (args.length < 3) {
				Usage();
			}
			client.incCompanyMoneyTest(args[1], new BigInteger(args[2]));
			break;
		case "decMoney":
			if (args.length < 3) {
				Usage();
			}
			client.decCompanyMoneyTest(args[1], new BigInteger(args[2]));
			break;
		case "ifAccountExists":
			if (args.length < 2) {
				Usage();
			}
			if(args.length == 2)
				client.checkTestAccount(args[1]);
			else 
				client.checkTestAccount(args[1], args[2]);
			break;
		case "accountID":
			if (args.length < 2) {
				Usage();
			}
			client.findTestAccountID(args[1]);
			break;
		case "findReceipt":
			if (args.length < 2) {
				Usage();
			}
			client.findTestReceiptByID(new BigInteger(args[1]));
			break;	
		case "register":
			if (args.length < 5) {
				Usage();
			}
			client.registerTestAccount(args[1], args[2], new BigInteger(args[3]),new BigInteger(args[4]));
			break;
		case "sendTx":
			if (args.length < 6) {
				Usage();
			}
			client.sendTxTestAccount(args[1], args[2], new BigInteger(args[3]),new BigInteger(args[4]),new BigInteger(args[5]));
			break;
		case "transfer":
			if (args.length < 5) {
				Usage();
			}
			client.transferTest(args[1], args[2], args[3],new BigInteger(args[4]));
			break;
		case "allReceiptsMoney":
			if (args.length < 3) {
				Usage();
			}
			client.allMoneyTestAccount(args[1], args[2]);
			break;
		case "finance":
			if (args.length < 3) {
				Usage();
			}
			client.financeTestAccount(args[1], new BigInteger(args[2]));
			break;
		case "pay":
			if (args.length < 4) {
				Usage();
			}
			client.payTestAccount(args[1], args[2], new BigInteger(args[3]));
			break;
		default: {
			Usage();
		}
		}

		System.exit(0);
	}
}


