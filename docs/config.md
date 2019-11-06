### BlockchainExplorer
``` json
{
    "<coin symbol>": "<explorer url>"
}
```

##### sample:
``` json
{
    "AION": "https://mainnet.aion.network/#/dashboard",
    "ETH": "https://etherscan.io"
}
```
##### mongo init script
``` mongo
db.moduleConfig.insert({"moduleName": "BlockchainExplorer", "enabled": false})
```

### News
``` json
{
    "zh": [
        { "name": "ChainNews", "enabled": true}, 
        { "name": "CoinVoice", "feed":"http://www.coinvoice.cn/feed", "enabled": true} 
    ],
    "en": [
        { "name": "UTB", "feed": "https://usethebitcoin.com/feed/", "enabled": true},
        { "name": "Cointelegraph", "feed": "https://cointelegraph.com/rss", "enabled": true},
        { "name": "Bitcoin.com", "feed": "https://news.bitcoin.com/feed/", "enabled": true}
    ]
}
```
##### mongo init script
```
db.moduleConfig.insert({"moduleName": "News", "enabled": false})
```

### Aion Staking
##### mongo init script
```
db.moduleConfig.insert({"moduleName": "AionStaking", "enabled": false})
```
