package com.chaion.makkiiserver.controller;

import com.chaion.makkiiserver.model.ATSToken;
import com.chaion.makkiiserver.model.EthToken;
import com.chaion.makkiiserver.repository.AionTokenRepository;
import com.chaion.makkiiserver.repository.EthTokenRepository;
import com.chaion.makkiiserver.exception.CodedErrorEnum;
import com.chaion.makkiiserver.exception.CodedException;
import com.chaion.makkiiserver.repository.file.StorageException;
import com.chaion.makkiiserver.repository.file.StorageService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Api(value="Token Management APIs", description="List and search tokens, current support tokens are eth and ats.")
@RestController
@RequestMapping("token")
public class TokenController {
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    StorageService storageService;

    @Autowired
    EthTokenRepository ethRepo;

    @Autowired
    AionTokenRepository aionRepo;

    @Autowired
    RestTemplate rest;

    @Value("${app_env}")
    String appEnv;

    // ----------------------------------------------------------------
    // ----------------------- Aion Token -----------------------------
    // ----------------------------------------------------------------
    @ApiOperation(value="Get Aion tokens by page",
            response=ATSToken.class,
            produces = "application/json")
    @GetMapping("/aion")
    public List<ATSToken> getAionTokens(
            @ApiParam(required=true, value="start index of tokens", example = "0")
            @RequestParam(value = "offset") int offset,
            @ApiParam(required=true, value="number of tokens to get", example = "20")
            @RequestParam(value = "limit") int limit) {
        Pageable page = PageRequest.of(offset, limit);
        return aionRepo.findAll(page).getContent();
    }

    @ApiOperation(value="Search aion tokens by contract address or coin name",
        response=ATSToken.class,
        produces = "application/json")
    @GetMapping("/aion/search")
    public List<ATSToken> searchAionTokens(
            @ApiParam(value="either address(precise search, case-insensitive, either 0x-prefix or not) " +
                    "or coin name(partial search, case-insensitive).")
            @RequestParam(value="keyword") String keyword) {
        if (keyword.matches("^(0x)?[0-9a-fA-F]{64}$")) {
            keyword = keyword.toLowerCase();
            if (keyword.startsWith("0x") || keyword.startsWith("0X")) {
                keyword = keyword.substring(2);
            }
            return aionRepo.findByContractAddress(keyword);
        }
        return aionRepo.findByName(keyword);
    }

    @ApiOperation(value="Refresh all ATS tokens information. ",
            notes = "This api will crawl aion explorer website and http request may time out.")
    @PostMapping("/aion/refresh")
    public void refreshAionTokens() {
        logger.info("start crawling eth token... env: " + appEnv);
        int totalPages = 1, page = 0, count = 0, size = 25;
        while (page < totalPages) {
            String url;
            if (appEnv.equalsIgnoreCase("prod")) {
                url = "https://mainnet-api.aion.network/aion/dashboard/getTokenList?page=" + page + "&size=" + size;
            } else {
                url = "https://mastery-api.aion.network/aion/dashboard/getTokenList?page=" + page + "&size=" + size;
            }
            logger.info("crawling page " + url);

            ResponseEntity<String> resp = rest.getForEntity(url, String.class);
            String body = resp.getBody();
            JsonObject root = new JsonParser().parse(body).getAsJsonObject();
            JsonObject pageJson = root.get("page").getAsJsonObject();
            totalPages = pageJson.get("totalPages").getAsInt();
            int totalElements = pageJson.get("totalElements").getAsInt();
            logger.info("total elements: " + totalElements);


            JsonArray array = root.get("content").getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                ATSToken token = new ATSToken();

                JsonObject tokenJson = array.get(i).getAsJsonObject();
                token.setSymbol(tokenJson.get("symbol").getAsString());

                String contractAddr = tokenJson.get("contractAddr").getAsString();
                if (!contractAddr.startsWith("0x") && !contractAddr.startsWith("0X")) {
                    contractAddr = "0x" + contractAddr;
                }
                token.setContractAddr(contractAddr.toLowerCase());

                token.setTotalSupply(tokenJson.get("totalSupply").getAsString());
                token.setTokenDecimal(tokenJson.get("tokenDecimal").getAsString());
                token.setLiquidSupply(tokenJson.get("liquidSupply").getAsString());

                String creatorAddress = tokenJson.get("creatorAddress").getAsString();
                if (!creatorAddress.startsWith("0x") && !creatorAddress.startsWith("0X")) {
                    creatorAddress = "0x" + creatorAddress.toLowerCase();
                }
                token.setCreatorAddress(creatorAddress);

                String transactionHash = tokenJson.get("transactionHash").getAsString();
                if (!transactionHash.startsWith("0x") && !transactionHash.startsWith("0X")) {
                    transactionHash = "0x" + transactionHash.toLowerCase();
                }
                token.setTransactionHash(transactionHash);
                token.setGranularity(tokenJson.get("granularity").getAsString());
                token.setCreationTimestamp(tokenJson.get("creationTimestamp").getAsString());
                token.setName(tokenJson.get("name").getAsString());

                logger.info("" + token);
                aionRepo.save(token);

                count++;
            }

            stupidWait();
            page++;
        }

