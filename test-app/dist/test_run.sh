#!/bin/bash 

function usage() 
{
    echo " Usage : "
    echo "   bash test_run.sh deploy"
    echo "   bash test_run.sh register [account] [address] [amount] [role] "
    echo "   bash test_run.sh incMoney [account] [amount] "
    echo "   bash test_run.sh decMoney [account] [amount] "
    echo "   bash test_run.sh ifAccountExists [account] ([address]) "
    echo "   bash test_run.sh accountID [account] "
    echo "   bash test_run.sh sendTx [fromAccount] [toAccount] [amount] [beginTime] [endTime] "
    echo "   bash test_run.sh findReceipt [receiptID] "
    echo "   bash test_run.sh transfer [sourceAccount] [fromAccount] [toAccount] [amount] "
    echo "   bash test_run.sh allReceiptsMoney [fromAccount] [toAccount] "
    echo "   bash test_run.sh finance [account] [receiptID] "
    echo "   bash test_run.sh pay [FromAccount] [toAccount] [amount] "
    echo " "
    echo " "
    echo "examples : "
    echo "   bash test_run.sh deploy "
    echo "   bash test_run.sh register  Account0  account0Address 10000000 1 "
    echo "   bash test_run.sh incMoney Account0 10000 "
    echo "   bash test_run.sh decMoney Account0 10000 "
    echo "   bash test_run.sh ifAccountExists  Account1"
    echo "   bash test_run.sh ifAccountExists  Account0 account0Address"
    echo "   bash test_run.sh accountID Account0 "
    echo "   bash test_run.sh sendTx Account0 Account1 100000 20191213 20200101"
    echo "   bash test_run.sh findReceipt 0000001 "
    echo "   bash test_run.sh transfer  Account0 Account1 Account2  10000 "
    echo "   bash test_run.sh allReceiptsMoney Account0 Account1 "
    echo "   bash test_run.sh finance Account1 0000001 "
    echo "   bash test_run.sh pay Account0 Account1 100000"
    exit 0
}

    case $1 in
    deploy)
            [ $# -lt 1 ] && { usage; }
            ;;
    register)
            [ $# -lt 5 ] && { usage; }
            ;;
    incMoney)
            [ $# -lt 3 ] && { usage; }
            ;;
    decMoney)
            [ $# -lt 3 ] && { usage; }
            ;;
    ifAccountExists)
            [ $# -lt 2 ] && { usage; }
            ;;
    accountID)
            [ $# -lt 2 ] && { usage; }
            ;;
    sendTx)
            [ $# -lt 6 ] && { usage; }
            ;;
    findReceipt)
            [ $# -lt 2 ] && { usage; }
            ;;
    transfer)
            [ $# -lt 5 ] && { usage; }
            ;;
    
    allReceiptsMoney)
            [ $# -lt 3 ] && { usage; }
            ;;
    finance)
            [ $# -lt 3 ] && { usage; }
            ;;
    pay)
            [ $# -lt 4 ] && { usage; }
            ;;
    *)
        usage
            ;;
    esac

    java -Djdk.tls.namedGroups="secp256k1" -cp 'apps/*:conf/:lib/*' org.fisco.bcos.asset.client.TestClient $@

