package app.arcanum.crypto.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OutputMessageStream extends ByteArrayOutputStream {
	public void writeInt(int value) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putInt(value);
		write(buffer.array());
	}
	
	public void writeLong(long value) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putLong(value);
		write(buffer.array());
	}
	
	@Override
	public void close() {
		try {
			super.close();
		} catch (IOException e) {}
	}
}
