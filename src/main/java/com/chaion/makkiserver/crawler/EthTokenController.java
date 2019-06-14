package com.chaion.makkiserver.crawler;

import com.chaion.makkiserver.exception.CodedErrorEnum;
import com.chaion.makkiserver.exception.CodedException;
import com.chaion.makkiserver.file.StorageException;
import com.chaion.makkiserver.file.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EthTokenController {
    private static final Logger logger = LoggerFactory.getLogger(EthTokenController.class);

    @Autowired
    StorageService storageService;

    @Autowired
    EthTokenRepository repo;

    @GetMapping("/token/eth/search")
    public List<EthToken> getEthToken(@RequestParam(value = "keyword", required = false) String keyword,
                                      @RequestParam(value = "offset") int offset,
                                      @RequestParam(value = "limit") int limit) {
        Pageable page = PageRequest.of(offset, limit);
        if (keyword != null) {
            if (keyword.matches("^0x[0-9a-fA-F]{40}")) {
                return repo.findByContractAddress(keyword, page);
            }
            return repo.findByName(keyword, page);
        }
        return repo.findAll(page).getContent();
    }

    @GetMapping("/token/eth/img")
    public ResponseEntity<Resource> getTokenIcon(@RequestParam(value = "contractAddress") String contractAddress) {
        if (!contractAddress.startsWith("0x") && !contractAddress.startsWith("0X")) {
            contractAddress = "0x" + contractAddress;
        }

        Resource file = null;
        try {
            file = storageService.loadAsResource("img/eth/" + contractAddress.toLowerCase() + ".png");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/png")
                    .body(file);
        } catch (StorageException e) {
            logger.error("load file failed: ", e.getMessage());
            try {
                file = storageService.loadAsResource("img/eth/default_erc20.png");
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, "image/png")
                        .body(file);
            } catch (StorageException e1) {
                logger.error("load file failed: ", e1.getMessage());
                throw new CodedException(CodedErrorEnum.ERROR_FILE_NOT_FOUND);
            }
        }

    }
}
