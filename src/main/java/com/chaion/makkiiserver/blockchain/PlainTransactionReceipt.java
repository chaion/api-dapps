package com.chaion.makkiiserver.blockchain;

import java.util.List;

import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public class PlainTransactionReceipt extends TransactionReceipt {
    private String transactionHash;
    private String transactionIndex;
    private String blockHash;
    private String blockNumber;
    private String cumulativeGasUsed;
    private String gasUsed;
    private String contractAddress;
    private String root;
    private String from;
    private String to;
    private List<Log> logs;
    private String logsBloom;

    public PlainTransactionReceipt() {
    }

    public PlainTransactionReceipt(String transactionHash, String transactionIndex, String blockHash, String blockNumber, String cumulativeGasUsed, String gasUsed, String contractAddress, String root, String status, String from, String to, List<Log> logs, String logsBloom) {
        this.transactionHash = transactionHash;
        this.transactionIndex = transactionIndex;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.cumulativeGasUsed = cumulativeGasUsed;
        this.gasUsed = gasUsed;
        this.contractAddress = contractAddress;
        this.root = root;
        setStatus(status);
        this.from = from;
        this.to = to;
        this.logs = logs;
        this.logsBloom = logsBloom;
    }

    public String getTransactionHash() {
        return this.transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getTransactionIndexString() {
        return this.transactionIndex;
    }

    public String getTransactionIndexRaw() {
        return this.transactionIndex;
    }

    public void setTransactionIndex(String transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getBlockHash() {
        return this.blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getBlockNumberString() {
        return this.blockNumber;
    }

    public String getBlockNumberRaw() {
        return this.blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getCumulativeGasUsedString() {
        return this.cumulativeGasUsed;
    }

    public String getCumulativeGasUsedRaw() {
        return this.cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(String cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    public String getGasUsedString() {
        return this.gasUsed;
    }

    public String getGasUsedRaw() {
        return this.gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getContractAddress() {
        return this.contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getRoot() {
        return this.root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<Log> getLogs() {
        return this.logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }

    public String getLogsBloom() {
        return this.logsBloom;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof PlainTransactionReceipt)) {
            return false;
        } else {
            PlainTransactionReceipt that = (PlainTransactionReceipt)o;
            if (this.getTransactionHash() != null) {
                if (!this.getTransactionHash().equals(that.getTransactionHash())) {
                    return false;
                }
            } else if (that.getTransactionHash() != null) {
                return false;
            }

            label153: {
                if (this.transactionIndex != null) {
                    if (this.transactionIndex.equals(that.transactionIndex)) {
                        break label153;
                    }
                } else if (that.transactionIndex == null) {
                    break label153;
                }

                return false;
            }

            if (this.getBlockHash() != null) {
                if (!this.getBlockHash().equals(that.getBlockHash())) {
                    return false;
                }
            } else if (that.getBlockHash() != null) {
                return false;
            }

            label139: {
                if (this.blockNumber != null) {
                    if (this.blockNumber.equals(that.blockNumber)) {
                        break label139;
                    }
                } else if (that.blockNumber == null) {
                    break label139;
                }

                return false;
            }

            if (this.cumulativeGasUsed != null) {
                if (!this.cumulativeGasUsed.equals(that.cumulativeGasUsed)) {
                    return false;
                }
            } else if (that.cumulativeGasUsed != null) {
                return false;
            }

            if (this.gasUsed != null) {
                if (!this.gasUsed.equals(that.gasUsed)) {
                    return false;
                }
            } else if (that.gasUsed != null) {
                return false;
            }

            label118: {
                if (this.getContractAddress() != null) {
                    if (this.getContractAddress().equals(that.getContractAddress())) {
                        break label118;
                    }
                } else if (that.getContractAddress() == null) {
                    break label118;
                }

                return false;
            }

            label111: {
                if (this.getRoot() != null) {
                    if (this.getRoot().equals(that.getRoot())) {
                        break label111;
                    }
                } else if (that.getRoot() == null) {
                    break label111;
                }

                return false;
            }

            if (this.getStatus() != null) {
                if (!this.getStatus().equals(that.getStatus())) {
                    return false;
                }
            } else if (that.getStatus() != null) {
                return false;
            }

            if (this.getFrom() != null) {
                if (!this.getFrom().equals(that.getFrom())) {
                    return false;
                }
            } else if (that.getFrom() != null) {
                return false;
            }

            label90: {
                if (this.getTo() != null) {
                    if (this.getTo().equals(that.getTo())) {
                        break label90;
                    }
                } else if (that.getTo() == null) {
                    break label90;
                }

                return false;
            }

            if (this.getLogs() != null) {
                if (!this.getLogs().equals(that.getLogs())) {
                    return false;
                }
            } else if (that.getLogs() != null) {
                return false;
            }

            return this.getLogsBloom() != null ? this.getLogsBloom().equals(that.getLogsBloom()) : that.getLogsBloom() == null;
        }
    }

    public int hashCode() {
        int result = this.getTransactionHash() != null ? this.getTransactionHash().hashCode() : 0;
        result = 31 * result + (this.transactionIndex != null ? this.transactionIndex.hashCode() : 0);
        result = 31 * result + (this.getBlockHash() != null ? this.getBlockHash().hashCode() : 0);
        result = 31 * result + (this.blockNumber != null ? this.blockNumber.hashCode() : 0);
        result = 31 * result + (this.cumulativeGasUsed != null ? this.cumulativeGasUsed.hashCode() : 0);
        result = 31 * result + (this.gasUsed != null ? this.gasUsed.hashCode() : 0);
        result = 31 * result + (this.getContractAddress() != null ? this.getContractAddress().hashCode() : 0);
        result = 31 * result + (this.getRoot() != null ? this.getRoot().hashCode() : 0);
        result = 31 * result + (this.getStatus() != null ? this.getStatus().hashCode() : 0);
        result = 31 * result + (this.getFrom() != null ? this.getFrom().hashCode() : 0);
        result = 31 * result + (this.getTo() != null ? this.getTo().hashCode() : 0);
        result = 31 * result + (this.getLogs() != null ? this.getLogs().hashCode() : 0);
        result = 31 * result + (this.getLogsBloom() != null ? this.getLogsBloom().hashCode() : 0);
        return result;
    }

    public String toString() {
        return "PlainTransactionReceipt{transactionHash='" + this.transactionHash + '\'' + ", transactionIndex='" + this.transactionIndex + '\'' + ", blockHash='" + this.blockHash + '\'' + ", blockNumber='" + this.blockNumber + '\'' + ", cumulativeGasUsed='" + this.cumulativeGasUsed + '\'' + ", gasUsed='" + this.gasUsed + '\'' + ", contractAddress='" + this.contractAddress + '\'' + ", root='" + this.root + '\'' + ", status='" + this.getStatus() + '\'' + ", from='" + this.from + '\'' + ", to='" + this.to + '\'' + ", logs=" + this.logs + ", logsBloom='" + this.logsBloom + '\'' + '}';
    }
}
