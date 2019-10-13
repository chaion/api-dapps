package com.chaion.makkiiserver.blockchain.aion;

import com.chaion.makkiiserver.blockchain.BlockchainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.aion.AionConstants;
import org.web3j.aion.VirtualMachine;
import org.web3j.aion.crypto.Ed25519KeyPair;
import org.web3j.aion.protocol.Aion;
import org.web3j.aion.tx.AionTransactionManager;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;

@Service
public class AionService {

    private static final Long DEFAULT_GAS_PRICE = 20000000000L;

    private static final Logger logger = LoggerFactory.getLogger(AionService.class);

    private Aion aion;

    public AionService(@Value("${blockchain.aion.apiserver}") String rpcServer) {
        aion = Aion.build(new HttpService(rpcServer));
    }

    /**
     * Send transaction
     *
     * @param privateKey
     * @param toAddress
     * @param amount
     * @return
     * @throws BlockchainException
     */
    public String sendTransaction(String privateKey, String toAddress, BigInteger amount) throws BlockchainException {
        logger.info("[aion][sendTransaction] transfer {} aion to {}", amount, toAddress);
        AionTransactionManager manager = new AionTransactionManager(aion,
                new Ed25519KeyPair(privateKey), VirtualMachine.FVM);
        BigInteger gasPrice = BigInteger.valueOf(DEFAULT_GAS_PRICE);
        BigInteger gasLimit = BigInteger.valueOf(AionConstants.NRG_TRANSACTION_MIN);
        try {
            EthSendTransaction tx = manager.sendTransaction(gasPrice, gasLimit, toAddress, "0x", amount, Boolean.FALSE);
            if (tx.hasError()) {
                Response.Error error = tx.getError();
                logger.error("[aion][sendTransaction] failed: ", error);
                throw new BlockchainException(String.format("error code: {}, message: {}, data: {}",
                        error.getCode(), error.getMessage(), error.getData()));
            }
            String txHash = tx.getTransactionHash();
            logger.info("[aion][sendTransaction] txId=" + txHash);
            return txHash;
        } catch (Exception e) {
            logger.error("[aion][sendTransaction] exception: ", e);
            throw new BlockchainException(e.getMessage());
        }
    }
}
