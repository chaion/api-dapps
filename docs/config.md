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
    "zh": ["ChainNews", "CoinVoice"],
    "en": []
}
```
##### mongo init script
```
db.moduleConfig.insert({"moduleName": "News", "enabled": false})
```
