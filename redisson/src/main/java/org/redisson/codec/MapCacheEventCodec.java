/**
 * Copyright 2018 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.codec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import io.netty.buffer.ByteBuf;

/**
 * 
 * @author Nikita Koksharov
 *
 */
public class MapCacheEventCodec implements Codec {

    private final Codec codec;
    private final boolean isWindows;
    
    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        public Object decode(ByteBuf buf, State state) throws IOException {
            List<Object> result = new ArrayList<Object>(3);

            Object key = MapCacheEventCodec.this.decode(buf, state, codec.getMapKeyDecoder());
            result.add(key);

            Object value = MapCacheEventCodec.this.decode(buf, state, codec.getMapValueDecoder());
            result.add(value);
            
            if (buf.isReadable()) {
                Object oldValue = MapCacheEventCodec.this.decode(buf, state, codec.getMapValueDecoder());
                result.add(oldValue);
            }
            
            return result;
        }
    };

    public MapCacheEventCodec(Codec codec, boolean isWindows) {
        super();
        this.codec = codec;
        this.isWindows = isWindows;
    }

    @Override
    public Decoder<Object> getMapValueDecoder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Encoder getMapValueEncoder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Encoder getMapKeyEncoder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        throw new UnsupportedOperationException();
    }

    private Object decode(ByteBuf buf, State state, Decoder<?> decoder) throws IOException {
        int keyLen;
        if (isWindows) {
            keyLen = buf.readIntLE();
        } else {
            keyLen = (int) buf.readLongLE();
        }
        ByteBuf keyBuf = buf.readSlice(keyLen);
        Object key = decoder.decode(keyBuf, state);
        return key;
    }

}
