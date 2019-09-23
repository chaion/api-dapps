package com.chaion.makkiiserver.blockchain.eth;

import com.chaion.makkiiserver.modules.token.EthToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This provider is to provide self-deployed token on pokket's private eth chain.
 */
@Service
public class PokketEthTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(PokketEthTokenProvider.class);

    private List<EthToken> tokens = new ArrayList<>();

    /**
     * read from token txt and load into token list.
     */
    public PokketEthTokenProvider() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("pokket_tokens.txt");

        BufferedReader stream = null;
        try {
            stream = new BufferedReader(new InputStreamReader(classPathResource.getInputStream()));
            String symbol;
            String line;
            EthToken et = null;
            while (true) {
                if (!((line = stream.readLine()) != null)) break;
                if (line.startsWith("[")) {
                    if (et != null) {
                        et.setContractType("ERC20");
                        tokens.add(et);
                    }
                    symbol = line.replace("[", "").replace("]", "");
                    et = new EthToken();
                    et.setSymbol(symbol);
                    et.setName(symbol);
                } else if (line.startsWith("address=")) {
                    if (et!=null) {
                        String address = line.split("=")[1];
                        et.setContractAddr(address);
                    }
                } else if (line.startsWith("decimals=")) {
                    if (et!=null) {
                        String decimals = line.split("=")[1];
                        et.setTokenDecimal(decimals);
                    }
                } else if (line.startsWith("totalSupply")) {
                    if (et!=null) {
                        String totalSupply = line.split("=")[1];
                        et.setTotalSupply(totalSupply);
                    }
                }
            }
            logger.info("initialize pokket tokens...");
            dump();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void dump() {
        for (EthToken t : tokens) {
            logger.info(t.getSymbol() + ",addr:" + t.getContractAddr() +
                    ",decimal:" + t.getTokenDecimal() + ",totalSupply:" + t.getTotalSupply());
        }
    }

    public List<EthToken> getTokens() {
        return tokens;
    }

    public List<EthToken> getBySymbol(String token) {
        List<EthToken> result = new ArrayList<>();
        for (EthToken t : tokens) {
            if (t.getSymbol().equalsIgnoreCase(token)) {
                result.add(t);
            }
        }
        return result;
    }

    public List<EthToken> searchTokens(String keyword) {
        List<EthToken> result = new ArrayList<>();
        for (EthToken t : tokens) {
            if (t.getContractAddr().equalsIgnoreCase(keyword)) {
                result.add(t);
            } else if (t.getName().equalsIgnoreCase(keyword)) {
                result.add(t);
            } else if (t.getSymbol().equalsIgnoreCase(keyword)) {
                result.add(t);
            }
        }
        return result;
    }
}
