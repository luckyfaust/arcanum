package app.arcanum.crypto.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class InputMessageStream extends InputStream {
	final byte[] _msg;
	int _offset;
	
	public InputMessageStream(byte[] msg) {
		_msg = msg;
	}
	
	public int readInt() throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(_msg, _offset, 4);
		buffer.order(ByteOrder.BIG_ENDIAN);
		_offset += 4;
		return buffer.getInt();
	}
	
	public long readLong() throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(_msg, _offset, 8);
		buffer.order(ByteOrder.BIG_ENDIAN);
		_offset += 8;
		return buffer.getLong();
	}
	
	@Override
	public int read() throws IOException {
		return _msg[_offset++];		
	}

	@Override
	public void close() {
		try {
			super.close();
		} catch (IOException e) {}
	}
}