        logger.info("finish crawling aion tokens: total " + count);
    }

    // ----------------------------------------------------------------
    // ----------------------- ETH Token -----------------------------
    // ----------------------------------------------------------------
    @ApiOperation(value="Get/Search ERC20 tokens by page",
            notes = "if keyword parameter is not present, this api will return all eth tokens by page; " +
                    "if it is present and is an address format, this api will do a full match against address; " +
                    "else this api will do a partial match against coin name",
            response = EthToken.class,
            produces = "application/json")
    @GetMapping("/eth/search")
    public List<EthToken> getEthTokens(
            @ApiParam(value="either address(precise search, case-insensitive, either 0x-prefix or not) " +
                    "or coin name(partial search, case-insensitive).")
            @RequestParam(value = "keyword", required = false) String keyword,
            @ApiParam(required=true, value="start index of tokens", example = "0")
            @RequestParam(value = "offset") int offset,
            @ApiParam(required=true, value="number of tokens to get", example = "20")
            @RequestParam(value = "limit") int limit) {
        Pageable page = PageRequest.of(offset, limit);
        if (keyword != null) {
            if (keyword.matches("^0x[0-9a-fA-F]{40}")) {
                return ethRepo.findByContractAddress(keyword, page);
            }
            return ethRepo.findByName(keyword, page);
        }
        return ethRepo.findAll(page).getContent();
    }

    @ApiOperation(value="Get eth token icon")
    @GetMapping("/eth/img")
    public ResponseEntity<Resource> getEthTokenIcon(
            @ApiParam(required=true, value="token contract address, case-insensitive, either 0x-prefix or not")
            @RequestParam(value = "contractAddress") String contractAddress) {
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

    @ApiOperation(value="Refresh all ERC-20 tokens information. ",
            notes = "This api will crawl etherscan.io website and http request may time out")
    @PostMapping("/eth/refresh")
    public void refreshEthTokenRepo() {
        logger.info("start crawling eth token...");
        ethRepo.deleteAll();

        int i = 1;
        while (true) {
            int totalPages = fetchEthTokenListPage(i++);
            if (i > totalPages) break;
        }
        logger.info("finish crawling eth tokens");
    }

    private int fetchEthTokenListPage(int page) {
        stupidWait();

        logger.info("crawl eth token page " + page);
        String html = rest.getForObject("https://etherscan.io/tokens?p=" + page, String.class);
        Document root = Jsoup.parse(html);
        String totalPages = root.selectFirst("#ContentPlaceHolder1_divpagingpanel > nav > ul > li:nth-child(3) > span > strong:nth-child(2)").text();
        Elements tbody = root.getElementsByTag("tbody");
        Elements trs = tbody.get(0).getElementsByTag("tr");
        for (Element tr : trs) {
            EthToken t = new EthToken();

            Elements imgs = tr.getElementsByTag("img");
            t.setImagePath(imgs.get(0).attr("src"));

            Elements as = tr.getElementsByTag("a");
            String link = as.get(0).attr("href");

            String intro = tr.getElementsByClass("media-body").get(0).getElementsByTag("p").text();
            t.setDesc(intro);

            fetchEthTokenDetailPage(t, link);
        }
        return Integer.parseInt(totalPages);
    }

    private void fetchEthTokenDetailPage(EthToken t, String link) {
        stupidWait();

        String html = rest.getForObject("https://etherscan.io/" + link, String.class);
        Document root = Jsoup.parse(html);
        String name = root.getElementsByClass("media-body").get(0).getElementsByTag("span").text();
        t.setName(name);
        logger.info("crawling eth token " + name + "...");

        Element div = root.selectFirst("#ContentPlaceHolder1_divSummary > div.row.mb-4 > div.col-md-6.mb-3.mb-md-0 > div > div.card-body > div.row.align-items-center > div.col-md-8.font-weight-medium");

        String[] totalSupply = div.text().split(" ");
        t.setTotalSupply(totalSupply[0]);
        t.setSymbol(totalSupply[1]);

        String erc = root.selectFirst("#ContentPlaceHolder1_divSummary > div.row.mb-4 > div.col-md-6.mb-3.mb-md-0 > div > div.card-header.d-flex.justify-content-between.align-items-center > h2 > span").text();
        t.setContractType(erc.replace("[", "").replace("]", ""));

        String contractAddress = root.selectFirst("#ContentPlaceHolder1_divSummary > div.row.mb-4 > div:nth-child(2) > div > div.card-body > div.row.align-items-center > div.col-md-8 > div").getElementsByTag("a").get(0).text();
        t.setContractAddr(contractAddress);

        String decimals = root.selectFirst("#ContentPlaceHolder1_trDecimals > div > div.col-md-8").text();
        t.setTokenDecimal(decimals);

        Element e = root.selectFirst("#ContentPlaceHolder1_tr_officialsite_1 > div > div.col-md-8 > a");
        if (e != null) {
            String site = e.attr("href");
            t.setOfficialSite(site);
        }

        ethRepo.save(t);
    }

    /**
     * add useless wait to avoid etherscan or aion explorer detection
     */
    private void stupidWait() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }
}
