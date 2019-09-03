package com.chaion.makkiiserver.blockchain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.util.Optional;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.Response;

/**
 *
 */
public class AlethEthGetTransactionReceipt extends Response<PlainTransactionReceipt> {
    public AlethEthGetTransactionReceipt() {
    }

    public Optional<PlainTransactionReceipt> getTransactionReceipt() {
        return Optional.ofNullable(this.getResult());
    }

    public static class ResponseDeserialiser extends JsonDeserializer<PlainTransactionReceipt> {
        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        public ResponseDeserialiser() {
        }

        public PlainTransactionReceipt deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return jsonParser.getCurrentToken() != JsonToken.VALUE_NULL ? (PlainTransactionReceipt)this.objectReader.readValue(jsonParser, PlainTransactionReceipt.class) : null;
        }
    }
}
