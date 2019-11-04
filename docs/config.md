### BlockchainExplorer
```
{
    "<coin symbol>": "<explorer url>"
}
```

##### sample:
```
{
    "AION": "https://mainnet.aion.network/#/dashboard",
    "ETH": "https://etherscan.io"
}
```
##### mongo init script
```
db.moduleConfig.insert({"moduleName": "BlockchainExplorer", "enabled": false})
```