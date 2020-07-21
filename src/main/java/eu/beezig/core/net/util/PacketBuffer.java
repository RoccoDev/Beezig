/*
 * Copyright (C) 2017-2020 Beezig Team
 *
 * This file is part of Beezig.
 *
 * Beezig is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beezig is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beezig.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.beezig.core.net.util;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PacketBuffer implements AutoCloseable {
    private ByteBuf internal;
    private int size;

    public PacketBuffer(ByteBuf internal) {
        this.internal = internal;
    }

    public ByteBuf getInternal() {
        return internal;
    }

    public void writeUUID(UUID uuid) {
        internal.writeLong(uuid.getLeastSignificantBits());
        internal.writeLong(uuid.getMostSignificantBits());
        size += 16;
    }

    public void writeString(String s) {
       writeByteArray(s.getBytes(StandardCharsets.UTF_8));
    }

    public void writeByteArray(byte[] bytes) {
        if(bytes.length > 255) throw new AssertionError("Byte array length must fit in a byte.");
        internal.writeByte(bytes.length);
        internal.writeBytes(bytes);
        size += bytes.length + 1;
    }

    public void writeEnum(Enum e) {
        writeInt(e.ordinal());
    }

    public void writeInt(int i) {
        internal.writeInt(i);
        size += 4;
    }

    public void writeLong(long l) {
        internal.writeLong(l);
        size += 8;
    }

    public void writeByte(byte b) {
        internal.writeByte(b);
        size++;
    }

    public UUID readUUID() {
        long lsb = internal.readLong();
        long msb = internal.readLong();
        return new UUID(msb, lsb);
    }

    public String readString() {
        return readString(internal.readByte());
    }

    public String readString(int length) {
        String string = internal.toString(internal.readerIndex(), length, StandardCharsets.UTF_8);
        internal.readerIndex(internal.readerIndex() + length);
        return string;
    }

    public byte[] readByteArray() {
        byte[] buf = new byte[internal.readInt()];
        internal.readBytes(buf);
        return buf;
    }

    public boolean readBoolean() {
        return readByte() == 1;
    }

    public long readLong() {
        return internal.readLong();
    }

    public <T extends Enum> T readEnum(Class<T> type) {
        return type.getEnumConstants()[internal.readInt()];
    }

    public int readInt() {
        return internal.readInt();
    }

    public byte readByte() {
        return internal.readByte();
    }

    public int getSize() {
        return size;
    }

    @Override
    public void close() throws Exception {
        internal.release();
    }
}
