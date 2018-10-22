package jp.co.soramitsu.sora.deauth.security;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import lombok.NonNull;

/**
 * This is class copy-pasted from spring-test library, which delegates to {@link InputStream}
 * Required to allow reading request body from {@link javax.servlet.http.HttpServletRequest} more
 * than once
 *
 * Such a requirement is raised by Decentralized Authentication mechanism
 */
public class DelegatingServletInputStream extends ServletInputStream {

  private final InputStream sourceStream;

  private boolean finished = false;

  /**
   * Create a DelegatingServletInputStream for the given source stream.
   *
   * @param sourceStream the source stream (never {@code null})
   */
  public DelegatingServletInputStream(@NonNull InputStream sourceStream) {
    this.sourceStream = sourceStream;
  }

  /**
   * Return the underlying source stream (never {@code null}).
   */
  public final InputStream getSourceStream() {
    return this.sourceStream;
  }

  @Override
  public int read() throws IOException {
    int data = this.sourceStream.read();
    if (data == -1) {
      this.finished = true;
    }
    return data;
  }

  @Override
  public int available() throws IOException {
    return this.sourceStream.available();
  }

  @Override
  public void close() throws IOException {
    super.close();
    this.sourceStream.close();
  }

  @Override
  public boolean isFinished() {
    return this.finished;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener readListener) {
    throw new UnsupportedOperationException();
  }
}