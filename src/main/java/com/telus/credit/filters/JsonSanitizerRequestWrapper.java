package com.telus.credit.filters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;

import com.google.json.JsonSanitizer;

public class JsonSanitizerRequestWrapper extends HttpServletRequestWrapper {
    private String body;

    public JsonSanitizerRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        InputStream is = request.getInputStream();
        this.body = JsonSanitizer.sanitize(IOUtils.toString(is));
        IOUtils.closeQuietly(is);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(this.body.getBytes(StandardCharsets.UTF_8));
        ServletInputStream sis = new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return bis.available() == 0;
            }

            @Override
            public boolean isReady() {
                return Boolean.TRUE;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                if (!isFinished()) {
                    try {
                        readListener.onDataAvailable();
                    } catch (IOException e) {
                        readListener.onError(e);
                    }
                } else {
                    try {
                        readListener.onAllDataRead();
                    } catch (IOException e) {
                        readListener.onError(e);
                    }
                }
            }

            @Override
            public int read() throws IOException {
                return bis.read();
            }
        };
        return sis;
    }
}
