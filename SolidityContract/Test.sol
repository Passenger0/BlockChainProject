pragma solidity ^0.4.24;

contract Test {
    //string process;

    struct Company{
        uint256 id;//公司ID
        string name;//姓名
        string address_;//地址
        uint256 money;//总资产
        uint256 role; // 0 for bank,1 for company
        uint256 registed;//是否注册
    }

    struct Receipt{
        uint256 id;//收据id
        string from;//收据欠款方
        string to;//收据收款方
        uint256 amount;//收据金额
       // bool status; // 是否用于融资
        uint256 begin;//收据开始日期
        uint256 end; //收据到期日
        //bool finished;    
    }

    struct Finance{
        uint256 finanID;//融资ID
        string company;//融资企业名，
        uint256 rcptID;//融资所用收据ID
        uint256 amount;//融资金额。
    }

    uint256 receiptID;
    uint256 companyID;
    uint256 financeID;

    Receipt[] ReceiptList;
    Company[] CompanyList;
    Finance[] FinanceList;
    uint256 receiptCount;
    uint256 companyCount;
    uint256 financeCount;


   // mapping(string=>Company) private CompanyList;
    //mapping(string=>mapping(string=>Receipt)) private ReceiptList;
    event RegisterEvent(int256 ret, string name,string address_, uint256 asset_value,uint256 role_);
    event TransferEvent(int256 ret, string source_account,string from_account, string to_account, uint256 amount);
    event IncMoneyEvent(int256 ret,string company,uint256 amount);//fianl balance
    event DecMoneyEvent(int256 ret,string company,uint256 amount);
    //event AccountExistsEvent(int256 ret,uint256 rcptID);
    //event AccountIDEvent(int256 ret,string name);
    event SendTxEvent(int256 ret,string from,string to,uint256 amount,uint256 begin ,uint256 end);
   // event AllMoneyEvent(int256 ret,);
    event FinanceEvent(int256 ret,string account,uint256 rcptID);
    event PayEvent(int256 ret,string from,string to,uint256 amount);

    constructor() public{
        receiptID = uint256(0);
        companyID = uint256(0);
        financeID = uint256(0);
        receiptCount = companyCount = financeCount = uint256(0);
        registerCompany("bank","bankAddress",12345678,0);
    }
    
    function hashCompareInternal(string a, string b) internal returns (bool) {
        return keccak256(a) == keccak256(b);
    }

    function findCompanyID(string name)public constant returns(int256,uint256){
        /*if(companyCount == uint256(0)){
            return false;
        }*/
        for (uint256 i = 0 ; i < companyCount ; i++){
            if(hashCompareInternal(CompanyList[i].name,name)){
                return (0,CompanyList[i].id);
            }
           
        }
         return (-1,uint256(0));
    }
    function findCompany(string name)public constant returns(int256,uint256){
        /*if(companyCount == uint256(0)){
            return false;
        }*/
        for (uint256 i = 0 ; i < companyCount ; i++){
            if(hashCompareInternal(CompanyList[i].name,name)){

                return (0,CompanyList[i].money);
            }
            //return false;
        }
        return (-1,uint256(0));
    }
    function findCompany(string name,string address_)public constant returns(int256,uint256){
        /*if(companyCount == uint256(0)){
            return false;
        }*/
        for (uint256 i = 0 ; i < companyCount ; i++){
            if(hashCompareInternal(CompanyList[i].name,name)
                 && hashCompareInternal(CompanyList[i].address_ ,address_)){
                return (0,CompanyList[i].money);
            }
            //return false;
        }
        return (-1,uint256(0));
    }
    function registerCompany(string name_,string address_,uint256 money,uint256 role_) public returns(int256){
        int256 ret_code = 0;
        int256 ret= 0;
        uint256 temp_asset_value = 0;

        // 查询账户是否存在
        (ret, temp_asset_value) = findCompany(name_,address_); 

        if(ret != 0){
            Company memory newCompany = Company(++companyID,name_,address_,money,role_,uint256(1));
            uint256 temp = companyCount;
            companyCount = CompanyList.push(newCompany);
            if(companyCount > temp){
                //result = 0;
                ret_code = 0;//details = "Company addition success!";
            }else{
                //result = false;
                ret_code = 1;//details = "Company addition failed!";
            }
        }else {
            ret_code = -1;//details = "Company already exists!";
        }
            
        emit RegisterEvent(ret_code,name_,address_,money,role_);   
        return ret_code;
    }

    function incCompanyMoney(string company,uint256 amount) public returns(int256){
        int256 ret_code;
        int256 ret= 0;
        uint256 temp_asset_value = 0;

        // 查询账户是否存在
        (ret, temp_asset_value) = findCompany(company);
        if(ret == 0){
            for (uint256 i = 0 ; i < companyCount ; i++){
                if(hashCompareInternal(CompanyList[i].name,company)){
                    uint256 temp = CompanyList[i].money;

                    CompanyList[i].money += amount;
                    //return (0,CompanyList[i].money);

                    if(temp >= CompanyList[i].money){
                        //result = false;
                        ret_code = 1;//details = "Operation failed!";
                    }
                    else {
                        //result = true;
                        ret_code = 0;//details = "Operation success!";
                        temp_asset_value = CompanyList[i].money;
                    }
                }
                //return false;
            }

        }else {
            ret_code = -1;//details = "Company not found!"
        }
        
        emit IncMoneyEvent(ret_code,company,temp_asset_value);
        return ret_code;
    }
    function decCompanyMoney(string company,uint256 amount) public returns(int256){
        int256 ret_code;
        int256 ret= 0;
        uint256 temp_asset_value = 0;

        // 查询账户是否存在
        (ret, temp_asset_value) = findCompany(company);
        
        if(ret == 0){
            if(temp_asset_value >= amount){
                for (uint256 i = 0 ; i < companyCount ; i++){
                    if(hashCompareInternal(CompanyList[i].name,company)){

                        uint256 temp = CompanyList[i].money;

                        CompanyList[i].money -= amount;
                        //return (0,CompanyList[i].money);

                        if(temp <= CompanyList[i].money){
                            //result = false;
                            ret_code = 1;//details = "Operation failed!";
                        }
                        else {
                            //result = true;
                            ret_code = 0;//details = "Operation success!";
                            temp_asset_value = CompanyList[i].money;
                        }
                    }
                    //return false;
                }

            }else {
                ret_code = -2;//details = "Money not enough!"
            }
        }else {
            ret_code = -1;//details = "Company not found!"
        }
        
        emit DecMoneyEvent(ret_code,company,temp_asset_value);//final balance
        return ret_code;
}
    //1. 购置物资/资产，签订应收账款收据
    function sendTxs(string from ,string to,uint256 amount,uint256 begin,uint256 end) public returns(int256){
        int256 ret_code = 0;
        int256 ret= 0;
        uint256 temp_asset_value = 0;


        // 查询账户是否存在
        (ret, temp_asset_value) = findCompany(from);

        if(ret == 0){
            (ret, temp_asset_value) = findCompany(to);
            if(ret == 0){
                Receipt memory newReceipt = Receipt(++receiptID,from,to,amount,begin,end);
                uint256 temp = receiptCount;
                receiptCount = ReceiptList.push(newReceipt);

                if(temp < receiptCount){
                    //result = true;
                    ret_code = 0;//details = "Sendtransaction success!";
                }else {
                    //result = true;
                    ret_code = 1;//details = "Sendtransaction failed!";
                }
            }else {
                ret_code = -2;//"The receiver is not a member of the system!";
            }
        } else {
            ret_code = -1;//details = "The debtor is not a member of the system!";
        }

        emit SendTxEvent(ret_code,from,to,amount,begin,end);
        return ret_code;
       /* //bool status = ! CompanyList[from].role;
        if(findCompanyID(from) == uint256(0)){
            result = false;
            details = "The debtor is not a member of the system!";
        }
        else if(findCompanyID(to) == uint256(0)){
            result = false;
            details = "The receiver is not a member of the system!";
        }
       else {
            Receipt memory newReceipt = Receipt(++receiptID,from,to,amount,begin,end);
            uint256 temp = receiptCount;
            receiptCount = ReceiptList.push(newReceipt);
            if(temp < receiptCount){
                result = true;
                details = "Sendtransaction success!";
            }else {
                result = true;
                details = "Sendtransaction failed!";
            }
       }*/
        
    }

    function allReceiptsMoney(string from,string to)public constant returns(int256,uint256){ 
        int256 ret_code;
        int256 ret= 0;
        uint256 temp_asset_value = 0;

        uint256 amount = uint256(0);
        // 查询账户是否存在
        (ret, temp_asset_value) = findCompany(from);

        if(ret == 0){
            (ret, temp_asset_value) = findCompany(to);
            if(ret == 0){
                //uint256 amount = uint256(0);
                for (uint256 i = uint256(0) ; i < receiptCount ; i++){
                    if(hashCompareInternal(ReceiptList[i].from ,from)
                     && hashCompareInternal(ReceiptList[i].to ,to)){
                        amount += ReceiptList[i].amount;
                    }
                }
                ret_code = 0;
            }else {
                ret_code = -2;//"The receiver is not a member of the system!";
            }
        } else {
            ret_code = -1;//details = "The debtor is not a member of the system!";
        }
        return (ret_code,amount);
        
    }
    //2. 收据账款转移
    function transfer(string source,string from,string to,uint256 amount) public returns(int256){

        int256 ret_code;
        int256 ret= 0;
        uint256 temp_asset_value = 0;

        uint256 allMoney;
        // 查询账户是否存在及收据总款


        (ret, allMoney) = allReceiptsMoney(source,from);
        if(ret == 0){
            (ret, temp_asset_value) = findCompany(to);
            if(ret == 0){
                if(allMoney >= amount){
                uint256 amount_to_transfer = amount;
                for (uint256 i = uint256(0)  ; i < receiptCount ; i++){
                    if(hashCompareInternal(ReceiptList[i].from ,source)
                         && hashCompareInternal(ReceiptList[i].to,from)){
                        if(ReceiptList[i].amount <= amount_to_transfer){
                            amount_to_transfer -= ReceiptList[i].amount;
                            ReceiptList[i].to = to;
                            //delete ReceiptList[i];
                        }else {
                            uint256 begin = ReceiptList[i].begin;
                            uint256 end = ReceiptList[i].end;
                            Receipt memory newReceipt = Receipt(++receiptID,source,to,amount_to_transfer,begin,end);
                            receiptCount++;

                            ReceiptList[i].amount -= amount_to_transfer;
                            amount_to_transfer = 0;
                        }
                        if(amount_to_transfer == 0){
                            ret_code = 0;
                            break;
                        }
                    }
                    }
                }else{
                    ret_code = 1;//details = "Money in the receipts not enough to transfer!";
                }
            }
            else {
                ret_code = -3;//to_account not found
            }
        }else {
            ret_code = ret;//-1 or -2 (source / from not exists)
        }

        emit TransferEvent(ret_code,source,from,to,amount);
        return ret_code;
/*
        if(findCompanyID(from) == uint256(0) 
            || findCompanyID(to) == uint256(0) 
            || findCompanyID(source) == uint256(0)){
            result = false;
            details = "Please make sure all the companies are the member of the system!";
        }else {
            uint256 allMoney = allReceiptsMoney(source,from);
            if(allMoney < amount){
                result = false;
                details = "Money in the receipts not enough to transfer!";
            }
            else {
                uint256 amount_to_transfer = amount;
                for (uint256 i = uint256(0)  ; i < receiptCount ; i++){
                    if(hashCompareInternal(ReceiptList[i].from ,source)
                         && hashCompareInternal(ReceiptList[i].to,from)){
                        if(ReceiptList[i].amount <= amount_to_transfer){
                            amount_to_transfer -= ReceiptList[i].amount;
                            ReceiptList[i].to = to;
                            //delete ReceiptList[i];
                        }else {
                            uint256 begin = ReceiptList[i].begin;
                            uint256 end = ReceiptList[i].end;
                            Receipt memory newReceipt = Receipt(++receiptID,source,to,amount_to_transfer,begin,end);
                            receiptCount++;

                            ReceiptList[i].amount -= amount_to_transfer;
                            amount_to_transfer = 0;
                        }
                        if(amount_to_transfer == 0){
                                break;
                        }
                    }
                }
                result = true;
                details = "Transfer success!";
            }
        }   */ 
    }

    function findReceiptByID(uint256 id)view public returns(int256,string,string,uint256){
        int256 ret_code;
        if(id > receiptID){
            ret_code = -1;
        }
        else {
            for (uint256 i = uint256(0); i < receiptCount ; i++){
                if(ReceiptList[i].id == id){
                    /*newReceipt.id = ReceiptList[i].id;
                    newReceipt.from = ReceiptList[i].from;
                    newReceipt.to = ReceiptList[i].to;
                    newReceipt.amount = ReceiptList[i].amount;
                    newReceipt.begin = ReceiptList[i].begin;
                    newReceipt.end = ReceiptList[i].end;*/
                    ret_code = 0;
                    return (ret_code,ReceiptList[i].from,ReceiptList[i].to,ReceiptList[i].amount);
                }
            }
            return (ret_code,"","",uint256(0));
        }
        //return newReceipt;
    }
    //3. 融资
    function financing(string company,uint256 rcptID) public  returns(int256,uint256){
        int256 ret_code;
        int256 ret= 0;
        uint256 temp_asset_value = 0;

        uint256 amount;
       

        (ret, temp_asset_value) = findCompany(company);
        if(ret == 0){
            string memory from;
            string memory to;
            (ret,from,to,amount) = findReceiptByID(rcptID);
            if(ret == 0){
                if(hashCompareInternal(to,company)){
                    if(amount > uint256(0)){
                        Finance memory finance = Finance(++financeID,company,rcptID,amount);
                        financeCount = FinanceList.push(finance);

                        if(financeCount == financeID){
                            ret_code = 0;//details = "Receipt financing success!";
                        }else {
                            ret_code = 1;//details = "Receipt financing failed!"
                        }
                    }else {
                        ret_code = -4;//details = "Disabled receipt!"
                    }
                }else {
                    ret_code = -3;//details = "The receiver of the receipt is not the company!"
                }
            }else {
                ret_code = -2;//"Receipt not found!!";
            }
        }else {
            ret_code = -1;//details ="company not found!"
        }

        emit FinanceEvent(ret_code,company,rcptID);

        return (ret_code,amount);
      /*  if(findCompanyID(company) == uint256(0)){
            result = false;
            details = "The company is not a member of the system!";
        }else{
            uint256 amount;
            string memory to;
            (to,amount)  = findReceiptByID(rcptID);
            if(amount > uint256(0)){
                if(hashCompareInternal(to,company)){
                    Finance memory finance = Finance(++financeID,company,rcptID,amount);
                    financeCount = FinanceList.push(finance);

                    if(financeCount == financeID){
                        ret_code = 0;//details = "Receipt financing success!";
                    }else {
                        ret_code = 1;//details = "Receipt financing failed!"
                    }
                }
                else {
                    //result = false;
                    ret_code = -1;//details = "The receiver of the receipt is not the company!";
                }
            }
            else {
                result = false;
                details = "Disabled receipt.The receipt has been payed!";
            }   
        }*/
    }

    //4.支付收据款项
    function pay(string from,string to,uint256 amount)public returns(int256,uint256){
        
        int256 ret_code;
        int256 ret= 0;
        uint256 temp_asset_value = 0;

        int256 inc;
        int256 dec;

        uint256 leftAmount = amount;
       
        (ret, temp_asset_value) = findCompany(from);
        if(ret == 0){
            (ret, temp_asset_value) = findCompany(to);
            if(ret == 0){
                for (uint256 i = 0; i < receiptCount ; i++){
                    if(hashCompareInternal(ReceiptList[i].from,from) 
                        && hashCompareInternal(ReceiptList[i].to,to) 
                        && ReceiptList[i].amount > uint256(0)){

                        if(ReceiptList[i].amount < leftAmount){
                            
                            leftAmount -= ReceiptList[i].amount;
                            dec = decCompanyMoney(from,ReceiptList[i].amount);
                            inc = incCompanyMoney(to,ReceiptList[i].amount);
                            if(dec != 0 || inc != 0){

                            }
                            ReceiptList[i].amount = 0;
                        }
                        else {

                            ReceiptList[i].amount -= leftAmount;
                            decCompanyMoney(from,leftAmount);
                            incCompanyMoney(to,leftAmount);
                            leftAmount = 0;
                            ret_code = 0;//details = "All receipt paid!No Money Left"
                            break;
                        }
                    }
                }
                if(leftAmount != 0 ){
                    ret_code = 1;//details = "All receipt paid!Money Left"
                }

            }else {
                ret_code = -2;//details = "to_account not found!"
            }
        }else {
            ret_code = -1;//details = "from_account not found!"
        }

        emit PayEvent(ret_code,from,to,amount - leftAmount);
        return (ret_code,leftAmount);

/*
        uint256 leftAmount = amount;
        for (uint256 i = 0; i < receiptCount ; i++){
            if(hashCompareInternal(ReceiptList[i].from,from) 
                && hashCompareInternal(ReceiptList[i].to,to) 
                && ReceiptList[i].amount > uint256(0)){

                if(ReceiptList[i].amount <= leftAmount){
                    
                    leftAmount -= ReceiptList[i].amount;
                    ReceiptList[i].amount = 0;
                }
                else {
                    ReceiptList[i].amount -= leftAmount;
                    leftAmount = 0;
                }
            }
        }
        return true;*/
    }
}